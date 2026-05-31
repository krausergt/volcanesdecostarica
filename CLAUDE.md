# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Android app that displays live webcam images from Costa Rican volcanoes and a Twitter/X timeline from OVSICORI-UNA. Written entirely in **Java** (no Kotlin). Do not introduce Kotlin unless explicitly asked.

## Stack (Legacy — do not upgrade unless asked)

- **AGP:** 3.1.0 — incompatible with modern Android Studio/Gradle 8.x. Do not upgrade without explicit instruction.
- **Gradle:** 4.4
- **compileSdkVersion / targetSdkVersion:** 26 (Android 8.0)
- **Support Library:** 26.1.0 — uses `android.support.*`, NOT AndroidX. Do not migrate to AndroidX unless asked.
- **Image loading:** Universal Image Loader 1.9.3 (not Glide/Coil)
- **Twitter:** Twitter SDK 3.2.0
- **Crash reporting:** Fabric (not modern Firebase Crashlytics)

## Build Commands

```bash
./gradlew assembleDebug    # debug build
./gradlew assembleRelease  # release build
./gradlew clean            # clean
```

There are no automated tests — no `test/` or `androidTest/` directories exist.

## Architecture

Two activities only:
- `MainActivity` — navigation drawer + Twitter timeline (OVSICORI_UNA)
- `PhotoLiveViewer` — live camera viewer with auto-refresh

**Camera selection is entirely position-based.** The position integer is passed from `NavigationDrawerFragment` to `PhotoLiveViewer` via `Intent.putExtra(OPTION_SELECTED, position)`. Every switch statement in `PhotoLiveViewer` uses this integer.

## Adding a New Camera

Use `/add-camera` — it walks through all the places that need updating. The steps are:
1. Add string resources in `app/src/main/res/values/strings.xml`
2. Add the title to the `titles` array in `NavigationDrawerFragment.getData()`
3. Add a `case` to the URL switch in `PhotoLiveViewer.onCreate()`
4. Add a `case` to the timer switch in `PhotoLiveViewer.startTimer()`
5. Add a `case` to the sharing switch in `PhotoLiveViewer.sharePicture()`

## Camera URL Pattern

All camera images follow this pattern:
```
{url_main}{feed_slug}{url_end}{unix_timestamp_seconds}
```
- `url_main` = `http://www.ovsprivado.una.ac.cr/images/stories/live`
- `url_end` = `/camara.jpg?`
- `feed_slug` = camera-specific string (e.g., `turrialba`, `poas`, `rincon`)

The timestamp at the end busts the HTTP cache on each refresh.

## Timer Intervals

Three intervals defined in `strings.xml`:
- `Timer10seg` = 10000 ms (10 s) — used only for Turrialba cam 1
- `Timer60seg` = 60000 ms (1 min) — default for most cameras
- `Timer5min` = 300000 ms (5 min) — used for slow/remote cameras

## Image Sharing

Sharing grabs the last successfully loaded image from Universal Image Loader's disk cache via `imageLoader.getDiskCache().get(url_success)` and shares it via FileProvider (authority: `krausoft.volcanesdecostarica.fileprovider`). The share message format is: `"{Mensaje string} [{dd-MM-yyyy, HH:mm:ss}]"`.

## Sensitive Data in Source

`strings.xml` contains Twitter API keys (`com.twitter.sdk.android.CONSUMER_KEY` / `CONSUMER_SECRET`) and a Fabric API key in `AndroidManifest.xml`. Do not log or expose these values.
