package com.example.expenseit.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "receipts")
data class Receipt(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val merchantName: String,
    val date: Long,
    val totalPrice: BigDecimal = BigDecimal("0.00"),
    val imageUrl: String
)
