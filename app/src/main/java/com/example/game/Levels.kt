package com.example.game

import kotlin.math.roundToInt

// Platform rectangle in virtual game coordinates
data class Platform(
  val x: Float,
  val y: Float,
  val width: Float,
  val height: Float = 12f,
  val isOneWay: Boolean = true
)

enum class ObstacleColor(val primaryColor: Long, val mouthGlow: Long, val displayName: String) {
  GREEN(0xFF2ECC71, 0xFFFF5722, "Green Gargoyle"),   // Green fire-spitting totem (Matches user screenshot)
  BLUE(0xFF29B6F6, 0xFF00E5FF, "Blue Frostbeast"),    // Blue frost totem
  RED(0xFFE74C3C, 0xFFFF9800, "Red Emberclaw"),       // Red demon totem
  YELLOW(0xFFFFCA28, 0xFFFF5722, "Golden Dragon")     // Gold beast totem
}

data class ObstacleHazard(
  val id: Int,
  val x: Float,
  val y: Float,
  val width: Float = 26f,
  val height: Float = 26f,
  val colorType: ObstacleColor = ObstacleColor.GREEN,
  val facesRight: Boolean = true,
  var attackTimer: Float = 0f,
  var attackInterval: Float = 4.0f,
  var isAttacking: Boolean = false,
  var attackAnimTimer: Float = 0f
)

data class HazardFireball(
  var x: Float,
  var y: Float,
  var vx: Float,
  var vy: Float = 0f,
  var radius: Float = 8.5f,
  var lifeTime: Float = 2.8f,
  val colorType: ObstacleColor = ObstacleColor.GREEN
)

enum class EnemyType {
  WISP,       // Ground waddler
  SKIMMER,    // Flying bat
  SPIKELING,  // Armored crawler
  PHANTOM,    // Hazard urgency enemy
  CRYO_TITAN  // Boss
}

enum class PickupType {
  COIN,
  BLUE_GEM,
  RED_GEM,
  POWER_SPEED,
  POWER_BLAST,
  POWER_RANGE,
  POWER_GIANT,
  LETTER_F,
  LETTER_R,
  LETTER_O,
  LETTER_S,
  LETTER_T
}

enum class WorldTheme(
  val displayName: String,
  val subTitle: String,
  val bgTop: Long,
  val bgMid: Long,
  val bgBot: Long,
  val brickColor: Long,
  val accentColor: Long
) {
  GLACIER_CAVERNS(
    displayName = "Glacier Caverns",
    subTitle = "Deep subterranean ice shelves",
    bgTop = 0xFF071B33,
    bgMid = 0xFF0F2B52,
    bgBot = 0xFF051121,
    brickColor = 0xFF1C4475,
    accentColor = 0xFF00E5FF
  ),
  FROST_CITADEL(
    displayName = "Frost Citadel",
    subTitle = "Ancient ramparts and frozen watchtowers",
    bgTop = 0xFF091F38,
    bgMid = 0xFF163C66,
    bgBot = 0xFF081526,
    brickColor = 0xFF234B7D,
    accentColor = 0xFF50E3C2
  ),
  CRYSTAL_SPIRE(
    displayName = "Crystal Spire",
    subTitle = "Resonant auroral high peaks",
    bgTop = 0xFF1C1236,
    bgMid = 0xFF2F1D58,
    bgBot = 0xFF100922,
    brickColor = 0xFF4A2F7D,
    accentColor = 0xFFE040FB
  ),
  BLIZZARD_RUINS(
    displayName = "Blizzard Ruins",
    subTitle = "Gale-force snow ravines",
    bgTop = 0xFF0F2930,
    bgMid = 0xFF174753,
    bgBot = 0xFF0A1B20,
    brickColor = 0xFF245B6B,
    accentColor = 0xFFFFD93D
  ),
  ABYSSAL_RIFT(
    displayName = "Abyssal Rift",
    subTitle = "Sub-zero volcanic magma vents",
    bgTop = 0xFF2D1020,
    bgMid = 0xFF42162E,
    bgBot = 0xFF170610,
    brickColor = 0xFF5E1F42,
    accentColor = 0xFFFF4757
  ),
  TITAN_THRONE(
    displayName = "Cryo-Titan Throne",
    subTitle = "Glacial sanctum of the ancient titan",
    bgTop = 0xFF2D144A,
    bgMid = 0xFF140827,
    bgBot = 0xFF4A1942,
    brickColor = 0xFF4A255C,
    accentColor = 0xFFFFD700
  )
}

data class LevelConfig(
  val levelNumber: Int,
  val name: String,
  val isBossLevel: Boolean,
  val platforms: List<Platform>,
  val enemySpawns: List<EnemySpawn>,
  val obstacleHazards: List<ObstacleHazard> = emptyList(),
  val theme: WorldTheme = WorldTheme.GLACIER_CAVERNS
)

