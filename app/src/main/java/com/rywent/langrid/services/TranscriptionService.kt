package com.rywent.langrid.services

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

sealed interface TranscriptionResult {
    data class Success(
        val transcription: String
    ) : TranscriptionResult

    data class Error(
        val message: String
    ) : TranscriptionResult

    data object NotFound : TranscriptionResult
}

object TranscriptionService {

    private const val TAG = "TranscriptionService"

    private const val CONNECT_TIMEOUT = 10_000
    private const val READ_TIMEOUT = 10_000

    suspend fun fetchTranscription(
        languageCode: String,
        word: String
    ): TranscriptionResult {
        return withContext(Dispatchers.IO) {
            val cleanWord = word.trim()
            val cleanLanguage = normalizeLanguageCode(languageCode)

            if (cleanWord.isBlank()) {
                return@withContext TranscriptionResult.NotFound
            }

            if (cleanLanguage == null) {
                return@withContext TranscriptionResult.Error(
                    "Unsupported language: $languageCode"
                )
            }

            try {
                Log.d(
                    TAG,
                    "Searching transcription: word='$cleanWord', language='$cleanLanguage'"
                )

                val dictionaryResult = fetchFromDictionaryApi(
                    languageCode = cleanLanguage,
                    word = cleanWord
                )

                if (dictionaryResult is TranscriptionResult.Success) {
                    return@withContext dictionaryResult
                }

                val wiktionaryResult = fetchFromWiktionary(
                    languageCode = cleanLanguage,
                    word = cleanWord
                )

                if (wiktionaryResult is TranscriptionResult.Success) {
                    return@withContext wiktionaryResult
                }

                TranscriptionResult.NotFound

            } catch (e: IOException) {
                Log.e(TAG, "Network error", e)

                TranscriptionResult.Error(
                    "Check your internet connection"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error", e)

                TranscriptionResult.Error(
                    "Failed to retrieve the transcript."
                )
            }
        }
    }

    private fun normalizeLanguageCode(
        languageCode: String
    ): String? {
        return when (languageCode.trim().lowercase()) {
            "en", "en-us", "en-gb" -> "en"
            "es", "es-es", "es-mx" -> "es"
            "de", "de-de" -> "de"
            "fr", "fr-fr" -> "fr"
            "it", "it-it" -> "it"
            "ru", "ru-ru" -> "ru"
            "pt", "pt-br", "pt-pt" -> "pt"
            "ja", "ja-jp" -> "ja"
            "zh", "zh-cn", "zh-tw" -> "zh"
            "ko", "ko-kr" -> "ko"
            "pl", "pl-pl" -> "pl"
            "uk", "uk-ua" -> "uk"
            "nl", "nl-nl" -> "nl"
            "tr", "tr-tr" -> "tr"
            "sv", "sv-se" -> "sv"
            "da", "da-dk" -> "da"
            "no", "no-no" -> "no"
            "fi", "fi-fi" -> "fi"
            "cs", "cs-cz" -> "cs"
            "ro", "ro-ro" -> "ro"

            else -> null
        }
    }

    private fun fetchFromDictionaryApi(
        languageCode: String,
        word: String
    ): TranscriptionResult {
        val encodedWord = encodePathSegment(word)

        val urlString =
            "https://api.dictionaryapi.dev/api/v2/entries/" +
                    "$languageCode/$encodedWord"

        Log.d(TAG, "Dictionary API URL: $urlString")

        val response = executeGet(urlString) ?: return TranscriptionResult.NotFound

        if (response.code !in 200..299) {
            Log.w(
                TAG,
                "Dictionary API returned HTTP ${response.code}"
            )

            return if (response.code >= 500) {
                TranscriptionResult.Error(
                    "The dictionary service is temporarily unavailable."
                )
            } else {
                TranscriptionResult.NotFound
            }
        }

        return try {
            val entries = JSONArray(response.body)

            for (entryIndex in 0 until entries.length()) {
                val entry = entries.optJSONObject(entryIndex)
                    ?: continue

                val directPhonetic = entry
                    .optString("phonetic")
                    .takeIf { it.isNotBlank() }

                if (directPhonetic != null) {
                    return TranscriptionResult.Success(
                        normalizeTranscription(directPhonetic)
                    )
                }

                val phonetics = entry.optJSONArray("phonetics")
                    ?: continue

                for (phoneticIndex in 0 until phonetics.length()) {
                    val phonetic = phonetics
                        .optJSONObject(phoneticIndex)
                        ?: continue

                    val transcription = phonetic
                        .optString("text")
                        .takeIf { it.isNotBlank() }

                    if (transcription != null) {
                        return TranscriptionResult.Success(
                            normalizeTranscription(transcription)
                        )
                    }
                }
            }

            TranscriptionResult.NotFound

        } catch (e: Exception) {
            Log.e(TAG, "Invalid Dictionary API response", e)
            TranscriptionResult.NotFound
        }
    }

    private fun fetchFromWiktionary(
        languageCode: String,
        word: String
    ): TranscriptionResult {
        val variants = listOf(
            word.trim(),
            word.trim().lowercase()
        ).distinct()

        for (variant in variants) {
            val encodedWord = encodePathSegment(variant)

            val urlString =
                "https://${getWiktionaryDomain(languageCode)}/w/api.php" +
                        "?action=parse" +
                        "&page=$encodedWord" +
                        "&prop=text" +
                        "&format=json" +
                        "&formatversion=2" +
                        "&redirects=1"

            Log.d(TAG, "Wiktionary URL: $urlString")

            val response = executeGet(
                urlString = urlString,
                userAgent = "LangridApp/1.0 (Android vocabulary app)"
            ) ?: continue

            if (response.code !in 200..299) {
                continue
            }

            try {
                val json = JSONObject(response.body)

                if (json.has("error")) {
                    Log.d(
                        TAG,
                        "Wiktionary page not found: $variant"
                    )
                    continue
                }

                val parseObject = json.optJSONObject("parse")
                    ?: continue

                val html = when {
                    parseObject.optString("text").isNotBlank() -> {
                        parseObject.optString("text")
                    }

                    parseObject.optJSONObject("text") != null -> {
                        parseObject
                            .optJSONObject("text")
                            ?.optString("*")
                            .orEmpty()
                    }

                    else -> {
                        ""
                    }
                }

                val transcription = extractIpaFromHtml(html)

                if (!transcription.isNullOrBlank()) {
                    Log.d(
                        TAG,
                        "Found Wiktionary transcription: $transcription"
                    )

                    return TranscriptionResult.Success(
                        transcription
                    )
                }

            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "Failed to parse Wiktionary response",
                    e
                )
            }
        }

        return TranscriptionResult.NotFound
    }

