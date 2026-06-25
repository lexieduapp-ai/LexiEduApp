import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../../core/theme/app_theme.dart';
import '../../core/utils/app_snackbar.dart';
import '../../data/services/ai_service.dart';
import '../../data/services/tts_service.dart';
import '../widgets/voice_settings_sheet.dart';

class ReaderScreen extends StatefulWidget {
  const ReaderScreen({
    required this.extractedText,
    required this.imagePath,
    required this.processingMs,
    super.key,
  });

  final String extractedText;
  final String imagePath;
  final int processingMs;

  @override
  State<ReaderScreen> createState() => _ReaderScreenState();
}

class _ReaderScreenState extends State<ReaderScreen> {
  final TtsService _ttsService = TtsService();
  final AiService _aiService = AiService();
  double _speechRate = 0.45;
  double _pitch = 1.0;
  bool _isSpeaking = false;
  double _fontSize = 22;

  bool get _hasText => widget.extractedText.trim().isNotEmpty;

  int get _wordCount =>
      widget.extractedText.trim().isEmpty ? 0 : widget.extractedText.trim().split(RegExp(r'\s+')).length;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) async {
      await _speak(showFeedback: false);
    });
  }

  @override
  void dispose() {
    _ttsService.dispose();
    super.dispose();
  }

  Future<void> _speak({bool showFeedback = true}) async {
    if (!_hasText) {
      if (mounted) {
        AppSnackBar.showWarning(
          context,
          'No hay texto para leer. Vuelve atrás y captura una imagen con texto visible.',
        );
      }
      return;
    }

    await HapticFeedback.selectionClick();
    final started = await _ttsService.speak(widget.extractedText);

    if (!mounted) return;

    if (!started) {
      AppSnackBar.showError(
        context,
        'No se pudo iniciar la lectura. Comprueba el volumen del dispositivo.',
      );
      return;
    }

    setState(() {
      _isSpeaking = true;
    });

    if (showFeedback) {
      AppSnackBar.showSuccess(context, 'Lectura iniciada');
    }
  }

  Future<void> _pause() async {
    await HapticFeedback.selectionClick();
    await _ttsService.pause();

    if (!mounted) return;

    setState(() {
      _isSpeaking = false;
    });
  }

  Future<void> _stop() async {
    await HapticFeedback.mediumImpact();
    await _ttsService.stop();

    if (!mounted) return;

    setState(() {
      _isSpeaking = false;
    });
  }

  Future<void> _copyText() async {
    await HapticFeedback.selectionClick();
    await Clipboard.setData(ClipboardData(text: widget.extractedText));
    if (!mounted) return;
    AppSnackBar.showSuccess(context, 'Texto copiado al portapapeles');
  }

  Future<void> _openVoiceSettings() async {
    await HapticFeedback.selectionClick();
    if (!mounted) return;
    final result = await showModalBottomSheet<(double, double)?>(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (_) => VoiceSettingsSheet(
        ttsService: _ttsService,
        initialSpeed: _speechRate,
        initialPitch: _pitch,
      ),
    );
    if (result != null && mounted) {
      setState(() {
        _speechRate = result.$1;
        _pitch = result.$2;
      });
    }
  }

  void _showAiSheet(String title, Future<String> Function() action) {
    HapticFeedback.selectionClick();
    showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (_) => _AiResultSheet(
        title: title,
        action: action,
        ttsService: _ttsService,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final seconds = (widget.processingMs / 1000).toStringAsFixed(1);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Lectura'),
        actions: [
          IconButton(
            onPressed: () => setState(() {
              _fontSize = (_fontSize - 2).clamp(14, 36);
            }),
            icon: const Icon(Icons.text_decrease_rounded),
            tooltip: 'Reducir texto',
          ),
          IconButton(
            onPressed: () => setState(() {
              _fontSize = (_fontSize + 2).clamp(14, 36);
            }),
            icon: const Icon(Icons.text_increase_rounded),
            tooltip: 'Agrandar texto',
          ),
          if (_hasText)
            IconButton(
              onPressed: _copyText,
              icon: const Icon(Icons.copy_rounded),
              tooltip: 'Copiar texto',
            ),
        ],
      ),
      body: SafeArea(
        child: Padding(
          padding: AppTheme.screenPadding,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Text(
                'Resultado OCR',
                semanticsLabel: 'Resultado del reconocimiento de texto',
                style: Theme.of(context).textTheme.headlineMedium,
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 8),
              _StatsRow(seconds: seconds, wordCount: _wordCount),
              const SizedBox(height: AppTheme.elementSpacing),
              Expanded(
                child: Container(
                  width: double.infinity,
                  padding: const EdgeInsets.all(18),
                  decoration: BoxDecoration(
                    color: AppTheme.surface,
                    border: Border.all(color: AppTheme.accentWhite, width: 2),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: SingleChildScrollView(
                    child: _hasText
                        ? SelectableText(
                            widget.extractedText,
                            style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                                  fontSize: _fontSize,
                                ),
                          )
                        : Text(
                            'No se detectó texto en la imagen.',
                            style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                                  color: AppTheme.disabledGray,
                                  fontStyle: FontStyle.italic,
                                ),
                            textAlign: TextAlign.center,
                          ),
                  ),
                ),
              ),
              const SizedBox(height: 12),
              if (_hasText)
                Row(
                  children: [
                    Expanded(
                      child: OutlinedButton.icon(
                        onPressed: () => _showAiSheet(
                          'Síntesis IA',
                          () => _aiService.sintetizar(widget.extractedText),
                        ),
                        icon: const Icon(Icons.auto_awesome_rounded,
                            semanticLabel: ''),
                        label: const Text('Sintetizar'),
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: OutlinedButton.icon(
                        onPressed: () => _showAiSheet(
                          'Explicación IA',
                          () => _aiService.explicar(widget.extractedText),
                        ),
                        icon: const Icon(Icons.lightbulb_outline_rounded,
                            semanticLabel: ''),
                        label: const Text('Explicar'),
                      ),
                    ),
                  ],
                ),
              const SizedBox(height: 12),
              ElevatedButton.icon(
                onPressed: () => _speak(showFeedback: true),
                icon: Icon(
                  _isSpeaking ? Icons.replay_rounded : Icons.volume_up_rounded,
                  semanticLabel: '',
                ),
                label: Text(_isSpeaking ? 'Repetir lectura' : 'Leer texto'),
              ),
              const SizedBox(height: 10),
              Row(
                children: [
                  Expanded(
                    child: OutlinedButton.icon(
                      onPressed: _pause,
                      icon: const Icon(Icons.pause_rounded, semanticLabel: ''),
                      label: const Text('Pausar'),
                    ),
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: OutlinedButton.icon(
                      onPressed: _stop,
                      icon: const Icon(Icons.stop_rounded, semanticLabel: ''),
                      label: const Text('Detener'),
                    ),
                  ),
                  const SizedBox(width: 8),
                  SizedBox(
                    height: 68,
                    width: 72,
                    child: Tooltip(
                      message: 'Ajustar velocidad y tono',
                      child: OutlinedButton(
                        onPressed: _openVoiceSettings,
                        style: OutlinedButton.styleFrom(
                          padding: EdgeInsets.zero,
                        ),
                        child: const Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Icon(Icons.tune_rounded,
                                semanticLabel: 'Ajustar voz', size: 22),
                            SizedBox(height: 2),
                            Text(
                              'Voz',
                              style: TextStyle(
                                  fontSize: 12, fontWeight: FontWeight.w700),
                            ),
                          ],
                        ),
                      ),
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}

// ── Fila de estadísticas ──────────────────────────────────────────────────────

class _StatsRow extends StatelessWidget {
  const _StatsRow({required this.seconds, required this.wordCount});

  final String seconds;
  final int wordCount;

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        _StatChip(
          icon: Icons.timer_outlined,
          label: '$seconds s',
          semantics: 'Procesado en $seconds segundos',
        ),
        const SizedBox(width: 12),
        _StatChip(
          icon: Icons.text_fields_rounded,
          label: '$wordCount palabras',
          semantics: '$wordCount palabras detectadas',
        ),
      ],
    );
  }
}

class _StatChip extends StatelessWidget {
  const _StatChip({
    required this.icon,
    required this.label,
    required this.semantics,
  });

  final IconData icon;
  final String label;
  final String semantics;

  @override
  Widget build(BuildContext context) {
    return Semantics(
      label: semantics,
      excludeSemantics: true,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
        decoration: BoxDecoration(
          color: AppTheme.surface,
          borderRadius: BorderRadius.circular(20),
          border: Border.all(
            color: AppTheme.primaryYellow.withValues(alpha: 0.30),
          ),
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(icon, color: AppTheme.primaryYellow, size: 16),
            const SizedBox(width: 6),
            Text(
              label,
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                    color: AppTheme.disabledGray,
                    fontSize: 15,
                  ),
            ),
          ],
        ),
      ),
    );
  }
}

