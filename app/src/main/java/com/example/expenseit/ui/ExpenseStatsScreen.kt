package com.example.expenseit.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.expenseit.ui.components.PageHeader

@Composable
fun ExpenseStatsScreen(navController: NavController, modifier: Modifier) {
    Scaffold(
        topBar = {
            PageHeader(title = "Expense Stats Screen", actionButtonVisible = false)
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding)
            ) {

            }
        }
    )
}