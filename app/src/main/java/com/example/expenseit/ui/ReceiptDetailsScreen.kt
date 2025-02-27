package com.example.expenseit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.expenseit.data.local.entities.Receipt
import com.example.expenseit.data.local.entities.ReceiptItem
import com.example.expenseit.ui.components.PageHeader
import com.example.expenseit.ui.viewmodels.ReceiptViewModel

@Composable
fun ReceiptDetailsScreen(navController: NavController, receiptId: Int) {
    var receipt by remember { mutableStateOf<Receipt?>(null) }
    var items by remember { mutableStateOf<List<ReceiptItem>>(emptyList()) }
    val receiptViewModel: ReceiptViewModel = hiltViewModel()

    LaunchedEffect(receiptId) {
        receiptViewModel.getReceiptById(receiptId) { fetchedReceipt, fetchedItems ->
            receipt = fetchedReceipt
            items = fetchedItems
        }
    }

    Scaffold(
        topBar = { PageHeader(
            title = "Receipt Details",
            actionButtonVisible = true,
            onClose = { navController.popBackStack() }
        ) },
        content = { innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                receipt?.let { rec ->
                    Text("Merchant: ${rec.merchantName}")
                    Text("Date: ${rec.transactionDate}")
                    Text("Total: ${rec.totalPrice}")

                    LazyColumn {
                        items(items) { item ->
                            Text("${item.itemName}: ${item.quantity} x ${item.price}")
                        }
                    }
                }
            }
        }
    )
}
