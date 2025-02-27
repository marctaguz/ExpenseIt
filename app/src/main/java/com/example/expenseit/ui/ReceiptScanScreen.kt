package com.example.expenseit.ui

import android.app.Activity.RESULT_OK
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
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
import com.example.expenseit.data.local.db.ReceiptDao
import com.example.expenseit.data.local.entities.Receipt
import com.example.expenseit.data.local.entities.ReceiptItem
import com.example.expenseit.ui.components.PageHeader
import com.example.expenseit.ui.viewmodels.ReceiptViewModel
import com.example.expenseit.utils.ReceiptApiClient
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
import java.util.UUID
import kotlin.coroutines.resume


@Composable
fun ReceiptScanScreen(navController: NavController,
                      receiptViewModel: ReceiptViewModel = hiltViewModel(),
                      modifier: Modifier) {

    val receipts by receiptViewModel.receipts.collectAsState()  // Observing list of receipts
    val options = remember {
        GmsDocumentScannerOptions.Builder()
            .setScannerMode(SCANNER_MODE_FULL)
            .setGalleryImportAllowed(true)
            .setPageLimit(1)
            .setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF)
            .build()
    }

    val activity = LocalContext.current as MainActivity
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val receiptApiClient = remember { ReceiptApiClient() }
    var receiptData by remember { mutableStateOf<ScanResult?>(null) }
    val scanner = remember { GmsDocumentScanning.getClient(options) }
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    suspend fun uploadImageToFirebase(context: Context, imageUri: Uri): String? {
        return suspendCancellableCoroutine { continuation ->
            val storageRef = Firebase.storage.reference.child("receipts/${UUID.randomUUID()}")
            Log.d("uploadImageToFirebase", "Uploading image to Firebase")
            val uploadTask = storageRef.putFile(imageUri)

            uploadTask.addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    Log.d("uploadImageToFirebase", "Download URL: $downloadUri")
                    continuation.resume(downloadUri.toString())
                }.addOnFailureListener { exception ->
                    Log.e("uploadImageToFirebase", "Failed to get download URL", exception)
                    continuation.resume(null)
                }
            }.addOnFailureListener { exception ->
                Log.e("uploadImageToFirebase", "Upload failed", exception)
                continuation.resume(null)
            }
            continuation.invokeOnCancellation {
                uploadTask.cancel()
            }
        }
    }

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
                        val downloadUrl = uploadImageToFirebase(context, uri)
                        if (downloadUrl != null) {
                            val scanResult = receiptApiClient.uploadReceipt(downloadUrl)
                            receiptData = scanResult

                            receiptData?.let { data ->
                                //Extract main receipt details
                                val merchantName = data.analyzeResult?.documents?.firstOrNull()?.fields?.MerchantName?.valueString ?: "Unknown Merchant"
                                val transactionDate = data.analyzeResult?.documents?.firstOrNull()?.fields?.TransactionDate?.valueDate ?: "0/0/0000"
                                val total = data.analyzeResult?.documents?.firstOrNull()?.fields?.Total?.valueCurrency?.amount ?: 0.0

                                //Extract individual item details
                                val items = data.analyzeResult?.documents?.firstOrNull()?.fields?.Items?.valueArray?.mapNotNull { item ->
                                    val itemFields = item.valueObject ?: return@mapNotNull null
                                    val itemName = itemFields["Description"]?.valueString ?: "Unknown Item"
                                    val quantity = itemFields["Quantity"]?.valueNumber ?: 1
                                    val price = itemFields["TotalPrice"]?.valueCurrency?.amount ?: 0.0
                                    ReceiptItem(receiptId = 0, itemName = itemName, quantity = quantity, price = price) // receiptId will be updated later
                                } ?: emptyList()

                                val newReceipt = Receipt(
                                    merchantName = merchantName,
                                    transactionDate = transactionDate,
                                    totalPrice = total,
                                    imageUrl = downloadUrl
                                )

                                Log.d("ReceiptScanScreen", "New receipt: $newReceipt")
                                Log.d("ReceiptScanScreen", "New receipt: $items")
                                receiptViewModel.insertReceipt(newReceipt, items)

                                navController.navigate("receipt_details/${newReceipt.id}")

                            }
                        } else {
                            Log.e("ReceiptScanScreen", "Failed to upload image to Firebase")
                            receiptData = null
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
                scanner.getStartScanIntent(activity)
                    .addOnSuccessListener { intentSender ->
                        scannerLauncher.launch(
                            IntentSenderRequest
                                .Builder(intentSender)
                                .build()
                        )
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
                LazyColumn(
                    modifier = Modifier.padding(8.dp)
                ) {
                    items(receipts.chunked(2)) { rowReceipts ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            rowReceipts.forEach { receipt ->
                                ReceiptCard(receipt, navController)
                            }
                            if (rowReceipts.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun ReceiptCard(receipt: Receipt, navController: NavController) {
    Card(
        modifier = Modifier
//            .weight(1f)
            .padding(8.dp)
            .clickable { navController.navigate("receipt_details/${receipt.id}") },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AsyncImage(
                model = receipt.imageUrl,
                contentDescription = "Receipt Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentScale = ContentScale.Crop
            )
            Text(
                text = receipt.merchantName,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}