package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AbyssMidnight
import com.example.ui.theme.ArcadeYellow
import com.example.ui.theme.ArcticDeep
import com.example.ui.theme.CoralBlaze
import com.example.ui.theme.FrostMint
import com.example.ui.theme.FrostTeal
import com.example.ui.theme.IceBorder
import com.example.ui.theme.IceSurface
import com.example.ui.theme.IceSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PowerUpBlast
import com.example.viewmodel.FrostwaveViewModel

@Composable
fun MainMenuScreen(
  viewModel: FrostwaveViewModel,
  onPlay1P: (powerUpStart: Boolean) -> Unit,
  onPlay2P: (powerUpStart: Boolean) -> Unit,
  onLevelSelect: () -> Unit,
  onLeaderboard: () -> Unit,
  onSettings: () -> Unit,
  onHowToPlay: () -> Unit
) {
  var powerUpStart by remember { mutableStateOf(viewModel.settings.value?.powerUpStart ?: false) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          listOf(AbyssMidnight, ArcticDeep, FrostTeal)
        )
      )
      .statusBarsPadding()
      .navigationBarsPadding()
      .padding(horizontal = 24.dp, vertical = 16.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Top
    ) {
      // Top High Score Ribbon
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp),
        colors = CardDefaults.cardColors(containerColor = IceSurface.copy(alpha = 0.8f)),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, IceBorder)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Star,
              contentDescription = null,
              tint = ArcadeYellow,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "HI-SCORE",
              color = ArcadeYellow,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }
          Text(
            text = "${viewModel.highestScore.value ?: 25000} PTS",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Arcade Title
      Text(
        text = "FROSTWAVE",
        color = NeonCyan,
        fontSize = 32.sp,
        fontWeight = FontWeight.Black,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 2.sp,
        textAlign = TextAlign.Center
      )
      Text(
        text = "SIEGE",
        color = ArcadeYellow,
        fontSize = 28.sp,
        fontWeight = FontWeight.Black,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 4.sp,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(28.dp))

      // Power-Up Start Toggle Option
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (powerUpStart) IceSurfaceElevated else IceSurface
        ),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
          1.5.dp,
          if (powerUpStart) PowerUpBlast else IceBorder
        )
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Icon(
              imageVector = Icons.Default.Bolt,
              contentDescription = null,
              tint = if (powerUpStart) PowerUpBlast else NeonCyan,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "POWER-UP START",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
              Text(
                text = "Begin with Speed & Frost Blast",
                color = if (powerUpStart) PowerUpBlast else FrostMint,
                fontSize = 11.sp
              )
            }
          }
          Switch(
            checked = powerUpStart,
            onCheckedChange = {
              powerUpStart = it
              viewModel.updateSettings(
                (viewModel.settings.value ?: com.example.data.model.GameSettingsEntity()).copy(
                  powerUpStart = it
                )
              )
            },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White,
              checkedTrackColor = PowerUpBlast
            ),
            modifier = Modifier.testTag("power_up_start_switch")
          )
        }
      }

      // Primary Action: 1-Player Campaign
      MenuActionButton(
        text = "PLAY (1 PLAYER)",
        subtitle = "Solo Hero Glint • Freeze & Roll",
        icon = Icons.Default.PlayArrow,
        color = NeonCyan,
        textColor = AbyssMidnight,
        testTag = "play_1p_button",
        onClick = { onPlay1P(powerUpStart) }
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Secondary Action: 2-Player Co-Op
      MenuActionButton(
        text = "2-PLAYER CO-OP",
        subtitle = "Glint & Ember • Shared device co-op",
        icon = Icons.Default.Group,
        color = CoralBlaze,
        textColor = AbyssMidnight,
        testTag = "play_2p_button",
        onClick = { onPlay2P(powerUpStart) }
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Stage Select Action (300 Levels)
      MenuActionButton(
        text = "STAGE SELECT (300 LEVELS)",
        subtitle = "Choose from 6 Worlds & 300 Stages",
        icon = Icons.Default.GridView,
        color = ArcadeYellow,
        textColor = AbyssMidnight,
        testTag = "stage_select_button",
        onClick = onLevelSelect
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Navigation Buttons: Leaderboard, How To Play, Settings
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        SecondaryMenuButton(
          text = "SCORES",
          icon = Icons.Default.Leaderboard,
          modifier = Modifier.weight(1f),
          testTag = "leaderboard_button",
          onClick = onLeaderboard
        )
        SecondaryMenuButton(
          text = "HOW TO PLAY",
          icon = Icons.Default.HelpOutline,
          modifier = Modifier.weight(1f),
          testTag = "how_to_play_button",
          onClick = onHowToPlay
        )
        SecondaryMenuButton(
          text = "SETTINGS",
          icon = Icons.Default.Settings,
          modifier = Modifier.weight(1f),
          testTag = "settings_button",
          onClick = onSettings
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Mini How To Hint Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = IceSurface.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, IceBorder.copy(alpha = 0.5f))
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "ARCADE PRO-TIP:",
            color = ArcadeYellow,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Shoot enemies repeatedly to freeze them into a solid snow orb, then KICK it to wipe out entire platforms of enemies in chain reactions!",
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 12.sp,
            lineHeight = 16.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
fun MenuActionButton(
  text: String,
  subtitle: String,
  icon: ImageVector,
  color: Color,
  textColor: Color,
  testTag: String,
  onClick: () -> Unit
) {
  Button(
    onClick = onClick,
    modifier = Modifier
      .fillMaxWidth()
      .height(68.dp)
      .border(1.5.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
      .testTag(testTag),
    colors = ButtonDefaults.buttonColors(
      containerColor = color,
      contentColor = textColor
    ),
    shape = RoundedCornerShape(16.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Start
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .background(textColor.copy(alpha = 0.18f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = textColor,
          modifier = Modifier.size(26.dp)
        )
      }
      Spacer(modifier = Modifier.width(14.dp))
      Column {
        Text(
          text = text,
          fontSize = 15.sp,
          fontWeight = FontWeight.Black,
          fontFamily = FontFamily.Monospace,
          color = textColor
        )
        Text(
          text = subtitle,
          fontSize = 11.5.sp,
          fontWeight = FontWeight.Bold,
          color = textColor.copy(alpha = 0.85f)
        )
      }
    }
  }
}

@Composable
fun SecondaryMenuButton(
  text: String,
  icon: ImageVector,
  modifier: Modifier = Modifier,
  testTag: String,
  onClick: () -> Unit
) {
  OutlinedButton(
    onClick = onClick,
    modifier = modifier
      .height(72.dp)
      .testTag(testTag),
    colors = ButtonDefaults.outlinedButtonColors(
      containerColor = IceSurface,
      contentColor = Color.White
    ),
    border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonCyan.copy(alpha = 0.6f)),
    shape = RoundedCornerShape(14.dp)
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = NeonCyan,
        modifier = Modifier.size(22.dp)
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Black,
        fontFamily = FontFamily.Monospace,
        textAlign = TextAlign.Center
      )
    }
  }
}
