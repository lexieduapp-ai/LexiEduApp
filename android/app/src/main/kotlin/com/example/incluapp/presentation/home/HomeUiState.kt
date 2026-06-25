package com.example.incluapp.presentation.home

data class HomeUiState(
    val totalReadings: Int = 0,
    val pendingSyncCount: Int = 0,
    val isOnline: Boolean = false,
    val syncConfigured: Boolean = false,
    val isSyncing: Boolean = false,
    val message: String? = null
)
