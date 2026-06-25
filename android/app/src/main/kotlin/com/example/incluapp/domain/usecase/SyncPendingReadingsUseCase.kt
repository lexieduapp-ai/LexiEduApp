package com.example.incluapp.domain.usecase

import com.example.incluapp.domain.repository.ReadingSyncRepository

class SyncPendingReadingsUseCase(
    private val repository: ReadingSyncRepository
) {
    suspend operator fun invoke() = repository.syncPendingReadings()
}
