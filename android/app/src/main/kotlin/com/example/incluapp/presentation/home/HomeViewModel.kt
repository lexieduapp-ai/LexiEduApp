package com.example.incluapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.incluapp.domain.model.SyncStatus
import com.example.incluapp.domain.repository.NetworkMonitor
import com.example.incluapp.domain.repository.ReadingRepository
import com.example.incluapp.domain.repository.ReadingSyncRepository
import com.example.incluapp.domain.usecase.SyncPendingReadingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    readingRepository: ReadingRepository,
    private val readingSyncRepository: ReadingSyncRepository,
    private val networkMonitor: NetworkMonitor,
    private val syncPendingReadingsUseCase: SyncPendingReadingsUseCase
) : ViewModel() {

    private val message = MutableStateFlow<String?>(null)
    private val isSyncing = MutableStateFlow(false)

    val uiState = combine(
        readingRepository.getAllReadings(),
        networkMonitor.isOnline,
        isSyncing,
        message
    ) { readings, online, syncing, currentMessage ->
        HomeUiState(
            totalReadings = readings.size,
            pendingSyncCount = readings.count {
                it.syncStatus == SyncStatus.PENDING || it.syncStatus == SyncStatus.FAILED
            },
            isOnline = online,
            syncConfigured = readingSyncRepository.isConfigured,
            isSyncing = syncing,
            message = currentMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(
            isOnline = networkMonitor.isCurrentlyOnline(),
            syncConfigured = readingSyncRepository.isConfigured
        )
    )

    init {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { online ->
                if (online && readingSyncRepository.isConfigured) {
                    syncPendingReadings(showResult = false)
                }
            }
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            syncPendingReadings(showResult = true)
        }
    }

    private suspend fun syncPendingReadings(showResult: Boolean) {
        if (isSyncing.value) return

        isSyncing.value = true
        val result = syncPendingReadingsUseCase()
        isSyncing.value = false

        if (showResult || result.uploaded > 0 || result.failed > 0) {
            message.value = result.message
        }
    }

    fun onCameraPermissionDenied() {
        message.value = "Permiso de camara denegado."
    }

    fun onImageSelectionFailed() {
        message.value = "No se pudo abrir la imagen seleccionada."
    }

    fun clearMessage() {
        message.value = null
    }

    companion object {
        fun factory(
            readingRepository: ReadingRepository,
            readingSyncRepository: ReadingSyncRepository,
            networkMonitor: NetworkMonitor,
            syncPendingReadingsUseCase: SyncPendingReadingsUseCase
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                HomeViewModel(
                    readingRepository = readingRepository,
                    readingSyncRepository = readingSyncRepository,
                    networkMonitor = networkMonitor,
                    syncPendingReadingsUseCase = syncPendingReadingsUseCase
                ) as T
        }
    }
}
