package com.example.expenseit.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import coil.Coil
import coil.request.ImageRequest
import com.example.expenseit.data.local.db.ReceiptDao
import com.example.expenseit.data.local.entities.Expense
import com.example.expenseit.data.local.entities.Receipt
import com.example.expenseit.data.local.entities.ReceiptItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ReceiptViewModel @Inject constructor(
    private val receiptDao: ReceiptDao
) : ViewModel() {

    private val _receipts = MutableStateFlow<List<Receipt>>(emptyList())
    val receipts: StateFlow<List<Receipt>> = _receipts.asStateFlow()

    init {
        loadAllReceipts()  // Load all receipts on startup
    }

    fun loadAllReceipts() {
        viewModelScope.launch {
            val allReceipts = receiptDao.getAllReceipts()
            _receipts.value = allReceipts
        }
    }

    fun preloadImages(context: Context, receipts: List<Receipt>) {
        viewModelScope.launch(Dispatchers.IO) {
            receipts.forEach { receipt ->
                val request = ImageRequest.Builder(context)
                    .data(receipt.imageUrl)
                    .build()
                Coil.imageLoader(context).enqueue(request) // Use the default ImageLoader
            }
        }
    }

    fun insertReceipt(receipt: Receipt, items: List<ReceiptItem>) {
        viewModelScope.launch {
            receiptDao.insertReceiptWithItems(receipt, items)
            loadAllReceipts()
        }
    }

    fun getReceiptById(receiptId: Int, onResult: (Receipt?, List<ReceiptItem>) -> Unit) {
        viewModelScope.launch {
            val receipt = receiptDao.getReceiptById(receiptId)
            val items = receiptDao.getItemsForReceipt(receiptId)
            onResult(receipt, items)
        }
    }

    fun updateReceipt(receipt: Receipt) {
        viewModelScope.launch {
            receiptDao.updateReceipt(receipt)
        }
    }

    fun updateReceiptItems(items: List<ReceiptItem>) {
        viewModelScope.launch(Dispatchers.IO) {
            items.forEach { item ->
                receiptDao.updateReceiptItem(item)
            }
        }
    }

    fun updateReceiptItem(item: ReceiptItem, onSuccess: () -> Unit) {
        viewModelScope.launch {
            receiptDao.updateReceiptItem(item)
            onSuccess()
        }
    }

    fun deleteReceipt(receiptId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            receiptDao.deleteReceiptById(receiptId)
            loadAllReceipts()
            onSuccess()
        }
    }
}
