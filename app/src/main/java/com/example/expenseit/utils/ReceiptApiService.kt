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
    val analyzeResult: AnalyzeResult?
)

data class AnalyzeResult(
    val apiVersion: String,
    val modelId: String,
    val stringIndexType: String,
    val content: String,
    val pages: List<Page>,
    val documents: List<Document>
)

data class Page(
    val pageNumber: Int,
    val angle: Double,
    val width: Int,
    val height: Int,
    val unit: String,
    val words: List<Word>,
    val lines: List<Line>
)

data class Word(
    val content: String,
    val polygon: List<Double>,
    val confidence: Double,
    val span: Span
)

data class Line(
    val content: String,
    val polygon: List<Double>,
    val spans: List<Span>
)

data class Span(
    val offset: Int,
    val length: Int
)

data class Document(
    val docType: String,
    val boundingRegions: List<BoundingRegion>,
    val fields: Fields
)

data class BoundingRegion(
    val pageNumber: Int,
    val polygon: List<Double>
)

data class Fields(
    val CountryRegion: FieldValue?,
    val Items: FieldValue?,
    val MerchantName: FieldValue?,
    val ReceiptType: FieldValue?,
    val Subtotal: FieldValue?,
    val Total: FieldValue?,
    val TransactionDate: FieldValue?,
    val TransactionTime: FieldValue?
)

data class FieldValue(
    val type: String,
    val valueString: String? = null,
    val valueCurrency: CurrencyValue? = null,
    val valueArray: List<FieldValue>? = null,
    val valueObject: Map<String, FieldValue>? = null,
    val valueDate: String? = null,
    val valueTime: String? = null,
    val valueNumber: Int? = null,
    val content: String? = null,
    val confidence: Double? = null
)

data class CurrencyValue(
    val currencySymbol: String,
    val amount: Double,
    val currencyCode: String
)