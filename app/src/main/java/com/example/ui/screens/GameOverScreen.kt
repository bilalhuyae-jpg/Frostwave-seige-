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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.audio.GameAudio
import com.example.ui.theme.AbyssMidnight
import com.example.ui.theme.ArcadeYellow
import com.example.ui.theme.DangerEmber
import com.example.ui.theme.IceBorder
import com.example.ui.theme.IceSurface
import com.example.ui.theme.NeonCyan
import kotlinx.coroutines.delay

@Composable
fun GameOverScreen(
  finalScore: Int,
  highScore: Int,
  levelReached: Int,
  onContinue: () -> Unit,
  onRetry: () -> Unit,
  onQuit: () -> Unit
) {
  var countdown by remember { mutableIntStateOf(9) }
  var countdownActive by remember { mutableIntStateOf(1) }

  LaunchedEffect(countdownActive) {
    while (countdown > 0 && countdownActive == 1) {
      delay(1000)
      countdown--
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.88f)),
    contentAlignment = Alignment.Center
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth(0.9f)
        .padding(16.dp),
      colors = CardDefaults.cardColors(containerColor = IceSurface),
      shape = RoundedCornerShape(24.dp),
      border = androidx.compose.foundation.BorderStroke(2.dp, DangerEmber)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "GAME OVER",
          color = DangerEmber,
          fontSize = 28.sp,
          fontWeight = FontWeight.Black,
          fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Classic Arcade Continue Countdown Ring
        if (countdown > 0) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 8.dp)
          ) {
            Text(
              text = "CONTINUE?",
              color = ArcadeYellow,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
              modifier = Modifier
                .size(60.dp)
                .background(AbyssMidnight, CircleShape)
                .border(2.5.dp, NeonCyan, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = countdown.toString(),
                color = if (countdown <= 3) DangerEmber else NeonCyan,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Final Score Stats
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
            Text(text = "FINAL SCORE:", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            Text(text = "$finalScore PTS", color = ArcadeYellow, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
          }
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(text = "RECORD HIGH:", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            Text(text = "$highScore PTS", color = NeonCyan, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
          }
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(text = "LEVEL REACHED:", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            Text(text = "LEVEL $levelReached", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons
        if (countdown > 0) {
          Button(
            onClick = {
              countdownActive = 0
              onContinue()
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
              .testTag("continue_countdown_button"),
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = AbyssMidnight),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text(
              text = "INSERT COIN / CONTINUE ($countdown)",
              fontSize = 13.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace
            )
          }
          Spacer(modifier = Modifier.height(10.dp))
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Button(
            onClick = onRetry,
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("retry_button"),
            colors = ButtonDefaults.buttonColors(containerColor = IceBorder, contentColor = Color.White),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "RETRY", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
          }

          Button(
            onClick = onQuit,
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("game_over_quit_button"),
            colors = ButtonDefaults.buttonColors(containerColor = DangerEmber, contentColor = Color.White),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "MENU", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
          }
        }
      }
    }
  }
}
