import 'package:flutter/material.dart';

import '../theme/app_theme.dart';

/// Helpers estáticos para mostrar SnackBars consistentes en toda la app.
/// Tres tipos: error (rojo), warning (ámbar) y success (verde).
class AppSnackBar {
  const AppSnackBar._();

  // Fondos con contraste ≥ 5:1 sobre texto blanco (WCAG AA).
  static const Color _errorBg = Color(0xFFB91C1C);
  static const Color _warningBg = Color(0xFF78350F);
  static const Color _successBg = Color(0xFF14532D);

  static void showError(BuildContext context, String message) => _show(
        context,
        message: message,
        icon: Icons.error_outline_rounded,
        backgroundColor: _errorBg,
      );

  static void showWarning(BuildContext context, String message) => _show(
        context,
        message: message,
        icon: Icons.warning_amber_rounded,
        backgroundColor: _warningBg,
      );

  static void showSuccess(BuildContext context, String message) => _show(
        context,
        message: message,
        icon: Icons.check_circle_outline_rounded,
        backgroundColor: _successBg,
      );

  static void _show(
    BuildContext context, {
    required String message,
    required IconData icon,
    required Color backgroundColor,
  }) {
    ScaffoldMessenger.of(context)
      ..hideCurrentSnackBar()
      ..showSnackBar(
        SnackBar(
          content: Row(
            children: [
              Icon(icon, color: AppTheme.accentWhite, size: 24),
              const SizedBox(width: 12),
              Expanded(
                child: Text(
                  message,
                  style: const TextStyle(
                    color: AppTheme.accentWhite,
                    fontSize: 16,
                    fontWeight: FontWeight.w600,
                    height: 1.4,
                  ),
                ),
              ),
            ],
          ),
          backgroundColor: backgroundColor,
          behavior: SnackBarBehavior.floating,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(10),
          ),
          margin: const EdgeInsets.all(16),
          duration: const Duration(seconds: 4),
        ),
      );
  }
}
