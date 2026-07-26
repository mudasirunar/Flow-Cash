package com.mudasir.flowcash.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.mudasir.flowcash.data.local.dao.AccountDao
import com.mudasir.flowcash.data.local.dao.BudgetDao
import com.mudasir.flowcash.data.local.dao.TransactionDao
import com.mudasir.flowcash.data.local.entity.AccountEntity
import com.mudasir.flowcash.data.local.entity.BudgetEntity
import com.mudasir.flowcash.data.local.entity.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class SyncState {
    IDLE, SYNCING, SUCCESS, ERROR
}

class RemoteSyncManager(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val budgetDao: BudgetDao
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val syncScope = CoroutineScope(Dispatchers.IO)

    private val _syncState = MutableStateFlow(SyncState.IDLE)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private var accountsListener: ListenerRegistration? = null
    private var transactionsListener: ListenerRegistration? = null
    private var budgetsListener: ListenerRegistration? = null

    private var activeUserId: String? = null

    fun startRealtimeSync(userId: String) {
        if (userId.isBlank()) return
        if (activeUserId == userId && accountsListener != null) return

        stopSync()
        activeUserId = userId
        _syncState.value = SyncState.SYNCING

        val userDocRef = firestore.collection("users").document(userId)

        // 1. Live Sync Accounts
        accountsListener = userDocRef.collection("accounts")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _syncState.value = SyncState.ERROR
                    return@addSnapshotListener
                }

                snapshot?.let { querySnap ->
                    syncScope.launch {
                        val remoteAccounts = mutableListOf<AccountEntity>()
                        for (doc in querySnap.documents) {
                            val id = doc.getString("id") ?: doc.id
                            val name = doc.getString("name") ?: continue
                            val holderName = doc.getString("holderName") ?: ""
                            val accountType = doc.getString("accountType") ?: "CARD"
                            val network = doc.getString("network") ?: "VISA"
                            val cardNumber = doc.getString("cardNumber") ?: ""
                            val expiryDate = doc.getString("expiryDate") ?: ""
                            val cardColorStart = doc.getString("cardColorStart") ?: "#1E1B4B"
                            val cardColorEnd = doc.getString("cardColorEnd") ?: "#4F46E5"
                            val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                            val updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                            val isDeleted = doc.getBoolean("isDeleted") ?: false

                            val entity = AccountEntity(
                                id = id,
                                name = name,
                                holderName = holderName,
                                accountType = accountType,
                                network = network,
                                cardNumber = cardNumber,
                                expiryDate = expiryDate,
                                cardColorStart = cardColorStart,
                                cardColorEnd = cardColorEnd,
                                createdAt = createdAt,
                                updatedAt = updatedAt,
                                lastUpdatedServerTimestamp = updatedAt,
                                isSynced = true,
                                isDeleted = isDeleted
                            )
                            remoteAccounts.add(entity)
                        }

                        for (account in remoteAccounts) {
                            if (account.isDeleted) {
                                accountDao.deleteAccountById(account.id)
                            } else {
                                accountDao.insertAccount(account)
                            }
                        }
                        _syncState.value = SyncState.SUCCESS
                    }
                }
            }

        // 2. Live Sync Transactions
        transactionsListener = userDocRef.collection("transactions")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _syncState.value = SyncState.ERROR
                    return@addSnapshotListener
                }

                snapshot?.let { querySnap ->
                    syncScope.launch {
                        val remoteTransactions = mutableListOf<TransactionEntity>()
                        for (doc in querySnap.documents) {
                            val id = doc.getString("id") ?: doc.id
                            val title = doc.getString("title") ?: continue
                            val subtitle = doc.getString("subtitle") ?: ""
                            val amount = doc.getDouble("amount") ?: 0.0
                            val type = doc.getString("type") ?: "EXPENSE"
                            val category = doc.getString("category") ?: "OTHER"
                            val accountName = doc.getString("accountName") ?: "Main Wallet"
                            val note = doc.getString("note")
                            val dateFormatted = doc.getString("dateFormatted") ?: "Just now"
                            val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                            val createdAt = doc.getLong("createdAt") ?: timestamp
                            val updatedAt = doc.getLong("updatedAt") ?: timestamp
                            val isDeleted = doc.getBoolean("isDeleted") ?: false

                            val entity = TransactionEntity(
                                id = id,
                                title = title,
                                subtitle = subtitle,
                                amount = amount,
                                type = type,
                                category = category,
                                accountName = accountName,
                                note = note,
                                dateFormatted = dateFormatted,
                                timestamp = timestamp,
                                createdAt = createdAt,
                                updatedAt = updatedAt,
                                lastUpdatedServerTimestamp = updatedAt,
                                isSynced = true,
                                isDeleted = isDeleted
                            )
                            remoteTransactions.add(entity)
                        }

                        for (tx in remoteTransactions) {
                            if (tx.isDeleted) {
                                transactionDao.deletePermanently(tx.id)
                            } else {
                                transactionDao.insertTransaction(tx)
                            }
                        }
                        _syncState.value = SyncState.SUCCESS
                    }
                }
            }

        // 3. Live Sync Budgets
        budgetsListener = userDocRef.collection("budgets")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _syncState.value = SyncState.ERROR
                    return@addSnapshotListener
                }

                snapshot?.let { querySnap ->
                    syncScope.launch {
                        val remoteBudgets = mutableListOf<BudgetEntity>()
                        for (doc in querySnap.documents) {
                            val categoryName = doc.getString("categoryName") ?: doc.id
                            val limitAmount = doc.getDouble("limitAmount") ?: 0.0
                            val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                            val updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                            val isDeleted = doc.getBoolean("isDeleted") ?: false

                            val entity = BudgetEntity(
                                categoryName = categoryName,
                                limitAmount = limitAmount,
                                createdAt = createdAt,
                                updatedAt = updatedAt,
                                lastUpdatedServerTimestamp = updatedAt,
                                isSynced = true,
                                isDeleted = isDeleted
                            )
                            remoteBudgets.add(entity)
                        }

                        for (budget in remoteBudgets) {
                            if (budget.isDeleted) {
                                budgetDao.deleteBudget(budget.categoryName)
                            } else {
                                budgetDao.insertBudget(budget)
                            }
                        }
                        _syncState.value = SyncState.SUCCESS
                    }
                }
            }

        // Trigger initial upload of any local unsynced data
        syncScope.launch {
            uploadUnsyncedData(userId)
        }
    }

    suspend fun uploadUnsyncedData(userId: String) {
        if (userId.isBlank()) return
        val userDocRef = firestore.collection("users").document(userId)

        try {
            // Upload Accounts
            val unsyncedAccounts = accountDao.getUnsyncedAccounts()
            for (acc in unsyncedAccounts) {
                val accMap = mapOf(
                    "id" to acc.id,
                    "name" to acc.name,
                    "holderName" to acc.holderName,
                    "accountType" to acc.accountType,
                    "network" to acc.network,
                    "cardNumber" to acc.cardNumber,
                    "expiryDate" to acc.expiryDate,
                    "cardColorStart" to acc.cardColorStart,
                    "cardColorEnd" to acc.cardColorEnd,
                    "createdAt" to acc.createdAt,
                    "updatedAt" to acc.updatedAt,
                    "isDeleted" to acc.isDeleted
                )
                userDocRef.collection("accounts").document(acc.id)
                    .set(accMap, SetOptions.merge()).await()
                accountDao.insertAccount(acc.copy(isSynced = true))
            }

            // Upload Transactions
            val unsyncedTransactions = transactionDao.getUnsyncedTransactions()
            for (tx in unsyncedTransactions) {
                val txMap = mapOf(
                    "id" to tx.id,
                    "title" to tx.title,
                    "subtitle" to tx.subtitle,
                    "amount" to tx.amount,
                    "type" to tx.type,
                    "category" to tx.category,
                    "accountName" to tx.accountName,
                    "note" to (tx.note ?: ""),
                    "dateFormatted" to tx.dateFormatted,
                    "timestamp" to tx.timestamp,
                    "createdAt" to tx.createdAt,
                    "updatedAt" to tx.updatedAt,
                    "isDeleted" to tx.isDeleted
                )
                userDocRef.collection("transactions").document(tx.id)
                    .set(txMap, SetOptions.merge()).await()
                transactionDao.insertTransaction(tx.copy(isSynced = true))
            }

            // Upload Budgets
            val unsyncedBudgets = budgetDao.getUnsyncedBudgets()
            for (b in unsyncedBudgets) {
                val bMap = mapOf(
                    "categoryName" to b.categoryName,
                    "limitAmount" to b.limitAmount,
                    "createdAt" to b.createdAt,
                    "updatedAt" to b.updatedAt,
                    "isDeleted" to b.isDeleted
                )
                userDocRef.collection("budgets").document(b.categoryName)
                    .set(bMap, SetOptions.merge()).await()
                budgetDao.insertBudget(b.copy(isSynced = true))
            }
        } catch (e: Exception) {
            _syncState.value = SyncState.ERROR
        }
    }

    fun stopSync() {
        accountsListener?.remove()
        transactionsListener?.remove()
        budgetsListener?.remove()
        accountsListener = null
        transactionsListener = null
        budgetsListener = null
        activeUserId = null
        _syncState.value = SyncState.IDLE
    }
}
