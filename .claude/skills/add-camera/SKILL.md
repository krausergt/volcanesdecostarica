---
name: add-camera
description: Agrega una nueva cámara de volcán a la app. Solo requiere editar dos archivos: CameraRepository.kt y strings_cameras.xml.
disable-model-invocation: true
---

Vas a agregar una nueva cámara al catálogo de la app Volcanes de Costa Rica.
La arquitectura es data-driven: **solo se editan dos archivos**.

## Paso 1: Recopilar información

Pedile al usuario:

1. **feedSlug** — segmento de URL de la cámara, tal como aparece en el sitio de OVSICORI
   (ej. `livearenal`). Verificar en `https://www.ovsicori.una.ac.cr/index.php/camaras`
   que la URL de la imagen sigue el patrón:
   `https://www.ovsicori.una.ac.cr/images/stories/camaras/{feedSlug}/camara.jpg`
2. **Título corto** — nombre que aparece en la lista y en la barra superior (ej. `Arenal`)
3. **Volcán al que pertenece** — para la agrupación en el listado. Opciones actuales:
   `volcano_turrialba`, `volcano_irazu`, `volcano_poas`, `volcano_rincon_vieja`.
   Si es un volcán nuevo, se crea un string nuevo.
4. **Texto de info** — descripción de la ubicación en español (ej. "Cámara en el cráter del
   volcán Arenal. La imagen se refresca cada 5 segundos.")
5. **Mensaje al compartir** — texto base sin fecha (ej. `Volcán Arenal`)

Si el usuario ejecutó `/add-camera <nombre>` con un argumento, pre-completa lo que puedas
y pedí solo lo que falta.

## Paso 2: Agregar strings en strings_cameras.xml

Archivo: `app/src/main/res/values/strings_cameras.xml`

Agregar dentro de `<resources>`, bajo el comentario del volcán correspondiente
(o crear un bloque nuevo si es un volcán nuevo):

```xml
<!-- Arenal -->
<string name="cam_arenal_title">Arenal</string>
<string name="cam_arenal_info">TEXTO_INFO</string>
<string name="cam_arenal_share">Volcán Arenal</string>
```

Si es un volcán nuevo, agregar también el string de grupo:
```xml
<string name="volcano_arenal">Arenal</string>
```

Usar un prefijo `cam_{slug_sin_live}_` como convención (ej. `cam_arenal_` para `livearenal`).

## Paso 3: Agregar la cámara en CameraRepository.kt

Archivo: `app/src/main/java/krausoft/volcanesdecostarica/data/CameraRepository.kt`

Agregar una línea en la lista `cameras`, en el orden geográfico/lógico que corresponda:

```kotlin
camera("livearenal", R.string.cam_arenal_title, R.string.cam_arenal_info, R.string.cam_arenal_share, R.string.volcano_arenal),
```

Si el volcán ya existe en el catálogo, usar su string de grupo (ej. `R.string.volcano_poas`).

**Eso es todo.** No hay más archivos que editar.

## Paso 4: Verificar

```bash
source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk use java 17.0.11-tem
./gradlew assembleDebug    # debe compilar sin errores
./gradlew test             # los 6 tests de CameraRepositoryTest deben seguir verdes
```

Recordarle al usuario que pruebe en dispositivo o emulador que:
- La nueva cámara aparece en el listado bajo su volcán
- El thumbnail carga correctamente
- El auto-refresco funciona (cada 5 s)
- Compartir incluye el mensaje correcto con fecha

## Referencia rápida

```
URL de imagen: https://www.ovsicori.una.ac.cr/images/stories/camaras/{feedSlug}/camara.jpg?t={ms}
Refresco:      5 000 ms para todas las cámaras (DEFAULT_REFRESH_MS en CameraRepository)
Volcanes actuales: Turrialba (1 cam), Irazú (1), Póas (3), Rincón de la Vieja (3)
```
