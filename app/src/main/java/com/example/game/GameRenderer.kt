package com.example.game

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

object GameRenderer {
  // Reusable Path to avoid object allocations in onDraw loop
  private val sharedPath = Path()

  fun renderGame(
    drawScope: DrawScope,
    engine: GameEngine,
    scanlinesEnabled: Boolean,
    textMeasurer: TextMeasurer
  ) {
    val scaleX = drawScope.size.width / LevelCatalog.GAME_WIDTH
    val scaleY = drawScope.size.height / LevelCatalog.GAME_HEIGHT
    val scale = minOf(scaleX, scaleY)

    val renderWidth = LevelCatalog.GAME_WIDTH * scale
    val renderHeight = LevelCatalog.GAME_HEIGHT * scale
    val offsetX = (drawScope.size.width - renderWidth) / 2f
    val offsetY = (drawScope.size.height - renderHeight) / 2f

    // Deep arcade backdrop
    drawScope.drawRect(Color(0xFF040812), Offset.Zero, drawScope.size)

    drawScope.clipRect(offsetX, offsetY, offsetX + renderWidth, offsetY + renderHeight) {
      // 1. Dynamic World Theme Level Background
      val theme = engine.currentLevel.theme
      val isBoss = engine.currentLevel.isBossLevel
      val bgBrush = Brush.verticalGradient(
        listOf(Color(theme.bgTop), Color(theme.bgMid), Color(theme.bgBot)),
        startY = offsetY,
        endY = offsetY + renderHeight
      )
      drawRect(bgBrush, Offset(offsetX, offsetY), Size(renderWidth, renderHeight))

      // Distant cavern icicles/stalactites in background
      drawDistantCavern(this, offsetX, offsetY, scale, isBoss, theme.accentColor)

      // 2. Platforms (Obstacles with crisp high contrast, 3D brick definition, and glowing snow caps)
      val brickColor = Color(theme.brickColor)
      val accentColor = Color(theme.accentColor)
      val snowCapHeight = 6f * scale

      for (plat in engine.currentLevel.platforms) {
        val px = offsetX + plat.x * scale
        val py = offsetY + plat.y * scale
        val pw = plat.width * scale
        val ph = plat.height * scale

        // Platform depth shadow for sharp contrast against background
        drawRoundRect(
          Color(0xFF02040A).copy(alpha = 0.65f),
          Offset(px, py + 3f * scale),
          Size(pw, ph),
          CornerRadius(4f * scale)
        )

        // Platform base brick
        drawRoundRect(
          brickColor,
          Offset(px, py),
          Size(pw, ph),
          CornerRadius(4f * scale)
        )

        // Arcade Chevron / Zigzag pattern on platform face (matching Snow Bros arcade screenshot)
        val chevronStep = 18f * scale
        var cx = px
        var isAlt = false
        while (cx < px + pw - 2f * scale) {
          val segW = chevronStep.coerceAtMost(px + pw - cx)
          val chevronColor = if (isAlt) Color(0xFFFFD700).copy(alpha = 0.85f) else Color(0xFF00E5FF).copy(alpha = 0.5f)
          sharedPath.reset()
          sharedPath.moveTo(cx, py + snowCapHeight)
          sharedPath.lineTo(cx + segW / 2f, py + ph)
          sharedPath.lineTo(cx + segW, py + snowCapHeight)
          sharedPath.close()
          drawPath(sharedPath, chevronColor)
          cx += segW
          isAlt = !isAlt
        }

        // Platform crisp neon border highlight
        drawRoundRect(
          accentColor,
          Offset(px, py),
          Size(pw, ph),
          CornerRadius(4f * scale),
          style = Stroke(width = 1.8f * scale)
        )

        // Snow top ledge - high contrast brilliant white / cyan crust
        drawRoundRect(
          Color.White,
          Offset(px, py),
          Size(pw, snowCapHeight),
          CornerRadius(3f * scale)
        )
        drawLine(
          Color(0xFF00E5FF),
          Offset(px + 1f * scale, py + snowCapHeight),
          Offset(px + pw - 1f * scale, py + snowCapHeight),
          strokeWidth = 2f * scale
        )
      }

      // 2b. Obstacle Hazards (Wall Gargoyles/Totems that attack, matching user screenshot)
      for (hazard in engine.obstacleHazards) {
        drawObstacleHazard(this, hazard, offsetX, offsetY, scale, sharedPath)
      }

      // 2c. Hazard Fireballs (Flames spat by green/multi-color obstacles)
      for (fireball in engine.hazardFireballs) {
        drawHazardFireball(this, fireball, offsetX, offsetY, scale, sharedPath)
      }

      // 3. Falling Icicles (Boss hazards)
      for (ic in engine.icicles) {
        val ix = offsetX + ic.x * scale
        val iy = offsetY + ic.y * scale
        val isz = ic.size * scale

        sharedPath.reset()
        sharedPath.moveTo(ix, iy + isz)
        sharedPath.lineTo(ix - isz * 0.35f, iy)
        sharedPath.lineTo(ix + isz * 0.35f, iy)
        sharedPath.close()

        drawPath(sharedPath, Color(0xFF00E5FF))
        drawPath(sharedPath, Color.White, style = Stroke(width = 1.2f * scale))
      }

      // 4. Pickups (Coins, Gems, FROST Letters, Power-ups)
      for (item in engine.pickups) {
        val ix = offsetX + item.x * scale
        val iy = offsetY + item.y * scale
        val sz = item.size * scale
        drawPickup(this, item.type, ix, iy, sz, scale, textMeasurer)
      }

      // 5. Enemies & Snow Orbs
      for (e in engine.enemies) {
        if (e.isEliminated) continue
        val ex = offsetX + e.x * scale
        val ey = offsetY + e.y * scale
        val ew = e.width * scale
        val eh = e.height * scale

        if (e.isRolling) {
          // Rolling giant Snow Orb
          drawRollingSnowOrb(this, ex, ey, ew, eh, e.rollTimer, scale)
        } else if (e.isFullyFrozen) {
          // Stationary Snow Orb with freeze shake
          val shake = if (e.freezeShakeTimer > 5f) (sin(e.freezeShakeTimer * 25f) * 2f * scale) else 0f
          drawSnowOrb(this, ex + shake, ey, ew, eh, scale, textMeasurer)
        } else {
          // Active enemy
          drawEnemy(this, e, ex, ey, ew, eh, scale)

          // Partial freeze crystallization overlay
          if (e.freezeProgress > 0f) {
            val freezeAlpha = (e.freezeProgress / 100f).coerceIn(0.25f, 0.85f)
            val center = Offset(ex + ew / 2f, ey + eh / 2f)
            drawCircle(
              Color(0xFF00E5FF).copy(alpha = freezeAlpha),
              radius = (ew / 2f) * 1.15f,
              center = center
            )
            drawCircle(
              Color.White.copy(alpha = freezeAlpha * 0.75f),
              radius = (ew / 2f) * 0.95f,
              center = center
            )
            drawCircle(
              Color(0xFF0288D1),
              radius = (ew / 2f) * 1.15f,
              center = center,
              style = Stroke(width = 1.5f * scale)
            )
          }
        }
      }

      // 6. Projectiles (Frost shots & Blast shots)
      for (proj in engine.projectiles) {
        val px = offsetX + proj.x * scale
        val py = offsetY + proj.y * scale
        val pr = proj.radius * scale
        val pColor = if (proj.isBlast) Color(0xFFFF5722) else Color(0xFF00E5FF)

        drawCircle(pColor, radius = pr, center = Offset(px, py))
        drawCircle(Color.White, radius = pr * 0.55f, center = Offset(px, py))
        drawCircle(Color.White, radius = pr, center = Offset(px, py), style = Stroke(width = 1.2f * scale))
      }

      // 7. Players (Glint P1 & Ember P2)
      for (p in engine.players) {
        if (p.isDead) continue
        // Invincibility flashing
        if (p.invincibleTimer > 0f && (p.invincibleTimer * 12f).toInt() % 2 == 0) continue

        val px = offsetX + p.x * scale
        val py = offsetY + p.y * scale
        val pw = p.width * scale
        val ph = p.height * scale
        val isP2 = p.id == 2

        drawPlayer(this, p, px, py, pw, ph, isP2, scale, textMeasurer)
      }

      // 8. Particles
      for (part in engine.particles) {
        val px = offsetX + part.x * scale
        val py = offsetY + part.y * scale
        val alpha = (part.life / part.maxLife).coerceIn(0f, 1f)
        drawCircle(
          Color(part.color).copy(alpha = alpha),
          radius = part.size * scale,
          center = Offset(px, py)
        )
      }

      // 9. Floating Score & Feedback Texts
      for (ft in engine.floatingTexts) {
        val tx = offsetX + ft.x * scale
        val ty = offsetY + ft.y * scale
        val alpha = ft.life.coerceIn(0f, 1f)
        drawText(
          textMeasurer = textMeasurer,
          text = ft.text,
          topLeft = Offset(tx, ty),
          style = TextStyle(
            color = Color(ft.color).copy(alpha = alpha),
            fontSize = (11f * scale).sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
          )
        )
      }

      // 10. Boss Health Bar in Boss Stages
      val boss = engine.enemies.find { it.type == EnemyType.CRYO_TITAN && !it.isEliminated }
      if (boss != null) {
        drawBossHealthBar(this, boss, offsetX, offsetY, renderWidth, scale, textMeasurer)
      }

      // 11. "READY!" Countdown Overlay
      if (engine.isReadyCountdown > 0f) {
        val text = "READY!"
        val style = TextStyle(
          color = Color(0xFFFFD93D),
          fontSize = (26f * scale).sp,
          fontWeight = FontWeight.Black,
          fontFamily = FontFamily.Monospace
        )
        val measure = textMeasurer.measure(text, style)
        val cx = offsetX + (renderWidth - measure.size.width) / 2f
        val cy = offsetY + (renderHeight - measure.size.height) / 2f - 20f * scale
        drawText(textMeasurer, text, Offset(cx, cy), style)
      }

      // 12. Lightweight scanline overlay (if enabled in settings)
      if (scanlinesEnabled) {
        // Fast 40-step scanline pass instead of 300 to eliminate stutter
        val step = 15f * scale
        var y = offsetY
        while (y < offsetY + renderHeight) {
          drawLine(
            color = Color.Black.copy(alpha = 0.12f),
            start = Offset(offsetX, y),
            end = Offset(offsetX + renderWidth, y),
            strokeWidth = 1f * scale
          )
          y += step
        }
      }

      // Crisp Dual-Tone Arcade Frame Border (Cyan inner neon + Outer Gold)
      drawRect(
        Color(0xFF00E5FF).copy(alpha = 0.75f),
        Offset(offsetX, offsetY),
        Size(renderWidth, renderHeight),
        style = Stroke(width = 1.5f * scale)
      )
      drawRect(
        Color(0xFFFFD93D),
        Offset(offsetX, offsetY),
        Size(renderWidth, renderHeight),
        style = Stroke(width = 2.5f * scale)
      )
    }
  }

