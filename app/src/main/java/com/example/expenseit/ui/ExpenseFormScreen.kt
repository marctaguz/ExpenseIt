package com.example.expenseit.ui

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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.expenseit.ui.components.PageHeader
import com.example.expenseit.ui.viewmodels.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExpenseFormScreen(
    navController: NavController,
    expenseViewModel: ExpenseViewModel = hiltViewModel(),
    expenseId: String?
) {
    val context = LocalContext.current
    val expense by expenseViewModel.expense.collectAsState()

    var title by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf<Long?>(System.currentTimeMillis()) }
    var amountError by remember { mutableStateOf("") }
    var showModal by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val formattedDate = date?.let { dateFormatter.format(Date(it)) } ?: ""

    // Load expense data if editing
    LaunchedEffect(expenseId) {
        if (expenseId != null) {
            expenseViewModel.loadExpenseById(expenseId.toLong())
        }
    }

    LaunchedEffect(expense) {
        expense?.let {
            title = it.title
            amount = it.amount.toString()
            category = it.category
            description = it.description
            date = it.date
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                PageHeader(
                    title = if (expenseId != null) "Edit Expense" else "Add Expense",
                    actionButtonVisible = true,
                    onClose = { navController.popBackStack() }
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
        content = { paddingValues ->
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)) {
                // Title field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Amount field
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        // Allow decimal input for amount with up to two decimal places
                        if (it.isEmpty() || it.matches("^\\d*\\.?\\d{0,2}\$".toRegex())) {
                            amount = it
                            amountError = "" // Clear error when valid input is entered
                        } else {
                            amountError = "Amount must be a valid number with up to 2 decimal places"
                        }
                    },
                    label = { Text("Amount") },
                    isError = amountError.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (amountError.isNotEmpty()) {
                    Text(text = amountError, color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.height(8.dp))

                val focusRequester = FocusRequester()
                val interactionSource = remember { MutableInteractionSource() }
                Box() {
                    // OutlinedTextField for displaying the selected category
                    OutlinedTextField(
                        value = category,
                        onValueChange = { /* Do nothing, handled by dialog */ },
                        label = { Text("Select Category") },
                        readOnly = true,
                        trailingIcon = {
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Select Category")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Transparent) // Ensure the background is transparent so the clickable Box is visible
                            .focusRequester(focusRequester = focusRequester)
                    )

                    Box(modifier = Modifier
                        .matchParentSize()
                        .clickable(
                            onClick = {
                                navController.navigate("category_list")
//                                showCategoryDialog = true
                                focusRequester.requestFocus()
                            },
                            interactionSource = interactionSource,
                            indication = null //to avoid the ripple on the Box
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Description field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Date field
                OutlinedTextField(
                    value = formattedDate,
                    onValueChange = { },
                    label = { Text("Date") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showModal = true }) {
                            Icon(imageVector = Icons.Default.DateRange, contentDescription = "Select Date")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Show the modal date picker
                if (showModal) {
                    DatePickerModal(
                        onDateSelected = { selectedDate ->
                            date = selectedDate
                            showModal = false
                        },
                        onDismiss = { showModal = false }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Save Button
                Button(
                    onClick = {
                        if (title.isNotEmpty() && amount.isNotEmpty() && category.isNotEmpty() && amountError.isEmpty()) {
                            val parsedAmount = amount.toDoubleOrNull() ?: 0.0
                            if (expenseId != null) {
                                // Update existing expense
                                expenseViewModel.updateExpense(expenseId.toLong(), title, parsedAmount, category, description, date!!) {
                                    navController.popBackStack()
                                }
                            } else {
                                // Add new expense
                                expenseViewModel.addExpense(title, parsedAmount, category, description, date!!) {
                                    navController.popBackStack()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Expense")
                }
            }
            // Observe NavBackStackEntry to get the selected category
            navController.currentBackStackEntry?.savedStateHandle?.getLiveData<String>("selectedCategory")
                ?.observe(LocalLifecycleOwner.current) { selectedCategory ->
                    category = selectedCategory
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
