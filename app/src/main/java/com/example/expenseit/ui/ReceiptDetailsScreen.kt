package com.example.expenseit.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.composables.core.Menu
import com.composables.core.MenuButton
import com.composables.core.MenuContent
import com.composables.core.MenuItem
import com.composables.core.rememberMenuState
import com.example.expenseit.R
import com.example.expenseit.data.local.entities.Receipt
import com.example.expenseit.data.local.entities.ReceiptItem
import com.example.expenseit.ui.components.CustomDateField
import com.example.expenseit.ui.components.CustomTextField
import com.example.expenseit.ui.components.PageHeader
import com.example.expenseit.ui.components.ReceiptItemCard
import com.example.expenseit.ui.viewmodels.ExpenseViewModel
import com.example.expenseit.ui.viewmodels.ReceiptViewModel
import java.math.BigDecimal

@Composable
fun ReceiptDetailsScreen(navController: NavController, receiptId: Int) {
    var receipt by remember { mutableStateOf<Receipt?>(null) }
    var editedItems by remember { mutableStateOf<List<ReceiptItem>>(emptyList()) }
    val receiptViewModel: ReceiptViewModel = hiltViewModel()
    var currentlyEditingItemId by remember { mutableStateOf<Int?>(null) }

    var editedMerchantName by remember { mutableStateOf("") }
    var editedTransactionDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var editedTotalPrice by remember { mutableStateOf(BigDecimal.ZERO) }

    var initialReceipt by remember { mutableStateOf<Receipt?>(null) }
    var initialItems by remember { mutableStateOf<List<ReceiptItem>>(emptyList()) }

    var showConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showImagePreview by remember { mutableStateOf(false) }
    var showExpenseDialog by remember { mutableStateOf(false) }

    var isDropdownExpanded by remember { mutableStateOf(false) }

    val expenseViewModel: ExpenseViewModel = hiltViewModel()

    val options = listOf("United States", "Greece", "Indonesia", "United Kingdom")
    var selected by remember { mutableStateOf(0) }
    val state = rememberMenuState(expanded = isDropdownExpanded)

    LaunchedEffect(receiptId) {
        receiptViewModel.getReceiptById(receiptId) { fetchedReceipt, fetchedItems ->
            receipt = fetchedReceipt
            editedItems = fetchedItems.toMutableList()
            editedMerchantName = fetchedReceipt?.merchantName ?: ""
            editedTransactionDate = fetchedReceipt?.date ?: System.currentTimeMillis()
            editedTotalPrice = fetchedReceipt?.totalPrice ?: BigDecimal("0.00")
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
                    leftActionButtonVisible = true,
                    onLeftAction = {
                        if (hasChanges()) {
                            showConfirmDialog = true
                        } else {
                            navController.popBackStack()
                        }
                    }
                )
                Menu(state, modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)) {
                    MenuButton(Modifier.clip(RoundedCornerShape(6.dp)))
                    {
                        Icon(Icons.Default.MoreVert, contentDescription = "More Options", tint = Color.White)
                    }

                    MenuContent(
                        modifier = Modifier.width(180.dp)
//                            .shadow(elevation = 1.dp, shape = RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .padding(0.dp),
                        enter = scaleIn(transformOrigin = TransformOrigin(1f, 0f)) +
                                fadeIn(tween(durationMillis = 500)) +
                                expandIn(expandFrom = Alignment.TopEnd),
                        exit = scaleOut(transformOrigin = TransformOrigin(0f, 0f)) +
                                fadeOut() +
                                shrinkOut(shrinkTowards = Alignment. TopStart)

                    ) {
                        MenuItem(
                            modifier = Modifier.clip(RoundedCornerShape(4.dp)),
                            onClick = {
                                isDropdownExpanded = false
                                // Implement Rescan functionality
                            }
                        ) {
                            Text("Rescan", modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 8.dp))
                        }
                        MenuItem(
                            modifier = Modifier.clip(RoundedCornerShape(4.dp)),
                            onClick = {
                                isDropdownExpanded = false
                                showExpenseDialog = true
                            }
                        ) {
                            Text("Create Expense", modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 8.dp))
                        }
                        HorizontalDivider()
                        MenuItem(
                            modifier = Modifier.clip(RoundedCornerShape(4.dp)),
                            onClick = {
                                isDropdownExpanded = false
                                showDeleteDialog = true
                            }
                        ) {
                            Text("Delete", modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 8.dp),color = Color.Red)
                        }
                    }

                }
            }
        },
        floatingActionButton = {
            FloatingActionButton (
                onClick = {
                    receipt?.let {
                        val updatedReceipt = it.copy(
                            merchantName = editedMerchantName,
                            date = editedTransactionDate,
                            totalPrice = editedTotalPrice
                        )
                        receiptViewModel.updateReceipt(updatedReceipt)
                        receiptViewModel.updateReceiptItems(editedItems)
                        navController.popBackStack()
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
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(12.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
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
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(rec.imageUrl)
                                        .placeholder(R.drawable.placeholder_image)
                                        .build(),
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

                    // Receipt Details section
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 0.dp, vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(1.dp),
                        colors = CardDefaults.cardColors(Color.White),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Receipt Details",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // Merchant Name
                            CustomTextField(
                                value = editedMerchantName,
                                onValueChange = { editedMerchantName = it },
                                label = "Merchant",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            )

                            // Date
                            CustomDateField(
                                date = editedTransactionDate,
                                onDateSelected = { editedTransactionDate = it },
                                label = "Date",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            )

                            // Total Price
                            CustomTextField(
                                value = editedTotalPrice.toString(),
                                onValueChange = { input ->
                                    editedTotalPrice = input.toBigDecimalOrNull() ?: BigDecimal.ZERO
                                },
                                label = "Total Price",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Items list section
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 0.dp, vertical = 8.dp)
                            .padding(bottom = 160.dp),
                        elevation = CardDefaults.cardElevation(1.dp),
                        colors = CardDefaults.cardColors(Color.White),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
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
                                        editedItems = editedItems + newItem
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
                                modifier = Modifier.padding(bottom = 8.dp),
                                color = Color(0xFFE5E7EB)
                            )

                            // Replace LazyColumn with a regular Column
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            ) {
                                editedItems.forEach { item ->
                                    ReceiptItemCard(
                                        item = item,
                                        isEditing = currentlyEditingItemId == item.id,
                                        onEditClick = {
                                            currentlyEditingItemId = item.id
                                        },
                                        onDoneEditing = { updatedItem ->
                                            editedItems = editedItems.map {
                                                if (it.id == updatedItem.id) updatedItem else it
                                            }
                                            currentlyEditingItemId = null
                                        },
                                        onDelete = {
                                            editedItems = editedItems.filter { it.id != item.id }
                                        }
                                    )
                                }
                            }
                        }
                    }



                }
            }
        }
    )

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
                    modifier = Modifier.fillMaxWidth(),
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

    // Delete Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Receipt?") },
            text = { Text("Do you really want to delete this receipt? This action cannot be undone.") },
            confirmButton = { TextButton(onClick = {
                receiptViewModel.deleteReceipt(receiptId)
                showDeleteDialog = false
                navController.popBackStack() }
            ) {
                Text("Delete")
            } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
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
                        val defaultCategoryId = 1L
                        expenseViewModel.addExpense(
                                title = rec.merchantName,
                                amount = rec.totalPrice,
                                categoryId = defaultCategoryId,
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
                    //TODO
                }) {
                    Text("Select Items")
                }
            }
        )
    }
}

