# Spec: Modernización de "Volcanes de Costa Rica" a Kotlin + Jetpack Compose

> Estado: **BORRADOR — Fase 1 (Specify), pendiente de aprobación.**
> Fecha: 2026-05-31 · Autor: Fabián Granados (con Claude Code)

## Objetivo

Llevar la app Android *Volcanes de Costa Rica* desde su stack legacy (Java + Support
Library 26.1.0 + Universal Image Loader, AGP 7.4.2 / SDK 33) a una base **moderna y
mantenible**: **Kotlin + Jetpack Compose + Material 3**, con el toolchain Android,
Gradle y Kotlin al día.

**Usuario:** público general en Costa Rica interesado en monitoreo visual de volcanes
activos. La función central es **ver imágenes de cámaras en vivo** de OVSICORI-UNA, con
auto-refresco, y poder **compartir** la imagen actual.

**Éxito:** la app modernizada conserva la función actual (ver y compartir cámaras en vivo)
pero sobre un stack actual, con UI declarativa Material 3, sin secretos en el repo, con el
catálogo **oficial actualizado** (8 cámaras de ovsicori.una.ac.cr) y una arquitectura
**data-driven** (lista de cámaras) que elimine los switch paralelos por posición.

### Decisiones tomadas (ver "Preguntas" del flujo)
- **Stack:** Kotlin + Jetpack Compose + Material 3 (nativo, solo Android).
- **Plataforma:** solo Android (no iOS, no Flutter, no KMP).
- **minSdk:** 26 (Android 8.0).
- **Git:** rama `modernizacion-compose` + PR al final.
- **CI:** GitHub Actions (build + test + lint) en cada push/PR.
- **Versión:** `versionName "3.0"` / `versionCode 18` (hoy 2.12 / 17).
- **Timeline de noticias (ex-Twitter):** PREGUNTA ABIERTA — se decide en fase posterior.

## Tech Stack (objetivo)

| Componente | Hoy | Objetivo |
|---|---|---|
| Lenguaje | Java 8/11 | **Kotlin 2.x (K2)** |
| UI | XML Views + Support Library 26.1.0 | **Jetpack Compose + Material 3** |
| AGP / Gradle | 7.4.2 / 7.5 | **AGP 8.x / Gradle 8.x** (versión exacta se fija en Fase 2) |
| compile/targetSdk | 33 | **36 (Android 16)** — requerido por activity/navigation 1.13/2.9 |
| minSdk | 16 | **26 (Android 8.0)** |
| Carga de imágenes | Universal Image Loader 1.9.3 | **Coil 3 (`coil-compose`)** |
| Navegación | 2 Activities + Fragment drawer | **Navigation Compose** (single-Activity) |
| Estado | campos en Activity | **ViewModel + StateFlow / Compose state** |
| Preferencias | SharedPreferences | **DataStore (Preferences)** |
| FAB / menú | CircularFloatingActionMenu, design lib | **Material 3 FAB / componentes Compose** |
| circleimageview | de.hdodenhof | Compose (`Modifier.clip(CircleShape)`) |

> Las versiones exactas (BOM de Compose, Kotlin, AGP, Coil) se confirman contra la
> documentación oficial y se **fijan en la Fase 2 (Plan)**, no se inventan aquí.

## Comandos

Java sigue activándose vía SDKMAN para Gradle (el toolchain de Kotlin corre sobre la JVM):
```bash
source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk use java 17.0.x-tem   # AGP 8 requiere JDK 17
./gradlew assembleDebug        # build debug
./gradlew assembleRelease      # build release
./gradlew clean
./gradlew test                 # unit tests (JVM)
./gradlew connectedAndroidTest # tests instrumentados / Compose UI (requiere emulador)
./gradlew lint                 # Android Lint
```
`local.properties` está en .gitignore; si falta:
`echo "sdk.dir=$HOME/Library/Android/sdk" > local.properties`

> Nota: AGP 8 exige **JDK 17** como mínimo. El CLAUDE.md actual referencia Java 11; se
> actualizará al cerrar esta migración.

## Estructura de proyecto (objetivo)

```
app/src/main/java/krausoft/volcanesdecostarica/
  MainActivity.kt              → única Activity, host de Compose + Navigation
  VolcanoApp.kt                → @Composable raíz: NavHost + tema
  ui/
    theme/                     → Color.kt, Type.kt, Theme.kt (Material 3)
    home/HomeScreen.kt         → lista/drawer de cámaras (reemplaza NavigationDrawerFragment)
    camera/CameraScreen.kt     → visor en vivo (reemplaza PhotoLiveViewer)
    camera/CameraViewModel.kt  → estado: url actual, refresco, último éxito
  data/
    Camera.kt                  → data class (id, título, feedSlug, refreshMs, info, shareMsg)
    CameraRepository.kt        → catálogo de las 6 cámaras + armado de URL
  share/ImageShareHelper.kt    → guarda/obtiene imagen y dispara Intent (FileProvider)
app/src/test/java/...          → unit tests (JVM): repo, armado de URL, intervalos
app/src/androidTest/java/...   → tests Compose (navegación, render, compartir)
.github/workflows/android.yml  → CI: build + test + lint en push/PR
docs/spec-modernizacion.md     → este documento
```

