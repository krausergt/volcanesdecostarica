# Plan de implementación (Fase 2) — Modernización a Kotlin + Compose

> Acompaña a [`spec-modernizacion.md`](./spec-modernizacion.md). Estado: **BORRADOR,
> pendiente de aprobación.** No se rompe en tareas finas (Fase 3) hasta aprobar este plan.

## Estrategia general

Reescritura completa en una rama `modernizacion-compose`, construida **de adentro hacia
afuera**: primero el toolchain compila vacío, luego la capa de datos (con tests), luego
tema/navegación, luego pantallas, luego compartir, y al final limpieza + CI. Cada fase
tiene un **checkpoint verificable** antes de seguir.

## Versiones objetivo (a confirmar contra documentación oficial en la 1ª tarea)

| Pieza | Objetivo aprox. | Notas |
|---|---|---|
| JDK | 17 | requerido por AGP 8 |
| Gradle | 8.x (wrapper) | |
| AGP | 8.x | |
| Kotlin | 2.x (K2) | usa plugin `org.jetbrains.kotlin.plugin.compose` |
| Compose | vía **BOM** | no fijar versiones de artefactos a mano |
| Coil | 3.x (`coil-compose`) | carga de imágenes |
| compileSdk/targetSdk | 35 | |
| minSdk | 26 | |

> Las versiones exactas se resuelven en la Tarea 1 (vía context7/docs oficiales) y se
> centralizan en un **version catalog** (`gradle/libs.versions.toml`).

## Componentes y dependencias

```
toolchain/scaffolding  ─┬─→ data layer (Camera + Repository + tests)
                        └─→ tema Material 3
                              │
              data + tema ────┴─→ navegación (single-Activity + NavHost)
                                    └─→ HomeScreen ─→ CameraScreen + ViewModel
                                                        └─→ ImageShareHelper (FAB)
                                                              └─→ limpieza + CI
```
- **Paralelizable** tras el scaffolding: *data layer* y *tema* son independientes.
- **Secuencial:** navegación → pantallas → compartir → limpieza.

## Fases y checkpoints

### Fase A — Toolchain y scaffolding (secuencial, fundacional)
- Crear rama `modernizacion-compose`.
- Subir wrapper de Gradle a 8.x; AGP 8.x; activar JDK 17.
- `gradle/libs.versions.toml` con todas las versiones; migrar build a Kotlin + Compose.
- `app/build.gradle(.kts)`: plugins kotlin-android + compose; `minSdk 26`, `target/compile 35`,
  `versionName "3.0"`, `versionCode 18`; `buildFeatures { compose = true }`.
- Dependencias nuevas: activity-compose, compose BOM (ui, material3, tooling), navigation-compose,
  lifecycle-viewmodel-compose, coil-compose, datastore-preferences.
- **Quitar:** appcompat/design/recyclerview support 26.1.0, universal-image-loader,
  circleimageview, CircularFloatingActionMenu.
- **Checkpoint A:** `./gradlew assembleDebug` compila una app vacía/placeholder en Compose.

### Fase B — Capa de datos (TDD)
- `data/Camera.kt` (data class) y `data/CameraRepository.kt` con las **8 cámaras** del spec.
- Builder de URL: `"$URL_MAIN$feedSlug/camara.jpg?t=$millis"`.
- **Tests unitarios primero** (`src/test`): catálogo de 8, slugs correctos, formato de URL,
  que el `?t=` cambia entre llamadas.
- **Checkpoint B:** `./gradlew test` verde.

### Fase C — Tema y navegación
- `ui/theme/` (Color, Type, Theme) Material 3 con dynamic color (API 31+) y fallback.
- `MainActivity.kt` single-Activity (`setContent`), `VolcanoApp.kt` con `NavHost`:
  ruta `home` y ruta `camera/{cameraId}`.
- **Checkpoint C:** la app abre en HomeScreen vacío y navega a un CameraScreen placeholder.

