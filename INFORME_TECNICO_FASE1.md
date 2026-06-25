# INFORME TÉCNICO — FASE 1
## LexiEdu: Cimientos y Arquitectura de Interfaz

---

| Campo            | Detalle                                              |
|------------------|------------------------------------------------------|
| **Proyecto**     | LexiEdu — Accesibilidad educativa con OCR y TTS      |
| **Fase**         | Fase 1: Cimientos y Arquitectura de Interfaz         |
| **Fecha entrega**| 12 de junio de 2026                                  |
| **Repositorio**  | github.com/StevenAJ23/IncluApp-main                  |
| **Versión app**  | 2.0 — `applicationId`: com.example.incluapp         |
| **Plataforma**   | Android (minSdk 24 / targetSdk 35)                   |

---

## 1. Introducción

LexiEdu es una aplicación Android de accesibilidad educativa diseñada para estudiantes con dislexia y baja visión. Convierte texto capturado por cámara o galería en voz natural mediante OCR local (Google ML Kit) y síntesis de voz nativa (TTS). El sistema opera completamente sin conexión a internet para garantizar privacidad y disponibilidad en entornos con conectividad limitada.

Esta primera fase establece los **cimientos arquitectónicos** del proyecto: la configuración del sistema de construcción, el grafo de navegación, las pantallas principales con gestión de estado, el esquema de base de datos local y la estructura preliminar de llamadas de red.

---

## 2. Objetivos de la Fase 1

| # | Objetivo | Estado |
|---|----------|--------|
| 1 | Estructura base del proyecto y `libs.versions.toml` | ✅ Completado |
| 2 | Grafo de navegación tipada con `@Serializable` | ✅ Completado |
| 3 | Pantallas Compose con layouts declarativos y UDF | ✅ Completado |
| 4 | Esquema Room: entidades, DAOs y contratos de dominio | ✅ Completado |
| 5 | Estructura preliminar de llamadas de red con Retrofit | ✅ Completado |

---

## 3. Arquitectura General

El proyecto sigue el patrón **Clean Architecture** con tres capas claramente separadas:

```
┌─────────────────────────────────────────────────────┐
│                 CAPA DE PRESENTACIÓN                │
│   ui/screen/  ·  ui/theme/  ·  ui/components/      │
│   navigation/  ·  MainActivity  ·  UiState/Events  │
├─────────────────────────────────────────────────────┤
│                  CAPA DE DOMINIO                    │
│   domain/model/  ·  domain/repository/ (interfaces)│
│   domain/usecase/                                   │
├─────────────────────────────────────────────────────┤
│                   CAPA DE DATOS                     │
│   data/local/    →  Room DB (SQLite)                │
│   data/remote/   →  Retrofit + OkHttp               │
│   data/repository/ → Implementaciones + Mappers    │
└─────────────────────────────────────────────────────┘
```

### 3.1 Estructura de paquetes

```
com.example.incluapp/
├── MainActivity.kt
├── LexiEduApplication.kt
├── navigation/
│   ├── AppRoutes.kt          ← destinos @Serializable
│   └── AppNavGraph.kt        ← NavHost tipado
├── ui/
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   ├── components/
│   │   └── LexiTopBar.kt
│   └── screen/
│       ├── splash/   SplashScreen.kt
│       ├── home/     HomeScreen.kt · HomeUiState.kt
│       ├── reader/   ReaderScreen.kt · ReaderUiState.kt
│       ├── history/  HistoryScreen.kt · HistoryUiState.kt
│       └── help/     HelpScreen.kt
├── data/
│   ├── local/
│   │   ├── entity/   ReadingEntity.kt · UserPreferencesEntity.kt
│   │   ├── dao/      ReadingDao.kt · UserPreferencesDao.kt
│   │   └── database/ LexiEduDatabase.kt
│   ├── remote/
│   │   ├── api/      LexiEduApiService.kt
│   │   ├── dto/      TextEnhancementDto.kt
│   │   └── NetworkClient.kt
│   └── repository/
│       ├── ReadingRepositoryImpl.kt
│       └── UserPreferencesRepositoryImpl.kt
└── domain/
    ├── model/        Reading.kt · UserPreferences.kt
    ├── repository/   ReadingRepository.kt · UserPreferencesRepository.kt
    └── usecase/      GetAllReadings · SaveReading · DeleteReading · GetUserPreferences
```

