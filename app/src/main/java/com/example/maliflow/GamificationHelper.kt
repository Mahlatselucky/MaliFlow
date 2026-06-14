package com.example.maliflow

import com.example.maliflow.data.database.AppDatabase
import com.example.maliflow.data.model.FlowPoints
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
        // Fixed: Updated method name to match FlowPointsDao
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

        val newStreak = when {
            existing.lastLogDate == today -> existing.streak
            existing.lastLogDate == yesterday(today) -> existing.streak + 1
            else -> 1
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

    fun getLevel(points: Int): Int = (points / 100) + 1

    fun pointsToNextLevel(points: Int): Int {
        val currentLevelFloor = (points / 100) * 100
        return (currentLevelFloor + 100) - points
    }
}