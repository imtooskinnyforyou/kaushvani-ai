package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkSurface,
    primaryContainer = TerracottaDark,
    onPrimaryContainer = TerracottaLight,
    secondary = DarkSecondary,
    onSecondary = DarkSurface,
    secondaryContainer = IndigoSecondary,
    tertiary = DarkTertiary,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = ArtisanSurfaceVariant,
    onSurface = ArtisanSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = TerracottaPrimary,
    onPrimary = OnTerracottaPrimary,
    primaryContainer = TerracottaContainer,
    onPrimaryContainer = TerracottaDark,
    secondary = IndigoSecondary,
    onSecondary = OnIndigoSecondary,
    secondaryContainer = IndigoSecondaryContainer,
    tertiary = MarigoldTertiary,
    onTertiary = OnMarigoldTertiary,
    tertiaryContainer = MarigoldContainer,
    background = ArtisanBackground,
    surface = ArtisanSurface,
    surfaceVariant = ArtisanSurfaceVariant,
    onBackground = ArtisanTextPrimary,
    onSurface = ArtisanTextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep warm artisanal branding consistent
    content: @Composable () -> Unit,
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
