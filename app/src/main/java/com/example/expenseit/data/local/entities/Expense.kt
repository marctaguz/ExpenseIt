package com.example.expenseit.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: BigDecimal = BigDecimal("0.00"),
    val categoryId: Long,
    val description: String,
    val date: Long,
    val receiptId: Int? = null
)

data class ExpenseSummary(
    val month: String,
    val total: BigDecimal,
)

