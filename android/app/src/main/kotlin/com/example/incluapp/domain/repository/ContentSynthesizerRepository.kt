package com.example.incluapp.domain.repository

import com.example.incluapp.domain.model.ContentSynthesis

interface ContentSynthesizerRepository {
    suspend fun synthesize(text: String): ContentSynthesis
}
