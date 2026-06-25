package com.example.incluapp.data.ai

import com.example.incluapp.domain.model.ContentSynthesis
import com.example.incluapp.domain.repository.ContentSynthesizerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OfflineContentSynthesizerRepository : ContentSynthesizerRepository {

    override suspend fun synthesize(text: String): ContentSynthesis =
        withContext(Dispatchers.Default) {
            val cleanText = text.replace(Regex("\\s+"), " ").trim()
            if (cleanText.isBlank()) {
                return@withContext ContentSynthesis("", "", emptyList())
            }

            val sentences = splitSentences(cleanText)

            // Texto muy corto (tabla, formulario): no hay suficiente para rankear
            if (sentences.size <= 2) {
                return@withContext ContentSynthesis(
                    summary = sentences.joinToString(" "),
                    simplifiedText = simplify(sentences),
                    keyPoints = extractTopKeywords(cleanText, limit = 5)
                )
            }

            val keywords = extractKeywordScores(cleanText)
            val total = sentences.size

            val ranked = sentences.mapIndexed { idx, sentence ->
                // Peso por posición: las primeras oraciones tienden a ser más informativas
                val positionWeight = when {
                    idx == 0              -> 1.5f
                    idx <= total / 5      -> 1.25f
                    idx >= total - 2      -> 1.1f
                    else                  -> 1.0f
                }
                // Penalizar oraciones demasiado cortas o demasiado largas
                val wordCount = sentence.split(Regex("\\s+")).size
                val lengthWeight = when {
                    wordCount < 4  -> 0.4f
                    wordCount > 50 -> 0.75f
                    else           -> 1.0f
                }
                RankedSentence(
                    index = idx,
                    sentence = sentence,
                    score = (scoreSentence(sentence, keywords) * positionWeight * lengthWeight).toInt()
                )
            }.sortedByDescending { it.score }

            // Cantidad de oraciones en el resumen: 1/3 del total, mínimo 2 máximo 4
            val summaryCount = (total / 3).coerceIn(2, 4)
            val keySentences = ranked
                .take(summaryCount)
                .sortedBy { it.index }
                .map { it.sentence }

            ContentSynthesis(
                summary = keySentences.joinToString(" "),
                simplifiedText = simplify(sentences),
                keyPoints = ranked
                    .take(5)
                    .sortedBy { it.index }
                    .map { it.sentence.trimEnd('.', ';', ':') }
            )
        }

    private fun splitSentences(text: String): List<String> =
        // Separar en puntos/exclamación/interrogación O en saltos de línea
        text.split(Regex("(?<=[.!?])\\s+|\n+"))
            .map { it.trim() }
            // Descartar fragmentos de menos de 3 palabras (ruido OCR)
            .filter { s -> s.isNotBlank() && s.split(Regex("\\s+")).size >= 3 }
            .ifEmpty { listOf(text.trim()) }

    private fun extractKeywordScores(text: String): Map<String, Int> =
        text.lowercase()
            .split(Regex("[^a-z0-9áéíóúñü]+"))
            .asSequence()
            .map { it.trim() }
            .filter { it.length > 3 && it !in STOP_WORDS }
            .groupingBy { it }
            .eachCount()

    private fun scoreSentence(sentence: String, keywords: Map<String, Int>): Int =
        sentence.lowercase()
            .split(Regex("[^a-z0-9áéíóúñü]+"))
            .sumOf { keywords[it] ?: 0 }

    private fun extractTopKeywords(text: String, limit: Int): List<String> =
        extractKeywordScores(text)
            .entries
            .sortedByDescending { it.value }
            .take(limit)
            .map { it.key.replaceFirstChar { c -> c.uppercase() } }

    private fun simplify(sentences: List<String>): String =
        sentences.take(6).joinToString("\n\n") { s ->
            s.replace(Regex("\\bel cual\\b", RegexOption.IGNORE_CASE), "que")
             .replace(Regex("\\bla cual\\b", RegexOption.IGNORE_CASE), "que")
             .replace(Regex("\\blos cuales\\b", RegexOption.IGNORE_CASE), "que")
             .replace(Regex("\\blas cuales\\b", RegexOption.IGNORE_CASE), "que")
             .replace(" debido a que ", " porque ")
             .replace(" debido a ", " por ")
             .replace(" con el objetivo de ", " para ")
             .replace(" con la finalidad de ", " para ")
             .replace(" con el propósito de ", " para ")
             .replace(" aproximadamente ", " cerca de ")
             .replace(" en consecuencia ", " entonces ")
             .replace(" no obstante ", " pero ")
             .replace(" sin embargo ", " pero ")
             .replace(" de tal manera que ", " para que ")
             .replace(" a pesar de que ", " aunque ")
             .replace(" con respecto a ", " sobre ")
             .replace(" en relación con ", " sobre ")
             .replace(" es decir ", " o sea ")
             .replace(" por consiguiente ", " entonces ")
             .replace(" posteriormente ", " después ")
             .replace(" anteriormente ", " antes ")
             .replace(" adicionalmente ", " además ")
             .replace(" en primer lugar ", " primero ")
             .replace(" en segundo lugar ", " segundo ")
             .replace(" en tercer lugar ", " tercero ")
             .replace(" a continuación ", " luego ")
             .replace(" a través de ", " por medio de ")
             .replace(" en virtud de ", " por ")
             .replace(" a fin de ", " para ")
             .trim()
        }

    private data class RankedSentence(val index: Int, val sentence: String, val score: Int)

    private companion object {
        val STOP_WORDS = setOf(
            // Artículos
            "el", "la", "los", "las", "un", "una", "unos", "unas",
            // Preposiciones
            "a", "ante", "bajo", "con", "contra", "de", "desde",
            "en", "entre", "hacia", "hasta", "para", "por", "según",
            "sin", "sobre", "tras", "durante", "mediante",
            // Conjunciones
            "que", "y", "o", "pero", "mas", "sino", "aunque", "porque",
            "como", "cuando", "si", "ni", "sea", "pues",
            // Pronombres personales y determinantes
            "este", "esta", "estos", "estas", "ese", "esa", "esos", "esas",
            "aquel", "aquella", "aquellos", "aquellas",
            "yo", "tu", "él", "ella", "nosotros", "vosotros", "ellos", "ellas",
            "me", "te", "se", "nos", "os", "le", "les", "lo",
            "mi", "mis", "su", "sus", "nuestro", "nuestra", "nuestros", "nuestras",
            // Verbos ser / estar / haber
            "es", "son", "era", "eran", "fue", "fueron", "ser", "sido",
            "estar", "está", "están", "estaba", "estaban", "estuvo",
            "ha", "han", "hay", "haber", "había", "hubo",
            // Verbos comunes de alta frecuencia
            "tiene", "tienen", "tener", "tenía", "tuvo",
            "hace", "hacen", "hacer", "hizo",
            "puede", "pueden", "poder", "pudo",
            "debe", "deben", "deber",
            "dice", "dicen", "decir", "dijo",
            "van", "voy", "iban", "venir",
            // Adverbios frecuentes
            "no", "también", "tambien", "muy", "más", "mas", "tan",
            "ya", "solo", "sólo", "aún", "aun", "bien", "mal",
            "aquí", "allí", "ahí", "así", "ahora", "antes", "después",
            "despues", "siempre", "nunca", "todo", "nada", "algo",
            "alguien", "nadie", "demás", "demas",
            // Cuantificadores
            "todo", "toda", "todos", "todas",
            "mucho", "mucha", "muchos", "muchas",
            "poco", "poca", "pocos", "pocas",
            "otro", "otra", "otros", "otras",
            "mismo", "misma", "mismos", "mismas",
            "tanto", "tanta", "tantos", "tantas",
            "algún", "alguno", "alguna", "algunos", "algunas",
            "ningún", "ninguno", "ninguna",
            // Pronombres relativos
            "donde", "cuando", "cual", "cuales", "quien", "quienes",
            "cuyo", "cuya", "cuyos", "cuyas",
            // Contracciones y partículas
            "del", "al"
        )
    }
}
