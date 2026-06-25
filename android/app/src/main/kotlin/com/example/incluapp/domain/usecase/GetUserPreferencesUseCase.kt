package com.example.incluapp.domain.usecase

import com.example.incluapp.domain.model.UserPreferences
import com.example.incluapp.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow

class GetUserPreferencesUseCase(private val repository: UserPreferencesRepository) {
    operator fun invoke(): Flow<UserPreferences> = repository.getUserPreferences()
}
