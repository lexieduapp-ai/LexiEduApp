package com.example.incluapp.domain.usecase

import com.example.incluapp.domain.repository.ReadingRepository

class DeleteReadingUseCase(private val repository: ReadingRepository) {
    suspend operator fun invoke(id: Long) = repository.deleteReading(id)
}
