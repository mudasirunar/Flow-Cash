package com.mudasir.flowcash.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mudasir.flowcash.data.local.entity.AccountEntity
import com.mudasir.flowcash.data.model.CategoryType
import com.mudasir.flowcash.data.model.TransactionItem
import com.mudasir.flowcash.data.model.TransactionType
import com.mudasir.flowcash.ui.theme.ExpenseRed
import com.mudasir.flowcash.ui.theme.IncomeGreen
import com.mudasir.flowcash.ui.theme.PrimaryIndigo
import com.mudasir.flowcash.ui.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    dashboardViewModel: DashboardViewModel,
    userName: String = "Mudasir",
    currencySymbol: String = "$",
    onAddTransactionClick: () -> Unit,
    onAddAccountClick: () -> Unit = {}
) {
    val allTransactions by dashboardViewModel.transactions.collectAsState()
    val transactions by dashboardViewModel.filteredTransactions.collectAsState()
    val selectedFilter by dashboardViewModel.selectedFilter.collectAsState()
    val selectedAccount by dashboardViewModel.selectedAccount.collectAsState()
    val accounts by dashboardViewModel.accounts.collectAsState()

    var isDataVisible by remember { mutableStateOf(true) }
    var showBottomSheetSelector by remember { mutableStateOf(false) }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Welcome back 👋", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(userName, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Eye toggle for ALL data
                IconButton(onClick = { isDataVisible = !isDataVisible }) {
                    Icon(
                        imageVector = if (isDataVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle Data Visibility",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
        }

        Spacer(modifier = Modifier.height(20.dp))

        // === Adaptive Dashboard Card ===
        if (selectedAccount != null) {
            // Card is selected — render it
            SelectedAccountCard(
                account = selectedAccount!!,
                netBalance = netBalance,
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                currencySymbol = currencySymbol,
                isDataVisible = isDataVisible,
                onClick = { showBottomSheetSelector = true }
            )
        } else {
            // No card selected — show clean overview
            OverviewCard(
                netBalance = netBalance,
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                currencySymbol = currencySymbol,
                isDataVisible = isDataVisible,
                onClick = { showBottomSheetSelector = true }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Filter Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recent Activity", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground))
            Text("${transactions.size} items", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (transactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No Activity Registered Yet", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Start by recording your first transaction.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onAddTransactionClick, shape = RoundedCornerShape(12.dp)) { Text("Record First Transaction") }
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(items = transactions, key = { it.id }) { tx ->
                    TransactionRowItem(transaction = tx, currencySymbol = currencySymbol)
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

                // "All Accounts" option
                AccountSelectorRow(
                    label = "All Accounts",
                    isSelected = selectedAccount == null,
                    onClick = {
                        dashboardViewModel.setSelectedAccount(null)
                        showBottomSheetSelector = false
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))

                accounts.forEach { account ->
                    AccountSelectorRow(
                        label = account.name,
                        isSelected = account.id == selectedAccount?.id,
                        onClick = {
                            dashboardViewModel.setSelectedAccount(account)
                            showBottomSheetSelector = false
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Add new card/wallet button
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
}

@Composable
private fun AccountSelectorRow(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CreditCard, contentDescription = label, tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(16.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface))
        }
        if (isSelected) Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
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
    onClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        // Aura glow
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(240.dp, 100.dp)
                .blur(50.dp)
                .background(Brush.radialGradient(colors = listOf(PrimaryIndigo.copy(alpha = 0.3f), Color.Transparent)))
        )

        Box(
            modifier = Modifier
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
                    Text("FlowCash", color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 2.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Animated balance
                AnimatedVisibility(visible = isDataVisible, enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { -it / 3 }, exit = fadeOut(tween(300)) + slideOutVertically(tween(300)) { -it / 3 }) {
                    Text(
                        text = "$currencySymbol${String.format("%,.2f", netBalance)}",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 32.sp)
                    )
                }
                if (!isDataVisible) {
                    Text("$currencySymbol ••••••", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold, color = Color.White.copy(alpha = 0.5f), fontSize = 32.sp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                IncomeExpenseMetricsRow(totalIncome, totalExpense, currencySymbol, isDataVisible)
            }
        }
    }
}

// === Selected Account Card (realistic credit card or account card) ===
@Composable
private fun SelectedAccountCard(
    account: AccountEntity,
    netBalance: Double,
    totalIncome: Double,
    totalExpense: Double,
    currencySymbol: String,
    isDataVisible: Boolean,
    onClick: () -> Unit
) {
    val startColor = parseHexColor(account.cardColorStart)
    val endColor = parseHexColor(account.cardColorEnd)
    val isCard = account.accountType == "CARD"
    val network = account.network

    Box(modifier = Modifier.fillMaxWidth()) {
        // Aura glow
        Box(
            modifier = Modifier.align(Alignment.Center).size(240.dp, 100.dp).blur(50.dp)
                .background(Brush.radialGradient(colors = listOf(endColor.copy(alpha = 0.3f), Color.Transparent)))
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(startColor, endColor)))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(24.dp))
                .clickable(onClick = onClick)
                .padding(22.dp)
        ) {
            Column {
                // Top: Name pill + Network
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.08f)).padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(account.name, style = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.SemiBold))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }

                    // Network logo
                    if (isCard) {
                        when (network) {
                            "MASTERCARD" -> Row {
                                Box(Modifier.size(22.dp).clip(CircleShape).background(Color(0xFFEB001B)))
                                Spacer(Modifier.width(-8.dp))
                                Box(Modifier.size(22.dp).clip(CircleShape).background(Color(0xFFFBBF24).copy(alpha = 0.85f)))
                            }
                            "VISA" -> Text("VISA", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp, letterSpacing = 2.sp)
                            "AMEX" -> Text("AMEX", color = Color(0xFF0070CD), fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color.White).padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    } else {
                        val typeIcon = when (account.accountType) {
                            "CASH_WALLET" -> Icons.Default.Wallet
                            "BANK_ACCOUNT" -> Icons.Default.AccountBalance
                            "INVESTMENT" -> Icons.AutoMirrored.Filled.ShowChart
                            "FREELANCE_INCOME" -> Icons.Default.Work
                            "SAVINGS" -> Icons.Default.Savings
                            else -> Icons.Default.AttachMoney
                        }
                        Icon(typeIcon, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(22.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Chip + Contactless (only for cards)
                if (isCard) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp, 26.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(Brush.linearGradient(listOf(Color(0xFFFCD34D), Color(0xFFF59E0B))))
                                .border(0.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(5.dp))
                        ) {
                            Column(modifier = Modifier.fillMaxSize().padding(3.dp), verticalArrangement = Arrangement.SpaceEvenly) {
                                repeat(3) {
                                    Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFD97706).copy(alpha = 0.4f)))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(Icons.Default.Contactless, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Balance
                AnimatedVisibility(visible = isDataVisible, enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { -it / 3 }, exit = fadeOut(tween(300)) + slideOutVertically(tween(300)) { -it / 3 }) {
                    Text("$currencySymbol${String.format("%,.2f", netBalance)}", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 28.sp))
                }
                if (!isDataVisible) {
                    Text("$currencySymbol ••••••", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold, color = Color.White.copy(alpha = 0.5f), fontSize = 28.sp))
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Card number (only for card type) + holder name
                if (isCard) {
                    AnimatedVisibility(
                        visible = isDataVisible,
                        enter = fadeIn(tween(400)),
                        exit = fadeOut(tween(300))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val rawNumber = account.cardNumber.ifBlank { "0000000000000000" }

                            val fullFormatted = rawNumber
                                .padEnd(16, '0')
                                .chunked(4)
                                .joinToString("   ")

                            Text(
                                text = fullFormatted,
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                fontSize = 13.sp
                            )
                            Text(
                                text = account.expiryDate.ifBlank { "MM/YY" },
                                color = Color.White.copy(alpha = 0.7f),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Masked view when Eye icon is turned off
                    if (!isDataVisible) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val lastFour = account.cardNumber.takeLast(4).ifBlank { "0000" }
                            val maskedFormatted = "••••   ••••   ••••   $lastFour"

                            Text(
                                text = maskedFormatted,
                                color = Color.White.copy(alpha = 0.5f),
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 2.sp,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "••/••",
                                color = Color.White.copy(alpha = 0.35f),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Holder name
                if (account.holderName.isNotBlank()) {
                    AnimatedVisibility(visible = isDataVisible, enter = fadeIn(tween(400)), exit = fadeOut(tween(300))) {
                        Text(account.holderName.uppercase(), color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
                    }
                    if (!isDataVisible) {
                        Text("•••••••", color = Color.White.copy(alpha = 0.25f), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                IncomeExpenseMetricsRow(totalIncome, totalExpense, currencySymbol, isDataVisible)
            }
        }
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
                AnimatedVisibility(visible = isVisible, enter = fadeIn(tween(400)), exit = fadeOut(tween(300))) {
                    Text("$currencySymbol${String.format("%,.2f", totalIncome)}", style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                }
                if (!isVisible) Text("$currencySymbol •••", color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(28.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.08f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.ArrowDownward, contentDescription = "Expense", tint = Color(0xFFFF8A80), modifier = Modifier.size(14.dp))
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text("Expense", style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp))
                AnimatedVisibility(visible = isVisible, enter = fadeIn(tween(400)), exit = fadeOut(tween(300))) {
                    Text("$currencySymbol${String.format("%,.2f", totalExpense)}", style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                }
                if (!isVisible) Text("$currencySymbol •••", color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun TransactionRowItem(transaction: TransactionItem, currencySymbol: String) {
    val isIncome = transaction.type == TransactionType.INCOME
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(44.dp).clip(CircleShape).background(if (isIncome) IncomeGreen.copy(alpha = 0.15f) else ExpenseRed.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                    Icon(getCategoryIcon(transaction.category), contentDescription = transaction.category.name, tint = if (isIncome) IncomeGreen else ExpenseRed, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(transaction.title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface))
                    Text("${transaction.accountName} • ${transaction.dateFormatted}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(
                text = "${if (isIncome) "+" else "-"}$currencySymbol${String.format("%,.2f", transaction.amount)}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = if (isIncome) IncomeGreen else ExpenseRed)
            )
        }
    }
}

fun getCategoryIcon(category: CategoryType): ImageVector {
    return when (category) {
        CategoryType.SALARY -> Icons.Default.Work
        CategoryType.FREELANCE -> Icons.Default.AttachMoney
        CategoryType.INVESTMENT -> Icons.Default.ArrowUpward
        CategoryType.FOOD -> Icons.Default.Fastfood
        CategoryType.SHOPPING -> Icons.Default.ShoppingBag
        CategoryType.BILLS -> Icons.Default.Receipt
        else -> Icons.Default.Category
    }
}
