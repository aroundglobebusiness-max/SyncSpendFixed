package com.example.syncspend.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY name ASC")
    fun getAllAccounts(): Flow<List<Account>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: Account): Long

    @Update
    suspend fun update(account: Account)

    @Delete
    suspend fun delete(account: Account)

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getById(id: Long): Account?
}

@Dao
interface EntityDao {
    @Query("SELECT * FROM entities ORDER BY name ASC")
    fun getAllEntities(): Flow<List<EntityPerson>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: EntityPerson): Long

    @Update
    suspend fun update(entity: EntityPerson)

    @Delete
    suspend fun delete(entity: EntityPerson)

    @Query("SELECT * FROM entities WHERE id = :id")
    suspend fun getById(id: Long): EntityPerson?
}

@Dao
interface GatewayDao {
    @Query("SELECT * FROM gateways ORDER BY name ASC")
    fun getAllGateways(): Flow<List<Gateway>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(gateway: Gateway): Long

    @Delete
    suspend fun delete(gateway: Gateway)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY dateMs DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE entityId = :entityId ORDER BY dateMs DESC")
    fun getTransactionsByEntity(entityId: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE dateMs >= :fromMs ORDER BY dateMs DESC")
    fun getTransactionsSince(fromMs: Long): Flow<List<Transaction>>

    @Insert
    suspend fun insert(transaction: Transaction): Long

    @Delete
    suspend fun delete(transaction: Transaction)
}
