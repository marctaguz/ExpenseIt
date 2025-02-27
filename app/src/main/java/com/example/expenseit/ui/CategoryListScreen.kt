package com.example.expenseit.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialogDefaults.containerColor
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.expenseit.data.local.db.CategoryDao
import com.example.expenseit.data.local.entities.Category
import com.example.expenseit.ui.components.PageHeader
import com.example.expenseit.ui.viewmodels.CategoryViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    categoryViewModel: CategoryViewModel,
) {
    val categories by categoryViewModel.categories.collectAsState()
    var isEditMode by remember { mutableStateOf(false) }
    var addingNewCategory by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    val dragDropState = rememberDragDropState(listState) { fromIndex, toIndex ->
        val updatedCategories = categories.toMutableList().apply {
            add(toIndex, removeAt(fromIndex))
        }
        categoryViewModel.updateCategoryOrder(updatedCategories)
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                PageHeader(
                    title = if (isEditMode) "Edit Categories" else "Select Category",
                    actionButtonVisible = true,
                    onClose = { navController.popBackStack() }
                )
                IconButton(
                    onClick = { isEditMode = !isEditMode },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = if (isEditMode) Icons.Default.Done else Icons.Default.Edit,
                        contentDescription = if (isEditMode) "Done" else "Edit",
                        tint = Color.White
                    )
                }
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
                        .height(600.dp)
                        .let {
                            if (isEditMode) it.dragContainer(dragDropState) else it
                        },
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(categories, key = { _, item -> item.id }) { index, category ->
                        if (isEditMode) {
                            DraggableItem(dragDropState, index) { isDragging ->
                                EditModeCategoryItem(
                                    category = category,
                                    isDragging = isDragging,
                                    dragDropState = dragDropState, // Pass the dragDropState
                                    onRename = { newName ->
                                        categoryViewModel.updateCategory(category, newName)
                                    },
                                    onDelete = {
                                        categoryViewModel.deleteCategory(category)
                                    }
                                )
                            }
                        } else {
                            TextButton(
                                onClick = {
                                    navController.previousBackStackEntry?.savedStateHandle?.set(
                                        "selectedCategory",
                                        category.name
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

                // Add New Category Row at the Bottom
                if (isEditMode) {
                    if (addingNewCategory) {
                        // Editable Row for New Category
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.LightGray)
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextField(
                                value = newCategoryName,
                                onValueChange = { newCategoryName = it },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                placeholder = { Text("Enter category name") },
                                colors = TextFieldDefaults.colors(
                                    //setting the text field background when it is focused
                                    focusedContainerColor = Color.Transparent,
                                    //setting the text field background when it is unfocused or initial state
                                    unfocusedContainerColor = Color.Transparent,
                                    //setting the text field background when it is disabled
                                    disabledContainerColor = Color.Transparent,
                                ),
                            )
                            IconButton(
                                onClick = {
                                    if (newCategoryName.isNotBlank()) {
                                        categoryViewModel.addCategory(newCategoryName) {
                                            newCategoryName = ""
                                            addingNewCategory = false
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Done,
                                    contentDescription = "Save New Category",
                                    tint = Color.Green
                                )
                            }
                        }
                    } else {
                        // Add New Category Button
                        TextButton(
                            onClick = { addingNewCategory = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.LightGray)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Add New Category",
                                textAlign = TextAlign.Center,
                                color = Color.Blue,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditModeCategoryItem(
    category: Category,
    isDragging: Boolean,
    dragDropState: DragDropState,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
) {
    var isEditing by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(category.name) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isDragging) Color.LightGray else Color.Transparent)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Drag Handle (Hamburger Icon)
        Box(
            modifier = Modifier
                .size(24.dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            dragDropState.onDragStart(offset)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragDropState.onDrag(dragAmount)
                        },
                        onDragEnd = {
                            dragDropState.onDragInterrupted()
                        },
                        onDragCancel = {
                            dragDropState.onDragInterrupted()
                        }
                    )
                }
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Drag Handle",
                tint = Color.Gray,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        if (isEditing) {
            TextField(
                value = newName,
                onValueChange = { newName = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    //setting the text field background when it is focused
                    focusedContainerColor = Color.Transparent,
                    //setting the text field background when it is unfocused or initial state
                    unfocusedContainerColor = Color.Transparent,
                    //setting the text field background when it is disabled
                    disabledContainerColor = Color.Transparent,
                ),
            )
            IconButton(
                onClick = {
                    if (newName.isNotBlank()) {
                        onRename(newName.trim())
                    }
                    isEditing = false
                }
            ) {
                Icon(imageVector = Icons.Default.Done, contentDescription = "Save", tint = Color.Green)
            }
        } else {
            Text(
                text = category.name,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                textAlign = TextAlign.Left
            )
            IconButton(onClick = { isEditing = true }) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Rename", tint = Color.Blue)
            }
        }

        IconButton(onClick = onDelete) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
        }
    }
}

