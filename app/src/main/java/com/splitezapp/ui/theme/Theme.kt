package com.splitezapp.ui.theme

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Primary = Color(0xFF3890F5)
val Accent = Color(0xFF22D4A6)
val Positive = Color(0xFF2EC76F)
val Negative = Color(0xFFEB5757)
val Surface = Color(0xFFF8F9FA)
val OnSurface = Color(0xFF1A1A1A)

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
