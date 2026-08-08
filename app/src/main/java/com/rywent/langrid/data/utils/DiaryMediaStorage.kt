package com.rywent.langrid.data.utils

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class DiaryMediaStorage(private val context: Context) {

    private val imagesDir: File
        get() = File(context.filesDir, "diary_images").also { if (!it.exists()) it.mkdirs() }

    private val voiceDir: File
        get() = File(context.filesDir, "voice_notes").also { if (!it.exists()) it.mkdirs() }

    fun isPersisted(path: String): Boolean {
        val p = path.removePrefix("file://")
        return p.startsWith(imagesDir.absolutePath) ||
                p.startsWith(voiceDir.absolutePath) ||
                (p.startsWith(context.filesDir.absolutePath) && File(p).exists())
    }

    suspend fun persistImage(source: String): String? = withContext(Dispatchers.IO) {
        try {
            if (source.isBlank()) return@withContext null
            if (isPersisted(source) && File(source.removePrefix("file://")).exists()) {
                return@withContext source.removePrefix("file://")
            }

            val dest = File(imagesDir, "${UUID.randomUUID()}.jpg")

            when {
                source.startsWith("content://") || source.startsWith("file://") -> {
                    context.contentResolver.openInputStream(Uri.parse(source))?.use { input ->
                        FileOutputStream(dest).use { output -> input.copyTo(output) }
                    } ?: return@withContext null
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

    suspend fun persistVoice(sourcePath: String, noteId: String = UUID.randomUUID().toString()): String? =
        withContext(Dispatchers.IO) {
            try {
                val src = File(sourcePath)
                if (!src.exists()) return@withContext null
                if (isPersisted(sourcePath)) return@withContext sourcePath.removePrefix("file://")
                val dest = File(voiceDir, "$noteId.m4a")
                src.copyTo(dest, overwrite = true)
                if (src.absolutePath != dest.absolutePath) src.delete()
                dest.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    fun deleteFile(path: String?) {
        if (path.isNullOrBlank()) return
        runCatching { File(path.removePrefix("file://")).delete() }
    }
}