  private fun drawDistantCavern(
    drawScope: DrawScope,
    ox: Float,
    oy: Float,
    scale: Float,
    isBoss: Boolean,
    accentColor: Long
  ) {
    val stalactiteColor = if (isBoss) Color(0xFF2E1245).copy(alpha = 0.45f) else Color(accentColor).copy(alpha = 0.2f)
    for (i in 0..7) {
      val x = ox + (i * 50f + 20f) * scale
      val h = (24f + (i % 3) * 14f) * scale
      sharedPath.reset()
      sharedPath.moveTo(x - 10f * scale, oy)
      sharedPath.lineTo(x + 10f * scale, oy)
      sharedPath.lineTo(x, oy + h)
      sharedPath.close()
      drawScope.drawPath(sharedPath, stalactiteColor)
    }
  }

  private fun drawPlayer(
    drawScope: DrawScope,
    p: Player,
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    isP2: Boolean,
    scale: Float,
    textMeasurer: TextMeasurer
  ) {
    val primaryColor = if (isP2) Color(0xFFFF5252) else Color(0xFF00E5FF) // High-contrast Red or Cyan
    val suitColor = if (isP2) Color(0xFFFF6E40) else Color(0xFF40C4FF)
    val trimColor = if (isP2) Color(0xFFFFD700) else Color(0xFF041828)

    // 1. Radiant Player Silhouette Aura so player is crystal-clear and immediately noticeable
    drawScope.drawCircle(
      primaryColor.copy(alpha = 0.28f),
      radius = w * 0.72f,
      center = Offset(x + w / 2f, y + h / 2f)
    )

    // Giant Mode Golden Glow Aura
    if (p.giantTimer > 0f) {
      drawScope.drawCircle(
        Color(0xFFFFD700).copy(alpha = 0.45f),
        radius = w * 0.95f,
        center = Offset(x + w / 2f, y + h / 2f)
      )
    }

    // Speed boost trail
    if (p.speedTimer > 0f) {
      val trailDir = if (p.facingRight) -8f else 8f
      drawScope.drawCircle(
        Color(0xFF00E5FF).copy(alpha = 0.45f),
        radius = w * 0.55f,
        center = Offset(x + trailDir * scale, y + h / 2f)
      )
    }

    // Overhead Player Indicator Tag (1P / 2P) - crisp and glowing
    val tagText = if (isP2) "2P" else "1P"
    val tagStyle = TextStyle(
      color = Color(0xFF040812),
      fontSize = (8f * scale).sp,
      fontWeight = FontWeight.Black,
      fontFamily = FontFamily.Monospace
    )
    val tagMeasure = textMeasurer.measure(tagText, tagStyle)
    val tagCenterX = x + w / 2f
    val tagY = y - 13f * scale
    val tagWidth = tagMeasure.size.width + 6f * scale
    val tagHeight = tagMeasure.size.height + 2f * scale
    drawScope.drawRoundRect(
      primaryColor,
      Offset(tagCenterX - tagWidth / 2f, tagY),
      Size(tagWidth, tagHeight),
      CornerRadius(3f * scale)
    )
    drawScope.drawRoundRect(
      Color.White,
      Offset(tagCenterX - tagWidth / 2f, tagY),
      Size(tagWidth, tagHeight),
      CornerRadius(3f * scale),
      style = Stroke(width = 1f * scale)
    )
    drawScope.drawText(
      textMeasurer,
      tagText,
      Offset(tagCenterX - tagMeasure.size.width / 2f, tagY + 1f * scale),
      tagStyle
    )

    // Body shadow for depth and visibility
    drawScope.drawCircle(
      Color.Black.copy(alpha = 0.45f),
      radius = w * 0.45f,
      center = Offset(x + w / 2f, y + h - 1.5f * scale)
    )

    // Body (Hero Winter Parka)
    val bodyRect = Offset(x + w * 0.16f, y + h * 0.32f)
    val bodySize = Size(w * 0.68f, h * 0.48f)
    drawScope.drawRoundRect(
      suitColor,
      bodyRect,
      bodySize,
      CornerRadius(5f * scale)
    )
    // High-contrast outer body outline
    drawScope.drawRoundRect(
      Color.White,
      bodyRect,
      bodySize,
      CornerRadius(5f * scale),
      style = Stroke(width = 1.6f * scale)
    )

    // Belt / Trim
    drawScope.drawRect(
      trimColor,
      Offset(x + w * 0.16f, y + h * 0.58f),
      Size(w * 0.68f, 3.5f * scale)
    )

    // Head / Helmet
    val headRadius = w * 0.33f
    val headCenter = Offset(x + w * 0.5f, y + h * 0.28f)
    drawScope.drawCircle(primaryColor, radius = headRadius, center = headCenter)
    drawScope.drawCircle(Color.White, radius = headRadius, center = headCenter, style = Stroke(width = 1.6f * scale))

    // Visor / Face
    val visorDir = if (p.facingRight) 2.5f * scale else -2.5f * scale
    drawScope.drawCircle(
      trimColor,
      radius = headRadius * 0.62f,
      center = Offset(headCenter.x + visorDir, headCenter.y)
    )
    // Bright glowing eyes
    drawScope.drawCircle(
      Color.White,
      radius = 2.8f * scale,
      center = Offset(headCenter.x + visorDir + (if (p.facingRight) 1.2f else -1.2f) * scale, headCenter.y - 1f * scale)
    )
    drawScope.drawCircle(
      primaryColor,
      radius = 1.3f * scale,
      center = Offset(headCenter.x + visorDir + (if (p.facingRight) 1.5f else -0.9f) * scale, headCenter.y - 1f * scale)
    )

    // Kick Animation or Regular Feet
    val legY = y + h * 0.8f
    val legW = 6.5f * scale
    val legH = 7f * scale
    if (p.kickAnimTimer > 0f) {
      val kickDir = if (p.facingRight) 1f else -1f
      drawScope.drawRoundRect(
        trimColor,
        Offset(x + w * 0.5f + (kickDir * 6f * scale), legY),
        Size(legW * 1.6f, legH),
        CornerRadius(2.5f * scale)
      )
      drawScope.drawRoundRect(
        Color.White,
        Offset(x + w * 0.5f + (kickDir * 6f * scale), legY),
        Size(legW * 1.6f, legH),
        CornerRadius(2.5f * scale),
        style = Stroke(width = 1.2f * scale)
      )
    } else {
      drawScope.drawRoundRect(trimColor, Offset(x + w * 0.20f, legY), Size(legW, legH), CornerRadius(2.5f * scale))
      drawScope.drawRoundRect(Color.White, Offset(x + w * 0.20f, legY), Size(legW, legH), CornerRadius(2.5f * scale), style = Stroke(width = 1f * scale))
      drawScope.drawRoundRect(trimColor, Offset(x + w * 0.58f, legY), Size(legW, legH), CornerRadius(2.5f * scale))
      drawScope.drawRoundRect(Color.White, Offset(x + w * 0.58f, legY), Size(legW, legH), CornerRadius(2.5f * scale), style = Stroke(width = 1f * scale))
    }
  }

