package com.example.incluapp.presentation.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import android.speech.SpeechRecognizer
import com.example.incluapp.domain.model.Reading
import com.example.incluapp.domain.model.SyncStatus
import com.example.incluapp.domain.model.VoiceCommand
import com.example.incluapp.domain.repository.ReadingRepository
import com.example.incluapp.domain.repository.SpeechRepository
import com.example.incluapp.domain.repository.UserPreferencesRepository
import com.example.incluapp.domain.repository.VoiceCommandRepository
import com.example.incluapp.domain.usecase.RecognizeTextUseCase
import com.example.incluapp.domain.usecase.SynthesizeContentUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReaderViewModel(
    private val readingId: Long,
    private val imageUri: String,
    private val readingRepository: ReadingRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val recognizeTextUseCase: RecognizeTextUseCase,
    private val synthesizeContentUseCase: SynthesizeContentUseCase,
    private val speechRepository: SpeechRepository,
    private val voiceCommandRepository: VoiceCommandRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ReaderUiState(
            readingId = readingId,
            imageUri = imageUri,
            isLoading = true,
            voiceNotAvailable = !voiceCommandRepository.isAvailable()
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        observePreferences()
        load()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            userPreferencesRepository.getUserPreferences().collect { prefs ->
                _uiState.update {
                    it.copy(
                        speechRate          = prefs.speechRate,
                        fontSize            = prefs.fontSize,
                        screenBrightness    = prefs.screenBrightness,
                        highContrastEnabled = prefs.highContrastEnabled
                    )
                }
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            if (readingId > 0L) loadSavedReading(readingId) else processNewImage(imageUri)
        }
    }

    private suspend fun loadSavedReading(id: Long) {
        val reading = readingRepository.getReadingById(id)
        if (reading == null) {
            _uiState.update { it.copy(isLoading = false, message = "No se encontro la lectura guardada.") }
            return
        }
        _uiState.update { reading.toUiState(it) }
    }

    private suspend fun processNewImage(uri: String) {
        if (uri.isBlank()) {
            _uiState.update { it.copy(isLoading = false, message = "No se recibio una imagen para procesar.") }
            return
        }
        try {
            val ocrResult = recognizeTextUseCase(uri)
            if (ocrResult.text.isBlank()) {
                _uiState.update { it.copy(isLoading = false, message = "No se detecto texto en la imagen.") }
                return
            }
            val synthesis = synthesizeContentUseCase(ocrResult.text)
            val reading = Reading(
                extractedText    = ocrResult.text,
                imagePath        = uri,
                processingTimeMs = ocrResult.processingTimeMs,
                title            = createTitle(ocrResult.text),
                summary          = synthesis.summary,
                simplifiedText   = synthesis.simplifiedText,
                keyPoints        = synthesis.keyPoints,
                wordCount        = ocrResult.wordCount,
                scanQualityScore = ocrResult.quality.score,
                requiresReview   = ocrResult.quality.requiresReview,
                scanWarnings     = ocrResult.quality.warnings
            )
            val savedId = readingRepository.saveReading(reading)
            _uiState.update { reading.copy(id = savedId).toUiState(it) }
        } catch (error: Exception) {
            _uiState.update { it.copy(isLoading = false, message = "No se pudo procesar la imagen.") }
        }
    }

    fun applyManualCorrection(correctedText: String) {
        val cleanText = correctedText.trim()
        if (cleanText.isBlank()) {
            _uiState.update { it.copy(message = "El texto corregido no puede estar vacio.") }
            return
        }
        viewModelScope.launch {
            val current = uiState.value
            val synthesis = synthesizeContentUseCase(cleanText)
            val correctedReading = Reading(
                id               = current.readingId.coerceAtLeast(0L),
                extractedText    = cleanText,
                imagePath        = current.imageUri,
                processingTimeMs = current.processingTimeMs,
                createdAt        = System.currentTimeMillis(),
                title            = createTitle(cleanText),
                summary          = synthesis.summary,
                simplifiedText   = synthesis.simplifiedText,
                keyPoints        = synthesis.keyPoints,
                wordCount        = cleanText.wordCount(),
                scanQualityScore = 100,
                requiresReview   = false,
                scanWarnings     = listOf("Texto revisado manualmente."),
                syncStatus       = SyncStatus.PENDING,
                updatedAt        = System.currentTimeMillis()
            )
            val savedId = readingRepository.saveReading(correctedReading)
            _uiState.update {
                correctedReading.copy(id = savedId).toUiState(it)
                    .copy(message = "Texto corregido y guardado.")
            }
        }
    }

    // ── TTS ──────────────────────────────────────────────────────────────────

    fun speak(text: String) {
        viewModelScope.launch {
            val started = speechRepository.speak(text = text, speechRate = uiState.value.speechRate)
            _uiState.update {
                it.copy(isSpeaking = started, message = if (started) null else "No se pudo iniciar la lectura.")
            }
        }
    }

    fun stopSpeech() {
        viewModelScope.launch {
            speechRepository.stop()
            _uiState.update { it.copy(isSpeaking = false) }
        }
    }

    fun pauseSpeech() {
        viewModelScope.launch {
            speechRepository.pause()
            _uiState.update { it.copy(isSpeaking = false) }
        }
    }

    // ── Preferencias ─────────────────────────────────────────────────────────

    fun changeSpeechRate(rate: Float) {
        val clean = rate.coerceIn(0.25f, 1.2f)
        _uiState.update { it.copy(speechRate = clean) }
        viewModelScope.launch { userPreferencesRepository.updateSpeechRate(clean) }
    }

    fun changeFontSize(size: Float) {
        val clean = size.coerceIn(14f, 40f)
        _uiState.update { it.copy(fontSize = clean) }
        viewModelScope.launch { userPreferencesRepository.updateFontSize(clean) }
    }

    fun increaseFontSize() = changeFontSize(uiState.value.fontSize + 4f)
    fun decreaseFontSize() = changeFontSize(uiState.value.fontSize - 4f)

    fun changeBrightness(brightness: Float) {
        val clean = brightness.coerceIn(-1f, 1f)
        _uiState.update { it.copy(screenBrightness = clean) }
        viewModelScope.launch { userPreferencesRepository.updateScreenBrightness(clean) }
    }

    fun increaseBrightness() = changeBrightness(uiState.value.screenBrightness.coerceAtLeast(0.2f) + 0.15f)
    fun decreaseBrightness() = changeBrightness(uiState.value.screenBrightness.coerceAtLeast(0.2f) - 0.15f)

    fun speedUp() = changeSpeechRate(uiState.value.speechRate + 0.15f)
    fun slowDown() = changeSpeechRate(uiState.value.speechRate - 0.15f)

    // ── Comandos de voz ──────────────────────────────────────────────────────

    /**
     * Activa el micrófono y ejecuta el comando que diga el usuario.
     * Llamar solo cuando RECORD_AUDIO está otorgado.
     */
    fun startVoiceCommand() {
        if (_uiState.value.isVoiceListening) return
        viewModelScope.launch {
            _uiState.update { it.copy(isVoiceListening = true, message = null) }
            val command = voiceCommandRepository.listenOnce()
            _uiState.update { it.copy(isVoiceListening = false) }
            executeVoiceCommand(command)
        }
    }

    private fun executeVoiceCommand(command: VoiceCommand) {
        when (command) {
            is VoiceCommand.Read              -> speak(uiState.value.extractedText)
            is VoiceCommand.Pause             -> pauseSpeech()
            is VoiceCommand.Stop              -> stopSpeech()
            is VoiceCommand.IncreaseFontSize  -> { increaseFontSize(); announce("Letra más grande") }
            is VoiceCommand.DecreaseFontSize  -> { decreaseFontSize(); announce("Letra más pequeña") }
            is VoiceCommand.IncreaseBrightness-> { increaseBrightness(); announce("Brillo aumentado") }
            is VoiceCommand.DecreaseBrightness-> { decreaseBrightness(); announce("Brillo reducido") }
            is VoiceCommand.SpeedUp           -> { speedUp(); announce("Más rápido") }
            is VoiceCommand.SlowDown          -> { slowDown(); announce("Más lento") }
            is VoiceCommand.Unknown           -> {
                val hint = "Prueba decir: leer, pausar, letra grande, más brillo…"
                val msg = if (command.transcript.isBlank()) "No se detectó voz. $hint"
                          else "No reconocí \"${command.transcript}\". $hint"
                _uiState.update { it.copy(message = msg) }
            }
            is VoiceCommand.RecognitionError  -> {
                val msg = when (command.code) {
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                        "Permiso de micrófono denegado. Ve a Ajustes → Aplicaciones → LexiEdu → Permisos."
                    SpeechRecognizer.ERROR_AUDIO ->
                        "No se pudo acceder al micrófono. ¿Otra app lo está usando?"
                    SpeechRecognizer.ERROR_NO_MATCH ->
                        "No se reconoció ninguna palabra. Habla claro y cerca del micrófono."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                        "No detecté voz. Toca el botón y habla de inmediato."
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
                        "El reconocedor está ocupado. Espera un momento e intenta de nuevo."
                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                        "Sin conexión a internet. Conecta el Wi-Fi e intenta de nuevo."
                    SpeechRecognizer.ERROR_SERVER ->
                        "Error del servidor de voz. Intenta de nuevo en unos segundos."
                    SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED ->
                        "El español no está disponible. Instala el idioma desde Ajustes de Google."
                    else ->
                        "Error de reconocimiento (código ${command.code}). Intenta de nuevo."
                }
                _uiState.update { it.copy(message = msg) }
            }
        }
    }

    private fun announce(text: String) {
        viewModelScope.launch {
            speechRepository.speak(text = text, speechRate = 0.9f)
        }
    }

    // ── Utilitarios ──────────────────────────────────────────────────────────

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun onLeavingReader() {
        stopSpeech()
    }

    private fun Reading.toUiState(previous: ReaderUiState): ReaderUiState =
        previous.copy(
            readingId        = id,
            title            = title,
            imageUri         = imagePath,
            extractedText    = extractedText,
            summary          = summary,
            simplifiedText   = simplifiedText,
            keyPoints        = keyPoints,
            processingTimeMs = processingTimeMs,
            wordCount        = wordCount,
            scanQualityScore = scanQualityScore,
            requiresReview   = requiresReview,
            scanWarnings     = scanWarnings,
            isLoading        = false,
            message          = null
        )

    private fun createTitle(text: String): String {
        val words = text.split(Regex("\\s+")).filter { it.isNotBlank() }
        return words.take(7).joinToString(" ").let { if (words.size > 7) "$it..." else it }
    }

    private fun String.wordCount(): Int = split(Regex("\\s+")).count { it.isNotBlank() }

    companion object {
        fun factory(
            readingId: Long,
            imageUri: String,
            readingRepository: ReadingRepository,
            userPreferencesRepository: UserPreferencesRepository,
            recognizeTextUseCase: RecognizeTextUseCase,
            synthesizeContentUseCase: SynthesizeContentUseCase,
            speechRepository: SpeechRepository,
            voiceCommandRepository: VoiceCommandRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ReaderViewModel(
                    readingId                 = readingId,
                    imageUri                  = imageUri,
                    readingRepository         = readingRepository,
                    userPreferencesRepository = userPreferencesRepository,
                    recognizeTextUseCase      = recognizeTextUseCase,
                    synthesizeContentUseCase  = synthesizeContentUseCase,
                    speechRepository          = speechRepository,
                    voiceCommandRepository    = voiceCommandRepository
                ) as T
        }
    }
}