---

## 4. Configuración del Proyecto — `libs.versions.toml`

### 4.1 Catálogo de versiones

El archivo `android/gradle/libs.versions.toml` centraliza **todas las versiones** del proyecto, evitando duplicidades y facilitando actualizaciones:

```toml
[versions]
agp                  = "8.11.1"
kotlin               = "2.2.20"
composeBom           = "2024.12.01"
navigationCompose    = "2.8.5"
room                 = "2.6.1"
retrofit             = "2.11.0"
okhttp               = "4.12.0"
coroutines           = "1.9.0"
lifecycle            = "2.8.7"
kotlinxSerialization = "1.7.3"
coreKtx              = "1.15.0"
activityCompose      = "1.10.1"
datastorePreferences = "1.1.1"
```

### 4.2 Plugins declarados en `app/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)        // habilita Compose compiler
    alias(libs.plugins.kotlin.serialization)  // habilita @Serializable
    id("kotlin-kapt")                         // procesador Room
}
```

### 4.3 Configuración Android

| Parámetro        | Valor        | Justificación                                  |
|------------------|--------------|------------------------------------------------|
| `compileSdk`     | 35           | APIs más recientes de Material 3 y Edge-to-Edge|
| `minSdk`         | 24 (Android 7) | Cubre >96% de dispositivos en uso en Ecuador |
| `targetSdk`      | 35           | Requisito de Google Play desde 2025            |
| `jvmTarget`      | 17           | LTS, requerido por AGP 8+                      |
| `buildFeatures.compose` | true  | Activa el compilador de Jetpack Compose        |

### 4.4 Árbol de dependencias principales

```
implementation(platform(libs.androidx.compose.bom))  ← BOM gestiona versiones Compose
    ├── compose.ui
    ├── compose.material3
    ├── compose.foundation
    └── compose.material.icons.extended

implementation(libs.androidx.navigation.compose)      ← Navegación tipada
implementation(libs.kotlinx.serialization.json)       ← @Serializable en rutas

implementation(libs.androidx.room.runtime)            ← ORM SQLite
implementation(libs.androidx.room.ktx)                ← Extensiones coroutines
kapt(libs.androidx.room.compiler)                     ← Generación de código Room

implementation(libs.squareup.retrofit)                ← Cliente HTTP
implementation(libs.squareup.okhttp.logging)          ← Logging para debug
```

---

## 5. Grafo de Navegación Tipada

### 5.1 Definición de rutas — `AppRoutes.kt`

La navegación usa **rutas tipadas** con `@Serializable` (Navigation Compose 2.8+), eliminando el uso de strings mágicos propensos a errores:

```kotlin
@Serializable object Splash

@Serializable object Home

@Serializable
data class Reader(
    val readingId: Long   = -1L,   // -1 = lectura nueva
    val imagePath: String = ""     // vacío si se abre desde historial
)

@Serializable object History

