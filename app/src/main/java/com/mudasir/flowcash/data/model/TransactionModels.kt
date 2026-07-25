package com.mudasir.flowcash.data.model

import androidx.compose.runtime.Immutable

enum class TransactionType {
    INCOME, EXPENSE, TRANSFER
}

enum class CategoryType {
    SALARY, INVESTMENT, FREELANCE, SHOPPING, FOOD, BILLS, TRANSPORT, ENTERTAINMENT, HEALTH, OTHER
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
    val accountName: String = "Main Wallet"
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

object MockData {
    val sampleTransactions = listOf(
        TransactionItem(
            id = "tx_1",
            title = "Tech Corp Salary",
            subtitle = "Monthly direct deposit",
            amount = 4850.00,
            type = TransactionType.INCOME,
            category = CategoryType.SALARY,
            dateFormatted = "Today, 09:30 AM",
            timestamp = System.currentTimeMillis()
        ),
        TransactionItem(
            id = "tx_2",
            title = "Whole Foods Market",
            subtitle = "Weekly groceries & essentials",
            amount = 142.50,
            type = TransactionType.EXPENSE,
            category = CategoryType.FOOD,
            dateFormatted = "Yesterday, 04:15 PM",
            timestamp = System.currentTimeMillis() - 86400000
        ),
        TransactionItem(
            id = "tx_3",
            title = "Freelance UI Project",
            subtitle = "Client payment for App Mockup",
            amount = 1200.00,
            type = TransactionType.INCOME,
            category = CategoryType.FREELANCE,
            dateFormatted = "24 Jul, 02:00 PM",
            timestamp = System.currentTimeMillis() - (86400000 * 2)
        ),
        TransactionItem(
            id = "tx_4",
            title = "Electricity & Power Bill",
            subtitle = "Utility bill payment",
            amount = 89.90,
            type = TransactionType.EXPENSE,
            category = CategoryType.BILLS,
            dateFormatted = "22 Jul, 11:20 AM",
            timestamp = System.currentTimeMillis() - (86400000 * 4)
        ),
        TransactionItem(
            id = "tx_5",
            title = "Apple Store Purchase",
            subtitle = "MagSafe Charger & Accessories",
            amount = 129.00,
            type = TransactionType.EXPENSE,
            category = CategoryType.SHOPPING,
            dateFormatted = "20 Jul, 06:45 PM",
            timestamp = System.currentTimeMillis() - (86400000 * 6)
        ),
        TransactionItem(
            id = "tx_6",
            title = "Stock Investment Dividends",
            subtitle = "Quarterly payout",
            amount = 310.40,
            type = TransactionType.INCOME,
            category = CategoryType.INVESTMENT,
            dateFormatted = "18 Jul, 10:00 AM",
            timestamp = System.currentTimeMillis() - (86400000 * 8)
        )
    )

    val sampleSummary = CashFlowSummary(
        totalBalance = 12480.00,
        monthlyIncome = 6360.40,
        monthlyExpense = 361.40,
        netCashFlow = 5999.00,
        savingsRatePercentage = 94
    )

    val sampleCategorySpendings = listOf(
        CategorySpending("Groceries & Food", CategoryType.FOOD, 142.50, 39.4f),
        CategorySpending("Shopping & Tech", CategoryType.SHOPPING, 129.00, 35.7f),
        CategorySpending("Utilities & Bills", CategoryType.BILLS, 89.90, 24.9f)
    )
}
