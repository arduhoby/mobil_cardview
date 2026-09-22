package com.kartview.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "glass_presets")
data class GlassPreset(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val cornerRadius: Float,
    val backgroundOpacity: Float,
    val blurIntensity: Float,
    val contentHeight: Float,
    val stripeEnabled: Boolean,
    val stripePosition: String,
    val stripeThickness: Float,
    val gradientEnabled: Boolean,
    val showBorder: Boolean,
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)
