package com.example.incluapp.domain.model

data class SyncResult(
    val uploaded: Int = 0,
    val failed: Int = 0,
    val skipped: Int = 0,
    val message: String = ""
)
