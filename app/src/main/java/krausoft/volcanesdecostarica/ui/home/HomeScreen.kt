package krausoft.volcanesdecostarica.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import krausoft.volcanesdecostarica.R
import krausoft.volcanesdecostarica.data.CameraRepository

/**
 * Pantalla principal: lista las 8 cámaras del catálogo. Al tocar una, navega a
 * su visor en vivo.
 *
 * @param onCameraClick se invoca con el id de la cámara elegida.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onCameraClick: (String) -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            items(CameraRepository.cameras, key = { it.id }) { camera ->
                ListItem(
                    headlineContent = { Text(stringResource(camera.titleRes)) },
                    leadingContent = {
                        Image(
                            painter = painterResource(R.drawable.volcano_icon),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                        )
                    },
                    trailingContent = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                        )
                    },
                    modifier = Modifier.clickable { onCameraClick(camera.id) },
                )
                HorizontalDivider()
            }
        }
    }
}
