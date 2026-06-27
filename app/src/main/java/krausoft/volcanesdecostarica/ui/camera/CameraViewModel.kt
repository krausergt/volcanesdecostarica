package krausoft.volcanesdecostarica.ui.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import krausoft.volcanesdecostarica.data.Camera
import krausoft.volcanesdecostarica.data.CameraRepository

/**
 * Estado de la pantalla del visor.
 *
 * @property imageUrl URL actual de la imagen (con cache-buster ?t=).
 * @property isRefreshing si se está mostrando el indicador de pull-to-refresh
 *   (sólo en refrescos manuales, no en los automáticos).
 * @property hasError si la última carga de imagen falló (p. ej. cámara sin señal).
 */
data class CameraUiState(
    val imageUrl: String = "",
    val isRefreshing: Boolean = false,
    val hasError: Boolean = false,
    /** Marca de tiempo (ms) de la última imagen cargada con éxito; 0 = aún no cargó. */
    val lastRefreshedMs: Long = 0L,
)

/**
 * ViewModel del visor de una cámara: mantiene la URL actual y la regenera con un
 * nuevo cache-buster en cada refresco. No conoce la UI ni resuelve strings.
 */
class CameraViewModel(val camera: Camera) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState(imageUrl = freshUrl()))
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    /** Refresco automático (timer): cambia la URL sin mostrar el spinner. */
    fun refresh() {
        _uiState.value = _uiState.value.copy(imageUrl = freshUrl())
    }

    /** Refresco manual (pull-to-refresh): muestra el spinner y recarga. */
    fun manualRefresh() {
        _uiState.value = _uiState.value.copy(imageUrl = freshUrl(), isRefreshing = true)
    }

    /** La imagen cargó bien: oculta el spinner, limpia el error y registra el momento. */
    fun onImageSuccess() {
        _uiState.value = _uiState.value.copy(
            isRefreshing = false,
            hasError = false,
            lastRefreshedMs = System.currentTimeMillis(),
        )
    }

    /** La imagen falló (sin señal/vacía): oculta el spinner y marca el error. */
    fun onImageError() {
        _uiState.value = _uiState.value.copy(isRefreshing = false, hasError = true)
    }

    // URL de imagen con la marca de tiempo actual como cache-buster.
    private fun freshUrl(): String =
        CameraRepository.imageUrl(camera, System.currentTimeMillis())

    companion object {
        /** Factory para crear el ViewModel con la cámara ya resuelta. */
        fun factory(camera: Camera) = viewModelFactory {
            initializer { CameraViewModel(camera) }
        }
    }
}
