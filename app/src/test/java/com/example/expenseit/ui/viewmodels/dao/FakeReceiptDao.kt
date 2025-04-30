package com.example.expenseit.ui.viewmodels.dao

import com.example.expenseit.data.local.db.ReceiptDao
import com.example.expenseit.data.local.entities.Receipt
import com.example.expenseit.data.local.entities.ReceiptItem
import com.example.expenseit.data.local.entities.ReceiptWithItems
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeReceiptDao : ReceiptDao {
    // backing state
    private val _receipts = MutableStateFlow<List<Receipt>>(emptyList())
    private val _items = mutableMapOf<Int, MutableList<ReceiptItem>>()

    // Helpers for Test to seed:
    fun emitReceipts(list: List<Receipt>) {
        _receipts.value = list.toList()
        // whenever you emit a new receipt you probably want to clear + repopulate items map
        _items.clear()
        list.forEach { _items[it.id] = mutableListOf() }
    }
    fun emitReceiptsWithItems(rwis: List<ReceiptWithItems>) {
        _receipts.value = rwis.map { it.receipt }
        _items.clear()
        rwis.forEach { _items[it.receipt.id] = it.items.toMutableList() }
    }

    // DAO impl:
    override suspend fun insertReceipt(receipt: Receipt): Long {
        // assign a new ID
        val newId = if (_receipts.value.isEmpty()) {
            1L // Start with 1 if the list is empty
        } else {
            _receipts.value.maxOf { it.id } + 1L
        }

        val saved = receipt.copy(id = newId.toInt())
        _receipts.update { it + saved }
        _items[saved.id] = mutableListOf()
        return newId
    }

    override suspend fun insertReceiptItems(items: List<ReceiptItem>) {
        items.forEach { item ->
            val list = _items[item.receiptId] ?: mutableListOf<ReceiptItem>().also {
                _items[item.receiptId] = it
            }
            list.add(item)
        }
    }

    override suspend fun getAllReceipts(): List<Receipt> =
        _receipts.value.toList()

    override suspend fun getItemsForReceipt(receiptId: Int): List<ReceiptItem> =
        _items[receiptId]?.toList() ?: emptyList()

    override suspend fun getReceiptById(receiptId: Int): Receipt? =
        _receipts.value.find { it.id == receiptId }

    override suspend fun updateReceipt(receipt: Receipt) {
        _receipts.update { list ->
            list.map { if (it.id == receipt.id) receipt else it }
        }
    }

    override suspend fun updateReceiptItem(item: ReceiptItem) {
        _items[item.receiptId]?.let { list ->
            list.replaceAll { if (it.id == item.id) item else it }
        }
    }

    override suspend fun deleteReceiptById(receiptId: Int) {
        _receipts.update { it.filterNot { it.id == receiptId } }
        _items.remove(receiptId)
    }

    override suspend fun getReceiptsWithItems(): List<ReceiptWithItems> =
        _receipts.value.map { receipt ->
            ReceiptWithItems(receipt, _items[receipt.id]?.toList() ?: emptyList())
        }

    override suspend fun getReceiptWithItems(receiptId: Int): ReceiptWithItems? {
        val r = _receipts.value.find { it.id == receiptId } ?: return null
        return ReceiptWithItems(r, _items[receiptId]?.toList() ?: emptyList())
    }
}
