package com.splitezapp.ui.theme

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Core palette — aligned with artifact design
val Primary = Color(0xFF3890F5)        // Blue
val PrimaryLight = Color(0xFF5BA8F7)   // Light blue
val DarkBg = Color(0xFF1A2235)         // Dark surface
val Positive = Color(0xFF2EC770)       // Green
val Negative = Color(0xFFEB5757)       // Red
val Accent = Color(0xFF22D4A6)         // Teal
val Muted = Color(0xFF5A6B82)          // Inactive/secondary
val Surface = Color(0xFFF5F7FA)
val OnSurface = Color(0xFF1A2233)
val TextSecondary = Color(0xFF5A6B82)
val TextTertiary = Color(0xFF8D9BB0)
val Divider = Color(0xFFD8E0EB)
val PillActive = Color(0xFF3890F5)
val PillInactive = Color(0xFFE8EDF4)
val SurfaceAlt = Color(0xFFEDF1F7)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Accent,
    error = Negative,
    surface = Surface,
    onSurface = OnSurface,
)

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    secondary = Accent,
    error = Negative,
)

@Composable
fun SplitEZTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content
    )
}

fun avatarColor(userId: String): Color {
    val hash = userId.hashCode()
    val hue = (hash and 0x7FFFFFFF) % 360
    val color = AndroidColor.HSVToColor(floatArrayOf(hue.toFloat(), 0.6f, 0.75f))
    return Color(color)
}

fun avatarInitials(firstName: String, lastName: String?): String {
    val first = firstName.firstOrNull()?.uppercase() ?: ""
    val last = lastName?.firstOrNull()?.uppercase() ?: ""
    return "$first$last".ifEmpty { "?" }
}
