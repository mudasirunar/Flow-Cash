package com.mudasir.flowcash.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mudasir.flowcash.data.local.entity.AccountEntity
import com.mudasir.flowcash.ui.theme.ExpenseRed
import com.mudasir.flowcash.ui.theme.PrimaryIndigo

// Premade card template data
data class CardTemplate(
    val name: String,
    val startHex: String,
    val endHex: String
)

val CARD_NETWORKS = listOf(
    "VISA",
    "MASTERCARD",
    "AMEX",
    "DISCOVER",
    "JCB",
    "UNIONPAY",
    "RUPAY"
)

val CARD_TEMPLATES = listOf(
    CardTemplate("Midnight Indigo", "#1E1B4B", "#4F46E5"),
    CardTemplate("Emerald Banking", "#064E3B", "#059669"),
    CardTemplate("Rose Gold", "#831843", "#E11D48"),
    CardTemplate("Obsidian Black", "#0F172A", "#1E293B"),
    CardTemplate("Royal Purple", "#4C1D95", "#7C3AED"),
    CardTemplate("Sapphire Blue", "#1E3A5F", "#2563EB"),
    CardTemplate("Sunset Orange", "#7C2D12", "#EA580C"),
    CardTemplate("Silver Platinum", "#374151", "#6B7280")
)

val ACCOUNT_TYPES = listOf(
    "CARD" to Icons.Default.CreditCard,
    "CASH_WALLET" to Icons.Default.Wallet,
    "INVESTMENT" to Icons.AutoMirrored.Filled.ShowChart,
    "FREELANCE_INCOME" to Icons.Default.Work,
    "SAVINGS" to Icons.Default.Savings,
    "OTHER" to Icons.Default.MoreHoriz
)

