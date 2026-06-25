package com.example.incluapp

import android.content.Context
import com.example.incluapp.data.ai.OfflineContentSynthesizerRepository
import com.example.incluapp.data.local.database.LexiEduDatabase
import com.example.incluapp.data.network.AndroidNetworkMonitor
import com.example.incluapp.data.ocr.MlKitTextRecognitionRepository
import com.example.incluapp.data.remote.HostingerReadingRemoteDataSource
import com.example.incluapp.data.repository.ReadingRepositoryImpl
import com.example.incluapp.data.repository.ReadingSyncRepositoryImpl
import com.example.incluapp.data.repository.UserPreferencesRepositoryImpl
import com.example.incluapp.data.speech.AndroidSpeechRepository
import com.example.incluapp.data.voice.AndroidVoiceCommandRepository
import com.example.incluapp.domain.repository.ContentSynthesizerRepository
import com.example.incluapp.domain.repository.NetworkMonitor
import com.example.incluapp.domain.repository.ReadingRepository
import com.example.incluapp.domain.repository.ReadingSyncRepository
import com.example.incluapp.domain.repository.SpeechRepository
import com.example.incluapp.domain.repository.TextRecognitionRepository
import com.example.incluapp.domain.repository.UserPreferencesRepository
import com.example.incluapp.domain.repository.VoiceCommandRepository
import com.example.incluapp.domain.usecase.RecognizeTextUseCase
import com.example.incluapp.domain.usecase.SyncPendingReadingsUseCase
import com.example.incluapp.domain.usecase.SynthesizeContentUseCase

class LexiEduContainer(context: Context) {

    private val database: LexiEduDatabase = LexiEduDatabase.getInstance(context)

    val readingRepository: ReadingRepository =
        ReadingRepositoryImpl(database.readingDao())

    val userPreferencesRepository: UserPreferencesRepository =
        UserPreferencesRepositoryImpl(database.userPreferencesDao())

    val textRecognitionRepository: TextRecognitionRepository =
        MlKitTextRecognitionRepository(context)

    val contentSynthesizerRepository: ContentSynthesizerRepository =
        OfflineContentSynthesizerRepository()

    val speechRepository: SpeechRepository =
        AndroidSpeechRepository(context)

    val voiceCommandRepository: VoiceCommandRepository =
        AndroidVoiceCommandRepository(context)

    val networkMonitor: NetworkMonitor =
        AndroidNetworkMonitor(context)

    private val readingRemoteDataSource =
        HostingerReadingRemoteDataSource()

    val readingSyncRepository: ReadingSyncRepository =
        ReadingSyncRepositoryImpl(
            readingRepository = readingRepository,
            remoteDataSource = readingRemoteDataSource,
            networkMonitor = networkMonitor
        )

    val recognizeTextUseCase =
        RecognizeTextUseCase(textRecognitionRepository)

    val synthesizeContentUseCase =
        SynthesizeContentUseCase(contentSynthesizerRepository)

    val syncPendingReadingsUseCase =
        SyncPendingReadingsUseCase(readingSyncRepository)
}
