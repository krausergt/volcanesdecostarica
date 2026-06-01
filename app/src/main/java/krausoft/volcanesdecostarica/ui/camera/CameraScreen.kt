package krausoft.volcanesdecostarica.ui.camera

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.toBitmap
import kotlinx.coroutines.delay
import krausoft.volcanesdecostarica.R
import krausoft.volcanesdecostarica.data.CameraRepository
import krausoft.volcanesdecostarica.share.shareImage

// Relación de aspecto de las cámaras de OVSICORI (todas entregan imágenes 4:3).
private const val CAMERA_ASPECT_RATIO = 4f / 3f

/**
 * Visor de una cámara en vivo: la imagen (4:3) se muestra arriba y se auto-refresca
 * cada [krausoft.volcanesdecostarica.data.Camera.refreshMs]; debajo van la
 * descripción y la fuente. Permite refrescar con pull-to-refresh y compartir
 * la imagen actual con el FAB.
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
    val context = LocalContext.current

    // Painter que expone el estado de carga para poder extraer el bitmap al compartir.
    val painter = rememberAsyncImagePainter(
        model = uiState.imageUrl,
        onSuccess = { viewModel.onImageSuccess() },
        onError = { viewModel.onImageError() },
    )
    val painterState by painter.state.collectAsStateWithLifecycle()

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

    val shareMessage = stringResource(camera.shareRes)
    val noImageMsg = stringResource(R.string.share_no_image)
    val shareLabel = stringResource(R.string.share_label)

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
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Extrae el bitmap del painter state y lanza el Intent de compartir.
                    val bitmap = (painterState as? AsyncImagePainter.State.Success)
                        ?.result?.image?.toBitmap()
                    if (bitmap != null) {
                        context.startActivity(
                            android.content.Intent.createChooser(
                                shareImage(context, bitmap, shareMessage),
                                shareLabel,
                            )
                        )
                    } else {
                        Toast.makeText(context, noImageMsg, Toast.LENGTH_SHORT).show()
                    }
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = stringResource(R.string.cd_share),
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Imagen de la cámara en la parte superior (4:3), con pull-to-refresh.
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.manualRefresh() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(CAMERA_ASPECT_RATIO)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.foundation.Image(
                        painter = painter,
                        contentDescription = stringResource(camera.titleRes),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                    )
                    // Aviso cuando la cámara no entrega imagen (respuesta vacía/sin señal).
                    if (uiState.hasError) {
                        Text(
                            text = stringResource(R.string.image_unavailable),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            }

            // Descripción de la cámara y enlace a la fuente oficial, justo debajo.
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
