package com.kartview.glasscard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas

class GlassBackdrop internal constructor(
    internal val size: Size,
    internal val draw: (Canvas) -> Unit,
)

@Composable
fun rememberGlassBackdrop(
    size: Size,
    draw: (Canvas) -> Unit,
): GlassBackdrop {
    return remember(size) { GlassBackdrop(size, draw) }
}