package org.cec.brick.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun CecColors(): ColorScheme {
    return MaterialTheme.colorScheme.copy(
        primary = Color.hsl(353f, 0.8f, 0.49f), // hsla(353, 80%, 49%, 1)
    )
}
