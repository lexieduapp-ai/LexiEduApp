package com.example.incluapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "readings")
data class ReadingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title           : String,
    val extractedText   : String,
    val imagePath       : String,
    val processingTimeMs: Long,
    val createdAt       : Long,
    val summary         : String = "",
    val simplifiedText  : String = "",
    val keyPoints       : String = "",
    val wordCount       : Int    = 0,
    val scanQualityScore: Int    = 100,
    val requiresReview  : Boolean = false,
    val scanWarnings    : String = "",
    val syncStatus      : String = "PENDING",
    val remoteId        : String? = null,
    val updatedAt       : Long   = System.currentTimeMillis(),
    val lastSyncAttemptAt: Long? = null
)