  private fun drawEnemy(
    drawScope: DrawScope,
    e: Enemy,
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    scale: Float
  ) {
    val center = Offset(x + w / 2f, y + h / 2f)

    when (e.type) {
      EnemyType.WISP -> {
        // Red Demon Wisp
        val bodyColor = Color(0xFFFF2A4B)
        drawScope.drawCircle(bodyColor, radius = w * 0.44f, center = center)
        drawScope.drawCircle(Color.Black.copy(alpha = 0.3f), radius = w * 0.44f, center = center, style = Stroke(width = 1.2f * scale))

        // Golden Horns
        val hornY = y + h * 0.12f
        drawScope.drawCircle(Color(0xFFFFD93D), radius = 3.2f * scale, center = Offset(x + w * 0.25f, hornY))
        drawScope.drawCircle(Color(0xFFFFD93D), radius = 3.2f * scale, center = Offset(x + w * 0.75f, hornY))

        // Expressive Eyes
        val eyeDir = if (e.facingRight) 2.5f * scale else -2.5f * scale
        drawScope.drawCircle(Color.White, radius = 4f * scale, center = Offset(x + w * 0.38f + eyeDir, y + h * 0.45f))
        drawScope.drawCircle(Color.White, radius = 4f * scale, center = Offset(x + w * 0.62f + eyeDir, y + h * 0.45f))
        drawScope.drawCircle(Color.Black, radius = 2f * scale, center = Offset(x + w * 0.38f + eyeDir, y + h * 0.45f))
        drawScope.drawCircle(Color.Black, radius = 2f * scale, center = Offset(x + w * 0.62f + eyeDir, y + h * 0.45f))
      }

      EnemyType.SKIMMER -> {
        // Electric Blue Flying Bat
        val bodyColor = Color(0xFF2979FF)
        drawScope.drawOval(bodyColor, Offset(x + w * 0.25f, y + h * 0.25f), Size(w * 0.5f, h * 0.5f))

        // Wings using reusable path
        val wingY = y + h * 0.2f
        sharedPath.reset()
        sharedPath.moveTo(x, wingY)
        sharedPath.lineTo(x + w * 0.35f, y + h * 0.5f)
        sharedPath.lineTo(x, y + h * 0.7f)
        sharedPath.close()
        drawScope.drawPath(sharedPath, Color(0xFF82B1FF))

        sharedPath.reset()
        sharedPath.moveTo(x + w, wingY)
        sharedPath.lineTo(x + w * 0.65f, y + h * 0.5f)
        sharedPath.lineTo(x + w, y + h * 0.7f)
        sharedPath.close()
        drawScope.drawPath(sharedPath, Color(0xFF82B1FF))

        // Glowing Eyes
        drawScope.drawCircle(Color(0xFFFFD93D), radius = 2.5f * scale, center = Offset(x + w * 0.42f, y + h * 0.45f))
        drawScope.drawCircle(Color(0xFFFFD93D), radius = 2.5f * scale, center = Offset(x + w * 0.58f, y + h * 0.45f))
      }

      EnemyType.SPIKELING -> {
        // Armored crawler with spikes
        val bodyColor = Color(0xFFFFB300)
        drawScope.drawRoundRect(
          bodyColor,
          Offset(x, y + h * 0.35f),
          Size(w, h * 0.6f),
          CornerRadius(4f * scale)
        )
        // Spikes on top
        for (i in 0..2) {
          val sx = x + (i * 8f + 4f) * scale
          sharedPath.reset()
          sharedPath.moveTo(sx, y + h * 0.35f)
          sharedPath.lineTo(sx + 3f * scale, y + h * 0.12f)
          sharedPath.lineTo(sx + 6f * scale, y + h * 0.35f)
          sharedPath.close()
          drawScope.drawPath(sharedPath, Color(0xFFFF6F00))
        }
        // Small legs
        drawScope.drawRect(Color(0xFFE65100), Offset(x + w * 0.2f, y + h * 0.9f), Size(4f * scale, 3f * scale))
        drawScope.drawRect(Color(0xFFE65100), Offset(x + w * 0.7f, y + h * 0.9f), Size(4f * scale, 3f * scale))
      }

      EnemyType.PHANTOM -> {
        // Spectral Crimson Wraith
        val bodyColor = Color(0xFFFF1744)
        drawScope.drawCircle(bodyColor.copy(alpha = 0.88f), radius = w * 0.45f, center = center)
        drawScope.drawCircle(Color(0xFFFF80AB).copy(alpha = 0.4f), radius = w * 0.55f, center = center)

        // Piercing Eyes
        drawScope.drawCircle(Color(0xFFFFEB3B), radius = 3.5f * scale, center = Offset(x + w * 0.35f, y + h * 0.45f))
        drawScope.drawCircle(Color(0xFFFFEB3B), radius = 3.5f * scale, center = Offset(x + w * 0.65f, y + h * 0.45f))
      }

      EnemyType.CRYO_TITAN -> {
        // Massive Boss Cryo-Titan
        val titanColor = if (e.bossPhase == 3) Color(0xFFFF3D00) else Color(0xFF1E88E5)
        drawScope.drawRoundRect(
          titanColor,
          Offset(x, y),
          Size(w, h),
          CornerRadius(14f * scale)
        )
        drawScope.drawRoundRect(
          Color.White,
          Offset(x, y),
          Size(w, h),
          CornerRadius(14f * scale),
          style = Stroke(width = 2f * scale)
        )

        // Frost Spikes on shoulders using reusable path
        sharedPath.reset()
        sharedPath.moveTo(x - 10f * scale, y + 20f * scale)
        sharedPath.lineTo(x, y)
        sharedPath.lineTo(x + 10f * scale, y + 25f * scale)
        sharedPath.close()
        drawScope.drawPath(sharedPath, Color(0xFF00E5FF))

        sharedPath.reset()
        sharedPath.moveTo(x + w + 10f * scale, y + 20f * scale)
        sharedPath.lineTo(x + w, y)
        sharedPath.lineTo(x + w - 10f * scale, y + 25f * scale)
        sharedPath.close()
        drawScope.drawPath(sharedPath, Color(0xFF00E5FF))

        // Glowing Core / Reactor
        val coreColor = when (e.bossPhase) {
          3 -> Color(0xFFFF1744)
          2 -> Color(0xFFFF9100)
          else -> Color(0xFF00E5FF)
        }
        drawScope.drawCircle(coreColor, radius = 9f * scale, center = Offset(x + w / 2f, y + h * 0.65f))

        // Glowing Boss Eyes
        drawScope.drawCircle(Color(0xFFFFD93D), radius = 6f * scale, center = Offset(x + w * 0.35f, y + h * 0.35f))
        drawScope.drawCircle(Color(0xFFFFD93D), radius = 6f * scale, center = Offset(x + w * 0.65f, y + h * 0.35f))
      }
    }
  }

