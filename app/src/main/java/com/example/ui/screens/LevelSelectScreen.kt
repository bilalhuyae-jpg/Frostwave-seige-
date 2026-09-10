package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.LevelCatalog
import com.example.game.WorldTheme
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
import com.example.ui.theme.IceWhite
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.viewmodel.FrostwaveViewModel

@Composable
fun LevelSelectScreen(
  viewModel: FrostwaveViewModel,
  onSelectLevel: (levelIndex: Int) -> Unit,
  onBack: () -> Unit
) {
  // Worlds: 6 Worlds x 50 stages = 300 Levels total
  // Within each world, group into 5 chapters of 10 levels each (or paging)
  var selectedWorldIndex by remember { mutableIntStateOf(0) }
  var selectedPageInWorld by remember { mutableIntStateOf(0) } // 5 pages of 10 levels = 50 levels per world

  val worlds = WorldTheme.entries
  val currentWorld = worlds[selectedWorldIndex]

  // Levels for current world page:
  val worldStartLevel = selectedWorldIndex * 50 + 1
  val pageStartLevel = worldStartLevel + selectedPageInWorld * 10
  val pageEndLevel = (pageStartLevel + 9).coerceAtMost(LevelCatalog.TOTAL_LEVELS)

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          listOf(
            Color(currentWorld.bgTop),
            Color(currentWorld.bgMid),
            AbyssMidnight
          )
        )
      )
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
    ) {
      // Top Navigation Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onBack,
          modifier = Modifier
            .size(44.dp)
            .background(IceSurface, CircleShape)
            .border(1.dp, IceBorder, CircleShape)
            .testTag("level_select_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = NeonCyan
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "STAGE SELECT",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = IceWhite
          )
          Text(
            text = "300 ARCADE CAMPAIGN LEVELS",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = NeonCyan
          )
        }

        // Total Levels Badge
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(currentWorld.brickColor))
            .border(1.dp, Color(currentWorld.accentColor), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
          Text(
            text = "300 STAGES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = Color(currentWorld.accentColor)
          )
        }
      }

      // World Tab Bar (6 Worlds)
      ScrollableTabRow(
        selectedTabIndex = selectedWorldIndex,
        containerColor = Color.Transparent,
        contentColor = NeonCyan,
        edgePadding = 16.dp,
        divider = {},
        indicator = { tabPositions ->
          if (selectedWorldIndex < tabPositions.size) {
            TabRowDefaults.SecondaryIndicator(
              Modifier.tabIndicatorOffset(tabPositions[selectedWorldIndex]),
              color = Color(currentWorld.accentColor),
              height = 3.dp
            )
          }
        }
      ) {
        worlds.forEachIndexed { index, world ->
          val isSelected = (index == selectedWorldIndex)
          Tab(
            selected = isSelected,
            onClick = {
              selectedWorldIndex = index
              selectedPageInWorld = 0
            },
            modifier = Modifier.testTag("world_tab_$index"),
            text = {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                  text = "WORLD ${index + 1}",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace,
                  color = if (isSelected) Color(world.accentColor) else TextMuted
                )
                Text(
                  text = world.displayName,
                  fontSize = 13.sp,
                  fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                  color = if (isSelected) IceWhite else TextMuted.copy(alpha = 0.7f)
                )
              }
            }
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // World Info Header Banner
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = IceSurface.copy(alpha = 0.85f)),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(currentWorld.accentColor).copy(alpha = 0.5f))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .background(Color(currentWorld.brickColor), CircleShape)
              .border(1.5.dp, Color(currentWorld.accentColor), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "W${selectedWorldIndex + 1}",
              fontSize = 12.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace,
              color = Color(currentWorld.accentColor)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = currentWorld.displayName,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              color = IceWhite
            )
            Text(
              text = currentWorld.subTitle,
              fontSize = 11.sp,
              color = TextMuted,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }

          // Stages range indicator
          Text(
            text = "L${worldStartLevel} - L${worldStartLevel + 49}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = Color(currentWorld.accentColor)
          )
        }
      }

      // Page Navigator for World (10 levels per view)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        IconButton(
          onClick = { if (selectedPageInWorld > 0) selectedPageInWorld-- },
          enabled = selectedPageInWorld > 0,
          modifier = Modifier
            .size(36.dp)
            .background(if (selectedPageInWorld > 0) IceSurface else IceSurface.copy(alpha = 0.3f), CircleShape)
            .testTag("page_prev_button")
        ) {
          Icon(
            imageVector = Icons.Default.ChevronLeft,
            contentDescription = "Previous Stages",
            tint = if (selectedPageInWorld > 0) NeonCyan else TextMuted.copy(alpha = 0.4f)
          )
        }

        // Page Indicator Chips (5 pages for 50 levels)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          for (p in 0 until 5) {
            val isPageSelected = (p == selectedPageInWorld)
            val pStart = worldStartLevel + p * 10
            val pEnd = pStart + 9
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (isPageSelected) Color(currentWorld.accentColor) else IceSurface)
                .clickable { selectedPageInWorld = p }
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = "$pStart-$pEnd",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = if (isPageSelected) AbyssMidnight else TextMuted
              )
            }
          }
        }

        IconButton(
          onClick = { if (selectedPageInWorld < 4) selectedPageInWorld++ },
          enabled = selectedPageInWorld < 4,
          modifier = Modifier
            .size(36.dp)
            .background(if (selectedPageInWorld < 4) IceSurface else IceSurface.copy(alpha = 0.3f), CircleShape)
            .testTag("page_next_button")
        ) {
          Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Next Stages",
            tint = if (selectedPageInWorld < 4) NeonCyan else TextMuted.copy(alpha = 0.4f)
          )
        }
      }

      // Level Grid: 10 levels on current page (2 columns x 5 rows)
      LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
      ) {
        val levelRange = pageStartLevel..pageEndLevel
        items(levelRange.count()) { index ->
          val levelNumber = pageStartLevel + index
          val config = LevelCatalog.getLevel(levelNumber)
          val isBoss = config.isBossLevel

          LevelCard(
            config = config,
            worldTheme = currentWorld,
            onClick = {
              onSelectLevel(levelNumber - 1) // 0-based index
            }
          )
        }
      }
    }
  }
}

