package com.example.incluapp.domain.repository

import com.example.incluapp.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

/** Contrato de dominio para las preferencias de accesibilidad del usuario. */
interface UserPreferencesRepository {
    fun getUserPreferences(): Flow<UserPreferences>
    suspend fun saveUserPreferences(preferences: UserPreferences)
    suspend fun updateSpeechRate(rate: Float)
    suspend fun updateFontSize(size: Float)
    suspend fun updateScreenBrightness(brightness: Float)
}
