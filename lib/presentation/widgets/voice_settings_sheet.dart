import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../../core/theme/app_theme.dart';
import '../../data/services/tts_service.dart';

class VoiceSettingsSheet extends StatefulWidget {
  const VoiceSettingsSheet({
    super.key,
    required this.ttsService,
    required this.initialSpeed,
    required this.initialPitch,
  });

  final TtsService ttsService;
  final double initialSpeed;
  final double initialPitch;

  @override
  State<VoiceSettingsSheet> createState() => _VoiceSettingsSheetState();
}

class _VoiceSettingsSheetState extends State<VoiceSettingsSheet> {
  late double _speed;
  late double _pitch;
  bool _isTesting = false;

  static const String _testPhrase =
      'Hola, este es el sonido de tu lector. Ajusta el tono y la velocidad hasta que te resulte cómodo.';

  @override
  void initState() {
    super.initState();
    _speed = widget.initialSpeed;
    _pitch = widget.initialPitch;
  }

  @override
  void dispose() {
    if (_isTesting) widget.ttsService.stop();
    super.dispose();
  }

  String _speedLabel(double v) {
    if (v <= 0.30) return 'Muy lenta';
    if (v <= 0.45) return 'Lenta';
    if (v <= 0.60) return 'Normal';
    return 'Rápida';
  }

  String _pitchLabel(double v) {
    if (v <= 0.75) return 'Grave';
    if (v <= 1.15) return 'Normal';
    return 'Aguda';
  }

  Future<void> _updateSpeed(double v) async {
    setState(() => _speed = v);
    await widget.ttsService.setSpeechRate(v);
  }

  Future<void> _updatePitch(double v) async {
    setState(() => _pitch = v);
    await widget.ttsService.setPitch(v);
  }

  Future<void> _setPitchPreset(double v) async {
    await HapticFeedback.selectionClick();
    await _updatePitch(v);
  }

