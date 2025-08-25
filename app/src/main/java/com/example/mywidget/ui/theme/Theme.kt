package com.example.mywidget.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Composable
fun MyWidgetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme(
            primary = PrimaryDark,
            secondary = SecondaryDark,
            tertiary = TertiaryDark,
            background = BackgroundDark,
            surface = SurfaceDark,
            onPrimary = Color.Black,
            onSecondary = Color.Black,
            onTertiary = Color.Black,
            onBackground = OnBackgroundDark,
            onSurface = OnSurfaceDark,
            outline = OutlineDark,
            surfaceVariant = CardBackgroundDark,
            onSurfaceVariant = OnSurfaceDark
        )
        else -> lightColorScheme(
            primary = Primary,
            secondary = Secondary,
            tertiary = Tertiary,
            background = BackgroundLight,
            surface = SurfaceLight,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onTertiary = Color.White,
            onBackground = OnBackgroundLight,
            onSurface = OnSurfaceLight,
            outline = OutlineLight,
            surfaceVariant = CardBackgroundLight,
            onSurfaceVariant = OnSurfaceLight
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}