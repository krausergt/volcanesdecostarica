package krausoft.volcanesdecostarica.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Esquema claro fijo (se usa si no hay color dinámico disponible).
private val LightColors = lightColorScheme(
    primary = LavaRed,
    onPrimary = OnLava,
    primaryContainer = LavaContainer,
    onPrimaryContainer = OnLavaContainer,
    secondary = EmberBrown,
    onSecondary = OnEmber,
    secondaryContainer = EmberContainer,
    onSecondaryContainer = OnEmberContainer,
    tertiary = SulfurGold,
    onTertiary = OnSulfur,
    background = LightBackground,
    onBackground = OnLightBackground,
    surface = LightBackground,
    onSurface = OnLightBackground,
)

// Esquema oscuro fijo.
private val DarkColors = darkColorScheme(
    primary = LavaRedDark,
    onPrimary = OnLavaDark,
    primaryContainer = LavaContainerDark,
    onPrimaryContainer = OnLavaContainerDark,
    secondary = EmberBrownDark,
    onSecondary = OnEmberDark,
    secondaryContainer = EmberContainerDark,
    onSecondaryContainer = OnEmberContainerDark,
    tertiary = SulfurGoldDark,
    onTertiary = OnSulfurDark,
    background = DarkBackground,
    onBackground = OnDarkBackground,
    surface = DarkBackground,
    onSurface = OnDarkBackground,
)

/**
 * Tema Material 3 de la app.
 *
 * Usa color dinámico (Material You) en Android 12+ y, si no, cae a la paleta
 * volcánica fija definida arriba.
 *
 * @param darkTheme si se usa el esquema oscuro; por defecto sigue al sistema.
 * @param dynamicColor si se permite el color dinámico del sistema (Android 12+).
 */
@Composable
fun VolcanesCostaRicaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}
