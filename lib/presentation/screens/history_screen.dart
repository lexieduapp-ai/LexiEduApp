import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:hive_flutter/hive_flutter.dart';

import '../../core/theme/app_theme.dart';
import '../../core/utils/app_snackbar.dart';
import 'reader_screen.dart';

class HistoryScreen extends StatefulWidget {
  const HistoryScreen({super.key});

  @override
  State<HistoryScreen> createState() => _HistoryScreenState();
}

class _HistoryScreenState extends State<HistoryScreen> {
  late final Box<Map> _box;
  late List<MapEntry<dynamic, Map>> _entries;

  @override
  void initState() {
    super.initState();
    _box = Hive.box<Map>('reading_history');
    _reload();
  }

  void _reload() {
    setState(() {
      _entries = _box.toMap().entries.toList().reversed.toList();
    });
  }

  Future<void> _delete(dynamic key) async {
    await HapticFeedback.mediumImpact();
    await _box.delete(key);
    _reload();
    if (mounted) AppSnackBar.showSuccess(context, 'Lectura eliminada');
  }

  Future<void> _clearAll() async {
    final confirm = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: AppTheme.surface,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        title: Text('Borrar todo',
            style: Theme.of(ctx).textTheme.titleLarge),
        content: Text(
          '¿Eliminar todo el historial de lecturas? Esta acción no se puede deshacer.',
          style: Theme.of(ctx).textTheme.bodyMedium,
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(ctx).pop(false),
            child: const Text('Cancelar'),
          ),
          TextButton(
            onPressed: () => Navigator.of(ctx).pop(true),
            child: const Text('Borrar todo',
                style: TextStyle(color: AppTheme.errorRed)),
          ),
        ],
      ),
    );
    if (confirm != true) return;
    await _box.clear();
    _reload();
    if (mounted) AppSnackBar.showSuccess(context, 'Historial borrado');
  }

  String _formatDate(String iso) {
    try {
      final dt = DateTime.parse(iso).toLocal();
      const meses = [
        '', 'ene', 'feb', 'mar', 'abr', 'may', 'jun',
        'jul', 'ago', 'sep', 'oct', 'nov', 'dic'
      ];
      return '${dt.day} ${meses[dt.month]} ${dt.year}  '
          '${dt.hour.toString().padLeft(2, '0')}:'
          '${dt.minute.toString().padLeft(2, '0')}';
    } catch (_) {
      return '';
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Historial'),
        actions: [
          if (_entries.isNotEmpty)
            IconButton(
              onPressed: _clearAll,
              icon: const Icon(Icons.delete_sweep_outlined),
              tooltip: 'Borrar todo',
            ),
        ],
      ),
      body: _entries.isEmpty
          ? const _EmptyState()
          : ListView.separated(
              padding: AppTheme.screenPadding,
              itemCount: _entries.length,
              separatorBuilder: (_, __) => const SizedBox(height: 12),
              itemBuilder: (_, i) {
                final entry = _entries[i];
                final data = Map<String, dynamic>.from(entry.value);
                final text = (data['text'] as String?) ?? '';
                final date = _formatDate((data['createdAt'] as String?) ?? '');
                final ms = (data['processingMs'] as int?) ?? 0;
                final words = text.trim().isEmpty
                    ? 0
                    : text.trim().split(RegExp(r'\s+')).length;
                final preview =
                    text.length > 100 ? '${text.substring(0, 100)}…' : text;

                return _HistoryCard(
                  preview: preview,
                  date: date,
                  wordCount: words,
                  processingMs: ms,
                  onTap: () => Navigator.of(context).push(
                    MaterialPageRoute<void>(
                      builder: (_) => ReaderScreen(
                        extractedText: text,
                        imagePath: (data['imagePath'] as String?) ?? '',
                        processingMs: ms,
                      ),
                    ),
                  ),
                  onDelete: () => _delete(entry.key),
                );
              },
            ),
    );
  }
}

// ── Tarjeta de historial ──────────────────────────────────────────────────────

