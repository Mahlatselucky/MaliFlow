package com.example.maliflow.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense_entries")
data class ExpenseEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: String,
    val startTime: String,
    val endTime: String,
    val description: String,
    val amount: Double,
    val categoryId: Int,
    val userId: Int,
    val photoPath: String? = null
)