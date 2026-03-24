package com.example.syncspend.data

import kotlinx.coroutines.flow.Flow

class SyncSpendRepository(private val db: AppDatabase) {

    val allAccounts: Flow<List<Account>> = db.accountDao().getAllAccounts()
    val allEntities: Flow<List<EntityPerson>> = db.entityDao().getAllEntities()
    val allGateways: Flow<List<Gateway>> = db.gatewayDao().getAllGateways()
    val allTransactions: Flow<List<Transaction>> = db.transactionDao().getAllTransactions()

    fun transactionsByEntity(entityId: Long) = db.transactionDao().getTransactionsByEntity(entityId)
    fun transactionsSince(fromMs: Long) = db.transactionDao().getTransactionsSince(fromMs)

    // Accounts
    suspend fun addAccount(account: Account) = db.accountDao().insert(account)
    suspend fun updateAccount(account: Account) = db.accountDao().update(account)
    suspend fun deleteAccount(account: Account) = db.accountDao().delete(account)
    suspend fun getAccountById(id: Long) = db.accountDao().getById(id)

    // Entities
    suspend fun addEntity(entity: EntityPerson) = db.entityDao().insert(entity)
    suspend fun updateEntity(entity: EntityPerson) = db.entityDao().update(entity)
    suspend fun deleteEntity(entity: EntityPerson) = db.entityDao().delete(entity)
    suspend fun getEntityById(id: Long) = db.entityDao().getById(id)

    // Gateways
    suspend fun addGateway(gateway: Gateway) = db.gatewayDao().insert(gateway)
    suspend fun deleteGateway(gateway: Gateway) = db.gatewayDao().delete(gateway)

    // Transactions with double-entry logic
    suspend fun addTransaction(transaction: Transaction) {
        val account = db.accountDao().getById(transaction.accountId) ?: return

        val newBalance = when (transaction.type) {
            TransactionType.PERSONAL_EXPENSE -> account.currentBalance - transaction.amount
            TransactionType.LENT -> account.currentBalance - transaction.amount
            TransactionType.REPAYMENT -> account.currentBalance + transaction.amount
        }
        db.accountDao().update(account.copy(currentBalance = newBalance))

        // Update entity debt
        transaction.entityId?.let { entityId ->
            val entity = db.entityDao().getById(entityId) ?: return@let
            val newDebt = when (transaction.type) {
                TransactionType.LENT -> entity.currentDebt + transaction.amount
                TransactionType.REPAYMENT -> entity.currentDebt - transaction.amount
                else -> entity.currentDebt
            }
            db.entityDao().update(entity.copy(currentDebt = newDebt))
        }

        db.transactionDao().insert(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        // Reverse the balance effect
        val account = db.accountDao().getById(transaction.accountId) ?: return
        val reversedBalance = when (transaction.type) {
            TransactionType.PERSONAL_EXPENSE -> account.currentBalance + transaction.amount
            TransactionType.LENT -> account.currentBalance + transaction.amount
            TransactionType.REPAYMENT -> account.currentBalance - transaction.amount
        }
        db.accountDao().update(account.copy(currentBalance = reversedBalance))

        // Reverse entity debt
        transaction.entityId?.let { entityId ->
            val entity = db.entityDao().getById(entityId) ?: return@let
            val reversedDebt = when (transaction.type) {
                TransactionType.LENT -> entity.currentDebt - transaction.amount
                TransactionType.REPAYMENT -> entity.currentDebt + transaction.amount
                else -> entity.currentDebt
            }
            db.entityDao().update(entity.copy(currentDebt = reversedDebt))
        }

        db.transactionDao().delete(transaction)
    }
}
