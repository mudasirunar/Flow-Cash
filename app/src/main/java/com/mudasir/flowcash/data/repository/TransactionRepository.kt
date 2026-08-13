package com.mudasir.flowcash.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.mudasir.flowcash.data.local.dao.AccountDao
import com.mudasir.flowcash.data.local.dao.BudgetDao
import com.mudasir.flowcash.data.local.dao.TransactionDao
import com.mudasir.flowcash.data.local.entity.AccountEntity
import com.mudasir.flowcash.data.local.entity.BudgetEntity
import com.mudasir.flowcash.data.local.entity.TransactionEntity
import com.mudasir.flowcash.data.model.CategoryType
import com.mudasir.flowcash.data.model.TransactionItem
import com.mudasir.flowcash.data.model.TransactionType
import com.mudasir.flowcash.data.remote.RemoteSyncManager
import com.mudasir.flowcash.data.remote.SyncState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao,
    private val accountDao: AccountDao,
    val remoteSyncManager: RemoteSyncManager = RemoteSyncManager(transactionDao, accountDao, budgetDao)
) {

    val allTransactions: Flow<List<TransactionItem>> = transactionDao.getAllTransactionsFlow().map { entities ->
        entities.map { it.toDomainModel() }
    }

    val unsyncedCount: Flow<Int> = transactionDao.getUnsyncedCountFlow()

    val allBudgets: Flow<List<BudgetEntity>> = budgetDao.getAllBudgets()

    val allAccounts: Flow<List<AccountEntity>> = accountDao.getAllAccounts()

    val syncState: StateFlow<SyncState> = remoteSyncManager.syncState

    fun startSync(userId: String) {
        remoteSyncManager.startRealtimeSync(userId)
    }

    private suspend fun triggerRemoteUpload() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        remoteSyncManager.uploadUnsyncedData(uid)
    }

    suspend fun addTransaction(
        title: String,
        subtitle: String = "",
        amount: Double,
        type: TransactionType,
        category: CategoryType,
        accountName: String = "Main Wallet",
        note: String? = null
    ) {
        val now = System.currentTimeMillis()
        val entity = TransactionEntity(
            id = "tx_${now}",
            title = title,
            subtitle = subtitle.ifBlank { "Manual Entry" },
            amount = amount,
            type = type.name,
            category = category.name,
            accountName = accountName,
            note = note,
            dateFormatted = "Just now",
            timestamp = now,
            createdAt = now,
            updatedAt = now,
            isSynced = false,
            isDeleted = false
        )
        transactionDao.insertTransaction(entity)
        triggerRemoteUpload()
    }

    suspend fun updateTransaction(
        id: String,
        title: String,
        subtitle: String = "",
        amount: Double,
        type: TransactionType,
        category: CategoryType,
        accountName: String = "Main Wallet",
        note: String? = null,
        createdAt: Long,
        timestamp: Long
    ) {
        val now = System.currentTimeMillis()
        val entity = TransactionEntity(
            id = id,
            title = title,
            subtitle = subtitle.ifBlank { "Manual Entry" },
            amount = amount,
            type = type.name,
            category = category.name,
            accountName = accountName,
            note = note,
            dateFormatted = "Just now",
            timestamp = timestamp,
            createdAt = createdAt,
            updatedAt = now,
            isSynced = false,
            isDeleted = false
        )
        transactionDao.insertTransaction(entity)
        triggerRemoteUpload()
    }

    suspend fun deleteTransactionsByAccountName(accountName: String) {
        transactionDao.deleteByAccountName(accountName)
        triggerRemoteUpload()
    }

    suspend fun deleteTransaction(id: String) {
        transactionDao.softDeleteById(id)
        triggerRemoteUpload()
    }

    suspend fun setBudget(categoryName: String, limitAmount: Double) {
        val now = System.currentTimeMillis()
        budgetDao.insertBudget(BudgetEntity(categoryName = categoryName, limitAmount = limitAmount, updatedAt = now, isSynced = false))
        triggerRemoteUpload()
    }

    suspend fun deleteBudget(categoryName: String) {
        budgetDao.softDeleteBudget(categoryName)
        triggerRemoteUpload()
    }

    suspend fun addAccount(account: AccountEntity) {
        val now = System.currentTimeMillis()
        accountDao.insertAccount(account.copy(updatedAt = now, isSynced = false))
        triggerRemoteUpload()
    }

    suspend fun deleteAccount(id: String) {
        accountDao.softDeleteAccountById(id)
        triggerRemoteUpload()
    }

    suspend fun clearLocalDatabase() {
        remoteSyncManager.clearRemoteUserData()
        remoteSyncManager.stopSync()
        transactionDao.clearAll()
        budgetDao.clearAllBudgets()
        accountDao.clearAllAccounts()
    }
}
