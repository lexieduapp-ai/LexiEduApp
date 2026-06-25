package com.example.incluapp.presentation.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.incluapp.LexiEduContainer
import com.example.incluapp.domain.model.Reading
import com.example.incluapp.domain.model.SyncStatus
import com.example.incluapp.presentation.components.LexiTopBar
import com.example.incluapp.ui.theme.PrimaryBackground
import com.example.incluapp.ui.theme.PrimaryYellow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryRoute(
    container: LexiEduContainer,
    onNavigateBack: () -> Unit,
    onOpenReading: (Long) -> Unit
) {
    val viewModel: HistoryViewModel = viewModel(
        factory = HistoryViewModel.factory(container.readingRepository)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HistoryScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onSearchChange = viewModel::updateSearchQuery,
        onOpenReading = onOpenReading,
        onDeleteReading = viewModel::deleteReading
    )
}

@Composable
private fun HistoryScreen(
    uiState: HistoryUiState,
    onNavigateBack: () -> Unit,
    onSearchChange: (String) -> Unit,
    onOpenReading: (Long) -> Unit,
    onDeleteReading: (Long) -> Unit
) {
    Scaffold(
        containerColor = PrimaryBackground,
        topBar = {
            LexiTopBar(
                title = "Historial",
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                placeholder = {
                    Text("Buscar lecturas")
                }
            )

            if (uiState.filteredReadings.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sin lecturas guardadas",
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = uiState.filteredReadings,
                        key = { it.id }
                    ) { reading ->
                        ReadingCard(
                            reading = reading,
                            onOpenReading = onOpenReading,
                            onDeleteReading = onDeleteReading
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReadingCard(
    reading: Reading,
    onOpenReading: (Long) -> Unit,
    onDeleteReading: (Long) -> Unit
) {
    val formattedDate = SimpleDateFormat(
        "dd/MM/yyyy HH:mm",
        Locale.getDefault()
    ).format(Date(reading.createdAt))

    Card(
        onClick = { onOpenReading(reading.id) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reading.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = reading.summary.ifBlank { reading.extractedText },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "$formattedDate | ${reading.wordCount} palabras",
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SyncChip(reading.syncStatus)
                    if (reading.requiresReview) {
                        ReviewChip()
                    }
                }
            }
            IconButton(onClick = { onDeleteReading(reading.id) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar"
                )
            }
        }
    }
}

@Composable
private fun ReviewChip() {
    AssistChip(
        onClick = {},
        label = { Text("Revisar OCR") },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.error,
            labelColor = MaterialTheme.colorScheme.onError
        )
    )
}

@Composable
private fun SyncChip(status: SyncStatus) {
    val label = when (status) {
        SyncStatus.PENDING -> "Pendiente web"
        SyncStatus.SYNCED -> "Sincronizada"
        SyncStatus.FAILED -> "Reintentar"
    }
    val containerColor = when (status) {
        SyncStatus.SYNCED -> MaterialTheme.colorScheme.secondary
        SyncStatus.PENDING -> PrimaryYellow
        SyncStatus.FAILED -> MaterialTheme.colorScheme.error
    }
    val labelColor = when (status) {
        SyncStatus.SYNCED -> MaterialTheme.colorScheme.onSecondary
        SyncStatus.PENDING -> PrimaryBackground
        SyncStatus.FAILED -> MaterialTheme.colorScheme.onError
    }

    AssistChip(
        onClick = {},
        label = { Text(label) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = containerColor,
            labelColor = labelColor
        )
    )
}
