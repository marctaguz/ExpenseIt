package com.example.expenseit.data.local.db

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.expenseit.data.repository.CurrencyDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore("settings")

object Keys {
    val CURRENCY = stringPreferencesKey("currency")
}

class SettingsDataStoreManager @Inject constructor(context: Context) : CurrencyDataSource {
    private val dataStore = context.dataStore

    // Flow to read the saved currency
    override val currency: Flow<String> = dataStore.data
        .map { prefs -> prefs[Keys.CURRENCY] ?: "$" }

    // Suspend function to save currency
    override suspend fun setCurrency(value: String) {
        dataStore.edit { prefs -> prefs[Keys.CURRENCY] = value }
    }
}
