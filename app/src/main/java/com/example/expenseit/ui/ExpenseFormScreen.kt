package com.example.expenseit.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.expenseit.ui.components.CustomDateField
import com.example.expenseit.ui.components.CustomNumberField
import com.example.expenseit.ui.components.CustomTextField
import com.example.expenseit.ui.components.PageHeader
import com.example.expenseit.ui.viewmodels.CategoryViewModel
import com.example.expenseit.ui.viewmodels.ExpenseViewModel

@Composable
fun ExpenseFormScreen(
    navController: NavController,
    expenseViewModel: ExpenseViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    expenseId: String?
) {
    val context = LocalContext.current
    val expense by expenseViewModel.expense.collectAsState()

    var title by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var categoryId by rememberSaveable { mutableLongStateOf(1L) }
    var description by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }
    var amountError by remember { mutableStateOf("") }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var showErrors by remember { mutableStateOf(false) }
    var isFormInitialized by rememberSaveable { mutableStateOf(false) }

    val category by categoryViewModel.getCategoryById(categoryId)
        .collectAsState(initial = null)

    LaunchedEffect(expenseId) {
        if (expenseId != null) {
            expenseViewModel.loadExpenseById(expenseId.toLong())
        }
    }

    LaunchedEffect(expense) {
        expense?.let {
            if (expenseId != null && !isFormInitialized) {
                title = it.title
                amount = it.amount.toString()
                categoryId = it.categoryId // Use categoryId
                description = it.description
                date = it.date
                isFormInitialized = true
            }
        }
    }

    LaunchedEffect(Unit) {
        navController.currentBackStackEntry?.savedStateHandle?.get<Long>("selectedCategoryId")?.let {
            Log.d("ExpenseFormScreen", "Received selected category ID: $it")
            categoryId = it
            navController.currentBackStackEntry?.savedStateHandle?.remove<Long>("selectedCategoryId")
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                PageHeader(
                    title = if (expenseId != null) "Edit Expense" else "Add Expense",
                    leftActionButtonVisible = true,
                    onLeftAction = { navController.popBackStack() }
                )
                // Delete button (only visible when editing an expense)
                if (expenseId != null) {
                    IconButton(onClick = {
                        showDeleteConfirmationDialog = true
                    },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(8.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton (
                onClick = {
                    showErrors = true
                    if (title.isNotEmpty() && amount.isNotEmpty() && categoryId != 0L && amountError.isEmpty()) {
                        val parsedAmount = amount.toBigDecimal()
                        if (expenseId != null) {
                            // Update existing expense
                            expenseViewModel.updateExpense(expenseId.toLong(), title, parsedAmount, categoryId, description, date
                            ) {
                                navController.popBackStack()
                            }
                        } else {
                            // Add new expense
                            expenseViewModel.addExpense(title, parsedAmount, categoryId, description, date
                            ) {
                                navController.popBackStack()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                elevation = FloatingActionButtonDefaults.elevation(2.dp, 2.dp)
            ) {
//                Icon(
//                    imageVector = Icons.Default.Done,
//                    contentDescription = "Save"
//                )
                Text(
                    text = "Save",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        content = { paddingValues ->
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)) {
                // Title field
                CustomTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Title",
                    modifier = Modifier.fillMaxWidth(),
                    showError = showErrors && title.isEmpty()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Amount field
                CustomNumberField(
                    value = amount,
                    onValueChange = {
                        amount = it
                    },
                    label = "Amount",
                    isDecimal = true,
                    modifier = Modifier.fillMaxWidth(),
                    showError = showErrors && title.isEmpty()
                )
                if (amountError.isNotEmpty()) {
                    Text(text = amountError, color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.height(8.dp))

                //Category Field
                val focusRequester = FocusRequester()
                val interactionSource = remember { MutableInteractionSource() }
                Box {
                    CustomTextField(
                        value = category?.name ?: "Select Category",
                        onValueChange = { /* Do nothing, handled by dialog */ },
                        label = "Category",
                        readOnly = true,
                        trailingIcon = {
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Select Category")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Transparent)
                            .focusRequester(focusRequester),
                        showError = false
                    )
                    Log.d("ExpenseFormScreen", "Category ID: $categoryId")
                    Box(modifier = Modifier
                        .matchParentSize()
                        .clickable(
                            onClick = {
                                navController.navigate("category_list")
                                focusRequester.requestFocus()
                            },
                            interactionSource = interactionSource,
                            indication = null
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Description field
                CustomTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Description",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Date field
                CustomDateField(
                    label = "Transaction Date",
                    date = date,
                    onDateSelected = { date = it },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

            }
        }
    )
    if (showDeleteConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationDialog = false },
            title = { Text("Are you sure?") },
            text = { Text("Do you really want to delete this expense? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    // Perform the deletion
                    if (expenseId != null) {
                        expenseViewModel.deleteExpense(expenseId.toLong()) {
                            navController.popBackStack() // Navigate back after deletion
                        }
                    }
                    showDeleteConfirmationDialog = false
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}