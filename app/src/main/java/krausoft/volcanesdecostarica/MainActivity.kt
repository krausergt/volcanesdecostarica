package krausoft.volcanesdecostarica

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import krausoft.volcanesdecostarica.ui.theme.VolcanesCostaRicaTheme

/**
 * Única Activity de la app: hospeda toda la interfaz declarativa en Jetpack Compose
 * y arranca el grafo de navegación ([VolcanoApp]) dentro del tema de la app.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Debe llamarse ANTES de super.onCreate() para que el sistema configure
        // la ventana del splash antes de que se infle el layout.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // dibuja el contenido detrás de las barras del sistema
        setContent {
            VolcanesCostaRicaTheme {
                VolcanoApp()
            }
        }
    }
}
