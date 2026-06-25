package com.example.incluapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey val id             : Int     = 1,
    val speechRate                 : Float   = 0.5f,
    val speechPitch                : Float   = 1.0f,
    val fontSize                   : Float   = 16f,
    val highContrastEnabled        : Boolean = false,
    val screenBrightness           : Float   = -1f
)