  private fun drawSnowOrb(
    drawScope: DrawScope,
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    scale: Float,
    textMeasurer: TextMeasurer
  ) {
    val radius = maxOf(w, h) * 0.55f
    val center = Offset(x + w / 2f, y + h / 2f)

    // Solid glistening snow sphere
    drawScope.drawCircle(Color(0xFFE1F5FE), radius = radius, center = center)
    drawScope.drawCircle(Color.White, radius = radius * 0.85f, center = center)
    drawScope.drawCircle(Color(0xFF0288D1), radius = radius, center = center, style = Stroke(width = 2.5f * scale))

    // Frozen face imprint inside orb
    drawScope.drawCircle(Color(0xFF0288D1), radius = 2.5f * scale, center = Offset(center.x - 4f * scale, center.y - 2f * scale))
    drawScope.drawCircle(Color(0xFF0288D1), radius = 2.5f * scale, center = Offset(center.x + 4f * scale, center.y - 2f * scale))

    // "KICK!" badge indicator above snow orb so player knows what to do!
    val kickText = "KICK!"
    val kickStyle = TextStyle(
      color = Color(0xFFFFD93D),
      fontSize = (8f * scale).sp,
      fontWeight = FontWeight.Black,
      fontFamily = FontFamily.Monospace
    )
    val measure = textMeasurer.measure(kickText, kickStyle)
    drawScope.drawText(
      textMeasurer,
      kickText,
      Offset(center.x - measure.size.width / 2f, y - 10f * scale),
      kickStyle
    )
  }

