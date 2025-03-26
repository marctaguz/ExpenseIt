package com.example.expenseit.utils

import com.example.expenseit.data.local.entities.Receipt
import com.example.expenseit.data.local.entities.ReceiptItem
import java.math.BigDecimal

object ReceiptParser {
    fun parseReceiptData(data: ScanResult): Pair<Receipt, List<ReceiptItem>>? {
        val document = data.analyzeResult?.documents?.firstOrNull() ?: return null

        val merchantName = document.fields?.MerchantName?.valueString ?: "Unknown Merchant"
        val transactionDate = document.fields?.TransactionDate?.valueDate?.let {
            DateUtils.dateStringToLong(it)
        } ?: System.currentTimeMillis()
        val total = document.fields?.Total?.valueCurrency?.amount
            ?.toBigDecimal()?.setScale(2, BigDecimal.ROUND_HALF_UP) ?: BigDecimal("0.00")

        val items = document.fields?.Items?.valueArray?.mapNotNull { item ->
            val itemFields = item.valueObject ?: return@mapNotNull null
            val itemName = itemFields["Description"]?.valueString ?: "Unknown Item"
            val quantity = itemFields["Quantity"]?.valueNumber ?: 1
            val price = itemFields["TotalPrice"]?.valueCurrency?.amount
                ?.toBigDecimal()?.setScale(2, BigDecimal.ROUND_HALF_UP) ?: BigDecimal("0.00")
            ReceiptItem(receiptId = 0, itemName = itemName, quantity = quantity, price = price)
        } ?: emptyList()

        val receipt = Receipt(
            merchantName = merchantName,
            date = transactionDate,
            totalPrice = total,
            imageUrl = "" // Will be set later
        )

        return Pair(receipt, items)
    }
}