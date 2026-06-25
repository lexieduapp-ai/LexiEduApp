import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:hive_flutter/hive_flutter.dart';
import 'package:image_picker/image_picker.dart';
import 'package:permission_handler/permission_handler.dart';

import '../../core/theme/app_theme.dart';
import '../../core/utils/app_snackbar.dart';
import '../../data/services/ocr_service.dart';
import '../widgets/feature_card.dart';
import 'help_screen.dart';
import 'history_screen.dart';
import 'reader_screen.dart';

class CameraScreen extends StatefulWidget {
  const CameraScreen({super.key});

  @override
  State<CameraScreen> createState() => _CameraScreenState();
}

class _CameraScreenState extends State<CameraScreen> {
  final ImagePicker _imagePicker = ImagePicker();
  final OcrService _ocrService = OcrService();

  bool _isProcessing = false;
  String? _statusMessage;

  @override
  void dispose() {
    _ocrService.dispose();
    super.dispose();
  }

  Future<void> _captureFromCamera() async {
    await _pickAndProcessImage(ImageSource.camera);
  }

  Future<void> _pickFromGallery() async {
    await _pickAndProcessImage(ImageSource.gallery);
  }

  Future<void> _pickAndProcessImage(ImageSource source) async {
    await HapticFeedback.mediumImpact();

    final hasPermission = await _ensurePermission(source);
    if (!hasPermission) return;

    // Para cámara: sin compresión post-captura (evita null en Android).
    // Para galería: comprimir para reducir tiempo de OCR.
    final image = source == ImageSource.camera
        ? await _imagePicker.pickImage(source: source)
        : await _imagePicker.pickImage(
            source: source,
            imageQuality: 92,
            maxWidth: 1800,
          );

    if (image == null) return;

    setState(() {
      _isProcessing = true;
      _statusMessage = 'Procesando texto en el dispositivo...';
    });

    final stopwatch = Stopwatch()..start();

    String extractedText;
    try {
      extractedText = await _ocrService.extractTextFromImage(image.path);
    } catch (e) {
      stopwatch.stop();
      if (!mounted) return;
      setState(() {
        _isProcessing = false;
        _statusMessage = null;
      });
      AppSnackBar.showError(
        context,
        'No se pudo analizar la imagen. Intenta con una foto más clara.',
      );
      return;
    }

    stopwatch.stop();
    if (!mounted) return;

    if (extractedText.trim().isEmpty) {
      setState(() {
        _isProcessing = false;
        _statusMessage = null;
      });
      AppSnackBar.showWarning(
        context,
        'No se detectó texto. Prueba con mejor iluminación o elige otra imagen.',
      );
      return;
    }

    await _saveReading(
      text: extractedText,
      imagePath: image.path,
      processingMs: stopwatch.elapsedMilliseconds,
    );

    setState(() {
      _isProcessing = false;
      _statusMessage = null;
    });

    await HapticFeedback.heavyImpact();
    if (!mounted) return;

    await Navigator.of(context).push(
      MaterialPageRoute<void>(
        builder: (_) => ReaderScreen(
          extractedText: extractedText,
          imagePath: image.path,
          processingMs: stopwatch.elapsedMilliseconds,
        ),
      ),
    );
  }

  Future<bool> _ensurePermission(ImageSource source) async {
    if (source != ImageSource.camera) return true;

    final status = await Permission.camera.request();

    if (status.isGranted || status.isLimited) return true;

    if (!mounted) return false;

    if (status.isPermanentlyDenied) {
      _showPermissionDialog();
    } else {
      AppSnackBar.showError(
        context,
        'Permiso de cámara denegado. Revisa los ajustes del dispositivo.',
      );
    }
    return false;
  }

  void _showPermissionDialog() {
    showDialog<void>(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: AppTheme.surface,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        title: Row(
          children: [
            const Icon(
              Icons.camera_alt_outlined,
              color: AppTheme.primaryYellow,
              size: 28,
            ),
            const SizedBox(width: 12),
            Text(
              'Permiso de cámara',
              style: Theme.of(ctx).textTheme.titleLarge,
            ),
          ],
        ),
        content: Text(
          'LexiEdu necesita acceso a la cámara para capturar texto. '
          'Habilita el permiso en los ajustes del dispositivo.',
          style: Theme.of(ctx).textTheme.bodyMedium,
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(ctx).pop(),
            child: const Text('Cancelar'),
          ),
          TextButton(
            onPressed: () {
              Navigator.of(ctx).pop();
              openAppSettings();
            },
            child: const Text('Abrir ajustes'),
          ),
        ],
      ),
    );
  }

  Future<void> _saveReading({
    required String text,
    required String imagePath,
    required int processingMs,
  }) async {
    try {
      final box = Hive.box<Map>('reading_history');
      await box.add({
        'text': text,
        'imagePath': imagePath,
        'processingMs': processingMs,
        'createdAt': DateTime.now().toIso8601String(),
      });
    } catch (_) {}
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('LexiEdu'),
        actions: [
          IconButton(
            onPressed: () => Navigator.of(context).push(
              MaterialPageRoute<void>(builder: (_) => const HistoryScreen()),
            ),
            icon: const Icon(Icons.history_rounded),
            tooltip: 'Historial',
          ),
          IconButton(
            onPressed: () => Navigator.of(context).push(
              MaterialPageRoute<void>(builder: (_) => const HelpScreen()),
            ),
            icon: const Icon(Icons.help_outline_rounded),
            tooltip: 'Ayuda',
          ),
        ],
      ),
      body: SafeArea(
        child: Padding(
          padding: AppTheme.screenPadding,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              const _AppHero(),
              const SizedBox(height: 16),
              const _FeatureCardsRow(),
              const SizedBox(height: 16),
              Expanded(
                child: Center(
                  child: AnimatedSwitcher(
                    duration: const Duration(milliseconds: 300),
                    child: _isProcessing
                        ? const _ProcessingState()
                        : _ReadyState(onTap: _captureFromCamera),
                  ),
                ),
              ),
              if (_statusMessage != null) ...[
                Text(
                  _statusMessage!,
                  style: Theme.of(context).textTheme.bodyMedium,
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 16),
              ],
              const _ButtonDivider(),
              const SizedBox(height: 12),
              ElevatedButton.icon(
                onPressed: _isProcessing ? null : _captureFromCamera,
                icon: const Icon(Icons.photo_camera, size: 34, semanticLabel: ''),
                label: const Text('Abrir cámara'),
              ),
              const SizedBox(height: 14),
              OutlinedButton.icon(
                onPressed: _isProcessing ? null : _pickFromGallery,
                icon: const Icon(Icons.image_search, size: 32, semanticLabel: ''),
                label: const Text('Elegir imagen'),
              ),
              const SizedBox(height: 20),
              const _PuceBadge(),
            ],
          ),
        ),
      ),
    );
  }
}

