package com.mudasir.flowcash.data.model

import androidx.compose.runtime.Immutable

enum class TransactionType {
    INCOME, EXPENSE
}

enum class CategoryType {
    // Income Categories
    SALARY, INVESTMENT, FREELANCE, BUSINESS, GIFTS, REFUNDS,

    // Expense Categories
    SHOPPING, FOOD, BILLS, TRANSPORT, ENTERTAINMENT, HEALTH, EDUCATION, TRAVEL, RENT, TAX,

    // Common / Other
    OTHER;

    fun isApplicableTo(transactionType: TransactionType): Boolean {
        return when (transactionType) {
            TransactionType.INCOME -> this in listOf(SALARY, INVESTMENT, FREELANCE, BUSINESS, GIFTS, REFUNDS, OTHER)
            TransactionType.EXPENSE -> this in listOf(SHOPPING, FOOD, BILLS, TRANSPORT, ENTERTAINMENT, HEALTH, EDUCATION, TRAVEL, RENT, TAX, OTHER)
        }
    }
}

@Immutable
data class TransactionCategory(
    val id: String,
    val name: String,
    val type: CategoryType,
    val iconName: String,
    val colorHex: String
)

@Immutable
data class TransactionItem(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val amount: Double,
    val type: TransactionType,
    val category: CategoryType,
    val dateFormatted: String,
    val timestamp: Long,
    val accountName: String = "Main Wallet",
    val note: String? = null,
    val createdAt: Long = timestamp,
    val updatedAt: Long = timestamp
)

@Immutable
data class CashFlowSummary(
    val totalBalance: Double,
    val monthlyIncome: Double,
    val monthlyExpense: Double,
    val netCashFlow: Double,
    val savingsRatePercentage: Int
)

@Immutable
data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val profilePicUrl: String? = null,
    val currencySymbol: String = "$"
)

@Immutable
data class CategorySpending(
    val categoryName: String,
    val categoryType: CategoryType,
    val amount: Double,
    val percentage: Float
)


