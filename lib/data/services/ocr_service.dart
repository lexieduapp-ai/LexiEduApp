import 'dart:io';

import 'package:google_mlkit_text_recognition/google_mlkit_text_recognition.dart';

class OcrService {
  OcrService()
      : _textRecognizer = TextRecognizer(
          script: TextRecognitionScript.latin,
        );

  final TextRecognizer _textRecognizer;

  /// Devuelve el texto extraído, cadena vacía si no hay texto, o lanza excepción si falla.
  Future<String> extractTextFromImage(String imagePath) async {
    final inputImage = InputImage.fromFile(File(imagePath));
    final recognizedText = await _textRecognizer.processImage(inputImage);

    if (recognizedText.text.trim().isEmpty) return '';

    final allLines = recognizedText.blocks.expand((b) => b.lines).toList();
    return _sortByReadingOrder(allLines).map((l) => l.text).join('\n').trim();
  }

  /// Groups lines into horizontal row bands by vertical overlap, then sorts
  /// each band left-to-right and outputs bands top-to-bottom — matching the
  /// natural left-to-right, line-by-line reading order humans use.
  List<TextLine> _sortByReadingOrder(List<TextLine> lines) {
    if (lines.isEmpty) return lines;

    final byY = [...lines]..sort(
        (a, b) => (a.boundingBox.top).compareTo(b.boundingBox.top),
      );

    final rows = <List<TextLine>>[];
    for (final line in byY) {
      final lineTop = line.boundingBox.top;
      final lineBottom = line.boundingBox.bottom;
      final lineHeight = (lineBottom - lineTop).abs().clamp(1, double.infinity);

      var placed = false;
      for (final row in rows) {
        final rep = row.first;
        final repTop = rep.boundingBox.top;
        final repBottom = rep.boundingBox.bottom;
        final repHeight =
            (repBottom - repTop).abs().clamp(1, double.infinity);

        final overlapTop = lineTop > repTop ? lineTop : repTop;
        final overlapBottom = lineBottom < repBottom ? lineBottom : repBottom;
        final overlap = (overlapBottom - overlapTop).clamp(0, double.infinity);
        final threshold =
            (lineHeight < repHeight ? lineHeight : repHeight) * 0.4;

        if (overlap >= threshold) {
          row.add(line);
          placed = true;
          break;
        }
      }

      if (!placed) rows.add([line]);
    }

    rows.sort((a, b) => a.first.boundingBox.top.compareTo(b.first.boundingBox.top));
    return rows
        .expand(
          (row) =>
              row..sort((a, b) => a.boundingBox.left.compareTo(b.boundingBox.left)),
        )
        .toList();
  }

  void dispose() {
    _textRecognizer.close();
  }
}
