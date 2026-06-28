# Tareas (Fase 3) — Modernización a Kotlin + Compose

> Deriva de [`plan-modernizacion.md`](./plan-modernizacion.md) y
> [`spec-modernizacion.md`](./spec-modernizacion.md). Cada tarea es atómica, con criterio
> de aceptación y verificación. Orden = dependencias. Noticias quedan **fuera de 3.0**.

Convención de verificación de build:
```bash
source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk use java 17.0.x-tem
```

---

## Fase A — Toolchain y scaffolding

- [ ] **T0 · Crear rama de trabajo**
  - Acceptance: existe la rama `modernizacion-compose` partiendo de `master`.
  - Verify: `git branch --show-current` → `modernizacion-compose`.
  - Files: — (solo git).

- [ ] **T1 · Fijar versiones + version catalog + wrapper Gradle 8**
  - Acceptance: versiones exactas de AGP 8.x, Gradle 8.x, Kotlin 2.x, Compose BOM, Coil 3
    confirmadas contra docs oficiales (context7) y centralizadas en
    `gradle/libs.versions.toml`. Wrapper apuntando a Gradle 8.x.
  - Verify: `./gradlew --version` muestra Gradle 8.x; `libs.versions.toml` existe y parsea.
  - Files: `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`.

- [ ] **T2 · Migrar archivos de build a Kotlin + Compose**
  - Acceptance: `build.gradle` raíz y `app/build.gradle` usan plugins kotlin-android +
    kotlin-compose; `minSdk 26`, `compile/targetSdk 35`, `versionName "3.0"`,
    `versionCode 18`, `buildFeatures { compose = true }`. Dependencias nuevas (activity-compose,
    compose BOM, material3, navigation-compose, lifecycle-viewmodel-compose, coil-compose,
    datastore-preferences) vía catalog. **Eliminadas:** appcompat/design/recyclerview 26.1.0,
    universal-image-loader, circleimageview, CircularFloatingActionMenu.
  - Verify: `grep -E "support|universalimageloader|twitter|circleimageview|CircularFloating" app/build.gradle` → 0.
  - Files: `build.gradle`, `app/build.gradle`, `settings.gradle`, `gradle.properties`.

- [ ] **T3 · Eliminar fuentes legacy (deja de compilar lo viejo)**
  - Acceptance: borrados los `.java` y layouts/menús XML que dependen de support lib.
  - Verify: `git rm` aplicado; `grep -r "android.support" app/src/main/java` → 0.
  - Files (deleciones): `PhotoLiveViewer.java`, `NavigationDrawerFragment.java`,
    `MenuAdapter.java`, `Information.java`, `Tools/BasicImageDownloader.java`,
    `Tools/HashCodeFileNameWithDummyExtGenerator.java`, layouts/menús XML legacy.
    (Tarea de borrado; excede 5 archivos por ser eliminaciones de bajo riesgo.)

- [ ] **T4 · Entry point Compose mínimo (Checkpoint A)**
  - Acceptance: `MainActivity.kt` (single-Activity, `setContent`) muestra un placeholder
    Compose con un tema Material 3 stub; `AndroidManifest` declara solo esa Activity +
    permiso INTERNET.
  - Verify: `./gradlew assembleDebug` **compila** y la app abre mostrando el placeholder.
  - Files: `MainActivity.kt`, `ui/theme/Theme.kt` (stub), `AndroidManifest.xml`.

---

## Fase B — Capa de datos (TDD)

- [ ] **T5 · `Camera` + `CameraRepository` + tests (Checkpoint B)**
  - Acceptance: `data class Camera` y `CameraRepository` con las **8 cámaras** del spec
    (slugs, títulos, refresh 5 s, mensajes). Builder `imageUrl(camera, millis)` =
    `https://www.ovsicori.una.ac.cr/images/stories/camaras/live{slug}/camara.jpg?t={millis}`.
    Tests unitarios escritos **antes** de la implementación.
  - Verify: `./gradlew test` verde; test confirma 8 cámaras, slugs exactos, formato de URL
    y que `?t=` cambia entre dos llamadas con millis distintos.
  - Files: `data/Camera.kt`, `data/CameraRepository.kt`, `test/.../CameraRepositoryTest.kt`.

---

## Fase C — Tema y navegación (paralelizable con B)

- [ ] **T6 · Tema Material 3 completo**
  - Acceptance: `Color.kt`, `Type.kt`, `Theme.kt` con esquema claro/oscuro y dynamic color
    en API 31+ (fallback en 26–30).
  - Verify: `./gradlew assembleDebug` compila; preview del tema renderiza.
  - Files: `ui/theme/Color.kt`, `ui/theme/Type.kt`, `ui/theme/Theme.kt`.