// ── Hero ──────────────────────────────────────────────────────────────────────

class _AppHero extends StatelessWidget {
  const _AppHero();

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        ExcludeSemantics(
          child: Container(
            width: 62,
            height: 62,
            decoration: BoxDecoration(
              color: AppTheme.primaryYellow.withValues(alpha: 0.12),
              borderRadius: BorderRadius.circular(18),
              border: Border.all(
                color: AppTheme.primaryYellow.withValues(alpha: 0.40),
                width: 1.5,
              ),
            ),
            child: const Icon(
              Icons.document_scanner,
              color: AppTheme.primaryYellow,
              size: 34,
            ),
          ),
        ),
        const SizedBox(width: 16),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                'LexiEdu',
                style: Theme.of(context).textTheme.headlineLarge,
              ),
              const SizedBox(height: 4),
              Text(
                'Convierte texto a voz\nsin internet ni servidores',
                style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                      color: AppTheme.disabledGray,
                    ),
              ),
            ],
          ),
        ),
      ],
    );
  }
}

// ── Tarjetas de funciones ─────────────────────────────────────────────────────

class _FeatureCardsRow extends StatelessWidget {
  const _FeatureCardsRow();

  @override
  Widget build(BuildContext context) {
    return const IntrinsicHeight(
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Expanded(
            child: FeatureCard(
              icon: Icons.wifi_off_rounded,
              title: 'Sin\ninternet',
              description: 'OCR local',
            ),
          ),
          SizedBox(width: 10),
          Expanded(
            child: FeatureCard(
              icon: Icons.record_voice_over_rounded,
              title: 'Voz\nnatural',
              description: 'TTS nativo',
            ),
          ),
          SizedBox(width: 10),
          Expanded(
            child: FeatureCard(
              icon: Icons.lock_outline_rounded,
              title: 'Privado',
              description: 'Solo local',
            ),
          ),
        ],
      ),
    );
  }
}

// ── Badge PUCE ────────────────────────────────────────────────────────────────

class _PuceBadge extends StatelessWidget {
  const _PuceBadge();

  @override
  Widget build(BuildContext context) {
    return ExcludeSemantics(
      child: Center(
        child: Image.asset(
          'assets/images/puce_logo.png',
          height: 36,
          fit: BoxFit.contain,
          color: AppTheme.disabledGray,
          colorBlendMode: BlendMode.modulate,
        ),
      ),
    );
  }
}

// ── Estado: listo ─────────────────────────────────────────────────────────────

class _ReadyState extends StatelessWidget {
  const _ReadyState({required this.onTap});

  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return Semantics(
      label: 'Abrir cámara para capturar texto',
      button: true,
      child: GestureDetector(
        onTap: onTap,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              width: 80,
              height: 80,
              decoration: BoxDecoration(
                color: AppTheme.primaryYellow.withValues(alpha: 0.10),
                shape: BoxShape.circle,
                border: Border.all(
                  color: AppTheme.primaryYellow.withValues(alpha: 0.70),
                  width: 2.5,
                ),
              ),
              child: const Icon(
                Icons.photo_camera_outlined,
                size: 40,
                color: AppTheme.primaryYellow,
              ),
            ),
            const SizedBox(height: 12),
            Text(
              'Toca para abrir la cámara',
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                    color: AppTheme.disabledGray,
                  ),
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }
}

// ── Estado: procesando OCR ────────────────────────────────────────────────────

class _ProcessingState extends StatelessWidget {
  const _ProcessingState();

  @override
  Widget build(BuildContext context) {
    return Semantics(
      label: 'Procesando texto, por favor espera',
      liveRegion: true,
      excludeSemantics: true,
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          const SizedBox(
            width: 72,
            height: 72,
            child: CircularProgressIndicator(strokeWidth: 7),
          ),
          const SizedBox(height: 16),
          Text(
            'Analizando imagen...',
            style: Theme.of(context).textTheme.bodyLarge,
          ),
        ],
      ),
    );
  }
}

// ── Separador entre botones ───────────────────────────────────────────────────

class _ButtonDivider extends StatelessWidget {
  const _ButtonDivider();

  @override
  Widget build(BuildContext context) {
    return ExcludeSemantics(
      child: Row(
        children: [
          const Expanded(child: Divider()),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 14),
            child: Text(
              'o',
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                    color: AppTheme.disabledGray,
                  ),
            ),
          ),
          const Expanded(child: Divider()),
        ],
      ),
    );
  }
}
