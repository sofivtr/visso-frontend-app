package cl.duoc.visso.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = White,
    primaryContainer = BlueLight,
    onPrimaryContainer = BluePrimaryDark,

    secondary = BlueAccent,
    onSecondary = White,

    background = White,
    onBackground = TextPrimary,

    surface = White,
    onSurface = TextPrimary,

    error = ErrorRed,
    onError = White,

    outline = Divider
)

@Composable
fun VissoAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Siempre usamos tema claro según requerimiento
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}