package com.mudasir.flowcash.ui.screens

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Widgets
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mudasir.flowcash.data.local.entity.AccountEntity
import com.mudasir.flowcash.ui.components.FlowCashConfirmDialog
import com.mudasir.flowcash.ui.theme.CARD_TEMPLATES
import com.mudasir.flowcash.ui.theme.CardTemplate
import com.mudasir.flowcash.ui.theme.ExpenseRed
import com.mudasir.flowcash.ui.theme.parseHexColor

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
    var selectedTemplateIndex by rememberSaveable(accountToEdit?.id) { mutableStateOf(initialTemplateIndex) }

    val hasChanges = remember(
        accountName, holderName, selectedType, customTypeName,
        selectedTemplateIndex, accountToEdit
    ) {
        val initialName = accountToEdit?.name ?: ""
        val initialHolder = accountToEdit?.holderName ?: ""

        val initialTypeRaw = accountToEdit?.accountType ?: "CARD"
        val initialSelectedType = if (ACCOUNT_TYPES.any { it.first == initialTypeRaw }) initialTypeRaw else "OTHER"
        val initialCustomTypeName = if (initialSelectedType == "OTHER") initialTypeRaw else ""

        val initialTemplateIdx = CARD_TEMPLATES.indexOfFirst { it.startHex == accountToEdit?.cardColorStart }.coerceAtLeast(0)

        accountName != initialName ||
                holderName != initialHolder ||
                selectedType != initialSelectedType ||
                (selectedType == "OTHER" && customTypeName != initialCustomTypeName) ||
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

    val isOtherType = selectedType == "OTHER"
    val isFormValid = accountName.isNotBlank() && (!isOtherType || customTypeName.isNotBlank())
    val isSaveEnabled = isFormValid && (!isEditMode || hasChanges)
    val currentTemplate = CARD_TEMPLATES[selectedTemplateIndex]

    val resolvedAccountTypeLabel = if (isOtherType && customTypeName.isNotBlank()) {
        customTypeName.trim()
    } else if (isOtherType) {
        "Other"
    } else {
        selectedType
    }

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
                    network = "",
                    cardNumber = "",
                    expiryDate = "",
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
                        holderName = holderName.ifBlank { "ACCOUNT HOLDER" },
                        accountType = resolvedAccountTypeLabel,
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
                    selectedTemplateIndex = selectedTemplateIndex,
                    onSelectedTemplateIndexChange = { selectedTemplateIndex = it },
                    isOtherType = isOtherType,
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
                        holderName = holderName.ifBlank { "ACCOUNT HOLDER" },
                        accountType = resolvedAccountTypeLabel,
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
                    selectedTemplateIndex = selectedTemplateIndex,
                    onSelectedTemplateIndexChange = { selectedTemplateIndex = it },
                    isOtherType = isOtherType,
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
    selectedTemplateIndex: Int,
    onSelectedTemplateIndexChange: (Int) -> Unit,
    isOtherType: Boolean,
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
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

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
                    Text(
                        text = template.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
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
    startHex: String,
    endHex: String
) {
    val formattedTypeLabel = remember(accountType) {
        accountType.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
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
            // Top row: Account Name + Account Type Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = accountName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = formattedTypeLabel,
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Bottom: Holder name label and value
            Column {
                Text(
                    text = "ACCOUNT HOLDER",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = holderName.uppercase(),
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp,
                    maxLines = 1
                )
            }
        }
    }
}
