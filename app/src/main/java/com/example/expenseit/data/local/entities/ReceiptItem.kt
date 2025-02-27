package com.example.expenseit.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "receipt_items")
data class ReceiptItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var receiptId: Int, // Foreign Key linking to Receipt
    val itemName: String,
    val quantity: Int,
    val price: Double
)
