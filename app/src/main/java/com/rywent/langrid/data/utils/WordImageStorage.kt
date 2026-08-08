package com.rywent.langrid.data.utils

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class WordImageStorage(private val context: Context) {

    private val dir: File
        get() = File(context.filesDir, "word_images").also { if (!it.exists()) it.mkdirs() }


    suspend fun persist(wordId: String, source: String): String? = withContext(Dispatchers.IO) {
        try {
            val dest = File(dir, "$wordId.jpg")

            when {
                source.startsWith(dir.absolutePath) || source.startsWith("file://${dir.absolutePath}") ->
                    source.removePrefix("file://")

                source.startsWith("content://") || source.startsWith("file://") -> {
                    context.contentResolver.openInputStream(Uri.parse(source))?.use { input ->
                        FileOutputStream(dest).use { output -> input.copyTo(output) }
                    }
                    dest.absolutePath
                }

                source.startsWith("http://") || source.startsWith("https://") -> {
                    val conn = (URL(source).openConnection() as HttpURLConnection).apply {
                        connectTimeout = 15_000
                        readTimeout = 20_000
                        instanceFollowRedirects = true
                    }
                    conn.inputStream.use { input ->
                        FileOutputStream(dest).use { output -> input.copyTo(output) }
                    }
                    conn.disconnect()
                    dest.absolutePath
                }

                File(source).exists() -> {
                    File(source).copyTo(dest, overwrite = true)
                    dest.absolutePath
                }

                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun delete(wordId: String) {
        File(dir, "$wordId.jpg").delete()
        dir.listFiles()?.filter { it.nameWithoutExtension == wordId }?.forEach { it.delete() }
    }
}