  private fun drawRollingSnowOrb(
    drawScope: DrawScope,
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    timer: Float,
    scale: Float
  ) {
    val radius = maxOf(w, h) * 0.65f
    val center = Offset(x + w / 2f, y + h / 2f)

    // Outer glow aura
    drawScope.drawCircle(Color(0xFF00E5FF).copy(alpha = 0.45f), radius = radius * 1.25f, center = center)
    // Giant white snow boulder
    drawScope.drawCircle(Color(0xFFE1F5FE), radius = radius, center = center)
    drawScope.drawCircle(Color.White, radius = radius * 0.82f, center = center)
    drawScope.drawCircle(Color(0xFF0288D1), radius = radius, center = center, style = Stroke(width = 3f * scale))

    // Rolling seam swirls
    val rot = timer * 14f
    for (i in 0..2) {
      val angle = rot + (i * 2.094f)
      val sx = center.x + cos(angle).toFloat() * (radius * 0.5f)
      val sy = center.y + sin(angle).toFloat() * (radius * 0.5f)
      drawScope.drawCircle(Color(0xFF4FC3F7), radius = 3.5f * scale, center = Offset(sx, sy))
    }
  }

  private fun drawPickup(
    drawScope: DrawScope,
    type: PickupType,
    x: Float,
    y: Float,
    sz: Float,
    scale: Float,
    textMeasurer: TextMeasurer
  ) {
    val center = Offset(x + sz / 2f, y + sz / 2f)
    when (type) {
      PickupType.COIN -> {
        drawScope.drawCircle(Color(0xFFFFD93D), radius = sz * 0.48f, center = center)
        drawScope.drawCircle(Color.White, radius = sz * 0.32f, center = center)
        drawScope.drawCircle(Color(0xFFFFA000), radius = sz * 0.48f, center = center, style = Stroke(width = 1.5f * scale))
      }

      PickupType.BLUE_GEM -> {
        sharedPath.reset()
        sharedPath.moveTo(center.x, y)
        sharedPath.lineTo(x + sz, center.y)
        sharedPath.lineTo(center.x, y + sz)
        sharedPath.lineTo(x, center.y)
        sharedPath.close()
        drawScope.drawPath(sharedPath, Color(0xFF00E5FF))
        drawScope.drawPath(sharedPath, Color.White, style = Stroke(width = 1.2f * scale))
      }

      PickupType.RED_GEM -> {
        sharedPath.reset()
        sharedPath.moveTo(center.x, y)
        sharedPath.lineTo(x + sz, center.y)
        sharedPath.lineTo(center.x, y + sz)
        sharedPath.lineTo(x, center.y)
        sharedPath.close()
        drawScope.drawPath(sharedPath, Color(0xFFFF1744))
        drawScope.drawPath(sharedPath, Color.White, style = Stroke(width = 1.2f * scale))
      }

      PickupType.POWER_SPEED -> {
        drawScope.drawCircle(Color(0xFF00E5FF), radius = sz * 0.48f, center = center)
        drawScope.drawCircle(Color.White, radius = sz * 0.28f, center = center)
        drawScope.drawCircle(Color.White, radius = sz * 0.48f, center = center, style = Stroke(width = 1.2f * scale))
      }

      PickupType.POWER_BLAST -> {
        drawScope.drawCircle(Color(0xFFFF5722), radius = sz * 0.48f, center = center)
        drawScope.drawCircle(Color.White, radius = sz * 0.28f, center = center)
        drawScope.drawCircle(Color.White, radius = sz * 0.48f, center = center, style = Stroke(width = 1.2f * scale))
      }

      PickupType.POWER_RANGE -> {
        drawScope.drawCircle(Color(0xFFFFD700), radius = sz * 0.48f, center = center)
        drawScope.drawCircle(Color.White, radius = sz * 0.28f, center = center)
        drawScope.drawCircle(Color.White, radius = sz * 0.48f, center = center, style = Stroke(width = 1.2f * scale))
      }

      PickupType.POWER_GIANT -> {
        drawScope.drawCircle(Color(0xFFE040FB), radius = sz * 0.48f, center = center)
        drawScope.drawCircle(Color.White, radius = sz * 0.28f, center = center)
        drawScope.drawCircle(Color.White, radius = sz * 0.48f, center = center, style = Stroke(width = 1.2f * scale))
      }

      PickupType.LETTER_F, PickupType.LETTER_R, PickupType.LETTER_O, PickupType.LETTER_S, PickupType.LETTER_T -> {
        val letterChar = when (type) {
          PickupType.LETTER_F -> "F"
          PickupType.LETTER_R -> "R"
          PickupType.LETTER_O -> "O"
          PickupType.LETTER_S -> "S"
          else -> "T"
        }
        // Golden letter badge with explicit letter printed in center!
        drawScope.drawCircle(Color(0xFFFFD93D), radius = sz * 0.5f, center = center)
        drawScope.drawCircle(Color(0xFFB78103), radius = sz * 0.5f, center = center, style = Stroke(width = 1.5f * scale))

        val letterStyle = TextStyle(
          color = Color(0xFF040812),
          fontSize = (9f * scale).sp,
          fontWeight = FontWeight.Black,
          fontFamily = FontFamily.Monospace
        )
        val measure = textMeasurer.measure(letterChar, letterStyle)
        drawScope.drawText(
          textMeasurer,
          letterChar,
          Offset(center.x - measure.size.width / 2f, center.y - measure.size.height / 2f),
          letterStyle
        )
      }
    }
  }

