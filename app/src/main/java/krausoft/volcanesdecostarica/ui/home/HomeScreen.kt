package krausoft.volcanesdecostarica.ui.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import krausoft.volcanesdecostarica.R
import krausoft.volcanesdecostarica.data.Camera
import krausoft.volcanesdecostarica.data.CameraRepository

/** Pestañas de la barra de navegación inferior; la expansión futura agrega aquí. */
private enum class NavTab(val labelRes: Int, val icon: ImageVector) {
    CAMERAS(R.string.tab_cameras, Icons.Filled.CameraAlt),
    NEWS(R.string.tab_news, Icons.Filled.Article),
    MAP(R.string.tab_map, Icons.Filled.Map),
}

/**
 * Pantalla principal: dashboard de monitoreo agrupado por volcán, con thumbnails
 * en vivo y barra de navegación inferior para extensión futura (Noticias, Mapa).
 *
 * @param onCameraClick se invoca con el id de la cámara elegida.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onCameraClick: (String) -> Unit, onAboutClick: () -> Unit) {
    var selectedTab by remember { mutableStateOf(NavTab.CAMERAS) }
    // Agrupa las cámaras por volcán preservando el orden del catálogo.
    val groups = remember { CameraRepository.cameras.groupBy { it.volcanoGroupRes } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onAboutClick) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = stringResource(R.string.about_title),
                        )
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                NavTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = stringResource(tab.labelRes)) },
                        label = { Text(stringResource(tab.labelRes)) },
                    )
                }
            }
        },
    ) { innerPadding ->
        when (selectedTab) {
            NavTab.CAMERAS -> CameraListContent(
                groups = groups,
                innerPadding = innerPadding,
                onCameraClick = onCameraClick,
            )
            NavTab.NEWS, NavTab.MAP -> ComingSoonContent(innerPadding = innerPadding)
        }
    }
}

/** Lista de cámaras agrupadas por volcán con encabezados y thumbnails. */
@Composable
private fun CameraListContent(
    groups: Map<Int, List<Camera>>,
    innerPadding: PaddingValues,
    onCameraClick: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(bottom = 8.dp),
    ) {
        groups.forEach { (groupRes, cameras) ->
            item(key = "header_$groupRes") {
                VolcanoSectionHeader(titleRes = groupRes)
            }
            items(cameras, key = { it.id }) { camera ->
                CameraCard(camera = camera, onClick = { onCameraClick(camera.id) })
            }
        }
    }
}

/** Placeholder para pestañas aún no implementadas. */
@Composable
private fun ComingSoonContent(innerPadding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.coming_soon),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Encabezado de sección: barra de acento en rojo lava + nombre del volcán en versalitas. */
@Composable
private fun VolcanoSectionHeader(titleRes: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(18.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(2.dp),
                ),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = stringResource(titleRes).uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
            ),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

/** Tarjeta de cámara: thumbnail en vivo, nombre y punto de estado de señal. */
@Composable
private fun CameraCard(camera: Camera, onClick: () -> Unit) {
    // null = cargando, true = imagen ok, false = error
    var loadOk by remember { mutableStateOf<Boolean?>(null) }
    val imageUrl = remember(camera.id) {
        CameraRepository.imageUrl(camera, System.currentTimeMillis())
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Thumbnail live de la cámara
            Box(
                modifier = Modifier
                    .size(width = 96.dp, height = 72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    onSuccess = { loadOk = true },
                    onError = { loadOk = false },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Spacer(Modifier.width(14.dp))

            Text(
                text = stringResource(camera.titleRes),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )

            StatusDot(loadOk = loadOk)

            Spacer(Modifier.width(4.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/** Punto de señal: pulsa en verde si la imagen cargó, gris si hubo error o está cargando. */
@Composable
private fun StatusDot(loadOk: Boolean?) {
    val isLive = loadOk == true
    val baseColor = if (isLive) Color(0xFF4CAF50) else Color(0xFF9E9E9E)

    val infiniteTransition = rememberInfiniteTransition(label = "statusPulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isLive) 0.35f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dotAlpha",
    )

    Box(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .size(8.dp)
            .background(color = baseColor.copy(alpha = alpha), shape = CircleShape),
    )
}
