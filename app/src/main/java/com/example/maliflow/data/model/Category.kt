package com.example.maliflow.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Category entity - updated for Part 3 to include budget limit and rollover fields
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val userId: Int,
    val emoji: String = "💰",
    val budgetLimit: Double = 0.0,
    val rolloverEnabled: Boolean = false,
    val rolloverBalance: Double = 0.0
)