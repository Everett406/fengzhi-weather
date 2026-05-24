package com.fengzhi.weather.ui.theme

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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = OrangePrimary,
    onPrimary = OrangeOnPrimary,
    primaryContainer = OrangePrimaryContainer,
    onPrimaryContainer = OrangeOnPrimaryContainer,
    secondary = OrangeSecondary,
    onSecondary = OrangeOnSecondary,
    secondaryContainer = OrangeSecondaryContainer,
    onSecondaryContainer = OrangeOnSecondaryContainer,
    tertiary = OrangeTertiary,
    onTertiary = OrangeOnTertiary,
    tertiaryContainer = OrangeTertiaryContainer,
    onTertiaryContainer = OrangeOnTertiaryContainer,
    error = ErrorLight,
    onError = ErrorOnLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = ErrorOnContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    scrim = ScrimLight,
    inverseSurface = InverseSurfaceLight,
    inverseOnSurface = InverseOnSurfaceLight,
    inversePrimary = InversePrimaryLight,
    surfaceTint = SurfaceTintLight
)

private val DarkColorScheme = darkColorScheme(
    primary = OrangePrimary,
    onPrimary = OrangeOnPrimary,
    primaryContainer = OrangePrimaryContainer,
    onPrimaryContainer = OrangeOnPrimaryContainer,
    secondary = OrangeSecondary,
    onSecondary = OrangeOnSecondary,
    secondaryContainer = OrangeSecondaryContainer,
    onSecondaryContainer = OrangeOnSecondaryContainer,
    tertiary = OrangeTertiary,
    onTertiary = OrangeOnTertiary,
    tertiaryContainer = OrangeTertiaryContainer,
    onTertiaryContainer = OrangeOnTertiaryContainer,
    error = ErrorLight,
    onError = ErrorOnLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = ErrorOnContainerLight,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    scrim = ScrimDark,
    inverseSurface = InverseSurfaceDark,
    inverseOnSurface = InverseOnSurfaceDark,
    inversePrimary = InversePrimaryDark,
    surfaceTint = SurfaceTintDark
)

@Composable
fun FengzhiWeatherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
