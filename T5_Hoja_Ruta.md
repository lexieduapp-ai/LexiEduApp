# T5 - Hoja de Ruta Técnica

## Pantallas del PMV

| Pantalla | Objetivo | Componentes principales |
| --- | --- | --- |
| Cámara | Capturar una imagen del texto impreso | Botón de cámara, selector de galería, indicador de procesamiento, feedback háptico |
| Lectura | Mostrar y leer el texto extraído | Texto OCR, botón leer/repetir, pausar, detener, control de velocidad |

## Privacidad y ausencia de endpoints

IncluApp no consume endpoints HTTP, no envía imágenes a servidores y no usa servicios externos de autenticación. El flujo completo ocurre en el dispositivo:

1. Captura local de imagen.
2. OCR local con `google_mlkit_text_recognition`.
3. Síntesis de voz con `flutter_tts`.
4. Persistencia local opcional con Hive.

Esta decisión reduce riesgo de exposición de datos escolares y sostiene el objetivo de costo operativo de USD 0.

## Base de datos local

Para el PMV se usa Hive como almacenamiento local ligero. La caja `reading_history` guarda:

- Texto extraído.
- Ruta local de la imagen.
- Tiempo de procesamiento.
- Fecha de creación.

En una iteración posterior, SQLite puede reemplazar o complementar Hive si se requiere búsqueda avanzada, sincronización manual exportable o reportes estructurados.

## Plan de pruebas de usabilidad

| Prueba | Método | Criterio de éxito |
| --- | --- | --- |
| Captura con buena luz | Estudiante captura una hoja impresa | OCR obtiene texto legible en menos de 3 segundos |
| Captura con baja luz | Estudiante repite captura en condiciones no ideales | La app entrega mensaje claro o texto parcialmente útil |
| Lectura en voz alta | Estudiante escucha el resultado OCR | Puede iniciar, pausar y detener sin ayuda |
| Baja visión | Usuario navega con alto contraste | Identifica botones principales a distancia corta |
| Dislexia | Usuario ajusta velocidad de lectura | Encuentra una velocidad cómoda para comprender |
| Sin internet | Modo avión activado antes de usar la app | OCR y TTS siguen funcionando |

## Iteraciones previstas

- Mejorar recorte automático del documento.
- Agregar historial visual con búsqueda local.
- Permitir exportar lecturas como archivo de texto.
- Evaluar fuente OpenDyslexic como opción configurable.
- Medir precisión OCR con muestras reales del Colegio Fe y Alegría.
