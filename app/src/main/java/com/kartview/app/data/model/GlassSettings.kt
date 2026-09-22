package com.kartview.app.data.model

import com.kartview.app.data.db.GlassPreset
import com.kartview.glasscard.StripePosition

data class GlassSettings(
    val cornerRadius: Float = 20f,
    val backgroundOpacity: Float = 0.52f,
    val blurIntensity: Float = 24f,
    val contentHeight: Float = 280f,
    val stripeEnabled: Boolean = true,
    val stripePosition: StripePosition = StripePosition.Left,
    val stripeThickness: Float = 3f,
    val gradientEnabled: Boolean = true,
    val showBorder: Boolean = true,
)

fun GlassPreset.toSettings(): GlassSettings = GlassSettings(
    cornerRadius = cornerRadius,
    backgroundOpacity = backgroundOpacity,
    blurIntensity = blurIntensity,
    contentHeight = contentHeight,
    stripeEnabled = stripeEnabled,
    stripePosition = runCatching { StripePosition.valueOf(stripePosition) }
        .getOrDefault(StripePosition.Left),
    stripeThickness = stripeThickness,
    gradientEnabled = gradientEnabled,
    showBorder = showBorder,
)

fun GlassSettings.toPreset(name: String): GlassPreset = GlassPreset(
    name = name,
    cornerRadius = cornerRadius,
    backgroundOpacity = backgroundOpacity,
    blurIntensity = blurIntensity,
    contentHeight = contentHeight,
    stripeEnabled = stripeEnabled,
    stripePosition = stripePosition.name,
    stripeThickness = stripeThickness,
    gradientEnabled = gradientEnabled,
    showBorder = showBorder,
    isActive = true,
)
