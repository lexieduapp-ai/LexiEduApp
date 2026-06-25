package com.example.incluapp.presentation.reader

data class ReaderUiState(
    val readingId          : Long          = -1L,
    val title              : String        = "Lectura",
    val imageUri           : String        = "",
    val extractedText      : String        = "",
    val summary            : String        = "",
    val simplifiedText     : String        = "",
    val keyPoints          : List<String>  = emptyList(),
    val processingTimeMs   : Long          = 0L,
    val wordCount          : Int           = 0,
    val scanQualityScore   : Int           = 100,
    val requiresReview     : Boolean       = false,
    val scanWarnings       : List<String>  = emptyList(),
    val speechRate         : Float         = 0.5f,
    val fontSize           : Float         = 18f,
    val isLoading          : Boolean       = false,
    val isSpeaking         : Boolean       = false,
    val message            : String?       = null,
    // Accesibilidad por voz
    val isVoiceListening   : Boolean       = false,
    val voiceNotAvailable  : Boolean       = false,
    // Control de brillo: -1f = sistema, 0.2f..1.0f = manual
    val screenBrightness   : Float         = -1f,
    // Alto contraste
    val highContrastEnabled: Boolean       = false
) {
    val hasText: Boolean get() = extractedText.isNotBlank()
}
