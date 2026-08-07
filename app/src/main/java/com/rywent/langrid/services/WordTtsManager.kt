package com.rywent.langrid.services

import android.content.Context

object WordTtsManager {
    @Volatile
    private var instance: WordTtsHelper? = null

    fun get(context: Context): WordTtsHelper {
        return instance ?: synchronized(this) {
            instance ?: WordTtsHelper(context.applicationContext).also { instance = it }
        }
    }
}