    private fun extractIpaFromHtml(
        html: String
    ): String? {
        if (html.isBlank()) {
            return null
        }

        val document = Jsoup.parse(html)

        val elements = document
            .select("span, div, li")
            .filter { element ->
                val classes = element.classNames()
                    .joinToString(" ")
                    .lowercase()

                classes.contains("ipa") ||
                        classes.contains("api") ||
                        classes.contains("phonetic")
            }

        for (element in elements) {
            val text = element.text()
                .replace('\u00A0', ' ')
                .trim()

            if (isValidTranscription(text)) {
                return text
            }
        }
        val rawIpaRegex = Regex(
            """(?:/|\[)[^/\[\]]{1,50}(?:/|\])"""
        )

        val rawMatches = rawIpaRegex.findAll(
            document.text()
        )

        for (match in rawMatches) {
            val text = match.value.trim()

            if (isValidTranscription(text)) {
                return text
            }
        }

        return null
    }

    private fun isValidTranscription(
        value: String
    ): Boolean {
        if (value.length !in 2..80) {
            return false
        }

        val lowerValue = value.lowercase()

        if (
            lowerValue.contains("upload") ||
            lowerValue.contains("http") ||
            lowerValue.contains("file:") ||
            lowerValue.contains("audio") ||
            lowerValue.contains("listen")
        ) {
            return false
        }

        return value.any { char ->
            char in IPA_CHARACTERS
        }
    }

    private fun normalizeTranscription(
        transcription: String
    ): String {
        return transcription
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun getWiktionaryDomain(
        languageCode: String
    ): String {
        return when (languageCode) {
            "zh" -> "zh.wiktionary.org"
            "ja" -> "ja.wiktionary.org"
            "ko" -> "ko.wiktionary.org"
            else -> "$languageCode.wiktionary.org"
        }
    }

    private fun encodePathSegment(
        value: String
    ): String {
        return URLEncoder
            .encode(value, Charsets.UTF_8.name())
            .replace("+", "%20")
    }

    private fun executeGet(
        urlString: String,
        userAgent: String = "LangridApp/1.0"
    ): HttpResponse? {
        val connection = URL(urlString)
            .openConnection() as HttpURLConnection

        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = CONNECT_TIMEOUT
            connection.readTimeout = READ_TIMEOUT
            connection.instanceFollowRedirects = true
            connection.setRequestProperty(
                "Accept",
                "application/json"
            )
            connection.setRequestProperty(
                "User-Agent",
                userAgent
            )

            val responseCode = connection.responseCode

            val stream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val body = stream
                ?.bufferedReader()
                ?.use { it.readText() }
                .orEmpty()

            HttpResponse(
                code = responseCode,
                body = body
            )

        } finally {
            connection.disconnect()
        }
    }

    private data class HttpResponse(
        val code: Int,
        val body: String
    )

    private const val IPA_CHARACTERS =
        "ɑɐɒæɓʙβɔɕçɗɖðʤəɘɚɛɜɝɞɟʝɡɢɣɤɦɧħɥɪɨɯʲʎɭɬɮɰɱɲɳɴŋɵøœɸɹɻɺɾɽʀʁɸʃʂʅʈθʉʊʋⱱʌɣχʐʑʒʓʔʕʢʡʦʧʨʤˈˌːˑ"
}