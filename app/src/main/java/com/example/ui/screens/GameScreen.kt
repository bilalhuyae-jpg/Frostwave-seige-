package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.GameAudio
import com.example.game.GameRenderer
import com.example.game.LevelCatalog
import com.example.ui.theme.AbyssMidnight
import com.example.ui.theme.ArcadeYellow
import com.example.ui.theme.ArcticDeep
import com.example.ui.theme.CoralBlaze
import com.example.ui.theme.DangerEmber
import com.example.ui.theme.FrostMint
import com.example.ui.theme.FrostTeal
import com.example.ui.theme.IceBorder
import com.example.ui.theme.IceSurface
import com.example.ui.theme.IceSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PowerUpBlast
import com.example.viewmodel.FrostwaveViewModel
import com.example.viewmodel.GameplayOverlay

@Composable
fun GameScreen(
  viewModel: FrostwaveViewModel
) {
  val engine = viewModel.gameEngine
  val overlayState by viewModel.overlayState.collectAsState()
  val settings by viewModel.settings.collectAsState()
  val highestScore by viewModel.highestScore.collectAsState()

  val scanlinesEnabled = settings?.scanlinesEnabled ?: true
  val buttonScale = settings?.buttonScale ?: 1.0f

  val textMeasurer = rememberTextMeasurer()
  var frameTicker by remember { mutableFloatStateOf(0f) }

  // BGM Lifecycle
  DisposableEffect(Unit) {
    GameAudio.startBgm()
    onDispose {
      GameAudio.pauseBgm()
    }
  }

  // 60fps Game Loop
  LaunchedEffect(overlayState) {
    if (overlayState == GameplayOverlay.NONE) {
      GameAudio.resumeBgm()
    } else {
      GameAudio.pauseBgm()
    }
    var lastFrameTime = 0L
    while (overlayState == GameplayOverlay.NONE) {
      withFrameNanos { time ->
        if (lastFrameTime != 0L) {
          val dt = ((time - lastFrameTime) / 1_000_000_000f).coerceIn(0.005f, 0.05f)
          engine.update(dt)
          frameTicker += dt
        }
        lastFrameTime = time
      }
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(AbyssMidnight)
      .statusBarsPadding()
      .navigationBarsPadding()
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // 1. Persistent Top Arcade HUD
      TopArcadeHud(
        engine = engine,
        highScore = highestScore ?: 25000,
        onPauseClicked = { viewModel.pauseGame() }
      )

      // 2. Center Stage Canvas (Fixed Aspect Ratio 2:3 - Perfectly fitted to playing board)
      BoxWithConstraints(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
      ) {
        val targetRatio = LevelCatalog.GAME_WIDTH / LevelCatalog.GAME_HEIGHT // 400f / 600f
        val boxRatio = maxWidth / maxHeight
        val canvasModifier = if (boxRatio > targetRatio) {
          Modifier
            .fillMaxHeight()
            .aspectRatio(targetRatio)
        } else {
          Modifier
            .fillMaxWidth()
            .aspectRatio(targetRatio)
        }

        Canvas(
          modifier = canvasModifier
            .clip(RoundedCornerShape(10.dp))
            .border(2.dp, ArcadeYellow.copy(alpha = 0.85f), RoundedCornerShape(10.dp))
            .background(Color(0xFF040812))
        ) {
          // Read frameTicker to guarantee Compose recomposition on each frame
          @Suppress("UNUSED_VARIABLE")
          val tick = frameTicker
          GameRenderer.renderGame(
            drawScope = this,
            engine = engine,
            scanlinesEnabled = scanlinesEnabled,
            textMeasurer = textMeasurer
          )
        }
      }

      // 3. Virtual Arcade Touch Controls
      ArcadeTouchControls(
        engine = engine,
        buttonScale = buttonScale,
        isLeftHanded = settings?.isLeftHanded ?: false,
        isTwoPlayer = engine.isTwoPlayer
      )
    }

    // Modal Overlays
    when (overlayState) {
      GameplayOverlay.PAUSED -> {
        PauseOverlay(
          onResume = { viewModel.resumeGame() },
          onRestart = { viewModel.restartLevel() },
          onQuit = { viewModel.quitToMainMenu() }
        )
      }
      GameplayOverlay.LEVEL_CLEAR -> {
        LevelClearScreen(
          levelNumber = engine.currentLevelIdx + 1,
          levelName = engine.currentLevel.name,
          currentScore = engine.players.firstOrNull()?.score ?: 0,
          collectedLetters = engine.collectedLetters,
          onNextLevel = { viewModel.advanceToNextLevel() }
        )
      }
      GameplayOverlay.GAME_OVER -> {
        GameOverScreen(
          finalScore = engine.players.firstOrNull()?.score ?: 0,
          highScore = highestScore ?: 25000,
          levelReached = engine.currentLevelIdx + 1,
          onContinue = { viewModel.continueGame() },
          onRetry = { viewModel.startNewGame(engine.isTwoPlayer, engine.powerUpStart) },
          onQuit = { viewModel.quitToMainMenu() }
        )
      }
      GameplayOverlay.NONE -> {}
    }
  }
}

