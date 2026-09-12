package com.example.game

import com.example.audio.GameAudio
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class Player(
  val id: Int,
  var x: Float,
  var y: Float,
  var vx: Float = 0f,
  var vy: Float = 0f,
  var facingRight: Boolean = true,
  var isGrounded: Boolean = false,
  var lives: Int = 3,
  var score: Int = 0,
  var invincibleTimer: Float = 0f,
  var isDead: Boolean = false,
  var giantTimer: Float = 0f,
  var speedTimer: Float = 0f,
  var blastTimer: Float = 0f,
  var rangeTimer: Float = 0f,
  var shootCooldown: Float = 0f,
  var kickAnimTimer: Float = 0f,
  var dropThrough: Boolean = false,
  var jumpHeld: Boolean = false,
  var jumpHoldTime: Float = 0f
) {
  val width: Float get() = if (giantTimer > 0f) 36f else 22f
  val height: Float get() = if (giantTimer > 0f) 44f else 28f
}

data class Projectile(
  var x: Float,
  var y: Float,
  var vx: Float,
  var vy: Float,
  val isBlast: Boolean,
  val isLongRange: Boolean,
  val ownerId: Int,
  var lifeTime: Float = 0.5f,
  var radius: Float = if (isBlast) 14f else 8f
)

data class Enemy(
  val id: Int,
  val type: EnemyType,
  var x: Float,
  var y: Float,
  var vx: Float = 0f,
  var vy: Float = 0f,
  var width: Float = 24f,
  var height: Float = 26f,
  var facingRight: Boolean = true,
  var isGrounded: Boolean = false,
  var freezeProgress: Float = 0f, // 0 to 100
  var freezeShakeTimer: Float = 0f,
  var isRolling: Boolean = false,
  var rollSpeed: Float = 0f,
  var rollBounces: Int = 0,
  var rollTimer: Float = 0f,
  var comboCount: Int = 0,
  var hp: Float = if (type == EnemyType.CRYO_TITAN) 100f else 1f,
  var maxHp: Float = if (type == EnemyType.CRYO_TITAN) 100f else 1f,
  var bossPhase: Int = 1,
  var bossActionTimer: Float = 0f,
  var isEliminated: Boolean = false,
  var aiTimer: Float = 0f
) {
  val isFullyFrozen: Boolean get() = freezeProgress >= 100f
}

data class Pickup(
  val type: PickupType,
  var x: Float,
  var y: Float,
  var vx: Float = 0f,
  var vy: Float = -60f,
  var lifeTime: Float = 12f,
  val size: Float = 16f
)

data class FloatingText(
  val text: String,
  var x: Float,
  var y: Float,
  var vy: Float = -35f,
  val color: Long,
  var life: Float = 1.0f
)

data class Particle(
  var x: Float,
  var y: Float,
  var vx: Float,
  var vy: Float,
  val color: Long,
  var size: Float,
  var life: Float,
  val maxLife: Float
)

data class FallingIcicle(
  var x: Float,
  var y: Float,
  var vy: Float = 220f,
  val size: Float = 14f
)

