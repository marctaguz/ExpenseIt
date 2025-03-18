package com.example.expenseit.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.expenseit.ui.components.PageHeader
import com.example.expenseit.ui.viewmodels.ExpenseViewModel
import com.example.expenseit.ui.viewmodels.StatsViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expenseit.ui.components.ExpenseLineChart
import com.example.expenseit.ui.viewmodels.SettingsViewModel

@Composable
fun ExpenseStatsScreen(navController: NavController, modifier: Modifier, expenseViewModel: ExpenseViewModel = hiltViewModel()) {
    val statsViewModel: StatsViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val currency by settingsViewModel.currency.collectAsStateWithLifecycle()
    val monthlyData by statsViewModel.monthlyExpenses.collectAsState(emptyList())

    Scaffold(
        topBar = {
            PageHeader(title = "Expense Stats Screen", actionButtonVisible = false)
        },
        content = { innerPadding ->
            val dataState by statsViewModel.entries.collectAsState(emptyList())

            Column(Modifier.padding(innerPadding).fillMaxSize()) {
                val (currentMonth, lastMonth) = statsViewModel.getMonthlyComparison(monthlyData)

                ExpenseComparisonCard(currentMonth, lastMonth, currency)

                val entries = statsViewModel.getEntriesForChart(dataState)
                val monthLabels = statsViewModel.getMonthLabels(dataState)
                if (entries.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(300.dp),
                        colors = CardDefaults.cardColors(Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        ExpenseLineChart(entries, monthLabels, currency)
                    }
                } else {
                    Text("No data available", Modifier.padding(16.dp))
                }
            }
        }
    )
}

@Composable
fun ExpenseComparisonCard(currentMonthTotal: Float, lastMonthTotal: Float, currency: String) {
    val percentageChange = if (lastMonthTotal > 0) {
        ((currentMonthTotal - lastMonthTotal) / lastMonthTotal) * 100
    } else {
        0f
    }

    val changeColor = when {
        percentageChange > 0 -> Color.Red  // Spending increased
        percentageChange < 0 -> Color.Green // Spending decreased
        else -> Color.Gray // No change
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("📅 This Month: $currency%.2f".format(currentMonthTotal))
            Text("📅 Last Month: $currency%.2f".format(lastMonthTotal))
            Text("📊 Change: %.2f%%".format(percentageChange), color = changeColor)
        }
    }
}





