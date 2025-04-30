package com.example.expenseit.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expenseit.data.repository.CurrencyDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataSource: CurrencyDataSource
) : ViewModel() {
    private val _currency = MutableStateFlow<String>("$")
    val currency: StateFlow<String> = _currency

    init {
        viewModelScope.launch {
            dataSource.currency.collect { _currency.value = it }
        }
    }

    // Method to update the currency in DataStore
    fun setCurrency(newCurrency: String) {
        viewModelScope.launch { dataSource.setCurrency(newCurrency) }
    }
}
