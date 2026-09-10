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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.audio.GameAudio
import com.example.ui.theme.AbyssMidnight
import com.example.ui.theme.ArcadeYellow
import com.example.ui.theme.IceBorder
import com.example.ui.theme.IceSurface
import com.example.ui.theme.NeonCyan

@Composable
fun PauseOverlay(
  onResume: () -> Unit,
  onRestart: () -> Unit,
  onQuit: () -> Unit
) {
  var isMuted by remember { mutableStateOf(GameAudio.isMuted) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.75f)),
    contentAlignment = Alignment.Center
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth(0.85f)
        .padding(16.dp),
      colors = CardDefaults.cardColors(containerColor = IceSurface),
      shape = RoundedCornerShape(20.dp),
      border = androidx.compose.foundation.BorderStroke(2.dp, NeonCyan)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Text(
          text = "GAME PAUSED",
          color = ArcadeYellow,
          fontSize = 22.sp,
          fontWeight = FontWeight.Black,
          fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Resume Button
        Button(
          onClick = onResume,
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("resume_button"),
          colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = AbyssMidnight),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "RESUME",
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Restart Level Button
        Button(
          onClick = onRestart,
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("restart_level_button"),
          colors = ButtonDefaults.buttonColors(containerColor = IceBorder, contentColor = Color.White),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "RESTART LEVEL",
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sound Mute Toggle
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(AbyssMidnight, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "SOUND SFX & MUSIC",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
          IconButton(
            onClick = {
              isMuted = !isMuted
              GameAudio.isMuted = isMuted
            }
          ) {
            Icon(
              imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
              contentDescription = "Sound Toggle",
              tint = if (isMuted) Color.Gray else NeonCyan
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quit to Menu Button
        Button(
          onClick = onQuit,
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("quit_menu_button"),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4757), contentColor = Color.White),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "QUIT TO MENU",
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp
          )
        }
      }
    }
  }
}