class GameEngine(
  var isTwoPlayer: Boolean = false,
  var powerUpStart: Boolean = false,
  var onLevelCleared: () -> Unit = {},
  var onGameOver: () -> Unit = {}
) {
  var currentLevelIdx = 0
  var levelTime = 0f
  var phantomSpawned = false
  val collectedLetters = mutableSetOf<Char>()

  lateinit var currentLevel: LevelConfig
  val players = mutableListOf<Player>()
  val enemies = mutableListOf<Enemy>()
  val projectiles = mutableListOf<Projectile>()
  val pickups = mutableListOf<Pickup>()
  val floatingTexts = mutableListOf<FloatingText>()
  val particles = mutableListOf<Particle>()
  val icicles = mutableListOf<FallingIcicle>()
  val obstacleHazards = mutableListOf<ObstacleHazard>()
  val hazardFireballs = mutableListOf<HazardFireball>()

  var nextEnemyId = 1
  var isPaused = false
  var isReadyCountdown = 2.0f // Classic arcade "READY!" prompt
  var bossDefeatedAnimation = 0f

  companion object {
    const val GRAVITY = 640f           // Balanced platformer gravity (down from 720f)
    const val WALK_SPEED = 125f        // Smooth, controllable medium walking speed (down from 140f)
    const val BOOSTED_WALK_SPEED = 180f // Controlled power-up speed (down from 220f)
    const val JUMP_IMPULSE = -290f     // Smooth crisp jump impulse
    const val VARIABLE_JUMP_BOOST = -380f // Responsive controlled jump boost
    const val ROLL_VELOCITY = 260f     // Clear, trackable medium snow orb roll (down from 330f)
  }

  init {
    startNewGame()
  }

  fun startNewGame(startLevel: Int = 0) {
    currentLevelIdx = startLevel.coerceIn(0, LevelCatalog.TOTAL_LEVELS - 1)
    collectedLetters.clear()
    players.clear()

    // Player 1 (Mint Hero "Glint")
    players.add(
      Player(
        id = 1,
        x = 60f,
        y = 500f,
        speedTimer = if (powerUpStart) 15f else 0f,
        blastTimer = if (powerUpStart) 15f else 0f
      )
    )

    if (isTwoPlayer) {
      // Player 2 (Coral Hero "Ember")
      players.add(
        Player(
          id = 2,
          x = 320f,
          y = 500f,
          speedTimer = if (powerUpStart) 15f else 0f,
          blastTimer = if (powerUpStart) 15f else 0f
        )
      )
    }

    loadLevel(currentLevelIdx)
  }

  fun loadLevel(index: Int) {
    currentLevelIdx = index.coerceIn(0, LevelCatalog.TOTAL_LEVELS - 1)
    currentLevel = LevelCatalog.getLevel(currentLevelIdx + 1)
    levelTime = 0f
    phantomSpawned = false
    isReadyCountdown = 1.8f
    bossDefeatedAnimation = 0f

    enemies.clear()
    projectiles.clear()
    pickups.clear()
    floatingTexts.clear()
    icicles.clear()
    obstacleHazards.clear()
    hazardFireballs.clear()

    // Populate active obstacle hazards from level
    for (hazard in currentLevel.obstacleHazards) {
      obstacleHazards.add(hazard.copy())
    }

    // Reset player positions
    players.getOrNull(0)?.apply {
      x = 80f
      y = 500f
      vx = 0f
      vy = 0f
      invincibleTimer = 2.0f
    }
    players.getOrNull(1)?.apply {
      x = 300f
      y = 500f
      vx = 0f
      vy = 0f
      invincibleTimer = 2.0f
    }

    // Spawn level enemies
    val stageNum = currentLevel.levelNumber
    for (spawn in currentLevel.enemySpawns) {
      val e = Enemy(
        id = nextEnemyId++,
        type = spawn.type,
        x = spawn.x,
        y = spawn.y,
        facingRight = spawn.x < LevelCatalog.GAME_WIDTH / 2f
      )
      if (spawn.type == EnemyType.CRYO_TITAN) {
        e.width = 72f
        e.height = 70f
        // Dynamic boss HP scaling across 300 levels
        val scaledHp = 100f + (stageNum / 10f) * 15f
        e.hp = scaledHp
        e.maxHp = scaledHp
      } else if (spawn.type == EnemyType.SKIMMER) {
        e.width = 24f
        e.height = 20f
      }
      enemies.add(e)
    }
  }

  fun retryLevel() {
    loadLevel(currentLevelIdx)
  }

  // Player Input Handlers
  fun setPlayerMove(playerId: Int, moveDir: Float, dropDown: Boolean = false) {
    val p = players.find { it.id == playerId } ?: return
    val speed = if (p.speedTimer > 0f) BOOSTED_WALK_SPEED else WALK_SPEED
    p.vx = moveDir * speed
    if (moveDir > 0.05f) p.facingRight = true
    if (moveDir < -0.05f) p.facingRight = false
    p.dropThrough = dropDown
  }

  fun setPlayerJump(playerId: Int, isPressed: Boolean) {
    val p = players.find { it.id == playerId } ?: return
    p.jumpHeld = isPressed
    if (isPressed && p.isGrounded && !p.isDead) {
      p.vy = JUMP_IMPULSE
      p.isGrounded = false
      p.jumpHoldTime = 0f
      GameAudio.playSound(GameAudio.SoundEffect.JUMP)
    }
  }

  fun firePlayer(playerId: Int) {
    val p = players.find { it.id == playerId } ?: return
    if (p.isDead || p.shootCooldown > 0f) return

    // Check if player is right next to a fully frozen Snow Orb: KICK IT!
    val kickRange = 26f
    val nearbyOrb = enemies.find { e ->
      e.isFullyFrozen && !e.isRolling && !e.isEliminated &&
        kotlin.math.abs(e.y - p.y) < 28f &&
        kotlin.math.abs(e.x - p.x) < kickRange + (e.width / 2f)
    }

    if (nearbyOrb != null) {
      // Kick the snow orb!
      p.kickAnimTimer = 0.25f
      nearbyOrb.isRolling = true
      nearbyOrb.rollBounces = 0
      nearbyOrb.rollTimer = 0f
      nearbyOrb.comboCount = 0
      val rollDir = if (p.x < nearbyOrb.x) 1f else -1f
      nearbyOrb.rollSpeed = rollDir * ROLL_VELOCITY
      nearbyOrb.vx = nearbyOrb.rollSpeed
      GameAudio.playSound(GameAudio.SoundEffect.ORB_KICK)

      // Burst of kick snow particles
      spawnParticleBurst(nearbyOrb.x, nearbyOrb.y, 0xFF50E3C2, 10)
      return
    }

    // Otherwise, shoot freeze projectile
    p.shootCooldown = 0.22f
    val dir = if (p.facingRight) 1f else -1f
    val spawnX = if (p.facingRight) p.x + p.width + 4f else p.x - 6f
    val spawnY = p.y + (p.height * 0.45f)
    val isBlast = p.blastTimer > 0f
    val isRange = p.rangeTimer > 0f
    val projSpeed = if (isRange) 480f else 320f

    projectiles.add(
      Projectile(
        x = spawnX,
        y = spawnY,
        vx = dir * projSpeed,
        vy = if (isRange) 0f else -30f,
        isBlast = isBlast,
        isLongRange = isRange,
        ownerId = playerId,
        lifeTime = if (isRange) 0.9f else 0.45f
      )
    )

    GameAudio.playSound(GameAudio.SoundEffect.SHOOT)
    spawnParticleBurst(spawnX, spawnY, 0xFF00E5FF, 4)
  }

  // Main 60fps game physics update
  fun update(dt: Float) {
    if (isPaused) return

    if (isReadyCountdown > 0f) {
      isReadyCountdown -= dt
      return
    }

    levelTime += dt

    // Pacing hazard: Frost Phantom
    if (levelTime > 45f && !phantomSpawned && !currentLevel.isBossLevel) {
      phantomSpawned = true
      enemies.add(
        Enemy(
          id = nextEnemyId++,
          type = EnemyType.PHANTOM,
          x = LevelCatalog.GAME_WIDTH / 2f,
          y = 40f,
          vx = 45f,
          vy = 30f
        )
      )
      addFloatingText("HAZARD: PHANTOM!", LevelCatalog.GAME_WIDTH / 2f - 40f, 60f, 0xFFFF4757)
      GameAudio.playSound(GameAudio.SoundEffect.BOSS_HIT)
    }

    updatePlayers(dt)
    updateProjectiles(dt)
    updateEnemies(dt)
    updateObstacleHazards(dt)
    updateHazardFireballs(dt)
    updateIcicles(dt)
    updatePickups(dt)
    updateParticles(dt)
    updateFloatingTexts(dt)

    checkCollisions()
    checkLevelCompletion(dt)
  }

  private fun updatePlayers(dt: Float) {
    for (p in players) {
      if (p.isDead) continue

      if (p.invincibleTimer > 0f) p.invincibleTimer -= dt
      if (p.giantTimer > 0f) p.giantTimer -= dt
      if (p.speedTimer > 0f) p.speedTimer -= dt
      if (p.blastTimer > 0f) p.blastTimer -= dt
      if (p.rangeTimer > 0f) p.rangeTimer -= dt
      if (p.shootCooldown > 0f) p.shootCooldown -= dt
      if (p.kickAnimTimer > 0f) p.kickAnimTimer -= dt

      // Variable jump height logic
      if (p.jumpHeld && !p.isGrounded && p.vy < 0f && p.jumpHoldTime < 0.22f) {
        p.jumpHoldTime += dt
        p.vy += (VARIABLE_JUMP_BOOST * dt)
      }

      // Gravity
      p.vy += GRAVITY * dt
      if (p.vy > 650f) p.vy = 650f

      // Move horizontally
      p.x += p.vx * dt
      // Clamp to screen boundaries
      if (p.x < 4f) p.x = 4f
      if (p.x + p.width > LevelCatalog.GAME_WIDTH - 4f) {
        p.x = LevelCatalog.GAME_WIDTH - 4f - p.width
      }

      // Vertical movement & platform collision
      val oldY = p.y
      p.y += p.vy * dt

      p.isGrounded = false
      for (plat in currentLevel.platforms) {
        val pLeft = p.x
        val pRight = p.x + p.width
        val platLeft = plat.x
        val platRight = plat.x + plat.width

        // Check horizontal overlap
        if (pRight > platLeft && pLeft < platRight) {
          if (plat.isOneWay) {
            // One-way platform: land only when falling downward and feet cross top
            if (!p.dropThrough && p.vy >= 0f && (oldY + p.height) <= plat.y + 4f && (p.y + p.height) >= plat.y) {
              p.y = plat.y - p.height
              p.vy = 0f
              p.isGrounded = true
            }
          } else {
            // Solid platform (floor/ceil)
            if (p.vy >= 0f && (p.y + p.height) >= plat.y && oldY + p.height <= plat.y + 12f) {
              p.y = plat.y - p.height
              p.vy = 0f
              p.isGrounded = true
            }
          }
        }
      }

      // Bottom screen clamp
      if (p.y + p.height > 540f) {
        p.y = 540f - p.height
        p.vy = 0f
        p.isGrounded = true
      }
    }
  }

  private fun updateProjectiles(dt: Float) {
    val iter = projectiles.iterator()
    while (iter.hasNext()) {
      val proj = iter.next()
      proj.x += proj.vx * dt
      proj.y += proj.vy * dt
      proj.lifeTime -= dt

      // Wall bounce or expire
      if (proj.x < 6f || proj.x > LevelCatalog.GAME_WIDTH - 6f || proj.lifeTime <= 0f) {
        spawnParticleBurst(proj.x, proj.y, 0xFF00E5FF, 3)
        iter.remove()
      }
    }
  }

  private fun updateEnemies(dt: Float) {
    for (e in enemies) {
      if (e.isEliminated) continue

      // Rolling Snow Orb logic
      if (e.isRolling) {
        e.rollTimer += dt
        e.vy += GRAVITY * dt
        e.x += e.vx * dt
        val oldY = e.y
        e.y += e.vy * dt

        // Platform collision for rolling orb
        for (plat in currentLevel.platforms) {
          if (e.x + e.width > plat.x && e.x < plat.x + plat.width) {
            if (e.vy >= 0f && (oldY + e.height) <= plat.y + 4f && (e.y + e.height) >= plat.y) {
              e.y = plat.y - e.height
              e.vy = 0f
            }
          }
        }

        // Floor collision
        if (e.y + e.height > 540f) {
          e.y = 540f - e.height
          e.vy = 0f
        }

        // Wall bounce
        if (e.x < 6f) {
          e.x = 6f
          e.vx = -e.vx
          e.rollBounces++
          GameAudio.playSound(GameAudio.SoundEffect.ORB_KICK)
          spawnParticleBurst(e.x, e.y, 0xFFFFFFFF, 6)
        } else if (e.x + e.width > LevelCatalog.GAME_WIDTH - 6f) {
          e.x = LevelCatalog.GAME_WIDTH - 6f - e.width
          e.vx = -e.vx
          e.rollBounces++
          GameAudio.playSound(GameAudio.SoundEffect.ORB_KICK)
          spawnParticleBurst(e.x + e.width, e.y, 0xFFFFFFFF, 6)
        }

        // Rolling trails
        if (Random.nextFloat() < 0.4f) {
          spawnParticleBurst(e.x + e.width / 2f, e.y + e.height, 0xFFF0F8FF, 2)
        }

        // Shunt/shatter after rolling duration or 5 wall bounces
        if (e.rollTimer > 4.5f || e.rollBounces >= 5) {
          shatterSnowOrb(e)
        }
        continue
      }

      // Fully frozen thawing logic
      if (e.isFullyFrozen) {
        e.freezeShakeTimer += dt
        if (e.freezeShakeTimer > 8.0f) {
          // Break free!
          e.freezeProgress = 0f
          e.freezeShakeTimer = 0f
          spawnParticleBurst(e.x + e.width / 2f, e.y + e.height / 2f, 0xFF00E5FF, 12)
          GameAudio.playSound(GameAudio.SoundEffect.CHAIN_CRUSH)
        }
        continue
      }

      // AI Behavior per enemy archetype
      when (e.type) {
        EnemyType.WISP -> {
          // Patrol and leap (smooth medium cadence)
          e.vy += GRAVITY * dt
          val walkSpeed = 44f
          e.vx = if (e.facingRight) walkSpeed else -walkSpeed
          e.x += e.vx * dt
          val oldY = e.y
          e.y += e.vy * dt

          // Wall bounce
          if (e.x < 10f) { e.x = 10f; e.facingRight = true }
          if (e.x + e.width > LevelCatalog.GAME_WIDTH - 10f) { e.x = LevelCatalog.GAME_WIDTH - 10f - e.width; e.facingRight = false }

          // Platform ground
          e.isGrounded = false
          for (plat in currentLevel.platforms) {
            if (e.x + e.width > plat.x && e.x < plat.x + plat.width) {
              if (e.vy >= 0f && (oldY + e.height) <= plat.y + 4f && (e.y + e.height) >= plat.y) {
                e.y = plat.y - e.height
                e.vy = 0f
                e.isGrounded = true
              }
            }
          }
          if (e.y + e.height > 540f) {
            e.y = 540f - e.height
            e.vy = 0f
            e.isGrounded = true
          }

          // Occasional jump
          e.aiTimer += dt
          if (e.aiTimer > 2.8f && e.isGrounded) {
            e.aiTimer = 0f
            if (Random.nextFloat() < 0.6f) {
              e.vy = -220f
            }
          }
        }

        EnemyType.SKIMMER -> {
          // Sinusoidal flight (moderate medium speed)
          e.aiTimer += dt
          e.vx = if (e.facingRight) 58f else -58f
          e.x += e.vx * dt
          e.vy = cos(e.aiTimer * 3.0f) * 50f
          e.y += e.vy * dt

          if (e.x < 12f) { e.x = 12f; e.facingRight = true }
          if (e.x + e.width > LevelCatalog.GAME_WIDTH - 12f) { e.x = LevelCatalog.GAME_WIDTH - 12f - e.width; e.facingRight = false }
          if (e.y < 30f) e.y = 30f
          if (e.y > 450f) e.y = 450f
        }

        EnemyType.SPIKELING -> {
          // Controlled dash and pause
          e.aiTimer += dt
          e.vy += GRAVITY * dt
          val isDashing = (e.aiTimer % 3.2f) < 1.2f
          val speed = if (isDashing) 90f else 28f
          e.vx = if (e.facingRight) speed else -speed
          e.x += e.vx * dt
          val oldY = e.y
          e.y += e.vy * dt

          if (e.x < 10f) { e.x = 10f; e.facingRight = true }
          if (e.x + e.width > LevelCatalog.GAME_WIDTH - 10f) { e.x = LevelCatalog.GAME_WIDTH - 10f - e.width; e.facingRight = false }

          for (plat in currentLevel.platforms) {
            if (e.x + e.width > plat.x && e.x < plat.x + plat.width) {
              if (e.vy >= 0f && (oldY + e.height) <= plat.y + 4f && (e.y + e.height) >= plat.y) {
                e.y = plat.y - e.height
                e.vy = 0f
              }
            }
          }
          if (e.y + e.height > 540f) {
            e.y = 540f - e.height
            e.vy = 0f
          }
        }

        EnemyType.PHANTOM -> {
          // Pacing hazard tracks nearest player through walls (balanced medium speed)
          val target = players.minByOrNull { kotlin.math.hypot(it.x - e.x, it.y - e.y) }
          if (target != null) {
            val angle = kotlin.math.atan2(target.y - e.y, target.x - e.x)
            e.vx = cos(angle) * 62f
            e.vy = sin(angle) * 62f
            e.x += e.vx * dt
            e.y += e.vy * dt
          }
        }

        EnemyType.CRYO_TITAN -> {
          // Boss AI
          e.bossActionTimer += dt
          e.vy += GRAVITY * dt
          e.y += e.vy * dt
          if (e.y + e.height > 540f) {
            e.y = 540f - e.height
            e.vy = 0f
          }

          // Boss phase based on HP
          e.bossPhase = when {
            e.hp <= 30f -> 3
            e.hp <= 65f -> 2
            else -> 1
          }

          val attackInterval = when (e.bossPhase) {
            3 -> 2.0f
            2 -> 3.2f
            else -> 4.5f
          }

          if (e.bossActionTimer > attackInterval) {
            e.bossActionTimer = 0f
            // Boss attack: ground pound & icicle drop
            GameAudio.playSound(GameAudio.SoundEffect.BOSS_HIT)
            for (i in 0..4) {
              val dropX = 30f + (i * 70f) + Random.nextFloat() * 30f
              icicles.add(FallingIcicle(x = dropX, y = 20f, vy = 200f + Random.nextFloat() * 80f))
            }
            // Spawn a minion so player can freeze it and kick it back!
            if (enemies.count { it.type == EnemyType.WISP && !it.isEliminated } < 3) {
              enemies.add(
                Enemy(
                  id = nextEnemyId++,
                  type = EnemyType.WISP,
                  x = if (Random.nextBoolean()) 40f else 320f,
                  y = 200f
                )
              )
            }
          }
        }
      }
    }
  }

  private fun updateIcicles(dt: Float) {
    val iter = icicles.iterator()
    while (iter.hasNext()) {
      val ic = iter.next()
      ic.y += ic.vy * dt
      if (ic.y > 540f) {
        spawnParticleBurst(ic.x, 540f, 0xFF00E5FF, 4)
        iter.remove()
      }
    }
  }

  private fun updatePickups(dt: Float) {
    val iter = pickups.iterator()
    while (iter.hasNext()) {
      val item = iter.next()
      item.lifeTime -= dt
      item.vy += GRAVITY * 0.4f * dt
      item.y += item.vy * dt
      item.x += item.vx * dt

      // Ground bounce
      if (item.y + item.size > 540f) {
        item.y = 540f - item.size
        item.vy = -item.vy * 0.4f
        item.vx *= 0.8f
      }

      if (item.lifeTime <= 0f) {
        iter.remove()
      }
    }
  }

  private fun updateParticles(dt: Float) {
    val iter = particles.iterator()
    while (iter.hasNext()) {
      val p = iter.next()
      p.life -= dt
      p.x += p.vx * dt
      p.y += p.vy * dt
      if (p.life <= 0f) iter.remove()
    }
  }

  private fun updateFloatingTexts(dt: Float) {
    val iter = floatingTexts.iterator()
    while (iter.hasNext()) {
      val ft = iter.next()
      ft.life -= dt
      ft.y += ft.vy * dt
      if (ft.life <= 0f) iter.remove()
    }
  }

  private fun updateObstacleHazards(dt: Float) {
    for (h in obstacleHazards) {
      if (h.isAttacking) {
        h.attackAnimTimer -= dt
        if (h.attackAnimTimer <= 0f) {
          h.isAttacking = false
        }
      }

      h.attackTimer += dt
      if (h.attackTimer >= h.attackInterval) {
        h.attackTimer = 0f
        h.isAttacking = true
        h.attackAnimTimer = 0.65f

        // Spit fireball/hazard shot from gargoyle mouth (matching screenshot)
        val mouthX = if (h.facesRight) h.x + h.width + 4f else h.x - 4f
        val mouthY = h.y + (h.height * 0.52f)
        val speed = if (h.facesRight) 155f else -155f
        hazardFireballs.add(
          HazardFireball(
            x = mouthX,
            y = mouthY,
            vx = speed,
            colorType = h.colorType
          )
        )
        spawnParticleBurst(mouthX, mouthY, h.colorType.mouthGlow, 8)
        GameAudio.playSound(GameAudio.SoundEffect.BOSS_HIT)
      }
    }
  }

  private fun updateHazardFireballs(dt: Float) {
    val iter = hazardFireballs.iterator()
    while (iter.hasNext()) {
      val fb = iter.next()
      fb.lifeTime -= dt
      fb.x += fb.vx * dt
      fb.y += fb.vy * dt

      // Trailing flame particles
      if (Random.nextFloat() < 0.4f) {
        particles.add(
          Particle(
            x = fb.x + (Random.nextFloat() * 4f - 2f),
            y = fb.y + (Random.nextFloat() * 4f - 2f),
            vx = -fb.vx * 0.12f,
            vy = Random.nextFloat() * -18f,
            color = fb.colorType.mouthGlow,
            size = 3.2f,
            life = 0.28f,
            maxLife = 0.28f
          )
        )
      }

      if (fb.lifeTime <= 0f || fb.x < 2f || fb.x > LevelCatalog.GAME_WIDTH - 2f) {
        iter.remove()
      }
    }
  }

  private fun checkCollisions() {
    // 1. Projectiles vs Enemies
    val projIter = projectiles.iterator()
    while (projIter.hasNext()) {
      val proj = projIter.next()
      var consumed = false

      for (e in enemies) {
        if (e.isEliminated || e.isRolling) continue

        val hit = proj.x > e.x && proj.x < e.x + e.width &&
          proj.y > e.y && proj.y < e.y + e.height

        if (hit) {
          if (e.type == EnemyType.CRYO_TITAN) {
            // Boss takes small chip damage from direct frost shots
            e.hp -= if (proj.isBlast) 2f else 1f
            addFloatingText("-1", e.x + e.width / 2f, e.y, 0xFF00E5FF)
            GameAudio.playSound(GameAudio.SoundEffect.FREEZE_HIT)
            spawnParticleBurst(proj.x, proj.y, 0xFF00E5FF, 6)
            consumed = true
            break
          } else {
            // Normal enemy freeze accumulation
            val freezeAmount = if (proj.isBlast) 100f else 35f
            e.freezeProgress = (e.freezeProgress + freezeAmount).coerceAtMost(100f)
            e.freezeShakeTimer = 0f
            GameAudio.playSound(GameAudio.SoundEffect.FREEZE_HIT)
            spawnParticleBurst(proj.x, proj.y, 0xFF50E3C2, 8)

            if (e.freezeProgress >= 100f) {
              addFloatingText("ORB READY!", e.x, e.y - 10f, 0xFFFFD93D)
            }
            consumed = true
            break
          }
        }
      }

      if (consumed) {
        projIter.remove()
      }
    }

    // 1b. Projectiles vs Hazard Fireballs (Extinguish attack!)
    val fIter = hazardFireballs.iterator()
    while (fIter.hasNext()) {
      val fb = fIter.next()
      var extinguished = false
      val pIter = projectiles.iterator()
      while (pIter.hasNext()) {
        val proj = pIter.next()
        val dist = kotlin.math.hypot(proj.x - fb.x, proj.y - fb.y)
        if (dist < proj.radius + fb.radius) {
          pIter.remove()
          extinguished = true
          spawnParticleBurst(fb.x, fb.y, 0xFFE0F7FA, 10)
          addFloatingText("EXTINGUISHED!", fb.x - 24f, fb.y - 8f, 0xFF00E5FF)
          GameAudio.playSound(GameAudio.SoundEffect.FREEZE_HIT)
          break
        }
      }
      if (extinguished) {
        fIter.remove()
      }
    }

    // 2. Rolling Snow Orbs vs other Enemies & Boss
    for (rolling in enemies) {
      if (!rolling.isRolling || rolling.isEliminated) continue

      for (target in enemies) {
        if (target == rolling || target.isEliminated) continue

        val overlap = rolling.x < target.x + target.width &&
          rolling.x + rolling.width > target.x &&
          rolling.y < target.y + target.height &&
          rolling.y + rolling.height > target.y

        if (overlap) {
          if (target.type == EnemyType.CRYO_TITAN) {
            // Massive boss hit! Deals 25 damage!
            target.hp -= 25f
            addFloatingText("CRITICAL -25!", target.x + target.width / 2f, target.y, 0xFFFFD93D)
            GameAudio.playSound(GameAudio.SoundEffect.BOSS_HIT)
            spawnParticleBurst(rolling.x + rolling.width / 2f, rolling.y, 0xFFFF5252, 16)
            shatterSnowOrb(rolling)

            if (target.hp <= 0f) {
              target.isEliminated = true
              GameAudio.playSound(GameAudio.SoundEffect.BOSS_DEFEAT)
              spawnParticleBurst(target.x + target.width / 2f, target.y + target.height / 2f, 0xFFFFD700, 24)
              addFloatingText("BOSS DEFEATED! +10000", target.x, target.y - 20f, 0xFFFFD93D)
              players.firstOrNull()?.let { it.score += 10000 }
            }
            break
          } else {
            // Eliminate target enemy in rolling chain!
            target.isEliminated = true
            rolling.comboCount++
            val scoreGain = 200 * rolling.comboCount
            players.firstOrNull()?.let { it.score += scoreGain }
            addFloatingText("+$scoreGain (x${rolling.comboCount})", target.x, target.y, 0xFFFFD93D)
            GameAudio.playSound(GameAudio.SoundEffect.CHAIN_CRUSH)
            spawnParticleBurst(target.x + target.width / 2f, target.y + target.height / 2f, 0xFF00E5FF, 12)

            // Spawn drops based on combo
            spawnDropsForKill(target.x, target.y, rolling.comboCount)
          }
        }
      }
    }

    // 3. Players vs Enemies / Hazards
    for (p in players) {
      if (p.isDead) continue

      // Giant mode contact crush
      if (p.giantTimer > 0f) {
        for (e in enemies) {
          if (e.isEliminated || e.isRolling) continue
          val overlap = p.x < e.x + e.width && p.x + p.width > e.x &&
            p.y < e.y + e.height && p.y + p.height > e.y
          if (overlap) {
            if (e.type == EnemyType.CRYO_TITAN) {
              e.hp -= 5f
              addFloatingText("-5", e.x, e.y, 0xFFFFD93D)
            } else {
              e.isEliminated = true
              p.score += 300
              GameAudio.playSound(GameAudio.SoundEffect.CHAIN_CRUSH)
              spawnParticleBurst(e.x + e.width / 2f, e.y + e.height / 2f, 0xFFE040FB, 12)
              spawnDropsForKill(e.x, e.y, 1)
            }
          }
        }
        continue
      }

      if (p.invincibleTimer > 0f) continue

      // Enemy hit check
      for (e in enemies) {
        if (e.isEliminated) continue
        // Safe to touch if fully frozen and not rolling!
        if (e.isFullyFrozen && !e.isRolling) continue

        val overlap = p.x < e.x + e.width && p.x + p.width > e.x &&
          p.y < e.y + e.height && p.y + p.height > e.y

        if (overlap) {
          playerHit(p)
          break
        }
      }

      // Falling icicles hit check
      for (ic in icicles) {
        if (ic.x > p.x && ic.x < p.x + p.width && ic.y > p.y && ic.y < p.y + p.height) {
          playerHit(p)
          break
        }
      }

      // Hazard Fireballs hit check (Green/Blue/Red obstacle attack)
      val fbIter = hazardFireballs.iterator()
      while (fbIter.hasNext()) {
        val fb = fbIter.next()
        val hit = fb.x > p.x - fb.radius && fb.x < p.x + p.width + fb.radius &&
          fb.y > p.y - fb.radius && fb.y < p.y + p.height + fb.radius
        if (hit) {
          playerHit(p)
          fbIter.remove()
          spawnParticleBurst(fb.x, fb.y, fb.colorType.mouthGlow, 12)
          break
        }
      }

      // Pickups check
      val pIter = pickups.iterator()
      while (pIter.hasNext()) {
        val item = pIter.next()
        val dist = kotlin.math.hypot(p.x + p.width / 2f - item.x, p.y + p.height / 2f - item.y)
        if (dist < 24f) {
          collectPickup(p, item)
          pIter.remove()
        }
      }
    }
  }

  private fun playerHit(p: Player) {
    p.lives--
    p.invincibleTimer = 2.5f
    GameAudio.playSound(GameAudio.SoundEffect.GAME_OVER)
    spawnParticleBurst(p.x + p.width / 2f, p.y + p.height / 2f, 0xFFFF4757, 18)
    addFloatingText("-1 LIFE", p.x, p.y - 12f, 0xFFFF4757)

    if (p.lives <= 0) {
      p.isDead = true
      if (players.all { it.isDead }) {
        onGameOver()
      }
    }
  }

  private fun collectPickup(p: Player, item: Pickup) {
    when (item.type) {
      PickupType.COIN -> {
        p.score += 200
        addFloatingText("+200", item.x, item.y, 0xFFFFD93D)
        GameAudio.playSound(GameAudio.SoundEffect.COIN)
      }
      PickupType.BLUE_GEM -> {
        p.score += 500
        addFloatingText("+500", item.x, item.y, 0xFF00E5FF)
        GameAudio.playSound(GameAudio.SoundEffect.COIN)
      }
      PickupType.RED_GEM -> {
        p.score += 1000
        addFloatingText("+1000", item.x, item.y, 0xFFFF4757)
        GameAudio.playSound(GameAudio.SoundEffect.COIN)
      }
      PickupType.POWER_SPEED -> {
        p.speedTimer = 12.0f
        addFloatingText("SPEED UP!", item.x, item.y, 0xFF00E5FF)
        GameAudio.playSound(GameAudio.SoundEffect.POWER_UP)
      }
      PickupType.POWER_BLAST -> {
        p.blastTimer = 12.0f
        addFloatingText("FROST BLAST!", item.x, item.y, 0xFFFF5722)
        GameAudio.playSound(GameAudio.SoundEffect.POWER_UP)
      }
      PickupType.POWER_RANGE -> {
        p.rangeTimer = 12.0f
        addFloatingText("MAX RANGE!", item.x, item.y, 0xFFFFD700)
        GameAudio.playSound(GameAudio.SoundEffect.POWER_UP)
      }
      PickupType.POWER_GIANT -> {
        p.giantTimer = 10.0f
        addFloatingText("GIANT MODE!", item.x, item.y, 0xFFE040FB)
        GameAudio.playSound(GameAudio.SoundEffect.POWER_UP)
      }
      PickupType.LETTER_F -> collectLetter(p, 'F', item.x, item.y)
      PickupType.LETTER_R -> collectLetter(p, 'R', item.x, item.y)
      PickupType.LETTER_O -> collectLetter(p, 'O', item.x, item.y)
      PickupType.LETTER_S -> collectLetter(p, 'S', item.x, item.y)
      PickupType.LETTER_T -> collectLetter(p, 'T', item.x, item.y)
    }
  }

  private fun collectLetter(p: Player, letter: Char, x: Float, y: Float) {
    collectedLetters.add(letter)
    GameAudio.playSound(GameAudio.SoundEffect.LETTER_COLLECT)
    addFloatingText("BONUS: $letter!", x, y, 0xFFFFD93D)

    if (collectedLetters.containsAll(listOf('F', 'R', 'O', 'S', 'T'))) {
      // Completed full word!
      p.score += 10000
      p.lives = (p.lives + 1).coerceAtMost(5)
      addFloatingText("F-R-O-S-T BONUS! +10,000 & 1UP!", LevelCatalog.GAME_WIDTH / 2f - 60f, 100f, 0xFFFFD93D)
      GameAudio.playSound(GameAudio.SoundEffect.LEVEL_CLEAR)
    }
  }

  private fun spawnDropsForKill(x: Float, y: Float, comboIndex: Int) {
    // Drop coins and gems
    pickups.add(Pickup(PickupType.COIN, x, y, vx = Random.nextFloat() * 40f - 20f))

    if (comboIndex >= 2) {
      pickups.add(Pickup(PickupType.BLUE_GEM, x + 8f, y, vx = Random.nextFloat() * 60f - 30f))
    }
    if (comboIndex >= 3) {
      pickups.add(Pickup(PickupType.RED_GEM, x - 8f, y, vx = Random.nextFloat() * 60f - 30f))
    }

    // Power-up chance
    if (Random.nextFloat() < 0.35f) {
      val powerTypes = listOf(PickupType.POWER_SPEED, PickupType.POWER_BLAST, PickupType.POWER_RANGE, PickupType.POWER_GIANT)
      pickups.add(Pickup(powerTypes.random(), x, y, vx = Random.nextFloat() * 30f - 15f))
    }

    // Bonus letter drop chance
    val missingLetters = listOf('F', 'R', 'O', 'S', 'T').filter { !collectedLetters.contains(it) }
    if (missingLetters.isNotEmpty() && (comboIndex >= 2 || Random.nextFloat() < 0.25f)) {
      val chosenLetter = missingLetters.random()
      val letterType = when (chosenLetter) {
        'F' -> PickupType.LETTER_F
        'R' -> PickupType.LETTER_R
        'O' -> PickupType.LETTER_O
        'S' -> PickupType.LETTER_S
        else -> PickupType.LETTER_T
      }
      pickups.add(Pickup(letterType, x, y, vx = Random.nextFloat() * 20f - 10f))
    }
  }

  private fun shatterSnowOrb(e: Enemy) {
    e.isEliminated = true
    e.isRolling = false
    GameAudio.playSound(GameAudio.SoundEffect.CHAIN_CRUSH)
    spawnParticleBurst(e.x + e.width / 2f, e.y + e.height / 2f, 0xFFF0F8FF, 20)
    // Small drop reward for rolling
    pickups.add(Pickup(PickupType.COIN, e.x + e.width / 2f, e.y))
  }

  private fun checkLevelCompletion(dt: Float) {
    var hasActiveEnemies = false
    for (e in enemies) {
      if (!e.isEliminated && e.type != EnemyType.PHANTOM) {
        hasActiveEnemies = true
        break
      }
    }
    if (!hasActiveEnemies) {
      bossDefeatedAnimation += dt
      if (bossDefeatedAnimation > 1.2f) {
        onLevelCleared()
      }
    }
  }

  private fun spawnParticleBurst(x: Float, y: Float, color: Long, count: Int) {
    if (particles.size > 60) return
    val spawnCount = minOf(count, 12)
    for (i in 0 until spawnCount) {
      val angle = Random.nextFloat() * 2.0 * Math.PI
      val speed = Random.nextFloat() * 100f + 25f
      particles.add(
        Particle(
          x = x,
          y = y,
          vx = (cos(angle) * speed).toFloat(),
          vy = (sin(angle) * speed).toFloat(),
          color = color,
          size = Random.nextFloat() * 3.5f + 1.5f,
          life = Random.nextFloat() * 0.35f + 0.15f,
          maxLife = 0.5f
        )
      )
    }
  }

  fun addFloatingText(text: String, x: Float, y: Float, color: Long) {
    if (floatingTexts.size > 8) {
      floatingTexts.removeAt(0)
    }
    floatingTexts.add(FloatingText(text, x, y, color = color))
  }
}
