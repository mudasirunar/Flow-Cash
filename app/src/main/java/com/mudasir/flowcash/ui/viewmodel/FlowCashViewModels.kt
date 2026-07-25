package com.mudasir.flowcash.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mudasir.flowcash.data.model.CategoryType
import com.mudasir.flowcash.data.model.MockData
import com.mudasir.flowcash.data.model.TransactionItem
import com.mudasir.flowcash.data.model.TransactionType
import com.mudasir.flowcash.data.model.UserProfile
import com.mudasir.flowcash.data.preferences.ThemeMode
import com.mudasir.flowcash.data.preferences.ThemePreferences
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
            kotlinx.coroutines.delay(1000) // Simulate network auth delay
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
            kotlinx.coroutines.delay(1200)
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

@Immutable
data class DashboardUiState(
    val transactions: List<TransactionItem> = MockData.sampleTransactions,
    val searchQuery: String = "",
    val selectedFilter: TransactionType? = null
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val _transactions = MutableStateFlow(MockData.sampleTransactions)
    private val _searchQuery = MutableStateFlow("")
    private val _selectedFilter = MutableStateFlow<TransactionType?>(null)

    val filteredTransactions: StateFlow<List<TransactionItem>> = combine(
        _transactions,
        _searchQuery,
        _selectedFilter
    ) { txs, query, filter ->
        txs.filter { tx ->
            val matchesSearch = query.isBlank() || tx.title.contains(query, ignoreCase = true) || tx.subtitle.contains(query, ignoreCase = true)
            val matchesFilter = filter == null || tx.type == filter
            matchesSearch && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MockData.sampleTransactions)

    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val selectedFilter: StateFlow<TransactionType?> = _selectedFilter.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: TransactionType?) {
        _selectedFilter.value = filter
    }

    fun addTransaction(
        title: String,
        amount: Double,
        type: TransactionType,
        category: CategoryType
    ) {
        val newTx = TransactionItem(
            id = "tx_${System.currentTimeMillis()}",
            title = title,
            subtitle = "Manual entry",
            amount = amount,
            type = type,
            category = category,
            dateFormatted = "Just now",
            timestamp = System.currentTimeMillis()
        )
        _transactions.value = listOf(newTx) + _transactions.value
    }
}

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val themePreferences = ThemePreferences(application)

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
}