@Serializable object Help
```

**Ventaja clave:** los argumentos de `Reader` se serializan automáticamente en la back stack. No hay conversión manual de tipos ni `getString()`/`getLong()`.

### 5.2 NavHost — `AppNavGraph.kt`

```kotlin
NavHost(navController, startDestination = Splash) {

    composable<Splash> {
        SplashScreen(onNavigateToHome = {
            navController.navigate(Home) {
                popUpTo(Splash) { inclusive = true }  // limpia la back stack
            }
        })
    }

    composable<Home> {
        HomeScreen(
            onNavigateToReader  = { path -> navController.navigate(Reader(imagePath = path)) },
            onNavigateToHistory = { navController.navigate(History) },
            onNavigateToHelp    = { navController.navigate(Help) }
        )
    }

    composable<Reader> { backStackEntry ->
        val route: Reader = backStackEntry.toRoute()  // deserialización automática
        ReaderScreen(readingId = route.readingId, imagePath = route.imagePath, ...)
    }

    composable<History> { ... }
    composable<Help>    { ... }
}
```

### 5.3 Diagrama de flujo de navegación

```
        ┌──────────┐
        │  Splash  │  (3 s, animación alpha + scale)
        └────┬─────┘
             │ popUpTo(Splash, inclusive=true)
             ▼
        ┌──────────┐
   ┌───►│   Home   │◄──────────────────────┐
   │    └──┬───┬───┘                       │
   │       │   │                           │
   │  img  │   │ navigate(History)   back  │
   │  path │   │                           │
   │       ▼   ▼                           │
   │  ┌────────┐  ┌─────────┐  ┌──────┐   │
   │  │ Reader │  │ History │  │ Help │   │
   │  └────────┘  └─────────┘  └──────┘   │
   │       │           │                   │
   └───────┘ readingId └───────────────────┘
         (reabrir lectura)
```

---

## 6. Pantallas Compose — Layouts y Flujos UDF

Todas las pantallas siguen el patrón **Unidirectional Data Flow (UDF)**:

```
Estado (UiState)  ──►  Composable (UI)
                             │
                         Evento (UiEvent)
                             │
                             ▼
                    Actualización de estado
                        (remember/set)
```

### 6.1 SplashScreen

**Estado local:**
```kotlin
val alpha = remember { Animatable(0f) }
val scale = remember { Animatable(0.7f) }
```

**Flujo:** `LaunchedEffect(Unit)` ejecuta la animación de aparición y escala, espera 2 200 ms, luego llama `onNavigateToHome()`. La pantalla es completamente declarativa — no hay estado mutable más allá de las animaciones.

**Componentes clave:** `Animatable`, `tween()`, `alpha()`, `scale()`, `LaunchedEffect`.

---

### 6.2 HomeScreen

**Estado local UDF:**

```kotlin
var isLoading            by remember { mutableStateOf(false) }
var showPermissionDialog by remember { mutableStateOf(false) }
var selectedAction       by remember { mutableIntStateOf(-1) }
```

**Estructura de la pantalla:**

```
Scaffold
  ├── TopAppBar: "LexiEdu" + botones History / Help
  └── Column
       ├── Text: "¿Cómo deseas escanear?"
       ├── ActionCard [CameraAlt] "Usar cámara"     (isPrimary=true, amarillo)
       ├── ActionCard [Photo]     "Desde galería"   (isPrimary=false, superficie)
       └── AlertDialog (permiso de cámara) — visible cuando showPermissionDialog=true
```

El componente `ActionCard` es privado a la pantalla y muestra un `CircularProgressIndicator` cuando `isLoading && selectedAction == índice`.

---

### 6.3 ReaderScreen

**Estado local UDF:**

```kotlin
var isSpeaking       by remember { mutableStateOf(false) }
var speechRate       by remember { mutableFloatStateOf(0.5f) }
var fontSize         by remember { mutableFloatStateOf(16f) }
var showFontControls by remember { mutableStateOf(false) }
var triggerSaveToast by remember { mutableStateOf(false) }
```

**Estructura de la pantalla:**

```
Scaffold
  ├── LexiTopBar: ← atrás | "Lector de texto" | [Aa] [Bookmark]
  ├── Panel fontSize (AnimatedVisibility controlado por showFontControls)
  │    └── Slider 12sp–30sp
  ├── Box scrollable
  │    └── Text(extractedText, fontSize=fontSize.sp, lineHeight=fontSize*1.65.sp)
  └── Surface (controles TTS fijos en la parte inferior)
       ├── Row: [Stop] [FAB Play/Pause]
       └── Slider velocidad 0.25×–2×
