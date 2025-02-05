package com.example.expenseit.utils

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Url

interface ReceiptApiService {
    @POST
    @Headers("Content-Type: application/json")
    fun scanDocument(
        @Url url: String, // Dynamic URL
        @Header("Ocp-Apim-Subscription-Key") apiKey: String, // API key header
        @Body body: ScanRequest // Request body
    ): Call<Void>

    @GET
    fun getScanResult(
        @Url operationLocation: String, // Dynamic URL
        @Header("Ocp-Apim-Subscription-Key") apiKey: String // API key header
    ): Call<ScanResult>
}

// Request body for the API
data class ScanRequest(
    val urlSource: String
)

// Response model for the scan result
data class ScanResult(
    val status: String, // e.g., "succeeded", "running", "failed"
    val createdDateTime: String,
    val lastUpdatedDateTime: String,
    val analyzeResult: AnalyzeResult? // Corrected field name
)

data class AnalyzeResult(
    val documents: List<Document>?
)

data class Document(
    val fields: Map<String, Field>
)

data class Field(
    val type: String,
    val valueString: String?,
    val valueNumber: Double?,
    val valueDate: String?,
    val valueTime: String?,
    val valuePhoneNumber: String?,
    val valueAddress: ValueAddress?,
    val content: String?
)

data class ValueAddress(
    val houseNumber: String?,
    val road: String?,
    val city: String?,
    val state: String?,
    val postalCode: String?,
    val streetAddress: String?
)