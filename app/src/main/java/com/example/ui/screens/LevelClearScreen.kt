package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AbyssMidnight
import com.example.ui.theme.ArcadeYellow
import com.example.ui.theme.FrostMint
import com.example.ui.theme.IceSurface
import com.example.ui.theme.NeonCyan

@Composable
fun LevelClearScreen(
  levelNumber: Int,
  levelName: String,
  currentScore: Int,
  collectedLetters: Set<Char>,
  onNextLevel: () -> Unit,
  onPlay1P: () -> Unit = {},
  onPlay2P: () -> Unit = {},
  onQuit: () -> Unit = {}
) {
  val isGameClear = levelNumber >= 300
  var startTally by remember { mutableStateOf(false) }
  LaunchedEffect(Unit) {
    startTally = true
  }

  val baseBonus = 1000 * levelNumber
  val letterBonus = collectedLetters.size * 500
  val totalStageBonus = baseBonus + letterBonus

  val animatedBonus by animateIntAsState(
    targetValue = if (startTally) totalStageBonus else 0,
    animationSpec = tween(1200, easing = FastOutSlowInEasing),
    label = "scoreTally"
  )

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.88f)),
    contentAlignment = Alignment.Center
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .padding(14.dp),
      colors = CardDefaults.cardColors(containerColor = IceSurface),
      shape = RoundedCornerShape(24.dp),
      border = androidx.compose.foundation.BorderStroke(2.dp, if (isGameClear) ArcadeYellow else NeonCyan)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Icon(
          imageVector = Icons.Default.CheckCircle,
          contentDescription = null,
          tint = if (isGameClear) ArcadeYellow else FrostMint,
          modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = if (isGameClear) "STAGE END" else "STAGE CLEAR!",
          color = ArcadeYellow,
          fontSize = if (isGameClear) 28.sp else 24.sp,
          fontWeight = FontWeight.Black,
          fontFamily = FontFamily.Monospace
        )

        Text(
          text = if (isGameClear) "ALL 300 STAGES COMPLETED!" else "STAGE $levelNumber / 300: $levelName",
          color = NeonCyan,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Bonus Letters Collected This Run
        Text(
          text = "BONUS LETTERS COLLECTED",
          color = Color.White,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          val word = listOf('F', 'R', 'O', 'S', 'T')
          for (ch in word) {
            val isCollected = collectedLetters.contains(ch)
            Box(
              modifier = Modifier
                .size(36.dp)
                .background(
                  if (isCollected) ArcadeYellow else AbyssMidnight,
                  CircleShape
                )
                .border(
                  1.5.dp,
                  if (isCollected) Color.White else Color.Gray,
                  CircleShape
                ),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = ch.toString(),
                color = if (isCollected) AbyssMidnight else Color.Gray,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Score Tally Box
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(AbyssMidnight, RoundedCornerShape(12.dp))
            .padding(14.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(text = "CLEAR BONUS:", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            Text(text = "+$baseBonus", color = FrostMint, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
          }
          Spacer(modifier = Modifier.height(4.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(text = "LETTERS (${collectedLetters.size}/5):", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            Text(text = "+$letterBonus", color = FrostMint, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
          }
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(text = "STAGE BONUS:", color = ArcadeYellow, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Text(text = "+$animatedBonus", color = ArcadeYellow, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (isGameClear) {
          // Stage 300 Winning Board Actions: Play 1 Player, Play 2 Players, Main Menu
          Button(
            onClick = onPlay1P,
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("play_1_player_button"),
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = AbyssMidnight),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text(
              text = "PLAY 1 PLAYER",
              fontSize = 13.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Button(
            onClick = onPlay2P,
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("play_2_players_button"),
            colors = ButtonDefaults.buttonColors(containerColor = ArcadeYellow, contentColor = AbyssMidnight),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text(
              text = "PLAY 2 PLAYERS",
              fontSize = 13.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Button(
            onClick = onQuit,
            modifier = Modifier
              .fillMaxWidth()
              .height(46.dp)
              .testTag("winning_board_menu_button"),
            colors = ButtonDefaults.buttonColors(containerColor = AbyssMidnight, contentColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)
          ) {
            Text(
              text = "MAIN MENU",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }
        } else {
          Button(
            onClick = onNextLevel,
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
              .testTag("next_level_button"),
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = AbyssMidnight),
            shape = RoundedCornerShape(14.dp)
          ) {
            Text(
              text = "CONTINUE TO NEXT STAGE",
              fontSize = 13.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
          }
        }
      }
    }
  }
}
