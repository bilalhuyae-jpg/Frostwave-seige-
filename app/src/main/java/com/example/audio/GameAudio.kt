package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.sin

object GameAudio {
  private const val TAG = "GameAudio"
  private const val SAMPLE_RATE = 22050
  private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

  var soundVolume: Float = 0.8f
    set(value) {
      field = value.coerceIn(0f, 1f)
    }

  var musicVolume: Float = 0.6f
    set(value) {
      field = value.coerceIn(0f, 1f)
      updateMusicVolume()
    }

  var isMuted: Boolean = false
    set(value) {
      field = value
      updateMusicVolume()
    }

  private val sfxTracks = ConcurrentHashMap<SoundEffect, List<AudioTrack>>()
  private val trackIndex = ConcurrentHashMap<SoundEffect, Int>()
  private var bgmTrack: AudioTrack? = null
  private var isBgmPlaying = false
  private var isInitialized = false

  enum class SoundEffect {
    JUMP,
    SHOOT,
    FREEZE_HIT,
    ORB_KICK,
    CHAIN_CRUSH,
    COIN,
    POWER_UP,
    LETTER_COLLECT,
    BOSS_HIT,
    BOSS_DEFEAT,
    LEVEL_CLEAR,
    GAME_OVER
  }

  fun init(context: Context) {
    if (isInitialized) return
    isInitialized = true

    scope.launch {
      try {
        val sfxAttributes = AudioAttributes.Builder()
          .setUsage(AudioAttributes.USAGE_GAME)
          .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
          .build()

        val audioFormat = AudioFormat.Builder()
          .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
          .setSampleRate(SAMPLE_RATE)
          .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
          .build()

        // Initialize in-memory static AudioTracks for all 12 sound effects
        // Rapid fire effects get 2 alternating voice tracks for smooth overlapping
        for (effect in SoundEffect.entries) {
          try {
            val samples = generateSamplesForEffect(effect)
            val voiceCount = when (effect) {
              SoundEffect.SHOOT, SoundEffect.JUMP, SoundEffect.ORB_KICK -> 2
              else -> 1
            }

            val tracks = mutableListOf<AudioTrack>()
            for (i in 0 until voiceCount) {
              val track = AudioTrack.Builder()
                .setAudioAttributes(sfxAttributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(samples.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

              track.write(samples, 0, samples.size)
              tracks.add(track)
            }
            sfxTracks[effect] = tracks
            trackIndex[effect] = 0
          } catch (e: Throwable) {
            Log.w(TAG, "Failed creating static AudioTrack for $effect: ${e.message}")
          }
        }

        // Initialize pure in-memory looping arcade BGM track
        try {
          val bgmSamples = generateBgmLoopSamples()
          val bgmAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

          val track = AudioTrack.Builder()
            .setAudioAttributes(bgmAttributes)
            .setAudioFormat(audioFormat)
            .setBufferSizeInBytes(bgmSamples.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

          track.write(bgmSamples, 0, bgmSamples.size)
          track.setLoopPoints(0, bgmSamples.size, -1) // Seamless infinite hardware loop
          bgmTrack = track

          updateMusicVolume()
          if (isBgmPlaying) {
            track.play()
          }
        } catch (e: Throwable) {
          Log.w(TAG, "Failed creating BGM AudioTrack: ${e.message}")
        }
      } catch (e: Throwable) {
        Log.e(TAG, "GameAudio init error: ${e.message}")
      }
    }
  }

  fun playSound(effect: SoundEffect) {
    if (isMuted || soundVolume <= 0.01f) return
    val tracks = sfxTracks[effect] ?: return
    try {
      val curIdx = trackIndex[effect] ?: 0
      val track = tracks[curIdx % tracks.size]
      trackIndex[effect] = curIdx + 1

      track.stop()
      track.reloadStaticData()
      track.setVolume(soundVolume)
      track.play()
    } catch (_: Throwable) {}
  }

  fun startBgm() {
    isBgmPlaying = true
    val track = bgmTrack ?: return
    try {
      updateMusicVolume()
      if (track.playState != AudioTrack.PLAYSTATE_PLAYING) {
        track.play()
      }
    } catch (_: Throwable) {}
  }

  fun stopBgm() {
    isBgmPlaying = false
    val track = bgmTrack ?: return
    try {
      if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
        track.pause()
        track.reloadStaticData()
      }
    } catch (_: Throwable) {}
  }

  fun pauseBgm() {
    val track = bgmTrack ?: return
    try {
      if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
        track.pause()
      }
    } catch (_: Throwable) {}
  }

  fun resumeBgm() {
    if (isBgmPlaying) {
      val track = bgmTrack ?: return
      try {
        updateMusicVolume()
        if (track.playState != AudioTrack.PLAYSTATE_PLAYING) {
          track.play()
        }
      } catch (_: Throwable) {}
    }
  }

  private fun updateMusicVolume() {
    val vol = if (isMuted) 0f else (musicVolume * 0.45f)
    try {
      bgmTrack?.setVolume(vol)
    } catch (_: Throwable) {}
  }

  fun release() {
    try {
      bgmTrack?.stop()
      bgmTrack?.release()
      bgmTrack = null

      for (tracks in sfxTracks.values) {
        for (track in tracks) {
          try {
            track.stop()
            track.release()
          } catch (_: Throwable) {}
        }
      }
      sfxTracks.clear()
      trackIndex.clear()
      isInitialized = false
    } catch (_: Throwable) {}
  }

  private fun generateBgmLoopSamples(): ShortArray {
    // 4.0 seconds arcade chiptune loop (8 measures of 0.5s each)
    val durationSeconds = 4.0f
    val totalSamples = (SAMPLE_RATE * durationSeconds).toInt()
    val buffer = ShortArray(totalSamples)

    val melodyNotes = floatArrayOf(
      261.63f, 329.63f, 392.00f, 523.25f, // C4, E4, G4, C5
      392.00f, 440.00f, 493.88f, 523.25f, // G4, A4, B4, C5
      349.23f, 440.00f, 523.25f, 659.25f, // F4, A4, C5, E5
      392.00f, 493.88f, 587.33f, 783.99f  // G4, B4, D5, G5
    )
    val bassNotes = floatArrayOf(
      130.81f, 130.81f, 130.81f, 164.81f, // C3, E3
      174.61f, 174.61f, 196.00f, 196.00f  // F3, G3
    )

    val noteSamples = totalSamples / melodyNotes.size
    val bassNoteSamples = totalSamples / bassNotes.size

    for (i in 0 until totalSamples) {
      val melIdx = (i / noteSamples).coerceIn(0, melodyNotes.size - 1)
      val melFreq = melodyNotes[melIdx]
      val melPhase = 2.0 * Math.PI * melFreq * (i.toDouble() / SAMPLE_RATE)
      val melEnv = 1.0 - ((i % noteSamples).toDouble() / noteSamples) * 0.4
      val melSquare = if (sin(melPhase) > 0) 6000.0 * melEnv else -6000.0 * melEnv

      val bassIdx = (i / bassNoteSamples).coerceIn(0, bassNotes.size - 1)
      val bassFreq = bassNotes[bassIdx]
      val bassPhase = 2.0 * Math.PI * bassFreq * (i.toDouble() / SAMPLE_RATE)
      val bassTriangle = sin(bassPhase) * 7000.0

      val mixed = (melSquare + bassTriangle).toInt().coerceIn(-32767, 32767)
      buffer[i] = mixed.toShort()
    }
    return buffer
  }

  private fun generateSamplesForEffect(effect: SoundEffect): ShortArray {
    return when (effect) {
      SoundEffect.JUMP -> {
        // Fast ascending sweep: 240Hz -> 540Hz over 110ms
        val durationMs = 110
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        var phase = 0.0
        for (i in 0 until numSamples) {
          val progress = i.toDouble() / numSamples
          val freq = 240.0 + progress * 300.0
          val amp = (1.0 - progress * 0.6) * 16000.0
          phase += 2.0 * Math.PI * freq / SAMPLE_RATE
          buffer[i] = (sin(phase) * amp).toInt().toShort()
        }
        buffer
      }
      SoundEffect.SHOOT -> {
        // Frost whoosh projectile throw: descending tone + crisp white noise
        val durationMs = 90
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        var phase = 0.0
        for (i in 0 until numSamples) {
          val progress = i.toDouble() / numSamples
          val freq = 850.0 - progress * 450.0
          val noise = (Math.random() * 2.0 - 1.0) * 7000.0 * (1.0 - progress)
          phase += 2.0 * Math.PI * freq / SAMPLE_RATE
          val sine = sin(phase) * 8000.0 * (1.0 - progress)
          buffer[i] = (sine + noise).toInt().coerceIn(-32767, 32767).toShort()
        }
        buffer
      }
      SoundEffect.FREEZE_HIT -> {
        // High crystalline impact bell ping (1250Hz + 2500Hz)
        val durationMs = 130
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        var phase1 = 0.0
        var phase2 = 0.0
        for (i in 0 until numSamples) {
          val progress = i.toDouble() / numSamples
          val env = (1.0 - progress) * (1.0 - progress) * 18000.0
          phase1 += 2.0 * Math.PI * 1250.0 / SAMPLE_RATE
          phase2 += 2.0 * Math.PI * 2500.0 / SAMPLE_RATE
          buffer[i] = ((sin(phase1) * 0.7 + sin(phase2) * 0.3) * env).toInt().toShort()
        }
        buffer
      }
      SoundEffect.ORB_KICK -> {
        // Heavy punchy kick impact (180Hz -> 50Hz)
        val durationMs = 120
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        var phase = 0.0
        for (i in 0 until numSamples) {
          val progress = i.toDouble() / numSamples
          val freq = 180.0 - progress * 130.0
          val env = (1.0 - progress) * 22000.0
          phase += 2.0 * Math.PI * freq / SAMPLE_RATE
          buffer[i] = (sin(phase) * env).toInt().toShort()
        }
        buffer
      }
      SoundEffect.CHAIN_CRUSH -> {
        // Shattering snow explosion (crunchy burst)
        val durationMs = 180
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
          val progress = i.toDouble() / numSamples
          val noise = (Math.random() * 2.0 - 1.0) * 20000.0 * (1.0 - progress * 0.8)
          buffer[i] = noise.toInt().coerceIn(-32767, 32767).toShort()
        }
        buffer
      }
      SoundEffect.COIN -> {
        // Dual-tone arcade coin chime
        val part1 = (SAMPLE_RATE * 45) / 1000
        val part2 = (SAMPLE_RATE * 80) / 1000
        val buffer = ShortArray(part1 + part2)
        var phase = 0.0
        for (i in 0 until part1) {
          phase += 2.0 * Math.PI * 987.77 / SAMPLE_RATE
          buffer[i] = (sin(phase) * 16000.0).toInt().toShort()
        }
        for (i in 0 until part2) {
          val progress = i.toDouble() / part2
          phase += 2.0 * Math.PI * 1318.51 / SAMPLE_RATE
          buffer[part1 + i] = (sin(phase) * (1.0 - progress) * 16000.0).toInt().toShort()
        }
        buffer
      }
      SoundEffect.POWER_UP -> {
        // Ascending 4-note chord: C5, E5, G5, C6
        val noteDur = (SAMPLE_RATE * 55) / 1000
        val freqs = doubleArrayOf(523.25, 659.25, 783.99, 1046.50)
        val buffer = ShortArray(noteDur * freqs.size)
        var offset = 0
        for (f in freqs) {
          var phase = 0.0
          for (i in 0 until noteDur) {
            phase += 2.0 * Math.PI * f / SAMPLE_RATE
            buffer[offset + i] = (sin(phase) * 17000.0).toInt().toShort()
          }
          offset += noteDur
        }
        buffer
      }
      SoundEffect.LETTER_COLLECT -> {
        // Sparkly high chord
        val durationMs = 150
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        var p1 = 0.0
        var p2 = 0.0
        for (i in 0 until numSamples) {
          val progress = i.toDouble() / numSamples
          p1 += 2.0 * Math.PI * 1760.0 / SAMPLE_RATE
          p2 += 2.0 * Math.PI * 2200.0 / SAMPLE_RATE
          val amp = (1.0 - progress) * 15000.0
          buffer[i] = ((sin(p1) + sin(p2)) * 0.5 * amp).toInt().toShort()
        }
        buffer
      }
      SoundEffect.BOSS_HIT -> {
        // Heavy bass crunch
        val durationMs = 140
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        var phase = 0.0
        for (i in 0 until numSamples) {
          val progress = i.toDouble() / numSamples
          val freq = 120.0 - progress * 60.0
          val noise = (Math.random() * 2.0 - 1.0) * 10000.0
          phase += 2.0 * Math.PI * freq / SAMPLE_RATE
          val sine = sin(phase) * 18000.0 * (1.0 - progress)
          buffer[i] = (sine + noise).toInt().coerceIn(-32767, 32767).toShort()
        }
        buffer
      }
      SoundEffect.BOSS_DEFEAT, SoundEffect.LEVEL_CLEAR -> {
        // Victory fanfare
        val noteDur = (SAMPLE_RATE * 80) / 1000
        val freqs = doubleArrayOf(523.25, 659.25, 783.99, 1046.50, 1318.51)
        val buffer = ShortArray(noteDur * freqs.size)
        var offset = 0
        for (f in freqs) {
          var phase = 0.0
          for (i in 0 until noteDur) {
            val prog = i.toDouble() / noteDur
            phase += 2.0 * Math.PI * f / SAMPLE_RATE
            buffer[offset + i] = (sin(phase) * (1.0 - prog * 0.3) * 18000.0).toInt().toShort()
          }
          offset += noteDur
        }
        buffer
      }
      SoundEffect.GAME_OVER -> {
        // Descending melancholy tone
        val noteDur = (SAMPLE_RATE * 120) / 1000
        val freqs = doubleArrayOf(440.0, 392.0, 349.23, 293.66)
        val buffer = ShortArray(noteDur * freqs.size)
        var offset = 0
        for (f in freqs) {
          var phase = 0.0
          for (i in 0 until noteDur) {
            val prog = i.toDouble() / noteDur
            phase += 2.0 * Math.PI * f / SAMPLE_RATE
            buffer[offset + i] = (sin(phase) * (1.0 - prog) * 15000.0).toInt().toShort()
          }
          offset += noteDur
        }
        buffer
      }
    }
  }
}