## Modelo de datos (el cambio arquitectónico central)

Hoy la cámara se selecciona por un **entero de posición** y hay `switch(option)` duplicado
en `onCreate`, `startTimer` y `sharePicture`. Se reemplaza por una **lista declarativa**:

```kotlin
data class Camera(
    val id: String,            // estable, = feedSlug, p.ej. "liveturrialba"
    val title: String,         // título en el menú (string resource)
    val feedSlug: String,      // "liveturrialba", "liveirazu", "livecraterpoas", ...
    val refreshMs: Long,       // por ahora 5_000 para todas (campo configurable)
    val infoText: String,      // descripción + cadencia
    val shareMessage: String,  // texto al compartir, p.ej. "Volcán Turrialba"
)

// URL = "$URL_MAIN$feedSlug/camara.jpg?t=$unixMillis"
```

**Catálogo oficial (8 cámaras), confirmado contra `ovsicori.una.ac.cr` el 2026-05-31.**
Todas refrescan cada **5 s** según el sitio oficial.

| # | feedSlug | título | refresco | mensaje compartir | página oficial |
|---|----------|--------|----------|-------------------|----------------|
| 0 | `liveturrialba` | Turrialba | 5 s | Volcán Turrialba | camara-v-turrialba |
| 1 | `liveirazu` | Irazú | 5 s | Volcán Irazú | camara-2-v-turrialba |
| 2 | `livecraterpoas` | Póas (Cráter) | 5 s | Volcán Póas (Cráter) | camara-crater-v-poas |
| 3 | `livepoas` | Póas (SO del Cráter) | 5 s | Volcán Póas (SO del Cráter) | camara-v-poas-so-del-crater |
| 4 | `livechahuites` | Póas (Chahuites) | 5 s | Volcán Póas (Chahuites) | camara-v-poas-chahuites |
| 5 | `liverincon` | Rincón de la Vieja (Sensoria) | 5 s | Volcán Rincón de la Vieja - Sensoria | rincon-de-la-vieja-sensoria2 |
| 6 | `livecurubande` | Rincón de la Vieja (Curubandé) | 5 s | Volcán Rincón de la Vieja - Curubandé | camara-v-rincon-de-la-vieja-curubande |
| 7 | `liverincon2` | Rincón de la Vieja (Gavilán/Upala) | 5 s | Volcán Rincón de la Vieja - Gavilán | rincon-de-la-vieja-gavilan |

> Cambios vs. legacy: el viejo `irazu` titulado "Turrialba Cam 2" era en realidad **Irazú**
> (corregido); `livepoas` es el "Póas SO del cráter" (antes `poas`); se **agregan**
> `livechahuites` y `livecurubande`. Los intervalos legacy (10s/60s/5min) quedan obsoletos:
> el sitio declara 5 s para todas. Los `infoText` se toman/actualizan de cada página oficial.
> Agregar una cámara nueva = agregar una entrada a esta lista (un solo lugar, no 3 switch).

## URLs y red

Se migra al endpoint **oficial y público** de OVSICORI (HTTPS), tomado del sitio
`https://www.ovsicori.una.ac.cr/index.php/camaras`:

- Patrón: `{url_main}{feedSlug}{url_end}{unix_ms}`
  - `url_main` = `https://www.ovsicori.una.ac.cr/images/stories/camaras/live`
  - `url_end` = `/camara.jpg?t=`
  - timestamp en **milisegundos** (`System.currentTimeMillis()`), p.ej.
    `…/liveturrialba/camara.jpg?t=1780255835209`
- **HTTPS:** ya no hace falta `network_security_config` para cleartext (se elimina del plan).
- **Cache-buster `?t={ms}`:** la imagen vive siempre en `…/live{slug}/camara.jpg` y el
  servidor la sobreescribe; el valor de `?t=` no selecciona imagen histórica (el servidor
  lo ignora), solo debe **cambiar en cada request** para saltar cachés intermedios
  (Coil/OkHttp/proxy/CDN). **Decisión:** se mantiene el `?t={ms}` en cada auto-refresco.
- **Catálogo confirmado (2026-05-31):** los 8 `feedSlug` y sus refrescos (5 s) fueron
  verificados abriendo cada página oficial e inspeccionando la URL real de la imagen. Ver
  la tabla en "Modelo de datos". Re-verificar ante cualquier cambio del sitio.