  Future<void> _toggleTest() async {
    await HapticFeedback.selectionClick();
    if (_isTesting) {
      await widget.ttsService.stop();
      if (mounted) setState(() => _isTesting = false);
      return;
    }
    setState(() => _isTesting = true);
    final started = await widget.ttsService.speak(_testPhrase);
    if (!mounted) return;
    if (!started) setState(() => _isTesting = false);
    // Estimar cuando termina y resetear indicador
    final words = _testPhrase.split(' ').length;
    final seconds = (words / (_speed * 120)).ceil() + 1;
    Future.delayed(Duration(seconds: seconds), () {
      if (mounted) setState(() => _isTesting = false);
    });
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: const BoxDecoration(
        color: AppTheme.surface,
        borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
      ),
      padding: EdgeInsets.fromLTRB(
          24, 12, 24, 24 + MediaQuery.of(context).viewInsets.bottom),
      child: SingleChildScrollView(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Drag handle
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

            // Título
            const Row(
              children: [
                Icon(Icons.tune_rounded,
                    color: AppTheme.primaryYellow, size: 26),
                SizedBox(width: 10),
                Text(
                  'Ajustar Voz',
                  style: TextStyle(
                    color: AppTheme.primaryYellow,
                    fontSize: 24,
                    fontWeight: FontWeight.w700,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 20),

            // ── Velocidad ────────────────────────────────────────────────
            Row(
              children: [
                const Icon(Icons.speed_rounded,
                    color: AppTheme.primaryYellow, size: 20),
                const SizedBox(width: 8),
                const Text(
                  'Velocidad',
                  style: TextStyle(
                    color: AppTheme.accentWhite,
                    fontSize: 18,
                    fontWeight: FontWeight.w600,
                  ),
                ),
                const Spacer(),
                _Badge(_speedLabel(_speed)),
              ],
            ),
            Slider(
              value: _speed,
              min: 0.25,
              max: 0.8,
              divisions: 11,
              label: _speedLabel(_speed),
              semanticFormatterCallback: _speedLabel,
              onChanged: _updateSpeed,
            ),
            const SizedBox(height: 8),

            // ── Tono de voz ───────────────────────────────────────────────
            Row(
              children: [
                const Icon(Icons.graphic_eq_rounded,
                    color: AppTheme.primaryYellow, size: 20),
                const SizedBox(width: 8),
                const Text(
                  'Tono de voz',
                  style: TextStyle(
                    color: AppTheme.accentWhite,
                    fontSize: 18,
                    fontWeight: FontWeight.w600,
                  ),
                ),
                const Spacer(),
                _Badge(_pitchLabel(_pitch)),
              ],
            ),
            const SizedBox(height: 8),

            // Presets de tono
            Row(
              children: [
                Expanded(
                    child: _PitchPreset(
                        label: 'Grave',
                        value: 0.65,
                        current: _pitch,
                        onTap: _setPitchPreset)),
                const SizedBox(width: 8),
                Expanded(
                    child: _PitchPreset(
                        label: 'Normal',
                        value: 1.0,
                        current: _pitch,
                        onTap: _setPitchPreset)),
                const SizedBox(width: 8),
                Expanded(
                    child: _PitchPreset(
                        label: 'Aguda',
                        value: 1.4,
                        current: _pitch,
                        onTap: _setPitchPreset)),
              ],
            ),
            Slider(
              value: _pitch.clamp(0.5, 1.6),
              min: 0.5,
              max: 1.6,
              divisions: 22,
              label: _pitchLabel(_pitch),
              onChanged: _updatePitch,
            ),

            const Divider(color: Color(0x30FFFFFF)),
            const SizedBox(height: 12),

            // ── Probar voz ────────────────────────────────────────────────
            const Row(
              children: [
                Icon(Icons.hearing_rounded,
                    color: AppTheme.primaryYellow, size: 20),
                SizedBox(width: 8),
                Expanded(
                  child: Text(
                    'Escucha cómo suena tu lector',
                    style: TextStyle(
                      color: AppTheme.accentWhite,
                      fontSize: 16,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 10),

            ElevatedButton.icon(
              onPressed: _toggleTest,
              style: ElevatedButton.styleFrom(
                backgroundColor:
                    _isTesting ? AppTheme.errorRed : AppTheme.primaryYellow,
                foregroundColor: AppTheme.primaryBackground,
                minimumSize: const Size.fromHeight(60),
                textStyle: const TextStyle(
                    fontSize: 18, fontWeight: FontWeight.w700),
              ),
              icon: Icon(
                _isTesting ? Icons.stop_rounded : Icons.play_circle_rounded,
                semanticLabel: '',
              ),
              label: Text(_isTesting ? 'Detener prueba' : 'Probar voz'),
            ),
            const SizedBox(height: 20),

            // ── Aplicar ───────────────────────────────────────────────────
            ElevatedButton(
              onPressed: () async {
                await widget.ttsService.stop();
                if (context.mounted) {
                  Navigator.pop(context, (_speed, _pitch));
                }
              },
              child: const Text('Aplicar configuración'),
            ),
          ],
        ),
      ),
    );
  }
}

// ── Widgets auxiliares ────────────────────────────────────────────────────────

class _Badge extends StatelessWidget {
  const _Badge(this.label);
  final String label;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: AppTheme.primaryYellow.withValues(alpha: 0.15),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: AppTheme.primaryYellow.withValues(alpha: 0.4)),
      ),
      child: Text(
        label,
        style: const TextStyle(
          color: AppTheme.primaryYellow,
          fontWeight: FontWeight.w700,
          fontSize: 15,
        ),
      ),
    );
  }
}

class _PitchPreset extends StatelessWidget {
  const _PitchPreset({
    required this.label,
    required this.value,
    required this.current,
    required this.onTap,
  });

  final String label;
  final double value;
  final double current;
  final Future<void> Function(double) onTap;

  bool get _selected => (current - value).abs() < 0.2;

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: () => onTap(value),
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 150),
        padding: const EdgeInsets.symmetric(vertical: 12),
        decoration: BoxDecoration(
          color: _selected ? AppTheme.primaryYellow : Colors.transparent,
          borderRadius: BorderRadius.circular(8),
          border: Border.all(
            color: _selected ? AppTheme.primaryYellow : AppTheme.disabledGray,
            width: _selected ? 2 : 1,
          ),
        ),
        child: Text(
          label,
          textAlign: TextAlign.center,
          style: TextStyle(
            color:
                _selected ? AppTheme.primaryBackground : AppTheme.accentWhite,
            fontWeight: FontWeight.w700,
            fontSize: 17,
          ),
        ),
      ),
    );
  }
}
