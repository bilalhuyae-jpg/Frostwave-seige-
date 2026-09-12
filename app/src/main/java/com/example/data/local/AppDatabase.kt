package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AchievementEntity
import com.example.data.model.GameSettingsEntity
import com.example.data.model.HighScoreEntity

@Database(
  entities = [
    HighScoreEntity::class,
    GameSettingsEntity::class,
    AchievementEntity::class
  ],
  version = 3,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun highScoreDao(): HighScoreDao
  abstract fun gameSettingsDao(): GameSettingsDao
  abstract fun achievementDao(): AchievementDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "frostwave_siege_db"
        ).fallbackToDestructiveMigration().build()
        INSTANCE = instance
        instance
      }
    }
  }
}