## Compartir imagen

- Tomar la última imagen cargada con éxito (de la caché de Coil) y compartirla vía
  **FileProvider** (autoridad: `krausoft.volcanesdecostarica.fileprovider`).
- Mensaje: `"{shareMessage} [{dd-MM-yyyy, HH:mm:ss}]"`.

## Estilo de código

```kotlin
@Composable
fun CameraScreen(
    state: CameraUiState,
    onRefresh: () -> Unit,
    onShare: () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(state.title) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onShare) {
                Icon(Icons.Default.Share, contentDescription = stringResource(R.string.compartir))
            }
        },
    ) { padding ->
        PullToRefreshBox(isRefreshing = state.isRefreshing, onRefresh = onRefresh) {
            AsyncImage(model = state.imageUrl, contentDescription = state.title, ...)
        }
    }
}
```
- Kotlin idiomático: `val` por defecto, null-safety, sin `!!` salvo justificado.
- Strings siempre vía `stringResource` / recursos (nada hardcodeado en UI).
- Una responsabilidad por archivo; ViewModel sin referencias a `View`/`Context` de UI.
- Sin Java nuevo. Sin AndroidX legacy (`android.support.*` desaparece por completo).

## Estrategia de testing

Hoy **no hay tests**. Se introduce una base mínima:
- **Unit (JVM, `src/test`)** con JUnit: `CameraRepository` (catálogo correcto, armado de
  URL con timestamp, mapeo de intervalos). Cobertura objetivo: lógica de datos ~80%.
- **Instrumentado (`src/androidTest`)** con Compose UI Test: render de `HomeScreen`,
  navegación a `CameraScreen`, visibilidad del FAB de compartir.
- No se persigue cobertura alta en UI; sí en la capa de datos (que es donde estaba el
  riesgo de los switch duplicados).

## Boundaries

**Siempre:**
- Correr `./gradlew test` y `./gradlew lint` antes de cada commit.
- Preservar paridad funcional exacta de las 6 cámaras (slug, intervalo, info, mensaje).
- Mantener la autoridad del FileProvider y el formato del mensaje de compartir.
- Strings en recursos. Solo tráfico HTTPS (endpoint oficial ovsicori.una.ac.cr).
- Confirmar slug + intervalo de cada cámara contra su página oficial antes de fijarlos.

**Preguntar primero:**
- Cambiar el catálogo de cámaras (agregar/quitar/renombrar).
- Subir minSdk por encima de 26 o cambiar targetSdk.
- Agregar dependencias no listadas en el Tech Stack.
- Cambiar `applicationId`, `versionCode`/`versionName`, o la firma de release.
- Resolver la pregunta abierta del timeline de noticias.

**Nunca:**
- Reintroducir `android.support.*`, Universal Image Loader o el SDK de Twitter.
- Commitear secretos (las claves de Twitter se eliminan en esta migración).
- Tráfico cleartext / `usesCleartextTraffic` (todo va por HTTPS).
- Introducir Kotlin Multiplatform/Flutter (quedó descartado).

## Criterios de éxito (testeables)

1. `./gradlew assembleDebug` y `assembleRelease` compilan sin Java ni Support Library.
2. `grep -r "android.support" app/src` → **0 resultados**. `grep -r "universalimageloader"` → 0.
3. Las claves `com.twitter.sdk.android.*` ya no existen en el repo (`git grep CONSUMER_KEY` → 0).
4. Las 8 cámaras oficiales cargan imagen y auto-refrescan cada 5 s.
5. Pull-to-refresh fuerza una recarga inmediata.
6. Compartir produce un Intent con la imagen y el texto `"{mensaje} [fecha]"`.
7. `./gradlew test` pasa (incluye tests de `CameraRepository`).
8. App corre en un dispositivo/emulador con Android 8.0 (API 26) y Android 16 (API 36).
9. Agregar una cámara nueva requiere editar **un solo** lugar (el catálogo), no 3 switch.
10. El workflow de GitHub Actions corre verde (build + test + lint) en el PR.
11. `versionName` = "3.0" y `versionCode` = 18 en el build final.

## Preguntas abiertas

1. **Branding/ícono:** ¿hay assets nuevos de ícono/splash, o se reusa el actual modernizado?

### Resueltas
- **Versión:** 3.0 / versionCode 18. ✅
- **Rama/PR:** rama `modernizacion-compose` + PR. ✅
- **CI:** sí, GitHub Actions (build + test + lint). ✅
- **Noticias/timeline (ex-Twitter):** **fuera del milestone 3.0.** El HomeScreen queda
  diseñado para poder sumarla después, pero no se implementa ahora. ✅
```
