package com.example.incluapp.domain.repository

import com.example.incluapp.domain.model.SyncResult

interface ReadingSyncRepository {
    val isConfigured: Boolean
    suspend fun syncPendingReadings(): SyncResult
}