@Composable
fun TopArcadeHud(
  engine: com.example.game.GameEngine,
  highScore: Int,
  onPauseClicked: () -> Unit
) {
  val p1 = engine.players.firstOrNull()
  val p2 = if (engine.isTwoPlayer && engine.players.size > 1) engine.players[1] else null
  val p1Score = p1?.score ?: 0
  val p1Lives = p1?.lives ?: 3
  val p2Score = p2?.score ?: 0
  val p2Lives = p2?.lives ?: 3

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 10.dp, vertical = 4.dp),
    colors = CardDefaults.cardColors(containerColor = IceSurface.copy(alpha = 0.94f)),
    shape = RoundedCornerShape(12.dp),
    border = androidx.compose.foundation.BorderStroke(1.5.dp, IceBorder)
  ) {
    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
      // Row 1: Scores and Pause
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // 1P Score
        Column {
          Text(
            text = "1P GLINT",
            color = FrostMint,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
          )
          Text(
            text = String.format(java.util.Locale.US, "%06d", p1Score),
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace
          )
        }

        // If 2-Player mode, display 2P score
        if (engine.isTwoPlayer && p2 != null) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "2P EMBER",
              color = CoralBlaze,
              fontSize = 10.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace,
              letterSpacing = 1.sp
            )
            Text(
              text = String.format(java.util.Locale.US, "%06d", p2Score),
              color = Color.White,
              fontSize = 15.sp,
              fontWeight = FontWeight.ExtraBold,
              fontFamily = FontFamily.Monospace
            )
          }
        } else {
          // High Score
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "HI-SCORE",
              color = ArcadeYellow,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              letterSpacing = 1.sp
            )
            Text(
              text = String.format(java.util.Locale.US, "%06d", highScore),
              color = Color.White.copy(alpha = 0.95f),
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        // Lives and Pause
        Row(verticalAlignment = Alignment.CenterVertically) {
          Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier.padding(end = 8.dp)
          ) {
            for (i in 1..3) {
              Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Life $i",
                tint = if (i <= p1Lives) DangerEmber else Color.DarkGray,
                modifier = Modifier.size(16.dp)
              )
            }
          }

          IconButton(
            onClick = onPauseClicked,
            modifier = Modifier
              .size(34.dp)
              .background(IceSurfaceElevated, CircleShape)
              .border(1.dp, IceBorder, CircleShape)
              .testTag("pause_button")
          ) {
            Icon(
              imageVector = Icons.Default.Pause,
              contentDescription = "Pause",
              tint = NeonCyan,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      // Row 2: Level Badge & F-R-O-S-T bonus letter badges
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .background(AbyssMidnight, RoundedCornerShape(4.dp))
              .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = "STAGE ${engine.currentLevelIdx + 1}/300",
              color = NeonCyan,
              fontSize = 10.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace
            )
          }
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = engine.currentLevel.name.uppercase(java.util.Locale.US),
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            maxLines = 1
          )
        }

        // FROST Letters Badges
        Row(
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          val word = listOf('F', 'R', 'O', 'S', 'T')
          for (ch in word) {
            val collected = engine.collectedLetters.contains(ch)
            Box(
              modifier = Modifier
                .size(20.dp)
                .background(
                  if (collected) ArcadeYellow else AbyssMidnight,
                  RoundedCornerShape(4.dp)
                )
                .border(
                  1.dp,
                  if (collected) Color.White else IceBorder.copy(alpha = 0.6f),
                  RoundedCornerShape(4.dp)
                ),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = ch.toString(),
                color = if (collected) AbyssMidnight else Color.Gray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun ArcadeTouchControls(
  engine: com.example.game.GameEngine,
  buttonScale: Float,
  isLeftHanded: Boolean,
  isTwoPlayer: Boolean
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 8.dp, vertical = 6.dp)
      .background(
        Brush.verticalGradient(
          listOf(IceSurfaceElevated.copy(alpha = 0.96f), IceSurface.copy(alpha = 0.98f))
        ),
        RoundedCornerShape(20.dp)
      )
      .border(1.5.dp, IceBorder, RoundedCornerShape(20.dp))
      .padding(horizontal = 10.dp, vertical = 8.dp)
  ) {
    if (isTwoPlayer) {
      // 2-Player Shared Layout: P1 controls on Left, P2 controls on Right
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Player 1 (Glint - Mint)
        PlayerMiniPad(
          playerId = 1,
          engine = engine,
          primaryColor = FrostMint,
          label = "1P GLINT"
        )

        // Player 2 (Ember - Coral)
        PlayerMiniPad(
          playerId = 2,
          engine = engine,
          primaryColor = DangerEmber,
          label = "2P EMBER"
        )
      }
    } else {
      // 1-Player Layout: Professional 4-Key D-Pad on one side, Action buttons (HIT, JUMP) on the other
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        val dPadContent = @Composable {
          ArcadeDirectionalPad(
            engine = engine,
            playerId = 1,
            scale = buttonScale
          )
        }

        val actionButtonsContent = @Composable {
          ArcadeHitJumpDeck(
            engine = engine,
            playerId = 1,
            scale = buttonScale
          )
        }

        if (isLeftHanded) {
          actionButtonsContent()
          dPadContent()
        } else {
          dPadContent()
          actionButtonsContent()
        }
      }
    }
  }
}

