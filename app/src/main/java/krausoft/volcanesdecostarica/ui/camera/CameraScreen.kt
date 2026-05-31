package krausoft.volcanesdecostarica.ui.camera

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import krausoft.volcanesdecostarica.R
import krausoft.volcanesdecostarica.data.CameraRepository

/**
 * Visor de una cámara. Placeholder del Checkpoint C: muestra el título real de
 * la cámara y permite volver. La imagen en vivo y el auto-refresco llegan en T9/T10.
 *
 * @param cameraId id recibido por la ruta de navegación.
 * @param onBack acción para volver a la pantalla anterior.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(cameraId: String, onBack: () -> Unit) {
    val camera = CameraRepository.findById(cameraId)
    val title = camera?.let { stringResource(it.titleRes) } ?: cameraId
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            // Placeholder temporal; se reemplaza por el visor con Coil en T10.
            Text("Visor en construcción ($cameraId)")
        }
    }
}
