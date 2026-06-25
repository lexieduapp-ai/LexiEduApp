package com.example.incluapp.domain.usecase

import com.example.incluapp.domain.repository.TextRecognitionRepository

class RecognizeTextUseCase(
    private val repository: TextRecognitionRepository
) {
    suspend operator fun invoke(imageUri: String) =
        repository.extractTextFromImage(imageUri)
}
