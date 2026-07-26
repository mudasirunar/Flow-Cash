package com.mudasir.flowcash.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Edit
import com.mudasir.flowcash.ui.components.FlowCashAlertDialog
import com.mudasir.flowcash.ui.components.FlowCashConfirmDialog
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalPlay
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mudasir.flowcash.data.local.entity.AccountEntity
import com.mudasir.flowcash.data.model.CategoryType
import com.mudasir.flowcash.data.model.TransactionItem
import com.mudasir.flowcash.data.model.TransactionType
import com.mudasir.flowcash.ui.components.UserProfileAvatar
import com.mudasir.flowcash.ui.theme.ExpenseRed
import com.mudasir.flowcash.ui.theme.IncomeGreen
import com.mudasir.flowcash.ui.theme.PrimaryIndigo
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.OutlinedButton
import com.mudasir.flowcash.ui.viewmodel.SettingsViewModel
import com.mudasir.flowcash.util.BiometricAuthHelper
import com.mudasir.flowcash.ui.viewmodel.DashboardViewModel
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    dashboardViewModel: DashboardViewModel,
    userName: String = "User",
    userEmail: String = "",
    profilePicUrl: String? = null,
    avatarColorHex: String? = null,
    currencySymbol: String = "$",
    onAddTransactionClick: (TransactionType?) -> Unit,
    onAddAccountClick: () -> Unit = {},
    onEditAccountClick: (AccountEntity) -> Unit = {},
    onTransactionClick: (TransactionItem) -> Unit = {},
    onTransactionLongClick: (TransactionItem) -> Unit = {},
    settingsViewModel: SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val biometricsEnabled by settingsViewModel.biometricsEnabled.collectAsState()

    var showBiometricSetupDialog by remember { mutableStateOf(false) }
    var biometricAlertTitle by remember { mutableStateOf<String?>(null) }
    var biometricAlertMessage by remember { mutableStateOf<String?>(null) }

    val allTransactions by dashboardViewModel.transactions.collectAsState()
    val transactions by dashboardViewModel.filteredTransactions.collectAsState()
    val selectedFilter by dashboardViewModel.selectedFilter.collectAsState()

    var timeTicker by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(60_000)
            timeTicker++
        }
    }
    val selectedAccount by dashboardViewModel.selectedAccount.collectAsState()
    val accountsState by dashboardViewModel.accounts.collectAsState()
    val accounts = remember(accountsState) { accountsState ?: emptyList() }
    val isLoading by dashboardViewModel.isLoading.collectAsState()

    val isDataVisible by dashboardViewModel.isDataVisible.collectAsState()

    val handleToggleVisibility = {
        if (isDataVisible) {
            // Hiding balances
            if (biometricsEnabled) {
                dashboardViewModel.setDataVisible(false)
            } else {
                showBiometricSetupDialog = true
            }
        } else {
            // Unhiding balances
            if (biometricsEnabled) {
                BiometricAuthHelper.promptBiometricAuth(
                    context = context,
                    title = "Unhide Financial Balances",
                    subtitle = "Scan fingerprint or Face ID to reveal your balances",
                    onSuccess = { dashboardViewModel.setDataVisible(true) },
                    onError = { err ->
                        biometricAlertTitle = "Biometric Verification Failed"
                        biometricAlertMessage = err
                    }
                )
            } else {
                showBiometricSetupDialog = true
            }
        }
    }
    var showBottomSheetSelector by rememberSaveable { mutableStateOf(false) }
    var deletingAccountId by rememberSaveable { mutableStateOf<String?>(null) }
    val accountToDelete = remember(deletingAccountId, accounts) {
        accounts.find { it.id == deletingAccountId }
    }

    val activeAccountTransactions = remember(allTransactions, selectedAccount) {
        allTransactions.filter {
            selectedAccount == null || it.accountName.equals(selectedAccount?.name, ignoreCase = true)
        }
    }
    val totalIncome = remember(activeAccountTransactions) {
        activeAccountTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    }
    val totalExpense = remember(activeAccountTransactions) {
        activeAccountTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }
    val netBalance = remember(totalIncome, totalExpense) { totalIncome - totalExpense }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLoading) {
        DashboardSkeleton(isLandscape = isLandscape)
    } else if (isLandscape) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Left Column: Header + Card Carousel
            Column(
                modifier = Modifier
                    .weight(1.25f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserProfileAvatar(
                            name = userName,
                            email = userEmail,
                            profilePicUrl = profilePicUrl,
                            avatarColorHex = avatarColorHex,
                            size = 42.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            AnimatedThunderTagline(textStyle = MaterialTheme.typography.bodySmall)
                            Text(userName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground))
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(18.dp)
                                .align(Alignment.Center)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Card / Carousel
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    if (accounts.isEmpty()) {
                        EmptyAccountsCard(onAddAccountClick = onAddAccountClick)
                    } else {
                        val pagerList = remember(accounts) { (listOf(null) + accounts + listOf("ADD_NEW")) as List<Any?> }
                        val listSize = pagerList.size
                        val initialPage = remember(listSize) {
                            val currentSelected = selectedAccount
                            val targetIndex = if (currentSelected == null) 0 else (accounts.indexOfFirst { it.id == currentSelected.id }.coerceAtLeast(0) + 1)
                            5000 * listSize + targetIndex
                        }
                        val pagerState = rememberPagerState(
                            initialPage = initialPage,
                            pageCount = { Int.MAX_VALUE }
                        )

                        // Two-way sync: Pager -> ViewModel selection
                        LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress, listSize, accounts) {
                            if (listSize > 0 && !pagerState.isScrollInProgress) {
                                val currentMappedIndex = pagerState.currentPage % listSize
                                val currentSelectedAccount = pagerList[currentMappedIndex]
                                if (currentSelectedAccount is AccountEntity?) {
                                    if (selectedAccount != currentSelectedAccount) {
                                        dashboardViewModel.setSelectedAccount(currentSelectedAccount)
                                    }
                                }
                            }
                        }

                        // Two-way sync: ViewModel selection -> Pager
                        var isFirstPageSync by remember { mutableStateOf(true) }
                        LaunchedEffect(selectedAccount, listSize, accounts) {
                            if (listSize > 0) {
                                val currentSelected = selectedAccount
                                val targetIndex = if (currentSelected == null) 0 else accounts.indexOfFirst { it.id == currentSelected.id } + 1
                                if (targetIndex >= 0) {
                                    val currentMappedIndex = pagerState.currentPage % listSize
                                    if (currentMappedIndex != targetIndex) {
                                        val diff1 = targetIndex - currentMappedIndex
                                        val diff2 = diff1 + listSize
                                        val diff3 = diff1 - listSize
                                        val offset = listOf(diff1, diff2, diff3).minByOrNull { kotlin.math.abs(it) } ?: diff1
                                        if (isFirstPageSync) {
                                            pagerState.scrollToPage(pagerState.currentPage + offset)
                                        } else {
                                            pagerState.animateScrollToPage(pagerState.currentPage + offset)
                                        }
                                    }
                                    isFirstPageSync = false
                                }
                            }
                        }

                        HorizontalPager(
                            state = pagerState,
                            contentPadding = PaddingValues(horizontal = 48.dp),
                            pageSpacing = 16.dp,
                            beyondViewportPageCount = 1,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(205.dp)
                        ) { page ->
                            val mappedIndex = page % listSize
                            val item = pagerList[mappedIndex]
                            val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer {
                                        val pageOffsetAbs = pageOffset.absoluteValue
                                        val scale = 0.86f + (1f - 0.86f) * (1f - pageOffsetAbs.coerceIn(0f, 1f))
                                        val alphaVal = 0.6f + (1f - 0.6f) * (1f - pageOffsetAbs.coerceIn(0f, 1f))

                                        scaleX = scale
                                        scaleY = scale
                                        this.alpha = alphaVal
                                        rotationY = -22f * pageOffset.coerceIn(-1f, 1f)
                                        cameraDistance = 12f * density
                                    }
                                    .zIndex(1f - pageOffset.absoluteValue)
                            ) {
                                when (item) {
                                    null -> {
                                        OverviewCard(
                                            netBalance = netBalance,
                                            totalIncome = totalIncome,
                                            totalExpense = totalExpense,
                                            currencySymbol = currencySymbol,
                                            isDataVisible = isDataVisible,
                                            onToggleVisibility = handleToggleVisibility,
                                            onClick = { showBottomSheetSelector = true }
                                        )
                                    }
                                    "ADD_NEW" -> {
                                        AddCardPlaceholder(onClick = onAddAccountClick)
                                    }
                                    is AccountEntity -> {
                                        val account = item
                                        val accountTransactions = remember(allTransactions, account) {
                                            allTransactions.filter {
                                                it.accountName.equals(account.name, ignoreCase = true)
                                            }
                                        }
                                        val accIncome = remember(accountTransactions) {
                                            accountTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                                        }
                                        val accExpense = remember(accountTransactions) {
                                            accountTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                                        }
                                        val accBalance = accIncome - accExpense

                                        SelectedAccountCard(
                                            account = account,
                                            netBalance = accBalance,
                                            totalIncome = accIncome,
                                            totalExpense = accExpense,
                                            currencySymbol = currencySymbol,
                                            isDataVisible = isDataVisible,
                                            onToggleVisibility = handleToggleVisibility,
                                            onClick = { showBottomSheetSelector = true },
                                            onEditClick = { onEditAccountClick(account) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Right Column: Filter Chips & Transactions
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedFilter == null,
                                onClick = { dashboardViewModel.setFilter(null) },
                                label = { Text("All", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = MaterialTheme.colorScheme.onPrimary)
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedFilter == TransactionType.INCOME,
                                onClick = { dashboardViewModel.setFilter(TransactionType.INCOME) },
                                label = { Text("Income", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = IncomeGreen, selectedLabelColor = Color.White)
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedFilter == TransactionType.EXPENSE,
                                onClick = { dashboardViewModel.setFilter(TransactionType.EXPENSE) },
                                label = { Text("Expenses", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ExpenseRed, selectedLabelColor = Color.White)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .width(44.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                            .clickable { onAddTransactionClick(selectedFilter) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Transaction",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Recent Activity Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recent Activity", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground))
                    Text("${transactions.size} items", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (transactions.isEmpty()) {
                    val emptyTitle = if (selectedAccount == null) "No Activity Registered Yet" else "No Activity for ${selectedAccount?.name}"
                    val emptyDesc = if (selectedAccount == null) {
                        "You haven't recorded any transactions across any accounts. Tap below to start tracking!"
                    } else {
                        "There are no transactions recorded for this wallet. Tap below to add one!"
                    }
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 16.dp)) {
                            Text(emptyTitle, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(emptyDesc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(onClick = { onAddTransactionClick(selectedFilter) }, shape = RoundedCornerShape(10.dp)) { Text("Add Transaction", fontSize = 12.sp) }
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(items = transactions, key = { it.id }) { tx ->
                            TransactionRowItem(
                                transaction = tx,
                                currencySymbol = currencySymbol,
                                timeTicker = timeTicker,
                                onClick = { onTransactionClick(tx) },
                                onLongClick = { onTransactionLongClick(tx) }
                            )
                        }
                    }
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserProfileAvatar(
                            name = userName,
                            email = userEmail,
                            profilePicUrl = profilePicUrl,
                            avatarColorHex = avatarColorHex,
                            size = 46.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            AnimatedThunderTagline(textStyle = MaterialTheme.typography.bodyMedium)
                            Text(userName, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground))
                        }
                    }

                    IconButton(
                        onClick = { },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                // === Adaptive Dashboard Card Carousel ===
                if (accounts.isEmpty()) {
                    EmptyAccountsCard(onAddAccountClick = onAddAccountClick)
                } else {
                    val pagerList = remember(accounts) { (listOf(null) + accounts + listOf("ADD_NEW")) as List<Any?> }
                    val listSize = pagerList.size
                    val initialPage = remember(listSize) {
                        val currentSelected = selectedAccount
                        val targetIndex = if (currentSelected == null) 0 else (accounts.indexOfFirst { it.id == currentSelected.id }.coerceAtLeast(0) + 1)
                        5000 * listSize + targetIndex
                    }
                    val pagerState = rememberPagerState(
                        initialPage = initialPage,
                        pageCount = { Int.MAX_VALUE }
                    )

                    // Two-way sync: Pager -> ViewModel selection
                    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress, listSize, accounts) {
                        if (listSize > 0 && !pagerState.isScrollInProgress) {
                            val currentMappedIndex = pagerState.currentPage % listSize
                            val currentSelectedAccount = pagerList[currentMappedIndex]
                            if (currentSelectedAccount is AccountEntity?) {
                                if (selectedAccount != currentSelectedAccount) {
                                    dashboardViewModel.setSelectedAccount(currentSelectedAccount)
                                }
                            }
                        }
                    }

                    // Two-way sync: ViewModel selection -> Pager
                    var isFirstPageSync by remember { mutableStateOf(true) }
                    LaunchedEffect(selectedAccount, listSize, accounts) {
                        if (listSize > 0) {
                            val currentSelected = selectedAccount
                            val targetIndex = if (currentSelected == null) 0 else accounts.indexOfFirst { it.id == currentSelected.id } + 1
                            if (targetIndex >= 0) {
                                val currentMappedIndex = pagerState.currentPage % listSize
                                if (currentMappedIndex != targetIndex) {
                                    val diff1 = targetIndex - currentMappedIndex
                                    val diff2 = diff1 + listSize
                                    val diff3 = diff1 - listSize
                                    val offset = listOf(diff1, diff2, diff3).minByOrNull { kotlin.math.abs(it) } ?: diff1
                                    if (isFirstPageSync) {
                                        pagerState.scrollToPage(pagerState.currentPage + offset)
                                    } else {
                                        pagerState.animateScrollToPage(pagerState.currentPage + offset)
                                    }
                                }
                                isFirstPageSync = false
                            }
                        }
                    }

                    HorizontalPager(
                        state = pagerState,
                        contentPadding = PaddingValues(horizontal = 48.dp),
                        pageSpacing = 16.dp,
                        beyondViewportPageCount = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(215.dp)
                    ) { page ->
                        val mappedIndex = page % listSize
                        val item = pagerList[mappedIndex]
                        val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    val pageOffsetAbs = pageOffset.absoluteValue
                                    val scale = 0.86f + (1f - 0.86f) * (1f - pageOffsetAbs.coerceIn(0f, 1f))
                                    val alphaVal = 0.6f + (1f - 0.6f) * (1f - pageOffsetAbs.coerceIn(0f, 1f))

                                    scaleX = scale
                                    scaleY = scale
                                    this.alpha = alphaVal
                                    rotationY = -22f * pageOffset.coerceIn(-1f, 1f)
                                    cameraDistance = 12f * density
                                }
                                .zIndex(1f - pageOffset.absoluteValue)
                        ) {
                            when (item) {
                                null -> {
                                    OverviewCard(
                                        netBalance = netBalance,
                                        totalIncome = totalIncome,
                                        totalExpense = totalExpense,
                                        currencySymbol = currencySymbol,
                                        isDataVisible = isDataVisible,
                                        onToggleVisibility = handleToggleVisibility,
                                        onClick = { showBottomSheetSelector = true }
                                    )
                                }
                                "ADD_NEW" -> {
                                    AddCardPlaceholder(onClick = onAddAccountClick)
                                }
                                is AccountEntity -> {
                                    val account = item
                                    val accountTransactions = remember(allTransactions, account) {
                                        allTransactions.filter {
                                            it.accountName.equals(account.name, ignoreCase = true)
                                        }
                                    }
                                    val accIncome = remember(accountTransactions) {
                                        accountTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                                    }
                                    val accExpense = remember(accountTransactions) {
                                        accountTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                                    }
                                    val accBalance = accIncome - accExpense

                                    SelectedAccountCard(
                                        account = account,
                                        netBalance = accBalance,
                                        totalIncome = accIncome,
                                        totalExpense = accExpense,
                                        currencySymbol = currencySymbol,
                                        isDataVisible = isDataVisible,
                                        onToggleVisibility = handleToggleVisibility,
                                        onClick = { showBottomSheetSelector = true },
                                        onEditClick = { onEditAccountClick(account) }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedFilter == null,
                                onClick = { dashboardViewModel.setFilter(null) },
                                label = { Text("All") },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = MaterialTheme.colorScheme.onPrimary)
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedFilter == TransactionType.INCOME,
                                onClick = { dashboardViewModel.setFilter(TransactionType.INCOME) },
                                label = { Text("Income") },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = IncomeGreen, selectedLabelColor = Color.White)
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedFilter == TransactionType.EXPENSE,
                                onClick = { dashboardViewModel.setFilter(TransactionType.EXPENSE) },
                                label = { Text("Expenses") },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ExpenseRed, selectedLabelColor = Color.White)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .width(44.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                            .clickable { onAddTransactionClick(selectedFilter) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Transaction",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                // Recent Activity Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recent Activity", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground))
                    Text("${transactions.size} items", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            if (transactions.isEmpty()) {
                item {
                    val emptyTitle = if (selectedAccount == null) "No Activity Registered Yet" else "No Activity for ${selectedAccount?.name}"
                    val emptyDesc = if (selectedAccount == null) {
                        "You haven't recorded any transactions across any of your accounts yet. Tap below to start tracking your cash flow!"
                    } else {
                        "There are no transactions recorded for this wallet. Tap below to add a transaction to this specific account!"
                    }
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 24.dp)) {
                            Text(emptyTitle, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(emptyDesc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { onAddTransactionClick(selectedFilter) }, shape = RoundedCornerShape(12.dp)) { Text("Record First Transaction") }
                        }
                    }
                }
            } else {
                items(items = transactions, key = { it.id }) { tx ->
                    TransactionRowItem(
                        transaction = tx,
                        currencySymbol = currencySymbol,
                        timeTicker = timeTicker,
                        onClick = { onTransactionClick(tx) },
                        onLongClick = { onTransactionLongClick(tx) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }

    // Bottom Sheet Wallet Selector
    if (showBottomSheetSelector) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showBottomSheetSelector = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
                Text("Select Active Wallet", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Choose a wallet to filter dashboard metrics.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(20.dp))

                AccountSelectorRow(
                    label = "All Accounts",
                    icon = Icons.Default.Wallet,
                    isSelected = selectedAccount == null,
                    isAllAccounts = true,
                    onClick = {
                        dashboardViewModel.setSelectedAccount(null)
                        showBottomSheetSelector = false
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))

                accounts.forEach { account ->
                    val accountIcon = if (account.accountType == "CARD") {
                        Icons.Default.CreditCard
                    } else {
                        getAccountTypeIcon(account.accountType)
                    }

                    AccountSelectorRow(
                        label = account.name,
                        icon = accountIcon,
                        isSelected = account.id == selectedAccount?.id,
                        onClick = {
                            dashboardViewModel.setSelectedAccount(account)
                            showBottomSheetSelector = false
                        },
                        onDelete = {
                            showBottomSheetSelector = false
                            deletingAccountId = account.id // Triggers Confirmation Dialog
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .clickable {
                            showBottomSheetSelector = false
                            onAddAccountClick()
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add New Card / Wallet", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                }
            }
        }
    }

// === Delete Confirmation Dialog ===
    if (accountToDelete != null) {
        FlowCashConfirmDialog(
            onDismissRequest = { deletingAccountId = null },
            title = "Delete Account?",
            message = "Are you sure you want to delete \"${accountToDelete?.name}\"? All related income and expense transactions will be permanently deleted from history.",
            icon = Icons.Default.DeleteForever,
            isDestructive = true,
            confirmButtonText = "Delete",
            onConfirm = {
                accountToDelete?.let { acc ->
                    dashboardViewModel.deleteAccountAndTransactions(acc)
                }
                deletingAccountId = null
            }
        )
    }

    if (showBiometricSetupDialog) {
        val targetVisibility = !isDataVisible
        val actionText = if (targetVisibility) "unhide" else "hide"
        FlowCashConfirmDialog(
            onDismissRequest = { showBiometricSetupDialog = false },
            title = "Biometric Security Required",
            message = "To $actionText sensitive financial numbers and balance details, please configure Biometric Protection. This activates fingerprint protection for your account.",
            icon = Icons.Default.Fingerprint,
            confirmButtonText = "Scan & Protect",
            onConfirm = {
                showBiometricSetupDialog = false
                BiometricAuthHelper.promptBiometricAuth(
                    context = context,
                    title = "Configure Biometric Security",
                    subtitle = "Scan fingerprint to activate security and $actionText balances",
                    onSuccess = {
                        settingsViewModel.setBiometricsEnabled(true)
                        dashboardViewModel.setDataVisible(targetVisibility)
                        biometricAlertTitle = "Biometric Protection Activated"
                        biometricAlertMessage = "Your fingerprint is configured and Biometric Protection is turned ON in Settings."
                    },
                    onError = { err ->
                        biometricAlertTitle = "Biometric Setup Failed"
                        biometricAlertMessage = err
                    }
                )
            }
        )
    }

    if (biometricAlertMessage != null) {
        FlowCashAlertDialog(
            onDismissRequest = { biometricAlertMessage = null },
            title = biometricAlertTitle ?: "Biometric Security",
            message = biometricAlertMessage ?: "",
            icon = Icons.Default.Security
        )
    }
}

// --- Updated AccountSelectorRow ---
@Composable
private fun AccountSelectorRow(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    isAllAccounts: Boolean = false,
    onClick: () -> Unit,
    onDelete: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            // Show Delete action for custom accounts
            if (!isAllAccounts) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Account",
                        tint = ExpenseRed.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyAccountsCard(
    onAddAccountClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(240.dp, 100.dp)
                .blur(50.dp)
                .background(Brush.radialGradient(colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), Color.Transparent)))
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .widthIn(max = 360.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.85f)
                        )
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(24.dp))
                .clickable(onClick = onAddAccountClick)
                .padding(22.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Get Started with FlowCash",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Create your first card or wallet (Cash, Bank Card, Savings) to start logging incomes and expenses.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.8f),
                        lineHeight = 16.sp
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Create Your First Wallet",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

// === Overview Card (No account selected) ===
@Composable
private fun OverviewCard(
    netBalance: Double,
    totalIncome: Double,
    totalExpense: Double,
    currencySymbol: String,
    isDataVisible: Boolean,
    onToggleVisibility: () -> Unit,
    onClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(240.dp, 100.dp)
                .blur(50.dp)
                .background(Brush.radialGradient(colors = listOf(PrimaryIndigo.copy(alpha = 0.3f), Color.Transparent)))
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .widthIn(max = 360.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(PrimaryIndigo, PrimaryIndigo.copy(alpha = 0.8f))))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(24.dp))
                .clickable(onClick = onClick)
                .padding(22.dp)
        ) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(Icons.Default.Wallet, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("All Accounts", style = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.SemiBold))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }

                    // Card Eye Button Toggle
                    CardEyeButton(isDataVisible = isDataVisible, onToggle = onToggleVisibility)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // REPLACE the balance Crossfade block with this single fixed Text view:
                val displayBalance = remember(isDataVisible, netBalance, currencySymbol) {
                    if (isDataVisible) "$currencySymbol ${String.format("%,.2f", netBalance)}"
                    else "$currencySymbol ••••••"
                }

                Text(
                    text = displayBalance,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDataVisible) Color.White else Color.White.copy(alpha = 0.6f),
                        fontSize = 28.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                IncomeExpenseMetricsRow(totalIncome, totalExpense, currencySymbol, isDataVisible)
            }
        }
    }
}

// === Selected Account Card ===
@Composable
private fun SelectedAccountCard(
    account: AccountEntity,
    netBalance: Double,
    totalIncome: Double,
    totalExpense: Double,
    currencySymbol: String,
    isDataVisible: Boolean,
    onToggleVisibility: () -> Unit,
    onClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val startColor = parseHexColor(account.cardColorStart)
    val endColor = parseHexColor(account.cardColorEnd)
    val isCard = account.accountType == "CARD"

    val formattedAccountType = account.accountType
        .replace("_", " ")
        .lowercase()
        .split(" ")
        .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }

    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier.align(Alignment.Center).size(240.dp, 100.dp).blur(50.dp)
                .background(Brush.radialGradient(colors = listOf(endColor.copy(alpha = 0.3f), Color.Transparent)))
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .widthIn(max = 360.dp)
                .fillMaxWidth()
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = endColor,
                    spotColor = endColor
                )
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(startColor, endColor)))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(24.dp))
                .clickable { onEditClick() }
                .padding(18.dp)
        ) {
            Column {
                // Top Row: Account Name Pill (left) + Card Eye Toggle & Network Logo (right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .clickable(onClick = onClick)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        val icon = if (isCard) Icons.Default.CreditCard else getAccountTypeIcon(account.accountType)
                        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(account.name, style = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.SemiBold))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CardEyeButton(isDataVisible = isDataVisible, onToggle = onToggleVisibility)
                        Spacer(modifier = Modifier.width(8.dp))

                        if (isCard) {
                            CardNetworkLogo(network = account.network)
                        } else {
                            Text(
                                text = formattedAccountType,
                                color = Color.White.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Middle Row: Balance Display (left) + EMV Gold Chip & Contactless (right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val displayAccountBalance = remember(isDataVisible, netBalance, currencySymbol) {
                        if (isDataVisible) "$currencySymbol ${String.format("%,.2f", netBalance)}"
                        else "$currencySymbol ••••••"
                    }

                    Text(
                        text = displayAccountBalance,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDataVisible) Color.White else Color.White.copy(alpha = 0.6f),
                            fontSize = 25.sp
                        )
                    )

                    if (isCard) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp, 22.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Brush.linearGradient(listOf(Color(0xFFFCD34D), Color(0xFFF59E0B))))
                                    .border(0.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            ) {
                                Column(modifier = Modifier.fillMaxSize().padding(2.dp), verticalArrangement = Arrangement.SpaceEvenly) {
                                    repeat(3) {
                                        Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFD97706).copy(alpha = 0.4f)))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.Contactless, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Card Number + Expiry + Holder Name in a sleek compact row
                if (isCard) {
                    Crossfade(targetState = isDataVisible, animationSpec = tween(250), label = "CardDetailsVisibility") { visible ->
                        val rawNumber = account.cardNumber.ifBlank { "0000000000000000" }
                        val numberStr = if (visible) rawNumber.padEnd(16, '0').chunked(4).joinToString(" ") else "•••• •••• •••• ${rawNumber.takeLast(4)}"
                        val expiryStr = if (visible) account.expiryDate.ifBlank { "MM/YY" } else "••/••"

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$numberStr   •   $expiryStr",
                                color = Color.White.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                fontSize = 11.sp
                            )
                            if (account.holderName.isNotBlank()) {
                                Text(
                                    text = account.holderName.uppercase(),
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                } else if (account.holderName.isNotBlank()) {
                    Crossfade(targetState = isDataVisible, animationSpec = tween(250), label = "HolderNameVisibility") { visible ->
                        if (visible) {
                            Text(account.holderName.uppercase(), color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
                        } else {
                            Text("•••••••", color = Color.White.copy(alpha = 0.25f), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Income/Expense metrics row cleanly visible at bottom of every card!
                IncomeExpenseMetricsRow(totalIncome, totalExpense, currencySymbol, isDataVisible)
            }
        }
    }
}

// Glassmorphism Card Eye Button Toggle
@Composable
private fun CardEyeButton(
    isDataVisible: Boolean,
    onToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.15f))
            .clickable(onClick = onToggle),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isDataVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
            contentDescription = "Toggle Visibility",
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
    }
}

fun getAccountTypeIcon(type: String): ImageVector {
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

@Composable
private fun IncomeExpenseMetricsRow(totalIncome: Double, totalExpense: Double, currencySymbol: String, isVisible: Boolean) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(28.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.08f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.ArrowUpward, contentDescription = "Income", tint = Color.Green, modifier = Modifier.size(14.dp))
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text("Income", style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp))
                val displayIncome = remember(isVisible, totalIncome, currencySymbol) {
                    if (isVisible) "$currencySymbol ${String.format("%,.2f", totalIncome)}"
                    else "$currencySymbol •••"
                }

                Text(
                    text = displayIncome,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = if (isVisible) Color.White else Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(28.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.08f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.ArrowDownward, contentDescription = "Expense", tint = Color(0xFFFF8A80), modifier = Modifier.size(14.dp))
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text("Expense", style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp))
                // In IncomeExpenseMetricsRow: Replace the Expense Crossfade block with this:
                val displayExpense = remember(isVisible, totalExpense, currencySymbol) {
                    if (isVisible) "$currencySymbol ${String.format("%,.2f", totalExpense)}"
                    else "$currencySymbol •••"
                }

                Text(
                    text = displayExpense,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = if (isVisible) Color.White else Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionRowItem(
    transaction: TransactionItem,
    currencySymbol: String,
    timeTicker: Int = 0,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val isIncome = transaction.type == TransactionType.INCOME
    val relativeTime = remember(transaction.timestamp, timeTicker) {
        formatRelativeTime(transaction.timestamp)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(44.dp).clip(CircleShape).background(if (isIncome) IncomeGreen.copy(alpha = 0.15f) else ExpenseRed.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                    Icon(getCategoryIcon(transaction.category), contentDescription = transaction.category.name, tint = if (isIncome) IncomeGreen else ExpenseRed, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    val categoryText = if (transaction.category == CategoryType.OTHER && transaction.subtitle.isNotBlank() && transaction.subtitle != "Manual entry") {
                        transaction.subtitle
                    } else {
                        transaction.category.name.lowercase().replaceFirstChar { it.uppercase() }
                    }
                    Text(transaction.title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface))
                    Text("${transaction.accountName} • $categoryText • $relativeTime", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(
                text = "${if (isIncome) "+" else "-"}$currencySymbol ${String.format("%,.2f", transaction.amount)}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = if (isIncome) IncomeGreen else ExpenseRed)
            )
        }
    }
}

fun getCategoryIcon(category: CategoryType): ImageVector {
    return when (category) {
        CategoryType.SALARY -> Icons.Default.Work
        CategoryType.FREELANCE -> Icons.Default.AttachMoney
        CategoryType.INVESTMENT -> Icons.AutoMirrored.Filled.ShowChart
        CategoryType.BUSINESS -> Icons.Default.Storefront
        CategoryType.GIFTS -> Icons.Default.CardGiftcard
        CategoryType.REFUNDS -> Icons.Default.Refresh
        CategoryType.FOOD -> Icons.Default.Fastfood
        CategoryType.SHOPPING -> Icons.Default.ShoppingBag
        CategoryType.BILLS -> Icons.Default.Receipt
        CategoryType.TRANSPORT -> Icons.Default.DirectionsCar
        CategoryType.ENTERTAINMENT -> Icons.Default.LocalPlay
        CategoryType.HEALTH -> Icons.Default.MedicalServices
        CategoryType.EDUCATION -> Icons.Default.School
        CategoryType.TRAVEL -> Icons.Default.Flight
        CategoryType.RENT -> Icons.Default.Home
        CategoryType.TAX -> Icons.Default.AccountBalance
        CategoryType.OTHER -> Icons.Default.Category
    }
}

@Composable
private fun AddCardPlaceholder(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .widthIn(max = 360.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(onClick = onClick)
            .padding(22.dp)
    ) {
        // Blur Glow
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(160.dp, 80.dp)
                .blur(40.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(136.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Card",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Add New Card / Wallet",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Create a custom card or cash wallet",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun ShimmerBrush(): Brush {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition(label = "Shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerTranslation"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )
}

@Composable
private fun DashboardSkeleton(isLandscape: Boolean) {
    val shimmerBrush = ShimmerBrush()
    if (isLandscape) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Left Column
            Column(
                modifier = Modifier
                    .weight(1.25f)
                    .fillMaxHeight()
            ) {
                // Header skeleton
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Box(modifier = Modifier.size(100.dp, 12.dp).clip(RoundedCornerShape(4.dp)).background(shimmerBrush))
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(modifier = Modifier.size(140.dp, 20.dp).clip(RoundedCornerShape(6.dp)).background(shimmerBrush))
                    }
                    Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(shimmerBrush))
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Card skeleton
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(185.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 360.dp)
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(shimmerBrush)
                    )
                }
            }

            // Right Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                // Filter chips skeleton
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) {
                        Box(modifier = Modifier.size(80.dp, 32.dp).clip(RoundedCornerShape(16.dp)).background(shimmerBrush))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Recent Activities title skeleton
                Box(modifier = Modifier.size(120.dp, 16.dp).clip(RoundedCornerShape(4.dp)).background(shimmerBrush))

                Spacer(modifier = Modifier.height(12.dp))

                // List items skeleton
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(2) {
                        SkeletonRowItem(shimmerBrush)
                    }
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header skeleton
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Box(modifier = Modifier.size(100.dp, 14.dp).clip(RoundedCornerShape(4.dp)).background(shimmerBrush))
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.size(160.dp, 24.dp).clip(RoundedCornerShape(6.dp)).background(shimmerBrush))
                }
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(shimmerBrush))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Card Carousel skeleton
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(215.dp),
                contentAlignment = Alignment.Center
            ) {
                // Main card
                Box(
                    modifier = Modifier
                        .widthIn(max = 360.dp)
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(shimmerBrush)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Filter chips skeleton
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(4) {
                    Box(modifier = Modifier.size(70.dp, 32.dp).clip(RoundedCornerShape(16.dp)).background(shimmerBrush))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recent Activities title skeleton
            Box(modifier = Modifier.size(140.dp, 18.dp).clip(RoundedCornerShape(4.dp)).background(shimmerBrush))

            Spacer(modifier = Modifier.height(14.dp))

            // List items skeleton
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(3) {
                    SkeletonRowItem(shimmerBrush)
                }
            }
        }
    }
}

