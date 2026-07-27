package com.mudasir.flowcash.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.rounded.SettingsSuggest
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.DarkMode
import com.mudasir.flowcash.ui.theme.PrimaryIndigo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Security
import com.mudasir.flowcash.ui.components.CurrencyOptionItem
import com.mudasir.flowcash.ui.components.FlowCashAlertDialog
import com.mudasir.flowcash.ui.components.FlowCashConfirmDialog
import com.mudasir.flowcash.ui.components.FlowCashSelectionDialog
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.mudasir.flowcash.util.BiometricAuthHelper
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mudasir.flowcash.BuildConfig
import com.mudasir.flowcash.data.preferences.ThemeMode
import com.mudasir.flowcash.ui.components.UserProfileAvatar
import com.mudasir.flowcash.ui.theme.ExpenseRed
import com.mudasir.flowcash.ui.theme.PrimaryIndigo
import com.mudasir.flowcash.ui.viewmodel.AuthViewModel
import com.mudasir.flowcash.ui.viewmodel.DashboardViewModel
import com.mudasir.flowcash.ui.viewmodel.SettingsViewModel
import com.mudasir.flowcash.util.UserAvatarUtils

data class CurrencyOption(
    val code: String,
    val symbol: String,
    val country: String,
    val name: String
)

val AvailableCurrencies = listOf(
    CurrencyOption("PKR", "Rs", "Pakistan", "Pakistani Rupee"),
    CurrencyOption("USD", "$", "United States", "US Dollar"),
    CurrencyOption("EUR", "€", "European Union", "Euro"),
    CurrencyOption("GBP", "£", "United Kingdom", "British Pound"),
    CurrencyOption("CAD", "$", "Canada", "Canadian Dollar"),
    CurrencyOption("AUD", "$", "Australia", "Australian Dollar"),
    CurrencyOption("JPY", "¥", "Japan", "Japanese Yen"),
    CurrencyOption("CHF", "Fr", "Switzerland", "Swiss Franc"),
    CurrencyOption("AED", "د.إ", "United Arab Emirates", "UAE Dirham"),
    CurrencyOption("SAR", "﷼", "Saudi Arabia", "Saudi Riyal"),
    CurrencyOption("SGD", "$", "Singapore", "Singapore Dollar")
)

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    authViewModel: AuthViewModel,
    dashboardViewModel: DashboardViewModel? = null,
    userName: String = "User",
    userEmail: String = "user@example.com",
    onLogoutClick: () -> Unit,
    bottomPadding: Dp = 100.dp
) {
    val themeMode by settingsViewModel.themeMode.collectAsState()
    val currency by settingsViewModel.currency.collectAsState()
    val currencyCode by settingsViewModel.currencyCode.collectAsState()
    val biometricsEnabled by settingsViewModel.biometricsEnabled.collectAsState()
    val dailyReminderEnabled by settingsViewModel.dailyReminderEnabled.collectAsState()
    val weeklySummaryEnabled by settingsViewModel.weeklySummaryEnabled.collectAsState()
    val isResettingData by settingsViewModel.isResettingData.collectAsState()

    val accounts by (dashboardViewModel?.accounts?.collectAsState() ?: remember { mutableStateOf(emptyList()) })
    val transactions by (dashboardViewModel?.transactions?.collectAsState() ?: remember { mutableStateOf(emptyList()) })

    val hasData = remember(accounts, transactions) {
        !accounts.isNullOrEmpty() || transactions.isNotEmpty()
    }
    val isResetEnabled = hasData && !isResettingData

    var showResetDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var isLoggingOut by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val biometricStatus = remember(context) {
        BiometricAuthHelper.getBiometricStatus(context)
    }
    val isBiometricReady = remember(context) {
        BiometricAuthHelper.isBiometricAvailable(context)
    }

    var showBiometricEnableDialog by remember { mutableStateOf(false) }
    var showBiometricDisableDialog by remember { mutableStateOf(false) }
    var biometricFeedbackTitle by remember { mutableStateOf<String?>(null) }
    var biometricFeedbackMessage by remember { mutableStateOf<String?>(null) }

    val modernSwitchColors = SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = PrimaryIndigo,
        checkedBorderColor = PrimaryIndigo,
        uncheckedThumbColor = Color.White.copy(alpha = 0.9f),
        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
        uncheckedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Settings & Preferences",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Profile Card with high-contrast surface container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    RoundedCornerShape(20.dp)
                )
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val authState by authViewModel.uiState.collectAsState()
                UserProfileAvatar(
                    name = userName,
                    email = userEmail,
                    profilePicUrl = authState.user?.profilePicUrl,
                    avatarColorHex = authState.user?.avatarColorHex,
                    size = 60.dp
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Personal",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = userEmail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Appearance & Theme Section
        Text(
            text = "Appearance & Theme",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        SlidingPillThemeSelector(
            selectedMode = themeMode,
            onModeSelected = { settingsViewModel.setThemeMode(it) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Security & Notifications Section
        Text(
            text = "Security & Notifications",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Biometrics Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = "Biometrics",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        val biometricSubtitle = when (biometricStatus) {
                            BiometricAuthHelper.BiometricStatus.AVAILABLE -> "Require fingerprint or Face ID to unlock balances and cards"
                            BiometricAuthHelper.BiometricStatus.NONE_ENROLLED -> "No fingerprint enrolled on device — tap to setup in Settings"
                            else -> "Biometric hardware unavailable on this device"
                        }

                        Column {
                            Text(
                                text = "Biometric Lock",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = biometricSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isBiometricReady) MaterialTheme.colorScheme.onSurfaceVariant else ExpenseRed
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = biometricsEnabled,
                        onCheckedChange = { targetState ->
                            if (targetState) {
                                if (!isBiometricReady) {
                                    biometricFeedbackTitle = "Biometrics Unavailable"
                                    biometricFeedbackMessage = when (biometricStatus) {
                                        BiometricAuthHelper.BiometricStatus.NONE_ENROLLED -> "No fingerprint or Face ID enrolled on this device. Please add a fingerprint in Android Settings > Security first."
                                        else -> "Biometric hardware is not available on this device."
                                    }
                                } else {
                                    showBiometricEnableDialog = true
                                }
                            } else {
                                showBiometricDisableDialog = true
                            }
                        },
                        colors = modernSwitchColors
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                )

                // Daily Reminder Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Reminder",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Daily Activity Reminders",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Get notified at 8 PM to log daily expenses",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = dailyReminderEnabled,
                        onCheckedChange = { settingsViewModel.setDailyReminderEnabled(it) },
                        colors = modernSwitchColors
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                )

                // Weekly Summary Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = "Summary",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Weekly Budget Summaries",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Receive weekly spending breakdown reports",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = weeklySummaryEnabled,
                        onCheckedChange = { settingsViewModel.setWeeklySummaryEnabled(it) },
                        colors = modernSwitchColors
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Currency & Regional Section
        Text(
            text = "Currency & Region",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { showCurrencyDialog = true },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = "Currency",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Primary Currency",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Applied to balances, cards, and reports",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = currency,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Data Management Section
        val resetCardBgColor = if (isResetEnabled) ExpenseRed.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        val resetCardBorderColor = if (isResetEnabled) ExpenseRed.copy(alpha = 0.25f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        val resetIconBgColor = if (isResetEnabled) ExpenseRed.copy(alpha = 0.15f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
        val resetIconTint = if (isResetEnabled) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        val resetTitleColor = if (isResetEnabled) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        val resetHeaderColor = if (isResetEnabled) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

        Text(
            text = "Data Management",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = resetHeaderColor
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable(enabled = isResetEnabled) { showResetDialog = true },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = resetCardBgColor),
            border = BorderStroke(1.dp, resetCardBorderColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(resetIconBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Reset Data",
                            tint = resetIconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Reset All App Data",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = resetTitleColor
                            )
                        )
                        Text(
                            text = if (hasData) "Clear all account balances and transaction history" else "No data registered yet (account empty)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isResetEnabled) 0.8f else 0.5f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // About App Card with BuildConfig version
        val appVersion = remember {
            try {
                BuildConfig.VERSION_NAME
            } catch (e: Exception) {
                "1.0"
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PrimaryIndigo),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "FlowCash",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "FlowCash",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "v$appVersion",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Personal Cash Flow & Smart Budget Tracker",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Logout Button with Confirmation Dialog
        Button(
            onClick = { showLogoutDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed.copy(alpha = 0.9f))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = "Logout",
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Log Out",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(bottomPadding))
    }

    if (showBiometricEnableDialog) {
        FlowCashConfirmDialog(
            onDismissRequest = { showBiometricEnableDialog = false },
            title = "Enable Biometric Protection",
            message = "Scan your fingerprint or Face ID to activate Biometric Protection for your financial balances.",
            icon = Icons.Default.Fingerprint,
            confirmButtonText = "Scan Fingerprint",
            onConfirm = {
                showBiometricEnableDialog = false
                BiometricAuthHelper.promptBiometricAuth(
                    context = context,
                    title = "Configure Biometric Lock",
                    subtitle = "Scan fingerprint to activate biometric security",
                    onSuccess = {
                        settingsViewModel.setBiometricsEnabled(true)
                        biometricFeedbackTitle = "Biometric Lock Activated"
                        biometricFeedbackMessage = "Your fingerprint is now configured. You can safely hide and unhide your sensitive balances on your dashboard."
                    },
                    onError = { err ->
                        biometricFeedbackTitle = "Biometric Setup Failed"
                        biometricFeedbackMessage = err
                    }
                )
            }
        )
    }

    if (showBiometricDisableDialog) {
        FlowCashConfirmDialog(
            onDismissRequest = { showBiometricDisableDialog = false },
            title = "Disable Biometric Lock",
            message = "Scan your fingerprint or Face ID to confirm disabling Biometric Protection.",
            icon = Icons.Default.Security,
            iconTint = ExpenseRed,
            badgeBackground = ExpenseRed.copy(alpha = 0.12f),
            showStrikeThrough = true,
            confirmButtonText = "Confirm",
            onConfirm = {
                showBiometricDisableDialog = false
                BiometricAuthHelper.promptBiometricAuth(
                    context = context,
                    title = "Disable Biometric Lock",
                    subtitle = "Scan fingerprint to confirm disabling security",
                    onSuccess = {
                        settingsViewModel.setBiometricsEnabled(false)
                        biometricFeedbackTitle = "Biometric Protection Disabled"
                        biometricFeedbackMessage = "Biometric security is turned off. Balances can now be unhidden without fingerprint verification."
                    },
                    onError = { err ->
                        biometricFeedbackTitle = "Verification Failed"
                        biometricFeedbackMessage = err
                    }
                )
            }
        )
    }

    if (biometricFeedbackMessage != null) {
        val isDisableFeedback = biometricFeedbackTitle == "Biometric Protection Disabled"
        FlowCashAlertDialog(
            onDismissRequest = { biometricFeedbackMessage = null },
            title = biometricFeedbackTitle ?: "Biometric Security",
            message = biometricFeedbackMessage ?: "",
            icon = Icons.Default.Security,
            iconTint = if (isDisableFeedback) ExpenseRed else PrimaryIndigo,
            badgeBackground = (if (isDisableFeedback) ExpenseRed else PrimaryIndigo).copy(alpha = 0.12f),
            showStrikeThrough = isDisableFeedback
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        FlowCashConfirmDialog(
            onDismissRequest = {
                if (!isLoggingOut) showLogoutDialog = false
            },
            title = if (isLoggingOut) "Signing Out..." else "Log Out of FlowCash?",
            message = if (isLoggingOut) "Clearing user preferences and local data..." else "Are you sure you want to log out of your account? You will need to sign in again to access your cash flow dashboard.",
            icon = Icons.AutoMirrored.Filled.Logout,
            isDestructive = true,
            isLoading = isLoggingOut,
            confirmButtonText = if (isLoggingOut) "Signing out..." else "Log Out",
            onConfirm = {
                isLoggingOut = true
                authViewModel.logout {
                    isLoggingOut = false
                    showLogoutDialog = false
                    onLogoutClick()
                }
            }
        )
    }

    // Reset Data Confirmation Dialog
    if (showResetDialog) {
        FlowCashConfirmDialog(
            onDismissRequest = {
                if (!isResettingData) showResetDialog = false
            },
            title = "Reset All App Data?",
            message = "This will permanently clear all your account balances, transaction records, and category settings from your device. This action cannot be undone.",
            icon = Icons.Default.DeleteSweep,
            isDestructive = true,
            confirmButtonText = if (isResettingData) "Clearing..." else "Clear All",
            onConfirm = {
                settingsViewModel.clearLocalData {
                    showResetDialog = false
                }
            }
        )
    }

    // Currency Selection Modal Dialog
    if (showCurrencyDialog) {
        val currencyOptions = AvailableCurrencies.map { option ->
            CurrencyOptionItem(
                symbol = option.symbol,
                code = option.code,
                name = "${option.name} • ${option.country}"
            )
        }
        FlowCashSelectionDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = "Select Primary Currency",
            icon = Icons.Default.AttachMoney,
            options = currencyOptions,
            selectedCode = currencyCode,
            onOptionSelected = { item ->
                settingsViewModel.setCurrency(item.symbol, item.code)
            }
        )
    }
}

private data class ThemeTabItem(
    val mode: ThemeMode,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: androidx.compose.ui.graphics.Color
)

@Composable
private fun SlidingPillThemeSelector(
    selectedMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit
) {
    val items = remember {
        listOf(
            ThemeTabItem(ThemeMode.SYSTEM, "System", Icons.Rounded.SettingsSuggest, PrimaryIndigo),
            ThemeTabItem(ThemeMode.LIGHT, "Light", Icons.Rounded.LightMode, androidx.compose.ui.graphics.Color(0xFFF59E0B)),
            ThemeTabItem(ThemeMode.DARK, "Dark", Icons.Rounded.DarkMode, androidx.compose.ui.graphics.Color(0xFF38BDF8))
        )
    }
    val selectedIndex = items.indexOfFirst { it.mode == selectedMode }.coerceAtLeast(0)
    val activeItem = items[selectedIndex]

    val activeColor by animateColorAsState(
        targetValue = activeItem.color,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = Spring.StiffnessLow
        ),
        label = "ThemeColorAnimation"
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(4.dp)
    ) {
        val widthPerItem = maxWidth / items.size
        val indicatorOffset by animateDpAsState(
            targetValue = widthPerItem * selectedIndex,
            animationSpec = spring(
                dampingRatio = 0.68f,
                stiffness = Spring.StiffnessLow
            ),
            label = "SlidingPillAnimation"
        )

        // Sliding Highlight Pill
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(widthPerItem)
                .height(44.dp)
                .border(1.5.dp, activeColor.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(activeColor.copy(alpha = 0.18f))
        )

        // Row of Icons and Labels
        Row(modifier = Modifier.fillMaxWidth()) {
            items.forEach { item ->
                val isSelected = selectedMode == item.mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onModeSelected(item.mode) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (isSelected) item.color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        )
                    }
                }
            }
        }
    }
}
