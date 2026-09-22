package com.kartview.app.ui.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kartview.glasscard.GlassCard
import com.kartview.glasscard.StripePosition
import com.kartview.glasscard.rememberGlassBackdrop

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onClose: () -> Unit,
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val presets by viewModel.presets.collectAsStateWithLifecycle()
    var presetName by rememberSaveable { mutableStateOf("") }

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val widthPx = with(density) { configuration.screenWidthDp.dp.toPx() }.toInt().coerceAtLeast(1)
    val heightPx = with(density) { configuration.screenHeightDp.dp.toPx() }.toInt().coerceAtLeast(1)
    val size = remember(widthPx, heightPx) {
        androidx.compose.ui.geometry.Size(widthPx.toFloat(), heightPx.toFloat())
    }
    val wallpaper = remember(size) { SettingsWallpaper(size) }
    val backdrop = rememberGlassBackdrop(size) { canvas -> wallpaper.drawInto(canvas) }

    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier.fillMaxSize(),
            onDraw = {
                drawIntoCanvas { canvas -> wallpaper.drawInto(canvas) }
            },
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Ayarlar",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                TextButton(onClick = onClose) {
                    Text("Geri", color = Color.White)
                }
            }

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(settings.contentHeight.dp),
                cornerRadius = settings.cornerRadius.dp,
                backgroundOpacity = settings.backgroundOpacity,
                blurIntensity = settings.blurIntensity,
                gradientEnabled = settings.gradientEnabled,
                showBorder = settings.showBorder,
                stripeEnabled = settings.stripeEnabled,
                stripePosition = settings.stripePosition,
                stripeThickness = settings.stripeThickness.dp,
                stripeColor = Color(0xFF3B82F6),
                backdrop = backdrop,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Canlı önizleme",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF11131A),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Değerleri değiştir, hemen yansısın.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF11131A).copy(alpha = 0.7f),
                    )
                }
            }

            SectionTitle("Kart")
            LabeledSlider(
                label = "Köşe yarıçapı",
                value = settings.cornerRadius,
                range = 0f..40f,
                valueText = "${settings.cornerRadius.toInt()} dp",
            ) { viewModel.update { s -> s.copy(cornerRadius = it) } }
            LabeledSlider(
                label = "Yüzey opaklığı",
                value = settings.backgroundOpacity,
                range = 0.3f..1f,
                valueText = "%.2f".format(settings.backgroundOpacity),
            ) { viewModel.update { s -> s.copy(backgroundOpacity = it) } }
            LabeledSlider(
                label = "Bulanıklık",
                value = settings.blurIntensity,
                range = 0f..40f,
                valueText = "${settings.blurIntensity.toInt()} dp",
            ) { viewModel.update { s -> s.copy(blurIntensity = it) } }
            LabeledSlider(
                label = "Kart yüksekliği",
                value = settings.contentHeight,
                range = 200f..420f,
                valueText = "${settings.contentHeight.toInt()} dp",
            ) { viewModel.update { s -> s.copy(contentHeight = it) } }
            ToggleRow(
                label = "Gradyan yüzey",
                checked = settings.gradientEnabled,
            ) { viewModel.update { s -> s.copy(gradientEnabled = it) } }
            ToggleRow(
                label = "Kenarlık",
                checked = settings.showBorder,
            ) { viewModel.update { s -> s.copy(showBorder = it) } }

            SectionTitle("Şerit")
            ToggleRow(
                label = "Şerit açık",
                checked = settings.stripeEnabled,
            ) { viewModel.update { s -> s.copy(stripeEnabled = it) } }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StripePosition.entries.forEach { position ->
                    FilterChip(
                        selected = settings.stripePosition == position,
                        onClick = { viewModel.update { s -> s.copy(stripePosition = position) } },
                        label = { Text(stripeLabel(position)) },
                    )
                }
            }
            LabeledSlider(
                label = "Şerit kalınlığı",
                value = settings.stripeThickness,
                range = 1f..8f,
                valueText = "%.1f dp".format(settings.stripeThickness),
            ) { viewModel.update { s -> s.copy(stripeThickness = it) } }

            SectionTitle("Preset")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = presetName,
                    onValueChange = { presetName = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("Preset adı") },
                    shape = RoundedCornerShape(12.dp),
                )
                OutlinedButton(
                    onClick = {
                        viewModel.savePreset(presetName)
                        presetName = ""
                    },
                    enabled = presetName.isNotBlank(),
                ) {
                    Text("Kaydet")
                }
            }

            if (presets.isEmpty()) {
                Text(
                    text = "Kayıtlı preset yok.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f),
                )
            } else {
                presets.forEach { preset ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = preset.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                            )
                            Text(
                                text = "r=${preset.cornerRadius.toInt()}dp · a=${"%.2f".format(preset.backgroundOpacity)} · blur=${preset.blurIntensity.toInt()}dp" +
                                    (if (preset.isActive) " · aktif" else ""),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f),
                            )
                        }
                        TextButton(onClick = { viewModel.applyPreset(preset) }) {
                            Text("Uygula", color = Color(0xFF90CAF9))
                        }
                        TextButton(onClick = { viewModel.deletePreset(preset) }) {
                            Text("Sil", color = Color(0xFFEF9A9A))
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = Color.White.copy(alpha = 0.85f),
    )
}

@Composable
private fun LabeledSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    valueText: String,
    onValueChange: (Float) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.White)
            Text(valueText, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
        }
        Slider(value = value, onValueChange = onValueChange, valueRange = range)
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.White)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun stripeLabel(position: StripePosition): String = when (position) {
    StripePosition.Top -> "Üst"
    StripePosition.Bottom -> "Alt"
    StripePosition.Left -> "Sol"
    StripePosition.Right -> "Sağ"
}

private class SettingsWallpaper(private val size: androidx.compose.ui.geometry.Size) {
    fun drawInto(canvas: androidx.compose.ui.graphics.Canvas) {
        val native = canvas.nativeCanvas
        native.drawRect(
            0f, 0f, size.width, size.height,
            android.graphics.Paint().apply {
                shader = android.graphics.LinearGradient(
                    0f, 0f, size.width, size.height,
                    intArrayOf(0xFF1E1B4B.toInt(), 0xFF5B21B6.toInt(), 0xFF0E7490.toInt(), 0xFF134E4A.toInt()),
                    null,
                    android.graphics.Shader.TileMode.CLAMP,
                )
            },
        )
        val blobs = listOf(
            Triple(size.width * 0.82f, size.height * 0.14f, 0xCCFF8A5C.toInt()),
            Triple(size.width * 0.08f, size.height * 0.92f, 0x804ADE80.toInt()),
            Triple(size.width * 0.88f, size.height * 0.55f, 0x8038BDF8.toInt()),
        )
        for ((cx, cy, color) in blobs) {
            native.drawCircle(
                cx, cy, size.width * 0.32f,
                android.graphics.Paint().apply {
                    isAntiAlias = true
                    this.color = color
                },
            )
        }
    }
}
