package com.example.expenseit.ui.viewmodels

import androidx.lifecycle.ViewModel

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ReceiptViewModel @Inject constructor() : ViewModel() {
    var receiptData: String? = null
        private set

    fun setReceiptData(data: String) {
        receiptData = data
    }
}