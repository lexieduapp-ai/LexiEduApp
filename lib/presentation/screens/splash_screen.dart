import 'dart:async';

import 'package:flutter/material.dart';

import '../../core/theme/app_theme.dart';
import 'camera_screen.dart';

class SplashScreen extends StatefulWidget {
  const SplashScreen({super.key});

  @override
  State<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen>
    with SingleTickerProviderStateMixin {
  late final AnimationController _ctrl;
  late final Animation<double> _fade;
  late final Animation<Offset> _slide;
  late final Animation<double> _logoPulse;
  Timer? _navTimer;

  @override
  void initState() {
    super.initState();

    _ctrl = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1400),
    );

    _fade = CurvedAnimation(parent: _ctrl, curve: Curves.easeIn);

    _slide = Tween<Offset>(
      begin: const Offset(0, 0.18),
      end: Offset.zero,
    ).animate(CurvedAnimation(parent: _ctrl, curve: Curves.easeOutCubic));

    _logoPulse = Tween<double>(begin: 0.85, end: 1.0).animate(
      CurvedAnimation(parent: _ctrl, curve: Curves.elasticOut),
    );

    _ctrl.forward();

    _navTimer = Timer(const Duration(milliseconds: 4000), _navigate);
  }

  void _navigate() {
    if (!mounted) return;
    Navigator.of(context).pushReplacement(
      PageRouteBuilder<void>(
        pageBuilder: (_, __, ___) => const CameraScreen(),
        transitionsBuilder: (_, anim, __, child) =>
            FadeTransition(opacity: anim, child: child),
        transitionDuration: const Duration(milliseconds: 500),
      ),
    );
  }

  @override
  void dispose() {
    _navTimer?.cancel();
    _ctrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppTheme.primaryBackground,
      body: SafeArea(
        child: SizedBox.expand(
          child: FadeTransition(
            opacity: _fade,
            child: SlideTransition(
              position: _slide,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                const Spacer(flex: 1),

                // Logo PUCE
                ScaleTransition(
                  scale: _logoPulse,
                  child: Image.asset(
                    'assets/images/puce_logo.png',
                    height: 52,
                    color: AppTheme.accentWhite,
                    colorBlendMode: BlendMode.modulate,
                  ),
                ),

                const SizedBox(height: 20),

                // Ícono de la app
                Container(
                  width: 96,
                  height: 96,
                  decoration: BoxDecoration(
                    color: AppTheme.primaryYellow.withValues(alpha: 0.12),
                    borderRadius: BorderRadius.circular(28),
                    border: Border.all(
                      color: AppTheme.primaryYellow.withValues(alpha: 0.55),
                      width: 2.5,
                    ),
                  ),
                  child: const Icon(
                    Icons.document_scanner_rounded,
                    color: AppTheme.primaryYellow,
                    size: 52,
                  ),
                ),

                const SizedBox(height: 28),

                // Nombre
                const Text(
                  'LexiEdu',
                  style: TextStyle(
                    color: AppTheme.primaryYellow,
                    fontSize: 46,
                    fontWeight: FontWeight.w800,
                    letterSpacing: -0.5,
                  ),
                ),

                const SizedBox(height: 10),

                const Text(
                  'Texto a voz · 100 % local · Sin internet',
                  style: TextStyle(
                    color: AppTheme.disabledGray,
                    fontSize: 16,
                    fontWeight: FontWeight.w500,
                  ),
                ),

                const Spacer(flex: 2),

                // Créditos
                Padding(
                  padding: const EdgeInsets.fromLTRB(24, 0, 24, 20),
                  child: Column(
                    children: [
                      // Badge versión
                      Container(
                        padding: const EdgeInsets.symmetric(
                            horizontal: 14, vertical: 5),
                        decoration: BoxDecoration(
                          border: Border.all(
                            color:
                                AppTheme.primaryYellow.withValues(alpha: 0.30),
                          ),
                          borderRadius: BorderRadius.circular(20),
                        ),
                        child: const Text(
                          'PMV 2 · v2.0.0',
                          style: TextStyle(
                            color: AppTheme.primaryYellow,
                            fontSize: 13,
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                      ),
                      const SizedBox(height: 8),

                      // Universidad y materia
                      const Text(
                        'Pontificia Universidad Católica del Ecuador',
                        textAlign: TextAlign.center,
                        style: TextStyle(
                          color: AppTheme.disabledGray,
                          fontSize: 12,
                          height: 1.5,
                        ),
                      ),
                      const Text(
                        'Emprendimiento Tecnológico · 2026',
                        textAlign: TextAlign.center,
                        style: TextStyle(
                          color: AppTheme.primaryYellow,
                          fontSize: 12,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                      const SizedBox(height: 10),

                      // Separador
                      Divider(
                        color: AppTheme.primaryYellow.withValues(alpha: 0.20),
                        thickness: 1,
                        indent: 40,
                        endIndent: 40,
                      ),
                      const SizedBox(height: 8),

                      // Docente
                      const Text(
                        'Docente: Francisco Clavijo',
                        style: TextStyle(
                          color: AppTheme.accentWhite,
                          fontSize: 13,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                      const SizedBox(height: 6),

                      // Desarrolladores
                      const Text(
                        'Juan C. Cevallos · Steven A. Rosero\n'
                        'Kevin D. Cepeda · Victoria Y. Galarza',
                        textAlign: TextAlign.center,
                        style: TextStyle(
                          color: AppTheme.disabledGray,
                          fontSize: 12,
                          height: 1.6,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ),
        ),
      ),
    );
  }
}
