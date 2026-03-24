package com.example.syncspend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.syncspend.data.AppDatabase
import com.example.syncspend.data.SyncSpendRepository
import com.example.syncspend.ui.screens.*
import com.example.syncspend.ui.theme.SyncSpendTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val db = AppDatabase.getInstance(applicationContext)
        val repo = SyncSpendRepository(db)

        setContent {
            SyncSpendTheme {
                val viewModel: MainViewModel = viewModel(factory = MainViewModel.Factory(repo))
                SyncSpendApp(viewModel)
            }
        }
    }
}

enum class Screen { DASHBOARD, SETTINGS, PEOPLE, REPORTS }

@Composable
fun SyncSpendApp(viewModel: MainViewModel) {
    var currentScreen by remember { mutableStateOf(Screen.DASHBOARD) }
    var showAddSheet by remember { mutableStateOf(false) }

    when (currentScreen) {
        Screen.DASHBOARD -> DashboardScreen(
            viewModel = viewModel,
            onAddTransaction = { showAddSheet = true },
            onSettings = { currentScreen = Screen.SETTINGS },
            onPeople = { currentScreen = Screen.PEOPLE },
            onReports = { currentScreen = Screen.REPORTS }
        )
        Screen.SETTINGS -> SettingsScreen(viewModel = viewModel, onBack = { currentScreen = Screen.DASHBOARD })
        Screen.PEOPLE -> PeopleScreen(viewModel = viewModel, onBack = { currentScreen = Screen.DASHBOARD })
        Screen.REPORTS -> ReportsScreen(viewModel = viewModel, onBack = { currentScreen = Screen.DASHBOARD })
    }

    if (showAddSheet) {
        AddTransactionSheet(
            viewModel = viewModel,
            onDismiss = { showAddSheet = false }
        )
    }
}
