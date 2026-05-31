package krausoft.volcanesdecostarica.ui.camera

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import krausoft.volcanesdecostarica.R
import krausoft.volcanesdecostarica.data.CameraRepository
import kotlinx.coroutines.delay

/**
 * Visor de una cámara en vivo: muestra la imagen (que se auto-refresca cada
 * [krausoft.volcanesdecostarica.data.Camera.refreshMs]), permite refrescar con
 * pull-to-refresh y muestra la descripción y la fuente.
 *
 * @param cameraId id recibido por la ruta de navegación.
 * @param onBack acción para volver a la pantalla anterior.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(cameraId: String, onBack: () -> Unit) {
    val camera = CameraRepository.findById(cameraId)
    if (camera == null) {
        // Defensa por si llega un id inválido: nada que mostrar, volver.
        LaunchedEffect(Unit) { onBack() }
        return
    }

    val viewModel: CameraViewModel = viewModel(
        key = cameraId,
        factory = CameraViewModel.factory(camera),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Mantiene la pantalla encendida mientras se ve la cámara.
    val view = LocalView.current
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    // Auto-refresco cada refreshMs, sólo mientras la pantalla está activa (RESUMED).
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (true) {
                viewModel.refresh()
                delay(camera.refreshMs)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(camera.titleRes)) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                AsyncImage(
                    model = uiState.imageUrl,
                    contentDescription = stringResource(camera.titleRes),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                    onLoading = { viewModel.setRefreshing(true) },
                    onSuccess = { viewModel.setRefreshing(false) },
                    onError = { viewModel.setRefreshing(false) },
                )
            }

            // Descripción de la cámara y enlace a la fuente oficial.
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(camera.infoRes),
                    style = MaterialTheme.typography.bodyMedium,
                )
                val uriHandler = LocalUriHandler.current
                val sourceUrl = stringResource(R.string.source_url)
                Text(
                    text = stringResource(R.string.source_ovsicori),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clickable { uriHandler.openUri(sourceUrl) },
                )
            }
        }
    }
}
