package com.example.incluapp.domain.repository

import com.example.incluapp.domain.model.Reading
import kotlinx.coroutines.flow.Flow

/** Contrato de dominio para el acceso al historial de lecturas. */
interface ReadingRepository {
    fun getAllReadings(): Flow<List<Reading>>
    suspend fun getReadingById(id: Long): Reading?
    suspend fun getPendingSyncReadings(): List<Reading>
    suspend fun saveReading(reading: Reading): Long
    suspend fun deleteReading(id: Long)
    suspend fun getTotalCount(): Int
    suspend fun getPendingSyncCount(): Int
    suspend fun markSynced(id: Long, remoteId: String, syncedAt: Long = System.currentTimeMillis())
    suspend fun markSyncFailed(id: Long, attemptedAt: Long = System.currentTimeMillis())
}
