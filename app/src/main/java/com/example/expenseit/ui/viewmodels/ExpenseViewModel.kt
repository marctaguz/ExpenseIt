package com.example.expenseit.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expenseit.data.local.db.ExpenseDao
import com.example.expenseit.data.local.entities.Expense
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val expenseDao: ExpenseDao
) : ViewModel() {

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    private val _expense = MutableStateFlow<Expense?>(null)
    val expense: StateFlow<Expense?> = _expense.asStateFlow()

    init {
        observeExpenses()
    }

    private fun observeExpenses() {
        viewModelScope.launch {
            expenseDao.getAllExpenses().collect { allExpenses ->
                _expenses.value = allExpenses
            }
        }
    }

    fun loadExpenseById(expenseId: Long) {
        viewModelScope.launch {
            _expense.value = expenseDao.getExpenseById(expenseId)
        }
    }

    fun addExpense(
        title: String,
        amount: BigDecimal,
        categoryId: Long,
        description: String,
        date: Long,
        receiptId: Int? = null,
        onSuccess: () -> Unit,
    ) {

        val newExpense = Expense(
            title = title,
            amount = amount,
            categoryId = categoryId,
            description = description,
            date = date,
            receiptId = receiptId
        )
        viewModelScope.launch {
            expenseDao.insert(newExpense)
            observeExpenses()
            onSuccess()
        }
    }

    fun updateExpense(expenseId: Long, title: String, amount: BigDecimal, categoryId: Long, description: String, date: Long, onSuccess: () -> Unit) {
        val updatedExpense = Expense(
            id = expenseId,
            title = title,
            amount = amount,
            categoryId = categoryId,
            description = description,
            date = date
        )
        viewModelScope.launch {
            expenseDao.update(updatedExpense)
            observeExpenses()
            onSuccess()
        }
    }

    fun deleteExpense(expenseId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            expenseDao.deleteExpenseById(expenseId)
            observeExpenses()
            onSuccess()
        }
    }

    fun getExpensesByDate(): Flow<Map<Long, BigDecimal>> {
        return expenseDao.getAllExpenses()
            .map { expenses ->
                expenses.groupBy { it.date }
                    .mapValues { (_, items) -> items.sumOf { it.amount } }
            }
    }

}

