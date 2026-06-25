package com.example.incluapp.domain.usecase

import com.example.incluapp.domain.repository.ContentSynthesizerRepository

class SynthesizeContentUseCase(
    private val repository: ContentSynthesizerRepository
) {
    suspend operator fun invoke(text: String) =
        repository.synthesize(text)
}
