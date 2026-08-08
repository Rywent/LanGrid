package com.rywent.langrid.services

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder

object OpenverseImageSearchService {

    private const val TAG = "OpenverseImageSearch"
    private const val BASE_URL = "https://api.openverse.org/v1/images/"

    private val cachedImages = mutableMapOf<String, MutableList<String>>()
    private val paginationIndexes = mutableMapOf<String, Int>()
    private val nextApiPage = mutableMapOf<String, Int>()

    private const val INITIAL_LOAD = 15
    private const val PAGE_SIZE = 10
    private const val FETCH_LIMIT = 20

    private const val CONNECT_TIMEOUT_MS = 15_000
    private const val READ_TIMEOUT_MS = 20_000
    private const val MAX_RETRIES = 2

    suspend fun searchImages(term: String): List<String> = withContext(Dispatchers.IO) {
        val query = term.trim().lowercase()
        if (query.length < 2) return@withContext emptyList()

        cachedImages[query]?.takeIf { it.isNotEmpty() }?.let { cached ->
            paginationIndexes[query] = minOf(INITIAL_LOAD, cached.size)
            return@withContext cached.take(INITIAL_LOAD)
        }

        val page1 = fetchPage(query, page = 1)
        cachedImages[query] = page1.toMutableList()
        nextApiPage[query] = 2
        paginationIndexes[query] = minOf(INITIAL_LOAD, page1.size)

        page1.take(INITIAL_LOAD)
    }

    suspend fun loadMoreImages(term: String): List<String> = withContext(Dispatchers.IO) {
        val query = term.trim().lowercase()
        val cache = cachedImages[query] ?: return@withContext emptyList()
        val currentIndex = paginationIndexes[query] ?: INITIAL_LOAD

        if (currentIndex < cache.size) {
            val nextIndex = minOf(currentIndex + PAGE_SIZE, cache.size)
            paginationIndexes[query] = nextIndex
            return@withContext cache.subList(currentIndex, nextIndex).toList()
        }

        val page = nextApiPage[query] ?: 2
        val more = fetchPage(query, page = page)
        if (more.isEmpty()) return@withContext emptyList()

        cache.addAll(more.filter { it !in cache })
        nextApiPage[query] = page + 1

        val nextIndex = minOf(currentIndex + PAGE_SIZE, cache.size)
        paginationIndexes[query] = nextIndex
        cache.subList(currentIndex, nextIndex).toList()
    }

    private suspend fun fetchPage(term: String, page: Int): List<String> {
        var lastError: Exception? = null

        repeat(MAX_RETRIES + 1) { attempt ->
            try {
                val encoded = URLEncoder.encode(term, "UTF-8")
                val urlString =
                    "$BASE_URL?q=$encoded&page_size=$FETCH_LIMIT&page=$page&mature=false"

                val jsonString = httpGet(urlString) ?: return emptyList()
                val results = JSONObject(jsonString).optJSONArray("results") ?: return emptyList()

                val imageUrls = mutableListOf<String>()
                for (i in 0 until results.length()) {
                    val item = results.getJSONObject(i)
                    val url = item.optString("url").takeIf { it.isNotBlank() }
                        ?: item.optString("thumbnail").takeIf { it.isNotBlank() }
                    if (url != null) imageUrls.add(url)
                }
                return imageUrls.distinct()
            } catch (e: SocketTimeoutException) {
                lastError = e
                Log.w(TAG, "Timeout page=$page attempt=${attempt + 1}, retry...")
                delay(400L * (attempt + 1))
            } catch (e: Exception) {
                lastError = e
                Log.e(TAG, "Error page=$page attempt=${attempt + 1}", e)
                delay(300L * (attempt + 1))
            }
        }

        Log.e(TAG, "Failed after retries", lastError)
        return emptyList()
    }

    private fun httpGet(urlString: String): String? {
        var connection: HttpURLConnection? = null
        return try {
            connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                instanceFollowRedirects = true
                setRequestProperty("Accept", "application/json")
                setRequestProperty(
                    "User-Agent",
                    "Langrid/1.0 (language-learning; https://github.com/Rywent)"
                )
            }

            val code = connection.responseCode
            if (code == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                val errorBody = try {
                    connection.errorStream?.bufferedReader()?.use { it.readText() }
                } catch (_: Exception) {
                    null
                }
                Log.e(TAG, "HTTP $code ${errorBody.orEmpty()}")
                null
            }
        } finally {
            connection?.disconnect()
        }
    }

    fun clearCache() {
        cachedImages.clear()
        paginationIndexes.clear()
        nextApiPage.clear()
    }
}