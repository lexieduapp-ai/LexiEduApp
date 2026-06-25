package com.example.incluapp.domain.usecase

import com.example.incluapp.domain.model.Reading
import com.example.incluapp.domain.repository.ReadingRepository

class SaveReadingUseCase(private val repository: ReadingRepository) {
    suspend operator fun invoke(reading: Reading): Long = repository.saveReading(reading)
}
