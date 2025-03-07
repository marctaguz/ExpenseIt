package com.example.expenseit.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: BigDecimal = BigDecimal("0.00"),
    val category: String,
    val description: String,
    val date: Long,
    val receiptId: Int? = null
)
