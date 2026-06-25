package com.example.incluapp.data.repository

import com.example.incluapp.data.remote.ReadingRemoteDataSource
import com.example.incluapp.domain.model.SyncResult
import com.example.incluapp.domain.repository.NetworkMonitor
import com.example.incluapp.domain.repository.ReadingRepository
import com.example.incluapp.domain.repository.ReadingSyncRepository

class ReadingSyncRepositoryImpl(
    private val readingRepository: ReadingRepository,
    private val remoteDataSource: ReadingRemoteDataSource,
    private val networkMonitor: NetworkMonitor
) : ReadingSyncRepository {

    override val isConfigured: Boolean
        get() = remoteDataSource.isConfigured

    override suspend fun syncPendingReadings(): SyncResult {
        val pending = readingRepository.getPendingSyncReadings()
        if (pending.isEmpty()) {
            return SyncResult(message = "No hay lecturas pendientes.")
        }

        if (!remoteDataSource.isConfigured) {
            return SyncResult(
                skipped = pending.size,
                message = "Sin endpoint web configurado."
            )
        }

        if (!networkMonitor.isCurrentlyOnline()) {
            return SyncResult(
                skipped = pending.size,
                message = "Sin conexion. Se mantiene la cola local."
            )
        }

        var uploaded = 0
        var failed = 0
        val attemptedAt = System.currentTimeMillis()

        pending.forEach { reading ->
            runCatching {
                remoteDataSource.uploadReading(reading)
            }.onSuccess { result ->
                readingRepository.markSynced(
                    id = reading.id,
                    remoteId = result.remoteId,
                    syncedAt = System.currentTimeMillis()
                )
                uploaded += 1
            }.onFailure {
                readingRepository.markSyncFailed(
                    id = reading.id,
                    attemptedAt = attemptedAt
                )
                failed += 1
            }
        }

        return SyncResult(
            uploaded = uploaded,
            failed = failed,
            message = "$uploaded sincronizadas, $failed con error."
        )
    }
}
