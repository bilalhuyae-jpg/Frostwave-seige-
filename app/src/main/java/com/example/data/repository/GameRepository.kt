package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.AchievementEntity
import com.example.data.model.GameSettingsEntity
import com.example.data.model.HighScoreEntity
import kotlinx.coroutines.flow.Flow

class GameRepository(private val database: AppDatabase) {
  val topHighScores: Flow<List<HighScoreEntity>> = database.highScoreDao().getTopHighScores()
  val highestScore: Flow<Int?> = database.highScoreDao().getHighestScore()
  val settings: Flow<GameSettingsEntity?> = database.gameSettingsDao().getSettings()
  val achievements: Flow<List<AchievementEntity>> = database.achievementDao().getAllAchievements()

  suspend fun saveScore(playerName: String, score: Int, level: Int, lettersCount: Int): Long {
    return database.highScoreDao().insertScore(
      HighScoreEntity(
        playerName = playerName,
        score = score,
        level = level,
        bonusLettersCompleted = lettersCount
      )
    )
  }

  suspend fun updateSettings(settings: GameSettingsEntity) {
    database.gameSettingsDao().insertOrUpdate(settings)
  }

  suspend fun initializeDefaultsIfEmpty() {
    val defaultSettings = GameSettingsEntity()
    database.gameSettingsDao().insertOrUpdate(defaultSettings)

    val defaultAchievements = listOf(
      AchievementEntity(
        id = "first_freeze",
        title = "Frost Initiate",
        description = "Encase an enemy in a solid snow orb.",
        isUnlocked = false,
        maxProgress = 1
      ),
      AchievementEntity(
        id = "chain_roll",
        title = "Avalanche Strike",
        description = "Eliminate 3 or more enemies with a single rolling snow orb.",
        isUnlocked = false,
        maxProgress = 3
      ),
      AchievementEntity(
        id = "word_frost",
        title = "Word Master",
        description = "Spell the complete word F-R-O-S-T in a single run.",
        isUnlocked = false,
        maxProgress = 5
      ),
      AchievementEntity(
        id = "boss_slayer",
        title = "Glacier Conqueror",
        description = "Defeat the mighty Cryo-Titan boss.",
        isUnlocked = false,
        maxProgress = 1
      ),
      AchievementEntity(
        id = "giant_rampage",
        title = "Sub-Zero Juggernaut",
        description = "Collect the Giant Mode power-up and crush enemies.",
        isUnlocked = false,
        maxProgress = 1
      )
    )
    database.achievementDao().insertDefaultAchievements(defaultAchievements)

    // Seed default arcade top scores if none exist
    database.highScoreDao().insertScore(HighScoreEntity(playerName = "GLINT_P1", score = 25000, level = 4, bonusLettersCompleted = 5))
    database.highScoreDao().insertScore(HighScoreEntity(playerName = "EMBER_P2", score = 18500, level = 3, bonusLettersCompleted = 3))
    database.highScoreDao().insertScore(HighScoreEntity(playerName = "FROST_ACE", score = 12000, level = 2, bonusLettersCompleted = 2))
  }

  suspend fun unlockAchievement(achievementId: String) {
    database.achievementDao().updateAchievement(
      AchievementEntity(
        id = achievementId,
        title = "",
        description = "",
        isUnlocked = true,
        progress = 1,
        maxProgress = 1
      )
    )
  }
}