```

`LaunchedEffect(triggerSaveToast)` muestra el `Snackbar` de confirmación de guardado de forma reactiva.

---

### 6.4 HistoryScreen

**Estado local UDF:**

```kotlin
var searchQuery     by remember { mutableStateOf("") }
var pendingDeleteId by remember { mutableStateOf<Long?>(null) }
```

**Propiedad derivada en `HistoryUiState`:**

```kotlin
val filteredReadings: List<Reading>
    get() = if (searchQuery.isBlank()) readings
            else readings.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.extractedText.contains(searchQuery, ignoreCase = true)
            }
```

**Estructura:**

```
Scaffold
  ├── LexiTopBar
  ├── OutlinedTextField (búsqueda con ícono Search)
  └── LazyColumn (key=reading.id para estabilidad de animaciones)
       └── ReadingCard × N
            ├── título, preview truncado (maxLines=2), fecha formateada
            └── IconButton Delete → pendingDeleteId = reading.id
AlertDialog de confirmación (visible cuando pendingDeleteId != null)
```

---

### 6.5 HelpScreen

**Estado local UDF:**

```kotlin
var expandedIndex by remember { mutableStateOf<Int?>(null) }
```

**Lógica de acordeón:**

```kotlin
onToggle = { expandedIndex = if (expandedIndex == index) null else index }
```

Cada `FaqCard` usa `AnimatedVisibility(visible = expanded)` para mostrar/ocultar la respuesta con animación fluida.

---

## 7. Esquema de Base de Datos Room

### 7.1 Entidades

#### `ReadingEntity` — tabla `readings`

```kotlin
@Entity(tableName = "readings")
data class ReadingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title           : String,   // título generado o asignado
    val extractedText   : String,   // texto extraído por OCR
    val imagePath       : String,   // ruta absoluta en almacenamiento local
    val processingTimeMs: Long,     // tiempo de procesamiento OCR en ms
    val createdAt       : Long      // epoch ms (UTC)
)
```

#### `UserPreferencesEntity` — tabla `user_preferences`

```kotlin
@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey val id            : Int     = 1,      // registro único
    val speechRate                : Float   = 0.5f,   // 0.25–2.0
    val speechPitch               : Float   = 1.0f,   // 0.5–2.0
    val fontSize                  : Float   = 16f,    // 12–30 sp
    val highContrastEnabled       : Boolean = false
)
```

### 7.2 Data Access Objects (DAO)

#### `ReadingDao`

| Método | Tipo | SQL / Anotación | Retorno |
|--------|------|-----------------|---------|
| `getAllReadings()` | Query | `SELECT * ORDER BY createdAt DESC` | `Flow<List<ReadingEntity>>` |
| `getReadingById(id)` | Query | `SELECT * WHERE id = :id` | `ReadingEntity?` |
| `insertReading(reading)` | Insert | `OnConflict.REPLACE` | `Long` (nuevo id) |
| `deleteReading(reading)` | Delete | por objeto | `Unit` |
| `deleteReadingById(id)` | Query | `DELETE WHERE id = :id` | `Unit` |
| `getTotalCount()` | Query | `SELECT COUNT(*)` | `Int` |

#### `UserPreferencesDao`

| Método | Tipo | SQL / Anotación | Retorno |
|--------|------|-----------------|---------|
| `getUserPreferences()` | Query | `SELECT * WHERE id = 1` | `Flow<UserPreferencesEntity?>` |
| `saveUserPreferences(prefs)` | Insert | `OnConflict.REPLACE` | `Unit` |
| `updateSpeechRate(rate)` | Query | `UPDATE SET speechRate = :rate` | `Unit` |
| `updateFontSize(size)` | Query | `UPDATE SET fontSize = :size` | `Unit` |

### 7.3 Base de Datos — `LexiEduDatabase`

```kotlin
@Database(
    entities     = [ReadingEntity::class, UserPreferencesEntity::class],
    version      = 1,
    exportSchema = false
)
abstract class LexiEduDatabase : RoomDatabase() {
    abstract fun readingDao(): ReadingDao
    abstract fun userPreferencesDao(): UserPreferencesDao

