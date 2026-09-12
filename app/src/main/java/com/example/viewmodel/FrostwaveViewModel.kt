package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.GameAudio
import com.example.data.local.AppDatabase
import com.example.data.model.AchievementEntity
import com.example.data.model.GameSettingsEntity
import com.example.data.model.HighScoreEntity
import com.example.data.repository.GameRepository
import com.example.game.GameEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenDestination {
  SPLASH,
  MAIN_MENU,
  LEVEL_SELECT,
  GAMEPLAY,
  LEADERBOARD,
  SETTINGS,
  HOW_TO_PLAY
}

enum class GameplayOverlay {
  NONE,
  PAUSED,
  LEVEL_CLEAR,
  GAME_OVER
}

class FrostwaveViewModel(application: Application) : AndroidViewModel(application) {
  private val repository: GameRepository

  private val _currentScreen = MutableStateFlow(ScreenDestination.SPLASH)
  val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

  private val _overlayState = MutableStateFlow(GameplayOverlay.NONE)
  val overlayState: StateFlow<GameplayOverlay> = _overlayState.asStateFlow()

  val highScores: StateFlow<List<HighScoreEntity>>
  val highestScore: StateFlow<Int?>
  val settings: StateFlow<GameSettingsEntity?>
  val achievements: StateFlow<List<AchievementEntity>>

  lateinit var gameEngine: GameEngine
    private set

  private val _continueCountdown = MutableStateFlow(9)
  val continueCountdown: StateFlow<Int> = _continueCountdown.asStateFlow()

  private val _hasActiveGame = MutableStateFlow(false)
  val hasActiveGame: StateFlow<Boolean> = _hasActiveGame.asStateFlow()

  init {
    val db = AppDatabase.getDatabase(application)
    repository = GameRepository(db)

    highScores = repository.topHighScores.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      emptyList()
    )

    highestScore = repository.highestScore.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      25000
    )

    settings = repository.settings.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      GameSettingsEntity()
    )

    achievements = repository.achievements.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      emptyList()
    )

    viewModelScope.launch {
      repository.initializeDefaultsIfEmpty()
    }

    GameAudio.init(application)
    initGameEngine()
  }

  private fun initGameEngine() {
    val currentSet = settings.value ?: GameSettingsEntity()
    gameEngine = GameEngine(
      isTwoPlayer = currentSet.isTwoPlayer,
      powerUpStart = currentSet.powerUpStart,
      onLevelCleared = {
        _overlayState.value = GameplayOverlay.LEVEL_CLEAR
        GameAudio.playSound(GameAudio.SoundEffect.LEVEL_CLEAR)
        viewModelScope.launch {
          val completedLevel = gameEngine.currentLevel.levelNumber
          repository.unlockNextLevel(completedLevel)
        }
      },
      onGameOver = {
        _overlayState.value = GameplayOverlay.GAME_OVER
        _continueCountdown.value = 9
        saveCurrentRunScore()
      }
    )
    GameAudio.soundVolume = currentSet.soundVolume
    GameAudio.musicVolume = currentSet.musicVolume
  }

  fun navigateTo(screen: ScreenDestination) {
    _currentScreen.value = screen
    if (screen == ScreenDestination.GAMEPLAY) {
      GameAudio.startBgm()
    } else {
      GameAudio.stopBgm()
    }
  }

  fun startNewGame(twoPlayer: Boolean = false, powerUpStart: Boolean = false, startLevel: Int = 0) {
    updateSettings(
      (settings.value ?: GameSettingsEntity()).copy(
        isTwoPlayer = twoPlayer,
        powerUpStart = powerUpStart
      )
    )
    gameEngine.isTwoPlayer = twoPlayer
    gameEngine.powerUpStart = powerUpStart
    gameEngine.startNewGame(startLevel)
    _hasActiveGame.value = true
    _overlayState.value = GameplayOverlay.NONE
    navigateTo(ScreenDestination.GAMEPLAY)
  }

  fun resumeGame() {
    gameEngine.isPaused = false
    _overlayState.value = GameplayOverlay.NONE
    GameAudio.startBgm()
  }

  fun pauseGame() {
    gameEngine.isPaused = true
    _overlayState.value = GameplayOverlay.PAUSED
    GameAudio.stopBgm()
  }

  fun restartLevel() {
    gameEngine.retryLevel()
    _overlayState.value = GameplayOverlay.NONE
    gameEngine.isPaused = false
    GameAudio.startBgm()
  }

  fun advanceToNextLevel() {
    gameEngine.loadLevel(gameEngine.currentLevelIdx + 1)
    _overlayState.value = GameplayOverlay.NONE
  }

  fun continueGame() {
    // Continue gives 3 lives back and keeps score!
    for (p in gameEngine.players) {
      p.lives = 3
      p.isDead = false
      p.invincibleTimer = 3.0f
    }
    _overlayState.value = GameplayOverlay.NONE
    GameAudio.startBgm()
  }

  fun quitToMainMenu() {
    saveCurrentRunScore()
    gameEngine.isPaused = false
    _overlayState.value = GameplayOverlay.NONE
    _hasActiveGame.value = false
    navigateTo(ScreenDestination.MAIN_MENU)
  }

  fun updateSettings(newSettings: GameSettingsEntity) {
    viewModelScope.launch {
      repository.updateSettings(newSettings)
      GameAudio.soundVolume = newSettings.soundVolume
      GameAudio.musicVolume = newSettings.musicVolume
    }
  }

  fun saveCurrentRunScore() {
    val p1 = gameEngine.players.firstOrNull() ?: return
    if (p1.score > 0) {
      viewModelScope.launch {
        repository.saveScore(
          playerName = "HERO_P1",
          score = p1.score,
          level = gameEngine.currentLevelIdx + 1,
          lettersCount = gameEngine.collectedLetters.size
        )
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
    GameAudio.release()
  }
}
