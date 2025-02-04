package com.example.expenseit.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.expenseit.data.local.db.ExpenseDatabase
import com.example.expenseit.ui.theme.ExpenseItTheme
import com.example.expenseit.ui.viewmodels.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ExpenseDatabase.initializeDefaultCategories(this)


        setContent {
            ExpenseItTheme {
                ExpenseTrackerApp()
            }
        }
    }
}