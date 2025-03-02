package com.example.expenseit.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "receipts")
data class Receipt(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val merchantName: String,
    val date: Long,
    val totalPrice: Double,
    val imageUrl: String
)
