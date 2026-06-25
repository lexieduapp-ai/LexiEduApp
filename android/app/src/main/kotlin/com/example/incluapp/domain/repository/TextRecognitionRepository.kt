package com.example.incluapp.domain.repository

import com.example.incluapp.domain.model.OcrResult

interface TextRecognitionRepository {
    suspend fun extractTextFromImage(imageUri: String): OcrResult
}
