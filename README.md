# Volcanes de Costa Rica

App Android para ver en tiempo real las cámaras en vivo de los volcanes de Costa Rica, junto con el timeline de Twitter/X del [OVSICORI-UNA](http://www.ovsicori.una.ac.cr/).

## Cámaras disponibles

| Volcán | Cámara | Intervalo de refresco |
|--------|--------|-----------------------|
| Turrialba | Cráter (mirando al oeste) | 10 segundos |
| Turrialba | Cam 2 — vista Irazú | 60 segundos |
| Poás | Cam principal (1.8 km al SW del cráter) | 60 segundos |
| Poás | Cráter | 60 segundos |
| Rincón de la Vieja | Sensoria | 60 segundos |
| Rincón de la Vieja | Upala | 5 minutos |

## Características

- Imágenes en vivo con refresco automático
- Swipe para refrescar manualmente
- Compartir la imagen actual con fecha y hora
- Timeline de Twitter/X del OVSICORI-UNA en la pantalla principal
- Pantalla encendida mientras se visualiza una cámara

## Requisitos

- Android 4.1 (API 16) o superior

## Compilar

```bash
./gradlew assembleDebug    # APK de debug
./gradlew assembleRelease  # APK de release
./gradlew clean            # limpiar build
```

## Fuente de datos

Las imágenes provienen del sistema de cámaras del [OVSICORI-UNA](http://www.ovsicori.una.ac.cr/) (Observatorio Vulcanológico y Sismológico de Costa Rica).

## Contacto

krausoft@gmail.com
