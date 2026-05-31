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
 * @property isRefreshing si hay una carga de imagen en curso.
 */
data class CameraUiState(
    val imageUrl: String = "",
    val isRefreshing: Boolean = false,
)

/**
 * ViewModel del visor de una cámara: mantiene la URL actual y la regenera con un
 * nuevo cache-buster en cada refresco. No conoce la UI ni resuelve strings.
 */
class CameraViewModel(val camera: Camera) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState(imageUrl = freshUrl()))
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    /** Regenera la URL con un nuevo ?t= para forzar la recarga de la imagen. */
    fun refresh() {
        _uiState.value = _uiState.value.copy(imageUrl = freshUrl())
    }

    /** Indica si hay una carga en curso (lo reportan los callbacks de Coil). */
    fun setRefreshing(refreshing: Boolean) {
        _uiState.value = _uiState.value.copy(isRefreshing = refreshing)
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
