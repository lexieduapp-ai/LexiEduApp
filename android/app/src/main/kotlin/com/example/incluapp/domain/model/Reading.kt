package com.example.incluapp.domain.model

data class Reading(
    val id              : Long         = 0L,
    val extractedText   : String,
    val imagePath       : String,
    val processingTimeMs: Long         = 0L,
    val createdAt       : Long         = System.currentTimeMillis(),
    val title           : String,
    val summary         : String       = "",
    val simplifiedText  : String       = "",
    val keyPoints       : List<String> = emptyList(),
    val wordCount       : Int          = 0,
    val scanQualityScore: Int          = 100,
    val requiresReview  : Boolean      = false,
    val scanWarnings    : List<String> = emptyList(),
    val syncStatus      : SyncStatus   = SyncStatus.PENDING,
    val remoteId        : String?      = null,
    val updatedAt       : Long         = System.currentTimeMillis(),
    val lastSyncAttemptAt: Long?       = null
)
