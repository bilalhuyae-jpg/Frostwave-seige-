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

  // 10 Distinct Architectural Platform Archetypes featuring iconic Snow Bros layouts
  private val archetypes: List<(Int) -> List<Platform>> = listOf(
    // 0: Pyramid Sanctum (Directly from Screenshot 1)
    // Central stepped mountain with side ledges and apex perch
    { _ ->
      listOf(
        baseFloor,
        Platform(x = 75f, y = 450f, width = 250f),  // Pyramid Tier 1 (Base)
        Platform(x = 110f, y = 380f, width = 180f), // Pyramid Tier 2
        Platform(x = 145f, y = 310f, width = 110f), // Pyramid Tier 3
        Platform(x = 175f, y = 240f, width = 50f),  // Pyramid Apex
        Platform(x = 10f, y = 370f, width = 65f),   // Left Mid Ledge
        Platform(x = 325f, y = 370f, width = 65f),  // Right Mid Ledge
        Platform(x = 10f, y = 240f, width = 65f),   // Left High Ledge
        Platform(x = 325f, y = 240f, width = 65f),  // Right High Ledge
        Platform(x = 80f, y = 160f, width = 70f),   // Upper Sky Left
        Platform(x = 250f, y = 160f, width = 70f),  // Upper Sky Right
        Platform(x = 130f, y = 100f, width = 140f), // Ceiling Bridge
        Platform(x = 10f, y = 100f, width = 60f),   // Left Crow Nest
        Platform(x = 330f, y = 100f, width = 60f),  // Right Crow Nest
        Platform(x = 175f, y = 170f, width = 50f)   // Apex Cloud Step
      )
    },

    // 1: Vertical Chutes & Comb Wells (Directly from Screenshot 2)
    // Horizontal comb spine with vertical dividing teeth forming drop wells, lower pits, top ledges
    { _ ->
      listOf(
        baseFloor,
        Platform(x = 20f, y = 350f, width = 360f),  // Comb Horizontal Spine
        Platform(x = 30f, y = 260f, width = 22f, height = 90f, isOneWay = false),  // Chute Wall 1
        Platform(x = 135f, y = 260f, width = 22f, height = 90f, isOneWay = false), // Chute Wall 2
        Platform(x = 245f, y = 260f, width = 22f, height = 90f, isOneWay = false), // Chute Wall 3
        Platform(x = 350f, y = 260f, width = 22f, height = 90f, isOneWay = false), // Chute Wall 4
        Platform(x = 15f, y = 140f, width = 75f),   // Upper Ledge 1
        Platform(x = 115f, y = 140f, width = 75f),  // Upper Ledge 2
        Platform(x = 210f, y = 140f, width = 75f),  // Upper Ledge 3
        Platform(x = 310f, y = 140f, width = 75f),  // Upper Ledge 4
        Platform(x = 30f, y = 460f, width = 95f),   // Lower Pit Left
        Platform(x = 155f, y = 460f, width = 90f),  // Lower Pit Center
        Platform(x = 275f, y = 460f, width = 95f),  // Lower Pit Right
        Platform(x = 130f, y = 70f, width = 140f),  // Skylight Beam
        Platform(x = 10f, y = 70f, width = 60f),    // Attic Left
        Platform(x = 330f, y = 70f, width = 60f)    // Attic Right
      )
    },

    // 2: Octagonal Ring Fortress & Central Altar (Directly from Screenshot 3)
    // Hollow enclosed sanctum with floating altar in the middle and perimeter stairs
    { _ ->
      listOf(
        baseFloor,
        Platform(x = 130f, y = 200f, width = 140f), // Fortress Top Arch
        Platform(x = 80f, y = 265f, width = 55f),   // Left Upper Slope
        Platform(x = 80f, y = 355f, width = 55f),   // Left Lower Slope
        Platform(x = 265f, y = 265f, width = 55f),  // Right Upper Slope
        Platform(x = 265f, y = 355f, width = 55f),  // Right Lower Slope
        Platform(x = 115f, y = 425f, width = 170f), // Fortress Floor
        Platform(x = 170f, y = 315f, width = 60f),  // Central Altar Pedestal
        Platform(x = 10f, y = 470f, width = 65f),   // Outer Stair Left 1
        Platform(x = 325f, y = 470f, width = 65f),  // Outer Stair Right 1
        Platform(x = 10f, y = 340f, width = 65f),   // Outer Stair Left 2
        Platform(x = 325f, y = 340f, width = 65f),  // Outer Stair Right 2
        Platform(x = 10f, y = 210f, width = 65f),   // Outer Stair Left 3
        Platform(x = 325f, y = 210f, width = 65f),  // Outer Stair Right 3
        Platform(x = 110f, y = 110f, width = 180f), // Fortress Spire Roof
        Platform(x = 160f, y = 480f, width = 80f)   // Base Pedestal Ledge
      )
    },

    // 3: Titan Throne Boss Arena (Directly from Screenshot 4)
    // Multi-tier battle ramparts above the giant boss chamber with checkerboard ledges
    { _ ->
      listOf(
        baseFloor,
        Platform(x = 50f, y = 295f, width = 300f),  // Main Cross Highway
        Platform(x = 10f, y = 405f, width = 95f),   // Lower Wing Left
        Platform(x = 295f, y = 405f, width = 95f),  // Lower Wing Right
        Platform(x = 20f, y = 195f, width = 135f),  // Sniping Deck Left
        Platform(x = 245f, y = 195f, width = 135f), // Sniping Deck Right
        Platform(x = 115f, y = 95f, width = 170f),  // High Command Bridge
        Platform(x = 160f, y = 405f, width = 80f),  // Boss Head Hazard Step
        Platform(x = 10f, y = 95f, width = 85f),    // High Left Turret
        Platform(x = 305f, y = 95f, width = 85f),   // High Right Turret
        Platform(x = 160f, y = 195f, width = 80f),  // Middle Drop Shaft
        Platform(x = 10f, y = 490f, width = 60f),   // Low Left Bunker
        Platform(x = 330f, y = 490f, width = 60f),  // Low Right Bunker
        Platform(x = 170f, y = 490f, width = 60f),  // Low Center Shield
        Platform(x = 120f, y = 245f, width = 160f)  // Mid-Tier Deflector
      )
    },

    // 4: Dual Watchtowers & Center Chasm
    { _ ->
      listOf(
        baseFloor,
        Platform(x = 20f, y = 440f, width = 120f),
        Platform(x = 260f, y = 440f, width = 120f),
        Platform(x = 25f, y = 330f, width = 110f),
        Platform(x = 265f, y = 330f, width = 110f),
        Platform(x = 120f, y = 270f, width = 160f),
        Platform(x = 20f, y = 210f, width = 120f),
        Platform(x = 260f, y = 210f, width = 120f),
        Platform(x = 100f, y = 110f, width = 200f),
        Platform(x = 140f, y = 370f, width = 120f),
        Platform(x = 30f, y = 100f, width = 60f),
        Platform(x = 310f, y = 100f, width = 60f),
        Platform(x = 150f, y = 480f, width = 100f),
        Platform(x = 80f, y = 190f, width = 60f),
        Platform(x = 260f, y = 190f, width = 60f),
        Platform(x = 160f, y = 50f, width = 80f)
      )
    },

    // 5: Grand Zig-Zag Switchback
    { _ ->
      listOf(
        baseFloor,
        Platform(x = 20f, y = 460f, width = 260f),
        Platform(x = 120f, y = 380f, width = 260f),
        Platform(x = 20f, y = 300f, width = 260f),
        Platform(x = 120f, y = 210f, width = 260f),
        Platform(x = 70f, y = 120f, width = 260f),
        Platform(x = 295f, y = 460f, width = 85f),
        Platform(x = 20f, y = 380f, width = 85f),
        Platform(x = 295f, y = 300f, width = 85f),
        Platform(x = 20f, y = 210f, width = 85f),
        Platform(x = 150f, y = 500f, width = 100f),
        Platform(x = 10f, y = 120f, width = 50f),
        Platform(x = 340f, y = 120f, width = 50f),
        Platform(x = 120f, y = 60f, width = 160f),
        Platform(x = 160f, y = 340f, width = 80f),
        Platform(x = 160f, y = 170f, width = 80f)
      )
    },

    // 6: Floating Diamond Sanctum
    { _ ->
      listOf(
        baseFloor,
        Platform(x = 135f, y = 460f, width = 130f),
        Platform(x = 35f, y = 370f, width = 120f),
        Platform(x = 245f, y = 370f, width = 120f),
        Platform(x = 135f, y = 280f, width = 130f),
        Platform(x = 35f, y = 190f, width = 120f),
        Platform(x = 245f, y = 190f, width = 120f),
        Platform(x = 135f, y = 100f, width = 130f),
        Platform(x = 10f, y = 460f, width = 80f),
        Platform(x = 310f, y = 460f, width = 80f),
        Platform(x = 10f, y = 280f, width = 80f),
        Platform(x = 310f, y = 280f, width = 80f),
        Platform(x = 10f, y = 100f, width = 80f),
        Platform(x = 310f, y = 100f, width = 80f),
        Platform(x = 150f, y = 500f, width = 100f),
        Platform(x = 160f, y = 40f, width = 80f)
      )
    },

    // 7: Triple Column Cascade
    { _ ->
      listOf(
        baseFloor,
        Platform(x = 25f, y = 440f, width = 90f),
        Platform(x = 155f, y = 440f, width = 90f),
        Platform(x = 285f, y = 440f, width = 90f),
        Platform(x = 85f, y = 330f, width = 100f),
        Platform(x = 215f, y = 330f, width = 100f),
        Platform(x = 25f, y = 220f, width = 90f),
        Platform(x = 155f, y = 220f, width = 90f),
        Platform(x = 285f, y = 220f, width = 90f),
        Platform(x = 100f, y = 110f, width = 200f),
        Platform(x = 15f, y = 110f, width = 60f),
        Platform(x = 325f, y = 110f, width = 60f),
        Platform(x = 90f, y = 490f, width = 80f),
        Platform(x = 230f, y = 490f, width = 80f),
        Platform(x = 150f, y = 160f, width = 100f),
        Platform(x = 140f, y = 50f, width = 120f)
      )
    },

    // 8: Hourglass Funnel
    { _ ->
      listOf(
        baseFloor,
        Platform(x = 20f, y = 460f, width = 150f),
        Platform(x = 230f, y = 460f, width = 150f),
        Platform(x = 110f, y = 370f, width = 180f),
        Platform(x = 145f, y = 280f, width = 110f),
        Platform(x = 20f, y = 190f, width = 150f),
        Platform(x = 230f, y = 190f, width = 150f),
        Platform(x = 90f, y = 100f, width = 220f),
        Platform(x = 10f, y = 370f, width = 70f),
        Platform(x = 320f, y = 370f, width = 70f),
        Platform(x = 10f, y = 280f, width = 80f),
        Platform(x = 310f, y = 280f, width = 80f),
        Platform(x = 10f, y = 100f, width = 60f),
        Platform(x = 330f, y = 100f, width = 60f),
        Platform(x = 150f, y = 490f, width = 100f),
        Platform(x = 160f, y = 50f, width = 80f)
      )
    },

    // 9: Labyrinth Fortress Keep
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
        Platform(x = 110f, y = 110f, width = 180f),
        Platform(x = 20f, y = 110f, width = 60f),
        Platform(x = 320f, y = 110f, width = 60f),
        Platform(x = 140f, y = 430f, width = 120f),
        Platform(x = 20f, y = 210f, width = 50f),
        Platform(x = 150f, y = 50f, width = 100f)
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

    // Determine archetype based on level progression:
    // Boss stages always use the Titan Throne Boss Arena (Screenshot 4)
    // Stages 1, 11, 21... use Pyramid Sanctum (Screenshot 1)
    // Stages 2, 12, 22... use Vertical Comb Chutes & Wells (Screenshot 2)
    // Stages 3, 13, 23... use Octagonal Ring Fortress & Altar (Screenshot 3)
    val archIndex = if (isBoss) 3 else ((lvl - 1) % 10)
    val fullArchetypePlatforms = archetypes[archIndex](lvl)

    // Platform layout scaled with exact obstacle count (5 at level 1 up to 15 at level 300)
    val targetObstacleCount = (5f + (lvl - 1) * 10f / 299f).roundToInt().coerceIn(5, 15)
    val platforms = fullArchetypePlatforms.take(targetObstacleCount)

    // Obstacle Hazards (Wall gargoyles that attack, matching user's screenshots)
    val obstacleHazards = mutableListOf<ObstacleHazard>()
    when (archIndex) {
      0 -> {
        // Pyramid Sanctum: Gargoyles on side wings
        obstacleHazards.add(
          ObstacleHazard(
            id = 1,
            x = 16f,
            y = 370f - 26f,
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
            y = 370f - 26f,
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
              y = 240f - 26f,
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
              y = 240f - 26f,
              colorType = ObstacleColor.YELLOW,
              facesRight = false,
              attackTimer = 3.0f,
              attackInterval = 4.2f
            )
          )
        }
      }
      1 -> {
        // Comb Wells: Gargoyles on lower pits & outer walls
        obstacleHazards.add(
          ObstacleHazard(
            id = 1,
            x = 16f,
            y = 350f - 26f,
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
            y = 350f - 26f,
            colorType = ObstacleColor.BLUE,
            facesRight = false,
            attackTimer = 2.5f,
            attackInterval = 4.2f
          )
        )
        if (lvl >= 15) {
          obstacleHazards.add(
            ObstacleHazard(
              id = 3,
              x = 160f,
              y = 460f - 26f,
              colorType = ObstacleColor.YELLOW,
              facesRight = (lvl % 2 == 0),
              attackTimer = 1.8f,
              attackInterval = 3.8f
            )
          )
        }
      }
      2 -> {
        // Ring Fortress: Gargoyles on outer ramparts
        obstacleHazards.add(
          ObstacleHazard(
            id = 1,
            x = 16f,
            y = 340f - 26f,
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
            y = 340f - 26f,
            colorType = ObstacleColor.BLUE,
            facesRight = false,
            attackTimer = 2.4f,
            attackInterval = 4.0f
          )
        )
        if (lvl >= 15) {
          obstacleHazards.add(
            ObstacleHazard(
              id = 3,
              x = 16f,
              y = 210f - 26f,
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
            y = 295f - 26f,
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
            y = 295f - 26f,
            colorType = ObstacleColor.BLUE,
            facesRight = false,
            attackTimer = 2.6f,
            attackInterval = 4.2f
          )
        )
        if (lvl >= 15) {
          obstacleHazards.add(
            ObstacleHazard(
              id = 3,
              x = 16f,
              y = 195f - 26f,
              colorType = ObstacleColor.RED,
              facesRight = true,
              attackTimer = 1.6f,
              attackInterval = 3.9f
            )
          )
        }
        if (lvl >= 50) {
          obstacleHazards.add(
            ObstacleHazard(
              id = 4,
              x = 358f,
              y = 195f - 26f,
              colorType = ObstacleColor.YELLOW,
              facesRight = false,
              attackTimer = 3.0f,
              attackInterval = 4.4f
            )
          )
        }
      }
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

      // Available spawn tiers based on platforms and archetype
      val spawnNodes = when (archIndex) {
        0 -> listOf(
          Pair(95f, 410f),
          Pair(275f, 410f),
          Pair(135f, 340f),
          Pair(235f, 340f),
          Pair(180f, 200f),
          Pair(35f, 330f),
          Pair(345f, 330f),
          Pair(120f, 100f)
        )
        1 -> listOf(
          Pair(85f, 300f),
          Pair(190f, 300f),
          Pair(295f, 300f),
          Pair(50f, 410f),
          Pair(320f, 410f),
          Pair(50f, 100f),
          Pair(150f, 100f),
          Pair(250f, 100f)
        )
        2 -> listOf(
          Pair(180f, 275f),
          Pair(35f, 430f),
          Pair(345f, 430f),
          Pair(35f, 300f),
          Pair(345f, 300f),
          Pair(180f, 160f),
          Pair(100f, 385f),
          Pair(270f, 385f)
        )
        else -> listOf(
          Pair(60f, 400f),
          Pair(300f, 400f),
          Pair(170f, 320f),
          Pair(60f, 250f),
          Pair(310f, 250f),
          Pair(180f, 150f),
          Pair(70f, 80f),
          Pair(290f, 80f)
        )
      }

      for (i in 0 until totalEnemies) {
        val node = spawnNodes[i % spawnNodes.size]
        // Vary coordinates slightly with level seed
        val offsetX = ((lvl * 11 + i * 17) % 24) - 12f
        val spawnX = (node.first + offsetX).coerceIn(30f, 350f)
        val spawnY = node.second

        // Enemy type distribution based on progression & archetype
        val enemyType = when {
          archIndex == 2 && i % 2 == 1 -> EnemyType.PHANTOM // Spectral ghosts in Fortress (Screenshot 3)
          lvl < 5 -> if (archIndex == 0 && i % 2 == 1) EnemyType.SKIMMER else EnemyType.WISP
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

  // Pre-cached small list for backward compatibility where a list is expected
  val levels: List<LevelConfig> by lazy {
    (1..TOTAL_LEVELS).map { getLevel(it) }
  }
}
