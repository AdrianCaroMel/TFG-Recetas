package es.tfg.recetas.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Color(0xFF2E7D32),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB8F0B9),
    onPrimaryContainer = Color(0xFF002106),
    secondary = Color(0xFFF57C00),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDCC2),
    onSecondaryContainer = Color(0xFF2E1500),
    background = Color(0xFFFBFDF7),
    surface = Color(0xFFFBFDF7),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9CD69D),
    onPrimary = Color(0xFF00390F),
    primaryContainer = Color(0xFF12531B),
    onPrimaryContainer = Color(0xFFB8F0B9),
    secondary = Color(0xFFFFB77C),
    onSecondary = Color(0xFF4C2700),
    secondaryContainer = Color(0xFF6C3A00),
    onSecondaryContainer = Color(0xFFFFDCC2),
)

@Composable
fun RecetasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colors, content = content)
}
