# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Android app that displays live webcam images from Costa Rican volcanoes (OVSICORI-UNA cameras).
Written entirely in **Kotlin + Jetpack Compose** (no Java, no XML layouts, no Support Library).

## Stack

- **Language:** Kotlin 2.3.21 (K2 compiler)
- **UI:** Jetpack Compose + Material 3 (single-Activity, `VolcanoApp` NavHost)
- **AGP:** 8.13.2 / **Gradle:** 8.13 / **JDK:** 17 (via SDKMAN, `17.0.11-tem`)
- **compile/targetSdk:** 36 / **minSdk:** 26 (Android 8.0)
- **Image loading:** Coil 3 (`coil-compose` + `coil-network-okhttp`)
- **Navigation:** Navigation Compose 2.9.8
- **Lifecycle/ViewModel:** lifecycle-viewmodel-compose 2.10.0
- **Versions:** centralized in `gradle/libs.versions.toml` (version catalog)

## Build Commands

JDK 17 must be activated via SDKMAN before running Gradle:
```bash
source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk use java 17.0.11-tem
./gradlew assembleDebug         # debug build
./gradlew assembleRelease       # release build
./gradlew testDebugUnitTest     # unit tests (JVM)
./gradlew lintDebug             # Android Lint
./gradlew clean
```

`local.properties` is gitignored — if missing, create it:
`echo "sdk.dir=$HOME/Library/Android/sdk" > local.properties`

## Architecture

Single-Activity app (Compose + Navigation):

```
MainActivity
  └── VolcanoApp (NavHost)
        ├── HomeScreen        — lista de 8 cámaras (CameraRepository)
        └── CameraScreen      — visor en vivo (CameraViewModel + Coil AsyncImage)
              └── ImageShareHelper — comparte via FileProvider
```

**Data layer:**
- `data/Camera.kt` — data class (id, feedSlug, refreshMs, @StringRes)
- `data/CameraRepository.kt` — catálogo de 8 cámaras + `imageUrl()` + `findById()`

## Adding a New Camera

Edit **a single place** — `CameraRepository.cameras`:
```kotlin
camera("livenuevacam", R.string.cam_nueva_title, R.string.cam_nueva_info, R.string.cam_nueva_share),
```
Then add the corresponding strings to `res/values/strings_cameras.xml`. That's it.

## Camera URL Pattern

All camera images follow this pattern (confirmed against official OVSICORI site, 2026-05-31):
```
https://www.ovsicori.una.ac.cr/images/stories/camaras/{feedSlug}/camara.jpg?t={unix_millis}
```
- **`feedSlug`** examples: `liveturrialba`, `liveirazu`, `livecraterpoas`, `livepoas`,
  `livechahuites`, `liverincon`, `livecurubande`, `liverincon2`
- **`?t=`** is a cache-buster; the server ignores the value — only needs to change each request.
- All cameras refresh every **5 s** (per the official site declaration).

## Image Sharing

`ImageShareHelper.shareImage()` re-encodes the current bitmap to JPEG in `cacheDir` and
shares via `FileProvider` (authority: `krausoft.volcanesdecostarica.fileprovider`).
Share message format: `"{shareMessage} [{dd-MM-yyyy, HH:mm:ss}]"`.

The bitmap is extracted from `AsyncImagePainter.State.Success.result.image.toBitmap()`.

## Tests

Unit tests live in `app/src/test/`:
- `data/CameraRepositoryTest.kt` — 6 tests: catalog of 8, slugs, refresh rate, URL format,
  cache-buster changes, findById.

No instrumented tests yet (`src/androidTest/` is empty).

## CI

GitHub Actions (`.github/workflows/android.yml`): `assembleDebug` + `testDebugUnitTest` +
`lintDebug` with JDK 17 Temurin, triggered on push/PR to `master`.

## Code Conventions

- **Comments:** concise comments in **Spanish** on every method/function and non-obvious
  blocks, explaining *what* it does and *why* when not obvious — for someone new to the code.
  KDoc (`/** */`) on public APIs; single-line on private/internal. No redundant comments.
- Kotlin idiomatic: `val` by default, null-safety, no `!!` unless justified.
- All user-visible strings in resources (never hardcoded in UI).
- No Java. No `android.support.*`. No XML layouts. No Universal Image Loader.
