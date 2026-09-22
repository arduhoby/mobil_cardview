package com.kartview.rolodex

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

data class RolodexConfig(
    val stepRatio: Float = 0.44f,
    val rowHeightScale: Float = 0.40f,
    val widthFirstRow: Float = 0.90f,
    val widthSecondRow: Float = 0.80f,
    val widthMin: Float = 0.42f,
    val deformStartRow: Float = 2.75f,
    val deformSpan: Float = 1.25f,
    val cascadePerUnit: Float = 0.20f,
    val cascadeMaxWidth: Float = 0.55f,
    val rotationDeg: Float = 14f,
    val fadeStartRow: Float = 4.1f,
    val fadeSpan: Float = 0.7f,
) {
    companion object {
        val Default = RolodexConfig()
    }
}

@Composable
fun <T> Rolodex(
    items: List<T>,
    key: (T) -> Any,
    cardHeight: Dp,
    onDismiss: (T) -> Unit,
    modifier: Modifier = Modifier,
    spacing: Dp = 6.dp,
    config: RolodexConfig = RolodexConfig.Default,
    onFocusChanged: ((Int) -> Unit)? = null,
    content: @Composable (T, Boolean) -> Unit,
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val scroll = remember { Animatable(0f) }
    var focusedIndex by remember { mutableIntStateOf(0) }
    var viewportW by remember { mutableFloatStateOf(1f) }
    var viewportH by remember { mutableFloatStateOf(1f) }

    var lastFocused by remember { mutableIntStateOf(-1) }
    val reportFocus = { new: Int ->
        if (lastFocused != new) {
            lastFocused = new
            onFocusChanged?.invoke(new)
        }
    }

    val cardPx = with(density) { cardHeight.toPx() }
    val spacingPx = with(density) { spacing.toPx() }
    val stepPx = (cardPx * config.stepRatio + spacingPx).coerceAtLeast(1f)
    val maxOffset = ((items.size - 1).coerceAtLeast(0)) * stepPx
    val baseTopPx = ((viewportH - cardPx) / 2f).roundToInt()

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged {
                viewportW = it.width.toFloat().coerceAtLeast(1f)
                viewportH = it.height.toFloat().coerceAtLeast(1f)
            }
            .pointerInput(items.size, stepPx, maxOffset) {
                val tracker = VelocityTracker()
                detectVerticalDragGestures(
                    onDragStart = {
                        tracker.resetTracking()
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        tracker.addPosition(change.uptimeMillis, change.position)
                        scope.launch {
                            scroll.snapTo((scroll.value - dragAmount).coerceIn(0f, maxOffset))
                        }
                    },
                    onDragEnd = {
                        val velocity = tracker.calculateVelocity().y
                        val projected = scroll.value / stepPx - velocity * 0.00035f
                        val targetIndex = projected
                            .roundToInt()
                            .coerceIn(0, (items.size - 1).coerceAtLeast(0))
                        if (focusedIndex != targetIndex) {
                            reportFocus(targetIndex)
                        }
                        focusedIndex = targetIndex
                        scope.launch {
                            scroll.animateTo(
                                targetIndex * stepPx,
                                spring(dampingRatio = 0.82f, stiffness = 420f),
                            )
                        }
                    },
                )
            }
    ) {
        items.forEachIndexed { index, item ->
            val itemKey = key(item)
            val isFocused = index == focusedIndex
            RolodexCard(
                key = itemKey,
                isFocused = isFocused,
                thresholdPx = viewportW * 0.22f,
                dismissExitPx = viewportW * 1.2f,
                onDismiss = { onDismiss(item) },
                modifier = Modifier
                    .zIndex(if (isFocused) 200f else 100f - abs(index - focusedIndex))
                    .offset { IntOffset(0, baseTopPx) }
                    .graphicsLayer {
                        val delta = index * stepPx - scroll.value
                        val r = abs(delta) / stepPx
                        val deform = ((r - config.deformStartRow) / config.deformSpan).coerceIn(0f, 1f)
                        val sy = lerp(1f, config.rowHeightScale, (r / 0.5f).coerceIn(0f, 1f))
                        val sx = when {
                            r < 0.5f -> lerp(1f, config.widthFirstRow, r / 0.5f)
                            r < 1.5f -> config.widthFirstRow
                            r < 2.5f -> config.widthSecondRow
                            else -> lerp(config.widthSecondRow, config.widthMin, deform)
                        }
                        val fade = ((r - config.fadeStartRow) / config.fadeSpan).coerceIn(0f, 1f)
                        val cascade = if (r < 3f) {
                            0f
                        } else {
                            viewportW * (config.cascadePerUnit * (r - 3f)).coerceAtMost(config.cascadeMaxWidth)
                        }
                        scaleX = sx
                        scaleY = sy
                        translationX = cascade
                        translationY = delta
                        rotationY = if (r >= 3f) {
                            (delta / stepPx).coerceIn(-3f, 3f) *
                                config.rotationDeg * ((r - 3f) / 1.2f).coerceAtMost(1f)
                        } else {
                            0f
                        }
                        alpha = 1f - fade * 0.98f
                    },
            ) {
                content(item, isFocused)
            }
        }
    }
}

@Composable
private fun RolodexCard(
    key: Any,
    isFocused: Boolean,
    thresholdPx: Float,
    dismissExitPx: Float,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val dragX = remember(key) { Animatable(0f) }
    val dragAlpha = remember(key) { Animatable(1f) }
    var dismissing by remember(key) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .pointerInput(key, isFocused) {
                if (!isFocused) return@pointerInput
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (dismissing) return@detectHorizontalDragGestures
                        scope.launch {
                            if (abs(dragX.value) < thresholdPx) {
                                dragX.animateTo(
                                    0f,
                                    spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium,
                                    ),
                                )
                            } else {
                                dismissing = true
                                coroutineScope {
                                    launch {
                                        dragX.animateTo(
                                            sign(dragX.value) * dismissExitPx,
                                            tween(durationMillis = 280),
                                        )
                                    }
                                    launch { dragAlpha.animateTo(0f, tween(durationMillis = 280)) }
                                }
                                onDismiss()
                            }
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        if (dismissing) return@detectHorizontalDragGestures
                        scope.launch { dragX.snapTo(dragX.value + dragAmount) }
                    },
                )
            }
            .graphicsLayer {
                translationX = dragX.value
                alpha = dragAlpha.value
                rotationZ = dragX.value * 0.02f
            },
    ) {
        content()
    }
}