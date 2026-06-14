package com.example.maliflow.data.dao

import androidx.room.*
import androidx.lifecycle.LiveData
import com.example.maliflow.data.model.Category

@Dao
interface CategoryDao {
    @Insert
    suspend fun insert(category: Category)

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query("SELECT * FROM categories WHERE userId = :userId")
    fun getCategoriesByUser(userId: Int): LiveData<List<Category>>

    @Query("SELECT * FROM categories WHERE userId = :userId")
    suspend fun getCategoriesByUserSync(userId: Int): List<Category>
}