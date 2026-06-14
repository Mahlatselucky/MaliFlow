package com.example.maliflow.data.dao

import androidx.room.*
import com.example.maliflow.data.model.FlowPoints

@Dao
interface FlowPointsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(flowPoints: FlowPoints)

    @Update
    suspend fun update(flowPoints: FlowPoints)

    @Query("SELECT * FROM flow_points WHERE userId = :userId LIMIT 1")
    suspend fun getFlowPointsByUserId(userId: Int): FlowPoints?
}
