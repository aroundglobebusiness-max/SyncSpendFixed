package com.example.syncspend.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Converters {
    @TypeConverter
    fun fromAccountType(value: AccountType): String = value.name
    @TypeConverter
    fun toAccountType(value: String): AccountType = AccountType.valueOf(value)
    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name
    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)
}

@Database(
    entities = [Account::class, EntityPerson::class, Gateway::class, Transaction::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun entityDao(): EntityDao
    abstract fun gatewayDao(): GatewayDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "syncspend.db")
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { database ->
                                    // Default accounts
                                    database.accountDao().insert(Account(name = "Slice Credit Card", type = AccountType.CREDIT, creditLimit = 15000.0, currentBalance = 0.0))
                                    database.accountDao().insert(Account(name = "Super Credit Card", type = AccountType.CREDIT, creditLimit = 20000.0, currentBalance = 0.0))
                                    database.accountDao().insert(Account(name = "SBM Credit Card", type = AccountType.CREDIT, creditLimit = 10000.0, currentBalance = 0.0))
                                    database.accountDao().insert(Account(name = "Federal Bank Account", type = AccountType.SAVINGS, currentBalance = 0.0))
                                    database.accountDao().insert(Account(name = "Slice Savings Account", type = AccountType.SAVINGS, currentBalance = 0.0))
                                    // Default entities
                                    database.entityDao().insert(EntityPerson(name = "Personal"))
                                    database.entityDao().insert(EntityPerson(name = "Home"))
                                    database.entityDao().insert(EntityPerson(name = "Anandhu"))
                                    database.entityDao().insert(EntityPerson(name = "Balan"))
                                    // Default gateways
                                    listOf("SuperMoney", "Cred", "PhonePe", "Slice App", "Amazon Pay", "BHIM", "Ind Money", "Pop UPI", "Navi", "Salary Se").forEach {
                                        database.gatewayDao().insert(Gateway(name = it))
                                    }
                                }
                            }
                        }
                    })
                    .build().also { INSTANCE = it }
            }
        }
    }
}
