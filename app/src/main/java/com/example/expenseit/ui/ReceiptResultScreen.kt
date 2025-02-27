package com.example.expenseit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.expenseit.ui.components.PageHeader
import com.example.expenseit.ui.viewmodels.ReceiptViewModel
import org.json.JSONObject

//@Composable
//fun ReceiptResultScreen(navController: NavController, viewModel: ReceiptViewModel) {
//    val receiptData = viewModel.receiptData ?: "{}"  // Default to empty JSON if null
//    val parsedData = remember { parseReceiptData(receiptData) }
//
//    Scaffold(
//        topBar = { PageHeader(title = "Receipt Details", actionButtonVisible = true) },
//        content = { innerPadding ->
//            Column(
//                modifier = Modifier.padding(innerPadding).fillMaxSize(),
//                verticalArrangement = Arrangement.Center,
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                LazyColumn(modifier = Modifier.weight(1f)) {
//                    items(parsedData) { (key, value) ->
//                        Text(text = "$key: $value")
//                    }
//                }
//                Button(onClick = { navController.popBackStack() }) {
//                    Text(text = "Back")
//                }
//            }
//        }
//    )
//}

private fun parseReceiptData(receiptData: String): List<Pair<String, String>> {
    val jsonObject = JSONObject(receiptData)
    val extractedItems = mutableListOf<Pair<String, String>>()

    jsonObject.keys().forEach { key ->
        extractedItems.add(Pair(key, jsonObject.getString(key)))
    }
    return extractedItems
}
