package com.mudasir.flowcash.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mudasir.flowcash.data.model.CategoryType
import com.mudasir.flowcash.data.model.TransactionItem
import com.mudasir.flowcash.data.model.TransactionType

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val subtitle: String = "",
    val amount: Double,
    val type: String, // TransactionType.name
    val category: String, // CategoryType.name
    val accountName: String = "Main Wallet",
    val note: String? = null,
    val dateFormatted: String,
    val timestamp: Long,
    // Metadata fields prepared for upcoming Firebase Sync
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
    val lastUpdatedServerTimestamp: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): TransactionItem {
        val txType = try {
            TransactionType.valueOf(type)
        } catch (e: Exception) {
            TransactionType.EXPENSE
        }

        val catType = try {
            CategoryType.valueOf(category)
        } catch (e: Exception) {
            CategoryType.OTHER
        }

        return TransactionItem(
            id = id,
            title = title,
            subtitle = subtitle,
            amount = amount,
            type = txType,
            category = catType,
            dateFormatted = dateFormatted,
            timestamp = timestamp,
            accountName = accountName,
            note = note,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomainModel(
            item: TransactionItem,
            note: String? = item.note,
            isSynced: Boolean = false
        ): TransactionEntity {
            return TransactionEntity(
                id = item.id,
                title = item.title,
                subtitle = item.subtitle,
                amount = item.amount,
                type = item.type.name,
                category = item.category.name,
                accountName = item.accountName,
                note = note,
                dateFormatted = item.dateFormatted,
                timestamp = item.timestamp,
                isSynced = isSynced,
                isDeleted = false,
                lastUpdatedServerTimestamp = 0L,
                createdAt = item.createdAt,
                updatedAt = item.updatedAt
            )
        }
    }
}
