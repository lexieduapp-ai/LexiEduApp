package com.example.incluapp.navigation

import kotlinx.serialization.Serializable

@Serializable
object Splash

@Serializable
object Home

@Serializable
data class Reader(
    val readingId: Long = -1L,
    val imagePath: String = ""
)

@Serializable
object History

@Serializable
object Help