@Composable
fun ArcadeDirectionalPad(
  engine: com.example.game.GameEngine,
  playerId: Int,
  scale: Float
) {
  val btnSize = (44 * scale).dp
  val spacing = (3 * scale).dp

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(spacing)
  ) {
    // Top Row: UPPER (Jump / Up)
    ArcadeDirectionButton(
      icon = Icons.Default.ArrowUpward,
      label = "UP",
      size = btnSize,
      testTag = "dpad_up_$playerId",
      onStateChange = { isPressed ->
        engine.setPlayerJump(playerId, isPressed)
      }
    )

    // Middle Row: LEFT, CENTER PIVOT, RIGHT
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
      // LEFT
      ArcadeDirectionButton(
        icon = Icons.AutoMirrored.Filled.ArrowBack,
        label = "LEFT",
        size = btnSize,
        testTag = "dpad_left_$playerId",
        onStateChange = { isPressed ->
          engine.setPlayerMove(playerId, if (isPressed) -1f else 0f)
        }
      )

      // Center D-Pad metallic pivot hub
      Box(
        modifier = Modifier
          .size(btnSize * 0.72f)
          .clip(CircleShape)
          .background(IceSurface)
          .border(1.2.dp, IceBorder, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Box(
          modifier = Modifier
            .size(btnSize * 0.28f)
            .clip(CircleShape)
            .background(NeonCyan.copy(alpha = 0.85f))
        )
      }

      // RIGHT
      ArcadeDirectionButton(
        icon = Icons.AutoMirrored.Filled.ArrowForward,
        label = "RIGHT",
        size = btnSize,
        testTag = "dpad_right_$playerId",
        onStateChange = { isPressed ->
          engine.setPlayerMove(playerId, if (isPressed) 1f else 0f)
        }
      )
    }

    // Bottom Row: LOWER (Drop through platform / Down)
    ArcadeDirectionButton(
      icon = Icons.Default.ArrowDownward,
      label = "DOWN",
      size = btnSize,
      testTag = "dpad_down_$playerId",
      onStateChange = { isPressed ->
        engine.setPlayerMove(playerId, 0f, dropDown = isPressed)
      }
    )
  }
}

