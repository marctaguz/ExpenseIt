package com.example.expenseit.ui.viewmodels.dao

import com.example.expenseit.data.local.db.ExpenseDao
import com.example.expenseit.data.local.entities.CategoryTotal
import com.example.expenseit.data.local.entities.Expense
import com.example.expenseit.data.local.entities.ExpenseSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.exp

class FakeExpenseDao : ExpenseDao {

    private val _allByMonth      = MutableStateFlow<List<ExpenseSummary>>(emptyList())
    private val _monthlyExpenses = MutableStateFlow<List<ExpenseSummary>>(emptyList())
    private val _categoryTotals  = MutableStateFlow<List<CategoryTotal>>(emptyList())

    private val _allExpenses = MutableStateFlow<List<Expense>>(emptyList())
    fun emitAllExpenses(list: List<Expense>) {
        // Update _allExpenses instead
        _allExpenses.value = list
    }
    override fun getAllExpensesByMonth(): Flow<List<ExpenseSummary>> =
        _allByMonth

    override fun getMonthlyExpenses(): Flow<List<ExpenseSummary>> =
        _monthlyExpenses

    override fun getCategoryTotals(): Flow<List<CategoryTotal>> =
        _categoryTotals

    override fun getCategoryTotalsForMonth(month: String): Flow<List<CategoryTotal>> =
        _categoryTotals

    override fun getAllExpenses(): Flow<List<Expense>> =
        _allExpenses

    override suspend fun insert(expense: Expense): Long {
        // just append
        _allExpenses.update { it + expense }
        return expense.id
    }

    override suspend fun update(expense: Expense) {
        _allExpenses.update { list ->
            list.map { if (it.id == expense.id) expense else it }
        }
    }

    override suspend fun deleteExpenseById(id: Long) {
        _allExpenses.update { it.filter { e -> e.id != id } }
    }

    override suspend fun getExpenseById(id: Long): Expense? =
        _allExpenses.value.find { it.id == id }

    // test helpers
    fun emitAllByMonth(data: List<ExpenseSummary>) {
        _allByMonth.value = data
    }
    fun emitMonthlyExpenses(data: List<ExpenseSummary>) {
        _monthlyExpenses.value = data
    }
    fun emitCategoryTotals(data: List<CategoryTotal>) {
        _categoryTotals.value = data
    }
}
