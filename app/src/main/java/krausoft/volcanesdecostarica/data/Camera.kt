package krausoft.volcanesdecostarica.data

import androidx.annotation.StringRes

/**
 * Describe una cámara en vivo del catálogo de OVSICORI.
 *
 * Los textos se guardan como referencias a recursos (@StringRes) para mantener
 * la internacionalización; el slug y el refresco son los datos estables.
 *
 * @property id identificador estable (coincide con [feedSlug]); clave de navegación.
 * @property feedSlug segmento de la URL de la imagen (p. ej. "liveturrialba").
 * @property refreshMs cada cuánto refrescar la imagen, en milisegundos.
 * @property titleRes título mostrado en la lista y la barra superior.
 * @property infoRes descripción de la ubicación y la cadencia.
 * @property shareRes texto base al compartir (la fecha se agrega aparte).
 */
data class Camera(
    val id: String,
    val feedSlug: String,
    val refreshMs: Long,
    @StringRes val titleRes: Int,
    @StringRes val infoRes: Int,
    @StringRes val shareRes: Int,
    /** Nombre del volcán al que pertenece esta cámara; se usa para agrupar en la lista. */
    @StringRes val volcanoGroupRes: Int,
)