@Composable
fun ArcadeHitJumpDeck(
  engine: com.example.game.GameEngine,
  playerId: Int,
  scale: Float
) {
  Row(
    horizontalArrangement = Arrangement.spacedBy((10 * scale).dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // HIT Button: Freeze & Kick snow orbs
    ArcadeActionButton(
      title = "HIT",
      subtitle = "FREEZE / KICK",
      icon = Icons.Default.Bolt,
      primaryColor = NeonCyan,
      scale = scale,
      testTag = "action_hit_$playerId",
      onPress = {
        engine.firePlayer(playerId)
      }
    )

    // JUMP Button: High / Short jumps
    ArcadeHoldActionButton(
      title = "JUMP",
      subtitle = "HOLD HIGH",
      icon = Icons.Default.Upgrade,
      primaryColor = ArcadeYellow,
      scale = scale,
      testTag = "action_jump_$playerId",
      onStateChange = { isPressed ->
        engine.setPlayerJump(playerId, isPressed)
      }
    )
  }
}

@Composable
fun ArcadeDirectionButton(
  icon: ImageVector,
  label: String,
  size: androidx.compose.ui.unit.Dp,
  testTag: String,
  onStateChange: (Boolean) -> Unit
) {
  var isPressed by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier
      .size(size)
      .clip(RoundedCornerShape(12.dp))
      .background(
        Brush.verticalGradient(
          if (isPressed) {
            listOf(NeonCyan, Color(0xFF0091EA))
          } else {
            listOf(IceSurfaceElevated, IceSurface)
          }
        )
      )
      .border(
        width = if (isPressed) 2.dp else 1.2.dp,
        color = if (isPressed) Color.White else IceBorder,
        shape = RoundedCornerShape(12.dp)
      )
      .pointerInput(Unit) {
        detectTapGestures(
          onPress = {
            isPressed = true
            onStateChange(true)
            tryAwaitRelease()
            isPressed = false
            onStateChange(false)
          }
        )
      }
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = if (isPressed) AbyssMidnight else Color.White,
        modifier = Modifier.size(size * 0.44f)
      )
      Text(
        text = label,
        color = if (isPressed) AbyssMidnight else Color(0xFF90A4AE),
        fontSize = 7.5.sp,
        fontWeight = FontWeight.Black,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 0.4.sp
      )
    }
  }
}

@Composable
fun ArcadeActionButton(
  title: String,
  subtitle: String,
  icon: ImageVector,
  primaryColor: Color,
  scale: Float,
  testTag: String,
  onPress: () -> Unit
) {
  var isPressed by remember { mutableStateOf(false) }
  val widthDp = (78 * scale).dp
  val heightDp = (60 * scale).dp

  Box(
    modifier = Modifier
      .width(widthDp)
      .height(heightDp)
      .clip(RoundedCornerShape(16.dp))
      .background(
        Brush.verticalGradient(
          if (isPressed) {
            listOf(Color.White, primaryColor)
          } else {
            listOf(primaryColor, primaryColor.copy(alpha = 0.82f))
          }
        )
      )
      .border(
        width = 2.dp,
        color = if (isPressed) Color.White else Color.White.copy(alpha = 0.85f),
        shape = RoundedCornerShape(16.dp)
      )
      .pointerInput(Unit) {
        detectTapGestures(
          onPress = {
            isPressed = true
            onPress()
            tryAwaitRelease()
            isPressed = false
          }
        )
      }
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = AbyssMidnight,
          modifier = Modifier.size((16 * scale).dp)
        )
        Text(
          text = title,
          color = AbyssMidnight,
          fontSize = (13 * scale).sp,
          fontWeight = FontWeight.Black,
          fontFamily = FontFamily.Monospace,
          letterSpacing = 0.5.sp
        )
      }
      Text(
        text = subtitle,
        color = AbyssMidnight.copy(alpha = 0.85f),
        fontSize = (7.5f * scale).sp,
        fontWeight = FontWeight.ExtraBold,
        fontFamily = FontFamily.Monospace
      )
    }
  }
}