fun parseHexColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        PrimaryIndigo
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountScreen(
    accountToEdit: AccountEntity? = null,
    onSave: (AccountEntity) -> Unit,
    onDelete: ((AccountEntity) -> Unit)? = null,
    onBack: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val isEditMode = accountToEdit != null

    // Determine default pre-selected template or fallback
    val initialTemplateIndex = remember(accountToEdit) {
        CARD_TEMPLATES.indexOfFirst { it.startHex == accountToEdit?.cardColorStart }.coerceAtLeast(0)
    }

    var accountName by rememberSaveable(accountToEdit?.id) { mutableStateOf(accountToEdit?.name ?: "") }
    var holderName by rememberSaveable(accountToEdit?.id) { mutableStateOf(accountToEdit?.holderName ?: "") }
    
    val initialType = remember(accountToEdit) {
        val type = accountToEdit?.accountType ?: "CARD"
        if (ACCOUNT_TYPES.any { it.first == type }) type else "OTHER"
    }
    
    var customTypeName by rememberSaveable(accountToEdit?.id) {
        val type = accountToEdit?.accountType ?: "CARD"
        val initialSelectedType = if (ACCOUNT_TYPES.any { it.first == type }) type else "OTHER"
        mutableStateOf(if (initialSelectedType == "OTHER") type else "")
    }
    
    var selectedType by rememberSaveable(accountToEdit?.id) { mutableStateOf(initialType) }
    var selectedNetwork by rememberSaveable(accountToEdit?.id) { mutableStateOf(accountToEdit?.network?.ifEmpty { "VISA" } ?: "VISA") }
    var cardNumber by rememberSaveable(accountToEdit?.id) { mutableStateOf(accountToEdit?.cardNumber ?: "") }
    var rawExpiryDigits by rememberSaveable(accountToEdit?.id) { mutableStateOf(accountToEdit?.expiryDate?.filter { it.isDigit() } ?: "") }
    var selectedTemplateIndex by rememberSaveable(accountToEdit?.id) { mutableStateOf(initialTemplateIndex) }

    val hasChanges = remember(
        accountName, holderName, selectedType, customTypeName,
        selectedNetwork, cardNumber, rawExpiryDigits, selectedTemplateIndex, accountToEdit
    ) {
        val initialName = accountToEdit?.name ?: ""
        val initialHolder = accountToEdit?.holderName ?: ""
        
        val initialTypeRaw = accountToEdit?.accountType ?: "CARD"
        val initialSelectedType = if (ACCOUNT_TYPES.any { it.first == initialTypeRaw }) initialTypeRaw else "OTHER"
        val initialCustomTypeName = if (initialSelectedType == "OTHER") initialTypeRaw else ""
        
        val initialNetwork = accountToEdit?.network?.ifEmpty { "VISA" } ?: "VISA"
        val initialCardNumber = accountToEdit?.cardNumber ?: ""
        val initialRawExpiry = accountToEdit?.expiryDate?.filter { it.isDigit() } ?: ""
        
        val initialTemplateIdx = CARD_TEMPLATES.indexOfFirst { it.startHex == accountToEdit?.cardColorStart }.coerceAtLeast(0)

        accountName != initialName ||
        holderName != initialHolder ||
        selectedType != initialSelectedType ||
        (selectedType == "OTHER" && customTypeName != initialCustomTypeName) ||
        (selectedType == "CARD" && (
            selectedNetwork != initialNetwork ||
            cardNumber != initialCardNumber ||
            rawExpiryDigits != initialRawExpiry
        )) ||
        selectedTemplateIndex != initialTemplateIdx
    }

    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    val handleBack = {
        if (hasChanges) {
            showDiscardDialog = true
        } else {
            onBack()
        }
    }

    BackHandler(enabled = true) {
        if (showDiscardDialog) {
            showDiscardDialog = false
        } else if (showDeleteDialog) {
            showDeleteDialog = false
        } else {
            handleBack()
        }
    }

    if (showDiscardDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = {
                Text(
                    text = "Discard Changes?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "You have unsaved changes. Are you sure you want to discard them?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDiscardDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ExpenseRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Discard", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.OutlinedButton(
                    onClick = { showDiscardDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Delete Account?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${accountToEdit?.name}\"? All related income and expense transactions will be permanently deleted from history.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        accountToEdit?.let { onDelete?.invoke(it) }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ExpenseRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.OutlinedButton(
                    onClick = { showDeleteDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Focus state for validation on touch loss
    var cardNumberTouched by rememberSaveable(accountToEdit?.id) { mutableStateOf(false) }
    var cardNumberHasFocus by rememberSaveable(accountToEdit?.id) { mutableStateOf(false) }

    var expiryTouched by rememberSaveable(accountToEdit?.id) { mutableStateOf(false) }
    var expiryHasFocus by rememberSaveable(accountToEdit?.id) { mutableStateOf(false) }

    val isCardType = selectedType == "CARD"
    val isOtherType = selectedType == "OTHER"
    val isFormValid = accountName.isNotBlank() &&
            (!isOtherType || customTypeName.isNotBlank()) &&
            (!isCardType || (cardNumber.length == 16 && rawExpiryDigits.length == 4))
    val isSaveEnabled = isFormValid && (!isEditMode || hasChanges)
    val currentTemplate = CARD_TEMPLATES[selectedTemplateIndex]

    val resolvedAccountTypeLabel = if (isOtherType && customTypeName.isNotBlank()) {
        customTypeName.trim()
    } else if (isOtherType) {
        "Other"
    } else {
        selectedType
    }

    // Format raw 4 digits into MM/YY strictly for display on the Live Card Preview
    val previewFormattedExpiry = remember(rawExpiryDigits) {
        if (rawExpiryDigits.length >= 3) {
            "${rawExpiryDigits.take(2)}/${rawExpiryDigits.drop(2)}"
        } else {
            rawExpiryDigits
        }
    }

    // Errors evaluate only when the field has lost focus
    val isCardNumberError = isCardType && cardNumberTouched && !cardNumberHasFocus && cardNumber.length < 16
    val isExpiryError = isCardType && expiryTouched && !expiryHasFocus && rawExpiryDigits.length < 4

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "Edit Account" else "Add Account",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = handleBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isEditMode) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Account",
                                tint = ExpenseRed
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        val onSaveClick = {
            if (isFormValid) {
                val account = AccountEntity(
                    id = accountToEdit?.id ?: "acc_${System.currentTimeMillis()}",
                    name = accountName,
                    holderName = holderName,
                    accountType = resolvedAccountTypeLabel,
                    network = if (isCardType) selectedNetwork else "NONE",
                    cardNumber = if (isCardType) cardNumber else "",
                    expiryDate = if (isCardType) previewFormattedExpiry else "",
                    cardColorStart = currentTemplate.startHex,
                    cardColorEnd = currentTemplate.endHex,
                    createdAt = accountToEdit?.createdAt ?: System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    isSynced = false,
                    isDeleted = false
                )
                onSave(account)
            }
        }

        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Left Column: Sticky card preview
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    LiveCardPreview(
                        accountName = accountName.ifBlank { "Account Name" },
                        holderName = holderName.ifBlank { "CARDHOLDER" },
                        accountType = resolvedAccountTypeLabel,
                        network = selectedNetwork,
                        fullCardNumber = cardNumber,
                        expiryDate = previewFormattedExpiry.ifBlank { "MM/YY" },
                        startHex = currentTemplate.startHex,
                        endHex = currentTemplate.endHex
                    )
                }

                // Right Column: Scrollable input fields
                AccountForm(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    selectedType = selectedType,
                    onSelectedTypeChange = { selectedType = it },
                    customTypeName = customTypeName,
                    onCustomTypeNameChange = { customTypeName = it },
                    accountName = accountName,
                    onAccountNameChange = { accountName = it },
                    holderName = holderName,
                    onHolderNameChange = { holderName = it },
                    selectedNetwork = selectedNetwork,
                    onSelectedNetworkChange = { selectedNetwork = it },
                    cardNumber = cardNumber,
                    onCardNumberChange = { input -> cardNumber = input.filter { it.isDigit() }.take(16) },
                    rawExpiryDigits = rawExpiryDigits,
                    onRawExpiryChange = { input -> rawExpiryDigits = input.filter { it.isDigit() }.take(4) },
                    selectedTemplateIndex = selectedTemplateIndex,
                    onSelectedTemplateIndexChange = { selectedTemplateIndex = it },
                    isCardType = isCardType,
                    isOtherType = isOtherType,
                    isCardNumberError = isCardNumberError,
                    isExpiryError = isExpiryError,
                    onCardNumberFocusChanged = { touched, focused ->
                        if (touched) cardNumberTouched = true
                        cardNumberHasFocus = focused
                    },
                    onExpiryFocusChanged = { touched, focused ->
                        if (touched) expiryTouched = true
                        expiryHasFocus = focused
                    },
                    currentTemplate = currentTemplate,
                    isSaveEnabled = isSaveEnabled,
                    isEditMode = isEditMode,
                    onSaveClick = onSaveClick,
                    focusManager = focusManager
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LiveCardPreview(
                        accountName = accountName.ifBlank { "Account Name" },
                        holderName = holderName.ifBlank { "CARDHOLDER" },
                        accountType = resolvedAccountTypeLabel,
                        network = selectedNetwork,
                        fullCardNumber = cardNumber,
                        expiryDate = previewFormattedExpiry.ifBlank { "MM/YY" },
                        startHex = currentTemplate.startHex,
                        endHex = currentTemplate.endHex
                    )
                }

                AccountForm(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    selectedType = selectedType,
                    onSelectedTypeChange = { selectedType = it },
                    customTypeName = customTypeName,
                    onCustomTypeNameChange = { customTypeName = it },
                    accountName = accountName,
                    onAccountNameChange = { accountName = it },
                    holderName = holderName,
                    onHolderNameChange = { holderName = it },
                    selectedNetwork = selectedNetwork,
                    onSelectedNetworkChange = { selectedNetwork = it },
                    cardNumber = cardNumber,
                    onCardNumberChange = { input -> cardNumber = input.filter { it.isDigit() }.take(16) },
                    rawExpiryDigits = rawExpiryDigits,
                    onRawExpiryChange = { input -> rawExpiryDigits = input.filter { it.isDigit() }.take(4) },
                    selectedTemplateIndex = selectedTemplateIndex,
                    onSelectedTemplateIndexChange = { selectedTemplateIndex = it },
                    isCardType = isCardType,
                    isOtherType = isOtherType,
                    isCardNumberError = isCardNumberError,
                    isExpiryError = isExpiryError,
                    onCardNumberFocusChanged = { touched, focused ->
                        if (touched) cardNumberTouched = true
                        cardNumberHasFocus = focused
                    },
                    onExpiryFocusChanged = { touched, focused ->
                        if (touched) expiryTouched = true
                        expiryHasFocus = focused
                    },
                    currentTemplate = currentTemplate,
                    isSaveEnabled = isSaveEnabled,
                    isEditMode = isEditMode,
                    onSaveClick = onSaveClick,
                    focusManager = focusManager
                )
            }
        }
    }
}

