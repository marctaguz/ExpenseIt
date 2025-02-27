package com.example.expenseit.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.expenseit.R
import com.example.expenseit.data.local.db.ExpenseDao
import com.example.expenseit.data.local.entities.Expense
import com.example.expenseit.ui.components.ExpenseItem
import com.example.expenseit.ui.viewmodels.ExpenseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.exp

@Composable
fun ExpenseListScreen(navController: NavController, expenseViewModel: ExpenseViewModel = hiltViewModel(), modifier: Modifier) {
    val expenses by expenseViewModel.expenses.collectAsState()

    Scaffold(
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Row {
                Image(painter = painterResource(id = R.drawable.expenseit_logo),
                    modifier = Modifier.size(26.dp),
                    contentDescription = null)
                Text(text = "ExpenseIt", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (expenses.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.expenseit_logo),
                        contentDescription = "No Expenses",
                        modifier = Modifier.size(150.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No expenses to show.",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { navController.navigate("add_expense") },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(text = "Add Your First Expense")
                    }
                }
            } else {
                LazyColumn {
                    items(expenses) { expense ->
                        ExpenseItem(expense = expense, onClick = {
                            navController.navigate("add_expense/${expense.id}")
                        })
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }


}

