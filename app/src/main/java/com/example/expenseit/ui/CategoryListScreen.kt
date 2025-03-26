package com.example.expenseit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.expenseit.ui.components.PageHeader
import com.example.expenseit.ui.viewmodels.CategoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    categoryViewModel: CategoryViewModel = hiltViewModel(),
) {
    val categories by categoryViewModel.categories.collectAsState()

    LaunchedEffect(Unit) {
        categoryViewModel.loadCategories()
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                PageHeader(
                    title = "Select Category",
                    leftActionButtonVisible = true,
                    onLeftAction = { navController.popBackStack() }
                )
            }
        },
        content = { innerPadding ->
            Column(
                modifier = modifier
                    .padding(innerPadding)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(600.dp),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(categories, key = { _, item -> item.id }) { _, category ->
                        TextButton(
                            onClick = {
                                navController.previousBackStackEntry?.savedStateHandle?.set(
                                    "selectedCategoryId",
                                    category.id
                                )
                                navController.popBackStack()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = category.name,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Left
                            )
                        }
                    }
                }
            }
        }
    )
}