data class EnemySpawn(
  val type: EnemyType,
  val x: Float,
  val y: Float,
  val delaySeconds: Float = 0f
)

object LevelCatalog {
  const val GAME_WIDTH = 400f
  const val GAME_HEIGHT = 600f
  const val TOTAL_LEVELS = 300

  // Floor platform common to all stages
  private val baseFloor = Platform(x = 0f, y = 540f, width = 400f, height = 24f, isOneWay = false)

  // 10 Distinct Architectural Platform Archetypes
  private val archetypes: List<(Int) -> List<Platform>> = listOf(
    // 0: Classic Dual Ramparts + Center Bridge (Snow Bros style)
    { _ ->
      listOf(
        baseFloor,
        Platform(x = 30f, y = 440f, width = 140f),
        Platform(x = 230f, y = 440f, width = 140f),
        Platform(x = 90f, y = 330f, width = 220f),
        Platform(x = 30f, y = 220f, width = 130f),
        Platform(x = 240f, y = 220f, width = 130f),
        Platform(x = 120f, y = 120f, width = 160f)
      )
    },
    // 1: Alternating Stepped Terraces
    { _ ->
      listOf(
        baseFloor,
        Platform(x = 20f, y = 450f, width = 110f),
        Platform(x = 270f, y = 450f, width = 110f),
        Platform(x = 130f, y = 380f, width = 140f),
        Platform(x = 40f, y = 290f, width = 130f),
        Platform(x = 230f, y = 290f, width = 130f),
        Platform(x = 110f, y = 190f, width = 180f),
        Platform(x = 30f, y = 105f, width = 100f),
        Platform(x = 270f, y = 105f, width = 100f)
      )
    },
    // 2: Dynamic Zig-Zag Spire
    { _ ->
      listOf(
        baseFloor,
        Platform(x = 20f, y = 455f, width = 230f),
        Platform(x = 150f, y = 375f, width = 230f),
        Platform(x = 20f, y = 295f, width = 230f),
        Platform(x = 150f, y = 210f, width = 230f),
        Platform(x = 80f, y = 125f, width = 240f)
      )
    },
    // 3: Boss Arena (Spacious floor with high defensive ledges)
    { _ ->
      listOf(
        baseFloor,
        Platform(x = 20f, y = 430f, width = 110f),
        Platform(x = 270f, y = 430f, width = 110f),
        Platform(x = 30f, y = 310f, width = 100f),
        Platform(x = 270f, y = 310f, width = 100f),
        Platform(x = 120f, y = 210f, width = 160f)
      )
    },
    // 4: Symmetrical Triple Column Cascade
    { _ ->
      listOf(
        baseFloor,
        Platform(x = 30f, y = 450f, width = 90f),
        Platform(x = 155f, y = 450f, width = 90f),
        Platform(x = 280f, y = 450f, width = 90f),
        Platform(x = 80f, y = 340f, width = 100f),
        Platform(x = 220f, y = 340f, width = 100f),
        Platform(x = 30f, y = 230f, width = 90f),
        Platform(x = 155f, y = 230f, width = 90f),
        Platform(x = 280f, y = 230f, width = 90f),
        Platform(x = 100f, y = 120f, width = 200f)
      )
    },
    // 5: Hourglass Funnel (Center choke point with wide escape wings)
    { _ ->
      listOf(
        baseFloor,
        Platform(x = 20f, y = 460f, width = 160f),
        Platform(x = 220f, y = 460f, width = 160f),
        Platform(x = 120f, y = 370f, width = 160f),
        Platform(x = 150f, y = 280f, width = 100f),
        Platform(x = 30f, y = 200f, width = 150f),
        Platform(x = 220f, y = 200f, width = 150f),
        Platform(x = 90f, y = 110f, width = 220f)
      )
    },
    // 6: Floating Diamond Sanctum
    { _ ->
      listOf(
        baseFloor,
        Platform(x = 130f, y = 460f, width = 140f),
        Platform(x = 40f, y = 370f, width = 120f),
        Platform(x = 240f, y = 370f, width = 120f),
        Platform(x = 20f, y = 270f, width = 100f),
        Platform(x = 140f, y = 270f, width = 120f),
        Platform(x = 280f, y = 270f, width = 100f),
        Platform(x = 50f, y = 170f, width = 130f),
        Platform(x = 220f, y = 170f, width = 130f),
        Platform(x = 130f, y = 90f, width = 140f)
      )
    },
    // 7: Twin Watchtowers (Vertical climbing chimneys)
    { _ ->
      listOf(
        baseFloor,
        Platform(x = 20f, y = 440f, width = 130f),
        Platform(x = 250f, y = 440f, width = 130f),
        Platform(x = 30f, y = 340f, width = 110f),
        Platform(x = 260f, y = 340f, width = 110f),
        Platform(x = 140f, y = 270f, width = 120f),
        Platform(x = 20f, y = 200f, width = 130f),
        Platform(x = 250f, y = 200f, width = 130f),
        Platform(x = 80f, y = 110f, width = 240f)
      )
    },
    // 8: Open Skylight Ramparts (Great for aerial freeze shots)
    { _ ->
      listOf(
        baseFloor,
        Platform(x = 50f, y = 460f, width = 300f),
        Platform(x = 20f, y = 360f, width = 120f),
        Platform(x = 260f, y = 360f, width = 120f),
        Platform(x = 100f, y = 260f, width = 200f),
        Platform(x = 20f, y = 160f, width = 130f),
        Platform(x = 250f, y = 160f, width = 130f),
        Platform(x = 140f, y = 80f, width = 120f)
      )
    },
    // 9: Labyrinth Fortress (Multi-tier challenge)
    { _ ->
      listOf(
        baseFloor,
        Platform(x = 20f, y = 470f, width = 100f),
        Platform(x = 160f, y = 470f, width = 80f),
        Platform(x = 280f, y = 470f, width = 100f),
        Platform(x = 80f, y = 390f, width = 140f),
        Platform(x = 260f, y = 390f, width = 110f),
        Platform(x = 30f, y = 300f, width = 120f),
        Platform(x = 190f, y = 300f, width = 140f),
        Platform(x = 80f, y = 210f, width = 160f),
        Platform(x = 270f, y = 210f, width = 100f),
        Platform(x = 110f, y = 110f, width = 180f)
      )
    }
  )