  private fun drawBossHealthBar(
    drawScope: DrawScope,
    boss: Enemy,
    ox: Float,
    oy: Float,
    rw: Float,
    scale: Float,
    textMeasurer: TextMeasurer
  ) {
    val barW = rw * 0.72f
    val barH = 12f * scale
    val bx = ox + (rw - barW) / 2f
    val by = oy + 32f * scale

    // Background
    drawScope.drawRoundRect(
      Color(0xFF061122),
      Offset(bx, by),
      Size(barW, barH),
      CornerRadius(4f * scale)
    )

    // Current HP
    val hpPct = (boss.hp / boss.maxHp).coerceIn(0f, 1f)
    val hpColor = when (boss.bossPhase) {
      3 -> Color(0xFFFF1744)
      2 -> Color(0xFFFF9100)
      else -> Color(0xFF00E5FF)
    }
    drawScope.drawRoundRect(
      hpColor,
      Offset(bx, by),
      Size(barW * hpPct, barH),
      CornerRadius(4f * scale)
    )

    // Border
    drawScope.drawRoundRect(
      Color.White,
      Offset(bx, by),
      Size(barW, barH),
      CornerRadius(4f * scale),
      style = Stroke(width = 1.5f * scale)
    )

    // Title label
    val label = "CRYO-TITAN (PHASE ${boss.bossPhase})"
    val style = TextStyle(
      color = Color.White,
      fontSize = (9f * scale).sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace
    )
    val measure = textMeasurer.measure(label, style)
    drawScope.drawText(
      textMeasurer,
      label,
      Offset(bx + (barW - measure.size.width) / 2f, by - 13f * scale),
      style
    )
  }

