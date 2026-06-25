package com.example.incluapp.presentation.reader

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.incluapp.LexiEduContainer
import com.example.incluapp.presentation.components.LexiTopBar
import com.example.incluapp.ui.theme.DisabledGray
import com.example.incluapp.ui.theme.ErrorRed
import com.example.incluapp.ui.theme.OnPrimaryYellow
import com.example.incluapp.ui.theme.OnSurface
import com.example.incluapp.ui.theme.PrimaryBackground
import com.example.incluapp.ui.theme.PrimaryYellow
import com.example.incluapp.ui.theme.Surface
import com.example.incluapp.ui.theme.SurfaceVariant

// ─────────────────────────────────────────────────────────────────────────────
// ReaderRoute — punto de entrada público
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ReaderRoute(
    container: LexiEduContainer,
    readingId: Long,
    imageUri: String,
    onNavigateBack: () -> Unit
) {
    val viewModel: ReaderViewModel = viewModel(
        key = "reader-$readingId-$imageUri",
        factory = ReaderViewModel.factory(
            readingId                 = readingId,
            imageUri                  = imageUri,
            readingRepository         = container.readingRepository,
            userPreferencesRepository = container.userPreferencesRepository,
            recognizeTextUseCase      = container.recognizeTextUseCase,
            synthesizeContentUseCase  = container.synthesizeContentUseCase,
            speechRepository          = container.speechRepository,
            voiceCommandRepository    = container.voiceCommandRepository
        )
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.startVoiceCommand()
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.onLeavingReader() }
    }

    // Aplica brillo a la ventana de la actividad
    val activity = context as? Activity
    SideEffect {
        activity?.window?.attributes = activity?.window?.attributes?.also {
            it.screenBrightness = uiState.screenBrightness
        }
    }

    ReaderScreen(
        uiState                 = uiState,
        onNavigateBack          = onNavigateBack,
        onSpeak                 = viewModel::speak,
        onPause                 = viewModel::pauseSpeech,
        onStop                  = viewModel::stopSpeech,
        onChangeSpeechRate      = viewModel::changeSpeechRate,
        onChangeFontSize        = viewModel::changeFontSize,
        onChangeBrightness      = viewModel::changeBrightness,
        onApplyManualCorrection = viewModel::applyManualCorrection,
        onMessageShown          = viewModel::clearMessage,
        onVoiceRequested = {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) viewModel.startVoiceCommand()
            else micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// ReaderScreen — layout principal
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ReaderScreen(
    uiState: ReaderUiState,
    onNavigateBack: () -> Unit,
    onSpeak: (String) -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onChangeSpeechRate: (Float) -> Unit,
    onChangeFontSize: (Float) -> Unit,
    onChangeBrightness: (Float) -> Unit,
    onApplyManualCorrection: (String) -> Unit,
    onMessageShown: () -> Unit,
    onVoiceRequested: () -> Unit
) {
    val snackbar = remember { SnackbarHostState() }
    val clipboard = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var showCorrectionDialog by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var correctionDraft by remember(uiState.extractedText) { mutableStateOf(uiState.extractedText) }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { snackbar.showSnackbar(it); onMessageShown() }
    }

    val currentText = when (selectedTab) {
        1 -> uiState.summary.ifBlank { uiState.simplifiedText }
        2 -> uiState.keyPoints.joinToString("\n")
        else -> uiState.extractedText
    }

    Scaffold(
        containerColor = PrimaryBackground,
        snackbarHost = {
            SnackbarHost(snackbar) { data ->
                Snackbar(
                    snackbarData   = data,
                    containerColor = SurfaceVariant,   // fondo oscuro #222222
                    contentColor   = OnSurface,        // texto claro #E8E8E8
                    actionColor    = PrimaryYellow
                )
            }
        },
        topBar = {
            LexiTopBar(
                title          = "Lector",
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(
                        enabled  = currentText.isNotBlank(),
                        onClick  = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            clipboard.setText(AnnotatedString(currentText))
                        },
                        modifier = Modifier.semantics { contentDescription = "Copiar texto" }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null,
                            tint = if (currentText.isNotBlank()) PrimaryYellow else DisabledGray)
                    }
                    IconButton(
                        enabled  = uiState.hasText,
                        onClick  = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            correctionDraft = uiState.extractedText
                            showCorrectionDialog = true
                        },
                        modifier = Modifier.semantics { contentDescription = "Corregir texto OCR" }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null,
                            tint = if (uiState.hasText) PrimaryYellow else DisabledGray)
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingState(Modifier.padding(padding))
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // ── Cabecera con información del documento ────────────────────
            ReaderHeader(uiState, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))

            // ── Aviso de calidad OCR (solo si hay problema) ───────────────
            if (uiState.requiresReview || uiState.scanWarnings.isNotEmpty()) {
                ScanQualityBanner(
                    uiState  = uiState,
                    onReview = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        correctionDraft = uiState.extractedText
                        showCorrectionDialog = true
                    },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(8.dp))
            }

            // ── Pestañas de contenido ─────────────────────────────────────
            ContentTabs(
                selectedTab   = selectedTab,
                onTabSelected = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    selectedTab = it
                }
            )

            // ── Área de lectura (toma todo el espacio disponible) ─────────
            ReadingContent(
                selectedTab    = selectedTab,
                uiState        = uiState,
                showVoiceHelp  = !uiState.voiceNotAvailable,
                modifier       = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // ── Ajustes colapsables (disclosure progresivo) ───────────────
            SettingsSection(
                uiState            = uiState,
                isExpanded         = showSettings,
                onToggle           = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showSettings = !showSettings
                },
                onChangeSpeechRate = onChangeSpeechRate,
                onChangeFontSize   = onChangeFontSize,
                onChangeBrightness = onChangeBrightness,
                modifier           = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(8.dp))

            // ── Controles primarios — zona del pulgar ─────────────────────
            PrimaryControls(
                uiState     = uiState,
                currentText = currentText,
                onSpeak     = onSpeak,
                onPause     = onPause,
                onStop      = onStop,
                modifier    = Modifier.padding(horizontal = 16.dp)
            )

            // ── Comandos de voz ───────────────────────────────────────────
            if (!uiState.voiceNotAvailable) {
                Spacer(Modifier.height(8.dp))
                VoiceCommandButton(
                    isListening      = uiState.isVoiceListening,
                    onVoiceRequested = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onVoiceRequested()
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    if (showCorrectionDialog) {
        CorrectionDialog(
            text         = correctionDraft,
            onTextChange = { correctionDraft = it },
            onDismiss    = { showCorrectionDialog = false },
            onSave       = {
                onApplyManualCorrection(correctionDraft)
                showCorrectionDialog = false
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Componentes
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = PrimaryYellow, strokeWidth = 3.dp)
            Text(
                text      = "Procesando imagen...",
                color     = OnSurface,
                fontSize  = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ── Cabecera ─────────────────────────────────────────────────────────────────

@Composable
private fun ReaderHeader(uiState: ReaderUiState, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text       = uiState.title,
            color      = OnSurface,
            fontSize   = 18.sp,
            fontWeight = FontWeight.Bold,
            maxLines   = 2
        )
        Text(
            text     = "${uiState.wordCount} palabras  ·  ${uiState.processingTimeMs} ms",
            color    = DisabledGray,
            fontSize = 13.sp
        )
    }
}

// ── Banner de calidad OCR ─────────────────────────────────────────────────────

@Composable
private fun ScanQualityBanner(
    uiState: ReaderUiState,
    onReview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isError = uiState.requiresReview
    val borderColor = if (isError) ErrorRed else PrimaryYellow

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(10.dp)),
        shape  = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text       = if (isError) "OCR ${uiState.scanQualityScore}/100 — revisar"
                                 else "OCR ${uiState.scanQualityScore}/100",
                    color      = borderColor,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                uiState.scanWarnings.take(2).forEach { w ->
                    Text(text = w, color = DisabledGray, fontSize = 12.sp, maxLines = 1)
                }
            }
            if (isError) {
                TextButton(
                    onClick = onReview,
                    modifier = Modifier
                        .height(40.dp)
                        .semantics { contentDescription = "Corregir texto OCR" }
                ) {
                    Text("Corregir", color = ErrorRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ── Pestañas ──────────────────────────────────────────────────────────────────

private val TAB_LABELS = listOf("Texto", "Síntesis", "Claves")

@Composable
private fun ContentTabs(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    TabRow(
        selectedTabIndex  = selectedTab,
        containerColor    = PrimaryBackground,
        contentColor      = PrimaryYellow,
        indicator         = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                color    = PrimaryYellow
            )
        }
    ) {
        TAB_LABELS.forEachIndexed { index, label ->
            val isSelected = selectedTab == index
            Tab(
                selected = isSelected,
                onClick  = { onTabSelected(index) },
                modifier = Modifier
                    .height(52.dp)
                    .semantics { contentDescription = "Pestaña $label" },
                text = {
                    Text(
                        text       = label,
                        fontSize   = 14.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                        color      = if (isSelected) PrimaryYellow else DisabledGray
                    )
                }
            )
        }
    }
}

// ── Contenido de lectura ──────────────────────────────────────────────────────

@Composable
private fun ReadingContent(
    selectedTab: Int,
    uiState: ReaderUiState,
    showVoiceHelp: Boolean = false,
    modifier: Modifier = Modifier
) {
    val text = when (selectedTab) {
        1 -> buildString {
            if (uiState.summary.isNotBlank()) append(uiState.summary)
            if (uiState.simplifiedText.isNotBlank()) {
                if (isNotEmpty()) append("\n\n")
                append(uiState.simplifiedText)
            }
        }
        2 -> uiState.keyPoints.joinToString("\n\n") { "• $it" }
        else -> uiState.extractedText
    }.trim()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = Surface)
    ) {
        // Todo el contenido (texto + guía de voz) en un solo scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            if (text.isBlank()) {
                Box(
                    modifier           = Modifier.fillMaxWidth().height(160.dp),
                    contentAlignment   = Alignment.Center
                ) {
                    Text(
                        text      = "No hay contenido en esta pestaña.",
                        color     = DisabledGray,
                        textAlign = TextAlign.Center,
                        fontSize  = 14.sp
                    )
                }
            } else {
                SelectionContainer {
                    Text(
                        text       = text,
                        fontSize   = uiState.fontSize.sp,
                        lineHeight = (uiState.fontSize * 1.65f).sp,
                        color      = OnSurface
                    )
                }
            }

            // Guía de comandos de voz al final del scroll — no ocupa espacio visual fijo
            if (showVoiceHelp) {
                Spacer(Modifier.height(20.dp))
                VoiceCommandsHelpCard()
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

// ── Ajustes colapsables (disclosure progresivo) ───────────────────────────────

@Composable
private fun SettingsSection(
    uiState: ReaderUiState,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onChangeSpeechRate: (Float) -> Unit,
    onChangeFontSize: (Float) -> Unit,
    onChangeBrightness: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = Surface)
    ) {
        // Cabecera siempre visible — el IconButton al final es el control de colapsar/expandir
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text       = "Ajustes de lectura",
                color      = OnSurface,
                fontSize   = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Resumen compacto de los valores actuales
                if (!isExpanded) {
                    Text(
                        text     = "${"%.1f".format(uiState.speechRate)}×  ·  ${uiState.fontSize.toInt()}sp",
                        color    = DisabledGray,
                        fontSize = 12.sp
                    )
                }
                IconButton(
                    onClick  = onToggle,
                    modifier = Modifier
                        .size(44.dp)
                        .semantics {
                            contentDescription = if (isExpanded)
                                "Ocultar ajustes de velocidad, letra y brillo"
                            else
                                "Mostrar ajustes de velocidad, letra y brillo"
                        }
                ) {
                    Icon(
                        imageVector        = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint               = PrimaryYellow
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter   = expandVertically(),
            exit    = shrinkVertically()
        ) {
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SettingSlider(
                    label    = "Velocidad",
                    value    = uiState.speechRate,
                    display  = "${"%.2f".format(uiState.speechRate)}×",
                    onChange = onChangeSpeechRate,
                    range    = 0.25f..1.2f
                )
                SettingSlider(
                    label    = "Tamaño de letra",
                    value    = uiState.fontSize,
                    display  = "${uiState.fontSize.toInt()} sp",
                    onChange = onChangeFontSize,
                    range    = 14f..40f
                )
                val brightnessDisplay = if (uiState.screenBrightness < 0.05f) "Auto"
                                        else "${(uiState.screenBrightness * 100).toInt()}%"
                SettingSlider(
                    label    = "Brillo",
                    value    = uiState.screenBrightness.coerceAtLeast(0.05f),
                    display  = brightnessDisplay,
                    onChange = onChangeBrightness,
                    range    = 0.05f..1.0f
                )
            }
        }
    }
}

@Composable
private fun SettingSlider(
    label: String,
    value: Float,
    display: String,
    onChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = DisabledGray, fontSize = 12.sp, modifier = Modifier.width(110.dp))
        Slider(
            value         = value,
            onValueChange = onChange,
            valueRange    = range,
            modifier      = Modifier
                .weight(1f)
                .height(36.dp)
                .semantics { contentDescription = "$label: $display" }
        )
        Text(
            text     = display,
            color    = PrimaryYellow,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(44.dp),
            textAlign = TextAlign.End
        )
    }
}

