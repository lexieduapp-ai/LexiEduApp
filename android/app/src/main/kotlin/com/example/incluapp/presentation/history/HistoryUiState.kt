package com.example.incluapp.presentation.history

import com.example.incluapp.domain.model.Reading

data class HistoryUiState(
    val readings: List<Reading> = emptyList(),
    val searchQuery: String = ""
) {
    val filteredReadings: List<Reading>
        get() = if (searchQuery.isBlank()) {
            readings
        } else {
            readings.filter { reading ->
                reading.title.contains(searchQuery, ignoreCase = true) ||
                    reading.extractedText.contains(searchQuery, ignoreCase = true) ||
                    reading.summary.contains(searchQuery, ignoreCase = true)
            }
        }
}
