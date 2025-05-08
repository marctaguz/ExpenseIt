package com.example.expenseit.ui.viewmodels.dao

import com.example.expenseit.data.repository.CurrencyDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeCurrencyDataSource : CurrencyDataSource {
    private val _cur = MutableStateFlow("$")
    override val currency: Flow<String> = _cur

    override suspend fun setCurrency(value: String) {
        _cur.value = value
    }

    // helper to simulate external updates:
    fun emit(value: String) { _cur.value = value }
}