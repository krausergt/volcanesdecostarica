package krausoft.volcanesdecostarica

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import krausoft.volcanesdecostarica.ui.theme.VolcanesCostaRicaTheme

/**
 * Única Activity de la app: hospeda toda la interfaz declarativa en Jetpack Compose.
 * La navegación real entre pantallas se agrega en tareas posteriores (T7+).
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // dibuja el contenido detrás de las barras del sistema
        setContent {
            VolcanesCostaRicaTheme {
                PlaceholderScreen()
            }
        }
    }
}

/** Pantalla temporal del Checkpoint A; se reemplaza por la UI real en las próximas tareas. */
@Composable
private fun PlaceholderScreen() {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Text(
            text = "Volcanes de Costa Rica",
            modifier = Modifier.padding(innerPadding),
        )
    }
}
