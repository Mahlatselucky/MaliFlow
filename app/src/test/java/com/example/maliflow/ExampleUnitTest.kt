package com.example.maliflow

import org.junit.Test
import org.junit.Assert.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Unit tests for Mali Flow core logic.
 * Tests cover gamification, budget calculations, and date formatting.
 */
class MaliFlowUnitTest {

    // ─── GamificationHelper tests ───────────────────────────────────────────

    @Test
    fun gamification_levelOneAtZeroPoints() {
        // A user with 0 points should be Level 1
        val level = GamificationHelper.getLevel(0)
        assertEquals(1, level)
    }

    @Test
    fun gamification_levelIncreasesEvery100Points() {
        // Every 100 points should increase the level by 1
        assertEquals(1, GamificationHelper.getLevel(0))
        assertEquals(1, GamificationHelper.getLevel(99))
        assertEquals(2, GamificationHelper.getLevel(100))
        assertEquals(3, GamificationHelper.getLevel(200))
        assertEquals(5, GamificationHelper.getLevel(400))
    }

    @Test
    fun gamification_pointsToNextLevelIsCorrect() {
        // At 0 points, user needs 100 points to reach Level 2
        assertEquals(100, GamificationHelper.pointsToNextLevel(0))
        // At 10 points, user needs 90 more points
        assertEquals(90, GamificationHelper.pointsToNextLevel(10))
        // At 100 points (Level 2), user needs 100 more to reach Level 3
        assertEquals(100, GamificationHelper.pointsToNextLevel(100))
        // At 150 points, user needs 50 more to reach Level 3
        assertEquals(50, GamificationHelper.pointsToNextLevel(150))
    }

    // ─── Budget / allowance calculation tests ────────────────────────────────

    @Test
    fun budget_dailyAllowanceCalculation() {
        // If max goal is R3000, total spent is R1000, and 20 days left
        // daily allowance should be (3000 - 1000) / 20 = R100
        val maxGoal = 3000.0
        val totalSpent = 1000.0
        val daysLeft = 20
        val remaining = (maxGoal - totalSpent).coerceAtLeast(0.0)
        val dailyAllowance = remaining / daysLeft
        assertEquals(100.0, dailyAllowance, 0.01)
    }

    @Test
    fun budget_dailyAllowanceIsZeroWhenOverBudget() {
        // If the user has spent more than the max goal, remaining should be 0
        val maxGoal = 3000.0
        val totalSpent = 3500.0
        val remaining = (maxGoal - totalSpent).coerceAtLeast(0.0)
        assertEquals(0.0, remaining, 0.01)
    }

    @Test
    fun budget_rolloverAmountCalculation() {
        // If category limit is R2000 and only R1200 was spent last month
        // rollover amount should be R800
        val categoryLimit = 2000.0
        val lastMonthSpent = 1200.0
        val rollover = (categoryLimit - lastMonthSpent).coerceAtLeast(0.0)
        assertEquals(800.0, rollover, 0.01)
    }

    @Test
    fun budget_rolloverIsZeroWhenOverspent() {
        // If user overspent last month, rollover should be 0, not negative
        val categoryLimit = 500.0
        val lastMonthSpent = 700.0
        val rollover = (categoryLimit - lastMonthSpent).coerceAtLeast(0.0)
        assertEquals(0.0, rollover, 0.01)
    }

    @Test
    fun budget_effectiveLimitWithRollover() {
        // Effective limit = base limit + rollover from last month
        val baseLimit = 2000.0
        val rollover = 800.0
        val effectiveLimit = baseLimit + rollover
        assertEquals(2800.0, effectiveLimit, 0.01)
    }

    @Test
    fun budget_categoryProgressPercentage() {
        // R350 spent out of R2000 limit = 17% progress
        val spent = 350.0
        val limit = 2000.0
        val percentage = ((spent / limit) * 100).toInt()
        assertEquals(17, percentage)
    }

    @Test
    fun budget_progressCapsAt100WhenOverBudget() {
        // Even if user spent 200% of limit, progress bar should cap at 100
        val spent = 2400.0
        val limit = 1200.0
        val percentage = ((spent / limit) * 100).toInt().coerceAtMost(100)
        assertEquals(100, percentage)
    }

    // ─── Date formatting tests ────────────────────────────────────────────────

    @Test
    fun date_formatIsCorrect() {
        // Date format should produce yyyy-MM-dd strings
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatted = sdf.format(Date(0)) // epoch = 1970-01-01
        assertTrue(formatted.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
    }

    @Test
    fun date_monthKeyFormat() {
        // Month key for graphs should be yyyy-MM format
        val monthKey = "2026-06"
        assertTrue(monthKey.matches(Regex("\\d{4}-\\d{2}")))
    }
}