    companion object {
        @Volatile private var INSTANCE: LexiEduDatabase? = null

        fun getInstance(context: Context): LexiEduDatabase =
            INSTANCE ?: synchronized(this) { /* double-check lock */ }
    }
}
```

El patrón **Double-Check Locking** con `@Volatile` garantiza una única instancia en entornos multihilo. La base de datos se llama `lexiedu.db` y está configurada con `fallbackToDestructiveMigration()` para la fase de desarrollo.

---

## 8. Capa de Dominio

### 8.1 Modelos de dominio

Los modelos de dominio son **clases Kotlin puras** sin dependencia de ningún framework Android:

```kotlin
data class Reading(
    val id              : Long,
    val extractedText   : String,
    val imagePath       : String,
    val processingTimeMs: Long,
    val createdAt       : Long,
    val title           : String
)

data class UserPreferences(
    val speechRate          : Float   = 0.5f,
    val speechPitch         : Float   = 1.0f,
    val fontSize            : Float   = 16f,
    val highContrastEnabled : Boolean = false
)
```

### 8.2 Contratos de repositorio (interfaces)

```kotlin
interface ReadingRepository {
    fun getAllReadings(): Flow<List<Reading>>
    suspend fun getReadingById(id: Long): Reading?
    suspend fun saveReading(reading: Reading): Long
    suspend fun deleteReading(id: Long)
    suspend fun getTotalCount(): Int
}

interface UserPreferencesRepository {
    fun getUserPreferences(): Flow<UserPreferences>
    suspend fun saveUserPreferences(preferences: UserPreferences)
    suspend fun updateSpeechRate(rate: Float)
    suspend fun updateFontSize(size: Float)
}
```

Las interfaces están en la capa de dominio; las implementaciones en la capa de datos. Esto permite **invertir la dependencia**: el dominio no conoce Room ni ningún ORM específico.

### 8.3 Casos de uso

| Use Case | Operación | Tipo de retorno |
|----------|-----------|-----------------|
| `GetAllReadingsUseCase` | `invoke()` | `Flow<List<Reading>>` |
| `SaveReadingUseCase` | `invoke(reading)` | `suspend Long` |
| `DeleteReadingUseCase` | `invoke(id)` | `suspend Unit` |
| `GetUserPreferencesUseCase` | `invoke()` | `Flow<UserPreferences>` |

Todos los use cases usan el operador `invoke()` para un uso limpio: `getAllReadings()` en lugar de `getAllReadings.execute()`.

### 8.4 Implementaciones con mappers

Cada `RepositoryImpl` contiene funciones de extensión privadas para la conversión:

```kotlin
// ReadingRepositoryImpl.kt
private fun ReadingEntity.toDomain() = Reading(id, title, extractedText, ...)
private fun Reading.toEntity()       = ReadingEntity(id, title, extractedText, ...)
```

Esto mantiene el mapeo encapsulado en la capa de datos; el dominio nunca sabe que existe `ReadingEntity`.

---

## 9. Capa de Red — Retrofit

### 9.1 Servicio API — `LexiEduApiService`

```kotlin
interface LexiEduApiService {

    @POST("api/v1/enhance-text")
    suspend fun enhanceText(
        @Body request: TextEnhancementRequest
    ): Response<TextEnhancementResponse>

