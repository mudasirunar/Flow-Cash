package com.mudasir.flowcash.data.repository

import com.mudasir.flowcash.data.local.dao.TransactionDao
import com.mudasir.flowcash.data.local.entity.TransactionEntity
import com.mudasir.flowcash.data.model.CategoryType
import com.mudasir.flowcash.data.model.TransactionItem
import com.mudasir.flowcash.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepository(private val transactionDao: TransactionDao) {

    val allTransactions: Flow<List<TransactionItem>> = transactionDao.getAllTransactionsFlow().map { entities ->
        entities.map { it.toDomainModel() }
    }

    val unsyncedCount: Flow<Int> = transactionDao.getUnsyncedCountFlow()

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

    suspend fun deleteTransaction(id: String) {
        transactionDao.softDeleteById(id)
    }

    suspend fun clearDatabase() {
        transactionDao.clearAll()
    }
}
