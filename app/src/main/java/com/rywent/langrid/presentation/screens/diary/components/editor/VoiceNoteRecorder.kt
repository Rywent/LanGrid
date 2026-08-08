package com.rywent.langrid.presentation.screens.diary.components.editor

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.util.UUID

class VoiceNoteRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var startedAt = 0L

    val isRecording: Boolean get() = recorder != null

    fun start(): Boolean {
        stopInternal(delete = true)
        val dir = File(context.filesDir, "voice_notes").apply { mkdirs() }
        val file = File(dir, "${UUID.randomUUID()}.m4a")
        return try {
            val r = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            r.setAudioSource(MediaRecorder.AudioSource.MIC)
            r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            r.setAudioEncodingBitRate(128_000)
            r.setAudioSamplingRate(44_100)
            r.setOutputFile(file.absolutePath)
            r.prepare()
            r.start()
            recorder = r
            outputFile = file
            startedAt = System.currentTimeMillis()
            true
        } catch (_: Exception) {
            file.delete()
            recorder = null
            outputFile = null
            false
        }
    }

    fun stop(): Pair<String, Long>? {
        val file = outputFile
        val duration = (System.currentTimeMillis() - startedAt).coerceAtLeast(0L)
        return try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            outputFile = null
            if (file != null && file.exists() && file.length() > 0) {
                file.absolutePath to duration
            } else {
                file?.delete()
                null
            }
        } catch (_: Exception) {
            recorder?.release()
            recorder = null
            outputFile = null
            file?.delete()
            null
        }
    }

    fun cancel() {
        stopInternal(delete = true)
    }

    fun amplitude(): Int {
        return try {
            recorder?.maxAmplitude ?: 0
        } catch (_: Exception) {
            0
        }
    }

    private fun stopInternal(delete: Boolean) {
        try {
            recorder?.apply {
                runCatching { stop() }
                release()
            }
        } catch (_: Exception) {
        }
        recorder = null
        if (delete) outputFile?.delete()
        outputFile = null
        startedAt = 0L
    }
}