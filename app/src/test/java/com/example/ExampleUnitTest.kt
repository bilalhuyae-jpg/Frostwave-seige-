package com.example

import com.example.game.LevelCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testTotalLevelsCount() {
    assertEquals(1020, LevelCatalog.TOTAL_LEVELS)
  }

  @Test
  fun testStage2HasUpwardPath() {
    val stage2 = LevelCatalog.getLevel(2)
    assertNotNull(stage2)
    assertTrue("Stage 2 must have platforms", stage2.platforms.isNotEmpty())

    // Must have a platform at or near y=460 to jump up from floor (y=540)
    val hasTier1 = stage2.platforms.any { it.y in 440f..480f }
    assertTrue("Stage 2 must have Tier 1 platform to climb up from floor", hasTier1)

    // Must have a platform at or near y=380 to reach Tier 2
    val hasTier2 = stage2.platforms.any { it.y in 360f..400f }
    assertTrue("Stage 2 must have Tier 2 platform to climb higher", hasTier2)

    // Must have top platforms to reach the apex
    val hasTop = stage2.platforms.any { it.y <= 160f }
    assertTrue("Stage 2 must have top platforms", hasTop)
  }

  @Test
  fun testStage1020Configuration() {
    val stage1020 = LevelCatalog.getLevel(1020)
    assertNotNull(stage1020)
    assertEquals(1020, stage1020.levelNumber)
    assertTrue("Stage 1020 must be a boss stage", stage1020.isBossLevel)
    assertTrue("Stage 1020 must have enemies", stage1020.enemySpawns.isNotEmpty())
    assertTrue("Stage 1020 must have platforms", stage1020.platforms.size >= 5)
  }

  @Test
  fun testAll1020StagesGenerateSuccessfully() {
    for (i in 1..1020) {
      val level = LevelCatalog.getLevel(i)
      assertNotNull("Level $i should not be null", level)
      assertEquals("Level number must match", i, level.levelNumber)
      assertTrue("Level $i must have a floor and platforms", level.platforms.isNotEmpty())
      assertTrue("Level $i must have enemies", level.enemySpawns.isNotEmpty())
    }
  }
}
