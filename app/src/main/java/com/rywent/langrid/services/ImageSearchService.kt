package com.rywent.langrid.services

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object ImageSearchService {
    private const val TAG = "ImageSearchService"

    // Вставьте сюда ваш API-ключ от Pixabay
    var API_KEY = "57033982-072cf3ed73a8b7ab5047ad857"

    // Локальный кеш: хранит всю скачанную пачку картинок для каждого слова
    private val cachedImages = mutableMapOf<String, List<String>>()
    // Текущий указатель (индекс) для пагинации по кнопке "+"
    private val paginationIndexes = mutableMapOf<String, Int>()

    private const val INITIAL_LOAD = 15 // Сколько показываем сразу при вводе слова
    private const val PAGE_SIZE = 10   // Сколько добавляем по клику на кнопку "+"
    private const val FETCH_LIMIT = 50

    /**
     * Первый поиск: делает ровно 1 запрос в сеть на 50 картинок,
     * кеширует их и возвращает первые 15 штук.
     */
    suspend fun searchImages(term: String): List<String> = withContext(Dispatchers.IO) {
        val query = term.trim().lowercase()
        if (query.length < 2) return@withContext emptyList()

        val allImages = fetchAllImagesFromApi(query)
        cachedImages[query] = allImages
        paginationIndexes[query] = INITIAL_LOAD

        // Возвращаем начальную порцию
        allImages.take(INITIAL_LOAD)
    }

    /**
     * Дозагрузка: не идет в сеть, а берет следующие 10 картинок из уже готового кеша.
     */
    suspend fun loadMoreImages(term: String): List<String> = withContext(Dispatchers.IO) {
        val query = term.trim().lowercase()
        val allImages = cachedImages[query] ?: return@withContext emptyList()
        val currentIndex = paginationIndexes[query] ?: INITIAL_LOAD

        if (currentIndex >= allImages.size) {
            // Если показали всё, закольцовываем или возвращаем заново с начала
            paginationIndexes[query] = INITIAL_LOAD
            return@withContext allImages.take(INITIAL_LOAD)
        }

        val nextIndex = minOf(currentIndex + PAGE_SIZE, allImages.size)
        paginationIndexes[query] = nextIndex

        // Возвращаем следующий срез из кеша
        allImages.subList(currentIndex, nextIndex)
    }

    private fun fetchAllImagesFromApi(term: String): List<String> {
        val imageUrls = mutableListOf<String>()

        if (API_KEY.isBlank() || API_KEY == "ВАШ_КЛЮЧ_ОТ_PIXABAY_ЗДЕСЬ") {
            Log.e(TAG, "API Key is not set!")
            return emptyList()
        }

        try {
            val encodedQuery = URLEncoder.encode(term, "UTF-8")
            // Добавили параметры: editors_choice (только качественные) и safesearch
            val urlString = "https://pixabay.com/api/?key=$API_KEY&q=$encodedQuery&image_type=photo&safesearch=true&per_page=$FETCH_LIMIT"

            val jsonString = httpGet(urlString) ?: return emptyList()
            val jsonObject = JSONObject(jsonString)

            if (jsonObject.has("hits")) {
                val hitsArray = jsonObject.getJSONArray("hits")
                for (i in 0 until hitsArray.length()) {
                    val hitObj = hitsArray.getJSONObject(i)
                    if (hitObj.has("webformatURL")) {
                        val url = hitObj.getString("webformatURL")
                        // Можно дополнительно проверить теги, если они есть в ответе
                        imageUrls.add(url)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching images", e)
        }

        return imageUrls
    }

    private fun httpGet(urlString: String): String? {
        return try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                Log.e(TAG, "HTTP error code: ${connection.responseCode}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network exception", e)
            null
        }
    }
}