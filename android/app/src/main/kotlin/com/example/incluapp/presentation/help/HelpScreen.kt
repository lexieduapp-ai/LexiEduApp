package com.example.incluapp.presentation.help

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.incluapp.presentation.components.LexiTopBar
import com.example.incluapp.ui.theme.PrimaryBackground
import com.example.incluapp.ui.theme.PrimaryYellow

@Composable
fun HelpScreen(
    onNavigateBack: () -> Unit
) {
    val items = listOf(
        HelpItem(
            icon = Icons.AutoMirrored.Filled.TextSnippet,
            title = "OCR local",
            body = "La imagen se procesa en el dispositivo con ML Kit. El texto queda guardado en Room para usarlo sin internet."
        ),
        HelpItem(
            icon = Icons.Default.RecordVoiceOver,
            title = "Lectura en voz",
            body = "El texto se reproduce con el motor TextToSpeech nativo de Android. Puedes cambiar velocidad y tamano de letra."
        ),
        HelpItem(
            icon = Icons.Default.PrivacyTip,
            title = "Sintesis IA offline",
            body = "La sintesis usa reglas locales para resumir, simplificar y extraer puntos clave sin enviar datos a servidores."
        )
    )

    Scaffold(
        containerColor = PrimaryBackground,
        topBar = {
            LexiTopBar(
                title = "Ayuda",
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                HelpCard(item)
            }
        }
    }
}

@Composable
private fun HelpCard(item: HelpItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = PrimaryYellow
            )
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = item.body,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private data class HelpItem(
    val icon: ImageVector,
    val title: String,
    val body: String
)
