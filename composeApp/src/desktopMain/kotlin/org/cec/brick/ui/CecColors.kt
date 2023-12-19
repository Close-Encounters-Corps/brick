package org.cec.brick.ui

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun CecColors(): Colors {
    return Colors(
        isLight = true,
        primary = Color.hsl(353f, 0.8f, 0.49f), // hsla(353, 80%, 49%, 1)
        onPrimary = Color.White,
        secondary = Color.Black,
        onSecondary = Color.White,
        background = Color.Black,
        onBackground = Color.White,
        error = Color.Black,
        onError = Color.White,
        primaryVariant = Color.Black,
        secondaryVariant = Color.White,
        surface = Color.Black,
        onSurface = Color.White,
    )
}
