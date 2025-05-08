package com.example.expenseit.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import coil.Coil
import coil.request.ImageRequest
import com.example.expenseit.data.local.db.ReceiptDao
import com.example.expenseit.data.local.entities.Receipt
import com.example.expenseit.data.local.entities.ReceiptItem
import com.example.expenseit.data.local.entities.ReceiptWithItems
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

    private val _receiptsWithItems = MutableStateFlow<List<ReceiptWithItems>>(emptyList())
    val receiptsWithItems: StateFlow<List<ReceiptWithItems>> = _receiptsWithItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    init {
        loadAllReceipts()  // Load all receipts on startup
        loadAllReceiptsWithItems()
    }

    fun loadAllReceiptsWithItems() {
        _isLoading.value = true
        viewModelScope.launch {
            val receipts = receiptDao.getReceiptsWithItems()
            _receiptsWithItems.value = receipts
            _isLoading.value = false
        }
    }

    fun loadAllReceipts() {
        _isLoading.value = true
        viewModelScope.launch {
            val allReceipts = receiptDao.getAllReceipts()
            _receipts.value = allReceipts
            _isLoading.value = false
        }
    }

    fun preloadImages(context: Context, receipts: List<Receipt>) {
        viewModelScope.launch(Dispatchers.IO) {
            receipts.forEach { receipt ->
                val request = ImageRequest.Builder(context)
                    .data(receipt.imageUrl)
                    .build()
                Coil.imageLoader(context).enqueue(request)
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
        viewModelScope.launch {
            items.forEach { item ->
                receiptDao.updateReceiptItem(item)
            }
        }
    }

    fun deleteReceipt(receiptId: Int) {
        viewModelScope.launch {
            receiptDao.deleteReceiptById(receiptId)
            loadAllReceipts()
        }
    }
}
