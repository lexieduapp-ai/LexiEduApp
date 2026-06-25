package com.example.incluapp.data.repository

import com.example.incluapp.data.local.dao.ReadingDao
import com.example.incluapp.data.local.entity.ReadingEntity
import com.example.incluapp.domain.model.Reading
import com.example.incluapp.domain.model.SyncStatus
import com.example.incluapp.domain.repository.ReadingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReadingRepositoryImpl(
    private val dao: ReadingDao
) : ReadingRepository {

    override fun getAllReadings(): Flow<List<Reading>> =
        dao.getAllReadings().map { list -> list.map { it.toDomain() } }

    override suspend fun getReadingById(id: Long): Reading? =
        dao.getReadingById(id)?.toDomain()

    override suspend fun getPendingSyncReadings(): List<Reading> =
        dao.getPendingSyncReadings().map { it.toDomain() }

    override suspend fun saveReading(reading: Reading): Long =
        dao.insertReading(reading.toEntity())

    override suspend fun deleteReading(id: Long) =
        dao.deleteReadingById(id)

    override suspend fun getTotalCount(): Int =
        dao.getTotalCount()

    override suspend fun getPendingSyncCount(): Int =
        dao.getPendingSyncCount()

    override suspend fun markSynced(id: Long, remoteId: String, syncedAt: Long) =
        dao.markSynced(id = id, remoteId = remoteId, syncedAt = syncedAt)

    override suspend fun markSyncFailed(id: Long, attemptedAt: Long) =
        dao.markSyncFailed(id = id, attemptedAt = attemptedAt)

    private fun ReadingEntity.toDomain() = Reading(
        id = id,
        title = title,
        extractedText = extractedText,
        imagePath = imagePath,
        processingTimeMs = processingTimeMs,
        createdAt = createdAt,
        summary = summary,
        simplifiedText = simplifiedText,
        keyPoints = keyPoints
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() },
        wordCount = wordCount,
        scanQualityScore = scanQualityScore,
        requiresReview = requiresReview,
        scanWarnings = scanWarnings
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() },
        syncStatus = runCatching { SyncStatus.valueOf(syncStatus) }
            .getOrDefault(SyncStatus.PENDING),
        remoteId = remoteId,
        updatedAt = updatedAt,
        lastSyncAttemptAt = lastSyncAttemptAt
    )

    private fun Reading.toEntity() = ReadingEntity(
        id = id,
        title = title,
        extractedText = extractedText,
        imagePath = imagePath,
        processingTimeMs = processingTimeMs,
        createdAt = createdAt,
        summary = summary,
        simplifiedText = simplifiedText,
        keyPoints = keyPoints.joinToString("\n"),
        wordCount = wordCount,
        scanQualityScore = scanQualityScore,
        requiresReview = requiresReview,
        scanWarnings = scanWarnings.joinToString("\n"),
        syncStatus = syncStatus.name,
        remoteId = remoteId,
        updatedAt = updatedAt,
        lastSyncAttemptAt = lastSyncAttemptAt
    )
}
