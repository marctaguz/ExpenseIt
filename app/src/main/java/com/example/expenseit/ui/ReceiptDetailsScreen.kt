package com.example.expenseit.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.expenseit.data.local.entities.Receipt
import com.example.expenseit.data.local.entities.ReceiptItem
import com.example.expenseit.ui.components.CustomTextField
import com.example.expenseit.ui.components.PageHeader
import com.example.expenseit.ui.components.ReceiptItemCard
import com.example.expenseit.ui.viewmodels.ReceiptViewModel

@Composable
fun ReceiptDetailsScreen(navController: NavController, receiptId: Int) {
    var receipt by remember { mutableStateOf<Receipt?>(null) }
    var editedItems by remember { mutableStateOf<List<ReceiptItem>>(emptyList()) }
    val receiptViewModel: ReceiptViewModel = hiltViewModel()
    var currentlyEditingItemId by remember { mutableStateOf<Int?>(null) } // Track currently edited item

    var editedMerchantName by remember { mutableStateOf("") }
    var editedTransactionDate by remember { mutableStateOf("") }


    LaunchedEffect(receiptId) {
        receiptViewModel.getReceiptById(receiptId) { fetchedReceipt, fetchedItems ->
            receipt = fetchedReceipt
            editedItems = fetchedItems.toMutableList()  // Create a copy for UI editing
            editedMerchantName = fetchedReceipt?.merchantName ?: ""
            editedTransactionDate = fetchedReceipt?.transactionDate ?: ""
        }
    }

    Scaffold(
        containerColor = Color(0xFFF9FAFB),
        topBar = {
            Box(modifier = Modifier.fillMaxWidth()
            ) {
                PageHeader(
                    title = "Receipt Details",
                    actionButtonVisible = true,
                    onClose = { navController.popBackStack() }
                )
                Button(
                    onClick = {
                        receipt?.let {
                            val updatedReceipt = it.copy(
                                merchantName = editedMerchantName,
                                transactionDate = editedTransactionDate
                            )
                            receiptViewModel.updateReceipt(updatedReceipt)
                        }
                        receiptViewModel.updateReceiptItems(editedItems)
                        navController.popBackStack()
                    },
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp),
                    border = BorderStroke(1.dp, Color.White)
                ) {
                    Icon(imageVector = Icons.Default.Done, contentDescription = "Save")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Save")
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
                                CustomTextField(
                                    value = editedTransactionDate,
                                    onValueChange = { editedTransactionDate = it },
                                    label = "Date"
                                )

                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .padding(top = 10.dp, bottom = 130.dp),
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
                                                price = 0.0
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
}

