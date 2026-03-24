package com.example.syncspend.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AccountType { SAVINGS, CREDIT }

enum class TransactionType { PERSONAL_EXPENSE, LENT, REPAYMENT }

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: AccountType,
    val creditLimit: Double = 0.0,
    val currentBalance: Double = 0.0
)

@Entity(tableName = "entities")
data class EntityPerson(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val currentDebt: Double = 0.0
)

@Entity(tableName = "gateways")
data class Gateway(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: TransactionType,
    val title: String,
    val amount: Double,
    val dateMs: Long,
    val entityId: Long? = null,
    val accountId: Long,
    val gatewayId: Long? = null,
    val notes: String = ""
)
