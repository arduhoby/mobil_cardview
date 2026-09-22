package com.kartview.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [GlassPreset::class], version = 2, exportSchema = false)
abstract class GlassCardDatabase : RoomDatabase() {

    abstract fun presetDao(): PresetDao

    companion object {
        @Volatile
        private var instance: GlassCardDatabase? = null

        fun get(context: Context): GlassCardDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    GlassCardDatabase::class.java,
                    "kartview.db",
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}
