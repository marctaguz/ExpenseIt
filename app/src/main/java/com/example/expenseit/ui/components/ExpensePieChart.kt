package com.example.expenseit.ui.components

import android.animation.ValueAnimator
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.utils.ViewPortHandler

@Composable
fun ExpensePieChart(entries: List<PieEntry>) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(top = 24.dp)
            .padding(16.dp),
        factory = { context ->
            val pieChart = PieChart(context)

            val dataSet = PieDataSet(entries, "").apply {
                setDrawValues(true)
                valueTextSize = 14f
                valueTextColor = Color.Black.toArgb()
                sliceSpace = 3f
                selectionShift = 12f
                colors = listOf(
                    Color(0xFF00BFA6).toArgb(),
                    Color(0xFFFFA000).toArgb(),
                    Color(0xFF1976D2).toArgb(),
                    Color(0xFFE53935).toArgb(),
                    Color(0xFF8E24AA).toArgb()
                )

                valueFormatter = object : DefaultValueFormatter(2), IValueFormatter {
                    override fun getFormattedValue(
                        value: Float,
                        entry: com.github.mikephil.charting.data.Entry?,
                        dataSetIndex: Int,
                        viewPortHandler: ViewPortHandler?
                    ): String? {
                        return "%.1f%%".format(value)
                    }
                }
            }

            val legend = pieChart.legend
            legend.isEnabled = true
            legend.textSize = 14f
            legend.form = Legend.LegendForm.CIRCLE
            legend.orientation = Legend.LegendOrientation.HORIZONTAL
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM

            pieChart.data = PieData(dataSet)
            pieChart.setDrawEntryLabels(false)
            pieChart.legend.isEnabled = true
            pieChart.description.isEnabled = false
            pieChart.setUsePercentValues(true)
            pieChart.setEntryLabelColor(Color.Black.toArgb())
            pieChart.animateY(1000)

            // animate selection shift when a slice is selected
            pieChart.setOnChartValueSelectedListener(object : com.github.mikephil.charting.listener.OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: com.github.mikephil.charting.highlight.Highlight?) {
                    val animator = ValueAnimator.ofFloat(dataSet.selectionShift, 12f)
                    animator.duration = 300
                    animator.addUpdateListener { animation ->
                        dataSet.selectionShift = animation.animatedValue as Float
                        pieChart.invalidate()
                    }
                    animator.start()
                }

                override fun onNothingSelected() {
                    val animator = ValueAnimator.ofFloat(dataSet.selectionShift, 0f)
                    animator.duration = 300
                    animator.addUpdateListener { animation ->
                        dataSet.selectionShift = animation.animatedValue as Float
                        pieChart.invalidate()
                    }
                    animator.start()
                }
            })

            dataSet.setDrawValues(true)
            dataSet.setValueTextColor(Color.Black.toArgb())
            dataSet.valueTextSize = 14f
            dataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            dataSet.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            dataSet.valueLinePart1Length = 0.6f
            dataSet.valueLinePart2Length = 0.4f
            dataSet.valueLineColor = Color.Gray.toArgb()
//            dataSet.valueFormatter = object : com.github.mikephil.charting.formatter.DefaultValueFormatter(2),
//                IValueFormatter {
//                override fun getFormattedValue(value: Float, entry: Entry?, dataSetIndex: Int, viewPortHandler: ViewPortHandler?): String? {
//                    return "$%.2f".format(value)
//                }
//            }
//            pieChart.holeRadius = 45f
//            pieChart.transparentCircleRadius = 50f
//            pieChart.setHoleColor(Color.White.toArgb())
//            pieChart.centerText = "Spending"
//            pieChart.setCenterTextSize(18f)

            pieChart
        },
        update = { pieChart ->
            (pieChart.data?.dataSet as? PieDataSet)?.let { dataSet ->
                dataSet.values = entries
                pieChart.data?.notifyDataChanged()
                pieChart.notifyDataSetChanged()
                pieChart.invalidate()
                pieChart.animateY(1000)
            }
        }
    )
}

