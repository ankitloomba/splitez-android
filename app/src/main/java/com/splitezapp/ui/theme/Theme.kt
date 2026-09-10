package com.splitezapp.ui.theme

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Core palette — aligned with design spec
val Primary = Color(0xFF4338CA)        // Indigo
val PrimaryLight = Color(0xFFC7D2FE)   // Light indigo
val DarkBg = Color(0xFF10142A)         // Deep navy header
val Positive = Color(0xFF4ADE80)       // Green
val Negative = Color(0xFFDC2626)       // Red
val Accent = Color(0xFFEEF0FF)         // Tinted surface
val Muted = Color(0xFF8792A8)          // Inactive/secondary
val Surface = Color(0xFFF1F5F9)
val OnSurface = Color(0xFF1A1A1A)
val BalanceGreen = Color(0xFF16A34A)
val TextSecondary = Color(0xFF64748B)
val Divider = Color(0xFFEEF0F4)
val PillActive = Color(0xFF4338CA)
val PillInactive = Color(0xFFF1F5F9)

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
