package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AchievementEntity
import com.example.data.model.GameSettingsEntity
import com.example.data.model.HighScoreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HighScoreDao {
  @Query("SELECT * FROM high_scores ORDER BY score DESC LIMIT 10")
  fun getTopHighScores(): Flow<List<HighScoreEntity>>

  @Query("SELECT MAX(score) FROM high_scores")
  fun getHighestScore(): Flow<Int?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertScore(score: HighScoreEntity): Long
}

@Dao
interface GameSettingsDao {
  @Query("SELECT * FROM game_settings WHERE id = 1 LIMIT 1")
  fun getSettings(): Flow<GameSettingsEntity?>

  @Query("SELECT * FROM game_settings WHERE id = 1 LIMIT 1")
  fun getSettingsDirect(): GameSettingsEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertOrUpdate(settings: GameSettingsEntity): Long
}

@Dao
interface AchievementDao {
  @Query("SELECT * FROM achievements")
  fun getAllAchievements(): Flow<List<AchievementEntity>>

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  fun insertDefaultAchievements(achievements: List<AchievementEntity>): List<Long>

  @Update
  fun updateAchievement(achievement: AchievementEntity): Int
}
