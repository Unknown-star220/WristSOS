package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        DailyStatsEntity::class,
        AlarmEntity::class,
        BadgeEntity::class,
        AppFilterEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class WatchDatabase : RoomDatabase() {
    abstract fun watchDao(): WatchDao

    companion object {
        @Volatile
        private var INSTANCE: WatchDatabase? = null

        fun getDatabase(context: Context): WatchDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WatchDatabase::class.java,
                    "cyberwatch_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
