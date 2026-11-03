package com.payamaknashnas.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.payamaknashnas.app.R

/** Palette used across the app: deep ink + champagne gold (no default Android blue). */
object Lux {
    val Ink = Color(0xFF07090D)
    val InkSoft = Color(0xFF0B0E14)
    val Surface = Color(0xFF11161D)
    val SurfaceHigh = Color(0xFF171E27)
    val Stroke = Color(0xFF252E3A)
    val StrokeSoft = Color(0xFF1B222C)
    val Gold = Color(0xFFD4AF6A)
    val GoldDeep = Color(0xFFA8843C)
    val GoldGhost = Color(0x1FD4AF6A)
    val TextPrimary = Color(0xFFF4F1EA)
    val TextSecondary = Color(0xFF98A4B3)
    val TextMuted = Color(0xFF6C7787)
    val Danger = Color(0xFFE0605A)
    val DangerGhost = Color(0x1FE0605A)
    val Success = Color(0xFF57C58A)
}

val VazirFamily = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_bold, FontWeight.Bold)
)

private val LuxTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = VazirFamily, fontWeight = FontWeight.Bold,
        fontSize = 30.sp, lineHeight = 42.sp, color = Lux.TextPrimary
    ),
    headlineMedium = TextStyle(
        fontFamily = VazirFamily, fontWeight = FontWeight.Bold,
        fontSize = 24.sp, lineHeight = 36.sp, color = Lux.TextPrimary
    ),
    headlineSmall = TextStyle(
        fontFamily = VazirFamily, fontWeight = FontWeight.Bold,
        fontSize = 20.sp, lineHeight = 32.sp, color = Lux.TextPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = VazirFamily, fontWeight = FontWeight.Medium,
        fontSize = 18.sp, lineHeight = 28.sp, color = Lux.TextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = VazirFamily, fontWeight = FontWeight.Medium,
        fontSize = 15.sp, lineHeight = 24.sp, color = Lux.TextPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = VazirFamily, fontWeight = FontWeight.Normal,
        fontSize = 15.sp, lineHeight = 26.sp, color = Lux.TextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = VazirFamily, fontWeight = FontWeight.Normal,
        fontSize = 13.5.sp, lineHeight = 23.sp, color = Lux.TextSecondary
    ),
    bodySmall = TextStyle(
        fontFamily = VazirFamily, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 20.sp, color = Lux.TextMuted
    ),
    labelLarge = TextStyle(
        fontFamily = VazirFamily, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp
    )
)

private val LuxColors = darkColorScheme(
    primary = Lux.Gold,
    onPrimary = Color(0xFF1A1305),
    primaryContainer = Lux.GoldDeep,
    onPrimaryContainer = Lux.TextPrimary,
    secondary = Lux.Gold,
    onSecondary = Color(0xFF1A1305),
    background = Lux.Ink,
    onBackground = Lux.TextPrimary,
    surface = Lux.Surface,
    onSurface = Lux.TextPrimary,
    surfaceVariant = Lux.SurfaceHigh,
    onSurfaceVariant = Lux.TextSecondary,
    outline = Lux.Stroke,
    error = Lux.Danger,
    onError = Color.White
)

@Composable
fun PayamakTheme(content: @Composable () -> Unit) {
    // The app is dark-first by design; the light system flag is intentionally ignored
    // so the luxury dark identity stays consistent on every device.
    isSystemInDarkTheme()
    MaterialTheme(colorScheme = LuxColors, typography = LuxTypography, content = content)
}
