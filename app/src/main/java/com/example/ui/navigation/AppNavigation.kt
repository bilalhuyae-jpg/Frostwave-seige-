package com.example.ui.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HowToPlayScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.LevelSelectScreen
import com.example.ui.screens.MainMenuScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.viewmodel.FrostwaveViewModel
import com.example.viewmodel.ScreenDestination

@Composable
fun AppNavigation(
  viewModel: FrostwaveViewModel
) {
  val currentScreen by viewModel.currentScreen.collectAsState()

  Crossfade(
    targetState = currentScreen,
    animationSpec = tween(280),
    label = "ScreenTransition"
  ) { screen ->
    when (screen) {
      ScreenDestination.SPLASH -> {
        SplashScreen(
          onStartClicked = {
            viewModel.navigateTo(ScreenDestination.MAIN_MENU)
          }
        )
      }

      ScreenDestination.MAIN_MENU -> {
        MainMenuScreen(
          viewModel = viewModel,
          onPlay1P = { powerUpStart ->
            viewModel.startNewGame(twoPlayer = false, powerUpStart = powerUpStart)
          },
          onPlay2P = { powerUpStart ->
            viewModel.startNewGame(twoPlayer = true, powerUpStart = powerUpStart)
          },
          onLevelSelect = {
            viewModel.navigateTo(ScreenDestination.LEVEL_SELECT)
          },
          onLeaderboard = {
            viewModel.navigateTo(ScreenDestination.LEADERBOARD)
          },
          onSettings = {
            viewModel.navigateTo(ScreenDestination.SETTINGS)
          },
          onHowToPlay = {
            viewModel.navigateTo(ScreenDestination.HOW_TO_PLAY)
          }
        )
      }

      ScreenDestination.LEVEL_SELECT -> {
        LevelSelectScreen(
          viewModel = viewModel,
          onSelectLevel = { levelIndex, isTwoPlayer ->
            viewModel.startNewGame(twoPlayer = isTwoPlayer, powerUpStart = false, startLevel = levelIndex)
          },
          onBack = {
            viewModel.navigateTo(ScreenDestination.MAIN_MENU)
          }
        )
      }

      ScreenDestination.GAMEPLAY -> {
        GameScreen(viewModel = viewModel)
      }

      ScreenDestination.LEADERBOARD -> {
        LeaderboardScreen(
          viewModel = viewModel,
          onBack = {
            viewModel.navigateTo(ScreenDestination.MAIN_MENU)
          }
        )
      }

      ScreenDestination.SETTINGS -> {
        SettingsScreen(
          viewModel = viewModel,
          onBack = {
            viewModel.navigateTo(ScreenDestination.MAIN_MENU)
          }
        )
      }

      ScreenDestination.HOW_TO_PLAY -> {
        HowToPlayScreen(
          onBack = {
            viewModel.navigateTo(ScreenDestination.MAIN_MENU)
          }
        )
      }
    }
  }
}
