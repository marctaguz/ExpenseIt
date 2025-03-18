package com.example.expenseit.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.expenseit.R
import com.example.expenseit.data.local.entities.Category
import com.example.expenseit.ui.components.PageHeader
import com.example.expenseit.ui.theme.categoryColors
import com.example.expenseit.ui.viewmodels.CategoryViewModel
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCategoriesScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val categoryViewModel: CategoryViewModel = hiltViewModel()
    val categories by categoryViewModel.categories.collectAsState()
    Log.d("EditCategoriesScreen", "Categories: $categories")
    var addingNewCategory by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var newCategoryColor by remember { mutableStateOf("categoryColour1") }

    // Local state for the list
    var localCategories by remember { mutableStateOf(categories) }

    // Update localCategories when categories change (e.g., from ViewModel)
    LaunchedEffect(categories) {
        localCategories = categories
    }

    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { fromIndex, toIndex ->
        // Update the local list immediately for smooth UI updates
        localCategories = localCategories.toMutableList().apply {
            add(toIndex.index, removeAt(fromIndex.index))
        }
    }

    Scaffold(
        topBar = {
            Box(modifier = Modifier.fillMaxWidth()) {
                PageHeader(
                    title = "Edit Categories",
                    actionButtonVisible = true,
                    onClose = {
                        categoryViewModel.updateCategoryOrder(localCategories)
                        navController.popBackStack()
                    }
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
                        .height(800.dp),
                    state = lazyListState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(localCategories, key = { _, item -> item.id }) { index, category ->
                        ReorderableItem(reorderableState, category.id) { isDragging ->
                            EditModeCategoryItem(
                                this,
                                category = category,
                                onRename = { newName ->
                                    categoryViewModel.updateCategory(category, newName, category.color)
                                },
                                onDelete = {
                                    categoryViewModel.deleteCategory(category)
                                },
                                onColorChange = { newColor ->
                                    categoryViewModel.updateCategory(category, category.name, newColor)
                                }
                            )
                        }
                    }
                }

                if (addingNewCategory) {
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
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                            ),
                        )
                        IconButton(
                            onClick = {
                                if (newCategoryName.isNotBlank()) {
                                    categoryViewModel.addCategory(newCategoryName, newCategoryColor) {
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
                        ColorPickerButton(
                            selectedColor = newCategoryColor,
                            onColorSelected = { newCategoryColor = it }
                        )
                    }
                } else {
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
    )
}

@Composable
fun EditModeCategoryItem(
    scope: ReorderableCollectionItemScope,
    category: Category,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
    onColorChange: (String) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(category.name) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        IconButton(
            modifier = with(scope) {
                Modifier.draggableHandle()
            },
            onClick = {},
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Drag Handle",
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }

        ColorPickerButton(
            selectedColor = category.color,
            onColorSelected = onColorChange
        )

        if (isEditing) {
            TextField(
                value = newName,
                onValueChange = { newName = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
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
                Icon(imageVector = Icons.Default.Done, contentDescription = "Save")
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
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Rename")
            }
        }

        IconButton(onClick = onDelete) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
        }
    }
}

@Composable
fun ColorPickerButton(
    selectedColor: String,
    onColorSelected: (String) -> Unit
) {
    var showColorPicker by remember { mutableStateOf(false) }

    IconButton(onClick = { showColorPicker = true }) {
        Icon(
            painter = painterResource(R.drawable.ic_tag),
            contentDescription = "Change Color",
            tint = categoryColors[selectedColor] ?: Color.Gray,
            modifier = Modifier.size(24.dp)
        )
    }

    if (showColorPicker) {
        ColorPickerDialog(
            onDismiss = { showColorPicker = false },
            onColorSelected = onColorSelected
        )
    }
}

@Composable
fun ColorPickerDialog(
    onDismiss: () -> Unit,
    onColorSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select a Color") },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                categoryColors.forEach { (colorName, color) ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(color)
                            .clickable { onColorSelected(colorName) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}