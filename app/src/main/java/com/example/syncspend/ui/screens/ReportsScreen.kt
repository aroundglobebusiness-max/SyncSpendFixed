package com.example.syncspend.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.syncspend.MainViewModel
import com.example.syncspend.data.TransactionType
import com.example.syncspend.ui.theme.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val transactions by viewModel.transactions.collectAsState()
    var selectedPeriod by remember { mutableIntStateOf(0) }
    val periods = listOf("Weekly", "Monthly", "All Time")

    val now = Calendar.getInstance()
    val filteredTx = when (selectedPeriod) {
        0 -> {
            val weekAgo = now.clone() as Calendar
            weekAgo.add(Calendar.DAY_OF_YEAR, -7)
            transactions.filter { it.dateMs >= weekAgo.timeInMillis }
        }
        1 -> {
            val monthAgo = now.clone() as Calendar
            monthAgo.add(Calendar.MONTH, -1)
            transactions.filter { it.dateMs >= monthAgo.timeInMillis }
        }
        else -> transactions
    }

    val totalExpense = filteredTx.filter { it.type == TransactionType.PERSONAL_EXPENSE }.sumOf { it.amount }
    val totalLent = filteredTx.filter { it.type == TransactionType.LENT }.sumOf { it.amount }
    val totalReceived = filteredTx.filter { it.type == TransactionType.REPAYMENT }.sumOf { it.amount }

    Scaffold(
        containerColor = BackgroundGrey,
        topBar = {
            TopAppBar(
                title = { Text("Reports", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundGrey)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Period selector
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    periods.forEachIndexed { index, label ->
                        FilterChip(
                            selected = selectedPeriod == index,
                            onClick = { selectedPeriod = index },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentBlue,
                                selectedLabelColor = CardWhite
                            )
                        )
                    }
                }
            }

            // Summary cards
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryCard(modifier = Modifier.weight(1f), label = "Spent", amount = totalExpense, color = PrimaryText)
                    SummaryCard(modifier = Modifier.weight(1f), label = "Lent", amount = totalLent, color = LentOrange)
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryCard(modifier = Modifier.weight(1f), label = "Received", amount = totalReceived, color = IncomeGreen)
                    SummaryCard(modifier = Modifier.weight(1f), label = "Net", amount = totalReceived - totalExpense - totalLent,
                        color = if (totalReceived - totalExpense - totalLent >= 0) IncomeGreen else DestructiveRed)
                }
            }

            // Top expenses
            item {
                Text("Top Expenses", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp))
            }
            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(1.dp)) {
                    Column {
                        val topExpenses = filteredTx.filter { it.type == TransactionType.PERSONAL_EXPENSE }
                            .sortedByDescending { it.amount }.take(5)
                        if (topExpenses.isEmpty()) {
                            Text("No expenses in this period", modifier = Modifier.padding(16.dp), color = SecondaryText)
                        } else {
                            topExpenses.forEachIndexed { index, tx ->
                                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(tx.title, style = MaterialTheme.typography.bodyLarge)
                                    Text("-₹${String.format("%.2f", tx.amount)}", color = PrimaryText, fontWeight = FontWeight.SemiBold)
                                }
                                if (index < topExpenses.size - 1)
                                    HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp, color = DividerGrey)
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun SummaryCard(modifier: Modifier = Modifier, label: String, amount: Double, color: androidx.compose.ui.graphics.Color) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(1.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = SecondaryText)
            Spacer(Modifier.height(4.dp))
            Text("₹${String.format("%.2f", amount)}", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, color = color)
        }
    }
}
