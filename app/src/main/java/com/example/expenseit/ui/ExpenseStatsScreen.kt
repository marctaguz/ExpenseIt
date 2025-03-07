package com.example.expenseit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.expenseit.ui.components.PageHeader
import com.example.expenseit.ui.viewmodels.ExpenseViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.shader.verticalGradient
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.shader.ShaderProvider
import java.math.BigDecimal

@Composable
fun ExpenseStatsScreen(navController: NavController, modifier: Modifier, expenseViewModel: ExpenseViewModel = hiltViewModel()) {
    var selectedChart by remember { mutableStateOf("line") }

    Scaffold(
        topBar = {
            PageHeader(title = "Expense Stats Screen", actionButtonVisible = false)
        },
        content = { innerPadding ->
            Column(Modifier
                .padding(innerPadding)
                .fillMaxSize())
            {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Button(onClick = { selectedChart = "line" }) { Text("Trend") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { selectedChart = "bar" }) { Text("Category") }
                }

                when (selectedChart) {
                    "line" -> ExpenseLineChart(expenseViewModel)
                }
            }
        }
    )
}

@Composable
fun ExpenseLineChart(viewModel: ExpenseViewModel) {
    val expensesByDate by viewModel.getExpensesByDate().collectAsStateWithLifecycle(emptyMap())

    val xValues = expensesByDate.keys.sorted()
    val yValues = xValues.map { expensesByDate[it] ?: BigDecimal.ZERO }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(xValues, yValues) {
        modelProducer.runTransaction {
            lineSeries { series(xValues.map { it.toInt() }, yValues.map { it.toFloat() }) }
        }
    }

    CartesianChartHost(
        rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                        fill = LineCartesianLayer.LineFill.single(fill(Color.Blue)),
                        areaFill = LineCartesianLayer.AreaFill.single(
                            fill(
                                ShaderProvider.verticalGradient(
                                arrayOf(Color.Blue.copy(alpha = 0.4f), Color.Transparent)
                            ))
                        ),
                    )
                )
            ),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(),
        ),
        modelProducer,
        Modifier.height(224.dp)
    )
}