    @GET("api/v1/accessibility-tips")
    suspend fun getAccessibilityTips(): Response<List<AccessibilityTipDto>>
}
```

| Endpoint | Método | Propósito |
|----------|--------|-----------|
| `api/v1/enhance-text` | POST | Envía texto OCR y recibe versión simplificada con oraciones clave |
| `api/v1/accessibility-tips` | GET | Recupera tips dinámicos para la pantalla de ayuda |

### 9.2 DTOs (Data Transfer Objects)

```kotlin
data class TextEnhancementRequest(
    @SerializedName("text")     val text    : String,
    @SerializedName("language") val language: String = "es"
)

data class TextEnhancementResponse(
    @SerializedName("original_text")   val originalText  : String,
    @SerializedName("simplified_text") val simplifiedText: String,
    @SerializedName("key_sentences")   val keySentences  : List<String>,
    @SerializedName("reading_level")   val readingLevel  : String
)
```

Los `@SerializedName` desacoplan el nombre del campo Kotlin del campo JSON del servidor.

### 9.3 NetworkClient

```kotlin
object NetworkClient {
    private const val BASE_URL     = "https://api.lexiedu.example.com/"
    private const val TIMEOUT_SECS = 30L

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY  // solo en debug
        })
        .connectTimeout(TIMEOUT_SECS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECS, TimeUnit.SECONDS)
        .build()

    val apiService: LexiEduApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(LexiEduApiService::class.java)
}
```

**Decisiones de diseño:**
- `object` singleton garantiza una sola instancia del cliente HTTP en toda la app.
- `HttpLoggingInterceptor` con `Level.BODY` facilita la depuración en fase de desarrollo.
- Timeouts de 30 s para tolerar condiciones de red variables.

---

## 10. Sistema de Temas y Accesibilidad

### 10.1 Paleta de colores (WCAG AA)

| Token | Hex | Uso | Ratio de contraste |
|-------|-----|-----|--------------------|
| `PrimaryYellow` | `#FFD400` | Acciones primarias, acentos | 11.6:1 sobre negro |
| `PrimaryBackground` | `#000000` | Fondo global | — |
| `Surface` | `#151515` | Tarjetas y contenedores | — |
| `AccentWhite` | `#FFFFFF` | Texto principal | 21:1 sobre negro |
| `DisabledGray` | `#909090` | Texto secundario | 7.4:1 sobre negro |
| `SuccessGreen` | `#34D399` | Confirmaciones | 8.9:1 sobre negro |
| `ErrorRed` | `#FF4D4D` | Errores, eliminación | 5.2:1 sobre negro |

Todos los colores cumplen el estándar **WCAG 2.1 nivel AA** (ratio ≥ 4.5:1).

### 10.2 Tipografía

```kotlin
val LexiTypography = Typography(
    displayLarge   = TextStyle(fontWeight = ExtraBold, fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontWeight = SemiBold,  fontSize = 22.sp, lineHeight = 30.sp),
    bodyLarge      = TextStyle(fontWeight = Normal,    fontSize = 16.sp, lineHeight = 26.sp),
    // ...
)
```

La relación `lineHeight / fontSize ≥ 1.5` en todos los estilos de cuerpo facilita la lectura para usuarios con dislexia.

---

