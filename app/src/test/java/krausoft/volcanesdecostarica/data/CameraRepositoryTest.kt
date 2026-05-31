package krausoft.volcanesdecostarica.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests de la capa de datos: verifican el catálogo oficial de 8 cámaras y la
 * construcción de la URL de imagen con el cache-buster (?t=).
 */
class CameraRepositoryTest {

    /** El catálogo debe tener las 8 cámaras oficiales, con sus slugs en el orden definido. */
    @Test
    fun catalogo_tiene_las_8_camaras_oficiales_en_orden() {
        val slugs = CameraRepository.cameras.map { it.feedSlug }
        assertEquals(
            listOf(
                "liveturrialba",
                "liveirazu",
                "livecraterpoas",
                "livepoas",
                "livechahuites",
                "liverincon",
                "livecurubande",
                "liverincon2",
            ),
            slugs,
        )
    }

    /** El sitio oficial declara 5 s de refresco para todas las cámaras. */
    @Test
    fun todas_refrescan_cada_5_segundos() {
        assertTrue(CameraRepository.cameras.all { it.refreshMs == 5_000L })
    }

    /** Cada cámara debe tener un id único (se usa como clave de navegación). */
    @Test
    fun cada_camara_tiene_id_unico() {
        val ids = CameraRepository.cameras.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    /** La URL combina base oficial + slug + camara.jpg?t={millis}. */
    @Test
    fun url_de_imagen_usa_base_oficial_slug_y_cache_buster() {
        val turrialba = CameraRepository.cameras.first()
        val url = CameraRepository.imageUrl(turrialba, 1_780_255_835_209)
        assertEquals(
            "https://www.ovsicori.una.ac.cr/images/stories/camaras/" +
                "liveturrialba/camara.jpg?t=1780255835209",
            url,
        )
    }

    /** El cache-buster debe cambiar entre llamadas para saltar cachés intermedios. */
    @Test
    fun cache_buster_cambia_entre_llamadas() {
        val cam = CameraRepository.cameras.first()
        assertNotEquals(
            CameraRepository.imageUrl(cam, 1L),
            CameraRepository.imageUrl(cam, 2L),
        )
    }

    /** findById devuelve la cámara correspondiente, o null si el id no existe. */
    @Test
    fun findById_devuelve_la_camara_o_null() {
        assertEquals("liveirazu", CameraRepository.findById("liveirazu")?.feedSlug)
        assertNull(CameraRepository.findById("noexiste"))
    }
}
