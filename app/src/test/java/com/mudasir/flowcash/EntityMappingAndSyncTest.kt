package com.mudasir.flowcash

import com.mudasir.flowcash.data.local.entity.AccountEntity
import com.mudasir.flowcash.data.local.entity.BudgetEntity
import com.mudasir.flowcash.data.local.entity.TransactionEntity
import com.mudasir.flowcash.data.model.CategoryType
import com.mudasir.flowcash.data.model.TransactionItem
import com.mudasir.flowcash.data.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test

class EntityMappingAndSyncTest {

    @Test
    fun testTransactionEntity_toDomainModel_preservesAllFields() {
        val now = System.currentTimeMillis()
        val entity = TransactionEntity(
            id = "tx_101",
            title = "Monthly Salary",
            subtitle = "Tech Corp",
            amount = 5000.0,
            type = "INCOME",
            category = "SALARY",
            accountName = "Primary Card",
            note = "Direct Deposit",
            dateFormatted = "Today",
            timestamp = now,
            isSynced = true,
            isDeleted = false,
            createdAt = now,
            updatedAt = now
        )

        val domain = entity.toDomainModel()
        assertEquals("tx_101", domain.id)
        assertEquals("Monthly Salary", domain.title)
        assertEquals("Tech Corp", domain.subtitle)
        assertEquals(5000.0, domain.amount, 0.001)
        assertEquals(TransactionType.INCOME, domain.type)
        assertEquals(CategoryType.SALARY, domain.category)
        assertEquals("Primary Card", domain.accountName)
        assertEquals("Direct Deposit", domain.note)
    }

    @Test
    fun testTransactionEntity_fromDomainModel_setsUnsyncedFlag() {
        val now = System.currentTimeMillis()
        val item = TransactionItem(
            id = "tx_102",
            title = "Coffee",
            subtitle = "Cafe",
            amount = 4.50,
            type = TransactionType.EXPENSE,
            category = CategoryType.FOOD,
            dateFormatted = "Just now",
            timestamp = now,
            accountName = "Cash Wallet",
            note = "Espresso"
        )

        val entity = TransactionEntity.fromDomainModel(item, isSynced = false)
        assertEquals("tx_102", entity.id)
        assertFalse("New/updated local transaction must be marked unsynced", entity.isSynced)
        assertFalse("New transaction must not be marked deleted", entity.isDeleted)
    }

    @Test
    fun testAccountEntity_cardDefaults_preservedCorrectly() {
        val account = AccountEntity(
            id = "acc_01",
            name = "Main Savings",
            holderName = "Mudasir",
            accountType = "CARD",
            network = "VISA",
            cardColorStart = "#1E1B4B",
            cardColorEnd = "#4F46E5"
        )

        assertEquals("acc_01", account.id)
        assertEquals("#1E1B4B", account.cardColorStart)
        assertEquals("#4F46E5", account.cardColorEnd)
        assertFalse(account.isDeleted)
    }

    @Test
    fun testBudgetEntity_limitAmount_preservedCorrectly() {
        val budget = BudgetEntity(
            categoryName = "Food",
            limitAmount = 600.0
        )

        assertEquals("Food", budget.categoryName)
        assertEquals(600.0, budget.limitAmount, 0.001)
        assertFalse(budget.isDeleted)
    }
}
