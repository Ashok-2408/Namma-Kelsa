package com.nammakelsa.core.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Orange500,
    onPrimary = OnPrimary,
    primaryContainer = Orange200,
    secondary = Blue500,
    onSecondary = OnSecondary,
    secondaryContainer = Blue200,
    tertiary = Green500,
    tertiaryContainer = Green200,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    error = Error,
    onError = OnPrimary
)

private val DarkColorScheme = darkColorScheme(
    primary = Orange200,
    onPrimary = OnBackground,
    primaryContainer = Orange700,
    secondary = Blue200,
    onSecondary = OnBackground,
    secondaryContainer = Blue500,
    tertiary = Green200,
    tertiaryContainer = Green500,
    background = DarkBackground,
    onBackground = Background,
    surface = DarkSurface,
    onSurface = Background,
    error = Error,
    onError = OnPrimary
)

@Composable
fun NammaKelsaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
