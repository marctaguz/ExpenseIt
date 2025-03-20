package com.example.expenseit.ui

import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.expenseit.R
import com.example.expenseit.ui.components.ExpenseItem
import com.example.expenseit.ui.theme.PurpleGrey40
import com.example.expenseit.ui.viewmodels.CategoryViewModel
import com.example.expenseit.ui.viewmodels.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ExpenseListScreen(
    navController: NavController,
    expenseViewModel: ExpenseViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    modifier: Modifier,
    scrollToTop: Boolean,
    onScrollToTopCompleted: () -> Unit
) {
    val lazyListState = rememberLazyListState()
    val expenses by expenseViewModel.expenses.collectAsState()
    val categories by categoryViewModel.categories.collectAsState()

    val groupedExpenses = expenses.groupBy { expense ->
        SimpleDateFormat("MMM dd,yyyy", Locale.getDefault()).format(expense.date)
    }

    LaunchedEffect(Unit) {
        categoryViewModel.loadCategories()
    }

    LaunchedEffect(scrollToTop) {
        if (scrollToTop) {
            lazyListState.animateScrollToItem(
                index = 0,
                scrollOffset = 0
            )
            onScrollToTopCompleted()
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(top = 16.dp),
            ) {
                Image(painter = painterResource(id = R.drawable.expenseit_logo),
                    modifier = Modifier.size(26.dp),
                    contentDescription = null)
                Text(text = "ExpenseIt", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp))
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("add_expense") {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.padding(bottom = 100.dp)
            )
            {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Expense")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
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
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize()
                        .padding(bottom = 100.dp)
                ) {
                    groupedExpenses.forEach { (date, expensesForDate) ->
                        // Date Header
                        item {
                            Text(
                                text = date,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Light,
                                color = PurpleGrey40,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }
                        // Expense items for this date
                        items(expensesForDate) { expense ->
                            ExpenseItem(expense = expense, categoryViewModel = categoryViewModel, onClick = {
                                navController.navigate("add_expense/${expense.id}")
                            })
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }

        }
    }


}

