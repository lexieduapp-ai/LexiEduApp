package com.example.incluapp.data.image

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object ImageUriFactory {
    fun createCaptureUri(context: Context): Uri {
        val capturesDir = File(context.cacheDir, "captures").apply {
            mkdirs()
        }
        val imageFile = File.createTempFile("lexiedu_", ".jpg", capturesDir)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    }
}
