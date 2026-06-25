package com.example.incluapp.data.remote

import com.example.incluapp.domain.model.Reading
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class HostingerReadingRemoteDataSource(
    private val baseUrl: String = RemoteSyncConfig.BASE_URL
) : ReadingRemoteDataSource {

    override val isConfigured: Boolean
        get() = baseUrl.isNotBlank()

    override suspend fun uploadReading(reading: Reading): RemoteUploadResult =
        withContext(Dispatchers.IO) {
            check(isConfigured) { "Remote sync endpoint is not configured." }

            val endpoint = "${baseUrl.trimEnd('/')}/api/readings"
            val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15_000
                readTimeout = 20_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
            }

            val payload = reading.toJson().toString()
            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(payload)
            }

            val status = connection.responseCode
            val responseText = if (status in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            }

            connection.disconnect()

            if (status !in 200..299) {
                error("Remote sync failed with HTTP $status: $responseText")
            }

            val remoteId = responseText
                .takeIf { it.isNotBlank() }
                ?.let { runCatching { JSONObject(it) }.getOrNull() }
                ?.optString("id")
                ?.takeIf { it.isNotBlank() }
                ?: responseText
                    .takeIf { it.isNotBlank() }
                    ?.let { runCatching { JSONObject(it) }.getOrNull() }
                    ?.optString("remote_id")
                    ?.takeIf { it.isNotBlank() }
                ?: "hostinger-${reading.id}-${System.currentTimeMillis()}"

            RemoteUploadResult(remoteId = remoteId)
        }

    private fun Reading.toJson(): JSONObject =
        JSONObject()
            .put("local_id", id)
            .put("title", title)
            .put("extracted_text", extractedText)
            .put("summary", summary)
            .put("simplified_text", simplifiedText)
            .put("key_points", JSONArray(keyPoints))
            .put("word_count", wordCount)
            .put("scan_quality_score", scanQualityScore)
            .put("requires_review", requiresReview)
            .put("scan_warnings", JSONArray(scanWarnings))
            .put("processing_time_ms", processingTimeMs)
            .put("created_at", createdAt)
            .put("updated_at", updatedAt)
}
