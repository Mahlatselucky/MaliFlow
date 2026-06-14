package com.example.maliflow.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Tracks gamification data per user - points, streak, and badge progress
@Entity(tableName = "flow_points")
data class FlowPoints(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val points: Int = 0,
    val streak: Int = 0,
    val lastLogDate: String = "",
    val totalExpensesLogged: Int = 0,
    val goalsMetCount: Int = 0
)