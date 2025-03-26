package com.example.expenseit.data.local.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.math.BigDecimal

@Entity(tableName = "receipts")
data class Receipt(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val merchantName: String,
    val date: Long,
    val totalPrice: BigDecimal = BigDecimal("0.00"),
    val imageUrl: String
)

@Entity(tableName = "receipt_items")
data class ReceiptItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var receiptId: Int, // Foreign Key linking to Receipt
    val itemName: String,
    val quantity: Int,
    val price: BigDecimal = BigDecimal("0.00")
)

data class ReceiptWithItems(
    @Embedded
    val receipt: Receipt,

    @Relation(
        parentColumn = "id",
        entityColumn = "receiptId"
    )
    val items: List<ReceiptItem>
)