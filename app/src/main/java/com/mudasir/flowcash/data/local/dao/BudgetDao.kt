package com.mudasir.flowcash.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mudasir.flowcash.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Query("SELECT * FROM budgets WHERE isDeleted = 0")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE isDeleted = 0")
    suspend fun getAllBudgetsList(): List<BudgetEntity>

    @Query("SELECT * FROM budgets WHERE isSynced = 0")
    suspend fun getUnsyncedBudgets(): List<BudgetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(budgets: List<BudgetEntity>)

    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    @Query("UPDATE budgets SET isDeleted = 1, isSynced = 0, updatedAt = :timestamp WHERE categoryName = :categoryName")
    suspend fun softDeleteBudget(categoryName: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM budgets WHERE categoryName = :categoryName")
    suspend fun deleteBudget(categoryName: String)

    @Query("DELETE FROM budgets")
    suspend fun clearAllBudgets()
}
