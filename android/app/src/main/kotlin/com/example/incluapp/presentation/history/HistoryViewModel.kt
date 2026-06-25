package com.example.incluapp.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.incluapp.domain.repository.ReadingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val readingRepository: ReadingRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    val uiState = combine(
        readingRepository.getAllReadings(),
        searchQuery
    ) { readings, query ->
        HistoryUiState(
            readings = readings,
            searchQuery = query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState()
    )

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun deleteReading(id: Long) {
        viewModelScope.launch {
            readingRepository.deleteReading(id)
        }
    }

    companion object {
        fun factory(
            readingRepository: ReadingRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                HistoryViewModel(readingRepository) as T
        }
    }
}
