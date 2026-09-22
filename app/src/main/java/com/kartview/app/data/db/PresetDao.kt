package com.kartview.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PresetDao {

    @Query("SELECT * FROM glass_presets ORDER BY createdAt DESC")
    fun observePresets(): Flow<List<GlassPreset>>

    @Query("SELECT * FROM glass_presets WHERE isActive = 1 LIMIT 1")
    suspend fun getActive(): GlassPreset?

    @Query("UPDATE glass_presets SET isActive = 0")
    suspend fun clearActive()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(preset: GlassPreset): Long

    @Delete
    suspend fun delete(preset: GlassPreset)

    @Transaction
    suspend fun activate(preset: GlassPreset) {
        clearActive()
        upsert(preset.copy(isActive = true))
    }

    @Transaction
    suspend fun saveAndActivate(preset: GlassPreset): Long {
        clearActive()
        return upsert(preset.copy(isActive = true))
    }
}
