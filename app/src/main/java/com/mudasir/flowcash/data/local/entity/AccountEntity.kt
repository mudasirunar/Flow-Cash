package com.mudasir.flowcash.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val network: String, // CASH, VISA, MASTERCARD, AMEX
    val lastFourDigits: String,
    val expiryDate: String,
    val cardColorHex: String // Hex string for card background color
)
