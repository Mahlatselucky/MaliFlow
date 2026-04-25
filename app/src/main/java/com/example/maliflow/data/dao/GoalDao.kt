package com.example.maliflow.data.dao

import androidx.room.*
import androidx.lifecycle.LiveData
import com.example.maliflow.data.model.Goal

@Dao
interface GoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: Goal)

    @Query("SELECT * FROM goals WHERE userId = :userId AND month = :month LIMIT 1")
    fun getGoalByMonth(userId: Int, month: String): LiveData<Goal?>
}