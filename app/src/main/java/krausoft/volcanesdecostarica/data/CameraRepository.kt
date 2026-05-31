package krausoft.volcanesdecostarica.data

import krausoft.volcanesdecostarica.R

/**
 * Fuente de verdad del catálogo de cámaras y de la construcción de sus URLs.
 *
 * El catálogo (8 cámaras) está confirmado contra el sitio oficial de OVSICORI.
 * Para agregar/quitar una cámara se edita ÚNICAMENTE la lista [cameras].
 */
object CameraRepository {

    /** Refresco común que el sitio oficial declara para todas las cámaras. */
    private const val DEFAULT_REFRESH_MS = 5_000L

    /** Base de la URL de imagen y ruta + parámetro anti-caché (?t=). */
    private const val URL_BASE = "https://www.ovsicori.una.ac.cr/images/stories/camaras/"
    private const val IMAGE_PATH = "/camara.jpg?t="

    /** Catálogo oficial de cámaras, en el orden en que se muestran al usuario. */
    val cameras: List<Camera> = listOf(
        camera("liveturrialba", R.string.cam_turrialba_title, R.string.cam_turrialba_info, R.string.cam_turrialba_share),
        camera("liveirazu", R.string.cam_irazu_title, R.string.cam_irazu_info, R.string.cam_irazu_share),
        camera("livecraterpoas", R.string.cam_craterpoas_title, R.string.cam_craterpoas_info, R.string.cam_craterpoas_share),
        camera("livepoas", R.string.cam_poas_title, R.string.cam_poas_info, R.string.cam_poas_share),
        camera("livechahuites", R.string.cam_chahuites_title, R.string.cam_chahuites_info, R.string.cam_chahuites_share),
        camera("liverincon", R.string.cam_rincon_title, R.string.cam_rincon_info, R.string.cam_rincon_share),
        camera("livecurubande", R.string.cam_curubande_title, R.string.cam_curubande_info, R.string.cam_curubande_share),
        camera("liverincon2", R.string.cam_rincon2_title, R.string.cam_rincon2_info, R.string.cam_rincon2_share),
    )

    /** Devuelve la cámara con ese [id], o null si no existe en el catálogo. */
    fun findById(id: String): Camera? = cameras.firstOrNull { it.id == id }

    /**
     * Construye la URL de la imagen agregando [millis] como cache-buster (?t=).
     * El servidor ignora el valor; sólo importa que cambie entre llamadas para
     * evitar que un caché intermedio devuelva una imagen vieja.
     */
    fun imageUrl(camera: Camera, millis: Long): String =
        "$URL_BASE${camera.feedSlug}$IMAGE_PATH$millis"

    /** Atajo para crear una entrada del catálogo; el id coincide con el slug. */
    private fun camera(slug: String, titleRes: Int, infoRes: Int, shareRes: Int): Camera =
        Camera(
            id = slug,
            feedSlug = slug,
            refreshMs = DEFAULT_REFRESH_MS,
            titleRes = titleRes,
            infoRes = infoRes,
            shareRes = shareRes,
        )
}
