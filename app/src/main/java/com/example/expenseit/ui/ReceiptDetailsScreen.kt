package com.example.expenseit.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.expenseit.data.local.entities.Expense
import com.example.expenseit.data.local.entities.Receipt
import com.example.expenseit.data.local.entities.ReceiptItem
import com.example.expenseit.ui.components.CustomDateField
import com.example.expenseit.ui.components.CustomTextField
import com.example.expenseit.ui.components.PageHeader
import com.example.expenseit.ui.components.ReceiptItemCard
import com.example.expenseit.ui.viewmodels.ExpenseViewModel
import com.example.expenseit.ui.viewmodels.ReceiptViewModel
import com.example.expenseit.ui.viewmodels.SettingsViewModel
import java.math.BigDecimal

@Composable
fun ReceiptDetailsScreen(navController: NavController, receiptId: Int) {
    var receipt by remember { mutableStateOf<Receipt?>(null) }
    var editedItems by remember { mutableStateOf<List<ReceiptItem>>(emptyList()) }
    val receiptViewModel: ReceiptViewModel = hiltViewModel()
    var currentlyEditingItemId by remember { mutableStateOf<Int?>(null) } // Track currently edited item

    var editedMerchantName by remember { mutableStateOf("") }
    var editedTransactionDate by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Store initial state for comparison
    var initialReceipt by remember { mutableStateOf<Receipt?>(null) }
    var initialItems by remember { mutableStateOf<List<ReceiptItem>>(emptyList()) }

    var showConfirmDialog by remember { mutableStateOf(false) }
    var showImagePreview by remember { mutableStateOf(false) }

    val totalPrice = editedItems
        .sumOf { it.price * it.quantity.toBigDecimal() }
        .setScale(2, BigDecimal.ROUND_HALF_UP)
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val expenseViewModel: ExpenseViewModel = hiltViewModel()
    val currency by settingsViewModel.currency.collectAsStateWithLifecycle()

    var showExpenseDialog by remember { mutableStateOf(false) }

    LaunchedEffect(receiptId) {
        receiptViewModel.getReceiptById(receiptId) { fetchedReceipt, fetchedItems ->
            receipt = fetchedReceipt
            editedItems = fetchedItems.toMutableList()  // Create a copy for UI editing
            editedMerchantName = fetchedReceipt?.merchantName ?: ""
            editedTransactionDate = fetchedReceipt?.date ?: System.currentTimeMillis()
            // Save the initial state for later comparison
            initialReceipt = fetchedReceipt?.copy()
            initialItems = fetchedItems.map { it.copy() }
        }
    }

    fun hasChanges(): Boolean {
        return receipt?.merchantName != editedMerchantName ||
                receipt?.date != editedTransactionDate ||
                editedItems != initialItems
    }

    Scaffold(
        topBar = {
            Box(modifier = Modifier.fillMaxWidth()
            ) {
                PageHeader(
                    title = "Receipt Details",
                    actionButtonVisible = true,
                    onClose = {
                        if (hasChanges()) {
                            showConfirmDialog = true
                        } else {
                            navController.popBackStack()
                        }
                    }                )
                Button(
                    onClick = {
                        receipt?.let {
                            val updatedReceipt = it.copy(
                                merchantName = editedMerchantName,
                                date = editedTransactionDate,
                                totalPrice = totalPrice
                            )
                            receiptViewModel.updateReceipt(updatedReceipt)
                        }
                        receiptViewModel.updateReceiptItems(editedItems)
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp),
                    border = BorderStroke(1.dp, Color.White)
                ) {
                    Icon(imageVector = Icons.Default.Done, contentDescription = "Save")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Save")
                }
            }
        },
        bottomBar = {
            receipt?.let { rec ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = "Total: ",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = currency + " %.2f".format(totalPrice),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { /* Rescan functionality (to be implemented later) */ },
                            modifier = Modifier.padding(start = 0.dp)
                        ) {
                            Text("Rescan")
                        }
                        Button(
                            onClick = { showExpenseDialog = true }
                        ) {
                            Text("Create Expense")
                        }
                    }
                }
            }
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(12.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                receipt?.let { rec ->

                    //Receipt Image section
                    Card(
                        modifier = Modifier
                            .padding(bottom = 10.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(Color.White),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (rec.imageUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = rec.imageUrl,
                                    contentDescription = "Receipt Image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .size(150.dp)
                                        .clickable { showImagePreview = true },
                                    alignment = Alignment.Center
                                )
                            } else {
                                Text(
                                    text = "No receipt image available",
                                    color = Color.Gray,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                    //Merchant and Date section
                    Card(modifier = Modifier
                        .fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(1.dp),
                        colors = CardDefaults.cardColors(Color.White),
                    ) {
                        Row(modifier = Modifier
                            .padding(horizontal = 5.dp, vertical = 20.dp)
                        ) {
                            Column(modifier = Modifier
                                .weight(0.5f)
                                .padding(horizontal = 10.dp)
                            ) {
                                CustomTextField(
                                    value = editedMerchantName,
                                    onValueChange = { editedMerchantName = it },
                                    label = "Merchant"
                                )
                            }
                            Column(modifier = Modifier
                                .weight(0.5f)
                                .padding(horizontal = 10.dp)
                            ) {
                                CustomDateField(
                                    date = editedTransactionDate,
                                    onDateSelected = { editedTransactionDate = it },
                                    label = "Date"
                                )

                            }
                        }
                    }

                    //Items list section
                    Card(
                        modifier = Modifier
                            .padding(top = 10.dp, bottom = 30.dp)
                            .weight(1f),
                        elevation = CardDefaults.cardElevation(1.dp),
                        colors = CardDefaults.cardColors(Color.White),
                    ) {
                        LazyColumn(
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Items",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Button(
                                        onClick = {
                                            val newItem = ReceiptItem(
                                                receiptId = receiptId,
                                                itemName = "New Item",
                                                quantity = 1,
                                                price = BigDecimal("0.00")
                                            )
                                            editedItems = editedItems + newItem  // Add to local list

                                        },
                                        contentPadding = PaddingValues(
                                            horizontal = 12.dp,
                                            vertical = 6.dp
                                        )
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Add",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add Item")
                                    }
                                }

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = Color(0xFFE5E7EB)
                                )
                            }


                            items(editedItems) { item ->
                                ReceiptItemCard(
                                    item = item,
                                    isEditing = currentlyEditingItemId == item.id,
                                    onEditClick = {
                                        currentlyEditingItemId = item.id
                                    }, // Set to edit mode
                                    onDoneEditing = { updatedItem ->
                                        // Update the local list only (not saving to DB yet)
                                        editedItems = editedItems.map {
                                            if (it.id == updatedItem.id) updatedItem else it
                                        }
                                        currentlyEditingItemId = null
                                    },
                                    onDelete = {
                                        editedItems = editedItems.filter { it.id != item.id }  // Remove from local list
                                    }
                                )
                            }
                        }
                    }


                }
            }
        }
    )

    // ✅ Full Screen Image Preview Dialog
    if (showImagePreview) {
        AlertDialog(
            containerColor = Color.Transparent,
            onDismissRequest = { showImagePreview = false },
            confirmButton = {},
            dismissButton = {},
            text = {
                AsyncImage(
                    model = receipt?.imageUrl,
                    contentDescription = "Full Screen Receipt",
                    modifier = Modifier.fillMaxSize(),
                    alignment = Alignment.Center
                )
            }
        )
    }

    // Confirmation Dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Discard changes?") },
            text = { Text("You have unsaved changes. Are you sure you want to leave without saving?") },
            confirmButton = { TextButton(onClick = { showConfirmDialog = false; navController.popBackStack() }) { Text("Discard") } },
            dismissButton = { TextButton(onClick = { showConfirmDialog = false }) { Text("Cancel") } }
        )
    }

    // Show expense creation dialog
    if (showExpenseDialog) {
        AlertDialog(
            onDismissRequest = { showExpenseDialog = false },
            title = { Text("Create Expense") },
            text = { Text("Would you like to create an expense using the total price or select items?") },
            confirmButton = {
                Button(onClick = {
                    receipt?.let { rec ->
                        expenseViewModel.addExpense(
                                title = rec.merchantName,
                                amount = rec.totalPrice,
                                category = "Uncategorized",
                                description = "Created from receipt",
                                date = rec.date,
                                receiptId = rec.id,
                                onSuccess = { navController.popBackStack() }
                        )
                        showExpenseDialog = false
                        navController.popBackStack()
                    }
                }) {
                    Text("Use Total Price")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showExpenseDialog = false
                    navController.navigate("select_expense_items/$receiptId")
                }) {
                    Text("Select Items")
                }
            }
        )
    }
}