// ── Controles primarios — zona del pulgar ─────────────────────────────────────
//
//   IHC: el botón LEER ocupa el mayor espacio visual y táctil.
//   Pause y Stop son acciones secundarias — tonal/outline, más pequeños.

@Composable
private fun PrimaryControls(
    uiState: ReaderUiState,
    currentText: String,
    onSpeak: (String) -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val canRead = currentText.isNotBlank()

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pausar — acción secundaria izquierda
        FilledTonalIconButton(
            onClick  = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onPause()
            },
            enabled  = uiState.isSpeaking,
            modifier = Modifier
                .size(60.dp)
                .semantics { contentDescription = "Pausar lectura" },
            colors   = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = SurfaceVariant,
                contentColor   = OnSurface,
                disabledContainerColor = SurfaceVariant.copy(alpha = 0.4f),
                disabledContentColor   = DisabledGray
            )
        ) {
            Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(28.dp))
        }

        // LEER — acción primaria, ocupa todo el espacio central
        Button(
            enabled        = canRead,
            onClick        = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onSpeak(currentText)
            },
            modifier       = Modifier
                .weight(1f)
                .height(64.dp)
                .semantics {
                    contentDescription = if (uiState.isSpeaking)
                        "Releer el texto desde el principio"
                    else
                        "Leer el texto en voz alta"
                },
            shape          = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 12.dp),
            colors         = ButtonDefaults.buttonColors(
                containerColor         = PrimaryYellow,
                contentColor           = OnPrimaryYellow,
                disabledContainerColor = SurfaceVariant,
                disabledContentColor   = DisabledGray
            ),
            elevation      = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(26.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text       = if (uiState.isSpeaking) "Releer" else "Leer",
                fontSize   = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        // Detener — acción secundaria derecha
        FilledTonalIconButton(
            onClick  = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onStop()
            },
            enabled  = uiState.isSpeaking,
            modifier = Modifier
                .size(60.dp)
                .semantics { contentDescription = "Detener lectura" },
            colors   = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = SurfaceVariant,
                contentColor   = OnSurface,
                disabledContainerColor = SurfaceVariant.copy(alpha = 0.4f),
                disabledContentColor   = DisabledGray
            )
        ) {
            Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(28.dp))
        }
    }
}

