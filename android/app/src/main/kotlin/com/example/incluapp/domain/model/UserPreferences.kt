package com.example.incluapp.domain.model

data class UserPreferences(
    val speechRate          : Float   = 0.5f,
    val speechPitch         : Float   = 1.0f,
    val fontSize            : Float   = 16f,
    val highContrastEnabled : Boolean = false,
    // -1f = usa el brillo del sistema; 0.2f..1.0f = control manual
    val screenBrightness    : Float   = -1f
)
