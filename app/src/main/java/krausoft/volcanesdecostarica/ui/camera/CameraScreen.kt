package krausoft.volcanesdecostarica.ui.camera

import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.toBitmap
import kotlinx.coroutines.delay
import krausoft.volcanesdecostarica.R
import krausoft.volcanesdecostarica.data.CameraRepository
import krausoft.volcanesdecostarica.share.shareImage

// Relación de aspecto de las cámaras de OVSICORI (todas entregan imágenes 4:3).
private const val CAMERA_ASPECT_RATIO = 4f / 3f

/**
 * Visor de una cámara en vivo: imagen 4:3 con auto-refresco cada
 * [krausoft.volcanesdecostarica.data.Camera.refreshMs], crossfade entre fotogramas
 * e indicador de tiempo desde la última actualización.
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

    // Painter con crossfade de 300 ms para suavizar la transición entre fotogramas.
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(uiState.imageUrl)
            .crossfade(300)
            .build(),
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

    // Contador de segundos desde la última imagen exitosa; se reinicia con cada carga.
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    LaunchedEffect(uiState.lastRefreshedMs) {
        elapsedSeconds = 0
        if (uiState.lastRefreshedMs > 0L) {
            while (true) {
                delay(1_000L)
                elapsedSeconds++
            }
        }
    }

    val timestampText = when {
        uiState.lastRefreshedMs == 0L -> stringResource(R.string.live_indicator)
        elapsedSeconds == 0 -> stringResource(R.string.updated_just_now)
        else -> stringResource(R.string.updated_ago_seconds, elapsedSeconds)
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

            // Indicador "En vivo" con tiempo desde la última actualización.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LiveDot()
                Spacer(Modifier.width(6.dp))
                Text(
                    text = timestampText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Descripción de la cámara y enlace a la fuente oficial.
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
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

/** Punto verde pulsante que indica que la cámara está transmitiendo en vivo. */
@Composable
private fun LiveDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "livePulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "liveDotAlpha",
    )
    Box(
        modifier = Modifier
            .size(8.dp)
            .background(color = Color(0xFF4CAF50).copy(alpha = alpha), shape = CircleShape),
    )
}
