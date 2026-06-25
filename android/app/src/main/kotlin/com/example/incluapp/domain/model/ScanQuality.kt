package com.example.incluapp.domain.model

data class ScanQuality(
    val score: Int,
    val requiresReview: Boolean,
    val warnings: List<String>
)
