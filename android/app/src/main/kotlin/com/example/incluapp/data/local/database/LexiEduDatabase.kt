package com.example.incluapp.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.incluapp.data.local.dao.ReadingDao
import com.example.incluapp.data.local.dao.UserPreferencesDao
import com.example.incluapp.data.local.entity.ReadingEntity
import com.example.incluapp.data.local.entity.UserPreferencesEntity

@Database(
    entities     = [ReadingEntity::class, UserPreferencesEntity::class],
    version      = 5,
    exportSchema = false
)
abstract class LexiEduDatabase : RoomDatabase() {

    abstract fun readingDao(): ReadingDao
    abstract fun userPreferencesDao(): UserPreferencesDao

    companion object {
        @Volatile private var INSTANCE: LexiEduDatabase? = null

        fun getInstance(context: Context): LexiEduDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    LexiEduDatabase::class.java,
                    "lexiedu.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                .also { INSTANCE = it }
            }
    }
}
