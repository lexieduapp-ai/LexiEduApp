package com.example.incluapp.data.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import com.example.incluapp.domain.repository.SpeechRepository
import java.text.Normalizer
import java.util.Locale
import kotlinx.coroutines.CompletableDeferred

class AndroidSpeechRepository(
    context: Context
) : SpeechRepository {

    private val appContext = context.applicationContext
    private val ready = CompletableDeferred<Boolean>()
    private lateinit var textToSpeech: TextToSpeech

    init {
        textToSpeech = TextToSpeech(appContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Intentar locales en orden de preferencia.
                // es_US y es_419 tienen modelos de voz que manejan mejor el español
                // latinoamericano y palabras técnicas/institucionales que es_ES desconoce.
                val locales = listOf(
                    Locale("es", "US"),   // Español EE.UU. — mejor cobertura de vocabulario
                    Locale("es", "MX"),   // Español México
                    Locale("es", "419"),  // Español Latinoamericano (ISO 3166)
                    Locale("es", "ES"),   // Español España — fallback
                    Locale("es")          // Genérico
                )
                val supported = locales.any { locale ->
                    val result = textToSpeech.setLanguage(locale)
                    result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED
                }
                ready.complete(supported)
            } else {
                ready.complete(false)
            }
        }
    }

    override suspend fun speak(text: String, speechRate: Float, pitch: Float): Boolean {
        if (text.isBlank() || !ready.await()) return false

        textToSpeech.setSpeechRate(speechRate.coerceIn(0.25f, 1.2f))
        textToSpeech.setPitch(pitch.coerceIn(0.6f, 1.6f))

        val result = textToSpeech.speak(
            normalizeForTts(text),
            TextToSpeech.QUEUE_FLUSH,
            null,
            "lexiedu-${System.nanoTime()}"
        )
        return result == TextToSpeech.SUCCESS
    }

    override suspend fun pause() {
        if (ready.await()) textToSpeech.stop()
    }

    override suspend fun stop() {
        if (ready.await()) textToSpeech.stop()
    }

    override fun shutdown() {
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    /**
     * Pre-procesa el texto antes de enviarlo al TTS para corregir pronunciaciones conocidas.
     *
     * Problemas identificados en pruebas:
     * - Caracteres Unicode descompuestos (á = a + ́) → TTS los deletrea en vez de leerlos.
     *   Fix: normalizar a NFC (forma precompuesta: á = U+00E1).
     * - Palabras institucionales desconocidas (fiscomisional) → TTS las deletrea.
     *   Fix: separar en raíces conocidas por el diccionario del TTS.
     * - Acentuación incorrecta en ciertas palabras (redacción, laboratorio).
     *   Fix: quitar tildes en palabras problemáticas para que el TTS use estrés natural.
     */
    private fun normalizeForTts(text: String): String {
        // 1. Normalizar Unicode a NFC: convierte "a + ́" → "á" precompuesto.
        //    Esto evita que el TTS deletree caracteres que el OCR guardó como secuencias
        //    de código separadas (causa más común del deletreo de "alegría", etc.)
        var result = Normalizer.normalize(text, Normalizer.Form.NFC)

        // 2. Palabras que el TTS desconoce y deletrea → separar en raíces conocidas
        result = result
            .replace(Regex("\\bfiscomisionales\\b", RegexOption.IGNORE_CASE), "fisco misionales")
            .replace(Regex("\\bfiscomisional\\b",   RegexOption.IGNORE_CASE), "fisco misional")

        // 3. Palabras con pronunciación errónea en ciertas versiones del TTS de Google.
        //    Quitar la tilde hace que el motor use el estrés por defecto de la sílaba,
        //    que coincide con la pronunciación correcta en estos casos.
        result = result
            .replace("Redacción",   "Redaccion")
            .replace("redacción",   "redaccion")
            .replace("Producción",  "Produccion")
            .replace("producción",  "produccion")
            .replace("Institución", "Institucion")
            .replace("institución", "institucion")
            .replace("Función",     "Funcion")
            .replace("función",     "funcion")
            // "laboratorio" → "laboratorico" es un bug del TTS en versiones antiguas.
            // Escribirlo con guion bajo en la sílaba tónica lo forza a releer de otra forma.
            .replace(Regex("\\blaboratorio(s?)\\b", RegexOption.IGNORE_CASE)) { m ->
                "labo-ratorio${m.groupValues[1]}"
            }

        return result
    }
}
