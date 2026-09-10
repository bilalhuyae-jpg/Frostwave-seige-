package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AbyssMidnight
import com.example.ui.theme.ArcadeYellow
import com.example.ui.theme.ArcticDeep
import com.example.ui.theme.FrostMint
import com.example.ui.theme.FrostTeal
import com.example.ui.theme.NeonCyan
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
  onStartClicked: () -> Unit
) {
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.96f,
    targetValue = 1.04f,
    animationSpec = infiniteRepeatable(
      animation = tween(900, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseScale"
  )

  val glowAlpha by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(750, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "glowAlpha"
  )

  // Manual start only - no automatic navigation without user click
  // (User requested: Khud ba khud koi chej open na ho)

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          listOf(AbyssMidnight, ArcticDeep, FrostTeal, AbyssMidnight)
        )
      )
      .clickable { onStartClicked() }
      .statusBarsPadding()
      .navigationBarsPadding()
      .padding(24.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxSize()
    ) {
      // Top Arcade Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "1P 00",
          color = ArcadeYellow,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
        Text(
          text = "HI-SCORE 25000",
          color = Color.White,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
        Text(
          text = "CREDIT 01",
          color = NeonCyan,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
      }

      // Center Hero Visual & Title Card
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.Center
      ) {
        // Hero Banner Art
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, NeonCyan, RoundedCornerShape(16.dp))
        ) {
          Image(
            painter = painterResource(id = R.drawable.img_hero_banner),
            contentDescription = "Frostwave Siege Arcade Banner",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
          )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Title text with frost glow effect
        Text(
          text = "FROSTWAVE",
          color = NeonCyan,
          fontSize = 38.sp,
          fontWeight = FontWeight.Black,
          fontFamily = FontFamily.Monospace,
          letterSpacing = 2.sp,
          textAlign = TextAlign.Center,
          modifier = Modifier.scale(pulseScale)
        )
        Text(
          text = "S I E G E",
          color = ArcadeYellow,
          fontSize = 32.sp,
          fontWeight = FontWeight.Black,
          fontFamily = FontFamily.Monospace,
          letterSpacing = 6.sp,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "FREEZE & ROLL ARCADE PLATFORMER",
          color = FrostMint,
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          letterSpacing = 1.sp,
          textAlign = TextAlign.Center
        )
      }

      // Bottom Call to Action Button
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 24.dp)
      ) {
        Button(
          onClick = onStartClicked,
          modifier = Modifier
            .fillMaxWidth(0.85f)
            .height(56.dp)
            .scale(pulseScale)
            .testTag("start_game_button"),
          colors = ButtonDefaults.buttonColors(
            containerColor = NeonCyan,
            contentColor = AbyssMidnight
          ),
          shape = RoundedCornerShape(28.dp)
        ) {
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
          )
          Text(
            text = "PRESS TO START",
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "© 2026 AI STUDIO ARCADE • ALL RIGHTS RESERVED",
          color = Color.White.copy(alpha = glowAlpha * 0.7f),
          fontSize = 10.sp,
          fontWeight = FontWeight.Normal,
          fontFamily = FontFamily.Monospace,
          textAlign = TextAlign.Center
        )
      }
    }
  }
}
