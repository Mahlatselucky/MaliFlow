package com.example.maliflow.data.dao

import androidx.room.*
import androidx.lifecycle.LiveData
import com.example.maliflow.data.model.ExpenseEntry

@Dao
interface ExpenseEntryDao {
    @Insert
    suspend fun insert(entry: ExpenseEntry)

    @Delete
    suspend fun delete(entry: ExpenseEntry)

    @Query("SELECT * FROM expense_entries WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    fun getEntriesByPeriod(userId: Int, startDate: String, endDate: String): LiveData<List<ExpenseEntry>>

    @Query("SELECT categoryId, SUM(amount) as total FROM expense_entries WHERE userId = :userId AND date BETWEEN :startDate AND :endDate GROUP BY categoryId")
    fun getCategoryTotals(userId: Int, startDate: String, endDate: String): LiveData<List<CategoryTotal>>

    @Query("SELECT categoryId, SUM(amount) as total FROM expense_entries WHERE userId = :userId AND date BETWEEN :startDate AND :endDate GROUP BY categoryId")
    suspend fun getCategoryTotalsSync(userId: Int, startDate: String, endDate: String): List<CategoryTotal>

    @Query("SELECT date, SUM(amount) as total FROM expense_entries WHERE userId = :userId AND date BETWEEN :startDate AND :endDate GROUP BY date ORDER BY date ASC")
    suspend fun getDailyTotals(userId: Int, startDate: String, endDate: String): List<DailyTotal>
}

data class CategoryTotal(
    val categoryId: Int,
    val total: Double
)

data class DailyTotal(
    val date: String,
    val total: Double
)
