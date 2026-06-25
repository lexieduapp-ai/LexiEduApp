package com.example.incluapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.incluapp.data.local.entity.ReadingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingDao {

    @Query("SELECT * FROM readings ORDER BY createdAt DESC")
    fun getAllReadings(): Flow<List<ReadingEntity>>

    @Query("SELECT * FROM readings WHERE id = :id")
    suspend fun getReadingById(id: Long): ReadingEntity?

    @Query("SELECT * FROM readings WHERE syncStatus IN ('PENDING', 'FAILED') ORDER BY updatedAt ASC")
    suspend fun getPendingSyncReadings(): List<ReadingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReading(reading: ReadingEntity): Long

    @Delete
    suspend fun deleteReading(reading: ReadingEntity)

    @Query("DELETE FROM readings WHERE id = :id")
    suspend fun deleteReadingById(id: Long)

    @Query("SELECT COUNT(*) FROM readings")
    suspend fun getTotalCount(): Int

    @Query("SELECT COUNT(*) FROM readings WHERE syncStatus IN ('PENDING', 'FAILED')")
    suspend fun getPendingSyncCount(): Int

    @Query(
        """
        UPDATE readings
        SET syncStatus = 'SYNCED',
            remoteId = :remoteId,
            lastSyncAttemptAt = :syncedAt
        WHERE id = :id
        """
    )
    suspend fun markSynced(id: Long, remoteId: String, syncedAt: Long)

    @Query(
        """
        UPDATE readings
        SET syncStatus = 'FAILED',
            lastSyncAttemptAt = :attemptedAt
        WHERE id = :id
        """
    )
    suspend fun markSyncFailed(id: Long, attemptedAt: Long)
}
