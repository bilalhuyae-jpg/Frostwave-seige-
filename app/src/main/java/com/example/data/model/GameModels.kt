package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "high_scores")
data class HighScoreEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val playerName: String,
  val score: Int,
  val level: Int,
  val bonusLettersCompleted: Int,
  val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "game_settings")
data class GameSettingsEntity(
  @PrimaryKey
  val id: Int = 1,
  val isTwoPlayer: Boolean = false,
  val isLeftHanded: Boolean = false,
  val buttonScale: Float = 1.0f,
  val soundVolume: Float = 0.8f,
  val musicVolume: Float = 0.6f,
  val scanlinesEnabled: Boolean = true,
  val powerUpStart: Boolean = false,
  val screenFitAspect: Boolean = true, // true = 4:3 arcade boxed, false = stretch/full
  val language: String = "English"
)

@Entity(tableName = "achievements")
data class AchievementEntity(
  @PrimaryKey
  val id: String,
  val title: String,
  val description: String,
  val isUnlocked: Boolean = false,
  val progress: Int = 0,
  val maxProgress: Int = 1
)
