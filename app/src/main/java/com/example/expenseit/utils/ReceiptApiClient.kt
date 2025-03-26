package com.example.expenseit.utils

import android.util.Log
import com.example.expenseit.utils.Constants.API_KEY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ReceiptApiClient {
    private val apiKey = API_KEY
    private val endpoint = "https://expenseit.cognitiveservices.azure.com"
    private val modelID = "prebuilt-receipt"
    private val apiVersion = "2024-11-30"
    //private val documentUrl = "https://firebasestorage.googleapis.com/v0/b/expenseit-86eeb.firebasestorage.app/o/receipts%2FWhatsApp%20Image%202025-02-16%20at%203.09.42%E2%80%AFAM.jpeg?alt=media&token=e7cf18cf-d2e9-4c4b-95e7-1026f755ef70"
//    private val url = "$endpoint/formrecognizer/documentModels/$modelID:analyze?api-version=$apiVersion"
    private val url = "$endpoint/documentintelligence/documentModels/$modelID:analyze?api-version=$apiVersion"

    private val retrofit: Retrofit by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(endpoint)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    private val apiService: ReceiptApiService by lazy {
        retrofit.create(ReceiptApiService::class.java)
    }

    suspend fun uploadReceipt(documentUrl: String): ScanResult? {
        return withContext(Dispatchers.IO) {
            try {
                // Create the request body
                val requestBody = ScanRequest(urlSource = documentUrl)

                // Make the API call
                val response = apiService.scanDocument(url, apiKey, requestBody).execute()

                if (response.isSuccessful) {
                    Log.d("ReceiptApiClient", "Upload successful")
                    val operationLocation = response.headers()["Operation-Location"]
                    if (operationLocation != null) {
                        Log.d("ReceiptApiClient", "Operation-Location: $operationLocation")
                        fetchScanResult(operationLocation) // Fetch the analysis result
                    } else {
                        Log.e("ReceiptApiClient", "Operation-Location header missing")
                        null
                    }
                } else {
                    Log.e("ReceiptApiClient", "Upload failed: ${response.code()}, ${response.errorBody()?.string()}")
                    null
                }
            } catch (e: Exception) {
                Log.e("ReceiptApiClient", "Error uploading receipt", e)
                null
            }
        }
    }

    suspend fun fetchScanResult(operationLocation: String): ScanResult? {
        return withContext(Dispatchers.IO) {
            try {
                var result: ScanResult? = null
                var retries = 0
                val maxRetries = 10 // Maximum number of retries
                val delayMillis = 2000L // Delay between retries (2 seconds)

                while (retries < maxRetries) {
                    val response = apiService.getScanResult(operationLocation, apiKey).execute()

                    if (response.isSuccessful) {
                        result = response.body()
                        if (result?.status == "succeeded") {
                            Log.d("ReceiptApiClient", "Scan succeeded")
                            return@withContext result
                        } else if (result?.status == "failed") {
                            Log.e("ReceiptApiClient", "Scan failed")
                            return@withContext null
                        }
                    } else {
                        Log.e("ReceiptApiClient", "Fetch failed: ${response.code()}, ${response.errorBody()?.string()}")
                        return@withContext null
                    }

                    retries++
                    delay(delayMillis) // Wait before retrying
                }

                Log.e("ReceiptApiClient", "Max retries reached")
                null
            } catch (e: Exception) {
                Log.e("ReceiptApiClient", "Error fetching scan result", e)
                null
            }
        }
    }
}
