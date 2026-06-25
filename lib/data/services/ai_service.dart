import 'dart:math' as math;

/// Sintetizador y explicador de texto 100 % local — sin internet, sin API.
///
/// Usa puntuación TF (frecuencia de términos) para seleccionar
/// las oraciones más representativas del texto original.
class AiService {
  static const Set<String> _stopwords = {
    'de', 'la', 'el', 'en', 'y', 'a', 'que', 'es', 'se', 'no', 'un',
    'una', 'los', 'las', 'por', 'con', 'del', 'al', 'lo', 'como', 'su',
    'para', 'más', 'pero', 'o', 'si', 'le', 'me', 'mi', 'te', 'tu',
    'nos', 'hay', 'ya', 'bien', 'vez', 'fue', 'ser', 'ha', 'han', 'son',
    'están', 'este', 'esta', 'estos', 'estas', 'ese', 'esa', 'esos',
    'esas', 'cual', 'donde', 'cuando', 'porque', 'sino', 'también',
    'sobre', 'entre', 'hasta', 'desde', 'hacia', 'durante', 'mediante',
    'ante', 'bajo', 'contra', 'sin', 'según', 'tras', 'muy', 'todo',
    'toda', 'todos', 'todas', 'otro', 'otra', 'otros', 'otras',
    'mismo', 'misma', 'mismos', 'mismas', 'cada', 'cualquier',
  };

  /// Extrae las oraciones clave del texto (resumen extractivo TF).
  Future<String> sintetizar(String texto) async {
    final trimmed = texto.trim();
    if (trimmed.isEmpty) return 'No hay texto para sintetizar.';

    final sentences = _splitSentences(trimmed);
    if (sentences.length <= 3) return trimmed;

    final freq = _wordFrequency(trimmed);
    final maxFreq = freq.values.fold(1, math.max<int>);

    final scored = sentences.asMap().entries.map((e) {
      final score =
          _scoreSentence(e.value, freq, maxFreq, e.key, sentences.length);
      return (idx: e.key, score: score);
    }).toList()
      ..sort((a, b) => b.score.compareTo(a.score));

    final topCount = math.max(2, (sentences.length * 0.4).ceil().clamp(2, 5));
    final topIndices = scored.take(topCount).map((e) => e.idx).toSet();

    return sentences
        .asMap()
        .entries
        .where((e) => topIndices.contains(e.key))
        .map((e) => e.value)
        .join(' ');
  }

  /// Simplifica el texto rompiendo oraciones largas y añadiendo contexto.
  Future<String> explicar(String texto) async {
    final trimmed = texto.trim();
    if (trimmed.isEmpty) return 'No hay texto para explicar.';

    final sentences = _splitSentences(trimmed);
    final simplified = <String>[];

    for (final s in sentences) {
      final words = s.split(RegExp(r'\s+'));
      if (words.length > 22) {
        final parts = s
            .split(RegExp(
                r',\s*(?=(?:que|y|pero|porque|aunque|mientras|cuando|si)\s)'))
            .map((p) => p.trim())
            .where((p) => p.isNotEmpty);
        simplified.addAll(parts);
      } else {
        simplified.add(s.trim());
      }
    }

    return 'En palabras simples:\n\n${simplified.join('. ')}';
  }

  List<String> _splitSentences(String text) => text
      .split(RegExp(r'(?<=[.!?])\s+'))
      .map((s) => s.trim())
      .where((s) => s.length > 15)
      .toList();

  Map<String, int> _wordFrequency(String text) {
    final freq = <String, int>{};
    for (final w in text
        .toLowerCase()
        .replaceAll(RegExp(r'[^\w\sáéíóúüñáéíóúüñ]'), ' ')
        .split(RegExp(r'\s+'))
        .where((w) => w.length > 3 && !_stopwords.contains(w))) {
      freq[w] = (freq[w] ?? 0) + 1;
    }
    return freq;
  }

  double _scoreSentence(
    String sentence,
    Map<String, int> freq,
    int maxFreq,
    int index,
    int total,
  ) {
    final words = sentence
        .toLowerCase()
        .replaceAll(RegExp(r'[^\w\sáéíóúüñ]'), ' ')
        .split(RegExp(r'\s+'))
        .where((w) => w.length > 3)
        .toList();

    if (words.isEmpty) return 0;

    final score = words
            .map((w) => (freq[w] ?? 0) / maxFreq)
            .fold(0.0, (a, b) => a + b) /
        words.length;

    final posBoost = (index == 0 || index == total - 1) ? 1.25 : 1.0;
    return score * posBoost;
  }
}
