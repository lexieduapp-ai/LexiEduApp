package com.example.incluapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.incluapp.data.local.entity.UserPreferencesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPreferencesDao {

    @Query("SELECT * FROM user_preferences WHERE id = 1")
    fun getUserPreferences(): Flow<UserPreferencesEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserPreferences(preferences: UserPreferencesEntity)

    @Query("UPDATE user_preferences SET speechRate = :rate WHERE id = 1")
    suspend fun updateSpeechRate(rate: Float): Int

    @Query("UPDATE user_preferences SET fontSize = :size WHERE id = 1")
    suspend fun updateFontSize(size: Float): Int

    @Query("UPDATE user_preferences SET screenBrightness = :brightness WHERE id = 1")
    suspend fun updateScreenBrightness(brightness: Float): Int
}
