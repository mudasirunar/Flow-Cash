package com.mudasir.flowcash.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import com.mudasir.flowcash.ui.components.FlowCashInputDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mudasir.flowcash.data.local.entity.AccountEntity
import com.mudasir.flowcash.data.model.BarChartEntry
import com.mudasir.flowcash.data.model.CategorySpending
import com.mudasir.flowcash.data.model.FinancialHealthScore
import com.mudasir.flowcash.data.model.PeriodType
import com.mudasir.flowcash.data.model.TransactionItem
import com.mudasir.flowcash.data.model.TransactionType
import com.mudasir.flowcash.ui.theme.ExpenseRed
import com.mudasir.flowcash.ui.theme.IncomeGreen
import com.mudasir.flowcash.ui.theme.PrimaryIndigo
import com.mudasir.flowcash.ui.viewmodel.DashboardViewModel
import java.util.Calendar

@Composable
fun AnalyticsScreen(
    dashboardViewModel: DashboardViewModel,
    currencySymbol: String = "$"
) {
    val allTransactions by dashboardViewModel.transactions.collectAsState()
    val accounts by dashboardViewModel.accounts.collectAsState()
    val budgets by dashboardViewModel.budgets.collectAsState()

    var selectedAccountFilter by remember { mutableStateOf<AccountEntity?>(null) }
    var selectedPeriod by remember { mutableStateOf(PeriodType.MONTHLY) }

    var showBudgetDialog by remember { mutableStateOf(false) }
    var selectedCategoryForBudget by remember { mutableStateOf("") }
    var budgetInputValue by remember { mutableStateOf("") }

    // Filter transactions by selected account
    val filteredTransactions = remember(allTransactions, selectedAccountFilter) {
        if (selectedAccountFilter == null) {
            allTransactions
        } else {
            allTransactions.filter { it.accountName.equals(selectedAccountFilter?.name, ignoreCase = true) }
        }
    }

    // Totals
    val totalIncome = remember(filteredTransactions) {
        filteredTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    }
    val totalExpense = remember(filteredTransactions) {
        filteredTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }
    val netSurplus = totalIncome - totalExpense

    // Average daily spend calculation
    val avgDailySpend = remember(filteredTransactions, totalExpense) {
        if (filteredTransactions.isEmpty() || totalExpense <= 0) 0.0
        else {
            val timestamps = filteredTransactions.map { it.timestamp }
            val minTime = timestamps.minOrNull() ?: System.currentTimeMillis()
            val maxTime = timestamps.maxOrNull() ?: System.currentTimeMillis()
            val diffDays = ((maxTime - minTime) / (1000 * 60 * 60 * 24)).coerceAtLeast(1)
            totalExpense / diffDays
        }
    }

    // Financial Health Score
    val healthScore = remember(totalIncome, totalExpense) {
        val savingsRate = if (totalIncome > 0) {
            (((totalIncome - totalExpense) / totalIncome) * 100).toInt().coerceIn(0, 100)
        } else 0

        when {
            savingsRate >= 30 -> FinancialHealthScore(
                savingsRate = savingsRate,
                statusText = "Excellent Health",
                guidanceMessage = "Superb cashflow management! You're saving over 30% of your income. Consider allocating surplus to long-term investments.",
                isHealthy = true
            )
            savingsRate >= 15 -> FinancialHealthScore(
                savingsRate = savingsRate,
                statusText = "Good Stability",
                guidanceMessage = "Solid savings rate. Aim for 20%+ by trimming minor non-essential category expenses.",
                isHealthy = true
            )
            savingsRate > 0 -> FinancialHealthScore(
                savingsRate = savingsRate,
                statusText = "Needs Attention",
                guidanceMessage = "Low savings rate. Review high spending categories and set strict monthly budget limits.",
                isHealthy = false
            )
            else -> FinancialHealthScore(
                savingsRate = 0,
                statusText = "Deficit / High Risk",
                guidanceMessage = "Your expenses meet or exceed income. Focus on eliminating optional expenses to prevent cash flow strain.",
                isHealthy = false
            )
        }
    }

    // Category Spendings & Budgets
    val categorySpendings = remember(filteredTransactions, totalExpense, budgets) {
        val expenses = filteredTransactions.filter { it.type == TransactionType.EXPENSE }
        expenses.groupBy { it.category }
            .map { (cat, list) ->
                val amt = list.sumOf { it.amount }
                val catName = cat.name.lowercase().replaceFirstChar { it.uppercase() }
                val budgetLimit = budgets.find { it.categoryName.equals(catName, ignoreCase = true) }?.limitAmount ?: 0.0
                val pct = if (totalExpense > 0) ((amt / totalExpense) * 100).toFloat() else 0f

                CategorySpending(
                    categoryName = catName,
                    categoryType = cat,
                    amount = amt,
                    percentage = pct
                )
            }
            .sortedByDescending { it.amount }
    }

    // Bar chart entries aggregated by period
    val barEntries = remember(filteredTransactions, selectedPeriod) {
        computeBarChartEntries(filteredTransactions, selectedPeriod)
    }

    val isChartCriteriaMet = remember(barEntries) {
        barEntries.count { it.income > 0 || it.expense > 0 } >= 2
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Title Header
        Text(
            text = "Analytics & Insights",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 1. Account Selector Chips Row
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                val isSelected = selectedAccountFilter == null
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        .clickable { selectedAccountFilter = null }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "All",
                            tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "All Accounts",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }

            items(accounts ?: emptyList()) { acc ->
                val isSelected = selectedAccountFilter?.id == acc.id
                val icon = if (acc.accountType == "CARD") Icons.Default.CreditCard else Icons.Default.AccountBalanceWallet
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        .clickable { selectedAccountFilter = acc }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = icon,
                            contentDescription = acc.name,
                            tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = acc.name,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Time Period Segmented Selector (Weekly / Monthly / Yearly)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            PeriodType.entries.forEach { period ->
                val isSel = selectedPeriod == period
                val label = period.name.lowercase().replaceFirstChar { it.uppercase() }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSel) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable { selectedPeriod = period }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (filteredTransactions.isEmpty()) {
            // Tailored Account / Global Empty State Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(PrimaryIndigo.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            tint = PrimaryIndigo,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = if (selectedAccountFilter == null) "No Financial Activity Registered" else "No Data for ${selectedAccountFilter?.name}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (selectedAccountFilter == null)
                            "Add income or expense entries using the bottom '+' button to automatically generate cashflow trend graphs, category distribution, and budgeting analytics."
                        else
                            "There are no recorded transactions for this specific account yet. Switch to another account or tap '+' to add transactions here!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        } else {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                // 3. Health & Savings Rate Overview Banner Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(PrimaryIndigo, Color(0xFF7C4DFF))
                            )
                        )
                        .padding(22.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Monthly Savings Rate",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                )
                                Text(
                                    text = "${healthScore.savingsRate}%",
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        fontSize = 38.sp
                                    )
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (healthScore.isHealthy) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (healthScore.isHealthy) IncomeGreen else Color(0xFFFBBF24),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = healthScore.statusText,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Guidance message
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = Color(0xFFFCD34D),
                                modifier = Modifier.size(16.dp).padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = healthScore.guidanceMessage,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // 4 Key Stats Grid inside Card
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Black.copy(alpha = 0.15f))
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MetricStatItem(
                                label = "Total Income",
                                value = "$currencySymbol${String.format("%,.0f", totalIncome)}",
                                color = IncomeGreen
                            )
                            MetricStatItem(
                                label = "Total Expense",
                                value = "$currencySymbol${String.format("%,.0f", totalExpense)}",
                                color = Color(0xFFF87171)
                            )
                            MetricStatItem(
                                label = "Net Surplus",
                                value = "$currencySymbol${String.format("%,.0f", netSurplus)}",
                                color = Color.White
                            )
                            MetricStatItem(
                                label = "Avg Daily",
                                value = "$currencySymbol${String.format("%,.0f", avgDailySpend)}",
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 4. Trend Bar Chart Header & Content
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${selectedPeriod.name.lowercase().replaceFirstChar { it.uppercase() }} Cashflow Trend",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(IncomeGreen))
                        Spacer(Modifier.width(4.dp))
                        Text("Income", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(12.dp))
                        Box(Modifier.size(8.dp).clip(CircleShape).background(PrimaryIndigo))
                        Spacer(Modifier.width(4.dp))
                        Text("Expense", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isChartCriteriaMet) {
                    CashflowBarChart(
                        entries = barEntries,
                        currencySymbol = currencySymbol
                    )
                } else {
                    // Criteria Guidance Alert Card when data is insufficient for charts
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = "Not Enough Historical Trend Data",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Cashflow charts require activity spanning at least 2 distinct periods in this view. Keep recording daily transactions to unlock comparison graphs!",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 5. Category Breakdown & Budget Management Section
                Text(
                    text = "Category Spending & Limits",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap on any category card to define or update monthly spending thresholds.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (categorySpendings.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No category expenses recorded yet for this view.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    categorySpendings.forEach { spending ->
                        val matchingBudget = budgets.find { it.categoryName.equals(spending.categoryName, ignoreCase = true) }
                        val budgetLimit = matchingBudget?.limitAmount ?: 0.0

                        CategoryProgressRow(
                            spending = spending,
                            budgetLimit = budgetLimit,
                            currencySymbol = currencySymbol,
                            onEditBudgetClick = {
                                selectedCategoryForBudget = spending.categoryName
                                budgetInputValue = if (budgetLimit > 0) budgetLimit.toInt().toString() else ""
                                showBudgetDialog = true
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }

    // Set Budget Limit Modal Dialog
    if (showBudgetDialog) {
        FlowCashInputDialog(
            onDismissRequest = { showBudgetDialog = false },
            title = "Set Monthly Budget",
            subtitle = "Define a monthly spending limit for $selectedCategoryForBudget to receive live threshold alerts.",
            icon = Icons.Default.AccountBalanceWallet,
            inputValue = budgetInputValue,
            onValueChange = { budgetInputValue = it },
            placeholder = "Enter amount",
            prefixText = currencySymbol,
            keyboardType = KeyboardType.Number,
            confirmButtonText = "Save Limit",
            onConfirm = {
                val limit = budgetInputValue.toDoubleOrNull() ?: 0.0
                dashboardViewModel.updateBudget(selectedCategoryForBudget, limit)
                showBudgetDialog = false
            }
        )
    }
}

// === Subcomponents ===

@Composable
private fun MetricStatItem(
    label: String,
    value: String,
    color: Color
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 14.sp
            )
        )
    }
}

// Custom Interactive Canvas Bar Chart
@Composable
private fun CashflowBarChart(
    entries: List<BarChartEntry>,
    currencySymbol: String
) {
    var selectedEntryIndex by remember { mutableStateOf<Int?>(null) }

    val maxVal = remember(entries) {
        val highest = entries.flatMap { listOf(it.income, it.expense) }.maxOrNull() ?: 1.0
        if (highest <= 0) 1.0 else highest
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column {
            // Selected Bar Tooltip Pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp),
                contentAlignment = Alignment.Center
            ) {
                val activeIdx = selectedEntryIndex
                if (activeIdx != null && activeIdx in entries.indices) {
                    val entry = entries[activeIdx]
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "${entry.label}: Income $currencySymbol${String.format("%,.0f", entry.income)} • Expense $currencySymbol${String.format("%,.0f", entry.expense)}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Text(
                        text = "Tap on any bar to view exact amounts",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Canvas Bar Visualizer
            val primaryColor = PrimaryIndigo
            val incomeColor = IncomeGreen
            val selectedHighlightColor = MaterialTheme.colorScheme.primary

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .pointerInput(entries) {
                        detectTapGestures { tapOffset ->
                            val widthPerItem = size.width / entries.size.toFloat()
                            val clickedIndex = (tapOffset.x / widthPerItem).toInt().coerceIn(0, entries.size - 1)
                            selectedEntryIndex = clickedIndex
                        }
                    }
            ) {
                val chartWidth = size.width
                val chartHeight = size.height
                val itemCount = entries.size
                val itemWidth = chartWidth / itemCount

                entries.forEachIndexed { idx, entry ->
                    val isSelected = selectedEntryIndex == idx
                    val xCenter = idx * itemWidth + itemWidth / 2f

                    val maxBarHeight = chartHeight - 24.dp.toPx()

                    val incHeight = ((entry.income / maxVal) * maxBarHeight).toFloat().coerceAtLeast(4.dp.toPx())
                    val expHeight = ((entry.expense / maxVal) * maxBarHeight).toFloat().coerceAtLeast(4.dp.toPx())

                    val barWidth = (itemWidth * 0.28f).coerceAtMost(16.dp.toPx())

                    // Income Bar (Left)
                    drawRoundRect(
                        color = if (isSelected) incomeColor else incomeColor.copy(alpha = 0.85f),
                        topLeft = Offset(xCenter - barWidth - 2.dp.toPx(), chartHeight - incHeight),
                        size = Size(barWidth, incHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )

                    // Expense Bar (Right)
                    drawRoundRect(
                        color = if (isSelected) primaryColor else primaryColor.copy(alpha = 0.85f),
                        topLeft = Offset(xCenter + 2.dp.toPx(), chartHeight - expHeight),
                        size = Size(barWidth, expHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bar Labels Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                entries.forEachIndexed { idx, entry ->
                    val isSel = selectedEntryIndex == idx
                    Text(
                        text = entry.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryProgressRow(
    spending: CategorySpending,
    budgetLimit: Double,
    currencySymbol: String,
    onEditBudgetClick: () -> Unit
) {
    val isBudgetSet = budgetLimit > 0.0
    val isOverBudget = isBudgetSet && spending.amount > budgetLimit
    val progressFraction = if (isBudgetSet) (spending.amount / budgetLimit).toFloat().coerceIn(0f, 1f) else spending.percentage / 100f

    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(durationMillis = 1000),
        label = "ProgressAnim"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isOverBudget) ExpenseRed.copy(alpha = 0.08f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
            .border(
                1.dp,
                if (isOverBudget) ExpenseRed.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onEditBudgetClick)
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = spending.categoryName,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    if (isBudgetSet) {
                        Text(
                            text = "Monthly Limit: $currencySymbol${String.format("%,.2f", budgetLimit)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isOverBudget) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "No Budget Limit Set • Tap to configure",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isBudgetSet) {
                            "$currencySymbol${String.format("%,.2f", spending.amount)} (${(progressFraction * 100).toInt()}%)"
                        } else {
                            "$currencySymbol${String.format("%,.2f", spending.amount)} (${spending.percentage.toInt()}%)"
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isOverBudget) ExpenseRed else MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Budget",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = if (isOverBudget) ExpenseRed else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            if (isOverBudget) {
                Spacer(modifier = Modifier.height(6.dp))
                val excess = spending.amount - budgetLimit
                Text(
                    text = "⚠️ Over budget limit by $currencySymbol${String.format("%,.2f", excess)}!",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = ExpenseRed,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

// Helper calculation to aggregate entries by period
private fun computeBarChartEntries(
    transactions: List<TransactionItem>,
    periodType: PeriodType
): List<BarChartEntry> {
    val calendar = Calendar.getInstance()
    val now = System.currentTimeMillis()

    return when (periodType) {
        PeriodType.WEEKLY -> {
            val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            calendar.timeInMillis = now
            val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
            val currentYear = calendar.get(Calendar.YEAR)

            val map = days.associateWith { Pair(0.0, 0.0) }.toMutableMap()
            transactions.forEach { tx ->
                calendar.timeInMillis = tx.timestamp
                if (calendar.get(Calendar.WEEK_OF_YEAR) == currentWeek && calendar.get(Calendar.YEAR) == currentYear) {
                    val dayIdx = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
                    val dayName = days.getOrElse(dayIdx) { "Mon" }
                    val current = map[dayName] ?: Pair(0.0, 0.0)
                    if (tx.type == TransactionType.INCOME) {
                        map[dayName] = Pair(current.first + tx.amount, current.second)
                    } else {
                        map[dayName] = Pair(current.first, current.second + tx.amount)
                    }
                }
            }
            days.map { day ->
                val pair = map[day] ?: Pair(0.0, 0.0)
                BarChartEntry(day, pair.first, pair.second)
            }
        }

        PeriodType.MONTHLY -> {
            val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            calendar.timeInMillis = now
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH)

            val map = months.associateWith { Pair(0.0, 0.0) }.toMutableMap()
            transactions.forEach { tx ->
                calendar.timeInMillis = tx.timestamp
                if (calendar.get(Calendar.YEAR) == currentYear) {
                    val monthIdx = calendar.get(Calendar.MONTH)
                    val monthName = months.getOrElse(monthIdx) { "Jan" }
                    val current = map[monthName] ?: Pair(0.0, 0.0)
                    if (tx.type == TransactionType.INCOME) {
                        map[monthName] = Pair(current.first + tx.amount, current.second)
                    } else {
                        map[monthName] = Pair(current.first, current.second + tx.amount)
                    }
                }
            }

            // Show up to current month (or min 6 months)
            val maxShow = (currentMonth + 1).coerceAtLeast(6)
            months.take(maxShow).map { month ->
                val pair = map[month] ?: Pair(0.0, 0.0)
                BarChartEntry(month, pair.first, pair.second)
            }
        }

        PeriodType.YEARLY -> {
            val yearMap = mutableMapOf<Int, Pair<Double, Double>>()
            transactions.forEach { tx ->
                calendar.timeInMillis = tx.timestamp
                val y = calendar.get(Calendar.YEAR)
                val current = yearMap[y] ?: Pair(0.0, 0.0)
                if (tx.type == TransactionType.INCOME) {
                    yearMap[y] = Pair(current.first + tx.amount, current.second)
                } else {
                    yearMap[y] = Pair(current.first, current.second + tx.amount)
                }
            }

            if (yearMap.isEmpty()) {
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                listOf(BarChartEntry(currentYear.toString(), 0.0, 0.0))
            } else {
                yearMap.keys.sorted().map { y ->
                    val pair = yearMap[y] ?: Pair(0.0, 0.0)
                    BarChartEntry(y.toString(), pair.first, pair.second)
                }
            }
        }
    }
}
