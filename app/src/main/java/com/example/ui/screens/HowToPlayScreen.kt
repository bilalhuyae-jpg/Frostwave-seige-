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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AbyssMidnight
import com.example.ui.theme.ArcadeYellow
import com.example.ui.theme.ArcticDeep
import com.example.ui.theme.DangerEmber
import com.example.ui.theme.FrostMint
import com.example.ui.theme.FrostTeal
import com.example.ui.theme.IceBorder
import com.example.ui.theme.IceSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PowerUpBlast
import com.example.ui.theme.PowerUpGiant
import com.example.ui.theme.PowerUpSpeed

@Composable
fun HowToPlayScreen(
  onBack: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(listOf(AbyssMidnight, ArcticDeep, FrostTeal))
      )
      .statusBarsPadding()
      .navigationBarsPadding()
      .padding(16.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
    ) {
      // Top Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onBack,
          modifier = Modifier
            .size(40.dp)
            .background(IceSurface, CircleShape)
            .border(1.dp, IceBorder, CircleShape)
            .testTag("how_to_play_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = Color.White
          )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
          text = "HOW TO PLAY",
          color = ArcadeYellow,
          fontSize = 20.sp,
          fontWeight = FontWeight.Black,
          fontFamily = FontFamily.Monospace
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Section 1: Core Loop
      GuideCard(
        title = "1. FREEZE & ROLL",
        icon = Icons.Default.AcUnit,
        iconTint = NeonCyan
      ) {
        Text(
          text = "• SHOOT enemies with your frost wave to encase them in ice.\n" +
            "• At 100% freeze, they turn into a solid SNOW ORB.\n" +
            "• Walk up and press FIRE to KICK the orb! It rolls at high speed along platforms and bounces off walls.\n" +
            "• Rolling orbs wipe out everything in their path for massive combo points!",
          color = Color.White,
          fontSize = 12.sp,
          lineHeight = 18.sp
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Section 2: Combos & Letters
      GuideCard(
        title = "2. CHAIN COMBOS & F-R-O-S-T",
        icon = Icons.Default.SportsScore,
        iconTint = ArcadeYellow
      ) {
        Text(
          text = "• Crushing 1 enemy: +200 pts\n" +
            "• Crushing 2 enemies in one roll: +400 pts (COMBO x2)\n" +
            "• Crushing 3+ enemies: +800 / +1600 pts!\n" +
            "• Defeated enemies drop letters F, R, O, S, T. Collect all 5 letters to earn a massive +10,000 PTS BONUS & an extra 1-UP life!",
          color = Color.White,
          fontSize = 12.sp,
          lineHeight = 18.sp
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Section 3: Power-ups
      GuideCard(
        title = "3. ARCADE POWER-UPS",
        icon = Icons.Default.Bolt,
        iconTint = PowerUpBlast
      ) {
        PowerUpRow(color = PowerUpSpeed, name = "SPEED BOOTS", desc = "Super fast movement & agile jump responsiveness")
        Spacer(modifier = Modifier.height(6.dp))
        PowerUpRow(color = PowerUpBlast, name = "FROST BLAST", desc = "Instant 100% freeze on hit + wider impact area")
        Spacer(modifier = Modifier.height(6.dp))
        PowerUpRow(color = ArcadeYellow, name = "MAX RANGE", desc = "Frost waves travel full-screen horizontally")
        Spacer(modifier = Modifier.height(6.dp))
        PowerUpRow(color = PowerUpGiant, name = "GIANT MODE", desc = "Hero grows large and crushes enemies upon touch")
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Section 4: Boss Strategy
      GuideCard(
        title = "4. BOSS BATTLES: CRYO-TITANS",
        icon = Icons.Default.Shield,
        iconTint = DangerEmber
      ) {
        Text(
          text = "• Encounter powerful Cryo-Titans every 10 stages!\n" +
            "• Direct shots only deal 1-2 damage chip damage.\n" +
            "• SECRET STRATEGY: Freeze minion Wisps into Snow Orbs, then kick the orbs directly into the Titan to deal massive CRITICAL DAMAGE!",
          color = Color.White,
          fontSize = 12.sp,
          lineHeight = 18.sp
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Section 5: 300 Levels System
      GuideCard(
        title = "5. 300 LEVELS & 6 WORLDS",
        icon = Icons.Default.Star,
        iconTint = ArcadeYellow
      ) {
        Text(
          text = "• 300 unique stages spanning 6 thematic realms: Glacier Caverns, Frost Citadel, Crystal Spire, Blizzard Ruins, Abyssal Rift, and Titan Throne.\n" +
            "• Jump directly to any stage or world anytime via the STAGE SELECT menu.\n" +
            "• Bosses, enemy density, and platform architecture dynamically scale as you climb toward Stage 300!",
          color = Color.White,
          fontSize = 12.sp,
          lineHeight = 18.sp
        )
      }

      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@Composable
fun GuideCard(
  title: String,
  icon: ImageVector,
  iconTint: Color,
  content: @Composable () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = IceSurface),
    shape = RoundedCornerShape(14.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, IceBorder)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .background(iconTint.copy(alpha = 0.2f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = title,
          color = ArcadeYellow,
          fontWeight = FontWeight.Black,
          fontSize = 13.sp,
          fontFamily = FontFamily.Monospace
        )
      }
      Spacer(modifier = Modifier.height(10.dp))
      content()
    }
  }
}

@Composable
fun PowerUpRow(color: Color, name: String, desc: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier = Modifier
        .size(12.dp)
        .background(color, CircleShape)
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = "$name: ",
      color = color,
      fontWeight = FontWeight.Bold,
      fontSize = 11.sp,
      fontFamily = FontFamily.Monospace
    )
    Text(
      text = desc,
      color = Color.White.copy(alpha = 0.9f),
      fontSize = 11.sp
    )
  }
}