// ── Hoja de resultado de IA ───────────────────────────────────────────────────

class _AiResultSheet extends StatefulWidget {
  const _AiResultSheet({
    required this.title,
    required this.action,
    required this.ttsService,
  });

  final String title;
  final Future<String> Function() action;
  final TtsService ttsService;

  @override
  State<_AiResultSheet> createState() => _AiResultSheetState();
}

class _AiResultSheetState extends State<_AiResultSheet> {
  String? _result;
  String? _error;
  bool _loading = true;
  bool _speaking = false;

  @override
  void initState() {
    super.initState();
    _fetch();
  }

  @override
  void dispose() {
    if (_speaking) widget.ttsService.stop();
    super.dispose();
  }

  Future<void> _fetch() async {
    try {
      final text = await widget.action();
      if (!mounted) return;
      setState(() {
        _result = text;
        _loading = false;
      });
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _error = e.toString().replaceFirst('Exception: ', '');
        _loading = false;
      });
    }
  }

  Future<void> _speak() async {
    if (_result == null) return;
    await HapticFeedback.selectionClick();
    final started = await widget.ttsService.speak(_result!);
    if (!mounted) return;
    setState(() => _speaking = started);
  }

  Future<void> _stop() async {
    await widget.ttsService.stop();
    if (!mounted) return;
    setState(() => _speaking = false);
  }

  @override
  Widget build(BuildContext context) {
    return DraggableScrollableSheet(
      expand: false,
      initialChildSize: 0.60,
      minChildSize: 0.35,
      maxChildSize: 0.88,
      builder: (context, scrollController) => Container(
        decoration: const BoxDecoration(
          color: AppTheme.surface,
          borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
        ),
        child: Padding(
          padding: const EdgeInsets.fromLTRB(24, 12, 24, 24),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Center(
                child: Container(
                  width: 40,
                  height: 4,
                  decoration: BoxDecoration(
                    color: AppTheme.disabledGray,
                    borderRadius: BorderRadius.circular(2),
                  ),
                ),
              ),
              const SizedBox(height: 16),
              Row(
                children: [
                  const Icon(Icons.auto_awesome_rounded,
                      color: AppTheme.primaryYellow, size: 28),
                  const SizedBox(width: 10),
                  Expanded(
                    child: Text(
                      widget.title,
                      style: Theme.of(context).textTheme.headlineMedium,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 16),
              Expanded(
                child: _loading
                    ? const Center(
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            CircularProgressIndicator(
                                color: AppTheme.primaryYellow),
                            SizedBox(height: 16),
                            Text(
                              'Procesando con IA...',
                              style:
                                  TextStyle(color: AppTheme.disabledGray, fontSize: 18),
                            ),
                          ],
                        ),
                      )
                    : _error != null
                        ? Center(
                            child: Column(
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: [
                                const Icon(Icons.error_outline_rounded,
                                    color: AppTheme.errorRed, size: 48),
                                const SizedBox(height: 12),
                                Text(
                                  _error!,
                                  textAlign: TextAlign.center,
                                  style: const TextStyle(
                                      color: AppTheme.errorRed, fontSize: 18),
                                ),
                                const SizedBox(height: 20),
                                OutlinedButton.icon(
                                  onPressed: () {
                                    setState(() {
                                      _loading = true;
                                      _error = null;
                                    });
                                    _fetch();
                                  },
                                  icon: const Icon(Icons.refresh_rounded,
                                      semanticLabel: ''),
                                  label: const Text('Reintentar'),
                                ),
                              ],
                            ),
                          )
                        : SingleChildScrollView(
                            controller: scrollController,
                            child: Container(
                              padding: const EdgeInsets.all(16),
                              decoration: BoxDecoration(
                                color: AppTheme.primaryBackground,
                                border: Border.all(
                                  color: AppTheme.primaryYellow
                                      .withValues(alpha: 0.4),
                                ),
                                borderRadius: BorderRadius.circular(8),
                              ),
                              child: SelectableText(
                                _result!,
                                style: const TextStyle(
                                  color: AppTheme.accentWhite,
                                  fontSize: 20,
                                  height: 1.7,
                                ),
                              ),
                            ),
                          ),
              ),
              if (!_loading && _error == null) ...[
                const SizedBox(height: 12),
                ElevatedButton.icon(
                  onPressed: _speaking ? _stop : _speak,
                  icon: Icon(
                    _speaking ? Icons.stop_rounded : Icons.volume_up_rounded,
                    semanticLabel: '',
                  ),
                  label: Text(_speaking ? 'Detener lectura' : 'Leer resultado'),
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }
}
