package com.example.incluapp.data.remote

import com.example.incluapp.domain.model.Reading

interface ReadingRemoteDataSource {
    val isConfigured: Boolean
    suspend fun uploadReading(reading: Reading): RemoteUploadResult
}

data class RemoteUploadResult(
    val remoteId: String
)