  private fun drawObstacleHazard(
    drawScope: DrawScope,
    hazard: ObstacleHazard,
    ox: Float,
    oy: Float,
    scale: Float,
    sharedPath: Path
  ) {
    val hx = ox + hazard.x * scale
    val hy = oy + hazard.y * scale
    val hw = hazard.width * scale
    val hh = hazard.height * scale
    val primaryColor = Color(hazard.colorType.primaryColor)
    val glowColor = Color(hazard.colorType.mouthGlow)

    // Base dark stone foundation block
    drawScope.drawRoundRect(
      Color(0xFF0D1B2A),
      Offset(hx - 2f * scale, hy - 2f * scale),
      Size(hw + 4f * scale, hh + 4f * scale),
      CornerRadius(6f * scale)
    )

    // Main Gargoyle / Beast Head Body (matches green obstacle from screenshot!)
    drawScope.drawRoundRect(
      primaryColor,
      Offset(hx, hy),
      Size(hw, hh),
      CornerRadius(5f * scale)
    )

    // Head horns / crest (demon/gargoyle horns on top)
    sharedPath.reset()
    if (hazard.facesRight) {
      sharedPath.moveTo(hx + hw * 0.2f, hy)
      sharedPath.lineTo(hx - 4f * scale, hy - 7f * scale)
      sharedPath.lineTo(hx + hw * 0.45f, hy)
      sharedPath.close()
      sharedPath.moveTo(hx + hw * 0.55f, hy)
      sharedPath.lineTo(hx + hw * 0.3f, hy - 8f * scale)
      sharedPath.lineTo(hx + hw * 0.8f, hy)
      sharedPath.close()
    } else {
      sharedPath.moveTo(hx + hw * 0.8f, hy)
      sharedPath.lineTo(hx + hw + 4f * scale, hy - 7f * scale)
      sharedPath.lineTo(hx + hw * 0.55f, hy)
      sharedPath.close()
      sharedPath.moveTo(hx + hw * 0.45f, hy)
      sharedPath.lineTo(hx + hw * 0.7f, hy - 8f * scale)
      sharedPath.lineTo(hx + hw * 0.2f, hy)
      sharedPath.close()
    }
    drawScope.drawPath(sharedPath, primaryColor)
    drawScope.drawPath(sharedPath, Color.Black.copy(alpha = 0.5f), style = Stroke(width = 1.2f * scale))

    // Glowing Eyes
    val eyeX = if (hazard.facesRight) hx + hw * 0.62f else hx + hw * 0.22f
    val eyeY = hy + hh * 0.32f
    val eyeColor = if (hazard.isAttacking) Color(0xFFFFEB3B) else Color(0xFFFF5252)
    val eyeRadius = if (hazard.isAttacking) 4.5f * scale else 3.5f * scale

    // Eye glow aura
    drawScope.drawCircle(eyeColor.copy(alpha = 0.4f), radius = eyeRadius * 1.6f, center = Offset(eyeX, eyeY))
    drawScope.drawCircle(eyeColor, radius = eyeRadius, center = Offset(eyeX, eyeY))
    // Slit pupil
    drawScope.drawOval(
      Color.Black,
      Offset(eyeX - 1.2f * scale, eyeY - 2.5f * scale),
      Size(2.4f * scale, 5f * scale)
    )

    // Mouth / Maw
    val mouthW = hw * 0.65f
    val mouthH = if (hazard.isAttacking) hh * 0.45f else hh * 0.22f
    val mouthX = if (hazard.facesRight) hx + hw * 0.45f else hx - hw * 0.1f
    val mouthY = hy + hh * 0.55f

    if (hazard.isAttacking) {
      // Wide open fiery mouth breathing fire! (Just like in the screenshot!)
      drawScope.drawRoundRect(
        Color(0xFF1A0000),
        Offset(mouthX, mouthY),
        Size(mouthW, mouthH),
        CornerRadius(3f * scale)
      )
      // Internal Fireball / Flame charge inside mouth
      drawScope.drawCircle(
        glowColor,
        radius = (mouthH * 0.55f),
        center = Offset(mouthX + mouthW / 2f, mouthY + mouthH / 2f)
      )
      drawScope.drawCircle(
        Color(0xFFFFEB3B),
        radius = (mouthH * 0.3f),
        center = Offset(mouthX + mouthW / 2f, mouthY + mouthH / 2f)
      )
      // Sharp fangs on jaw
      sharedPath.reset()
      sharedPath.moveTo(mouthX + 2f * scale, mouthY)
      sharedPath.lineTo(mouthX + 6f * scale, mouthY + 4f * scale)
      sharedPath.lineTo(mouthX + 10f * scale, mouthY)
      sharedPath.close()
      drawScope.drawPath(sharedPath, Color.White)
    } else {
      // Idle stone grin
      drawScope.drawRoundRect(
        Color(0xFF0F172A),
        Offset(mouthX, mouthY),
        Size(mouthW, mouthH),
        CornerRadius(2f * scale)
      )
      // Fangs
      sharedPath.reset()
      sharedPath.moveTo(mouthX + 2f * scale, mouthY)
      sharedPath.lineTo(mouthX + 5f * scale, mouthY + 3f * scale)
      sharedPath.lineTo(mouthX + 8f * scale, mouthY)
      sharedPath.close()
      drawScope.drawPath(sharedPath, Color.White)
    }

    // Outer dark stroke
    drawScope.drawRoundRect(
      Color(0xFF0B1320),
      Offset(hx, hy),
      Size(hw, hh),
      CornerRadius(5f * scale),
      style = Stroke(width = 1.6f * scale)
    )
  }

