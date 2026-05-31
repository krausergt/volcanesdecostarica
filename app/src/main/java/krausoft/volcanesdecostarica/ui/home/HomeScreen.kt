package krausoft.volcanesdecostarica.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import krausoft.volcanesdecostarica.R
import krausoft.volcanesdecostarica.data.CameraRepository

/**
 * Pantalla principal: lista las cámaras disponibles. Placeholder funcional del
 * Checkpoint C (sólo permite navegar); se enriquece visualmente en T8.
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
                    modifier = Modifier.clickable { onCameraClick(camera.id) },
                )
                HorizontalDivider()
            }
        }
    }
}