// ── Botón de comandos de voz ──────────────────────────────────────────────────

@Composable
private fun VoiceCommandButton(
    isListening: Boolean,
    onVoiceRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "voice_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue  = 1f,
        targetValue   = 0.55f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label         = "alpha"
    )

    val containerColor = if (isListening) ErrorRed else SurfaceVariant
    val contentColor   = if (isListening) Color.White else OnSurface

    OutlinedButton(
        onClick  = onVoiceRequested,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .then(if (isListening) Modifier.alpha(pulseAlpha) else Modifier)
            .semantics {
                contentDescription = if (isListening)
                    "Escuchando comando de voz. Di: leer, pausar, letra grande, más brillo…"
                else
                    "Activar comando de voz. Puedes decir: leer, pausar, letra grande, más brillo"
            },
        shape    = RoundedCornerShape(16.dp),
        colors   = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor   = contentColor
        ),
        border   = androidx.compose.foundation.BorderStroke(
            width = 1.5.dp,
            color = if (isListening) ErrorRed else PrimaryYellow.copy(alpha = 0.5f)
        )
    ) {
        Icon(
            imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text       = if (isListening) "Escuchando…  di un comando" else "Comandos de voz",
            fontSize   = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ── Guía visual de comandos de voz ───────────────────────────────────────────

private val VOICE_COMMANDS = listOf(
    "\"leer\"  o  \"reproducir\""          to "▶ Iniciar lectura",
    "\"pausar\"  o  \"espera\""            to "⏸ Pausar",
    "\"detener\"  o  \"silencio\""         to "⏹ Detener",
    "\"letra grande\"  o  \"aumenta el texto\""  to "🔠 Letra más grande",
    "\"letra pequeña\"  o  \"reduce texto\""     to "🔡 Letra más pequeña",
    "\"sube el contraste\"  o  \"más brillo\""   to "☀ Más brillo",
    "\"baja el contraste\"  o  \"menos luz\""    to "🌙 Menos brillo",
    "\"más rápido\"  o  \"acelerar\""      to "⏩ Habla más rápido",
    "\"más lento\"  o  \"despacio\""       to "⏪ Habla más lento"
)

@Composable
private fun VoiceCommandsHelpCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(10.dp),
        colors   = CardDefaults.cardColors(containerColor = SurfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text       = "¿Qué puedo decir?",
                color      = PrimaryYellow,
                fontSize   = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
            VOICE_COMMANDS.forEach { (command, description) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text     = command,
                        color    = OnSurface,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text      = description,
                        color     = DisabledGray,
                        fontSize  = 11.sp,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

// ── Diálogo de corrección ────────────────────────────────────────────────────

@Composable
private fun CorrectionDialog(
    text: String,
    onTextChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        containerColor   = Surface,
        onDismissRequest = onDismiss,
        title = {
            Text("Corregir texto OCR", color = OnSurface, fontWeight = FontWeight.Bold)
        },
        text = {
            OutlinedTextField(
                value         = text,
                onValueChange = onTextChange,
                modifier      = Modifier.fillMaxWidth().height(260.dp),
                textStyle     = MaterialTheme.typography.bodyMedium.copy(color = OnSurface),
                minLines      = 8,
                supportingText = {
                    Text(
                        "Edita aquí cuando el OCR no reconoció bien la letra manuscrita.",
                        color = DisabledGray, fontSize = 12.sp
                    )
                }
            )
        },
        confirmButton = {
            Button(
                onClick = onSave,
                colors  = ButtonDefaults.buttonColors(
                    containerColor = PrimaryYellow,
                    contentColor   = OnPrimaryYellow
                )
            ) { Text("Guardar", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = DisabledGray)
            }
        }
    )
}
