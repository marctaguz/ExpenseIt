package com.example.expenseit.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.expenseit.data.local.db.ExpenseDao
import com.example.expenseit.data.local.entities.CategoryTotal
import com.example.expenseit.data.local.entities.ExpenseSummary
import com.example.expenseit.utils.DateUtils
import com.github.mikephil.charting.data.Entry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val expenseDao: ExpenseDao
) : ViewModel() {

    fun getEntriesForChart(entries: List<ExpenseSummary>): List<Entry> {
        return entries.mapIndexed { index, entry ->
            Entry(index.toFloat(), entry.total.toFloat())
        }
    }

    fun getMonthLabels(entries: List<ExpenseSummary>): List<String> {
        return entries.map { DateUtils.formatMonthForChart(it.month) }
    }

    fun getMonthlyComparison(data: List<ExpenseSummary>): Pair<Float, Float> {
        if (data.size < 2) return Pair(0f, 0f) // Not enough data

        val currentMonth = data[0].total.toFloat()
        val lastMonth = data[1].total.toFloat()

        return Pair(currentMonth, lastMonth)
    }

    fun getCategoryTotalsForMonth(month: String) : Flow<List<CategoryTotal>> {
        return expenseDao.getCategoryTotalsForMonth(month)
    }

}