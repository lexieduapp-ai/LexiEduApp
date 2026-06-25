package com.example.incluapp.data.ocr

import android.content.Context
import android.net.Uri
import com.example.incluapp.domain.model.OcrResult
import com.example.incluapp.domain.model.ScanQuality
import com.example.incluapp.domain.repository.TextRecognitionRepository
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MlKitTextRecognitionRepository(
    context: Context
) : TextRecognitionRepository {

    private val appContext = context.applicationContext
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun extractTextFromImage(imageUri: String): OcrResult =
        withContext(Dispatchers.Default) {
            val stopwatch = StopwatchStart.now()
            val inputImage = InputImage.fromFilePath(appContext, Uri.parse(imageUri))
            val recognizedText = recognizer.process(inputImage).awaitResult()

            val lines = recognizedText.textBlocks.flatMap { block -> block.lines }
            val text = lines.sortedByReadingOrder()
                .joinToString(separator = "\n") { line -> line.text }
                .trim()
            val wordCount = text.wordCount()
            val quality = text.scanQuality(
                lineCount = lines.size,
                blockCount = recognizedText.textBlocks.size,
                wordCount = wordCount
            )

            OcrResult(
                text = text,
                processingTimeMs = stopwatch.elapsedMillis(),
                wordCount = wordCount,
                quality = quality
            )
        }

    private suspend fun <T> Task<T>.awaitResult(): T =
        suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { result ->
                if (continuation.isActive) continuation.resume(result)
            }
            addOnFailureListener { error ->
                if (continuation.isActive) continuation.resumeWithException(error)
            }
            addOnCanceledListener {
                continuation.cancel()
            }
        }

    private fun String.wordCount(): Int =
        trim()
            .split(Regex("\\s+"))
            .count { it.isNotBlank() }

    private fun String.scanQuality(
        lineCount: Int,
        blockCount: Int,
        wordCount: Int
    ): ScanQuality {
        val cleanText = trim()
        if (cleanText.isBlank()) {
            return ScanQuality(
                score = 0,
                requiresReview = true,
                warnings = listOf("No se detecto texto legible.")
            )
        }

        var score = 100
        val warnings = mutableListOf<String>()
        val symbolRatio = cleanText.count { !it.isLetterOrDigit() && !it.isWhitespace() } /
            cleanText.length.coerceAtLeast(1).toFloat()
        val tokens = cleanText.split(Regex("\\s+")).filter { it.isNotBlank() }
        val shortTokenRatio = tokens.count { it.length <= 2 } / tokens.size.coerceAtLeast(1).toFloat()
        val averageLineLength = cleanText.length / lineCount.coerceAtLeast(1)

        if (wordCount < 4) {
            score -= 35
            warnings += "Se detectaron muy pocas palabras."
        }
        if (lineCount <= 1 && wordCount < 10) {
            score -= 15
            warnings += "El texto aparece muy fragmentado."
        }
        if (averageLineLength < 10 && lineCount > 2) {
            score -= 15
            warnings += "Varias lineas son demasiado cortas; puede haber letra dificil o imagen movida."
        }
        if (symbolRatio > 0.18f) {
            score -= 20
            warnings += "Hay muchos simbolos o caracteres poco claros."
        }
        if (shortTokenRatio > 0.55f && tokens.size > 4) {
            score -= 20
            warnings += "Hay muchas palabras cortas o partidas; revisa el resultado."
        }
        if (blockCount == 0) {
            score -= 30
            warnings += "No se encontraron bloques de texto consistentes."
        }

        val finalScore = score.coerceIn(0, 100)
        return ScanQuality(
            score = finalScore,
            requiresReview = finalScore < 70 || warnings.isNotEmpty(),
            warnings = warnings.ifEmpty {
                listOf("Lectura clara.")
            }
        )
    }

    /**
     * Sorts text lines in natural reading order (top-to-bottom, left-to-right).
     *
     * ML Kit returns text blocks by visual region (table cells, columns), which can produce
     * out-of-order results for structured layouts. This groups lines into horizontal "row bands"
     * by checking vertical overlap of their bounding boxes, then sorts left-to-right within
     * each band, and finally outputs bands in top-to-bottom order.
     */
    private fun List<com.google.mlkit.vision.text.Text.Line>.sortedByReadingOrder()
            : List<com.google.mlkit.vision.text.Text.Line> {
        if (isEmpty()) return this

        // Sort by vertical position first so we process lines top to bottom
        val byY = sortedBy { it.boundingBox?.top ?: 0 }

        // Group into horizontal row bands using vertical overlap
        val rows = mutableListOf<MutableList<com.google.mlkit.vision.text.Text.Line>>()
        for (line in byY) {
            val box = line.boundingBox
            val lineTop = box?.top ?: 0
            val lineBottom = box?.bottom ?: lineTop
            val lineHeight = (lineBottom - lineTop).coerceAtLeast(1)

            var placed = false
            for (row in rows) {
                val repBox = row.first().boundingBox
                val repTop = repBox?.top ?: 0
                val repBottom = repBox?.bottom ?: repTop
                val repHeight = (repBottom - repTop).coerceAtLeast(1)

                // Two lines belong to the same row if they overlap vertically by >= 40%
                val overlapTop = maxOf(lineTop, repTop)
                val overlapBottom = minOf(lineBottom, repBottom)
                val overlap = (overlapBottom - overlapTop).coerceAtLeast(0)
                val threshold = minOf(lineHeight, repHeight) * 0.4f

                if (overlap >= threshold) {
                    row.add(line)
                    placed = true
                    break
                }
            }

            if (!placed) rows.add(mutableListOf(line))
        }

        // Within each row band sort left-to-right, then flatten all rows top-to-bottom
        return rows
            .sortedBy { row -> row.minOf { it.boundingBox?.top ?: 0 } }
            .flatMap { row -> row.sortedBy { it.boundingBox?.left ?: 0 } }
    }

    private class StopwatchStart private constructor(
        private val startedAt: Long
    ) {
        fun elapsedMillis(): Long = (System.nanoTime() - startedAt) / 1_000_000L

        companion object {
            fun now() = StopwatchStart(System.nanoTime())
        }
    }
}
