package com.example.syncspend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.syncspend.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(private val repo: SyncSpendRepository) : ViewModel() {

    val accounts = repo.allAccounts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val entities = repo.allEntities.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val gateways = repo.allGateways.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val transactions = repo.allTransactions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val netWorth: StateFlow<Double> = accounts.map { list ->
        list.sumOf { it.currentBalance }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun addTransaction(transaction: Transaction) = viewModelScope.launch {
        repo.addTransaction(transaction)
    }

    fun deleteTransaction(transaction: Transaction) = viewModelScope.launch {
        repo.deleteTransaction(transaction)
    }

    fun addAccount(name: String, type: AccountType, creditLimit: Double, initialBalance: Double) = viewModelScope.launch {
        repo.addAccount(Account(name = name, type = type, creditLimit = creditLimit, currentBalance = initialBalance))
    }

    fun updateAccount(account: Account) = viewModelScope.launch {
        repo.updateAccount(account)
    }

    fun deleteAccount(account: Account) = viewModelScope.launch {
        repo.deleteAccount(account)
    }

    fun addEntity(name: String) = viewModelScope.launch {
        repo.addEntity(EntityPerson(name = name))
    }

    fun deleteEntity(entity: EntityPerson) = viewModelScope.launch {
        repo.deleteEntity(entity)
    }

    fun addGateway(name: String) = viewModelScope.launch {
        repo.addGateway(Gateway(name = name))
    }

    fun deleteGateway(gateway: Gateway) = viewModelScope.launch {
        repo.deleteGateway(gateway)
    }

    fun getTransactionsByEntity(entityId: Long) = repo.transactionsByEntity(entityId)

    // Helper: get account name by id
    fun accountName(id: Long): String = accounts.value.find { it.id == id }?.name ?: "Unknown"
    fun entityName(id: Long?): String = if (id == null) "" else entities.value.find { it.id == id }?.name ?: "Unknown"
    fun gatewayName(id: Long?): String = if (id == null) "" else gateways.value.find { it.id == id }?.name ?: ""

    class Factory(private val repo: SyncSpendRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repo) as T
        }
    }
}
