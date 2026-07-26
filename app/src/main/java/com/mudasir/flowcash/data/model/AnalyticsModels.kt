package com.mudasir.flowcash.data.model

import androidx.compose.runtime.Immutable

enum class PeriodType {
    WEEKLY, MONTHLY, YEARLY
}

@Immutable
data class BarChartEntry(
    val label: String,
    val income: Double,
    val expense: Double
)

@Immutable
data class FinancialHealthScore(
    val savingsRate: Int,
    val statusText: String,
    val guidanceMessage: String,
    val isHealthy: Boolean
)
