package com.example.expenseit.ui

import android.app.Activity.RESULT_OK
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.expenseit.ui.components.PageHeader
import com.example.expenseit.ui.components.ReceiptListItem
import com.example.expenseit.ui.theme.primaryLight
import com.example.expenseit.ui.viewmodels.ReceiptViewModel
import com.example.expenseit.utils.FirebaseUtils
import com.example.expenseit.utils.ReceiptApiClient
import com.example.expenseit.utils.ReceiptParser
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.launch


@Composable
fun ReceiptListScreen(
    navController: NavController,
    receiptViewModel: ReceiptViewModel = hiltViewModel(),
    modifier: Modifier,
    scrollToTop: Boolean,
    onScrollToTopCompleted: () -> Unit
) {
    val lazyListState = rememberLazyListState()

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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val receiptApiClient = remember { ReceiptApiClient() }
    val scanner = remember { GmsDocumentScanning.getClient(options) }
    val isLoading by receiptViewModel.isLoading.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        receiptViewModel.loadAllReceipts()
    }

    LaunchedEffect(scrollToTop) {
        if (scrollToTop) {
            lazyListState.animateScrollToItem(
                index = 0,
                scrollOffset = 0
            )
            onScrollToTopCompleted()
        }
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            snackbarMessage = null
        }
    }

    // Preload images when the list of receipts is available
    LaunchedEffect(receipts) {
        if (receipts.isNotEmpty()) {
            receiptViewModel.preloadImages(context, receipts)
        }
    }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result ->
            if (result.resultCode == RESULT_OK) {
//                isLoading = true
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
                                        snackbarMessage = "Receipt added successfully!"
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("ReceiptScanScreen", "Error processing receipt", e)
                            snackbarMessage = "Failed to process receipt. Please try again."
                        } finally {
//                            isLoading = false
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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Card(
                        modifier = Modifier,
                        elevation = CardDefaults.cardElevation(1.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            items(receipts.size) { index ->
                                val receipt = receipts[index]
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn(animationSpec = tween(durationMillis = 500)),
                                    exit = fadeOut(animationSpec = tween(durationMillis = 500))
                                ) {
                                    ReceiptListItem(
                                        receipt = receipt,
                                        navController = navController
                                    )
                                }
                                if (index < receipts.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        color = Color(0xFFE5E7EB)
                                    )
                                }
                            }
                        }

                        if (receipts.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No receipts found. Scan a receipt to get started!",
                                    color = Color.Gray,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
//                if (isLoading) {
//                    Box(
//                        Modifier.fillMaxSize()
//                            .background(Color.Gray.copy(alpha = 0.5f))
//                    ) {
//                        CircularProgressIndicator(
//                            modifier = Modifier
//                                .align(Alignment.Center)
//                                .size(48.dp),
//                            color = primaryLight
//                        )
//                    }
//                }
            }
        }
    )
}



