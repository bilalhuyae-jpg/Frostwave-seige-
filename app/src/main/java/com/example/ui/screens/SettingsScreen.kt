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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.GameAudio
import com.example.data.model.GameSettingsEntity
import com.example.ui.theme.AbyssMidnight
import com.example.ui.theme.ArcadeYellow
import com.example.ui.theme.ArcticDeep
import com.example.ui.theme.FrostMint
import com.example.ui.theme.FrostTeal
import com.example.ui.theme.IceBorder
import com.example.ui.theme.IceSurface
import com.example.ui.theme.NeonCyan
import com.example.viewmodel.FrostwaveViewModel

@Composable
fun SettingsScreen(
  viewModel: FrostwaveViewModel,
  onBack: () -> Unit
) {
  val settingsState by viewModel.settings.collectAsState()
  val s = settingsState ?: GameSettingsEntity()

  var soundVolume by remember(s.soundVolume) { mutableFloatStateOf(s.soundVolume) }
  var musicVolume by remember(s.musicVolume) { mutableFloatStateOf(s.musicVolume) }
  var scanlinesEnabled by remember(s.scanlinesEnabled) { mutableStateOf(s.scanlinesEnabled) }
  var isLeftHanded by remember(s.isLeftHanded) { mutableStateOf(s.isLeftHanded) }
  var buttonScale by remember(s.buttonScale) { mutableFloatStateOf(s.buttonScale) }
  var language by remember(s.language) { mutableStateOf(s.language) }

  fun save() {
    viewModel.updateSettings(
      s.copy(
        soundVolume = soundVolume,
        musicVolume = musicVolume,
        scanlinesEnabled = scanlinesEnabled,
        isLeftHanded = isLeftHanded,
        buttonScale = buttonScale,
        language = language
      )
    )
  }

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
          onClick = {
            save()
            onBack()
          },
          modifier = Modifier
            .size(40.dp)
            .background(IceSurface, CircleShape)
            .border(1.dp, IceBorder, CircleShape)
            .testTag("settings_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = Color.White
          )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
          text = "ARCADE SETTINGS",
          color = ArcadeYellow,
          fontSize = 20.sp,
          fontWeight = FontWeight.Black,
          fontFamily = FontFamily.Monospace
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Audio Settings Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = IceSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, IceBorder)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "AUDIO & SOUND FX",
            color = NeonCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Sound FX Volume
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(text = "Sound FX: ${(soundVolume * 100).toInt()}%", color = Color.White, fontSize = 13.sp)
            Button(
              onClick = {
                GameAudio.playSound(GameAudio.SoundEffect.COIN)
              },
              colors = ButtonDefaults.buttonColors(containerColor = IceBorder),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.height(32.dp)
            ) {
              Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("TEST SFX", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
          }
          Slider(
            value = soundVolume,
            onValueChange = {
              soundVolume = it
              save()
            },
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
              thumbColor = NeonCyan,
              activeTrackColor = NeonCyan,
              inactiveTrackColor = AbyssMidnight
            ),
            modifier = Modifier.testTag("sound_volume_slider")
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Music Volume
          Text(text = "8-Bit Arcade Music: ${(musicVolume * 100).toInt()}%", color = Color.White, fontSize = 13.sp)
          Slider(
            value = musicVolume,
            onValueChange = {
              musicVolume = it
              save()
            },
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
              thumbColor = ArcadeYellow,
              activeTrackColor = ArcadeYellow,
              inactiveTrackColor = AbyssMidnight
            ),
            modifier = Modifier.testTag("music_volume_slider")
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Display & Retro CRT Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = IceSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, IceBorder)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "DISPLAY & VISUALS",
            color = NeonCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(text = "Retro CRT Scanlines", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
              Text(text = "Authentic 90s arcade monitor effect", color = FrostMint, fontSize = 11.sp)
            }
            Switch(
              checked = scanlinesEnabled,
              onCheckedChange = {
                scanlinesEnabled = it
                save()
              },
              colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = NeonCyan
              ),
              modifier = Modifier.testTag("crt_scanlines_switch")
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Controls Layout Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = IceSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, IceBorder)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "CONTROLS & ERGONOMICS",
            color = NeonCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )

          Spacer(modifier = Modifier.height(12.dp))

          Text(text = "Handedness", color = Color.White, fontSize = 13.sp)
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 4.dp)) {
            listOf("Right-Handed" to false, "Left-Handed" to true).forEach { (label, isLeft) ->
              FilterChip(
                selected = isLeftHanded == isLeft,
                onClick = {
                  isLeftHanded = isLeft
                  save()
                },
                label = { Text(label, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = NeonCyan,
                  selectedLabelColor = AbyssMidnight
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(text = "Virtual Button Sizing", color = Color.White, fontSize = 13.sp)
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 4.dp)) {
            listOf("Compact" to 0.85f, "Normal" to 1.0f, "Large" to 1.15f).forEach { (label, scaleVal) ->
              FilterChip(
                selected = buttonScale == scaleVal,
                onClick = {
                  buttonScale = scaleVal
                  save()
                },
                label = { Text(label, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = ArcadeYellow,
                  selectedLabelColor = AbyssMidnight
                )
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Language Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = IceSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, IceBorder)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "LANGUAGE",
            color = NeonCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )

          Spacer(modifier = Modifier.height(8.dp))

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
            listOf("English", "Español", "日本語", "Deutsch").forEach { lang ->
              FilterChip(
                selected = language == lang,
                onClick = {
                  language = lang
                  save()
                },
                label = { Text(lang, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = NeonCyan,
                  selectedLabelColor = AbyssMidnight
                )
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}
