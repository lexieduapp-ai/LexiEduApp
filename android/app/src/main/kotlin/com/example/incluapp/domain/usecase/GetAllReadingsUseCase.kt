package com.example.incluapp.domain.usecase

import com.example.incluapp.domain.model.Reading
import com.example.incluapp.domain.repository.ReadingRepository
import kotlinx.coroutines.flow.Flow

class GetAllReadingsUseCase(private val repository: ReadingRepository) {
    operator fun invoke(): Flow<List<Reading>> = repository.getAllReadings()
}
