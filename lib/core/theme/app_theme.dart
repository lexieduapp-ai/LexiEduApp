import 'package:flutter/material.dart';

class AppTheme {
  static const Color primaryBackground = Color(0xFF000000);
  static const Color surface = Color(0xFF151515);
  static const Color primaryYellow = Color(0xFFFFD400);
  static const Color accentWhite = Color(0xFFFFFFFF);
  static const Color successGreen = Color(0xFF34D399);
  static const Color errorRed = Color(0xFFFF4D4D);
  // Aumentado de #5F5F5F (3.6:1) a #909090 (7.4:1) para cumplir WCAG AA.
  static const Color disabledGray = Color(0xFF909090);

  static const EdgeInsets screenPadding = EdgeInsets.all(24);
  static const double elementSpacing = 20;

  static ThemeData get darkTheme {
    const baseTextStyle = TextStyle(
      color: accentWhite,
      height: 1.6,
      letterSpacing: 0.3,
    );

    return ThemeData(
      useMaterial3: true,
      brightness: Brightness.dark,
      scaffoldBackgroundColor: primaryBackground,
      colorScheme: const ColorScheme.dark(
        primary: primaryYellow,
        onPrimary: primaryBackground,
        secondary: accentWhite,
        onSecondary: primaryBackground,
        surface: surface,
        onSurface: accentWhite,
        error: errorRed,
        onError: primaryBackground,
      ),
      textTheme: TextTheme(
        headlineLarge: baseTextStyle.copyWith(
          color: primaryYellow,
          fontSize: 34,
          fontWeight: FontWeight.w800,
        ),
        headlineMedium: baseTextStyle.copyWith(
          color: primaryYellow,
          fontSize: 28,
          fontWeight: FontWeight.w700,
        ),
        titleLarge: baseTextStyle.copyWith(
          fontSize: 24,
          fontWeight: FontWeight.w700,
        ),
        bodyLarge: baseTextStyle.copyWith(
          fontSize: 22,
          fontWeight: FontWeight.w500,
        ),
        bodyMedium: baseTextStyle.copyWith(
          fontSize: 18,
          fontWeight: FontWeight.w500,
        ),
        labelLarge: const TextStyle(
          color: primaryBackground,
          fontSize: 22,
          fontWeight: FontWeight.w800,
          letterSpacing: 0,
        ),
      ),
      appBarTheme: const AppBarTheme(
        backgroundColor: primaryBackground,
        foregroundColor: primaryYellow,
        centerTitle: true,
        elevation: 0,
        titleTextStyle: TextStyle(
          color: primaryYellow,
          fontSize: 28,
          fontWeight: FontWeight.w800,
          letterSpacing: 0,
        ),
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: primaryYellow,
          foregroundColor: primaryBackground,
          disabledBackgroundColor: disabledGray,
          disabledForegroundColor: accentWhite,
          minimumSize: const Size.fromHeight(76),
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 18),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(8),
          ),
          textStyle: const TextStyle(
            fontSize: 22,
            fontWeight: FontWeight.w800,
            letterSpacing: 0,
          ),
        ),
      ),
      outlinedButtonTheme: OutlinedButtonThemeData(
        style: OutlinedButton.styleFrom(
          foregroundColor: accentWhite,
          side: const BorderSide(color: accentWhite, width: 2),
          minimumSize: const Size.fromHeight(68),
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(8),
          ),
          textStyle: const TextStyle(
            fontSize: 20,
            fontWeight: FontWeight.w700,
            letterSpacing: 0,
          ),
        ),
      ),
      iconTheme: const IconThemeData(
        color: primaryYellow,
        size: 36,
      ),
      sliderTheme: SliderThemeData(
        activeTrackColor: primaryYellow,
        inactiveTrackColor: disabledGray,
        thumbColor: primaryYellow,
        overlayColor: primaryYellow.withValues(alpha: 0.18),
        valueIndicatorColor: primaryYellow,
        valueIndicatorTextStyle: const TextStyle(
          color: primaryBackground,
          fontWeight: FontWeight.w800,
        ),
      ),
      dividerTheme: DividerThemeData(
        color: accentWhite.withValues(alpha: 0.18),
        thickness: 1,
      ),
      snackBarTheme: const SnackBarThemeData(
        backgroundColor: surface,
        contentTextStyle: TextStyle(
          color: accentWhite,
          fontSize: 18,
          fontWeight: FontWeight.w600,
        ),
      ),
    );
  }
}