- [ ] **T7 · Navegación single-Activity (Checkpoint C)**
  - Acceptance: `VolcanoApp.kt` con `NavHost`: rutas `home` y `camera/{cameraId}`;
    `MainActivity` invoca `VolcanoApp`. Pantallas placeholder por ahora.
  - Verify: la app abre en Home placeholder y navega a Camera placeholder con un `cameraId`.
  - Files: `VolcanoApp.kt`, `MainActivity.kt`, `ui/home/HomeScreen.kt` (stub),
    `ui/camera/CameraScreen.kt` (stub).

---

## Fase D — Pantallas

- [ ] **T8 · HomeScreen (lista de 8 cámaras)**
  - Acceptance: `LazyColumn` lista las 8 cámaras desde el repo; click navega a
    `camera/{id}`. Strings de títulos en `strings.xml`. Estructura deja lugar para una
    futura sección "Noticias" (fuera de alcance ahora).
  - Verify: en emulador, se ven 8 ítems y el tap navega a la cámara correcta.
  - Files: `ui/home/HomeScreen.kt`, `res/values/strings.xml`.

- [ ] **T9 · CameraViewModel (estado + auto-refresh lifecycle-aware)**
  - Acceptance: expone `CameraUiState` (imageUrl, isRefreshing, title, info); refresca cada
    `refreshMs` con coroutine atada al `Lifecycle` (solo en STARTED/RESUMED); `refresh()`
    fuerza recarga inmediata regenerando `?t=`.
  - Verify: test unitario del cálculo de URL/estado donde sea aislable; en runtime, log/observa
    que el refresco se pausa al ir a background.
  - Files: `ui/camera/CameraViewModel.kt`, (opcional) `test/.../CameraViewModelTest.kt`.

- [ ] **T10 · CameraScreen (Checkpoint D)**
  - Acceptance: `Scaffold` + `TopAppBar(title)` + `PullToRefreshBox` + `AsyncImage` (Coil)
    + texto info + link a fuente OVSICORI + FAB compartir. `FLAG_KEEP_SCREEN_ON` mientras
    se ve la cámara. Estados de carga/error (placeholder + error de Coil).
  - Verify: las **8 cámaras** muestran imagen real y refrescan a 5 s; pull-to-refresh recarga.
  - Files: `ui/camera/CameraScreen.kt`, `res/values/strings.xml`.

---

## Fase E — Compartir

- [ ] **T11 · ImageShareHelper + FileProvider (Checkpoint E)**
  - Acceptance: toma la imagen actual, la re-encodea a un `.jpg` en `cacheDir` (no depende
    del archivo interno de Coil) y la comparte vía `FileProvider`
    (`krausoft.volcanesdecostarica.fileprovider`). Texto: `"{shareMessage} [dd-MM-yyyy, HH:mm:ss]"`.
  - Verify: el FAB abre el chooser con imagen adjunta + texto correcto; en API 26 y 35.
  - Files: `share/ImageShareHelper.kt`, `res/xml/file_paths.xml`, `AndroidManifest.xml`,
    `ui/camera/CameraScreen.kt`.

---

## Fase F — Limpieza final

- [ ] **T12 · Recursos y manifest finales (Checkpoint F)**
  - Acceptance: `strings.xml` sin claves de Twitter ni strings obsoletos (timers legacy,
    feeds ovsprivado); con info/mensajes de las 8 cámaras. Manifest e ícono finales.
  - Verify: `git grep -E "CONSUMER_KEY|CONSUMER_SECRET|android.support|universalimageloader|ovsprivado"` → 0.
  - Files: `res/values/strings.xml`, `AndroidManifest.xml`, `res/mipmap*` (si aplica ícono).

---

## Fase G — CI y cierre

- [ ] **T13 · GitHub Actions**
  - Acceptance: workflow corre `assembleDebug`, `test`, `lint` en push/PR con JDK 17.
  - Verify: el workflow aparece verde en el primer push de la rama.
  - Files: `.github/workflows/android.yml`.

- [ ] **T14 · Verificación final + CLAUDE.md**
  - Acceptance: matriz manual en emulador API 26 y API 35; los 11 criterios de éxito del
    spec se cumplen. `CLAUDE.md` actualizado al stack nuevo (Kotlin/Compose, JDK 17, 8 cámaras).
  - Verify: checklist de criterios marcada; `assembleRelease` compila.
  - Files: `CLAUDE.md`.

- [ ] **T15 · Pull Request**
  - Acceptance: PR de `modernizacion-compose` → `master` enlazando spec/plan/tareas.
  - Verify: PR abierto con CI verde.
  - Files: — (git/gh).
