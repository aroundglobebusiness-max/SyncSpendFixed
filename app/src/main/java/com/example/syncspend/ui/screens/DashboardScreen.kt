package com.example.syncspend.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.syncspend.MainViewModel
import com.example.syncspend.data.AccountType
import com.example.syncspend.data.TransactionType
import com.example.syncspend.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onAddTransaction: () -> Unit,
    onSettings: () -> Unit,
    onPeople: () -> Unit,
    onReports: () -> Unit
) {
    val accounts by viewModel.accounts.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val netWorth by viewModel.netWorth.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    Scaffold(
        containerColor = BackgroundGrey,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransaction,
                shape = CircleShape,
                containerColor = Color.Black,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Portfolio Overview", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = {}) { Icon(Icons.Default.Search, contentDescription = "Search") }
                        IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, contentDescription = "Settings") }
                    }
                }
            }

            // Net Worth
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Net Worth", style = MaterialTheme.typography.bodyMedium, color = SecondaryText)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        currencyFormat.format(netWorth),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Black,
                        color = PrimaryText
                    )
                }
            }

            // Accounts horizontal scroll
            item {
                Text("Accounts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(accounts) { account ->
                        Card(
                            modifier = Modifier.width(180.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(account.name, style = MaterialTheme.typography.bodySmall, color = SecondaryText, maxLines = 1)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    currencyFormat.format(account.currentBalance),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (account.currentBalance < 0) DestructiveRed else PrimaryText
                                )
                                if (account.type == AccountType.CREDIT) {
                                    Spacer(Modifier.height(4.dp))
                                    Text("Limit: ${currencyFormat.format(account.creditLimit)}", style = MaterialTheme.typography.bodySmall, color = SecondaryText)
                                }
                            }
                        }
                    }
                }
            }

            // Recent Transactions header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recent Transactions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    TextButton(onClick = onReports) { Text("See Reports", color = AccentBlue) }
                }
            }

            // Transactions list
            if (transactions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No Transactions Yet", style = MaterialTheme.typography.titleMedium, color = SecondaryText)
                            Spacer(Modifier.height(8.dp))
                            Text("Tap + to add your first entry", style = MaterialTheme.typography.bodySmall, color = SecondaryText)
                        }
                    }
                }
            } else {
                item {
                    Card(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        transactions.forEachIndexed { index, tx ->
                            val amountColor = when (tx.type) {
                                TransactionType.PERSONAL_EXPENSE -> PrimaryText
                                TransactionType.LENT -> LentOrange
                                TransactionType.REPAYMENT -> IncomeGreen
                            }
                            val amountPrefix = when (tx.type) {
                                TransactionType.REPAYMENT -> "+"
                                else -> "-"
                            }
                            val subtitle = buildString {
                                viewModel.entityName(tx.entityId).takeIf { it.isNotEmpty() }?.let { append("$it • ") }
                                append(viewModel.accountName(tx.accountId))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Circle icon
                                Box(
                                    modifier = Modifier.size(40.dp).clip(CircleShape).background(BackgroundGrey),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(tx.title.take(1).uppercase(), fontWeight = FontWeight.Bold, color = SecondaryText)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(tx.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = SecondaryText)
                                    Text(
                                        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(tx.dateMs)),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SecondaryText
                                    )
                                }
                                Text(
                                    "$amountPrefix₹${String.format("%.2f", tx.amount)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = amountColor
                                )
                            }
                            if (index < transactions.size - 1) {
                                HorizontalDivider(modifier = Modifier.padding(start = 68.dp), thickness = 0.5.dp, color = DividerGrey)
                            }
                        }
                    }
                }
            }

            // People section
            item {
                Spacer(Modifier.height(16.dp))
                TextButton(
                    onClick = onPeople,
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    Text("View People & Debts →", color = AccentBlue)
                }
            }
        }
    }
}
