package com.example.syncspend.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.syncspend.MainViewModel
import com.example.syncspend.data.TransactionType
import com.example.syncspend.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val entities by viewModel.entities.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedEntityId by remember { mutableStateOf<Long?>(null) }

    if (selectedEntityId != null) {
        val entity = entities.find { it.id == selectedEntityId }
        if (entity != null) {
            PersonDetailScreen(
                viewModel = viewModel,
                entityId = entity.id,
                entityName = entity.name,
                currentDebt = entity.currentDebt,
                onBack = { selectedEntityId = null }
            )
            return
        }
    }

    Scaffold(
        containerColor = BackgroundGrey,
        topBar = {
            TopAppBar(
                title = { Text("People & Debts", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Person")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundGrey)
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            item { Spacer(Modifier.height(8.dp)) }
            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(1.dp)) {
                    Column {
                        entities.forEachIndexed { index, entity ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { selectedEntityId = entity.id }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(entity.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                    if (entity.currentDebt > 0) {
                                        Text("Owes you ₹${String.format("%.2f", entity.currentDebt)}",
                                            style = MaterialTheme.typography.bodySmall, color = LentOrange)
                                    } else if (entity.currentDebt < 0) {
                                        Text("You owe ₹${String.format("%.2f", -entity.currentDebt)}",
                                            style = MaterialTheme.typography.bodySmall, color = DestructiveRed)
                                    } else {
                                        Text("Settled", style = MaterialTheme.typography.bodySmall, color = IncomeGreen)
                                    }
                                }
                                IconButton(onClick = { viewModel.deleteEntity(entity) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DestructiveRed, modifier = Modifier.size(20.dp))
                                }
                            }
                            if (index < entities.size - 1)
                                HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp, color = DividerGrey)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddNewDialog(
            title = "New Person",
            onConfirm = { name -> viewModel.addEntity(name); showAddDialog = false },
            onDismiss = { showAddDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDetailScreen(viewModel: MainViewModel, entityId: Long, entityName: String, currentDebt: Double, onBack: () -> Unit) {
    val transactions by viewModel.getTransactionsByEntity(entityId).collectAsState(initial = emptyList())
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    Scaffold(
        containerColor = BackgroundGrey,
        topBar = {
            TopAppBar(
                title = { Text(entityName, fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundGrey)
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Current Balance", style = MaterialTheme.typography.bodySmall, color = SecondaryText)
                        Text(
                            if (currentDebt >= 0) "Owes you ₹${String.format("%.2f", currentDebt)}"
                            else "You owe ₹${String.format("%.2f", -currentDebt)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (currentDebt >= 0) LentOrange else DestructiveRed
                        )
                    }
                }
            }
            item { Text("Transactions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(vertical = 8.dp)) }
            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                    Column {
                        transactions.forEachIndexed { index, tx ->
                            val color = when (tx.type) {
                                TransactionType.LENT -> LentOrange
                                TransactionType.REPAYMENT -> IncomeGreen
                                else -> PrimaryText
                            }
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(tx.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                    Text(dateFormat.format(Date(tx.dateMs)), style = MaterialTheme.typography.bodySmall, color = SecondaryText)
                                }
                                Text(
                                    "${if (tx.type == TransactionType.REPAYMENT) "+" else "-"}₹${String.format("%.2f", tx.amount)}",
                                    color = color, fontWeight = FontWeight.SemiBold
                                )
                            }
                            if (index < transactions.size - 1)
                                HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp, color = DividerGrey)
                        }
                        if (transactions.isEmpty()) {
                            Text("No transactions yet", modifier = Modifier.padding(16.dp), color = SecondaryText)
                        }
                    }
                }
            }
        }
    }
}
