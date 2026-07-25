package com.mudasir.flowcash.ui.screens

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
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
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
    onAddTransactionClick: () -> Unit
) {
    val allTransactions by dashboardViewModel.transactions.collectAsState()
    val transactions by dashboardViewModel.filteredTransactions.collectAsState()
    val selectedFilter by dashboardViewModel.selectedFilter.collectAsState()
    val selectedAccount by dashboardViewModel.selectedAccount.collectAsState()
    val accounts by dashboardViewModel.accounts.collectAsState()

    var isBalanceVisible by remember { mutableStateOf(true) }
    var showBottomSheetSelector by remember { mutableStateOf(false) }
    var showAddCardDialog by remember { mutableStateOf(false) }

    // Dialog input states for adding custom card
    var cardNameInput by remember { mutableStateOf("") }
    var cardNetworkInput by remember { mutableStateOf("VISA") } // CASH, VISA, MASTERCARD, AMEX
    var cardDigitsInput by remember { mutableStateOf("") }
    var cardExpiryInput by remember { mutableStateOf("") }
    var cardColorSelected by remember { mutableStateOf("#4F46E5") }

    // Filter all transactions by account to calculate the active balance metrics
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
    val netBalance = remember(totalIncome, totalExpense) {
        totalIncome - totalExpense
    }

    val cardColor = remember(selectedAccount) {
        selectedAccount?.let {
            try {
                Color(android.graphics.Color.parseColor(it.cardColorHex))
            } catch (e: Exception) {
                PrimaryIndigo
            }
        } ?: PrimaryIndigo
    }

    val network = selectedAccount?.network ?: "MASTERCARD"

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
                Text(
                    text = "Welcome back 👋",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = userName,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            }

            IconButton(
                onClick = { },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Background Radial Aura Glowing Box
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(240.dp, 100.dp)
                    .blur(50.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                cardColor.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Redesigned Dynamic Glassmorphism Credit Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                cardColor,
                                cardColor.copy(alpha = 0.85f),
                                cardColor.copy(alpha = 0.7f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .clickable { showBottomSheetSelector = true }
                    .padding(22.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Custom Wallet Selector Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = "Card",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = selectedAccount?.name ?: "All Accounts",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Network Emblems
                        when (network) {
                            "MASTERCARD" -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEB001B))
                                    )
                                    Spacer(modifier = Modifier.width(-8.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFBBF24).copy(alpha = 0.85f))
                                    )
                                }
                            }
                            "VISA" -> {
                                Text(
                                    text = "VISA",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                            "AMEX" -> {
                                Text(
                                    text = "AMEX",
                                    color = Color(0xFF0070CD),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.White)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            else -> {
                                Icon(
                                    imageVector = Icons.Default.AttachMoney,
                                    contentDescription = "Cash",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Microchip and contactless payment badge
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (network != "CASH") {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp, 24.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    Color(0xFFFCD34D),
                                                    Color(0xFFF59E0B)
                                                )
                                            )
                                        )
                                        .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Icon(
                                    imageVector = Icons.Default.Contactless,
                                    contentDescription = "Contactless",
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Text(
                                    text = "LIQUID CASH FLOW",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        // Eye toggle icon
                        IconButton(
                            onClick = { isBalanceVisible = !isBalanceVisible },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isBalanceVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle Balance",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isBalanceVisible) "$currencySymbol${String.format("%,.2f", netBalance)}" else "$currencySymbol • • • •",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 30.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Masked credit card digits decoration
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val digits = selectedAccount?.lastFourDigits?.ifBlank { "8839" } ?: "8839"
                        Text(
                            text = if (network != "CASH") "••••  ••••  ••••  $digits" else "STANDARD DIGITAL WALLET",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White.copy(alpha = 0.4f),
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = if (network != "CASH") 2.sp else 0.sp
                            )
                        )
                        if (network != "CASH") {
                            Text(
                                text = selectedAccount?.expiryDate?.ifBlank { "08/29" } ?: "08/29",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Income / Expense Metrics Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Income
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "Income",
                                    tint = Color.Green,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "Income",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 10.sp
                                    )
                                )
                                Text(
                                    text = if (isBalanceVisible) "$currencySymbol${String.format("%,.2f", totalIncome)}" else "$currencySymbol • • •",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        // Expense
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = "Expense",
                                    tint = Color(0xFFFF8A80),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "Expense",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 10.sp
                                    )
                                )
                                Text(
                                    text = if (isBalanceVisible) "$currencySymbol${String.format("%,.2f", totalExpense)}" else "$currencySymbol • • •",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { dashboardViewModel.setFilter(null) },
                    label = { Text("All Transactions") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == TransactionType.INCOME,
                    onClick = { dashboardViewModel.setFilter(TransactionType.INCOME) },
                    label = { Text("Income Only") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IncomeGreen,
                        selectedLabelColor = Color.White
                    )
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == TransactionType.EXPENSE,
                    onClick = { dashboardViewModel.setFilter(TransactionType.EXPENSE) },
                    label = { Text("Expenses Only") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ExpenseRed,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Text(
                text = "${transactions.size} items",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No Activity Registered Yet",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Start by recording your first transaction flow.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onAddTransactionClick,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Record First Transaction")
                    }
                }
            }
        } else {
            // Transaction List with Stable Keys
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = transactions,
                    key = { transaction -> transaction.id }
                ) { transaction ->
                    TransactionRowItem(transaction = transaction, currencySymbol = currencySymbol)
                }
            }
        }
    }

    // Modal Bottom Sheet Account Selector Redesign
    if (showBottomSheetSelector) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showBottomSheetSelector = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Select Active Wallet",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Toggle your active wallet to filter dashboard transaction metrics.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                // All Accounts Item
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (selectedAccount == null) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            else Color.Transparent
                        )
                        .clickable {
                            dashboardViewModel.setSelectedAccount(null)
                            showBottomSheetSelector = false
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = "All",
                            tint = if (selectedAccount == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "All Accounts",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (selectedAccount == null) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedAccount == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                    if (selectedAccount == null) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Custom Accounts Items list
                accounts.forEach { account ->
                    val isSelected = account.id == selectedAccount?.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                else Color.Transparent
                            )
                            .clickable {
                                dashboardViewModel.setSelectedAccount(account)
                                showBottomSheetSelector = false
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = account.name,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = account.name,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Add Card/Wallet Action Trigger Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            showBottomSheetSelector = false
                            cardNameInput = ""
                            cardDigitsInput = ""
                            cardExpiryInput = ""
                            cardNetworkInput = "VISA"
                            cardColorSelected = "#4F46E5"
                            showAddCardDialog = true
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Custom Card / Wallet",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    }

    // Add Custom Card/Wallet AlertDialog
    if (showAddCardDialog) {
        AlertDialog(
            onDismissRequest = { showAddCardDialog = false },
            title = { Text("Add New Card / Wallet") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = cardNameInput,
                        onValueChange = { cardNameInput = it },
                        label = { Text("Wallet / Account Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("VISA", "MASTERCARD", "AMEX", "CASH").forEach { net ->
                            val isSel = cardNetworkInput == net
                            Text(
                                text = net,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { cardNetworkInput = net }
                                    .padding(vertical = 8.dp),
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                fontSize = 10.sp
                            )
                        }
                    }
                    if (cardNetworkInput != "CASH") {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = cardDigitsInput,
                                onValueChange = { cardDigitsInput = it.take(4) },
                                label = { Text("Last 4 Digits") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            OutlinedTextField(
                                value = cardExpiryInput,
                                onValueChange = { cardExpiryInput = it },
                                label = { Text("Expiry (MM/YY)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Select Card Color Style",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("#4F46E5", "#059669", "#E11D48", "#7C3AED", "#0F172A").forEach { hex ->
                            val isSel = cardColorSelected == hex
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                    .border(
                                        width = if (isSel) 3.dp else 0.dp,
                                        color = if (isSel) MaterialTheme.colorScheme.outline else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { cardColorSelected = hex }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (cardNameInput.isNotBlank()) {
                            dashboardViewModel.addAccount(
                                name = cardNameInput,
                                network = cardNetworkInput,
                                lastFourDigits = cardDigitsInput,
                                expiryDate = cardExpiryInput,
                                colorHex = cardColorSelected
                            )
                            showAddCardDialog = false
                        }
                    }
                ) {
                    Text("Add Account", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCardDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TransactionRowItem(
    transaction: TransactionItem,
    currencySymbol: String
) {
    val isIncome = transaction.type == TransactionType.INCOME

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Category Icon Badge
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isIncome) IncomeGreen.copy(alpha = 0.15f) else ExpenseRed.copy(
                                alpha = 0.15f
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(transaction.category),
                        contentDescription = transaction.category.name,
                        tint = if (isIncome) IncomeGreen else ExpenseRed,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = transaction.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "${transaction.accountName} • ${transaction.dateFormatted}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "${if (isIncome) "+" else "-"}$currencySymbol${String.format("%,.2f", transaction.amount)}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isIncome) IncomeGreen else ExpenseRed
                )
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
