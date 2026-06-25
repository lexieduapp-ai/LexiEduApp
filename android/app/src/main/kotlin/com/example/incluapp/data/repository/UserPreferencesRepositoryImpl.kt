package com.example.incluapp.data.repository

import com.example.incluapp.data.local.dao.UserPreferencesDao
import com.example.incluapp.data.local.entity.UserPreferencesEntity
import com.example.incluapp.domain.model.UserPreferences
import com.example.incluapp.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferencesRepositoryImpl(
    private val dao: UserPreferencesDao
) : UserPreferencesRepository {

    override fun getUserPreferences(): Flow<UserPreferences> =
        dao.getUserPreferences().map { entity -> entity?.toDomain() ?: UserPreferences() }

    override suspend fun saveUserPreferences(preferences: UserPreferences) =
        dao.saveUserPreferences(preferences.toEntity())

    override suspend fun updateSpeechRate(rate: Float) {
        val updated = dao.updateSpeechRate(rate)
        if (updated == 0) {
            dao.saveUserPreferences(UserPreferencesEntity(speechRate = rate))
        }
    }

    override suspend fun updateFontSize(size: Float) {
        val updated = dao.updateFontSize(size)
        if (updated == 0) {
            dao.saveUserPreferences(UserPreferencesEntity(fontSize = size))
        }
    }

    override suspend fun updateScreenBrightness(brightness: Float) {
        val updated = dao.updateScreenBrightness(brightness)
        if (updated == 0) {
            dao.saveUserPreferences(UserPreferencesEntity(screenBrightness = brightness))
        }
    }

    // ── Mappers ────────────────────────────────────────────────────────────

    private fun UserPreferencesEntity.toDomain() = UserPreferences(
        speechRate          = speechRate,
        speechPitch         = speechPitch,
        fontSize            = fontSize,
        highContrastEnabled = highContrastEnabled,
        screenBrightness    = screenBrightness
    )

    private fun UserPreferences.toEntity() = UserPreferencesEntity(
        speechRate          = speechRate,
        speechPitch         = speechPitch,
        fontSize            = fontSize,
        highContrastEnabled = highContrastEnabled,
        screenBrightness    = screenBrightness
    )
}
