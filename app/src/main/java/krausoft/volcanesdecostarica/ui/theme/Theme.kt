package krausoft.volcanesdecostarica.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Tema Material 3 de la app (stub del Checkpoint A).
 * Se completa en T6 con la paleta de colores y tipografía propias.
 *
 * @param darkTheme si se usa el esquema oscuro; por defecto sigue al sistema.
 */
@Composable
fun VolcanesCostaRicaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()
    MaterialTheme(colorScheme = colorScheme, content = content)
}
