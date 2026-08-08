package com.rywent.langrid.data.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class VoiceNoteStorage(private val context: Context) {

    private val dir: File
        get() = File(context.filesDir, "voice_notes").also { if (!it.exists()) it.mkdirs() }

    suspend fun persist(sourcePath: String, noteId: String = UUID.randomUUID().toString()): String? =
        withContext(Dispatchers.IO) {
            try {
                val src = File(sourcePath)
                if (!src.exists()) return@withContext null
                val dest = File(dir, "$noteId.m4a")
                src.copyTo(dest, overwrite = true)
                if (src.absolutePath != dest.absolutePath) {
                    src.delete()
                }
                dest.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    fun delete(path: String?) {
        if (path.isNullOrBlank()) return
        runCatching { File(path).delete() }
    }

    fun deleteById(noteId: String) {
        File(dir, "$noteId.m4a").delete()
    }
}