### Fase D — Pantallas
- `HomeScreen`: lista (LazyColumn) de las 8 cámaras; click → navega con `cameraId`.
  (Diseñado para poder sumar luego una sección de "Noticias" — ver pregunta abierta.)
- `CameraViewModel`: estado (`imageUrl`, `isRefreshing`, `title`, `info`); auto-refresco
  cada `refreshMs` mediante coroutine **lifecycle-aware** (se pausa fuera de foreground).
- `CameraScreen`: `Scaffold` + `TopAppBar` + `PullToRefreshBox` + `AsyncImage` (Coil) +
  texto info + link a fuente (OVSICORI). FAB de compartir.
- Mantener `FLAG_KEEP_SCREEN_ON` mientras se ve una cámara.
- **Checkpoint D:** en emulador, las 8 cámaras muestran imagen real y refrescan a 5 s;
  pull-to-refresh fuerza recarga.

### Fase E — Compartir
- `share/ImageShareHelper.kt`: obtener el bitmap/última imagen de la caché de Coil,
  escribirla en `cacheDir` y compartir vía **FileProvider** (autoridad
  `krausoft.volcanesdecostarica.fileprovider`, `file_paths.xml`).
- Mensaje: `"{shareMessage} [{dd-MM-yyyy, HH:mm:ss}]"`.
- **Checkpoint E:** compartir abre el chooser con imagen + texto correctos.

### Fase F — Manifest, recursos y limpieza
- `AndroidManifest.xml`: una sola Activity, permiso INTERNET, FileProvider; quitar
  `PhotoLiveViewer`/`MainActivity` viejas y referencias Twitter.
- `strings.xml`: **eliminar claves de Twitter**; agregar títulos/info/mensajes de las 8
  cámaras; quitar strings obsoletos (timers legacy, feeds ovsprivado).
- Borrar Java muerto: `PhotoLiveViewer.java`, `NavigationDrawerFragment.java`,
  `MenuAdapter.java`, `Information.java`, `Tools/BasicImageDownloader.java`,
  `Tools/HashCodeFileNameWithDummyExtGenerator.java`, layouts XML viejos.
- **Checkpoint F:** `grep -r "android.support\|universalimageloader\|CONSUMER_KEY" app/src`
  → 0 resultados; build sigue verde.

### Fase G — CI y verificación final
- `.github/workflows/android.yml`: setup JDK 17 + `./gradlew assembleDebug test lint`.
- Correr matriz manual: emulador API 26 y API 35.
- Validar los 11 criterios de éxito del spec.
- **Checkpoint G:** CI verde + criterios cumplidos → PR.

## Riesgos y mitigaciones

| Riesgo | Mitigación |
|---|---|
| Desalineación de versiones AGP/Kotlin/Compose | Usar Compose BOM + plugin compose de Kotlin 2; confirmar versiones con docs oficiales (context7) en Tarea 1. |
| Compartir desde la caché de Coil (nombre/ubicación del archivo no garantizados) | No depender del archivo interno de Coil: re-encodear el bitmap actual a un `.jpg` en `cacheDir` y compartir ese. |
| Polling de 5 s consumiendo batería/datos en background | Coroutine atada al `Lifecycle` (solo refresca en STARTED/RESUMED); cancelar al salir. |
| Servidor OVSICORI caído/lento o imagen vacía | Estados de carga/error en el ViewModel; mostrar último frame + placeholder de error (Coil `error`). |
| Pregunta abierta de "Noticias" | HomeScreen diseñado extensible; no bloquea el resto. Resolver antes de Fase 3. |
| Caché HTTP intermedio sirviendo imagen vieja | Mantener `?t={ms}`; además `networkCachePolicy`/headers en Coil si hace falta. |

## Trabajo paralelizable
Tras Fase A: **B (datos)** y **C-tema** pueden avanzar en paralelo. El resto es secuencial.
