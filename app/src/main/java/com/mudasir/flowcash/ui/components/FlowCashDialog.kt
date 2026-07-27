package com.mudasir.flowcash.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mudasir.flowcash.ui.theme.ExpenseRed
import com.mudasir.flowcash.ui.theme.PrimaryIndigo

// === 1. FlowCashAlertDialog (Info / Success / Feedback) ===
@Composable
fun FlowCashAlertDialog(
    onDismissRequest: () -> Unit,
    title: String,
    message: String,
    icon: ImageVector = Icons.Default.Info,
    iconTint: Color = PrimaryIndigo,
    badgeBackground: Color = PrimaryIndigo.copy(alpha = 0.12f),
    confirmButtonText: String = "OK",
    showStrikeThrough: Boolean = false,
    onConfirm: () -> Unit = onDismissRequest
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(28.dp))
                .border(
                    1.5.dp,
                    iconTint.copy(alpha = 0.45f),
                    RoundedCornerShape(28.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Icon Badge
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(badgeBackground)
                        .border(1.dp, iconTint.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint.copy(alpha = if (showStrikeThrough) 0.6f else 1f),
                        modifier = Modifier.size(30.dp)
                    )
                    if (showStrikeThrough) {
                        androidx.compose.foundation.Canvas(modifier = Modifier.size(30.dp)) {
                            drawLine(
                                color = iconTint,
                                start = androidx.compose.ui.geometry.Offset(x = size.width * 0.15f, y = size.height * 0.15f),
                                end = androidx.compose.ui.geometry.Offset(x = size.width * 0.85f, y = size.height * 0.85f),
                                strokeWidth = 3.5.dp.toPx(),
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    textAlign = TextAlign.Center
                )

                if (message.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = iconTint,
                        contentColor = Color.White
                    )
                ) {
                    Text(confirmButtonText, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// === 2. FlowCashConfirmDialog (Two-Button Action / Confirmation / Delete) ===
@Composable
fun FlowCashConfirmDialog(
    onDismissRequest: () -> Unit,
    title: String,
    message: String,
    icon: ImageVector,
    iconTint: Color = PrimaryIndigo,
    badgeBackground: Color = PrimaryIndigo.copy(alpha = 0.12f),
    confirmButtonText: String = "Confirm",
    dismissButtonText: String = "Cancel",
    isDestructive: Boolean = false,
    showStrikeThrough: Boolean = false,
    isLoading: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit = onDismissRequest
) {
    val buttonColor = if (isDestructive) ExpenseRed else iconTint

    Dialog(
        onDismissRequest = {
            if (!isLoading) onDismissRequest()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = !isLoading,
            dismissOnClickOutside = !isLoading
        )
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(28.dp))
                .border(
                    1.5.dp,
                    buttonColor.copy(alpha = 0.45f),
                    RoundedCornerShape(28.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Icon Badge
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(badgeBackground)
                        .border(1.dp, buttonColor.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = buttonColor,
                            strokeWidth = 3.dp
                        )
                    } else {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = buttonColor.copy(alpha = if (showStrikeThrough) 0.6f else 1f),
                            modifier = Modifier.size(32.dp)
                        )
                        if (showStrikeThrough) {
                            androidx.compose.foundation.Canvas(modifier = Modifier.size(32.dp)) {
                                drawLine(
                                    color = buttonColor,
                                    start = androidx.compose.ui.geometry.Offset(x = size.width * 0.15f, y = size.height * 0.15f),
                                    end = androidx.compose.ui.geometry.Offset(x = size.width * 0.85f, y = size.height * 0.85f),
                                    strokeWidth = 3.5.dp.toPx(),
                                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    textAlign = TextAlign.Center
                )

                if (message.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = if (isLoading) 0.2f else 0.4f)
                        )
                    ) {
                        Text(
                            dismissButtonText,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isLoading) 0.4f else 1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Button(
                        onClick = onConfirm,
                        enabled = !isLoading,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonColor,
                            contentColor = Color.White,
                            disabledContainerColor = buttonColor.copy(alpha = 0.5f),
                            disabledContentColor = Color.White.copy(alpha = 0.7f)
                        )
                    ) {
                        if (isLoading) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            confirmButtonText,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// === 3. FlowCashInputDialog (Budget / Text Input Form Dialog) ===
@Composable
fun FlowCashInputDialog(
    onDismissRequest: () -> Unit,
    title: String,
    subtitle: String = "",
    icon: ImageVector,
    inputValue: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "Enter value",
    prefixText: String = "",
    keyboardType: KeyboardType = KeyboardType.Number,
    confirmButtonText: String = "Save",
    dismissButtonText: String = "Cancel",
    onConfirm: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isCompactHeight = configuration.screenHeightDp < 520
    val imeBottomPadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val isImeOpen = imeBottomPadding > 0.dp

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth(0.92f)
                .imePadding()
                .padding(vertical = if (isCompactHeight || isImeOpen) 6.dp else 16.dp)
                .clip(RoundedCornerShape(28.dp))
                .border(
                    1.5.dp,
                    PrimaryIndigo.copy(alpha = 0.45f),
                    RoundedCornerShape(28.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isCompactHeight && isImeOpen) Modifier.verticalScroll(rememberScrollState()) else Modifier
                    )
                    .padding(if (isCompactHeight || isImeOpen) 14.dp else 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isCompactHeight || !isImeOpen) {
                    Box(
                        modifier = Modifier
                            .size(if (isCompactHeight) 40.dp else 60.dp)
                            .clip(CircleShape)
                            .background(PrimaryIndigo.copy(alpha = 0.12f))
                            .border(1.dp, PrimaryIndigo.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = PrimaryIndigo,
                            modifier = Modifier.size(if (isCompactHeight) 20.dp else 30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(if (isCompactHeight) 6.dp else 16.dp))
                }

                Text(
                    text = title,
                    style = (if (isCompactHeight || isImeOpen) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge).copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    textAlign = TextAlign.Center
                )

                if (subtitle.isNotBlank() && (!isCompactHeight || !isImeOpen)) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(if (isCompactHeight || isImeOpen) 8.dp else 18.dp))

                OutlinedTextField(
                    value = inputValue,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    placeholder = { Text(placeholder) },
                    prefix = if (prefixText.isNotBlank()) {
                        { Text(prefixText, fontWeight = FontWeight.Bold) }
                    } else null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryIndigo,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    )
                )

                Spacer(modifier = Modifier.height(if (isCompactHeight || isImeOpen) 10.dp else 24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .weight(1f)
                            .height(if (isCompactHeight || isImeOpen) 42.dp else 48.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                    ) {
                        Text(
                            dismissButtonText,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(if (isCompactHeight || isImeOpen) 42.dp else 48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryIndigo,
                            contentColor = Color.White
                        )
                    ) {
                        Text(confirmButtonText, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// === 4. FlowCashSelectionDialog (Currency Picker & Selection Grids) ===
data class CurrencyOptionItem(
    val symbol: String,
    val code: String,
    val name: String
)

@Composable
fun FlowCashSelectionDialog(
    onDismissRequest: () -> Unit,
    title: String,
    icon: ImageVector,
    options: List<CurrencyOptionItem>,
    selectedCode: String,
    onOptionSelected: (CurrencyOptionItem) -> Unit
) {
    val configuration = LocalConfiguration.current
    val isCompactHeight = configuration.screenHeightDp < 520

    val listState = rememberLazyListState()
    val selectedIndex = remember(options, selectedCode) {
        options.indexOfFirst { it.code.equals(selectedCode, ignoreCase = true) }
    }

    LaunchedEffect(selectedIndex, isCompactHeight) {
        if (selectedIndex >= 0) {
            val targetIndex = if (isCompactHeight) selectedIndex + 1 else selectedIndex
            listState.scrollToItem(targetIndex.coerceAtLeast(0))
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth(0.92f)
                .padding(vertical = if (isCompactHeight) 12.dp else 24.dp)
                .clip(RoundedCornerShape(28.dp))
                .border(
                    1.5.dp,
                    PrimaryIndigo.copy(alpha = 0.45f),
                    RoundedCornerShape(28.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            if (isCompactHeight) {
                // Compact Screen Height (< 520dp): Unified scrollable list where top header scrolls away to maximize height
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item(key = "header") {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryIndigo.copy(alpha = 0.12f))
                                    .border(1.dp, PrimaryIndigo.copy(alpha = 0.3f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = title,
                                    tint = PrimaryIndigo,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }

                    items(options, key = { it.code }) { item ->
                        CurrencyOptionRowItem(
                            item = item,
                            isSelected = item.code.equals(selectedCode, ignoreCase = true),
                            onOptionSelected = onOptionSelected,
                            onDismissRequest = onDismissRequest
                        )
                    }

                    item(key = "footer") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedButton(
                                onClick = onDismissRequest,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp),
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                )
                            ) {
                                Text("Close", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // Portrait: Fixed Header + Pinned Currency List (capped height) + Fixed Close Button
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(PrimaryIndigo.copy(alpha = 0.12f))
                            .border(1.dp, PrimaryIndigo.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = PrimaryIndigo,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(options, key = { it.code }) { item ->
                            CurrencyOptionRowItem(
                                item = item,
                                isSelected = item.code.equals(selectedCode, ignoreCase = true),
                                onOptionSelected = onOptionSelected,
                                onDismissRequest = onDismissRequest
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                    ) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrencyOptionRowItem(
    item: CurrencyOptionItem,
    isSelected: Boolean,
    onOptionSelected: (CurrencyOptionItem) -> Unit,
    onDismissRequest: () -> Unit
) {
    val itemBg = if (isSelected) {
        PrimaryIndigo.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }
    val itemBorder = if (isSelected) {
        PrimaryIndigo
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(itemBg)
            .border(1.dp, itemBorder, RoundedCornerShape(16.dp))
            .clickable {
                onOptionSelected(item)
                onDismissRequest()
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.primary.copy(
                            alpha = 0.1f
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.symbol,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else PrimaryIndigo
                    )
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "${item.code} (${item.symbol})",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = PrimaryIndigo,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