  /**
   * Deterministically generates any of the 300 levels on-demand.
   * Uses seed based on level number to provide stable, reproducible enemy spawns,
   * smooth difficulty scaling, and thematic world progressions every 50 stages.
   */
  fun getLevel(levelNumber: Int): LevelConfig {
    val lvl = levelNumber.coerceIn(1, TOTAL_LEVELS)
    val isBoss = (lvl % 10 == 0)

    // World theme (6 worlds, 50 levels each)
    val worldIndex = ((lvl - 1) / 50).coerceIn(0, 5)
    val theme = if (isBoss) WorldTheme.TITAN_THRONE else WorldTheme.entries[worldIndex]

    // Platform layout scaled with exact obstacle count (5 at level 1 up to 15 at level 300)
    val targetObstacleCount = (5f + (lvl - 1) * 10f / 299f).roundToInt().coerceIn(5, 15)
    val elevatedSlots = listOf(
      Platform(x = 25f, y = 460f, width = 130f),
      Platform(x = 245f, y = 460f, width = 130f),
      Platform(x = 90f, y = 390f, width = 220f),
      Platform(x = 20f, y = 320f, width = 120f),
      Platform(x = 260f, y = 320f, width = 120f),
      Platform(x = 110f, y = 250f, width = 180f),
      Platform(x = 25f, y = 180f, width = 110f),
      Platform(x = 265f, y = 180f, width = 110f),
      Platform(x = 125f, y = 110f, width = 150f),
      Platform(x = 160f, y = 460f, width = 80f),
      Platform(x = 20f, y = 390f, width = 60f),
      Platform(x = 320f, y = 390f, width = 60f),
      Platform(x = 165f, y = 320f, width = 70f),
      Platform(x = 135f, y = 50f, width = 130f)
    )
    val platforms = mutableListOf(baseFloor)
    platforms.addAll(elevatedSlots.take(targetObstacleCount - 1))

    // Obstacle Hazards (Wall gargoyles that attack, matching user's screenshot)
    val obstacleHazards = mutableListOf<ObstacleHazard>()
    // 1. Green Gargoyle (left wall, spits fire stream across stage)
    obstacleHazards.add(
      ObstacleHazard(
        id = 1,
        x = 16f,
        y = 320f - 26f,
        colorType = ObstacleColor.GREEN,
        facesRight = true,
        attackTimer = 0.8f,
        attackInterval = 3.6f
      )
    )
    // 2. Right wall totem (Blue Frostbeast)
    obstacleHazards.add(
      ObstacleHazard(
        id = 2,
        x = 358f,
        y = 250f - 26f,
        colorType = ObstacleColor.BLUE,
        facesRight = false,
        attackTimer = 2.4f,
        attackInterval = 4.2f
      )
    )
    if (lvl >= 15) {
      // 3. Red Emberclaw (attacks from tier 4)
      obstacleHazards.add(
        ObstacleHazard(
          id = 3,
          x = 20f,
          y = 180f - 26f,
          colorType = ObstacleColor.RED,
          facesRight = true,
          attackTimer = 1.4f,
          attackInterval = 4.0f
        )
      )
    }
    if (lvl >= 50) {
      // 4. Golden Dragon (tier 2 right)
      obstacleHazards.add(
        ObstacleHazard(
          id = 4,
          x = 356f,
          y = 390f - 26f,
          colorType = ObstacleColor.YELLOW,
          facesRight = false,
          attackTimer = 3.0f,
          attackInterval = 4.5f
        )
      )
    }
    if (lvl >= 100) {
      // 5. Additional Green Gargoyle for intense arcade action
      obstacleHazards.add(
        ObstacleHazard(
          id = 5,
          x = 18f,
          y = 460f - 26f,
          colorType = ObstacleColor.GREEN,
          facesRight = true,
          attackTimer = 2.0f,
          attackInterval = 3.8f
        )
      )
    }

    // Stage Name Generation
    val name = when {
      lvl == 300 -> "Omega Core: Supreme Cryo-Titan"
      isBoss -> "${theme.displayName} Core: Titan ${lvl / 10}"
      else -> {
        val prefixes = listOf(
          "Frost", "Crystal", "Glacier", "Arctic", "Blizzard",
          "Sub-Zero", "Permafrost", "Aurora", "Icefall", "Abyssal"
        )
        val suffixes = listOf(
          "Ramparts", "Cavern", "Spire", "Sanctum", "Bastion",
          "Chasm", "Terrace", "Labyrinth", "Depths", "Keep"
        )
        val prefix = prefixes[((lvl * 7) + 3) % prefixes.size]
        val suffix = suffixes[((lvl * 13) + 5) % suffixes.size]
        "$prefix $suffix"
      }
    }

    // Generate Enemy Spawns scaled to level
    val enemySpawns = mutableListOf<EnemySpawn>()

    if (isBoss) {
      // Boss level
      enemySpawns.add(EnemySpawn(EnemyType.CRYO_TITAN, 160f, 440f))
      // Minions spawn for snow ammo
      val minionCount = (2 + (lvl / 60)).coerceAtMost(6)
      for (i in 0 until minionCount) {
        val mx = if (i % 2 == 0) 50f + (i * 20f) else 330f - (i * 20f)
        val my = if (i < 2) 390f else 180f
        enemySpawns.add(
          EnemySpawn(
            type = if (i % 3 == 0) EnemyType.SPIKELING else EnemyType.WISP,
            x = mx,
            y = my,
            delaySeconds = (i * 0.8f)
          )
        )
      }
    } else {
      // Normal stages: scale from 3 enemies up to 8 enemies gradually
      val baseCount = 3 + (lvl / 45) // 3 enemies at lvl 1 -> 8 enemies at lvl 250+
      val totalEnemies = baseCount.coerceIn(3, 8)

      // Available spawn tiers based on platforms
      val spawnNodes = listOf(
        Pair(60f, 400f),
        Pair(300f, 400f),
        Pair(170f, 320f),
        Pair(60f, 250f),
        Pair(310f, 250f),
        Pair(180f, 150f),
        Pair(70f, 80f),
        Pair(290f, 80f)
      )

      for (i in 0 until totalEnemies) {
        val node = spawnNodes[i % spawnNodes.size]
        // Vary coordinates slightly with level seed
        val offsetX = ((lvl * 11 + i * 17) % 30) - 15f
        val spawnX = (node.first + offsetX).coerceIn(30f, 350f)
        val spawnY = node.second

        // Enemy type distribution based on progression
        val enemyType = when {
          lvl < 5 -> EnemyType.WISP
          lvl < 20 -> if (i % 3 == 0) EnemyType.SKIMMER else EnemyType.WISP
          lvl < 50 -> when (i % 3) {
            0 -> EnemyType.WISP
            1 -> EnemyType.SKIMMER
            else -> EnemyType.SPIKELING
          }
          else -> {
            val roll = (lvl * 19 + i * 23) % 100
            when {
              roll < 35 -> EnemyType.WISP
              roll < 70 -> EnemyType.SKIMMER
              else -> EnemyType.SPIKELING
            }
          }
        }

        enemySpawns.add(
          EnemySpawn(
            type = enemyType,
            x = spawnX,
            y = spawnY,
            delaySeconds = if (i > 4) (i - 4) * 1.5f else 0f
          )
        )
      }
    }

    return LevelConfig(
      levelNumber = lvl,
      name = name,
      isBossLevel = isBoss,
      platforms = platforms,
      enemySpawns = enemySpawns,
      obstacleHazards = obstacleHazards,
      theme = theme
    )
  }

  // Pre-cached small list for backward compatibility where a list is expected
  val levels: List<LevelConfig> by lazy {
    (1..TOTAL_LEVELS).map { getLevel(it) }
  }
}
