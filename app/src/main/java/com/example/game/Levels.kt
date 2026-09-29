package com.example.game

// Platform rectangle in virtual game coordinates
data class Platform(
  val x: Float,
  val y: Float,
  val width: Float,
  val height: Float = 12f,
  val isOneWay: Boolean = true
)

enum class ObstacleColor(val primaryColor: Long, val mouthGlow: Long, val displayName: String) {
  GREEN(0xFF2ECC71, 0xFFFF5722, "Green Gargoyle"),
  BLUE(0xFF29B6F6, 0xFF00E5FF, "Blue Frostbeast"),
  RED(0xFFE74C3C, 0xFFFF9800, "Red Emberclaw"),
  YELLOW(0xFFFFCA28, 0xFFFF5722, "Golden Dragon")
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
  const val TOTAL_LEVELS = 1020

  // Floor platform common to all stages (solid base floor)
  private val baseFloor = Platform(x = 0f, y = 540f, width = 400f, height = 24f, isOneWay = false)

  // 10 Distinct Architectural Platform Archetypes featuring iconic arcade layouts.
  // In every single archetype, tier elevations are spaced by 75f-80f, and all jumpable platforms
  // have isOneWay = true, providing clear and guaranteed upward climbing routes.
  private val archetypes: List<(Int) -> List<Platform>> = listOf(
    // 0: Pyramid Sanctum (Stages 1, 11, 21, ...)
    // Central stepped mountain with left and right wing staircases
    { _ ->
      listOf(
        baseFloor,
        // Tier 1 (y = 460) - 80px above floor
        Platform(x = 20f, y = 460f, width = 85f),
        Platform(x = 125f, y = 460f, width = 150f),
        Platform(x = 295f, y = 460f, width = 85f),
        // Tier 2 (y = 380) - 80px above Tier 1
        Platform(x = 15f, y = 380f, width = 90f),
        Platform(x = 140f, y = 380f, width = 120f),
        Platform(x = 295f, y = 380f, width = 90f),
        // Tier 3 (y = 300) - 80px above Tier 2
        Platform(x = 25f, y = 300f, width = 85f),
        Platform(x = 150f, y = 300f, width = 100f),
        Platform(x = 290f, y = 300f, width = 85f),
        // Tier 4 (y = 220) - 80px above Tier 3
        Platform(x = 35f, y = 220f, width = 85f),
        Platform(x = 160f, y = 220f, width = 80f),
        Platform(x = 280f, y = 220f, width = 85f),
        // Tier 5 (y = 140) - 80px above Tier 4
        Platform(x = 25f, y = 140f, width = 100f),
        Platform(x = 160f, y = 140f, width = 80f),
        Platform(x = 275f, y = 140f, width = 100f),
        // Crown Lookout (y = 75)
        Platform(x = 125f, y = 75f, width = 150f)
      )
    },

    // 1: Chutes & Wells / Comb Towers (Stages 2, 12, 22, ... - Specifically fixed for full upward mobility)
    // 3 parallel vertical climbing channels (Left, Center, Right) across all 5 tiers
    { _ ->
      listOf(
        baseFloor,
        // Tier 1 (y = 460) - Immediate 80px hop from floor across full width
        Platform(x = 20f, y = 460f, width = 105f),
        Platform(x = 145f, y = 460f, width = 110f),
        Platform(x = 275f, y = 460f, width = 105f),
        // Tier 2 (y = 380) - Clear comb shelves with jump lanes
        Platform(x = 25f, y = 380f, width = 105f),
        Platform(x = 145f, y = 380f, width = 110f),
        Platform(x = 270f, y = 380f, width = 105f),
        // Tier 3 (y = 300) - Mid-tier jump platforms
        Platform(x = 20f, y = 300f, width = 110f),
        Platform(x = 150f, y = 300f, width = 100f),
        Platform(x = 270f, y = 300f, width = 110f),
        // Tier 4 (y = 220) - High terrace steps
        Platform(x = 30f, y = 220f, width = 105f),
        Platform(x = 155f, y = 220f, width = 90f),
        Platform(x = 265f, y = 220f, width = 105f),
        // Tier 5 (y = 140) - Attic decks
        Platform(x = 20f, y = 140f, width = 110f),
        Platform(x = 145f, y = 140f, width = 110f),
        Platform(x = 270f, y = 140f, width = 110f),
        // Crown Sky Deck (y = 75)
        Platform(x = 115f, y = 75f, width = 170f)
      )
    },

    // 2: Octagonal Ring Fortress & Central Altar (Stages 3, 13, 23, ...)
    { _ ->
      listOf(
        baseFloor,
        // Tier 1 (y = 460)
        Platform(x = 15f, y = 460f, width = 90f),
        Platform(x = 140f, y = 460f, width = 120f),
        Platform(x = 295f, y = 460f, width = 90f),
        // Tier 2 (y = 380)
        Platform(x = 25f, y = 380f, width = 95f),
        Platform(x = 145f, y = 380f, width = 110f),
        Platform(x = 280f, y = 380f, width = 95f),
        // Tier 3 (y = 300)
        Platform(x = 20f, y = 300f, width = 90f),
        Platform(x = 135f, y = 300f, width = 130f),
        Platform(x = 290f, y = 300f, width = 90f),
        // Tier 4 (y = 220)
        Platform(x = 35f, y = 220f, width = 95f),
        Platform(x = 150f, y = 220f, width = 100f),
        Platform(x = 270f, y = 220f, width = 95f),
        // Tier 5 (y = 140)
        Platform(x = 25f, y = 140f, width = 105f),
        Platform(x = 145f, y = 140f, width = 110f),
        Platform(x = 270f, y = 140f, width = 105f),
        // Spire Roof (y = 75)
        Platform(x = 120f, y = 75f, width = 160f)
      )
    },

    // 3: Titan Throne Arena (Boss Stages: 10, 20, 30, ... 1020)
    { _ ->
      listOf(
        baseFloor,
        // Tier 1 (y = 460)
        Platform(x = 20f, y = 460f, width = 110f),
        Platform(x = 155f, y = 460f, width = 90f),
        Platform(x = 270f, y = 460f, width = 110f),
        // Tier 2 (y = 380)
        Platform(x = 30f, y = 380f, width = 100f),
        Platform(x = 145f, y = 380f, width = 110f),
        Platform(x = 270f, y = 380f, width = 100f),
        // Tier 3 (y = 300)
        Platform(x = 20f, y = 300f, width = 110f),
        Platform(x = 140f, y = 300f, width = 120f),
        Platform(x = 270f, y = 300f, width = 110f),
        // Tier 4 (y = 220)
        Platform(x = 25f, y = 220f, width = 105f),
        Platform(x = 150f, y = 220f, width = 100f),
        Platform(x = 270f, y = 220f, width = 105f),
        // Tier 5 (y = 140)
        Platform(x = 20f, y = 140f, width = 110f),
        Platform(x = 145f, y = 140f, width = 110f),
        Platform(x = 270f, y = 140f, width = 110f),
        // High Command Deck (y = 75)
        Platform(x = 110f, y = 75f, width = 180f)
      )
    },

    // 4: Twin Watchtowers & Center Bridge (Stages 5, 15, 25, ...)
    { _ ->
      listOf(
        baseFloor,
        // Tier 1 (y = 460)
        Platform(x = 20f, y = 460f, width = 120f),
        Platform(x = 165f, y = 460f, width = 70f),
        Platform(x = 260f, y = 460f, width = 120f),
        // Tier 2 (y = 380)
        Platform(x = 25f, y = 380f, width = 110f),
        Platform(x = 155f, y = 380f, width = 90f),
        Platform(x = 265f, y = 380f, width = 110f),
        // Tier 3 (y = 300)
        Platform(x = 20f, y = 300f, width = 120f),
        Platform(x = 150f, y = 300f, width = 100f),
        Platform(x = 260f, y = 300f, width = 120f),
        // Tier 4 (y = 220)
        Platform(x = 25f, y = 220f, width = 110f),
        Platform(x = 155f, y = 220f, width = 90f),
        Platform(x = 265f, y = 220f, width = 110f),
        // Tier 5 (y = 140)
        Platform(x = 20f, y = 140f, width = 120f),
        Platform(x = 145f, y = 140f, width = 110f),
        Platform(x = 260f, y = 140f, width = 120f),
        // Watchtower Spires (y = 75)
        Platform(x = 35f, y = 75f, width = 90f),
        Platform(x = 275f, y = 75f, width = 90f)
      )
    },

    // 5: Grand Zig-Zag Switchback (Stages 6, 16, 26, ...)
    { _ ->
      listOf(
        baseFloor,
        // Tier 1 (y = 460)
        Platform(x = 20f, y = 460f, width = 260f),
        Platform(x = 305f, y = 460f, width = 75f),
        // Tier 2 (y = 380)
        Platform(x = 20f, y = 380f, width = 75f),
        Platform(x = 120f, y = 380f, width = 260f),
        // Tier 3 (y = 300)
        Platform(x = 20f, y = 300f, width = 260f),
        Platform(x = 305f, y = 300f, width = 75f),
        // Tier 4 (y = 220)
        Platform(x = 20f, y = 220f, width = 75f),
        Platform(x = 120f, y = 220f, width = 260f),
        // Tier 5 (y = 140)
        Platform(x = 40f, y = 140f, width = 140f),
        Platform(x = 220f, y = 140f, width = 140f),
        // Apex Bridge (y = 75)
        Platform(x = 115f, y = 75f, width = 170f)
      )
    },

    // 6: Floating Diamond Sanctum (Stages 7, 17, 27, ...)
    { _ ->
      listOf(
        baseFloor,
        // Tier 1 (y = 460)
        Platform(x = 25f, y = 460f, width = 95f),
        Platform(x = 145f, y = 460f, width = 110f),
        Platform(x = 280f, y = 460f, width = 95f),
        // Tier 2 (y = 380)
        Platform(x = 40f, y = 380f, width = 100f),
        Platform(x = 260f, y = 380f, width = 100f),
        // Tier 3 (y = 300)
        Platform(x = 20f, y = 300f, width = 95f),
        Platform(x = 140f, y = 300f, width = 120f),
        Platform(x = 285f, y = 300f, width = 95f),
        // Tier 4 (y = 220)
        Platform(x = 40f, y = 220f, width = 100f),
        Platform(x = 260f, y = 220f, width = 100f),
        // Tier 5 (y = 140)
        Platform(x = 25f, y = 140f, width = 95f),
        Platform(x = 145f, y = 140f, width = 110f),
        Platform(x = 280f, y = 140f, width = 95f),
        // Top Prism (y = 75)
        Platform(x = 130f, y = 75f, width = 140f)
      )
    },

    // 7: Triple Column Cascade (Stages 8, 18, 28, ...)
    { _ ->
      listOf(
        baseFloor,
        // Tier 1 (y = 460)
        Platform(x = 25f, y = 460f, width = 90f),
        Platform(x = 155f, y = 460f, width = 90f),
        Platform(x = 285f, y = 460f, width = 90f),
        // Tier 2 (y = 380)
        Platform(x = 85f, y = 380f, width = 100f),
        Platform(x = 215f, y = 380f, width = 100f),
        // Tier 3 (y = 300)
        Platform(x = 25f, y = 300f, width = 90f),
        Platform(x = 155f, y = 300f, width = 90f),
        Platform(x = 285f, y = 300f, width = 90f),
        // Tier 4 (y = 220)
        Platform(x = 85f, y = 220f, width = 100f),
        Platform(x = 215f, y = 220f, width = 100f),
        // Tier 5 (y = 140)
        Platform(x = 25f, y = 140f, width = 90f),
        Platform(x = 155f, y = 140f, width = 90f),
        Platform(x = 285f, y = 140f, width = 90f),
        // Crown Deck (y = 75)
        Platform(x = 110f, y = 75f, width = 180f)
      )
    },

    // 8: Hourglass Funnel (Stages 9, 19, 29, ...)
    { _ ->
      listOf(
        baseFloor,
        // Tier 1 (y = 460)
        Platform(x = 20f, y = 460f, width = 130f),
        Platform(x = 250f, y = 460f, width = 130f),
        // Tier 2 (y = 380)
        Platform(x = 65f, y = 380f, width = 105f),
        Platform(x = 230f, y = 380f, width = 105f),
        // Tier 3 (y = 300)
        Platform(x = 15f, y = 300f, width = 75f),
        Platform(x = 130f, y = 300f, width = 140f),
        Platform(x = 310f, y = 300f, width = 75f),
        // Tier 4 (y = 220)
        Platform(x = 65f, y = 220f, width = 105f),
        Platform(x = 230f, y = 220f, width = 105f),
        // Tier 5 (y = 140)
        Platform(x = 20f, y = 140f, width = 130f),
        Platform(x = 250f, y = 140f, width = 130f),
        // Top Funnel Crown (y = 75)
        Platform(x = 115f, y = 75f, width = 170f)
      )
    },

    // 9: Labyrinth Fortress Keep (Stages 4, 14, 24, ...)
    { _ ->
      listOf(
        baseFloor,
        // Tier 1 (y = 460)
        Platform(x = 25f, y = 460f, width = 100f),
        Platform(x = 150f, y = 460f, width = 100f),
        Platform(x = 275f, y = 460f, width = 100f),
        // Tier 2 (y = 380)
        Platform(x = 75f, y = 380f, width = 115f),
        Platform(x = 210f, y = 380f, width = 115f),
        // Tier 3 (y = 300)
        Platform(x = 25f, y = 300f, width = 100f),
        Platform(x = 150f, y = 300f, width = 100f),
        Platform(x = 275f, y = 300f, width = 100f),
        // Tier 4 (y = 220)
        Platform(x = 75f, y = 220f, width = 115f),
        Platform(x = 210f, y = 220f, width = 115f),
        // Tier 5 (y = 140)
        Platform(x = 30f, y = 140f, width = 95f),
        Platform(x = 145f, y = 140f, width = 110f),
        Platform(x = 275f, y = 140f, width = 95f),
        // High Citadel Deck (y = 75)
        Platform(x = 110f, y = 75f, width = 180f)
      )
    }
  )

