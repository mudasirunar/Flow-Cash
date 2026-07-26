package com.mudasir.flowcash.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Edit
import com.mudasir.flowcash.ui.components.FlowCashAlertDialog
import com.mudasir.flowcash.ui.components.FlowCashConfirmDialog
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mudasir.flowcash.data.local.entity.AccountEntity
import com.mudasir.flowcash.data.model.CategoryType
import com.mudasir.flowcash.data.model.TransactionType
import com.mudasir.flowcash.ui.components.FloatingWelcomeBanner
import com.mudasir.flowcash.data.model.TransactionItem
import com.mudasir.flowcash.ui.theme.ExpenseRed
import com.mudasir.flowcash.ui.theme.IncomeGreen
import com.mudasir.flowcash.ui.theme.PrimaryIndigo
import androidx.compose.ui.platform.LocalContext
import com.mudasir.flowcash.util.BiometricAuthHelper
import com.mudasir.flowcash.ui.viewmodel.AuthViewModel
import com.mudasir.flowcash.ui.viewmodel.DashboardViewModel
import com.mudasir.flowcash.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    data object Dashboard : BottomNavItem("dashboard", "Dashboard", Icons.Default.Dashboard)
    data object Transactions : BottomNavItem("transactions", "History", Icons.AutoMirrored.Filled.ReceiptLong)
    data object Analytics : BottomNavItem("analytics", "Analytics", Icons.Default.Analytics)
    data object Settings : BottomNavItem("settings", "Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContainerScreen(
    authViewModel: AuthViewModel,
    dashboardViewModel: DashboardViewModel,
    settingsViewModel: SettingsViewModel,
    onLogoutClick: () -> Unit
) {
    val items = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Transactions,
        BottomNavItem.Analytics,
        BottomNavItem.Settings
    )

    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    var showAddModal by rememberSaveable { mutableStateOf(false) }
    var showAddAccountScreen by rememberSaveable { mutableStateOf(false) }

    var editingAccountId by rememberSaveable { mutableStateOf<String?>(null) }

    val authState by authViewModel.uiState.collectAsState()
    val currency by settingsViewModel.currency.collectAsState()
    val accountsState by dashboardViewModel.accounts.collectAsState()
    val accounts = remember(accountsState) { accountsState ?: emptyList() }
    var showAccountRequiredDialog by rememberSaveable { mutableStateOf(false) }
    val selectedAccount by dashboardViewModel.selectedAccount.collectAsState()
    var initialTransactionType by remember { mutableStateOf(TransactionType.INCOME) }
    var selectedTransactionForDetails by remember { mutableStateOf<TransactionItem?>(null) }
    var selectedTransactionForMenu by remember { mutableStateOf<TransactionItem?>(null) }
    var transactionToDelete by remember { mutableStateOf<TransactionItem?>(null) }
    var transactionToEdit by remember { mutableStateOf<TransactionItem?>(null) }

    val context = LocalContext.current
    val biometricsEnabled by settingsViewModel.biometricsEnabled.collectAsState()
    var biometricAlertTitle by remember { mutableStateOf<String?>(null) }
    var biometricAlertMessage by remember { mutableStateOf<String?>(null) }

    val accountToEdit = remember(editingAccountId, accounts) {
        accounts.find { it.id == editingAccountId }
    }

    val syncState by dashboardViewModel.syncState.collectAsState()

    LaunchedEffect(authState.user?.id) {
        authState.user?.id?.let { uid ->
            dashboardViewModel.startUserSync(uid)
        }
    }

    // Remote Deletion Guard: Close active edit sheets if the target item is deleted remotely
    val allTransactions by dashboardViewModel.transactions.collectAsState()
    LaunchedEffect(accounts, allTransactions) {
        if (editingAccountId != null && accounts.none { it.id == editingAccountId }) {
            showAddAccountScreen = false
            editingAccountId = null
        }
        if (transactionToEdit != null && allTransactions.none { it.id == transactionToEdit?.id }) {
            transactionToEdit = null
        }
        if (selectedTransactionForDetails != null && allTransactions.none { it.id == selectedTransactionForDetails?.id }) {
            selectedTransactionForDetails = null
        }
    }

    val userName = authState.user?.name ?: "User"
    val userEmail = authState.user?.email ?: "user@example.com"

    // Back handling to return to Dashboard (main screen) from other tabs before exiting
    if (selectedIndex != 0 && !showAddAccountScreen) {
        BackHandler {
            selectedIndex = 0
        }
    }

    // Back handling when AddAccountScreen overlay is active
    if (showAddAccountScreen) {
        BackHandler {
            showAddAccountScreen = false
            editingAccountId = null
        }

        // Fullscreen overlay hides bottom bar & covers status bar properly
        AddAccountScreen(
            accountToEdit = accountToEdit,
            onSave = { account ->
                dashboardViewModel.addAccount(account)
                showAddAccountScreen = false
                editingAccountId = null
            },
            onDelete = { account ->
                dashboardViewModel.deleteAccountAndTransactions(account)
                showAddAccountScreen = false
                editingAccountId = null
            },
            onBack = {
                showAddAccountScreen = false
                editingAccountId = null
            }
        )
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    items.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = selectedIndex == index,
                            onClick = { selectedIndex = index },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                AnimatedContent(
                    targetState = selectedIndex,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally(
                                initialOffsetX = { width -> width / 4 },
                                animationSpec = tween(280, easing = FastOutSlowInEasing)
                            ) + fadeIn(animationSpec = tween(250))) togetherWith
                                    (slideOutHorizontally(
                                        targetOffsetX = { width -> -width / 4 },
                                        animationSpec = tween(280, easing = FastOutSlowInEasing)
                                    ) + fadeOut(animationSpec = tween(220)))
                        } else {
                            (slideInHorizontally(
                                initialOffsetX = { width -> -width / 4 },
                                animationSpec = tween(280, easing = FastOutSlowInEasing)
                            ) + fadeIn(animationSpec = tween(250))) togetherWith
                                    (slideOutHorizontally(
                                        targetOffsetX = { width -> width / 4 },
                                        animationSpec = tween(280, easing = FastOutSlowInEasing)
                                    ) + fadeOut(animationSpec = tween(220)))
                        }
                    },
                    label = "BottomTabTransition"
                ) { targetIndex ->
                    when (targetIndex) {
                        0 -> DashboardScreen(
                            dashboardViewModel = dashboardViewModel,
                            settingsViewModel = settingsViewModel,
                            userName = userName,
                            userEmail = userEmail,
                            profilePicUrl = authState.user?.profilePicUrl,
                            avatarColorHex = authState.user?.avatarColorHex,
                            currencySymbol = currency,
                            onAddTransactionClick = { filterType ->
                                if (accounts.isEmpty()) {
                                    showAccountRequiredDialog = true
                                } else {
                                    initialTransactionType = filterType ?: TransactionType.INCOME
                                    showAddModal = true
                                }
                            },
                            onAddAccountClick = {
                                editingAccountId = null
                                showAddAccountScreen = true
                            },
                            onEditAccountClick = { account ->
                                if (biometricsEnabled) {
                                    BiometricAuthHelper.promptBiometricAuth(
                                        context = context,
                                        title = "Edit Card Security",
                                        subtitle = "Scan fingerprint or Face ID to edit ${account.name} details",
                                        onSuccess = {
                                            editingAccountId = account.id
                                            showAddAccountScreen = true
                                        },
                                        onError = { err ->
                                            biometricAlertTitle = "Authentication Required"
                                            biometricAlertMessage = err
                                        }
                                    )
                                } else {
                                    editingAccountId = account.id
                                    showAddAccountScreen = true
                                }
                            },
                            onTransactionClick = { tx -> selectedTransactionForDetails = tx },
                            onTransactionLongClick = { tx -> selectedTransactionForMenu = tx }
                        )
                        1 -> TransactionsScreen(
                            dashboardViewModel = dashboardViewModel,
                            currencySymbol = currency,
                            onAddTransactionClick = {
                                if (accounts.isEmpty()) {
                                    showAccountRequiredDialog = true
                                } else {
                                    initialTransactionType = TransactionType.INCOME
                                    showAddModal = true
                                }
                            },
                            onTransactionClick = { tx -> selectedTransactionForDetails = tx },
                            onTransactionLongClick = { tx -> selectedTransactionForMenu = tx }
                        )
                        2 -> AnalyticsScreen(
                            dashboardViewModel = dashboardViewModel,
                            currencySymbol = currency
                        )
                        3 -> SettingsScreen(
                            settingsViewModel = settingsViewModel,
                            authViewModel = authViewModel,
                            dashboardViewModel = dashboardViewModel,
                            userName = userName,
                            userEmail = userEmail,
                            onLogoutClick = onLogoutClick
                        )
                    }
                }
            }
        }
    }

    val welcomeEvent = authState.welcomeEvent
    val isDashboardLoading by dashboardViewModel.isLoading.collectAsState()
    FloatingWelcomeBanner(
        isVisible = welcomeEvent != null && !isDashboardLoading,
        userName = welcomeEvent?.name ?: userName,
        isNewUser = welcomeEvent?.isNewUser ?: false,
        onDismiss = { authViewModel.clearWelcomeEvent() }
    )

    if (biometricAlertMessage != null) {
        FlowCashAlertDialog(
            onDismissRequest = { biometricAlertMessage = null },
            title = biometricAlertTitle ?: "Biometric Security",
            message = biometricAlertMessage ?: "",
            icon = Icons.Default.Security
        )
    }

    if (showAccountRequiredDialog) {
        FlowCashConfirmDialog(
            onDismissRequest = { showAccountRequiredDialog = false },
            title = "Account Required",
            message = "You need to create a card or wallet account before adding transactions.",
            icon = Icons.Default.CreditCard,
            confirmButtonText = "Create",
            onConfirm = {
                showAccountRequiredDialog = false
                editingAccountId = null
                showAddAccountScreen = true
            }
        )
    }

    if (showAddModal) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showAddModal = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            AddTransactionSheetContent(
                initialType = initialTransactionType,
                initialAccountName = selectedAccount?.name,
                currencySymbol = currency,
                accountsList = accounts,
                onAdd = { title, amt, type, category, accountName, note, customCategory ->
                    dashboardViewModel.addTransaction(
                        title = title,
                        amount = amt,
                        type = type,
                        category = category,
                        accountName = accountName,
                        note = note,
                        subtitle = if (category == CategoryType.OTHER && !customCategory.isNullOrBlank()) customCategory else "Manual entry"
                    )
                    showAddModal = false
                }
            )
        }
    }

    val detailsTx = selectedTransactionForDetails
    if (detailsTx != null) {
        val isIncome = detailsTx.type == TransactionType.INCOME
        val sdf = remember { java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()) }
        val exactTime = remember(detailsTx.timestamp) { sdf.format(java.util.Date(detailsTx.timestamp)) }
        val relativeTime = remember(detailsTx.timestamp) { formatRelativeTime(detailsTx.timestamp) }

        androidx.compose.ui.window.Dialog(
            onDismissRequest = { selectedTransactionForDetails = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(28.dp))
                    .border(
                        1.5.dp,
                        (if (isIncome) IncomeGreen else ExpenseRed).copy(alpha = 0.45f),
                        RoundedCornerShape(28.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(if (isIncome) IncomeGreen.copy(alpha = 0.15f) else ExpenseRed.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(detailsTx.category),
                            contentDescription = detailsTx.category.name,
                            tint = if (isIncome) IncomeGreen else ExpenseRed,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "${if (isIncome) "+" else "-"}$currency ${String.format("%,.2f", detailsTx.amount)}",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isIncome) IncomeGreen else ExpenseRed
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = detailsTx.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Wallet, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Wallet / Account", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(detailsTx.accountName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Category, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Category", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            val catLabel = if (detailsTx.category == CategoryType.OTHER && detailsTx.subtitle.isNotBlank() && detailsTx.subtitle != "Manual entry") {
                                detailsTx.subtitle
                            } else {
                                detailsTx.category.name.lowercase().replaceFirstChar { it.uppercase() }
                            }
                            Text(catLabel, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Transaction Type", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(detailsTx.type.name.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = if (isIncome) IncomeGreen else ExpenseRed))
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Created At", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(relativeTime, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                                Text(exactTime, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    if (!detailsTx.note.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Note / Memo", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(detailsTx.note, style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic), color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { selectedTransactionForDetails = null },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isIncome) IncomeGreen else ExpenseRed,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    val menuTx = selectedTransactionForMenu
    if (menuTx != null) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { selectedTransactionForMenu = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Transaction Options",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

                androidx.compose.material3.TextButton(
                    onClick = {
                        selectedTransactionForDetails = menuTx
                        selectedTransactionForMenu = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("View Details", color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                androidx.compose.material3.TextButton(
                    onClick = {
                        transactionToEdit = menuTx
                        selectedTransactionForMenu = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Edit Transaction", color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                androidx.compose.material3.TextButton(
                    onClick = {
                        transactionToDelete = menuTx
                        selectedTransactionForMenu = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = ExpenseRed)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Delete Transaction", color = ExpenseRed)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    val deleteTx = transactionToDelete
    if (deleteTx != null) {
        FlowCashConfirmDialog(
            onDismissRequest = { transactionToDelete = null },
            title = "Delete Transaction?",
            message = "Are you sure you want to permanently delete this transaction? This action will adjust the balance of your account.",
            icon = Icons.Default.DeleteForever,
            isDestructive = true,
            confirmButtonText = "Delete",
            onConfirm = {
                dashboardViewModel.deleteTransaction(deleteTx.id)
                transactionToDelete = null
            }
        )
    }

    val editTx = transactionToEdit
    if (editTx != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { transactionToEdit = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            AddTransactionSheetContent(
                initialType = editTx.type,
                initialAccountName = editTx.accountName,
                currencySymbol = currency,
                accountsList = accounts,
                transactionToEdit = editTx,
                onAdd = { title, amt, type, category, accountName, note, customCategory ->
                    dashboardViewModel.updateTransaction(
                        id = editTx.id,
                        title = title,
                        amount = amt,
                        type = type,
                        category = category,
                        accountName = accountName,
                        note = note,
                        subtitle = if (category == CategoryType.OTHER && !customCategory.isNullOrBlank()) customCategory else "Manual entry",
                        createdAt = editTx.createdAt,
                        timestamp = editTx.timestamp
                    )
                    transactionToEdit = null
                }
            )
        }
    }
}

@Composable
fun AddTransactionSheetContent(
    initialType: TransactionType = TransactionType.INCOME,
    initialAccountName: String? = null,
    currencySymbol: String = "$",
    accountsList: List<AccountEntity> = emptyList(),
    transactionToEdit: TransactionItem? = null,
    onAdd: (title: String, amount: Double, type: TransactionType, category: CategoryType, account: String, note: String?, customCategory: String?) -> Unit
) {
    var selectedType by rememberSaveable(initialType, transactionToEdit) { mutableStateOf(transactionToEdit?.type ?: initialType) }
    var title by rememberSaveable(transactionToEdit) { mutableStateOf(transactionToEdit?.title ?: "") }
    var amountText by rememberSaveable(transactionToEdit) { mutableStateOf(transactionToEdit?.amount?.toString() ?: "") }
    var selectedCategory by rememberSaveable(transactionToEdit) { mutableStateOf(transactionToEdit?.category ?: CategoryType.SALARY) }
    LaunchedEffect(selectedType) {
        if (!selectedCategory.isApplicableTo(selectedType)) {
            selectedCategory = if (selectedType == TransactionType.INCOME) CategoryType.SALARY else CategoryType.SHOPPING
        }
    }
    var customCategoryText by rememberSaveable(transactionToEdit) { mutableStateOf(if (transactionToEdit?.category == CategoryType.OTHER) transactionToEdit.subtitle else "") }
    var amountError by remember { mutableStateOf<String?>(null) }
    var titleError by remember { mutableStateOf<String?>(null) }

    val defaultAccount = remember(initialAccountName, accountsList, transactionToEdit) {
        if (transactionToEdit != null) {
            transactionToEdit.accountName
        } else {
            val matchedAccount = if (!initialAccountName.isNullOrBlank()) {
                accountsList.find { it.name == initialAccountName }
            } else {
                null
            }
            matchedAccount?.name ?: accountsList.firstOrNull()?.name ?: "Main Wallet"
        }
    }
    var selectedAccount by rememberSaveable(defaultAccount) { mutableStateOf(defaultAccount) }
    var noteText by rememberSaveable(transactionToEdit) { mutableStateOf(transactionToEdit?.note ?: "") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val selectedAccountType = remember(selectedAccount, accountsList) {
        accountsList.find { it.name == selectedAccount }?.accountType ?: "CASH_WALLET"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = if (transactionToEdit != null) "Edit Transaction" else "Add Transaction",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Transaction Type Segmented Toggle (Income & Expense only)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf(
                TransactionType.INCOME to "Income",
                TransactionType.EXPENSE to "Expense"
            ).forEach { (type, label) ->
                val isSelected = selectedType == type
                val color = when (type) {
                    TransactionType.INCOME -> IncomeGreen
                    TransactionType.EXPENSE -> ExpenseRed
                    else -> MaterialTheme.colorScheme.primary
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) color else Color.Transparent)
                        .clickable { selectedType = type }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. Amount Input
        OutlinedTextField(
            value = amountText,
            onValueChange = {
                amountText = it
                amountError = null
            },
            label = { Text("Amount ($currencySymbol)") },
            placeholder = { Text("0.00") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
            singleLine = true,
            isError = amountError != null,
            supportingText = amountError?.let { { Text(it) } },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Description / Merchant Input
        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
                titleError = null
            },
            label = { Text("Description / Merchant") },
            placeholder = { Text("e.g. Starbucks, Salary, Amazon") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            singleLine = true,
            isError = titleError != null,
            supportingText = titleError?.let { { Text(it) } },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Category Horizontal Row Selector
        Text(
            text = "Category",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(8.dp))

        val filteredCategories = remember(selectedType) {
            CategoryType.entries.filter { it.isApplicableTo(selectedType) }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(filteredCategories) { category ->
                val isSelected = selectedCategory == category
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        .clickable { selectedCategory = category }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = getCategoryIcon(category),
                            contentDescription = category.name,
                            tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = category.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }
        }

        // Custom Category text field shown if OTHER is selected
        if (selectedCategory == CategoryType.OTHER) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = customCategoryText,
                onValueChange = { customCategoryText = it },
                label = { Text("Custom Category Name") },
                placeholder = { Text("e.g. Gift, Tax, Dividends") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5. Account Dropdown Selector
        Text(
            text = "Account / Wallet",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                .clickable { dropdownExpanded = true }
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = getLocalAccountTypeIcon(selectedAccountType),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = selectedAccount,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            DropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                accountsList.forEach { account ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = account.name,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (account.name == selectedAccount) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        onClick = {
                            selectedAccount = account.name
                            dropdownExpanded = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = getLocalAccountTypeIcon(account.accountType),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 6. Note Input
        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            label = { Text("Note / Memo (Optional)") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 7. Save Action Button
        Button(
            onClick = {
                val amt = amountText.toDoubleOrNull() ?: 0.0
                var hasError = false

                if (amountText.isBlank()) {
                    amountError = "Amount is required"
                    hasError = true
                } else if (amt <= 0.0) {
                    amountError = "Amount must be greater than 0"
                    hasError = true
                } else {
                    amountError = null
                }

                if (title.isBlank()) {
                    titleError = "Description / Merchant is required"
                    hasError = true
                } else {
                    titleError = null
                }

                if (!hasError) {
                    onAdd(
                        title,
                        amt,
                        selectedType,
                        selectedCategory,
                        selectedAccount,
                        noteText.ifBlank { null },
                        if (selectedCategory == CategoryType.OTHER) customCategoryText.ifBlank { null } else null
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = "Save")
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Save Transaction",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

private fun getLocalAccountTypeIcon(type: String): ImageVector {
    return when (type) {
        "CARD" -> Icons.Default.CreditCard
        "CASH_WALLET" -> Icons.Default.Wallet
        "BANK_ACCOUNT" -> Icons.Default.AccountBalance
        "INVESTMENT" -> Icons.AutoMirrored.Filled.ShowChart
        "FREELANCE_INCOME" -> Icons.Default.Work
        "SAVINGS" -> Icons.Default.Savings
        else -> Icons.Default.AccountBalanceWallet
    }
}
