package com.aiadvertisement.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = Purple500,
    onPrimary = White,
    primaryContainer = Purple100,
    onPrimaryContainer = Purple900,
    secondary = Teal200,
    onSecondary = White,
    secondaryContainer = Teal100,
    onSecondaryContainer = Teal900,
    tertiary = Pink400,
    background = Gray50,
    onBackground = Gray900,
    surface = White,
    onSurface = Gray900,
    error = Red500,
    onError = White
)

private val DarkColorScheme = darkColorScheme(
    primary = Purple200,
    onPrimary = Purple900,
    primaryContainer = Purple700,
    onPrimaryContainer = Purple100,
    secondary = Teal200,
    onSecondary = Teal900,
    secondaryContainer = Teal700,
    onSecondaryContainer = Teal100,
    tertiary = Pink200,
    background = Gray900,
    onBackground = White,
    surface = Gray800,
    onSurface = White,
    error = Red300,
    onError = Gray900
)

@Composable
fun AIAdvertisementTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
