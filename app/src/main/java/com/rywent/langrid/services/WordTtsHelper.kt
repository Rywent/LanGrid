package com.rywent.langrid.services

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import java.util.UUID

class WordTtsHelper(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var ready = false
    private var queue: List<String> = emptyList()
    private var index = 0
    private var onDoneAll: (() -> Unit)? = null
    private var speechRate: Float = 1.0f

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        if (ready) {
            tts?.setSpeechRate(speechRate)
        }
    }

    fun setSpeechRate(rate: Float) {
        speechRate = rate.coerceIn(0.25f, 2.5f)
        tts?.setSpeechRate(speechRate)
    }

    fun getSpeechRate(): Float = speechRate

    fun speakSequence(
        languageCode: String,
        phrases: List<String>,
        onDone: () -> Unit
    ) {
        if (!ready) {
            onDone()
            return
        }
        val clean = phrases.map { it.trim() }.filter { it.isNotEmpty() }
        if (clean.isEmpty()) {
            onDone()
            return
        }

        stop()
        queue = clean
        index = 0
        onDoneAll = onDone

        tts?.language = localeFromCode(languageCode)
        tts?.setSpeechRate(speechRate)

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = Unit

            override fun onDone(utteranceId: String?) {
                index++
                if (index < queue.size) {
                    speakNext()
                } else {
                    onDoneAll?.invoke()
                    onDoneAll = null
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                onDoneAll?.invoke()
                onDoneAll = null
            }
        })

        speakNext()
    }

    private fun speakNext() {
        val text = queue.getOrNull(index) ?: return
        val id = UUID.randomUUID().toString()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, Bundle(), id)
    }

    fun stop() {
        tts?.stop()
        queue = emptyList()
        index = 0
        onDoneAll?.invoke()
        onDoneAll = null
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
        ready = false
    }

    private fun localeFromCode(code: String): Locale {
        return when (code.lowercase()) {
            "en" -> Locale.US
            "en-gb", "en_gb" -> Locale.UK
            "ru" -> Locale("ru", "RU")
            "de" -> Locale.GERMANY
            "fr" -> Locale.FRANCE
            "es" -> Locale("es", "ES")
            "it" -> Locale.ITALY
            "pt" -> Locale("pt", "BR")
            "zh" -> Locale.CHINA
            "ja" -> Locale.JAPAN
            "ko" -> Locale.KOREA
            else -> {
                val parts = code.replace('_', '-').split('-')
                if (parts.size >= 2) Locale(parts[0], parts[1])
                else Locale(parts[0])
            }
        }
    }
}