class _HistoryCard extends StatelessWidget {
  const _HistoryCard({
    required this.preview,
    required this.date,
    required this.wordCount,
    required this.processingMs,
    required this.onTap,
    required this.onDelete,
  });

  final String preview;
  final String date;
  final int wordCount;
  final int processingMs;
  final VoidCallback onTap;
  final VoidCallback onDelete;

  @override
  Widget build(BuildContext context) {
    return Semantics(
      button: true,
      label: 'Lectura del $date. $wordCount palabras. Toca para escuchar.',
      child: Material(
        color: AppTheme.surface,
        borderRadius: BorderRadius.circular(12),
        child: InkWell(
          onTap: onTap,
          borderRadius: BorderRadius.circular(12),
          child: Container(
            padding: const EdgeInsets.fromLTRB(16, 14, 12, 14),
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(12),
              border: Border.all(
                color: AppTheme.primaryYellow.withValues(alpha: 0.20),
              ),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Fecha + botón eliminar
                Row(
                  children: [
                    const Icon(Icons.schedule_outlined,
                        color: AppTheme.disabledGray, size: 16),
                    const SizedBox(width: 6),
                    Expanded(
                      child: Text(
                        date,
                        style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                              color: AppTheme.disabledGray,
                              fontSize: 14,
                            ),
                      ),
                    ),
                    GestureDetector(
                      onTap: onDelete,
                      child: const Icon(Icons.close,
                          color: AppTheme.disabledGray, size: 20),
                    ),
                  ],
                ),
                const SizedBox(height: 10),

                // Preview del texto
                Text(
                  preview,
                  style: Theme.of(context).textTheme.bodyMedium,
                  maxLines: 3,
                  overflow: TextOverflow.ellipsis,
                ),
                const SizedBox(height: 12),

                // Stats + ícono de play
                Row(
                  children: [
                    _StatChip(
                      icon: Icons.text_fields_rounded,
                      label: '$wordCount palabras',
                    ),
                    const SizedBox(width: 10),
                    _StatChip(
                      icon: Icons.timer_outlined,
                      label: '${(processingMs / 1000).toStringAsFixed(1)} s',
                    ),
                    const Spacer(),
                    Container(
                      padding: const EdgeInsets.symmetric(
                          horizontal: 12, vertical: 6),
                      decoration: BoxDecoration(
                        color: AppTheme.primaryYellow.withValues(alpha: 0.12),
                        borderRadius: BorderRadius.circular(20),
                        border: Border.all(
                          color: AppTheme.primaryYellow.withValues(alpha: 0.35),
                        ),
                      ),
                      child: const Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Icon(Icons.play_arrow_rounded,
                              color: AppTheme.primaryYellow, size: 18),
                          SizedBox(width: 4),
                          Text(
                            'Escuchar',
                            style: TextStyle(
                              color: AppTheme.primaryYellow,
                              fontSize: 13,
                              fontWeight: FontWeight.w700,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

class _StatChip extends StatelessWidget {
  const _StatChip({required this.icon, required this.label});
  final IconData icon;
  final String label;

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Icon(icon, size: 14, color: AppTheme.disabledGray),
        const SizedBox(width: 4),
        Text(
          label,
          style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                color: AppTheme.disabledGray,
                fontSize: 14,
              ),
        ),
      ],
    );
  }
}

// ── Estado vacío ──────────────────────────────────────────────────────────────

class _EmptyState extends StatelessWidget {
  const _EmptyState();

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: AppTheme.screenPadding,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(
              Icons.history_rounded,
              size: 72,
              color: AppTheme.disabledGray.withValues(alpha: 0.50),
            ),
            const SizedBox(height: 20),
            Text(
              'Sin lecturas guardadas',
              style: Theme.of(context).textTheme.titleLarge?.copyWith(
                    color: AppTheme.disabledGray,
                  ),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 10),
            Text(
              'Las lecturas que hagas aparecerán aquí\npara que puedas escucharlas de nuevo.',
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
