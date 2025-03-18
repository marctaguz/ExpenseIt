package com.example.expenseit.ui

import android.app.Activity.RESULT_OK
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.expenseit.data.local.entities.Receipt
import com.example.expenseit.data.local.entities.ReceiptItem
import com.example.expenseit.ui.components.PageHeader
import com.example.expenseit.ui.components.ReceiptCard
import com.example.expenseit.ui.viewmodels.ReceiptViewModel
import com.example.expenseit.utils.DateUtils
import com.example.expenseit.utils.FirebaseUtils
import com.example.expenseit.utils.ReceiptApiClient
import com.example.expenseit.utils.ReceiptParser
import com.example.expenseit.utils.ScanResult
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import java.math.BigDecimal
import java.util.UUID
import kotlin.coroutines.resume


@Composable
fun ReceiptScanScreen(navController: NavController,
                      receiptViewModel: ReceiptViewModel = hiltViewModel(),
                      modifier: Modifier) {

    val options = remember {
        GmsDocumentScannerOptions.Builder()
            .setScannerMode(SCANNER_MODE_FULL)
            .setGalleryImportAllowed(true)
            .setPageLimit(1)
            .setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF)
            .build()
    }

    val receipts by receiptViewModel.receipts.collectAsState()
    val activity = LocalActivity.current
    val coroutineScope = rememberCoroutineScope()
    val receiptApiClient = remember { ReceiptApiClient() }
    val scanner = remember { GmsDocumentScanning.getClient(options) }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result ->
            if (result.resultCode == RESULT_OK) {
                val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
                scanResult?.pages?.forEach { page ->
                    coroutineScope.launch {
                        try {
                            val downloadUrl = FirebaseUtils.uploadImageToFirebase(page.imageUri)
                            if (downloadUrl != null) {
                                val receiptData = receiptApiClient.uploadReceipt(downloadUrl)
                                receiptData?.let { data ->
                                    val parsedReceipt = ReceiptParser.parseReceiptData(data)
                                    parsedReceipt?.let { (receipt, items) ->
                                        val newReceipt = receipt.copy(imageUrl = downloadUrl)
                                        receiptViewModel.insertReceipt(newReceipt, items)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("ReceiptScanScreen", "Error processing receipt", e)
                        }
                    }
                }
            }
        }
    )

    Scaffold(
        topBar = {
            PageHeader(title = "Receipt Scan Screen", actionButtonVisible = false)
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    // Safely unwrap the activity
                    activity?.let { nonNullActivity ->
                        scanner.getStartScanIntent(nonNullActivity)
                            .addOnSuccessListener { intentSender ->
                                scannerLauncher.launch(
                                    IntentSenderRequest.Builder(intentSender).build()
                                )
                            }
                    } ?: run {
                        // Handle the case where the activity is null
                        Log.e("ReceiptScanScreen", "Activity is null")
                    }
            },
                modifier = Modifier.padding(bottom = 100.dp)) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Scan Receipt")
                Text(text = "Scan Receipt")
            }
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        items(receipts) { receipt ->
                            ReceiptCard(receipt = receipt, navController = navController)
                        }
                    }
                }


        }
    )
}

