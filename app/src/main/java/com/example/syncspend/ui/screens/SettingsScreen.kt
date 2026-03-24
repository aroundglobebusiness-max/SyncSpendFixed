package com.example.syncspend.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.syncspend.MainViewModel
import com.example.syncspend.data.Account
import com.example.syncspend.data.AccountType
import com.example.syncspend.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val accounts by viewModel.accounts.collectAsState()
    val gateways by viewModel.gateways.collectAsState()
    var biometricEnabled by remember { mutableStateOf(false) }
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var showAddGatewayDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundGrey,
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundGrey)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Security section
            item {
                SectionHeader("Security")
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(1.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Biometric Lock", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Text("Use fingerprint to unlock app", style = MaterialTheme.typography.bodySmall, color = SecondaryText)
                        }
                        Switch(checked = biometricEnabled, onCheckedChange = { biometricEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = CardWhite, checkedTrackColor = IncomeGreen))
                    }
                }
            }

            // Accounts section
            item {
                SectionHeader("Accounts")
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(1.dp)) {
                    Column {
                        accounts.forEachIndexed { index, account ->
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(account.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                    Text("${account.type.name} • ₹${String.format("%.2f", account.currentBalance)}",
                                        style = MaterialTheme.typography.bodySmall, color = SecondaryText)
                                }
                                IconButton(onClick = { viewModel.deleteAccount(account) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DestructiveRed, modifier = Modifier.size(20.dp))
                                }
                            }
                            if (index < accounts.size - 1)
                                HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp, color = DividerGrey)
                        }
                        HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp, color = DividerGrey)
                        Row(modifier = Modifier.fillMaxWidth().clickable { showAddAccountDialog = true }
                            .padding(horizontal = 16.dp, vertical = 16.dp)) {
                            Text("+ Add Account", color = AccentBlue, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Payment Gateways section
            item {
                SectionHeader("Payment Apps")
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(1.dp)) {
                    Column {
                        gateways.forEachIndexed { index, gateway ->
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(gateway.name, style = MaterialTheme.typography.bodyLarge)
                                IconButton(onClick = { viewModel.deleteGateway(gateway) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DestructiveRed, modifier = Modifier.size(20.dp))
                                }
                            }
                            if (index < gateways.size - 1)
                                HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp, color = DividerGrey)
                        }
                        HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp, color = DividerGrey)
                        Row(modifier = Modifier.fillMaxWidth().clickable { showAddGatewayDialog = true }
                            .padding(horizontal = 16.dp, vertical = 16.dp)) {
                            Text("+ Add Payment App", color = AccentBlue, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Database section
            item {
                SectionHeader("Database & Export")
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(1.dp)) {
                    Column {
                        SettingsRow(title = "Sync with Notion", subtitle = "Connect your Notion database") {}
                        HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp, color = DividerGrey)
                        SettingsRow(title = "Export as CSV", subtitle = "Download all transactions") {}
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }

    if (showAddAccountDialog) {
        AddNewDialog(
            title = "New Account",
            onConfirm = { name ->
                viewModel.addAccount(name, AccountType.SAVINGS, 0.0, 0.0)
                showAddAccountDialog = false
            },
            onDismiss = { showAddAccountDialog = false }
        )
    }

    if (showAddGatewayDialog) {
        AddNewDialog(
            title = "New Payment App",
            onConfirm = { name -> viewModel.addGateway(name); showAddGatewayDialog = false },
            onDismiss = { showAddGatewayDialog = false }
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(title.uppercase(), style = MaterialTheme.typography.labelSmall, color = SecondaryText,
        modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 6.dp))
}

@Composable
fun SettingsRow(title: String, subtitle: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = SecondaryText)
        }
        Text("›", style = MaterialTheme.typography.titleLarge, color = SecondaryText)
    }
}