@Composable
private fun SkeletonRowItem(shimmerBrush: Brush) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(shimmerBrush))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Box(modifier = Modifier.size(120.dp, 14.dp).clip(RoundedCornerShape(4.dp)).background(shimmerBrush))
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.size(80.dp, 10.dp).clip(RoundedCornerShape(4.dp)).background(shimmerBrush))
                }
            }
            Box(modifier = Modifier.size(60.dp, 16.dp).clip(RoundedCornerShape(4.dp)).background(shimmerBrush))
        }
    }
}

fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    if (diff < 0) {
        return "Just now"
    }

    val oneMinute = 60 * 1000L
    val oneHour = 60 * oneMinute
    val oneDay = 24 * oneHour

    return when {
        diff < oneMinute -> "Just now"
        diff < oneHour -> {
            val minutes = diff / oneMinute
            "${minutes}m ago"
        }
        diff < oneDay -> {
            val hours = diff / oneHour
            "${hours}h ago"
        }
        diff < 2 * oneDay -> "Yesterday"
        else -> {
            val days = diff / oneDay
            if (days < 7) {
                "${days}d ago"
            } else {
                val weeks = days / 7
                if (weeks < 4) {
                    "${weeks}w ago"
                } else {
                    val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                    sdf.format(java.util.Date(timestamp))
                }
            }
        }
    }
}

@Composable
fun AnimatedThunderTagline(
    textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ThunderPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ThunderScale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ThunderAlpha"
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Smart Cash Flow",
            style = textStyle.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(3.dp))
        Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = "Thunder",
            tint = androidx.compose.ui.graphics.Color(0xFFFFB300),
            modifier = Modifier
                .size(16.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
        )
    }
}