@Composable
fun ArcadeHoldActionButton(
  title: String,
  subtitle: String,
  icon: ImageVector,
  primaryColor: Color,
  scale: Float,
  testTag: String,
  onStateChange: (Boolean) -> Unit
) {
  var isPressed by remember { mutableStateOf(false) }
  val widthDp = (78 * scale).dp
  val heightDp = (60 * scale).dp

  Box(
    modifier = Modifier
      .width(widthDp)
      .height(heightDp)
      .clip(RoundedCornerShape(16.dp))
      .background(
        Brush.verticalGradient(
          if (isPressed) {
            listOf(Color.White, primaryColor)
          } else {
            listOf(primaryColor, primaryColor.copy(alpha = 0.82f))
          }
        )
      )
      .border(
        width = 2.dp,
        color = if (isPressed) Color.White else Color.White.copy(alpha = 0.85f),
        shape = RoundedCornerShape(16.dp)
      )
      .pointerInput(Unit) {
        detectTapGestures(
          onPress = {
            isPressed = true
            onStateChange(true)
            tryAwaitRelease()
            isPressed = false
            onStateChange(false)
          }
        )
      }
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = AbyssMidnight,
          modifier = Modifier.size((16 * scale).dp)
        )
        Text(
          text = title,
          color = AbyssMidnight,
          fontSize = (13 * scale).sp,
          fontWeight = FontWeight.Black,
          fontFamily = FontFamily.Monospace,
          letterSpacing = 0.5.sp
        )
      }
      Text(
        text = subtitle,
        color = AbyssMidnight.copy(alpha = 0.85f),
        fontSize = (7.5f * scale).sp,
        fontWeight = FontWeight.ExtraBold,
        fontFamily = FontFamily.Monospace
      )
    }
  }
}

@Composable
fun PlayerMiniPad(
  playerId: Int,
  engine: com.example.game.GameEngine,
  primaryColor: Color,
  label: String
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .background(IceSurface, RoundedCornerShape(14.dp))
      .border(1.5.dp, primaryColor, RoundedCornerShape(14.dp))
      .padding(6.dp)
  ) {
    Text(
      text = label,
      color = primaryColor,
      fontSize = 9.sp,
      fontWeight = FontWeight.Black,
      fontFamily = FontFamily.Monospace
    )
    Spacer(modifier = Modifier.height(4.dp))
    Row(
      horizontalArrangement = Arrangement.spacedBy(4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      ArcadeDirectionButton(
        icon = Icons.AutoMirrored.Filled.ArrowBack,
        label = "L",
        size = 36.dp,
        testTag = "p${playerId}_left",
        onStateChange = { isPressed -> engine.setPlayerMove(playerId, if (isPressed) -1f else 0f) }
      )
      ArcadeDirectionButton(
        icon = Icons.Default.ArrowUpward,
        label = "UP",
        size = 36.dp,
        testTag = "p${playerId}_up",
        onStateChange = { isPressed -> engine.setPlayerJump(playerId, isPressed) }
      )
      ArcadeDirectionButton(
        icon = Icons.Default.ArrowDownward,
        label = "DN",
        size = 36.dp,
        testTag = "p${playerId}_down",
        onStateChange = { isPressed -> engine.setPlayerMove(playerId, 0f, dropDown = isPressed) }
      )
      ArcadeDirectionButton(
        icon = Icons.AutoMirrored.Filled.ArrowForward,
        label = "R",
        size = 36.dp,
        testTag = "p${playerId}_right",
        onStateChange = { isPressed -> engine.setPlayerMove(playerId, if (isPressed) 1f else 0f) }
      )
      ArcadeActionButton(
        title = "HIT",
        subtitle = "ATK",
        icon = Icons.Default.Bolt,
        primaryColor = primaryColor,
        scale = 0.65f,
        testTag = "p${playerId}_hit",
        onPress = { engine.firePlayer(playerId) }
      )
      ArcadeHoldActionButton(
        title = "JUMP",
        subtitle = "JMP",
        icon = Icons.Default.Upgrade,
        primaryColor = ArcadeYellow,
        scale = 0.65f,
        testTag = "p${playerId}_jump",
        onStateChange = { isPressed -> engine.setPlayerJump(playerId, isPressed) }
      )
    }
  }
}
