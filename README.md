# Volcanes de Costa Rica

App Android para ver en tiempo real las cámaras en vivo de los volcanes de Costa Rica,
monitoreados por el [OVSICORI-UNA](https://www.ovsicori.una.ac.cr/).

## Cámaras disponibles

Catálogo oficial confirmado contra el sitio de OVSICORI (2026-05-31).
Todas las cámaras refrescan automáticamente cada **5 segundos**.

| Volcán | Cámara |
|--------|--------|
| Turrialba | Cráter |
| Irazú | Cima |
| Póas | Cráter |
| Póas | SO del Cráter (1.8 km) |
| Póas | Chahuites |
| Rincón de la Vieja | Sensoria (~4 km N del cráter) |
| Rincón de la Vieja | Curubandé (10 km NE de Liberia) |
| Rincón de la Vieja | Gavilán (Upala) |

## Características

- Imágenes en vivo con refresco automático cada 5 s
- Pull-to-refresh para forzar una recarga manual
- Compartir la imagen actual con fecha y hora
- Aviso cuando una cámara no está emitiendo señal
- Pantalla encendida mientras se visualiza una cámara
- Soporte de tema claro/oscuro con Material 3 y color dinámico (Android 12+)

## Requisitos

- Android 8.0 (API 26) o superior

## Stack

- **Kotlin 2.x** + **Jetpack Compose** + **Material 3**
- **Coil 3** para carga de imágenes de red
- **Navigation Compose** (single-Activity)
- **AGP 8.13.2** / **Gradle 8.13** / **JDK 17**

## Compilar

Requiere JDK 17 activado vía SDKMAN:

```bash
source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk use java 17.0.11-tem

./gradlew assembleDebug         # APK de debug
./gradlew assembleRelease       # APK de release
./gradlew testDebugUnitTest     # tests unitarios
./gradlew lintDebug             # lint
./gradlew clean
```

Si falta `local.properties`:
```bash
echo "sdk.dir=$HOME/Library/Android/sdk" > local.properties
```

## Agregar una cámara nueva

Editar **un solo lugar** — `CameraRepository.cameras` en
`app/src/main/java/krausoft/volcanesdecostarica/data/CameraRepository.kt`:

```kotlin
camera("livenuevacam", R.string.cam_nueva_title, R.string.cam_nueva_info, R.string.cam_nueva_share),
```

Y agregar las strings correspondientes en `res/values/strings_cameras.xml`.

## Fuente de datos

Las imágenes provienen del sistema de cámaras del
[OVSICORI-UNA](https://www.ovsicori.una.ac.cr/) (Observatorio Vulcanológico y
Sismológico de Costa Rica, Universidad Nacional).

## Contacto

krausoft@gmail.com
