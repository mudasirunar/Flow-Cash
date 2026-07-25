package com.mudasir.flowcash.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val holderName: String = "",
    val accountType: String = "CARD", // CARD, CASH_WALLET, BANK_ACCOUNT, INVESTMENT, FREELANCE_INCOME, OTHER
    val network: String = "VISA", // VISA, MASTERCARD, AMEX, NONE
    val cardNumber: String = "",
    val expiryDate: String = "",
    val cardColorStart: String = "#1E1B4B", // Gradient start hex
    val cardColorEnd: String = "#4F46E5" // Gradient end hex
)
