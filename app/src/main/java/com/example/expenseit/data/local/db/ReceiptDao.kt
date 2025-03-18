package com.example.expenseit.data.local.db

import androidx.room.*
import com.example.expenseit.data.local.entities.Receipt
import com.example.expenseit.data.local.entities.ReceiptItem

@Dao
interface ReceiptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: Receipt): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceiptItems(items: List<ReceiptItem>)

    @Transaction
    suspend fun insertReceiptWithItems(receipt: Receipt, items: List<ReceiptItem>) {
        val receiptId = insertReceipt(receipt).toInt()
        items.forEach { it.receiptId = receiptId }
        insertReceiptItems(items)
    }

    @Query("SELECT * FROM receipts")
    suspend fun getAllReceipts(): List<Receipt>

    @Query("SELECT * FROM receipt_items WHERE receiptId = :receiptId")
    suspend fun getItemsForReceipt(receiptId: Int): List<ReceiptItem>

    @Query("SELECT * FROM receipts WHERE id = :receiptId")
    suspend fun getReceiptById(receiptId: Int): Receipt?

    @Update
    suspend fun updateReceipt(receipt: Receipt)

    @Update
    suspend fun updateReceiptItem(item: ReceiptItem)

    @Query("DELETE FROM receipts WHERE id = :receiptId")
    suspend fun deleteReceiptById(receiptId: Int)
}

