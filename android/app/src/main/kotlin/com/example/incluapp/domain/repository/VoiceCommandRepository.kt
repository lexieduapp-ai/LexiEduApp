package com.example.incluapp.domain.repository

import com.example.incluapp.domain.model.VoiceCommand

interface VoiceCommandRepository {
    /** Escucha una sola vez y devuelve el comando reconocido. */
    suspend fun listenOnce(): VoiceCommand

    /** Devuelve true si el reconocimiento de voz está disponible en el dispositivo. */
    fun isAvailable(): Boolean
}