  private fun drawHazardFireball(
    drawScope: DrawScope,
    fireball: HazardFireball,
    ox: Float,
    oy: Float,
    scale: Float,
    sharedPath: Path
  ) {
    val fx = ox + fireball.x * scale
    val fy = oy + fireball.y * scale
    val fr = fireball.radius * scale
    val glowColor = Color(fireball.colorType.mouthGlow)

    // Outer flame glow aura
    drawScope.drawCircle(glowColor.copy(alpha = 0.35f), radius = fr * 1.8f, center = Offset(fx, fy))
    // Mid fireball body
    drawScope.drawCircle(glowColor, radius = fr * 1.2f, center = Offset(fx, fy))
    // Bright yellow flame core
    drawScope.drawCircle(Color(0xFFFFEB3B), radius = fr * 0.75f, center = Offset(fx, fy))
    // Pure white hot center
    drawScope.drawCircle(Color.White, radius = fr * 0.4f, center = Offset(fx, fy))

    // Trailing flame flamelets
    val tailDir = if (fireball.vx > 0f) -1f else 1f
    sharedPath.reset()
    sharedPath.moveTo(fx, fy - fr * 0.7f)
    sharedPath.lineTo(fx + tailDir * (fr * 2.2f), fy)
    sharedPath.lineTo(fx, fy + fr * 0.7f)
    sharedPath.close()
    drawScope.drawPath(sharedPath, glowColor.copy(alpha = 0.8f))

    sharedPath.reset()
    sharedPath.moveTo(fx, fy - fr * 0.4f)
    sharedPath.lineTo(fx + tailDir * (fr * 1.4f), fy)
    sharedPath.lineTo(fx, fy + fr * 0.4f)
    sharedPath.close()
    drawScope.drawPath(sharedPath, Color(0xFFFFEB3B))
  }
}
