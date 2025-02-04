package com.example.expenseit.ui

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.expenseit.ui.components.PageHeader
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import okhttp3.OkHttpClient
import java.io.File
import java.io.FileOutputStream

//azure api key and endpoint
val apiKey = "3ZSxFSVH8fs9hBqXCQ4c3SUwePuWG7IIc0K77p6t0nJqm9GyKGJFJQQJ99BAACmepeSXJ3w3AAALACOGlnd4"
val endpoint = "https://expenseit.cognitiveservices.azure.com/"

val client = OkHttpClient()

@Composable
fun ReceiptScanScreen(navController: NavController, modifier: Modifier) {
    // Initialize document scanner options
    val options = remember {
        GmsDocumentScannerOptions.Builder()
            .setScannerMode(SCANNER_MODE_FULL)
            .setGalleryImportAllowed(true)
            .setPageLimit(5)
            .setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF)
            .build()
    }

    val scanner = remember { GmsDocumentScanning.getClient(options) }
    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val context = LocalContext.current

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

                // Process each image for text recognition
                imageUris.forEach { uri ->
                    processTextFromImage(uri, recognizer, context)
                }
            }
        }
    )

    val activity = LocalContext.current as Activity

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
                imageUris.forEach { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Button(onClick = {
                    scanner.getStartScanIntent(activity)
                        .addOnSuccessListener {
                            scannerLauncher.launch(
                                IntentSenderRequest.Builder(it).build()
                            )
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                activity,
                                it.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }) {
                    Text(text = "Scan Document")
                }
            }
        }
    )
}

private fun processTextFromImage(uri: Uri, recognizer: TextRecognizer, context: android.content.Context) {
    val inputImage = InputImage.fromFilePath(context, uri)

    recognizer.process(inputImage)
        .addOnSuccessListener { visionText ->
            // Handle the recognized text
            for (block in visionText.textBlocks) {
                val blockText = block.text
                val confidenceScore = block
                for (line in block.lines) {
                    val lineText = line.text
                    for (element in line.elements) {
                        val elementText = element.text
                        // Process each element here if needed
                    }
                }
                Toast.makeText(context, "Recognized text: $blockText", Toast.LENGTH_SHORT).show()
            }
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Text recognition failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}

