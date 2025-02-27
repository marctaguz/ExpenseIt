package com.example.expenseit.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expenseit.data.local.db.ExpenseDao
import com.example.expenseit.data.local.entities.Expense
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
        loadAllExpenses()  // Load all expenses on startup
    }

    private fun loadAllExpenses() {
        viewModelScope.launch {
            val allExpenses = expenseDao.getAllExpenses()
            _expenses.value = allExpenses
        }
    }

    fun loadExpenseById(expenseId: Long) {
        viewModelScope.launch {
            _expense.value = expenseDao.getExpenseById(expenseId)
        }
    }

    fun addExpense(title: String, amount: Double, category: String, description: String, date: Long, onSuccess: () -> Unit) {
        val newExpense = Expense(
            title = title,
            amount = amount,
            category = category,
            description = description,
            date = date
        )
        viewModelScope.launch {
            expenseDao.insert(newExpense)
            loadAllExpenses()
            onSuccess()
        }
    }

    fun updateExpense(expenseId: Long, title: String, amount: Double, category: String, description: String, date: Long, onSuccess: () -> Unit) {
        val updatedExpense = Expense(
            id = expenseId,
            title = title,
            amount = amount,
            category = category,
            description = description,
            date = date
        )
        viewModelScope.launch {
            expenseDao.update(updatedExpense)
            loadAllExpenses()
            onSuccess()
        }
    }

    fun deleteExpense(expenseId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            expenseDao.deleteExpenseById(expenseId)
            loadAllExpenses()
            onSuccess()
        }
    }
}

