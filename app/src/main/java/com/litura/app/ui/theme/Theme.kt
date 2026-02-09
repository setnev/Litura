package com.litura.app.ui.theme

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
    primary = LituraPrimary,
    onPrimary = LituraOnPrimary,
    secondary = LituraSecondary,
    onSecondary = LituraOnSecondary,
    tertiary = LituraTertiary,
    onTertiary = LituraOnTertiary,
    error = LituraError,
    onError = LituraOnError,
    background = LituraBackground,
    onBackground = LituraOnBackground,
    surface = LituraSurface,
    onSurface = LituraOnSurface,
    surfaceVariant = LituraSurfaceVariant,
    onSurfaceVariant = LituraOnSurfaceVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = LituraPrimary,
    onPrimary = LituraOnPrimary,
    secondary = LituraSecondary,
    onSecondary = LituraOnSecondary,
    tertiary = LituraTertiary,
    onTertiary = LituraOnTertiary,
    error = LituraErrorDark,
    onError = LituraOnError,
    background = LituraBackgroundDark,
    onBackground = LituraOnBackgroundDark,
    surface = LituraSurfaceDark,
    onSurface = LituraOnSurfaceDark,
    surfaceVariant = LituraSurfaceVariantDark,
    onSurfaceVariant = LituraOnSurfaceVariantDark
)

@Composable
fun LituraTheme(
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
        typography = LituraTypography,
        content = content
    )
}
