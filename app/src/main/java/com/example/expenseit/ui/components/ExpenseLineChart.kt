package com.example.expenseit.ui.components

import android.content.Context
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import com.example.expenseit.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener

@Composable
fun ExpenseLineChart(entries: List<Entry>, monthLabels: List<String>, currency: String) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp),
        factory = { context ->
            val chart = LineChart(context)
            val dataset = LineDataSet(entries, "Expenses").apply {
                color = "#FF2F7E79".toColorInt()
                valueTextColor = "#FF2F7E79".toColorInt()
                lineWidth = 3f
                setDrawFilled(true)
                setDrawCircles(false)
                circleRadius = 6f
                setCircleColor("#FF2F7E79".toColorInt())
                circleHoleColor = "#FF2F7E79".toColorInt()
                mode = LineDataSet.Mode.CUBIC_BEZIER
                valueTextSize = 0f
                val drawable = ContextCompat.getDrawable(context, R.drawable.chart_gradient)
                drawable?.let { fillDrawable = it }

                highLightColor = "#E0E0E0".toColorInt()
                setDrawHorizontalHighlightIndicator(true)
                setDrawVerticalHighlightIndicator(true)
                highlightLineWidth = 1f
            }

            chart.xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(true)
                enableGridDashedLine(10f, 10f, 0f)
                gridColor = "#E0E0E0".toColorInt()
                granularity = 1f
                labelCount = monthLabels.size
                textSize = 18f
                valueFormatter =
                    object : com.github.mikephil.charting.formatter.DefaultValueFormatter(4),
                        IAxisValueFormatter {
                        override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                            val index = value.toInt()
                            return monthLabels.getOrElse(index) { "" }
                        }
                    }
            }

            chart.axisLeft.apply {
                setDrawAxisLine(false)
                setDrawLabels(false)
                setDrawGridLines(true)
                enableGridDashedLine(10f, 10f, 0f)
                gridColor = "#E0E0E0".toColorInt()
            }
            chart.axisRight.isEnabled = false

            val markerView = CustomMarkerView(context, currency)
            markerView.chartView = chart
            chart.marker = markerView
            chart.isHighlightPerTapEnabled = true

            chart.animateY(1000)

            chart.legend.isEnabled = false
            chart.description.isEnabled = false
            chart.setTouchEnabled(true)
            chart.isDragEnabled = true
            chart.isScaleXEnabled = false
            chart.isScaleYEnabled = false

            chart.data = LineData(dataset)

            chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    e?.let {
                        for (entry in dataset.values) {
                            entry.icon = null
                        }

                        dataset.getEntryForXValue(e.x, e.y)?.let { selectedEntry ->
                            selectedEntry.icon = ContextCompat.getDrawable(context, R.drawable.circle_marker)
                        }

                        chart.invalidate()
                    }
                }

                override fun onNothingSelected() {
                    for (entry in dataset.values) {
                        entry.icon = null
                    }
                    chart.invalidate()
                }
            })

            chart.invalidate()
            chart
        }
    )
}

class CustomMarkerView(context: Context, private val currency: String) : MarkerView(context, R.layout.marker_view) {
    private val textView: TextView = findViewById(R.id.markerText)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        e?.let {
            textView.text = "$currency %.2f".format(e.y)
        }
        super.refreshContent(e, highlight)
    }
}