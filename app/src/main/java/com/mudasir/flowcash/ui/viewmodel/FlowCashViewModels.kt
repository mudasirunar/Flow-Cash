package com.mudasir.flowcash.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mudasir.flowcash.data.local.FlowCashDatabase
import com.mudasir.flowcash.data.model.CategoryType
import com.mudasir.flowcash.data.model.TransactionItem
import com.mudasir.flowcash.data.model.TransactionType
import com.mudasir.flowcash.data.model.UserProfile
import com.mudasir.flowcash.data.preferences.ThemeMode
import com.mudasir.flowcash.data.preferences.ThemePreferences
import com.mudasir.flowcash.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class AuthUiState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val user: UserProfile? = null,
    val errorMessage: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please fill in all fields")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            kotlinx.coroutines.delay(800)
            val user = UserProfile(
                id = "usr_101",
                name = if (email.contains("@")) email.substringBefore("@").replaceFirstChar { it.uppercase() } else "Mudasir",
                email = email
            )
            _uiState.value = AuthUiState(isLoggedIn = true, isLoading = false, user = user)
            onSuccess()
        }
    }

    fun signUp(name: String, email: String, pass: String, onSuccess: () -> Unit) {
        if (name.isBlank() || email.isBlank() || pass.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please fill in all details")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            val user = UserProfile(id = "usr_102", name = name, email = email)
            _uiState.value = AuthUiState(isLoggedIn = true, isLoading = false, user = user)
            onSuccess()
        }
    }

    fun logout() {
        _uiState.value = AuthUiState(isLoggedIn = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val database = FlowCashDatabase.getDatabase(application)
    private val repository = TransactionRepository(database.transactionDao(), database.budgetDao(), database.accountDao())

    private val _searchQuery = MutableStateFlow("")
    private val _selectedFilter = MutableStateFlow<TransactionType?>(null)
    private val _selectedAccount = MutableStateFlow<com.mudasir.flowcash.data.local.entity.AccountEntity?>(null)

    val transactions: StateFlow<List<TransactionItem>> = repository.allTransactions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val filteredTransactions: StateFlow<List<TransactionItem>> = combine(
        transactions,
        _searchQuery,
        _selectedFilter,
        _selectedAccount
    ) { txs, query, filter, account ->
        txs.filter { tx ->
            val matchesSearch = query.isBlank() || tx.title.contains(query, ignoreCase = true) || tx.subtitle.contains(query, ignoreCase = true)
            val matchesFilter = filter == null || tx.type == filter
            val matchesAccount = account == null || tx.accountName.equals(account.name, ignoreCase = true)
            matchesSearch && matchesFilter && matchesAccount
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val selectedFilter: StateFlow<TransactionType?> = _selectedFilter.asStateFlow()
    val selectedAccount: StateFlow<com.mudasir.flowcash.data.local.entity.AccountEntity?> = _selectedAccount.asStateFlow()

    val accounts: StateFlow<List<com.mudasir.flowcash.data.local.entity.AccountEntity>> = repository.allAccounts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val budgets: StateFlow<List<com.mudasir.flowcash.data.local.entity.BudgetEntity>> = repository.allBudgets.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateBudget(categoryName: String, limit: Double) {
        viewModelScope.launch {
            repository.setBudget(categoryName, limit)
        }
    }

    fun addAccount(name: String, network: String, lastFourDigits: String, expiryDate: String, colorHex: String) {
        viewModelScope.launch {
            repository.addAccount(name, network, lastFourDigits, expiryDate, colorHex)
        }
    }

    fun setSelectedAccount(account: com.mudasir.flowcash.data.local.entity.AccountEntity?) {
        _selectedAccount.value = account
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: TransactionType?) {
        _selectedFilter.value = filter
    }

    fun setSelectedAccount(account: String) {
        // Obsolete method replaced by Object overload
    }

    fun addTransaction(
        title: String,
        amount: Double,
        type: TransactionType,
        category: CategoryType,
        accountName: String = "Main Wallet",
        note: String? = null
    ) {
        viewModelScope.launch {
            repository.addTransaction(
                title = title,
                subtitle = "Manual entry",
                amount = amount,
                type = type,
                category = category,
                accountName = accountName,
                note = note
            )
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }
}

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val themePreferences = ThemePreferences(application)
    private val database = FlowCashDatabase.getDatabase(application)
    private val repository = TransactionRepository(database.transactionDao(), database.budgetDao(), database.accountDao())

    val themeMode: StateFlow<ThemeMode> = themePreferences.themeModeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ThemeMode.SYSTEM
    )

    val currency: StateFlow<String> = themePreferences.currencyFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "$"
    )

    val biometricsEnabled: StateFlow<Boolean> = themePreferences.biometricsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val unsyncedCount: StateFlow<Int> = repository.unsyncedCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themePreferences.setThemeMode(mode)
        }
    }

    fun setCurrency(currency: String) {
        viewModelScope.launch {
            themePreferences.setCurrency(currency)
        }
    }

    fun setBiometricsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            themePreferences.setBiometricsEnabled(enabled)
        }
    }

    fun clearLocalData() {
        viewModelScope.launch {
            repository.clearDatabase()
        }
    }
}