  /**
   * Deterministically generates any of the 1020 levels on-demand.
   * All 1020 stages have full, fully connected platform trees with clear upward paths.
   */
  fun getLevel(levelNumber: Int): LevelConfig {
    val lvl = levelNumber.coerceIn(1, TOTAL_LEVELS)
    val isBoss = (lvl % 10 == 0)

    // World theme (6 themes rotating every 50 stages)
    val worldIndex = ((lvl - 1) / 50) % WorldTheme.entries.size
    val theme = if (isBoss) WorldTheme.TITAN_THRONE else WorldTheme.entries[worldIndex]

    // Determine archetype:
    // Boss stages always use the Titan Throne Arena (Archetype 3)
    // Other stages cycle deterministically through archetypes 0-9
    val archIndex = if (isBoss) 3 else ((lvl - 1) % 10)
    // Use the complete, intact archetype with full upward climbing routes
    val platforms = archetypes[archIndex](lvl)

    // Obstacle Hazards (Wall gargoyles spitting fireballs)
    val obstacleHazards = mutableListOf<ObstacleHazard>()
    when (archIndex) {
      0 -> {
        // Pyramid Sanctum: Side gargoyles
        obstacleHazards.add(
          ObstacleHazard(
            id = 1,
            x = 16f,
            y = 380f - 26f,
            colorType = ObstacleColor.GREEN,
            facesRight = true,
            attackTimer = 0.8f,
            attackInterval = 3.6f
          )
        )
        obstacleHazards.add(
          ObstacleHazard(
            id = 2,
            x = 358f,
            y = 380f - 26f,
            colorType = ObstacleColor.BLUE,
            facesRight = false,
            attackTimer = 2.2f,
            attackInterval = 4.0f
          )
        )
        if (lvl >= 15) {
          obstacleHazards.add(
            ObstacleHazard(
              id = 3,
              x = 16f,
              y = 220f - 26f,
              colorType = ObstacleColor.RED,
              facesRight = true,
              attackTimer = 1.5f,
              attackInterval = 3.8f
            )
          )
        }
        if (lvl >= 50) {
          obstacleHazards.add(
            ObstacleHazard(
              id = 4,
              x = 358f,
              y = 220f - 26f,
              colorType = ObstacleColor.YELLOW,
              facesRight = false,
              attackTimer = 3.0f,
              attackInterval = 4.2f
            )
          )
        }
      }
      1 -> {
        // Chutes & Wells: Left and right gargoyles
        obstacleHazards.add(
          ObstacleHazard(
            id = 1,
            x = 16f,
            y = 380f - 26f,
            colorType = ObstacleColor.GREEN,
            facesRight = true,
            attackTimer = 1.0f,
            attackInterval = 3.5f
          )
        )
        obstacleHazards.add(
          ObstacleHazard(
            id = 2,
            x = 358f,
            y = 380f - 26f,
            colorType = ObstacleColor.BLUE,
            facesRight = false,
            attackTimer = 2.5f,
            attackInterval = 4.2f
          )
        )
        if (lvl >= 20) {
          obstacleHazards.add(
            ObstacleHazard(
              id = 3,
              x = 16f,
              y = 220f - 26f,
              colorType = ObstacleColor.YELLOW,
              facesRight = true,
              attackTimer = 1.8f,
              attackInterval = 3.8f
            )
          )
        }
      }
      2 -> {
        // Ring Fortress
        obstacleHazards.add(
          ObstacleHazard(
            id = 1,
            x = 16f,
            y = 380f - 26f,
            colorType = ObstacleColor.GREEN,
            facesRight = true,
            attackTimer = 0.9f,
            attackInterval = 3.6f
          )
        )
        obstacleHazards.add(
          ObstacleHazard(
            id = 2,
            x = 358f,
            y = 380f - 26f,
            colorType = ObstacleColor.BLUE,
            facesRight = false,
            attackTimer = 2.4f,
            attackInterval = 4.0f
          )
        )
        if (lvl >= 25) {
          obstacleHazards.add(
            ObstacleHazard(
              id = 3,
              x = 16f,
              y = 220f - 26f,
              colorType = ObstacleColor.RED,
              facesRight = true,
              attackTimer = 1.4f,
              attackInterval = 3.7f
            )
          )
        }
      }
      else -> {
        // General & Boss Stages
        obstacleHazards.add(
          ObstacleHazard(
            id = 1,
            x = 16f,
            y = 380f - 26f,
            colorType = ObstacleColor.GREEN,
            facesRight = true,
            attackTimer = 1.0f,
            attackInterval = 3.6f
          )
        )
        obstacleHazards.add(
          ObstacleHazard(
            id = 2,
            x = 358f,
            y = 380f - 26f,
            colorType = ObstacleColor.BLUE,
            facesRight = false,
            attackTimer = 2.6f,
            attackInterval = 4.2f
          )
        )
        if (lvl >= 30) {
          obstacleHazards.add(
            ObstacleHazard(
              id = 3,
              x = 16f,
              y = 220f - 26f,
              colorType = ObstacleColor.RED,
              facesRight = true,
              attackTimer = 1.6f,
              attackInterval = 3.9f
            )
          )
        }
      }
    }

    // Stage Name Generation
    val name = when {
      lvl == 1020 -> "Omega Core: Supreme Cryo-Titan"
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

    // Enemy Spawns scaled to level
    val enemySpawns = mutableListOf<EnemySpawn>()

    if (isBoss) {
      // Boss level: Titan + minions
      enemySpawns.add(EnemySpawn(EnemyType.CRYO_TITAN, 160f, 440f))
      val minionCount = (2 + (lvl / 60)).coerceAtMost(6)
      for (i in 0 until minionCount) {
        val mx = if (i % 2 == 0) 50f + (i * 25f) else 330f - (i * 25f)
        val my = if (i < 2) 410f else 250f
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
      // Normal stages: scale from 3 up to 8 enemies gradually
      val baseCount = 3 + (lvl / 45)
      val totalEnemies = baseCount.coerceIn(3, 8)

      // Spawn nodes placed directly on platforms for natural spawning
      val spawnNodes = when (archIndex) {
        0 -> listOf(
          Pair(60f, 420f),
          Pair(200f, 420f),
          Pair(340f, 420f),
          Pair(60f, 340f),
          Pair(200f, 340f),
          Pair(340f, 340f),
          Pair(70f, 260f),
          Pair(200f, 260f)
        )
        1 -> listOf(
          Pair(70f, 420f),
          Pair(200f, 420f),
          Platform_Spawn(330f, 420f),
          Pair(75f, 340f),
          Pair(200f, 340f),
          Pair(325f, 340f),
          Pair(75f, 260f),
          Pair(200f, 260f)
        )
        else -> listOf(
          Pair(70f, 420f),
          Pair(200f, 420f),
          Pair(330f, 420f),
          Pair(80f, 340f),
          Pair(200f, 340f),
          Pair(320f, 340f),
          Pair(80f, 260f),
          Pair(200f, 260f)
        )
      }

      for (i in 0 until totalEnemies) {
        val node = spawnNodes[i % spawnNodes.size]
        val offsetX = ((lvl * 11 + i * 17) % 24) - 12f
        val spawnX = (node.first + offsetX).coerceIn(30f, 350f)
        val spawnY = node.second

        // Enemy type progression
        val enemyType = when {
          archIndex == 2 && i % 2 == 1 -> EnemyType.PHANTOM
          lvl < 5 -> if (i % 2 == 1) EnemyType.SKIMMER else EnemyType.WISP
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
              roll < 65 -> EnemyType.SKIMMER
              roll < 85 -> EnemyType.SPIKELING
              else -> EnemyType.PHANTOM
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

  private fun Platform_Spawn(x: Float, y: Float) = Pair(x, y)
}