@Composable
private fun AccountForm(
    modifier: Modifier = Modifier,
    selectedType: String,
    onSelectedTypeChange: (String) -> Unit,
    customTypeName: String,
    onCustomTypeNameChange: (String) -> Unit,
    accountName: String,
    onAccountNameChange: (String) -> Unit,
    holderName: String,
    onHolderNameChange: (String) -> Unit,
    selectedNetwork: String,
    onSelectedNetworkChange: (String) -> Unit,
    cardNumber: String,
    onCardNumberChange: (String) -> Unit,
    rawExpiryDigits: String,
    onRawExpiryChange: (String) -> Unit,
    selectedTemplateIndex: Int,
    onSelectedTemplateIndexChange: (Int) -> Unit,
    isCardType: Boolean,
    isOtherType: Boolean,
    isCardNumberError: Boolean,
    isExpiryError: Boolean,
    onCardNumberFocusChanged: (Boolean, Boolean) -> Unit,
    onExpiryFocusChanged: (Boolean, Boolean) -> Unit,
    currentTemplate: CardTemplate,
    isSaveEnabled: Boolean,
    isEditMode: Boolean,
    onSaveClick: () -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    Column(modifier = modifier) {
        // === Account Type Selector ===
        Text(
            text = "Account Type",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ACCOUNT_TYPES.forEach { (type, icon) ->
                val label = type.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                FilterChip(
                    selected = selectedType == type,
                    onClick = { onSelectedTypeChange(type) },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(label, fontSize = 11.sp)
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        // Animated input field when "OTHER" is selected
        AnimatedVisibility(visible = isOtherType) {
            Column {
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = customTypeName,
                    onValueChange = onCustomTypeNameChange,
                    label = { Text("Custom Type") },
                    placeholder = { Text("e.g. Crypto, Gift Card") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // === Account Name ===
        OutlinedTextField(
            value = accountName,
            onValueChange = onAccountNameChange,
            label = { Text("Account / Wallet Name") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // === Holder Name ===
        OutlinedTextField(
            value = holderName,
            onValueChange = onHolderNameChange,
            label = { Text("Holder Name (display on card)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = if (isCardType) ImeAction.Next else ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // === Card-specific fields ===
        AnimatedVisibility(visible = isCardType) {
            Column {
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Card Network",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CARD_NETWORKS.forEach { net ->
                        val isSel = selectedNetwork == net
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { onSelectedNetworkChange(net) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = net,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    // Card Number Field
                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = onCardNumberChange,
                        label = { Text("Card Number") },
                        placeholder = { Text("1234567890123456") },
                        singleLine = true,
                        isError = isCardNumberError,
                        supportingText = {
                            if (isCardNumberError) {
                                Text(
                                    text = "Incomplete card number",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier
                            .weight(1.5f)
                            .onFocusChanged { focusState ->
                                onCardNumberFocusChanged(focusState.isFocused, focusState.isFocused)
                            }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Clean 4-Digit Raw Expiry Field
                    OutlinedTextField(
                        value = rawExpiryDigits,
                        onValueChange = onRawExpiryChange,
                        label = { Text("Expiry") },
                        placeholder = { Text("MMYY") },
                        singleLine = true,
                        isError = isExpiryError,
                        supportingText = {
                            if (isExpiryError) {
                                Text(
                                    text = "Invalid date",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { focusState ->
                                onExpiryFocusChanged(focusState.isFocused, focusState.isFocused)
                            }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // === Card Template Selection ===
        Text(
            text = "Card Style",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Select a card template to set the look of your account card.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CARD_TEMPLATES.forEachIndexed { index, template ->
                val isSelected = selectedTemplateIndex == index
                Box(
                    modifier = Modifier
                        .size(width = 120.dp, height = 80.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    parseHexColor(template.startHex),
                                    parseHexColor(template.endHex)
                                )
                            )
                        )
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onSelectedTemplateIndexChange(index) }
                        .padding(10.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .size(18.dp, 12.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFFFCD34D), Color(0xFFF59E0B))
                                    )
                                )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = template.name,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // === Save Action Button ===
        Button(
            onClick = onSaveClick,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            enabled = isSaveEnabled
        ) {
            Text(if (isEditMode) "Save Changes" else "Create Account", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun LiveCardPreview(
    accountName: String,
    holderName: String,
    accountType: String,
    network: String,
    fullCardNumber: String,
    expiryDate: String,
    startHex: String,
    endHex: String
) {
    val isCardType = accountType == "CARD"

    val formattedCardNumber = remember(fullCardNumber) {
        val padded = fullCardNumber.padEnd(16, '•')
        padded.chunked(4).joinToString("   ")
    }

    Box(
        modifier = Modifier
            .widthIn(max = 360.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(parseHexColor(startHex), parseHexColor(endHex))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(22.dp))
            .padding(20.dp)
    ) {
        Column {
            // Top row: Name + Network Logo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = accountName,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )

                if (isCardType) {
                    CardNetworkLogo(network = network)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Chip + Contactless
            if (isCardType) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Realistic chip with inner lines
                    Box(
                        modifier = Modifier
                            .size(36.dp, 26.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(
                                Brush.linearGradient(listOf(Color(0xFFFCD34D), Color(0xFFF59E0B)))
                            )
                            .border(0.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(5.dp))
                    ) {
                        // Inner chip lines
                        Column(
                            modifier = Modifier.fillMaxSize().padding(3.dp),
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            repeat(3) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(Color(0xFFD97706).copy(alpha = 0.4f))
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        imageVector = Icons.Default.Contactless,
                        contentDescription = "Contactless",
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (isCardType) 16.dp else 8.dp))

            // Card number or account type label
            if (isCardType) {
                Text(
                    text = formattedCardNumber,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.sp
                    )
                )
            } else {
                val typeLabel = accountType.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                Text(
                    text = typeLabel,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom: Holder name + Expiry
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "CARD HOLDER",
                        color = Color.White.copy(alpha = 0.35f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = holderName.uppercase(),
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                }
                if (isCardType && expiryDate.isNotBlank()) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "VALID THRU",
                            color = Color.White.copy(alpha = 0.35f),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = expiryDate,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CardNetworkLogo(network: String) {
    when (network.uppercase()) {
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
                        .background(Color(0xFFFBBF24).copy(alpha = 0.9f))
                )
            }
        }
        "VISA" -> {
            Text(
                text = "VISA",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 17.sp,
                letterSpacing = 2.sp
            )
        }
        "AMEX" -> {
            Text(
                text = "AMEX",
                color = Color(0xFF0070CD),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        "DISCOVER" -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "DISCOVER",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(3.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF6000))
                )
            }
        }
        "JCB" -> {
            Text(
                text = "JCB",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF003781))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        "UNIONPAY" -> {
            Text(
                text = "UnionPay",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF007B88))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        "RUPAY" -> {
            Text(
                text = "RuPay",
                color = Color(0xFF132A63),
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        else -> {
            Text(
                text = network,
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}
