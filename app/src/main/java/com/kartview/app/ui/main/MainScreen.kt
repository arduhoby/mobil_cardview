package com.kartview.app.ui.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.kartview.app.data.db.GlassCardDatabase
import com.kartview.app.data.model.DataType
import com.kartview.app.data.model.DemoItem
import com.kartview.app.data.model.GlassSettings
import com.kartview.app.ui.settings.SettingsScreen
import com.kartview.app.ui.settings.SettingsViewModel
import com.kartview.glasscard.GlassCard
import com.kartview.glasscard.rememberGlassBackdrop
import com.kartview.rolodex.Rolodex

@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel(),
) {
    val items = viewModel.items
    val darkTheme = isSystemInDarkTheme()

    val context = LocalContext.current
    val dao = remember(context) { GlassCardDatabase.get(context).presetDao() }
    val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(dao))
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    var showSettings by rememberSaveable { mutableStateOf(false) }

    if (showSettings) {
        SettingsScreen(
            viewModel = settingsViewModel,
            onClose = { showSettings = false },
        )
        return
    }

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val widthPx = with(density) { configuration.screenWidthDp.dp.toPx() }.toInt().coerceAtLeast(1)
    val heightPx = with(density) { configuration.screenHeightDp.dp.toPx() }.toInt().coerceAtLeast(1)
    val size = remember(widthPx, heightPx) { Size(widthPx.toFloat(), heightPx.toFloat()) }
    val wallpaper = remember(size, darkTheme) { Wallpaper(size, darkTheme) }
    val currentWallpaper by rememberUpdatedState(wallpaper)
    val backdrop = rememberGlassBackdrop(size) { canvas -> currentWallpaper.drawInto(canvas) }

    val onSurface = if (darkTheme) Color(0xFFE6E9F0) else Color(0xFF11131A)

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier.fillMaxSize(),
            onDraw = {
                drawIntoCanvas { canvas ->
                    wallpaper.drawInto(canvas)
                }
            }
        )
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Glass CardView",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = onSurface,
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IndicatorDot(stripeColorOf(DataType.Photo))
                        IndicatorDot(stripeColorOf(DataType.Note))
                        IndicatorDot(stripeColorOf(DataType.Location))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "sol \u015ferit = veri tipi",
                            style = MaterialTheme.typography.bodySmall,
                            color = onSurface.copy(alpha = 0.7f),
                        )
                    }
                }
                TextButton(onClick = { showSettings = true }) {
                    Text("Ayarlar", color = onSurface)
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                if (items.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "T\u00fcm kartlar kald\u0131r\u0131ld\u0131.",
                            style = MaterialTheme.typography.titleMedium,
                            color = onSurface.copy(alpha = 0.75f),
                        )
                    }
                }
                Rolodex(
                    items = items,
                    key = { it.id },
                    cardHeight = settings.contentHeight.dp,
                    onDismiss = { viewModel.dismiss(it.id) },
                ) { item, focused ->
                    DemoCard(
                        item = item,
                        backdrop = if (focused) backdrop else null,
                        settings = settings,
                        darkTheme = darkTheme,
                    )
                }
            }
        }
    }
}

@Composable
private fun IndicatorDot(color: Color) {
    Spacer(
        modifier = Modifier
            .padding(horizontal = 3.dp)
            .size(8.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun DemoCard(
    item: DemoItem,
    backdrop: com.kartview.glasscard.GlassBackdrop?,
    settings: GlassSettings,
    darkTheme: Boolean,
) {
    val stripeColor = stripeColorOf(item.type)
    val hasPhoto = item.imageUrl != null
    val textColor = if (darkTheme) Color(0xFFF2F4F8) else Color(0xFF11131A)

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = settings.cornerRadius.dp,
        backgroundOpacity = settings.backgroundOpacity,
        blurIntensity = settings.blurIntensity,
        gradientEnabled = settings.gradientEnabled,
        showBorder = settings.showBorder,
        stripeEnabled = settings.stripeEnabled,
        stripePosition = settings.stripePosition,
        stripeThickness = settings.stripeThickness.dp,
        stripeColor = stripeColor,
        backdrop = backdrop,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(settings.contentHeight.dp)
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 14.dp)
        ) {
            if (hasPhoto) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(typeTint(item.type)),
                    contentScale = ContentScale.Crop,
                )
                Spacer(Modifier.height(12.dp))
            } else {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(stripeColor),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = item.type.name.first().toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = item.type.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = stripeColor,
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .drawBehind {
                            val scrimBase = if (darkTheme) Color.Black else Color.White
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        scrimBase.copy(alpha = if (hasPhoto) 0.34f else 0.14f),
                                    ),
                                ),
                            )
                        },
                ) {}
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.72f),
                    )
                }
            }
        }
    }
}

@Composable
private fun stripeColorOf(type: DataType): Color = when (type) {
    DataType.Photo -> Color(0xFF3B82F6)
    DataType.Note -> Color(0xFFFF9800)
    DataType.Location -> Color(0xFF4CAF50)
}

@Composable
private fun typeTint(type: DataType): Color = when (type) {
    DataType.Photo -> Color(0x403B82F6)
    DataType.Note -> Color(0x40FF9800)
    DataType.Location -> Color(0x404CAF50)
}

private class Wallpaper(private val size: Size, dark: Boolean) {
    private class Blob(val center: Offset, val radius: Float, private val argb: Long) {
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            this.color = argb.toInt()
        }
    }

    private val blobs: List<Blob>
    private val gradientColors: IntArray

    init {
        if (dark) {
            gradientColors = intArrayOf(
                0xFF1E1B4B.toInt(), 0xFF5B21B6.toInt(),
                0xFF0E7490.toInt(), 0xFF134E4A.toInt(),
            )
            blobs = listOf(
                Blob(Offset(size.width * 0.82f, size.height * 0.14f), size.width * 0.32f, 0xCCFF8A5C),
                Blob(Offset(size.width * 0.08f, size.height * 0.92f), size.width * 0.42f, 0x804ADE80),
                Blob(Offset(size.width * 0.88f, size.height * 0.55f), size.width * 0.28f, 0x8038BDF8),
            )
        } else {
            gradientColors = intArrayOf(
                0xFFE3EFFF.toInt(), 0xFFE9E2FF.toInt(),
                0xFFCFF3FC.toInt(), 0xFFD8F2E7.toInt(),
            )
            blobs = listOf(
                Blob(Offset(size.width * 0.82f, size.height * 0.14f), size.width * 0.32f, 0xB3FF8FA3),
                Blob(Offset(size.width * 0.08f, size.height * 0.92f), size.width * 0.42f, 0x80A7E8CE),
                Blob(Offset(size.width * 0.88f, size.height * 0.55f), size.width * 0.28f, 0x807BB6F2),
            )
        }
    }

    fun drawInto(canvas: Canvas) {
        val native = canvas.nativeCanvas
        native.drawRect(
            0f, 0f, size.width, size.height,
            android.graphics.Paint().apply {
                shader = android.graphics.LinearGradient(
                    0f, 0f, size.width, size.height,
                    gradientColors,
                    null,
                    android.graphics.Shader.TileMode.CLAMP,
                )
            }
        )
        for (blob in blobs) {
            native.drawCircle(blob.center.x, blob.center.y, blob.radius, blob.paint)
        }
    }
}