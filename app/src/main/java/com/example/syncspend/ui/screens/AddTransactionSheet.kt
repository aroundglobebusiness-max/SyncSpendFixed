package com.example.syncspend.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.syncspend.MainViewModel
import com.example.syncspend.data.*
import com.example.syncspend.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val accounts by viewModel.accounts.collectAsState()
    val entities by viewModel.entities.collectAsState()
    val gateways by viewModel.gateways.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("EXPENSE", "LENT", "RECEIVED")

    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var selectedAccount by remember { mutableStateOf<Account?>(null) }
    var selectedEntity by remember { mutableStateOf<EntityPerson?>(null) }
    var selectedGateway by remember { mutableStateOf<Gateway?>(null) }

    // Nested sheet state
    var showAccountPicker by remember { mutableStateOf(false) }
    var showEntityPicker by remember { mutableStateOf(false) }
    var showGatewayPicker by remember { mutableStateOf(false) }

    // Add new dialogs
    var showAddAccount by remember { mutableStateOf(false) }
    var showAddEntity by remember { mutableStateOf(false) }
    var showAddGateway by remember { mutableStateOf(false) }
    var newItemName by remember { mutableStateOf("") }

    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    fun save() {
        val amt = amount.toDoubleOrNull() ?: return
        val account = selectedAccount ?: return
        val type = when (selectedTab) {
            0 -> TransactionType.PERSONAL_EXPENSE
            1 -> TransactionType.LENT
            else -> TransactionType.REPAYMENT
        }
        if (selectedTab == 1 && selectedEntity == null) return
        if (selectedTab == 2 && selectedEntity == null) return

        viewModel.addTransaction(
            Transaction(
                type = type,
                title = if (selectedTab == 2) "Received from ${selectedEntity?.name ?: ""}" else title,
                amount = amt,
                dateMs = selectedDate,
                entityId = selectedEntity?.id,
                accountId = account.id,
                gatewayId = selectedGateway?.id,
                notes = notes
            )
        )
        onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SheetBackground,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.92f).verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) { Text("Cancel", color = AccentBlue) }
                Text("Add Entry", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                TextButton(onClick = { save() }) { Text("Save", color = AccentBlue, fontWeight = FontWeight.SemiBold) }
            }

            // Tab selector
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(10.dp)).background(DividerGrey),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                tabs.forEachIndexed { index, label ->
                    Box(
                        modifier = Modifier.weight(1f).padding(3.dp).clip(RoundedCornerShape(8.dp))
                            .background(if (selectedTab == index) CardWhite else Color.Transparent)
                            .clickable { selectedTab = index }.padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, fontSize = 13.sp, fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selectedTab == index) PrimaryText else SecondaryText)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Input fields
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                Column {
                    if (selectedTab != 2) {
                        OutlinedTextField(
                            value = title, onValueChange = { title = it },
                            placeholder = { Text("Title", color = SecondaryText) },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent
                            )
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp, color = DividerGrey)
                    }
                    OutlinedTextField(
                        value = amount, onValueChange = { amount = it },
                        placeholder = { Text("Amount (₹)", color = SecondaryText) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent
                        )
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp, color = DividerGrey)
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Date", style = MaterialTheme.typography.bodyLarge)
                        Text(dateFormat.format(Date(selectedDate)), color = SecondaryText)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Routing fields
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                Column {
                    if (selectedTab == 1 || selectedTab == 2) {
                        PickerRow(
                            label = if (selectedTab == 1) "Paid For" else "Received From",
                            value = selectedEntity?.name ?: "Select Person",
                            onClick = { showEntityPicker = true }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 68.dp), thickness = 0.5.dp, color = DividerGrey)
                    }
                    PickerRow(
                        label = if (selectedTab == 2) "Deposited To" else "Paid From",
                        value = selectedAccount?.name ?: "Select Account",
                        onClick = { showAccountPicker = true }
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 68.dp), thickness = 0.5.dp, color = DividerGrey)
                    PickerRow(
                        label = "Via App",
                        value = selectedGateway?.name ?: "Select Gateway",
                        onClick = { showGatewayPicker = true }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Notes
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                OutlinedTextField(
                    value = notes, onValueChange = { notes = it },
                    placeholder = { Text("Notes (optional)", color = SecondaryText) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    )
                )
            }
            Spacer(Modifier.height(32.dp))
        }
    }

    // Account Picker
    if (showAccountPicker) {
        PickerSheet(
            title = "Select Account",
            items = accounts.map { it.name },
            onSelect = { idx -> selectedAccount = accounts[idx]; showAccountPicker = false },
            onDismiss = { showAccountPicker = false },
            onAddNew = { showAccountPicker = false; showAddAccount = true }
        )
    }

    // Entity Picker
    if (showEntityPicker) {
        PickerSheet(
            title = "Select Person",
            items = entities.map { it.name },
            onSelect = { idx -> selectedEntity = entities[idx]; showEntityPicker = false },
            onDismiss = { showEntityPicker = false },
            onAddNew = { showEntityPicker = false; showAddEntity = true }
        )
    }

    // Gateway Picker
    if (showGatewayPicker) {
        PickerSheet(
            title = "Select Payment App",
            items = gateways.map { it.name },
            onSelect = { idx -> selectedGateway = gateways[idx]; showGatewayPicker = false },
            onDismiss = { showGatewayPicker = false },
            onAddNew = { showGatewayPicker = false; showAddGateway = true }
        )
    }

    // Add New dialogs
    if (showAddAccount) {
        AddNewDialog(
            title = "New Account",
            onConfirm = { name ->
                viewModel.addAccount(name, AccountType.SAVINGS, 0.0, 0.0)
                showAddAccount = false
            },
            onDismiss = { showAddAccount = false }
        )
    }
    if (showAddEntity) {
        AddNewDialog(
            title = "New Person",
            onConfirm = { name -> viewModel.addEntity(name); showAddEntity = false },
            onDismiss = { showAddEntity = false }
        )
    }
    if (showAddGateway) {
        AddNewDialog(
            title = "New Payment App",
            onConfirm = { name -> viewModel.addGateway(name); showAddGateway = false },
            onDismiss = { showAddGateway = false }
        )
    }
}

@Composable
fun PickerRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(value, color = SecondaryText, style = MaterialTheme.typography.bodyMedium)
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = SecondaryText, modifier = Modifier.size(14.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickerSheet(title: String, items: List<String>, onSelect: (Int) -> Unit, onDismiss: () -> Unit, onAddNew: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SheetBackground,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 12.dp))
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                Column {
                    items.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { onSelect(index) }.padding(horizontal = 16.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(item, style = MaterialTheme.typography.bodyLarge)
                        }
                        if (index < items.size - 1)
                            HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp, color = DividerGrey)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            // Add New button
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable { onAddNew() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                    Text("+ Add New", color = AccentBlue, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
fun AddNewDialog(title: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(value = text, onValueChange = { text = it }, placeholder = { Text("Enter name") })
        },
        confirmButton = {
            TextButton(onClick = { if (text.isNotBlank()) onConfirm(text.trim()) }) {
                Text("Add", color = AccentBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
