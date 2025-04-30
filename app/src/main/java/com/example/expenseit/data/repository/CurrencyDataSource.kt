package com.example.expenseit.data.repository

import kotlinx.coroutines.flow.Flow

interface CurrencyDataSource {
    val currency: Flow<String>
    suspend fun setCurrency(value: String)
}