package io.ntfy.wear.ui.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

private val NtfyColors = Colors(
    primary = androidx.compose.ui.graphics.Color(0xFF4FD8C4),
    primaryVariant = androidx.compose.ui.graphics.Color(0xFF1B7B6F),
    secondary = androidx.compose.ui.graphics.Color(0xFF4FD8C4),
    error = androidx.compose.ui.graphics.Color(0xFFCF6679)
)

@Composable
fun NtfyTheme(content: @Composable () -> Unit) {
    MaterialTheme(colors = NtfyColors, content = content)
}
