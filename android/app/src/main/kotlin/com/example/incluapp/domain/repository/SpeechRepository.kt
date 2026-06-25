package com.example.incluapp.domain.repository

interface SpeechRepository {
    suspend fun speak(text: String, speechRate: Float, pitch: Float = 1.0f): Boolean
    suspend fun pause()
    suspend fun stop()
    fun shutdown()
}
