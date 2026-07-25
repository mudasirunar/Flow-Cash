package com.mudasir.flowcash.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mudasir.flowcash.data.model.CategoryType
import com.mudasir.flowcash.data.model.TransactionType
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

    var selectedIndex by remember { mutableIntStateOf(0) }
    var showAddModal by remember { mutableStateOf(false) }

    val authState by authViewModel.uiState.collectAsState()
    val currency by settingsViewModel.currency.collectAsState()

    val userName = authState.user?.name ?: "Mudasir Unar"
    val userEmail = authState.user?.email ?: "mudasir@flowcash.io"

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

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
            // Directional horizontal slide & fade tab animation
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
                        userName = userName,
                        currencySymbol = currency
                    )
                    1 -> TransactionsScreen(
                        dashboardViewModel = dashboardViewModel,
                        currencySymbol = currency,
                        onAddTransactionClick = { showAddModal = true }
                    )
                    2 -> AnalyticsScreen(currencySymbol = currency)
                    3 -> SettingsScreen(
                        settingsViewModel = settingsViewModel,
                        authViewModel = authViewModel,
                        userName = userName,
                        userEmail = userEmail,
                        onLogoutClick = onLogoutClick
                    )
                }
            }
        }

        // Add Transaction Modal Bottom Sheet
        if (showAddModal) {
            ModalBottomSheet(
                onDismissRequest = { showAddModal = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                AddTransactionSheetContent(
                    onAdd = { title, amount, type ->
                        dashboardViewModel.addTransaction(title, amount, type, CategoryType.SHOPPING)
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showAddModal = false
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AddTransactionSheetContent(
    onAdd: (title: String, amount: Double, type: TransactionType) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var isIncome by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "Add New Transaction",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title / Description") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("Amount ($)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val amt = amountText.toDoubleOrNull() ?: 0.0
                val type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE
                if (title.isNotBlank() && amt > 0.0) {
                    onAdd(title, amt, type)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Save Transaction", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
