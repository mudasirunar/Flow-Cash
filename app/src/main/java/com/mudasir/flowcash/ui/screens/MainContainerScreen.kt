package com.mudasir.flowcash.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mudasir.flowcash.data.local.entity.AccountEntity
import com.mudasir.flowcash.data.model.CategoryType
import com.mudasir.flowcash.data.model.TransactionItem
import com.mudasir.flowcash.data.model.TransactionType
import com.mudasir.flowcash.ui.components.BottomNavItem
import com.mudasir.flowcash.ui.components.FloatingBottomNavigation
import com.mudasir.flowcash.ui.components.FloatingWelcomeBanner
import com.mudasir.flowcash.ui.components.FlowCashAlertDialog
import com.mudasir.flowcash.ui.components.FlowCashConfirmDialog
import com.mudasir.flowcash.ui.theme.ExpenseRed
import com.mudasir.flowcash.ui.theme.IncomeGreen
import com.mudasir.flowcash.ui.viewmodel.AuthViewModel
import com.mudasir.flowcash.ui.viewmodel.DashboardViewModel
import com.mudasir.flowcash.ui.viewmodel.SettingsViewModel
import com.mudasir.flowcash.util.BiometricAuthHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContainerScreen(
        authViewModel: AuthViewModel,
        dashboardViewModel: DashboardViewModel,
        settingsViewModel: SettingsViewModel,
        onLogoutClick: () -> Unit
) {
    val items =
            listOf(
                    BottomNavItem.Dashboard,
                    BottomNavItem.Transactions,
                    BottomNavItem.Analytics,
                    BottomNavItem.Settings
            )

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { items.size })
    val selectedIndex = pagerState.currentPage
    val coroutineScope = rememberCoroutineScope()

    var navBarHeightPx by remember { mutableIntStateOf(0) }
    val localDensity = LocalDensity.current
    val navBarHeightDp =
            remember(navBarHeightPx, localDensity) { with(localDensity) { navBarHeightPx.toDp() } }
    var showAddModal by rememberSaveable { mutableStateOf(false) }
    var showAddAccountScreen by rememberSaveable { mutableStateOf(false) }

    var editingAccountId by rememberSaveable { mutableStateOf<String?>(null) }

    val authState by authViewModel.uiState.collectAsState()
    val currency by settingsViewModel.currency.collectAsState()
    val isDashboardLoading by dashboardViewModel.isLoading.collectAsState()
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

    val accountToEdit =
            remember(editingAccountId, accounts) { accounts.find { it.id == editingAccountId } }

    val syncState by dashboardViewModel.syncState.collectAsState()

    LaunchedEffect(authState.user?.id) {
        authState.user?.id?.let { uid -> dashboardViewModel.startUserSync(uid) }
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
        if (selectedTransactionForDetails != null &&
                        allTransactions.none { it.id == selectedTransactionForDetails?.id }
        ) {
            selectedTransactionForDetails = null
        }
    }

    val userName = authState.user?.name ?: "User"
    val userEmail = authState.user?.email ?: "user@example.com"

    // Back handling to return to Dashboard (main screen) from other tabs before exiting
    if (selectedIndex != 0 && !showAddAccountScreen) {
        BackHandler {
            coroutineScope.launch {
                pagerState.animateScrollToPage(
                        page = 0,
                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                )
            }
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
                    dashboardViewModel.setSelectedAccount(account)
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
        Scaffold(containerColor = Color.Transparent) { innerPadding ->
            Box(
                    modifier =
                            Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding())
            ) {
                val calculatedBottomPadding =
                        remember(navBarHeightDp) {
                            if (navBarHeightDp > 0.dp) navBarHeightDp + 16.dp else 100.dp
                        }

                @OptIn(ExperimentalFoundationApi::class)
                CompositionLocalProvider(LocalOverscrollFactory provides null) {
                    HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize(),
                            userScrollEnabled = !showAddAccountScreen
                    ) { page ->
                        when (page) {
                            0 ->
                                    DashboardScreen(
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
                                                    initialTransactionType =
                                                            filterType ?: TransactionType.INCOME
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
                                                            subtitle =
                                                                    "Scan fingerprint or Face ID to edit ${account.name} details",
                                                            onSuccess = {
                                                                editingAccountId = account.id
                                                                showAddAccountScreen = true
                                                            },
                                                            onError = { err ->
                                                                biometricAlertTitle =
                                                                        "Authentication Required"
                                                                biometricAlertMessage = err
                                                            }
                                                    )
                                                } else {
                                                    editingAccountId = account.id
                                                    showAddAccountScreen = true
                                                }
                                            },
                                            onTransactionClick = { tx ->
                                                selectedTransactionForDetails = tx
                                            },
                                            onTransactionLongClick = { tx ->
                                                selectedTransactionForMenu = tx
                                            },
                                            bottomPadding = calculatedBottomPadding
                                    )
                            1 ->
                                    TransactionsScreen(
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
                                            onTransactionClick = { tx ->
                                                selectedTransactionForDetails = tx
                                            },
                                            onTransactionLongClick = { tx ->
                                                selectedTransactionForMenu = tx
                                            },
                                            bottomPadding = calculatedBottomPadding
                                    )
                            2 ->
                                    AnalyticsScreen(
                                            dashboardViewModel = dashboardViewModel,
                                            currencySymbol = currency,
                                            bottomPadding = calculatedBottomPadding
                                    )
                            3 ->
                                    SettingsScreen(
                                            settingsViewModel = settingsViewModel,
                                            authViewModel = authViewModel,
                                            dashboardViewModel = dashboardViewModel,
                                            userName = userName,
                                            userEmail = userEmail,
                                            onLogoutClick = onLogoutClick,
                                            bottomPadding = calculatedBottomPadding
                                    )
                        }
                    }
                }

                val isImeVisible =
                        WindowInsets.ime.asPaddingValues().calculateBottomPadding() > 0.dp

                AnimatedVisibility(
                        visible = !isDashboardLoading && !isImeVisible,
                        enter =
                                fadeIn(animationSpec = tween(300)) +
                                        slideInVertically(initialOffsetY = { it / 2 }),
                        exit =
                                fadeOut(animationSpec = tween(200)) +
                                        slideOutVertically(targetOffsetY = { it / 2 }),
                        modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    FloatingBottomNavigation(
                            items = items,
                            pagerState = pagerState,
                            onItemSelected = { index ->
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(
                                            page = index,
                                            animationSpec =
                                                    tween(
                                                            durationMillis = 300,
                                                            easing = FastOutSlowInEasing
                                                    )
                                    )
                                }
                            },
                            modifier =
                                    Modifier.onGloballyPositioned { coordinates ->
                                        if (coordinates.size.height != navBarHeightPx) {
                                            navBarHeightPx = coordinates.size.height
                                        }
                                    }
                    )
                }
            }
        }
    }

    val welcomeEvent = authState.welcomeEvent
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
                modifier = Modifier.widthIn(max = 560.dp),
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
                                subtitle =
                                        if (category == CategoryType.OTHER &&
                                                        !customCategory.isNullOrBlank()
                                        )
                                                customCategory
                                        else "Manual entry"
                        )
                        showAddModal = false
                    }
            )
        }
    }

    val detailsTx = selectedTransactionForDetails
    if (detailsTx != null) {
        val isIncome = detailsTx.type == TransactionType.INCOME
        val sdf = remember {
            java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
        }
        val exactTime =
                remember(detailsTx.timestamp) { sdf.format(java.util.Date(detailsTx.timestamp)) }
        val relativeTime = remember(detailsTx.timestamp) { formatRelativeTime(detailsTx.timestamp) }

        androidx.compose.ui.window.Dialog(
                onDismissRequest = { selectedTransactionForDetails = null },
                properties =
                        androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                    modifier =
                            Modifier.widthIn(max = 440.dp)
                                    .fillMaxWidth(0.92f)
                                    .clip(RoundedCornerShape(28.dp))
                                    .border(
                                            1.5.dp,
                                            (if (isIncome) IncomeGreen else ExpenseRed).copy(
                                                    alpha = 0.45f
                                            ),
                                            RoundedCornerShape(28.dp)
                                    ),
                    colors =
                            CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                            ),
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Column(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .verticalScroll(rememberScrollState())
                                        .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                            modifier =
                                    Modifier.size(72.dp)
                                            .clip(CircleShape)
                                            .background(
                                                    if (isIncome) IncomeGreen.copy(alpha = 0.15f)
                                                    else ExpenseRed.copy(alpha = 0.15f)
                                            ),
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
                            text =
                                    "${if (isIncome) "+" else "-"}$currency ${String.format("%,.2f", detailsTx.amount)}",
                            style =
                                    MaterialTheme.typography.headlineLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isIncome) IncomeGreen else ExpenseRed
                                    )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                            text = detailsTx.title,
                            style =
                                    MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                    ),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Column(
                            modifier =
                                    Modifier.fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                    MaterialTheme.colorScheme.surfaceVariant.copy(
                                                            alpha = 0.7f
                                                    )
                                            )
                                            .border(
                                                    1.5.dp,
                                                    MaterialTheme.colorScheme.outline.copy(
                                                            alpha = 0.6f
                                                    ),
                                                    RoundedCornerShape(16.dp)
                                            )
                                            .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                        Icons.Default.Wallet,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                        "Wallet / Account",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                    detailsTx.accountName,
                                    style =
                                            MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold
                                            ),
                                    color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                        Icons.Default.Category,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                        "Category",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            val catLabel =
                                    if (detailsTx.category == CategoryType.OTHER &&
                                                    detailsTx.subtitle.isNotBlank() &&
                                                    detailsTx.subtitle != "Manual entry"
                                    ) {
                                        detailsTx.subtitle
                                    } else {
                                        detailsTx.category.name.lowercase().replaceFirstChar {
                                            it.uppercase()
                                        }
                                    }
                            Text(
                                    catLabel,
                                    style =
                                            MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold
                                            ),
                                    color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                        "Transaction Type",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                    detailsTx.type.name.lowercase().replaceFirstChar {
                                        it.uppercase()
                                    },
                                    style =
                                            MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color =
                                                            if (isIncome) IncomeGreen
                                                            else ExpenseRed
                                            )
                            )
                        }

                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                        Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                        "Created At",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                        relativeTime,
                                        style =
                                                MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.Bold
                                                ),
                                        color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                        exactTime,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (!detailsTx.note.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(
                                                        MaterialTheme.colorScheme.surfaceVariant
                                                                .copy(alpha = 0.7f)
                                                )
                                                .border(
                                                        1.5.dp,
                                                        MaterialTheme.colorScheme.outline.copy(
                                                                alpha = 0.6f
                                                        ),
                                                        RoundedCornerShape(16.dp)
                                                )
                                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                        Icons.AutoMirrored.Filled.Notes,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                        "Note / Memo",
                                        style =
                                                MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.Bold
                                                ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                    detailsTx.note,
                                    style =
                                            MaterialTheme.typography.bodySmall.copy(
                                                    fontStyle =
                                                            androidx.compose.ui.text.font.FontStyle
                                                                    .Italic
                                            ),
                                    color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                            onClick = { selectedTransactionForDetails = null },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors =
                                    ButtonDefaults.buttonColors(
                                            containerColor =
                                                    if (isIncome) IncomeGreen else ExpenseRed,
                                            contentColor = Color.White
                                    )
                    ) { Text("Close", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }

    val menuTx = selectedTransactionForMenu
    if (menuTx != null) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
                onDismissRequest = { selectedTransactionForMenu = null },
                modifier = Modifier.widthIn(max = 560.dp),
                sheetState = sheetState,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 12.dp
        ) {
            Column(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = 24.dp, vertical = 8.dp)
                                    .padding(bottom = 28.dp)
            ) {
                val isIncome = menuTx.type == TransactionType.INCOME
                val amountColor = if (isIncome) IncomeGreen else ExpenseRed

                Text(
                        text = "Transaction Options",
                        style =
                                MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 12.dp)
                )

                // Transaction Summary Card Preview
                Row(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                                MaterialTheme.colorScheme.surfaceVariant.copy(
                                                        alpha = 0.7f
                                                )
                                        )
                                        .border(
                                                1.5.dp,
                                                MaterialTheme.colorScheme.outline.copy(
                                                        alpha = 0.6f
                                                ),
                                                RoundedCornerShape(20.dp)
                                        )
                                        .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                            modifier =
                                    Modifier.size(46.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(amountColor.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                    ) {
                        Icon(
                                imageVector = getCategoryIcon(menuTx.category),
                                contentDescription = menuTx.title,
                                tint = amountColor,
                                modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                                text = menuTx.title,
                                style =
                                        MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = FontWeight.Bold
                                        ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                                text =
                                        "${menuTx.accountName} • ${formatRelativeTime(menuTx.timestamp)}",
                                style =
                                        MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                maxLines = 1
                        )
                    }

                    Text(
                            text =
                                    "${if (isIncome) "+" else "-"}$currency${String.format(java.util.Locale.US, "%.2f", menuTx.amount)}",
                            style =
                                    MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                    ),
                            color = amountColor
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Tile 1: View Details
                TransactionOptionTile(
                        label = "View Details",
                        subtitle = "Inspect full transaction summary & timestamps",
                        icon = Icons.Default.Info,
                        tint = MaterialTheme.colorScheme.primary,
                        onClick = {
                            selectedTransactionForDetails = menuTx
                            selectedTransactionForMenu = null
                        }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Action Tile 2: Edit Transaction
                TransactionOptionTile(
                        label = "Edit Transaction",
                        subtitle = "Modify amount, category, or note",
                        icon = Icons.Default.Edit,
                        tint = MaterialTheme.colorScheme.primary,
                        onClick = {
                            transactionToEdit = menuTx
                            selectedTransactionForMenu = null
                        }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Action Tile 3: Delete Transaction (Destructive)
                TransactionOptionTile(
                        label = "Delete Transaction",
                        subtitle = "Permanently remove and update wallet balance",
                        icon = Icons.Default.Delete,
                        tint = ExpenseRed,
                        isDestructive = true,
                        onClick = {
                            transactionToDelete = menuTx
                            selectedTransactionForMenu = null
                        }
                )
            }
        }
    }

    val deleteTx = transactionToDelete
    if (deleteTx != null) {
        FlowCashConfirmDialog(
                onDismissRequest = { transactionToDelete = null },
                title = "Delete Transaction?",
                message =
                        "Are you sure you want to permanently delete this transaction? This action will adjust the balance of your account.",
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
                                subtitle =
                                        if (category == CategoryType.OTHER &&
                                                        !customCategory.isNullOrBlank()
                                        )
                                                customCategory
                                        else "Manual entry",
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
        onAdd:
                (
                        title: String,
                        amount: Double,
                        type: TransactionType,
                        category: CategoryType,
                        account: String,
                        note: String?,
                        customCategory: String?) -> Unit
) {
    var selectedType by
            rememberSaveable(initialType, transactionToEdit) {
                mutableStateOf(transactionToEdit?.type ?: initialType)
            }
    var title by
            rememberSaveable(transactionToEdit) { mutableStateOf(transactionToEdit?.title ?: "") }
    var amountText by
            rememberSaveable(transactionToEdit) {
                mutableStateOf(transactionToEdit?.amount?.toString() ?: "")
            }
    var selectedCategory by
            rememberSaveable(transactionToEdit) {
                mutableStateOf(transactionToEdit?.category ?: CategoryType.SALARY)
            }
    LaunchedEffect(selectedType) {
        if (!selectedCategory.isApplicableTo(selectedType)) {
            selectedCategory =
                    if (selectedType == TransactionType.INCOME) CategoryType.SALARY
                    else CategoryType.SHOPPING
        }
    }
    var customCategoryText by
            rememberSaveable(transactionToEdit) {
                mutableStateOf(
                        if (transactionToEdit?.category == CategoryType.OTHER)
                                transactionToEdit.subtitle
                        else ""
                )
            }
    var amountError by remember { mutableStateOf<String?>(null) }
    var titleError by remember { mutableStateOf<String?>(null) }

    val defaultAccount =
            remember(initialAccountName, accountsList, transactionToEdit) {
                if (transactionToEdit != null) {
                    transactionToEdit.accountName
                } else {
                    val matchedAccount =
                            if (!initialAccountName.isNullOrBlank()) {
                                accountsList.find { it.name == initialAccountName }
                            } else {
                                null
                            }
                    matchedAccount?.name ?: accountsList.firstOrNull()?.name ?: "Main Wallet"
                }
            }
    var selectedAccount by rememberSaveable(defaultAccount) { mutableStateOf(defaultAccount) }
    var noteText by
            rememberSaveable(transactionToEdit) { mutableStateOf(transactionToEdit?.note ?: "") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val selectedAccountType =
            remember(selectedAccount, accountsList) {
                accountsList.find { it.name == selectedAccount }?.accountType ?: "CASH_WALLET"
            }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .padding(bottom = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header Title Banner
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (transactionToEdit != null) Icons.Default.EditNote else Icons.Default.AddCard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = if (transactionToEdit != null) "Edit Transaction" else "New Transaction",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = if (transactionToEdit != null) "Update cashflow details and account balances" else "Record income or expense to track cashflow",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))        // 1. Transaction Type Segmented Toggle (Income & Expense) with Bounded Height & Smooth Spring Liquid Glide
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
                .padding(4.dp)
        ) {
            val totalW = maxWidth
            val tabWidth = totalW / 2
            val isIncomeSelected = selectedType == TransactionType.INCOME
            val targetIndex = if (isIncomeSelected) 0f else 1f

            val animatedIndex by animateFloatAsState(
                targetValue = targetIndex,
                animationSpec = spring(
                    dampingRatio = 0.65f, // organic liquid spring glide
                    stiffness = Spring.StiffnessLow
                ),
                label = "TabSwitchIndex"
            )

            val animatedColor by animateColorAsState(
                targetValue = if (isIncomeSelected) IncomeGreen else ExpenseRed,
                animationSpec = tween(durationMillis = 220),
                label = "TabSwitchColor"
            )

            val indicatorOffset = tabWidth * animatedIndex

            // Animated sliding chip pill (darker rich tinted fill + 1.5.dp solid border)
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(tabWidth)
                    .height(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(animatedColor.copy(alpha = 0.38f))
                    .border(1.5.dp, animatedColor, RoundedCornerShape(14.dp))
            )

            // Text-only interactive Tab Options
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Income Tab Option
                Box(
                    modifier = Modifier
                        .width(tabWidth)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { selectedType = TransactionType.INCOME },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Income",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isIncomeSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    )
                }

                // Expense Tab Option
                Box(
                    modifier = Modifier
                        .width(tabWidth)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { selectedType = TransactionType.EXPENSE },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Expense",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (!isIncomeSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.AttachMoney,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            isError = amountError != null,
            supportingText = amountError?.let { { Text(it) } },
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Description / Merchant Input
        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
                titleError = null
            },
            label = { Text("Description / Merchant") },
            placeholder = { Text("e.g. Starbucks, Salary, Amazon") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            singleLine = true,
            isError = titleError != null,
            supportingText = titleError?.let { { Text(it) } },
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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

        val filteredCategories =
                remember(selectedType) {
                    CategoryType.entries.filter { it.isApplicableTo(selectedType) }
                }

        val categoryLazyState = androidx.compose.foundation.lazy.rememberLazyListState()
        LaunchedEffect(selectedCategory, filteredCategories) {
            val index = filteredCategories.indexOf(selectedCategory)
            if (index >= 0) {
                categoryLazyState.animateScrollToItem(index)
            }
        }

        LazyRow(
                state = categoryLazyState,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(filteredCategories) { category ->
                val isSelected = selectedCategory == category
                Box(
                        modifier =
                                Modifier.clip(RoundedCornerShape(12.dp))
                                        .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
                                                else
                                                        MaterialTheme.colorScheme.surfaceVariant
                                                                .copy(alpha = 0.35f)
                                        )
                                        .border(
                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                                shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { selectedCategory = category }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                                imageVector = getCategoryIcon(category),
                                contentDescription = category.name,
                                tint =
                                        if (isSelected) MaterialTheme.colorScheme.onSurface
                                        else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                                text =
                                        category.name.lowercase().replaceFirstChar {
                                            it.uppercase()
                                        },
                                style =
                                        MaterialTheme.typography.bodySmall.copy(
                                                fontWeight =
                                                        if (isSelected) FontWeight.Bold
                                                        else FontWeight.Normal,
                                                color =
                                                        if (isSelected) MaterialTheme.colorScheme.onSurface
                                                        else MaterialTheme.colorScheme.onSurface
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
                    shape = RoundedCornerShape(18.dp),
                    colors =
                            OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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
                modifier =
                        Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                )
                                .border(
                                        1.5.dp,
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        RoundedCornerShape(18.dp)
                                )
                                .clickable { dropdownExpanded = true }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                            modifier =
                                    Modifier.size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                    MaterialTheme.colorScheme.primary.copy(
                                                            alpha = 0.12f
                                                    )
                                            ),
                            contentAlignment = Alignment.Center
                    ) {
                        Icon(
                                imageVector = getLocalAccountTypeIcon(selectedAccountType),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                                text = selectedAccount,
                                style =
                                        MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                        )
                        )
                        Text(
                                text = selectedAccountType.replace("_", " "),
                                style =
                                        MaterialTheme.typography.labelSmall.copy(
                                                color =
                                                        MaterialTheme.colorScheme.onSurfaceVariant
                                                                .copy(alpha = 0.75f)
                                        )
                        )
                    }
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
                shape = RoundedCornerShape(24.dp),
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 12.dp,
                modifier = Modifier
                    .widthIn(min = 260.dp, max = 380.dp)
                    .border(
                        1.5.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        RoundedCornerShape(24.dp)
                    )
            ) {
                accountsList.forEach { account ->
                    val isAccSelected = account.name == selectedAccount
                    DropdownMenuItem(
                            text = {
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                            text = account.name,
                                            style =
                                                    MaterialTheme.typography.bodyMedium.copy(
                                                            fontWeight =
                                                                    if (isAccSelected)
                                                                            FontWeight.Bold
                                                                    else FontWeight.Medium,
                                                            color =
                                                                    if (isAccSelected)
                                                                            MaterialTheme
                                                                                    .colorScheme
                                                                                    .primary
                                                                    else
                                                                            MaterialTheme
                                                                                    .colorScheme
                                                                                    .onSurface
                                                    )
                                    )
                                    if (isAccSelected) {
                                        Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            onClick = {
                                selectedAccount = account.name
                                dropdownExpanded = false
                            },
                            leadingIcon = {
                                Box(
                                        modifier =
                                                Modifier.size(32.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(
                                                                if (isAccSelected)
                                                                        MaterialTheme.colorScheme
                                                                                .primary.copy(
                                                                                alpha = 0.15f
                                                                        )
                                                                else
                                                                        MaterialTheme.colorScheme
                                                                                .surfaceVariant
                                                                                .copy(alpha = 0.5f)
                                                        ),
                                        contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                            imageVector =
                                                    getLocalAccountTypeIcon(account.accountType),
                                            contentDescription = null,
                                            tint =
                                                    if (isAccSelected)
                                                            MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                    )
                                }
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
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Notes,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                colors =
                        OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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
                                if (selectedCategory == CategoryType.OTHER)
                                        customCategoryText.ifBlank { null }
                                else null
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors =
                        ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                        )
        ) {
            Text(
                    text = if (transactionToEdit != null) "Update Transaction" else "Save Transaction",
                    style =
                            MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                            )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

fun getLocalAccountTypeIcon(type: String): ImageVector {
    return when (type) {
        "CARD" -> Icons.Default.CreditCard
        "CASH_WALLET" -> Icons.Default.Wallet
        "BANK_ACCOUNT" -> Icons.Default.AccountBalance
        "INVESTMENT" -> Icons.Default.PieChart
        "FREELANCE_INCOME" -> Icons.Default.Work
        "SAVINGS" -> Icons.Default.Savings
        else -> Icons.Default.Widgets
    }
}

@Composable
private fun TransactionOptionTile(
        label: String,
        subtitle: String,
        icon: ImageVector,
        tint: Color,
        isDestructive: Boolean = false,
        onClick: () -> Unit
) {
    val containerColor =
            if (isDestructive) {
                ExpenseRed.copy(alpha = 0.14f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            }
    val borderColor =
            if (isDestructive) {
                ExpenseRed.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
            }

    Row(
            modifier =
                    Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(containerColor)
                            .border(1.5.dp, borderColor, RoundedCornerShape(18.dp))
                            .clickable(onClick = onClick)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
                modifier =
                        Modifier.size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(tint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
        ) {
            Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                    text = label,
                    style =
                            MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color =
                                            if (isDestructive) ExpenseRed
                                            else MaterialTheme.colorScheme.onSurface
                            )
            )
            Text(
                    text = subtitle,
                    style =
                            MaterialTheme.typography.bodySmall.copy(
                                    color =
                                            if (isDestructive) ExpenseRed.copy(alpha = 0.75f)
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                            )
            )
        }

        Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint =
                        if (isDestructive) ExpenseRed.copy(alpha = 0.6f)
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
        )
    }
}
