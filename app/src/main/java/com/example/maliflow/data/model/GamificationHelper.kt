package com.example.maliflow.data.model

import com.example.maliflow.data.database.AppDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Handles all Flow Points logic - awarding points and tracking streaks
object GamificationHelper {

    private const val POINTS_PER_EXPENSE = 10

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Call this every time the user successfully logs a new expense
    suspend fun onExpenseLogged(db: AppDatabase, userId: Int) {
        val today = dateFormat.format(Date())
        val existing = db.flowPointsDao().getFlowPointsByUserId(userId)

        if (existing == null) {
            // First expense ever logged by this user
            db.flowPointsDao().insert(
                FlowPoints(
                    userId = userId,
                    points = POINTS_PER_EXPENSE,
                    streak = 1,
                    lastLogDate = today,
                    totalExpensesLogged = 1,
                    goalsMetCount = 0
                )
            )
            return
        }

        // Work out the new streak based on when the user last logged something
        val newStreak = when {
            existing.lastLogDate == today -> existing.streak // already logged today
            existing.lastLogDate == yesterday(today) -> existing.streak + 1 // continuing the streak
            else -> 1 // streak was broken, restart at 1
        }

        db.flowPointsDao().update(
            existing.copy(
                points = existing.points + POINTS_PER_EXPENSE,
                streak = newStreak,
                lastLogDate = today,
                totalExpensesLogged = existing.totalExpensesLogged + 1
            )
        )
    }

    // Returns yesterday's date string given today's date string
    private fun yesterday(todayStr: String): String {
        val cal = Calendar.getInstance()
        try {
            cal.time = dateFormat.parse(todayStr) ?: Date()
        } catch (e: Exception) {
            cal.time = Date()
        }
        cal.add(Calendar.DAY_OF_MONTH, -1)
        return dateFormat.format(cal.time)
    }

    // Simple levelling system: every 100 points = 1 level
    fun getLevel(points: Int): Int = (points / 100) + 1

    // How many points needed to reach the next level
    fun pointsToNextLevel(points: Int): Int {
        val currentLevelFloor = (points / 100) * 100
        return (currentLevelFloor + 100) - points
    }
}
