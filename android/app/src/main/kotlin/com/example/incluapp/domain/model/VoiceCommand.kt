package com.example.incluapp.domain.model

sealed class VoiceCommand {
    object Read              : VoiceCommand()
    object Pause             : VoiceCommand()
    object Stop              : VoiceCommand()
    object IncreaseFontSize  : VoiceCommand()
    object DecreaseFontSize  : VoiceCommand()
    object IncreaseBrightness: VoiceCommand()
    object DecreaseBrightness: VoiceCommand()
    object SpeedUp           : VoiceCommand()
    object SlowDown          : VoiceCommand()
    /** Voz reconocida pero no coincide con ningún comando conocido. */
    data class Unknown(val transcript: String) : VoiceCommand()
    /** El reconocedor devolvió un error técnico. [code] es SpeechRecognizer.ERROR_*. */
    data class RecognitionError(val code: Int)  : VoiceCommand()
}
