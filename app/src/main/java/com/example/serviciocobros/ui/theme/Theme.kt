package com.example.serviciocobros.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Esquema Oscuro
private val DarkColorScheme = darkColorScheme(
    primary = OrangeTerracotta,
    onPrimary = Color.White,
    secondary = GreenEmerald,
    background = BlueMidnight,
    surface = GraySurfaceDark,
    onBackground = WhiteCream,
    onSurface = WhiteCream,
    error = RedCoral
)

// Esquema Claro (El principal de tu diseño)
private val LightColorScheme = lightColorScheme(
    primary = OrangeTerracotta,
    onPrimary = Color.White,
    secondary = GreenEmerald,
    tertiary = RedCoral,

    background = WhiteCream,
    surface = Color.White, // Las tarjetas blancas sobre fondo crema

    onBackground = BlueNight,
    onSurface = BlueNight,
    onSecondary = Color.White,
    error = RedCoral
)

@Composable
fun ServicioCobrosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color está disponible en Android 12+
    dynamicColor: Boolean = false, // Lo desactivamos para forzar TU marca (Naranja)
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Pintamos la barra de estado del color del fondo para que se vea integrado
            window.statusBarColor = colorScheme.background.toArgb()

            // Iconos oscuros si el fondo es claro
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}