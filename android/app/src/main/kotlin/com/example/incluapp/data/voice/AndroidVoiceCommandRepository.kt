package com.example.incluapp.data.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.example.incluapp.domain.model.VoiceCommand
import com.example.incluapp.domain.repository.VoiceCommandRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AndroidVoiceCommandRepository(
    context: Context
) : VoiceCommandRepository {

    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun isAvailable(): Boolean =
        SpeechRecognizer.isRecognitionAvailable(appContext)

    override suspend fun listenOnce(): VoiceCommand =
        suspendCancellableCoroutine { continuation ->
            mainHandler.post {
                val recognizer = SpeechRecognizer.createSpeechRecognizer(appContext)

                recognizer.setRecognitionListener(object : RecognitionListener {

                    override fun onResults(results: Bundle?) {
                        val transcript = results
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull() ?: ""
                        recognizer.destroy()
                        if (!continuation.isActive) return
                        if (transcript.isBlank()) {
                            continuation.resume(VoiceCommand.RecognitionError(SpeechRecognizer.ERROR_NO_MATCH))
                        } else {
                            continuation.resume(parseCommand(transcript))
                        }
                    }

                    override fun onError(error: Int) {
                        recognizer.destroy()
                        if (continuation.isActive) {
                            continuation.resume(VoiceCommand.RecognitionError(error))
                        }
                    }

                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}
                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    // Modelo de forma libre — más flexible que LANGUAGE_MODEL_WEB_SEARCH
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    // Idioma principal español; fallback al idioma del sistema
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "es")
                    // NO forzar offline: si no hay modelo offline usa la red (Google Speech API)
                    // putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)  ← era el bug principal
                    // Devuelve hasta 5 candidatos para aumentar la probabilidad de match
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                    // Permite resultados parciales — el reconocedor empieza a procesar más rápido
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    // Identificación del paquete — requerida en algunos fabricantes (Huawei, Xiaomi)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, appContext.packageName)
                }
                recognizer.startListening(intent)

                continuation.invokeOnCancellation {
                    mainHandler.post { recognizer.destroy() }
                }
            }
        }

    private fun parseCommand(transcript: String): VoiceCommand {
        // Normalizar: lowercase + quitar tildes para no depender de que el STT
        // devuelva siempre la versión acentuada ("más" y "mas" deben funcionar igual)
        val t = transcript.lowercase().trim()
            .replace("á", "a").replace("é", "e").replace("í", "i")
            .replace("ó", "o").replace("ú", "u").replace("ñ", "n")

        // Verbos de acción que el usuario puede usar naturalmente
        val verbeSube    = t.contains("sube") || t.contains("subir") ||
                           t.contains("aumenta") || t.contains("aumentar") ||
                           t.contains("agrand") || t.contains("mas ")  ||
                           t.contains("mas$")   || t.contains("incrementa")
        val verbeBaja    = t.contains("baja") || t.contains("bajar") ||
                           t.contains("reduce") || t.contains("reducir") ||
                           t.contains("disminuye") || t.contains("menos ") ||
                           t.contains("achica") || t.contains("pequen")

        return when {
            // ── Lectura ────────────────────────────────────────────────────────
            t.contains("leer") || t.contains("lee ") || t.contains("lee$") ||
                t.contains("reproducir") || t.contains("reproduce") ||
                t.contains("empezar") || t.contains("iniciar") ||
                t.contains("escuchar") || t.contains("play") ||
                t.contains("comienza") -> VoiceCommand.Read

            // ── Pausa ──────────────────────────────────────────────────────────
            t.contains("pausar") || t.contains("pausa") ||
                t.contains("espera") || t.contains("detente") -> VoiceCommand.Pause

            // ── Detener ────────────────────────────────────────────────────────
            t.contains("detener") || t.contains("parar") || t.contains("stop") ||
                t.contains("terminar") || t.contains("silencio") ||
                t.contains("callate") || t.contains("calla") -> VoiceCommand.Stop

            // ── Tamaño de letra ────────────────────────────────────────────────
            // Cubre: "aumenta el texto", "sube la letra", "letra más grande",
            //        "agrandar texto", "fuente más grande", "aumenta la fuente"
            (t.contains("texto") || t.contains("letra") || t.contains("fuente") ||
                t.contains("font") || t.contains("tamano") || t.contains("tipografia")) && verbeSube
                -> VoiceCommand.IncreaseFontSize

            (t.contains("texto") || t.contains("letra") || t.contains("fuente") ||
                t.contains("font") || t.contains("tamano") || t.contains("tipografia")) && verbeBaja
                -> VoiceCommand.DecreaseFontSize

            // Variantes directas sin objeto explícito
            t.contains("grande") && !t.contains("brillo") && !t.contains("contraste")
                -> VoiceCommand.IncreaseFontSize
            t.contains("pequena") && !t.contains("brillo") && !t.contains("contraste")
                -> VoiceCommand.DecreaseFontSize

            // ── Brillo / Contraste ─────────────────────────────────────────────
            // Cubre: "sube el contraste", "más brillo", "aumenta la luz",
            //        "más claro", "brillo alto", "subir contraste"
            (t.contains("brillo") || t.contains("contraste") || t.contains("luz") ||
                t.contains("claro") || t.contains("luminosidad") || t.contains("pantalla")) && verbeSube
                -> VoiceCommand.IncreaseBrightness

            (t.contains("brillo") || t.contains("contraste") || t.contains("luz") ||
                t.contains("oscuro") || t.contains("luminosidad") || t.contains("pantalla")) && verbeBaja
                -> VoiceCommand.DecreaseBrightness

            // ── Velocidad ──────────────────────────────────────────────────────
            // Cubre: "más rápido", "acelerar", "sube la velocidad", "habla mas rapido"
            (t.contains("rapido") || t.contains("velocidad") || t.contains("acelera") ||
                t.contains("veloz")) && (verbeSube || t.contains("rapido"))
                -> VoiceCommand.SpeedUp

            (t.contains("lento") || t.contains("despacio") || t.contains("velocidad") ||
                t.contains("calma")) && (verbeBaja || t.contains("lento") || t.contains("despacio"))
                -> VoiceCommand.SlowDown

            else -> VoiceCommand.Unknown(transcript)
        }
    }
}
