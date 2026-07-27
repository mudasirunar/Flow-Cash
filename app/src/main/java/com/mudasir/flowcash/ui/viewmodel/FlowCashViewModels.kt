package com.mudasir.flowcash.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mudasir.flowcash.data.local.FlowCashDatabase
import com.mudasir.flowcash.data.local.entity.AccountEntity
import com.mudasir.flowcash.data.local.entity.BudgetEntity
import com.mudasir.flowcash.data.model.CategoryType
import com.mudasir.flowcash.data.model.TransactionItem
import com.mudasir.flowcash.data.model.TransactionType
import com.mudasir.flowcash.data.model.UserProfile
import com.mudasir.flowcash.data.preferences.ThemeMode
import com.mudasir.flowcash.data.preferences.UserPreferences
import com.mudasir.flowcash.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import com.google.firebase.auth.AuthCredential
import com.mudasir.flowcash.data.repository.AuthRepository
import com.mudasir.flowcash.data.repository.FirebaseAuthRepositoryImpl

@Immutable
data class WelcomeEvent(
    val name: String,
    val isNewUser: Boolean
)

@Immutable
data class AuthUiState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val user: UserProfile? = null,
    val errorMessage: String? = null,
    val welcomeEvent: WelcomeEvent? = null
)

class AuthViewModel @JvmOverloads constructor(
    application: Application,
    private val authRepository: AuthRepository = FirebaseAuthRepositoryImpl()
) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val savedEmail: StateFlow<String> = userPreferences.savedEmailFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    val rememberMePreference: StateFlow<Boolean> = userPreferences.rememberMeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    init {
        checkExistingSession()
    }

    fun checkExistingSession() {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            val userProfile = authRepository.getCurrentUserProfile()
            _uiState.value = AuthUiState(
                isLoggedIn = true,
                isLoading = false,
                user = userProfile
            )
        } else {
            _uiState.value = AuthUiState(isLoggedIn = false, isLoading = false)
        }
    }

    fun saveRememberMe(remember: Boolean, email: String) {
        viewModelScope.launch {
            userPreferences.saveRememberMe(remember, email)
        }
    }

    private var errorDismissJob: kotlinx.coroutines.Job? = null

    private fun setErrorMessage(message: String) {
        errorDismissJob?.cancel()
        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = message)
        errorDismissJob = viewModelScope.launch {
            kotlinx.coroutines.delay(3000)
            _uiState.value = _uiState.value.copy(errorMessage = null)
        }
    }

    fun clearError() {
        errorDismissJob?.cancel()
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearWelcomeEvent() {
        _uiState.value = _uiState.value.copy(welcomeEvent = null)
    }

    fun login(email: String, pass: String, rememberMe: Boolean = true, onSuccess: () -> Unit) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || pass.isBlank()) {
            setErrorMessage("Please enter both email and password")
            return
        }

        clearError()
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            val result = authRepository.loginWithEmail(trimmedEmail, pass)
            result.onSuccess { userProfile ->
                userPreferences.saveRememberMe(rememberMe, trimmedEmail)
                _uiState.value = AuthUiState(
                    isLoggedIn = true,
                    isLoading = false,
                    user = userProfile,
                    welcomeEvent = WelcomeEvent(name = userProfile.name, isNewUser = false)
                )
                onSuccess()
            }.onFailure { exception ->
                setErrorMessage(formatAuthErrorMessage(exception))
            }
        }
    }

    fun signUp(
        firstName: String,
        lastName: String,
        email: String,
        pass: String,
        confirmPass: String,
        onSuccess: () -> Unit
    ) {
        val trimmedFirst = firstName.trim()
        val trimmedLast = lastName.trim()
        val trimmedEmail = email.trim()

        if (trimmedFirst.isBlank() || trimmedLast.isBlank() || trimmedEmail.isBlank() || pass.isBlank()) {
            setErrorMessage("Please fill in all required fields")
            return
        }

        if (pass != confirmPass) {
            setErrorMessage("Passwords do not match. Please verify both password fields.")
            return
        }

        if (pass.length < 8 || !pass.any { it.isUpperCase() } || !pass.any { it.isDigit() }) {
            setErrorMessage("Password does not meet security criteria. Needs 8+ characters with uppercase letter and digit.")
            return
        }

        clearError()
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            val result = authRepository.signUpWithEmail(trimmedFirst, trimmedLast, trimmedEmail, pass)
            result.onSuccess { userProfile ->
                userPreferences.saveRememberMe(true, trimmedEmail)
                _uiState.value = AuthUiState(
                    isLoggedIn = true,
                    isLoading = false,
                    user = userProfile,
                    welcomeEvent = WelcomeEvent(name = userProfile.name, isNewUser = true)
                )
                onSuccess()
            }.onFailure { exception ->
                setErrorMessage(formatAuthErrorMessage(exception))
            }
        }
    }

    fun signInWithGoogleCredential(credential: AuthCredential, onSuccess: () -> Unit) {
        clearError()
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            val result = authRepository.signInWithCredential(credential)
            result.onSuccess { userProfile ->
                _uiState.value = AuthUiState(
                    isLoggedIn = true,
                    isLoading = false,
                    user = userProfile,
                    welcomeEvent = WelcomeEvent(name = userProfile.name, isNewUser = false)
                )
                onSuccess()
            }.onFailure { exception ->
                setErrorMessage(formatAuthErrorMessage(exception))
            }
        }
    }

    fun logout() {
        authRepository.logout()
        com.mudasir.flowcash.util.GoogleAuthHelper.signOut(getApplication())
        viewModelScope.launch {
            val db = FlowCashDatabase.getDatabase(getApplication())
            val repo = TransactionRepository(db.transactionDao(), db.budgetDao(), db.accountDao())
            repo.clearLocalDatabase()
            val userPreferences = UserPreferences(getApplication())
            userPreferences.clearUserPreferences()
        }
        _uiState.value = AuthUiState(isLoggedIn = false, user = null)
    }

    private fun formatAuthErrorMessage(exception: Throwable): String {
        if (exception is com.google.firebase.auth.FirebaseAuthException) {
            when (exception.errorCode) {
                "ERROR_INVALID_EMAIL", "ERROR_INVALID_USER_EMAIL" ->
                    return "Please enter a valid email address."
                "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL", "ERROR_CREDENTIAL_ALREADY_IN_USE" ->
                    return "Incorrect email address or password. Please check your credentials and try again."
                "ERROR_USER_NOT_FOUND" ->
                    return "No account found with this email address. Please check your email or sign up."
                "ERROR_USER_DISABLED" ->
                    return "This account has been disabled. Please contact support."
                "ERROR_EMAIL_ALREADY_IN_USE" ->
                    return "An account with this email address already exists. Please log in instead."
                "ERROR_WEAK_PASSWORD" ->
                    return "Password is too weak. Please use at least 8 characters with letters and numbers."
                "ERROR_TOO_MANY_REQUESTS" ->
                    return "Too many failed attempts. Please wait a moment and try again later."
            }
        }

        val message = exception.localizedMessage ?: exception.message ?: ""
        val lower = message.lowercase()

        return when {
            lower.contains("invalid-credential") || lower.contains("wrong-password") ||
            lower.contains("invalid_login_credentials") || lower.contains("malformed") ||
            lower.contains("auth_credential") || lower.contains("badly formatted") ->
                "Incorrect email address or password. Please check your credentials and try again."
            lower.contains("user-not-found") || lower.contains("user-disabled") || lower.contains("no user") ->
                "No account found with this email address. Please check your email or sign up."
            lower.contains("email-already-in-use") || lower.contains("already in use") ->
                "An account with this email address already exists. Please log in instead."
            lower.contains("invalid-email") || lower.contains("invalid email") ->
                "Please enter a valid email address."
            lower.contains("weak-password") || lower.contains("weak password") ->
                "Your password is too weak. Please use at least 8 characters with letters and numbers."
            lower.contains("network") || lower.contains("unreachable") || lower.contains("timeout") || lower.contains("connection") ->
                "Network error. Please check your internet connection and try again."
            lower.contains("too-many-requests") || lower.contains("too many requests") ->
                "Too many failed attempts. Please wait a moment and try again later."
            else ->
                "Authentication failed. Please check your details and try again."
        }
    }
}

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val database = FlowCashDatabase.getDatabase(application)
    private val repository = TransactionRepository(database.transactionDao(), database.budgetDao(), database.accountDao())
    private val userPreferences = UserPreferences(application)

    val syncState: StateFlow<com.mudasir.flowcash.data.remote.SyncState> = repository.syncState

    init {
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            repository.startSync(currentUser.uid)
        }
    }

    fun startUserSync(userId: String) {
        repository.startSync(userId)
    }

    private val _searchQuery = MutableStateFlow("")
    private val _selectedFilter = MutableStateFlow<TransactionType?>(null)
    private val _selectedAccount = MutableStateFlow<AccountEntity?>(null)
    val isDataVisible: StateFlow<Boolean> = userPreferences.isDataVisibleFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = true
    )

    fun setDataVisible(visible: Boolean) {
        viewModelScope.launch {
            userPreferences.setDataVisible(visible)
        }
    }

    private val _isInitialSelectedAccountLoaded = MutableStateFlow(false)

    val accounts: StateFlow<List<AccountEntity>?> = repository.allAccounts
        .map<List<AccountEntity>, List<AccountEntity>?> { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    val isLoading: StateFlow<Boolean> = combine(
        accounts,
        _isInitialSelectedAccountLoaded
    ) { accs, loaded ->
        accs == null || !loaded
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

    init {
        viewModelScope.launch {
            val savedId = userPreferences.selectedAccountIdFlow.first()
            if (savedId.isNotBlank()) {
                val accountList = repository.allAccounts.first()
                val matched = accountList.find { it.id == savedId }
                _selectedAccount.value = matched
            }
            _isInitialSelectedAccountLoaded.value = true
        }
    }

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
    val selectedAccount: StateFlow<AccountEntity?> = _selectedAccount.asStateFlow()

    val budgets: StateFlow<List<BudgetEntity>> = repository.allBudgets.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateBudget(categoryName: String, limit: Double) {
        viewModelScope.launch {
            repository.setBudget(categoryName, limit)
        }
    }

    fun addAccount(account: AccountEntity) {
        viewModelScope.launch {
            repository.addAccount(account)
        }
    }

    fun deleteAccount(id: String) {
        viewModelScope.launch {
            repository.deleteAccount(id)
        }
    }

    fun setSelectedAccount(account: AccountEntity?) {
        _selectedAccount.value = account
        viewModelScope.launch {
            userPreferences.setSelectedAccountId(account?.id ?: "")
        }
    }

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
        category: CategoryType,
        accountName: String = "Main Wallet",
        note: String? = null,
        subtitle: String = "Manual entry"
    ) {
        viewModelScope.launch {
            repository.addTransaction(
                title = title,
                subtitle = subtitle,
                amount = amount,
                type = type,
                category = category,
                accountName = accountName,
                note = note
            )
        }
    }

    fun updateTransaction(
        id: String,
        title: String,
        amount: Double,
        type: TransactionType,
        category: CategoryType,
        accountName: String = "Main Wallet",
        note: String? = null,
        subtitle: String = "Manual entry",
        createdAt: Long,
        timestamp: Long
    ) {
        viewModelScope.launch {
            repository.updateTransaction(
                id = id,
                title = title,
                subtitle = subtitle,
                amount = amount,
                type = type,
                category = category,
                accountName = accountName,
                note = note,
                createdAt = createdAt,
                timestamp = timestamp
            )
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }

    // 1. Cascading delete: removes account AND its associated transactions
    fun deleteAccountAndTransactions(account: AccountEntity) {
        viewModelScope.launch {
            // Delete all transactions linked to this account name from Room
            repository.deleteTransactionsByAccountName(account.name)
            // Delete the account entity itself
            repository.deleteAccount(account.id)
 
            // Reset selected account filter if the active account was deleted
            if (_selectedAccount.value?.id == account.id) {
                setSelectedAccount(null)
            }
        }
    }

    // 2. Add or update account entity (Room `insert` using OnConflictStrategy.REPLACE will update existing IDs)
    fun updateAccount(account: AccountEntity) {
        viewModelScope.launch {
            repository.addAccount(account)
            // If the edited account was currently selected, update the StateFlow reference
            if (_selectedAccount.value?.id == account.id) {
                _selectedAccount.value = account
            }
        }
    }
}

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)
    private val database = FlowCashDatabase.getDatabase(application)
    private val repository = TransactionRepository(database.transactionDao(), database.budgetDao(), database.accountDao())

    val themeMode: StateFlow<ThemeMode> = userPreferences.themeModeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ThemeMode.SYSTEM
    )

    val currency: StateFlow<String> = userPreferences.currencyFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "$"
    )

    val currencyCode: StateFlow<String> = userPreferences.currencyCodeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "USD"
    )

    val biometricsEnabled: StateFlow<Boolean> = userPreferences.biometricsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val dailyReminderEnabled: StateFlow<Boolean> = userPreferences.dailyReminderFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val weeklySummaryEnabled: StateFlow<Boolean> = userPreferences.weeklySummaryFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    private val _isResettingData = MutableStateFlow(false)
    val isResettingData: StateFlow<Boolean> = _isResettingData.asStateFlow()

    val unsyncedCount: StateFlow<Int> = repository.unsyncedCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            userPreferences.setThemeMode(mode)
        }
    }

    fun setCurrency(symbol: String, code: String = "") {
        viewModelScope.launch {
            userPreferences.setCurrency(symbol, code)
        }
    }

    fun setBiometricsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setBiometricsEnabled(enabled)
        }
    }

    fun setDailyReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setDailyReminderEnabled(enabled)
        }
    }

    fun setWeeklySummaryEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setWeeklySummaryEnabled(enabled)
        }
    }

    fun clearLocalData(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isResettingData.value = true
            delay(600)
            repository.clearLocalDatabase()
            _isResettingData.value = false
            onComplete()
        }
    }
}
