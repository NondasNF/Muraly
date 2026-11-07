package br.com.mural.core.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Immutable
data class MuralExtraColors(
    val emptyCell: Color,
    val futureCell: Color,
    val dayNumberEmpty: Color
)

val LocalMuralExtraColors = staticCompositionLocalOf {
    MuralExtraColors(
        emptyCell = LightEmptyCell,
        futureCell = LightFutureCell,
        dayNumberEmpty = LightDayNumberEmpty
    )
}

private val LightColorScheme = lightColorScheme(
    primary = LightTerracotta,
    onPrimary = Color.White,
    secondary = LightTextSecondary,
    background = LightBackground,
    surface = LightSurface,
    onBackground = LightText,
    onSurface = LightText
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkTerracotta,
    onPrimary = Color.White,
    secondary = DarkTextSecondary,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkText,
    onSurface = DarkText
)

@Composable
fun MuralTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extraColors = if (darkTheme) {
        MuralExtraColors(
            emptyCell = DarkEmptyCell,
            futureCell = DarkFutureCell,
            dayNumberEmpty = DarkDayNumberEmpty
        )
    } else {
        MuralExtraColors(
            emptyCell = LightEmptyCell,
            futureCell = LightFutureCell,
            dayNumberEmpty = LightDayNumberEmpty
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalMuralExtraColors provides extraColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

object MuralTheme {
    val extraColors: MuralExtraColors
        @Composable
        get() = LocalMuralExtraColors.current
}