@Composable
private fun LevelCard(
  config: com.example.game.LevelConfig,
  worldTheme: WorldTheme,
  onClick: () -> Unit
) {
  val isBoss = config.isBossLevel
  val cardBorderColor = if (isBoss) DangerEmber else Color(worldTheme.accentColor).copy(alpha = 0.7f)
  val badgeColor = if (isBoss) DangerEmber else Color(worldTheme.accentColor)

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .aspectRatio(1.4f)
      .clickable(onClick = onClick)
      .testTag("level_card_${config.levelNumber}"),
    colors = CardDefaults.cardColors(
      containerColor = if (isBoss) Color(0xFF2A1130) else IceSurface
    ),
    shape = RoundedCornerShape(14.dp),
    border = androidx.compose.foundation.BorderStroke(if (isBoss) 2.dp else 1.2.dp, cardBorderColor)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(12.dp),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top row: Level badge and Boss indicator
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(badgeColor.copy(alpha = 0.2f))
            .border(1.dp, badgeColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = "STAGE ${config.levelNumber}",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = badgeColor
          )
        }

        if (isBoss) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(DangerEmber)
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = "BOSS",
              fontSize = 9.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace,
              color = IceWhite
            )
          }
        } else {
          Text(
            text = "${config.enemySpawns.size} FOES",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = TextMuted
          )
        }
      }

      // Middle: Stage Name
      Text(
        text = config.name,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        color = IceWhite,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      // Bottom Row: Action Prompt
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = if (isBoss) Icons.Default.Bolt else Icons.Default.PlayArrow,
            contentDescription = null,
            tint = if (isBoss) DangerEmber else NeonCyan,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (isBoss) "CHALLENGE" else "START",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = if (isBoss) DangerEmber else NeonCyan
          )
        }

        Text(
          text = "${config.platforms.size} PLATFORMS",
          fontSize = 9.sp,
          fontFamily = FontFamily.Monospace,
          color = TextMuted.copy(alpha = 0.7f)
        )
      }
    }
  }
}
