package com.mudasir.flowcash.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey
    val categoryName: String,
    val limitAmount: Double
)
