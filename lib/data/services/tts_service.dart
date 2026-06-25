import 'dart:developer';

import 'package:flutter_tts/flutter_tts.dart';

class TtsService {
  TtsService() {
    _ready = _initializeTts();
  }

  final FlutterTts _flutterTts = FlutterTts();
  late final Future<void> _ready;
  bool _isPlaying = false;
  double _speechRate = 0.45;

  bool get isPlaying => _isPlaying;

  // TTS nativo: usa el motor de Android/iOS, sin servicios pagos ni red.
  Future<void> _initializeTts() async {
    try {
      await _flutterTts.setLanguage('es-ES');
      await _flutterTts.setSpeechRate(_speechRate);
      await _flutterTts.setVolume(1.0);
      await _flutterTts.setPitch(1.0);

      await _flutterTts.setSharedInstance(true);
      await _flutterTts.setIosAudioCategory(
        IosTextToSpeechAudioCategory.playback,
        [
          IosTextToSpeechAudioCategoryOptions.allowBluetooth,
          IosTextToSpeechAudioCategoryOptions.allowBluetoothA2DP,
          IosTextToSpeechAudioCategoryOptions.mixWithOthers,
        ],
        IosTextToSpeechAudioMode.voicePrompt,
      );

      _flutterTts.setStartHandler(() {
        _isPlaying = true;
      });

      _flutterTts.setCompletionHandler(() {
        _isPlaying = false;
      });

      _flutterTts.setCancelHandler(() {
        _isPlaying = false;
      });

      _flutterTts.setErrorHandler((message) {
        _isPlaying = false;
        log('Error de TTS: $message', name: 'LexiEdu.TtsService');
      });
    } catch (error, stackTrace) {
      log(
        'No se pudo inicializar el motor TTS nativo.',
        name: 'LexiEdu.TtsService',
        error: error,
        stackTrace: stackTrace,
      );
    }
  }

  Future<bool> speak(String text) async {
    final cleanText = text.trim();
    if (cleanText.isEmpty) {
      return false;
    }

    try {
      await _ready;
      await stop();
      final result = await _flutterTts.speak(cleanText);
      return result == 1;
    } catch (error, stackTrace) {
      log(
        'No se pudo reproducir el texto.',
        name: 'LexiEdu.TtsService',
        error: error,
        stackTrace: stackTrace,
      );
      return false;
    }
  }

  Future<void> pause() async {
    await _ready;
    await _flutterTts.pause();
    _isPlaying = false;
  }

  Future<void> stop() async {
    await _ready;
    await _flutterTts.stop();
    _isPlaying = false;
  }

  Future<void> setSpeechRate(double rate) async {
    _speechRate = rate.clamp(0.25, 0.8).toDouble();
    await _ready;
    await _flutterTts.setSpeechRate(_speechRate);
  }

  Future<void> setPitch(double pitch) async {
    await _ready;
    await _flutterTts.setPitch(pitch.clamp(0.5, 2.0));
  }

  Future<List<dynamic>> getVoices() async {
    await _ready;
    final voices = await _flutterTts.getVoices;
    return voices is List ? voices : [];
  }

  Future<void> setVoice(Map<String, String> voice) async {
    await _ready;
    await _flutterTts.setVoice(voice);
  }

  void dispose() {
    _flutterTts.stop();
  }
}
