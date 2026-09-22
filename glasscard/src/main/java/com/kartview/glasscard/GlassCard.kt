package com.kartview.glasscard

import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class StripePosition { Top, Bottom, Left, Right }

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = GlassCardDefaults.CornerRadius,
    backgroundColor: Color = GlassCardDefaults.BackgroundColor,
    backgroundOpacity: Float = GlassCardDefaults.BackgroundOpacity,
    blurIntensity: Float = GlassCardDefaults.BlurIntensity,
    borderColor: Color = GlassCardDefaults.BorderColor,
    borderWidth: Dp = GlassCardDefaults.BorderWidth,
    showBorder: Boolean = true,
    elevation: Dp = GlassCardDefaults.Elevation,
    gradientEnabled: Boolean = true,
    stripeEnabled: Boolean = true,
    stripePosition: StripePosition = StripePosition.Left,
    stripeThickness: Dp = GlassCardDefaults.StripeThickness,
    stripeColor: Color = GlassCardDefaults.StripeColor,
    backdrop: GlassBackdrop? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape: Shape = RoundedCornerShape(cornerRadius)
    val cardPosition: MutableState<Offset> = remember { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current

    val blurPx = with(density) { blurIntensity.dp.toPx() }.coerceAtLeast(1f)
    val blurSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurIntensity > 0f

    Box(
        modifier = modifier
            .shadow(elevation = elevation, shape = shape, clip = false)
            .onGloballyPositioned { cardPosition.value = it.positionInRoot() }
            .clip(shape)
    ) {
        if (backdrop != null && blurSupported) {
            val source = backdrop
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .drawBehind {
                        val pos = cardPosition.value
                        drawIntoCanvas { canvas ->
                            canvas.save()
                            canvas.translate(-pos.x, -pos.y)
                            source.draw(canvas)
                            canvas.restore()
                        }
                    }
                    .graphicsLayer {
                        compositingStrategy = CompositingStrategy.Offscreen
                        renderEffect = BlurEffect(blurPx, blurPx, TileMode.Clamp)
                    }
            )
        }
        Column(
            modifier = Modifier
                .zIndex(1f)
                .drawBehind {
                    val radius = cornerRadius.toPx()
                    if (gradientEnabled) {
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    backgroundColor.copy(alpha = backgroundOpacity.coerceIn(0f, 1f)),
                                    backgroundColor.copy(alpha = (backgroundOpacity * 0.55f).coerceIn(0f, 1f)),
                                )
                            ),
                            cornerRadius = CornerRadius(radius)
                        )
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White.copy(alpha = 0.38f), Color.Transparent),
                                center = Offset(size.width * 0.5f, size.height * 0.16f),
                                radius = size.width * 0.95f,
                            ),
                            topLeft = Offset.Zero,
                            size = size,
                        )
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.White.copy(alpha = 0.50f), Color.Transparent),
                                startY = 0f,
                                endY = size.height * 0.30f,
                            ),
                            topLeft = Offset.Zero,
                            size = size,
                            cornerRadius = CornerRadius(radius)
                        )
                        drawBombeShading(radius = radius)
                    } else {
                        drawRoundRect(
                            color = backgroundColor.copy(alpha = backgroundOpacity.coerceIn(0f, 1f)),
                            cornerRadius = CornerRadius(radius)
                        )
                    }
                    if (stripeEnabled && stripeThickness > 0.dp) {
                        drawAccentStripe(
                            thicknessPx = stripeThickness.toPx(),
                            color = stripeColor,
                            radius = radius,
                            position = stripePosition,
                        )
                    }
                    if (showBorder && borderWidth > 0.dp) {
                        drawRoundRect(
                            color = borderColor,
                            style = Stroke(width = borderWidth.toPx()),
                            cornerRadius = CornerRadius(radius)
                        )
                    }
                }
        ) {
            content()
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBombeShading(radius: Float) {
    val w = size.width
    val h = size.height
    drawRoundRect(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.Transparent,
                Color.Black.copy(alpha = 0.10f),
                Color.Black.copy(alpha = 0.30f),
            ),
            center = Offset(w * 0.5f, h * 0.38f),
            radius = size.minDimension * 0.58f,
        ),
        topLeft = Offset.Zero,
        size = size,
        cornerRadius = CornerRadius(radius),
    )
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.70f),
                Color.Transparent,
                Color.Transparent,
                Color.Black.copy(alpha = 0.42f),
            ),
            start = Offset(0f, 0f),
            end = Offset(w, h),
        ),
        topLeft = Offset.Zero,
        size = size,
        cornerRadius = CornerRadius(radius),
        style = Stroke(width = 2.6.dp.toPx()),
    )
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.White.copy(alpha = 0.55f), Color.White.copy(alpha = 0.05f)),
            startY = 0f,
            endY = h * 0.35f,
        ),
        topLeft = Offset.Zero,
        size = size,
        cornerRadius = CornerRadius(radius),
        style = Stroke(width = 2.2.dp.toPx()),
    )
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.45f)),
            startY = h * 0.86f,
            endY = h,
        ),
        topLeft = Offset.Zero,
        size = size,
        cornerRadius = CornerRadius(radius),
        style = Stroke(width = 1.4.dp.toPx()),
    )
    rotate(degrees = 24f, pivot = Offset(w * 0.42f, h * 0.30f)) {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color.White.copy(alpha = 0.32f), Color.Transparent),
                start = Offset(0f, 0f),
                end = Offset(w * 0.9f, 0f),
            ),
            topLeft = Offset(-w * 0.20f, -h * 0.10f),
            size = Size(w * 1.5f, h * 0.16f),
        )
    }
    val glintRadius = radius.coerceAtLeast(10f) * 2.4f
    val glintInset = radius * 0.30f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.46f), Color.Transparent),
            radius = glintRadius,
        ),
        radius = glintRadius,
        center = Offset(glintInset, glintInset),
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.46f), Color.Transparent),
            radius = glintRadius,
        ),
        radius = glintRadius,
        center = Offset(w - glintInset, glintInset),
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawAccentStripe(
    thicknessPx: Float,
    color: Color,
    radius: Float,
    position: StripePosition,
) {
    val w = size.width
    val h = size.height
    val rect = when (position) {
        StripePosition.Top -> androidx.compose.ui.geometry.Rect(0f, 0f, w, thicknessPx)
        StripePosition.Bottom -> androidx.compose.ui.geometry.Rect(0f, h - thicknessPx, w, h)
        StripePosition.Left -> androidx.compose.ui.geometry.Rect(0f, 0f, thicknessPx, h)
        StripePosition.Right -> androidx.compose.ui.geometry.Rect(w - thicknessPx, 0f, w, h)
    }
    drawRoundRect(color = color, topLeft = rect.topLeft, size = Size(rect.width, rect.height), cornerRadius = CornerRadius(radius))
}