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
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Warning
import com.mudasir.flowcash.ui.components.FlowCashConfirmDialog
import androidx.compose.material.icons.filled.Widgets
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.mudasir.flowcash.ui.theme.CardTemplate
import com.mudasir.flowcash.ui.theme.CARD_TEMPLATES
import com.mudasir.flowcash.ui.theme.parseHexColor

val CARD_NETWORKS = listOf(
    "VISA",
    "MASTERCARD",
    "AMEX",
    "DISCOVER",
    "JCB",
    "UNIONPAY",
    "RUPAY"
)

val ACCOUNT_TYPES = listOf(
    "CARD" to Icons.Default.CreditCard,
    "CASH_WALLET" to Icons.Default.Wallet,
    "INVESTMENT" to Icons.Default.PieChart,
    "FREELANCE_INCOME" to Icons.Default.Work,
    "SAVINGS" to Icons.Default.Savings,
    "OTHER" to Icons.Default.Widgets
)

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
    var selectedTemplateIndex by rememberSaveable(accountToEdit?.id) { mutableStateOf(initialTemplateIndex) }

    val hasChanges = remember(
        accountName, holderName, selectedType, customTypeName,
        selectedNetwork, cardNumber, selectedTemplateIndex, accountToEdit
    ) {
        val initialName = accountToEdit?.name ?: ""
        val initialHolder = accountToEdit?.holderName ?: ""
        
        val initialTypeRaw = accountToEdit?.accountType ?: "CARD"
        val initialSelectedType = if (ACCOUNT_TYPES.any { it.first == initialTypeRaw }) initialTypeRaw else "OTHER"
        val initialCustomTypeName = if (initialSelectedType == "OTHER") initialTypeRaw else ""
        
        val initialNetwork = accountToEdit?.network?.ifEmpty { "VISA" } ?: "VISA"
        val initialCardNumber = accountToEdit?.cardNumber ?: ""
        
        val initialTemplateIdx = CARD_TEMPLATES.indexOfFirst { it.startHex == accountToEdit?.cardColorStart }.coerceAtLeast(0)

        accountName != initialName ||
        holderName != initialHolder ||
        selectedType != initialSelectedType ||
        (selectedType == "OTHER" && customTypeName != initialCustomTypeName) ||
        (selectedType == "CARD" && (
            selectedNetwork != initialNetwork ||
            cardNumber != initialCardNumber
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
        FlowCashConfirmDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = "Discard Changes?",
            message = "You have unsaved changes. Are you sure you want to discard them?",
            icon = Icons.Default.Warning,
            isDestructive = true,
            confirmButtonText = "Discard",
            onConfirm = {
                showDiscardDialog = false
                onBack()
            }
        )
    }

    if (showDeleteDialog) {
        FlowCashConfirmDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = "Delete Account?",
            message = "Are you sure you want to delete \"${accountToEdit?.name}\"? All related income and expense transactions will be permanently deleted from history.",
            icon = Icons.Default.DeleteForever,
            isDestructive = true,
            confirmButtonText = "Delete",
            onConfirm = {
                showDeleteDialog = false
                accountToEdit?.let { onDelete?.invoke(it) }
            }
        )
    }

    // Focus state for validation on touch loss
    var cardNumberTouched by rememberSaveable(accountToEdit?.id) { mutableStateOf(false) }
    var cardNumberHasFocus by rememberSaveable(accountToEdit?.id) { mutableStateOf(false) }

    val isCardType = selectedType == "CARD"
    val isOtherType = selectedType == "OTHER"
    val isFormValid = accountName.isNotBlank() &&
            (!isOtherType || customTypeName.isNotBlank()) &&
            (!isCardType || cardNumber.length == 16)
    val isSaveEnabled = isFormValid && (!isEditMode || hasChanges)
    val currentTemplate = CARD_TEMPLATES[selectedTemplateIndex]

    val resolvedAccountTypeLabel = if (isOtherType && customTypeName.isNotBlank()) {
        customTypeName.trim()
    } else if (isOtherType) {
        "Other"
    } else {
        selectedType
    }

    // Errors evaluate only when the field has lost focus
    val isCardNumberError = isCardType && cardNumberTouched && !cardNumberHasFocus && cardNumber.length < 16

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
                    expiryDate = accountToEdit?.expiryDate ?: "",
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
                        expiryDate = accountToEdit?.expiryDate ?: "",
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
                    selectedTemplateIndex = selectedTemplateIndex,
                    onSelectedTemplateIndexChange = { selectedTemplateIndex = it },
                    isCardType = isCardType,
                    isOtherType = isOtherType,
                    isCardNumberError = isCardNumberError,
                    onCardNumberFocusChanged = { touched, focused ->
                        if (touched) cardNumberTouched = true
                        cardNumberHasFocus = focused
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
                        expiryDate = accountToEdit?.expiryDate ?: "",
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
                    selectedTemplateIndex = selectedTemplateIndex,
                    onSelectedTemplateIndexChange = { selectedTemplateIndex = it },
                    isCardType = isCardType,
                    isOtherType = isOtherType,
                    isCardNumberError = isCardNumberError,
                    onCardNumberFocusChanged = { touched, focused ->
                        if (touched) cardNumberTouched = true
                        cardNumberHasFocus = focused
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
    selectedTemplateIndex: Int,
    onSelectedTemplateIndexChange: (Int) -> Unit,
    isCardType: Boolean,
    isOtherType: Boolean,
    isCardNumberError: Boolean,
    onCardNumberFocusChanged: (Boolean, Boolean) -> Unit,
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

        val accountTypeLazyState = rememberLazyListState()
        LaunchedEffect(selectedType) {
            val index = ACCOUNT_TYPES.indexOfFirst { it.first == selectedType }
            if (index >= 0) {
                accountTypeLazyState.animateScrollToItem(index)
            }
        }

        LazyRow(
            state = accountTypeLazyState,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ACCOUNT_TYPES.size) { index ->
                val (type, icon) = ACCOUNT_TYPES[index]
                val label = type.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                FilterChip(
                    selected = selectedType == type,
                    onClick = { onSelectedTypeChange(type) },
                    shape = RoundedCornerShape(12.dp),
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(label, fontSize = 11.sp)
                        }
                    },
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedType == type,
                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                        selectedBorderColor = MaterialTheme.colorScheme.primary,
                        borderWidth = 1.dp,
                        selectedBorderWidth = 1.5.dp
                    ),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedLabelColor = MaterialTheme.colorScheme.onSurface
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
                    shape = RoundedCornerShape(18.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    ),
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
            shape = RoundedCornerShape(18.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // === Holder Name ===
        OutlinedTextField(
            value = holderName,
            onValueChange = onHolderNameChange,
            label = { Text("Holder Name (display on card)") },
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            keyboardOptions = KeyboardOptions(
                imeAction = if (isCardType) ImeAction.Next else ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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
                val cardNetworkLazyState = rememberLazyListState()
                LaunchedEffect(selectedNetwork) {
                    val index = CARD_NETWORKS.indexOf(selectedNetwork)
                    if (index >= 0) {
                        cardNetworkLazyState.animateScrollToItem(index)
                    }
                }

                LazyRow(
                    state = cardNetworkLazyState,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(CARD_NETWORKS.size) { index ->
                        val net = CARD_NETWORKS[index]
                        val isSel = selectedNetwork == net
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                )
                                .border(
                                    width = if (isSel) 1.5.dp else 1.dp,
                                    color = if (isSel) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onSelectedNetworkChange(net) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = net,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Card Number Field
                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = onCardNumberChange,
                    label = { Text("Card Number") },
                    placeholder = { Text("1234567890123456") },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
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
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            onCardNumberFocusChanged(focusState.isFocused, focusState.isFocused)
                        }
                )
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

        val cardTemplateLazyState = rememberLazyListState()
        LaunchedEffect(selectedTemplateIndex) {
            if (selectedTemplateIndex >= 0 && selectedTemplateIndex < CARD_TEMPLATES.size) {
                cardTemplateLazyState.animateScrollToItem(selectedTemplateIndex)
            }
        }

        LazyRow(
            state = cardTemplateLazyState,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(CARD_TEMPLATES.size) { index ->
                val template = CARD_TEMPLATES[index]
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
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.8f),
                                            Color.White.copy(alpha = 0.4f)
                                        )
                                    )
                                )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = template.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            maxLines = 1
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
    expiryDate: String = "",
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
