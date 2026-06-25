package com.example.incluapp.presentation.home

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.incluapp.LexiEduContainer
import com.example.incluapp.R
import com.example.incluapp.data.image.ImageUriFactory
import com.example.incluapp.presentation.components.LexiTopBar
import com.example.incluapp.ui.theme.OnPrimaryYellow
import com.example.incluapp.ui.theme.PrimaryBackground
import com.example.incluapp.ui.theme.PrimaryYellow

@Composable
fun HomeRoute(
    container: LexiEduContainer,
    onNavigateToReader: (String) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToHelp: () -> Unit
) {
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.factory(
            readingRepository = container.readingRepository,
            readingSyncRepository = container.readingSyncRepository,
            networkMonitor = container.networkMonitor,
            syncPendingReadingsUseCase = container.syncPendingReadingsUseCase
        )
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingCameraUri
        if (success && uri != null) {
            onNavigateToReader(uri.toString())
        } else {
            viewModel.onImageSelectionFailed()
        }
    }

    fun launchCamera() {
        val uri = ImageUriFactory.createCaptureUri(context)
        pendingCameraUri = uri
        cameraLauncher.launch(uri)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCamera() else viewModel.onCameraPermissionDenied()
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) onNavigateToReader(uri.toString()) else viewModel.onImageSelectionFailed()
    }

    HomeScreen(
        uiState = uiState,
        onCapturePhoto = {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

            if (granted) {
                launchCamera()
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        },
        onPickFromGallery = { galleryLauncher.launch("image/*") },
        onNavigateToHistory = onNavigateToHistory,
        onNavigateToHelp = onNavigateToHelp,
        onSyncNow = viewModel::syncNow,
        onMessageShown = viewModel::clearMessage
    )
}

@Composable
private fun HomeScreen(
    uiState: HomeUiState,
    onCapturePhoto: () -> Unit,
    onPickFromGallery: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onSyncNow: () -> Unit,
    onMessageShown: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message) {
        val message = uiState.message
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            onMessageShown()
        }
    }

    Scaffold(
        containerColor = PrimaryBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            LexiTopBar(
                title = "LexiEdu",
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, contentDescription = "Historial")
                    }
                    IconButton(onClick = onNavigateToHelp) {
                        Icon(Icons.AutoMirrored.Filled.Help, contentDescription = "Ayuda")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            Image(
                painter = painterResource(id = R.drawable.puce_logo),
                contentDescription = "Pontificia Universidad Catolica del Ecuador",
                modifier = Modifier
                    .fillMaxWidth(0.58f)
                    .height(56.dp),
                contentScale = ContentScale.Fit
            )

            Text(
                text = "Escanea texto y escuchalo",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Text(
                text = "${uiState.totalReadings} lecturas locales | ${uiState.pendingSyncCount} pendientes",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )

            SyncStatusCard(
                uiState = uiState,
                onSyncNow = onSyncNow
            )

            ActionCard(
                icon = Icons.Default.CameraAlt,
                title = "Usar camara",
                subtitle = "Captura una pagina, apunte o pizarra",
                primary = true,
                onClick = onCapturePhoto
            )

            ActionCard(
                icon = Icons.Default.Photo,
                title = "Elegir imagen",
                subtitle = "Procesa una foto desde tu dispositivo",
                primary = false,
                onClick = onPickFromGallery
            )
        }
    }
}

@Composable
private fun SyncStatusCard(
    uiState: HomeUiState,
    onSyncNow: () -> Unit
) {
    val statusText = when {
        !uiState.syncConfigured -> "Modo local activo"
        uiState.isOnline -> "En linea: sincronizacion disponible"
        else -> "Sin red: trabajando solo con Room"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = statusText,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (uiState.syncConfigured) {
                    "Tus lecturas se guardan en Room y se enviaran cuando exista conexion."
                } else {
                    "Tus lecturas, OCR, voz y sintesis se guardan en este dispositivo."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            if (uiState.isSyncing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            if (uiState.syncConfigured) {
                Button(
                    onClick = onSyncNow,
                    enabled = !uiState.isSyncing && uiState.pendingSyncCount > 0
                ) {
                    Text("Sincronizar pendientes")
                }
            }
        }
    }
}

@Composable
private fun ActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    primary: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (primary) PrimaryYellow else MaterialTheme.colorScheme.surface
    val contentColor = if (primary) OnPrimaryYellow else MaterialTheme.colorScheme.onSurface

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(34.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.76f)
                )
            }
        }
    }
}
