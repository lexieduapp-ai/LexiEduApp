package com.example.incluapp.domain.model

data class OcrResult(
    val text: String,
    val processingTimeMs: Long,
    val wordCount: Int,
    val quality: ScanQuality
)
