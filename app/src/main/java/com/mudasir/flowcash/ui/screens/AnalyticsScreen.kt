package com.mudasir.flowcash.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mudasir.flowcash.data.model.CategorySpending
import com.mudasir.flowcash.data.model.TransactionType
import com.mudasir.flowcash.ui.theme.IncomeGreen
import com.mudasir.flowcash.ui.theme.PrimaryIndigo
import com.mudasir.flowcash.ui.viewmodel.DashboardViewModel

@Composable
fun AnalyticsScreen(
    dashboardViewModel: DashboardViewModel,
    currencySymbol: String = "$"
) {
    val transactions by dashboardViewModel.transactions.collectAsState()

    // Calculate live analytics metrics
    val totalIncome = remember(transactions) {
        transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    }
    val totalExpense = remember(transactions) {
        transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }
    val savingsRatePercentage = remember(totalIncome, totalExpense) {
        if (totalIncome > 0) {
            (((totalIncome - totalExpense) / totalIncome) * 100).toInt().coerceIn(0, 100)
        } else {
            0
        }
    }

    val categorySpendings = remember(transactions, totalExpense) {
        val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
        expenses.groupBy { it.category }
            .map { (cat, list) ->
                val amt = list.sumOf { it.amount }
                val pct = if (totalExpense > 0) ((amt / totalExpense) * 100).toFloat() else 0f
                CategorySpending(
                    categoryName = cat.name.lowercase().replaceFirstChar { it.uppercase() },
                    categoryType = cat,
                    amount = amt,
                    percentage = pct
                )
            }
            .sortedByDescending { it.amount }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Cashflow Analytics",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (transactions.isEmpty()) {
            // Premium Empty State Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        RoundedCornerShape(24.dp)
                    )
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
                            .background(PrimaryIndigo.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "No data",
                            tint = PrimaryIndigo,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "No Cashflow Activity Yet",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Add income or expense transactions using the '+' button to generate automated analytics insight charts.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        } else {
            // Insight Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(PrimaryIndigo, Color(0xFF7C4DFF))
                        )
                    )
                    .padding(20.dp)
            ) {
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
                            text = "$savingsRatePercentage%",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 38.sp
                            )
                        )
                        Text(
                            text = if (savingsRatePercentage >= 20) "Excellent cash flow health!" else "Try to reduce non-essential spendings.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = IncomeGreen
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = "Trending",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Spending by Category",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (categorySpendings.isEmpty()) {
                Text(
                    text = "No expenses recorded to analyze.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                categorySpendings.forEach { spending ->
                    CategoryProgressRow(spending = spending, currencySymbol = currencySymbol)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun CategoryProgressRow(
    spending: CategorySpending,
    currencySymbol: String
) {
    val animatedProgress by animateFloatAsState(
        targetValue = spending.percentage / 100f,
        animationSpec = tween(durationMillis = 1000)
    )

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
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = spending.categoryName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = "$currencySymbol${String.format("%,.2f", spending.amount)} (${spending.percentage.toInt()}%)",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
        }
    }
}