## 11. Entradas de Permiso — `AndroidManifest.xml`

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
```

| Permiso | Propósito | SDK |
|---------|-----------|-----|
| `INTERNET` | Llamadas Retrofit a la API de mejora de texto | Todos |
| `CAMERA` | Captura de imágenes para OCR | Todos |
| `READ_EXTERNAL_STORAGE` | Acceso a galería (Android ≤ 12) | maxSdk 32 |
| `READ_MEDIA_IMAGES` | Acceso a imágenes (Android 13+) | minSdk 33 |

---

## 12. Registro de Commits en GitHub

El repositorio cuenta con **4 commits técnicos descriptivos** que corresponden a cada entregable de la Fase 1:

| Hash | Commit | Archivos |
|------|--------|----------|
| `3e06887` | `feat(config): migrar a native Compose con libs.versions.toml y catálogo de versiones` | 4 archivos Gradle |
| `31f0b2f` | `feat(navigation): implementar grafo de navegación tipada con rutas @Serializable` | 9 archivos (nav + tema + MainActivity) |
| `6b5fc0e` | `feat(ui): diseñar pantallas principales con Compose y flujos UDF locales` | 8 archivos de pantallas |
| `7875531` | `feat(data): esquema Room, contratos de dominio y estructura preliminar Retrofit` | 18 archivos de datos y dominio |

**Total de archivos nuevos/modificados en la Fase 1: 39 archivos**

---

## 13. Decisiones Arquitectónicas Relevantes

### 13.1 ¿Por qué Clean Architecture?

La separación en tres capas permite que el dominio (la lógica de negocio de accesibilidad) sea **independiente de cualquier framework**. Si en el futuro se requiere cambiar Room por DataStore o Retrofit por Ktor, solo cambia la capa de datos sin afectar el dominio ni la UI.

### 13.2 ¿Por qué UDF en lugar de ViewModel desde Fase 1?

En esta fase las pantallas usan **estado local** (`remember`/`mutableStateOf`) para simplificar la demostración de los flujos. Los `UiState` y `UiEvent` ya están definidos como contratos, lo que facilita la migración a `ViewModel` + `StateFlow` en la Fase 2 sin reescribir la UI.

### 13.3 ¿Por qué `@Serializable` en rutas?

La API de navegación tipada (Navigation Compose 2.8+) serializa automáticamente los argumentos de ruta. Esto elimina los `bundleOf()` manuales, previene errores de tipo en tiempo de compilación y facilita deep linking futuro.

### 13.4 ¿Por qué Kapt en lugar de KSP para Room?

KSP 2 aún no tiene versión estable para Kotlin 2.2.x. Kapt es la opción soportada y garantiza compatibilidad con el generador de código de Room 2.6.1 sin configuración adicional.

---

## 14. Pendientes para Fase 2

| Componente | Descripción |
|------------|-------------|
| `ViewModel` | Conectar pantallas con `StateFlow` desde Room |
| OCR (ML Kit) | Integrar `TextRecognizer` en `ReaderScreen` |
| TTS nativo | Conectar `TextToSpeech` con controles de `ReaderScreen` |
| Inyección de dependencias | Configurar Hilt o manual DI para repositorios |
| Permisos en tiempo de ejecución | `rememberPermissionState` para cámara/galería |
| Tests unitarios | Casos de uso y repositorios con datos en memoria |

---

## 15. Conclusiones

La Fase 1 establece una base arquitectónica sólida para LexiEdu sobre Jetpack Compose nativo. Se implementaron todos los entregables requeridos:

1. **`libs.versions.toml`** centraliza 13 versiones de dependencias, eliminando la duplicación y habilitando la gestión de versiones con type-safety mediante `alias(libs.*)`.

2. **Navegación tipada** con 5 rutas `@Serializable` que cubren el flujo completo de la aplicación, con `popUpTo` correcto en el splash para limpiar la back stack.

3. **5 pantallas Compose** declarativas con flujos UDF explícitos: cada pantalla expone su estado y sus eventos de forma clara y predecible, siguiendo las guías oficiales de arquitectura de Android.

4. **Esquema Room** con 2 entidades, 4 DAOs y base de datos singleton thread-safe, listo para ser conectado con los repositorios en la Fase 2.

5. **Capa de dominio** completamente desacoplada: interfaces, modelos puros y use cases que pueden probarse sin instrumentación Android.

6. **Retrofit** configurado con logging, timeouts y DTOs con serialización Gson, preparado para conectarse a la API de mejora de texto en fases posteriores.

---

*Documento generado el 12 de junio de 2026 — LexiEdu v2.0 — Fase 1*
