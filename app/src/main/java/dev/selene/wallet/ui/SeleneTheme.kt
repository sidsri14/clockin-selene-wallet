package dev.selene.wallet.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val SeleneNight = Color(0xFF0B0E1A)
val SeleneSurface = Color(0xFF141A33)
val SeleneSurfaceHigh = Color(0xFF1D2442)
val SeleneAmethyst = Color(0xFF9B7BFF)
val SeleneMint = Color(0xFF51E5C3)
val SeleneText = Color(0xFFE9ECFF)
val SeleneTextDim = Color(0xFF8B93B8)

private val SeleneColors = darkColorScheme(
    primary = SeleneAmethyst,
    onPrimary = Color(0xFF160C2E),
    secondary = SeleneMint,
    onSecondary = Color(0xFF0A221B),
    background = SeleneNight,
    onBackground = SeleneText,
    surface = SeleneSurface,
    onSurface = SeleneText,
    surfaceVariant = SeleneSurfaceHigh,
    onSurfaceVariant = SeleneTextDim,
    error = Color(0xFFFF7A85)
)

@Composable
fun SeleneTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SeleneColors,
        typography = MaterialTheme.typography,
        content = content
    )
}