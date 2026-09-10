package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val FrostwaveColorScheme = darkColorScheme(
  primary = NeonCyan,
  onPrimary = AbyssMidnight,
  primaryContainer = IceSurfaceElevated,
  onPrimaryContainer = IceWhite,
  secondary = FrostMint,
  onSecondary = AbyssMidnight,
  tertiary = ArcadeYellow,
  onTertiary = AbyssMidnight,
  background = ArcticDeep,
  onBackground = IceWhite,
  surface = IceSurface,
  onSurface = IceWhite,
  surfaceVariant = IceSurfaceElevated,
  onSurfaceVariant = TextMuted,
  outline = IceBorder,
  outlineVariant = IceBorder,
  error = DangerEmber,
  onError = Color.White
)

@Composable
fun FrostwaveTheme(
  content: @Composable () -> Unit
) {
  val view = LocalView.current

  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      window?.let {
        it.statusBarColor = ArcticDeep.toArgb()
        it.navigationBarColor = AbyssMidnight.toArgb()
        val insetsController = WindowCompat.getInsetsController(it, view)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false
      }
    }
  }

  MaterialTheme(
    colorScheme = FrostwaveColorScheme,
    typography = Typography,
    content = content
  )
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  content: @Composable () -> Unit
) {
  FrostwaveTheme(content = content)
}
