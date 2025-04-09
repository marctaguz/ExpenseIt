package com.example.expenseit.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.expenseit.ui.components.PageHeader
import com.example.expenseit.ui.viewmodels.ExpenseViewModel
import com.example.expenseit.ui.viewmodels.StatsViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expenseit.ui.components.ExpenseLineChart
import com.example.expenseit.ui.components.ExpensePieChart
import com.example.expenseit.ui.theme.backgroundLight
import com.example.expenseit.ui.viewmodels.SettingsViewModel
import com.github.mikephil.charting.data.PieEntry
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ExpenseStatsScreen(navController: NavController, modifier: Modifier, expenseViewModel: ExpenseViewModel = hiltViewModel()) {
    val statsViewModel: StatsViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val currency by settingsViewModel.currency.collectAsStateWithLifecycle()
    val monthlyData by statsViewModel.monthlyExpenses.collectAsState(emptyList())
    val dataState by statsViewModel.entries.collectAsState(emptyList())
    val headerHeight = 160.dp
    val overlapHeight = 86.dp
    val currentMonthString = remember {
        // For example, using SimpleDateFormat (be aware of time zones)
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        sdf.format(Date())
    }
    var selectedMonth by remember { mutableStateOf(currentMonthString) }

    val categoryTotalsForMonth by remember(selectedMonth) {
        statsViewModel.getCategoryTotalsForMonth(selectedMonth)
    }.collectAsState(initial = emptyList())

    val pieEntries = categoryTotalsForMonth.map { PieEntry(it.total.toFloat(), it.categoryName) }

    val categoryTotals by statsViewModel
        .getCategoryTotalsForMonth(selectedMonth.toString())
        .collectAsStateWithLifecycle(emptyList())

    val displayMonth = try {
        val sdfInput = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val sdfOutput = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        sdfOutput.format(sdfInput.parse(selectedMonth)!!)
    } catch (e: Exception) { selectedMonth }

    fun adjustMonth(currentMonth: String, decrement: Boolean): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.time = sdf.parse(currentMonth) ?: Date()
        if (decrement) {
            calendar.add(Calendar.MONTH, -1)
        } else {
            calendar.add(Calendar.MONTH, 1)
        }
        return sdf.format(calendar.time)
    }

    Scaffold(
        topBar = {
            // This header shows the navigation for months.

        },
        content = { innerPadding ->

            Box(modifier = Modifier.fillMaxSize()) {
                // Header
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxWidth()
                        .height(headerHeight)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    PageHeader(title = "Statistics")
                }

                // Content
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = headerHeight - overlapHeight)
                        .clip(RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp)),
                    colors = CardDefaults.cardColors(backgroundLight),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { selectedMonth = adjustMonth(selectedMonth, decrement = true)
                        }) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowLeft,
                                contentDescription = "Previous Month"
                            )
                        }
                        Text(
                            text = displayMonth,
                            textAlign = TextAlign.Center,
                            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { selectedMonth = adjustMonth(selectedMonth, decrement = false) }) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = "Next Month"
                            )
                        }
                    }

                    Column(Modifier.padding(innerPadding).fillMaxSize()) {
                        val (currentMonth, lastMonth) = statsViewModel.getMonthlyComparison(monthlyData)

//                        ExpenseComparisonCard(currentMonth, lastMonth, currency)

//                        val categoryData by statsViewModel.categoryTotals.collectAsState(emptyList())
//                        val pieEntries = statsViewModel.getEntriesForPieChart(categoryData)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .height(300.dp),
                            colors = CardDefaults.cardColors(Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            if (pieEntries.isNotEmpty()) {
                                ExpensePieChart(entries = pieEntries)
                            } else {
                                // When no pie data, show a placeholder
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No data available",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        // Category totals list card
                        if (categoryTotalsForMonth.isNotEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                colors = CardDefaults.cardColors(Color.White),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(modifier = Modifier.padding(vertical = 0.dp)) {
                                    categoryTotalsForMonth.forEachIndexed { index, categoryTotal ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = categoryTotal.categoryName)
                                            Text(
                                                text = "$currency ${String.format("%.2f", categoryTotal.total.toFloat())}",
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                            )
                                        }
                                        if (index < categoryTotalsForMonth.size - 1) {
                                            HorizontalDivider(
                                                modifier = Modifier.padding(vertical = 4.dp),
                                                color = Color(0xFFE5E7EB)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        val entries = statsViewModel.getEntriesForChart(dataState)
                        val monthLabels = statsViewModel.getMonthLabels(dataState)
//                        if (entries.isNotEmpty()) {
//                            Card(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(16.dp)
//                                    .height(300.dp),
//                                colors = CardDefaults.cardColors(Color.White),
//                                elevation = CardDefaults.cardElevation(2.dp)
//                            ) {
////                                ExpenseLineChart(entries, monthLabels, currency)
//                            }
//                        } else {
//                            Text("No data available", Modifier.padding(16.dp))
//                        }
                    }
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
        percentageChange > 0 -> Color.Red  // Spending increIased
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





