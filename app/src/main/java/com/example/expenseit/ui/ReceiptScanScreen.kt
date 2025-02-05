package com.example.expenseit.ui

import android.app.Activity.RESULT_OK
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.expenseit.ui.components.PageHeader
import com.example.expenseit.utils.ReceiptApiClient
import com.example.expenseit.utils.ScanResult
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun ReceiptScanScreen(navController: NavController, modifier: Modifier) {

    val options = remember {
        GmsDocumentScannerOptions.Builder()
            .setScannerMode(SCANNER_MODE_FULL)
            .setGalleryImportAllowed(true)
            .setPageLimit(1)
            .setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF)
            .build()
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val receiptApiClient = remember { ReceiptApiClient() }
    var receiptData by remember { mutableStateOf<ScanResult?>(null) }
    val scanner = remember { GmsDocumentScanning.getClient(options) }
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = {
            if (it.resultCode == RESULT_OK) {
                val result = GmsDocumentScanningResult.fromActivityResultIntent(it.data)
                imageUris = result?.pages?.map { it.imageUri } ?: emptyList()

                result?.pdf?.let { pdf ->
                    val fos = FileOutputStream(File(context.filesDir, "scan.pdf"))
                    context.contentResolver.openInputStream(pdf.uri)?.use {
                        it.copyTo(fos)
                    }
                }

                imageUris.forEach { uri ->
                    coroutineScope.launch {
                        val scanResult = receiptApiClient.uploadReceipt()
                        if (scanResult != null) {
                            Log.d("ReceiptScanScreen", "Scan result: $scanResult")
                            receiptData = scanResult // Update the state with the result
                        } else {
                            Log.e("ReceiptScanScreen", "Failed to scan receipt")
                            receiptData = null // Reset the state
                        }
                    }                }
            }
        }
    )

    Scaffold(
        topBar = {
            PageHeader(title = "Receipt Scan Screen", actionButtonVisible = false)
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                receiptData?.let { data ->
                    val merchantName = data.analyzeResult?.documents?.firstOrNull()?.fields?.get("MerchantName")?.valueString
                    val transactionDate =
                        data.analyzeResult?.documents?.firstOrNull()?.fields?.get("TransactionDate")?.valueDate
                    val total =
                        data.analyzeResult?.documents?.firstOrNull()?.fields?.get("Total")?.valueNumber

                    Text(
                        text = "Merchant Name: $merchantName\nTransaction Date: $transactionDate\nTotal: $total",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            val scanResult = receiptApiClient.uploadReceipt()
                            if (scanResult != null) {
                                Log.d("ReceiptScanScreen", "Scan result: $scanResult")
                                receiptData = scanResult // Update the state with the result
                            } else {
                                Log.e("ReceiptScanScreen", "Failed to scan receipt")
                                receiptData = null // Reset the state
                            }
                        }
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                Text(text = "Scan Document")
            }
        }
        }
    )
}