package com.example.expenseit.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expenseit.data.local.db.SettingsDataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStoreManager: SettingsDataStoreManager
) : ViewModel() {
    private val _currency = MutableStateFlow<String>("$")
    val currency: StateFlow<String> = _currency

    init {
        // Initialize with the currency from DataStore
        viewModelScope.launch {
            settingsDataStoreManager.currency.collect { storedCurrency ->
                _currency.value = storedCurrency
            }
        }
    }

    // Method to update the currency in DataStore
    fun setCurrency(newCurrency: String) {
        viewModelScope.launch {
            settingsDataStoreManager.setCurrency(newCurrency)
        }
    }
}
