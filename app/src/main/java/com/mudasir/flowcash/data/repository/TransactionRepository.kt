package com.mudasir.flowcash.data.repository

import com.mudasir.flowcash.data.local.dao.AccountDao
import com.mudasir.flowcash.data.local.dao.BudgetDao
import com.mudasir.flowcash.data.local.dao.TransactionDao
import com.mudasir.flowcash.data.local.entity.AccountEntity
import com.mudasir.flowcash.data.local.entity.BudgetEntity
import com.mudasir.flowcash.data.local.entity.TransactionEntity
import com.mudasir.flowcash.data.model.CategoryType
import com.mudasir.flowcash.data.model.TransactionItem
import com.mudasir.flowcash.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao,
    private val accountDao: AccountDao
) {

    val allTransactions: Flow<List<TransactionItem>> = transactionDao.getAllTransactionsFlow().map { entities ->
        entities.map { it.toDomainModel() }
    }

    val unsyncedCount: Flow<Int> = transactionDao.getUnsyncedCountFlow()

    val allBudgets: Flow<List<BudgetEntity>> = budgetDao.getAllBudgets()

    val allAccounts: Flow<List<AccountEntity>> = accountDao.getAllAccounts()

    suspend fun addTransaction(
        title: String,
        subtitle: String = "",
        amount: Double,
        type: TransactionType,
        category: CategoryType,
        accountName: String = "Main Wallet",
        note: String? = null
    ) {
        val entity = TransactionEntity(
            id = "tx_${System.currentTimeMillis()}",
            title = title,
            subtitle = subtitle.ifBlank { "Manual Entry" },
            amount = amount,
            type = type.name,
            category = category.name,
            accountName = accountName,
            note = note,
            dateFormatted = "Just now",
            timestamp = System.currentTimeMillis(),
            isSynced = false,
            isDeleted = false
        )
        transactionDao.insertTransaction(entity)
    }

    suspend fun deleteTransactionsByAccountName(accountName: String) {
        transactionDao.deleteByAccountName(accountName)
    }

    suspend fun deleteTransaction(id: String) {
        transactionDao.softDeleteById(id)
    }

    suspend fun setBudget(categoryName: String, limitAmount: Double) {
        budgetDao.insertBudget(BudgetEntity(categoryName, limitAmount))
    }

    suspend fun deleteBudget(categoryName: String) {
        budgetDao.deleteBudget(categoryName)
    }

    suspend fun addAccount(account: AccountEntity) {
        accountDao.insertAccount(account)
    }

    suspend fun deleteAccount(id: String) {
        accountDao.deleteAccountById(id)
    }

    suspend fun clearDatabase() {
        transactionDao.clearAll()
        budgetDao.clearAllBudgets()
        accountDao.clearAllAccounts()
    }
}
