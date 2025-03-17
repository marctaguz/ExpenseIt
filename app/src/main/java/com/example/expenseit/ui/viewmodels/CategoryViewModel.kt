package com.example.expenseit.ui.viewmodels

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expenseit.data.local.db.CategoryDao
import com.example.expenseit.data.local.entities.Category
import com.example.expenseit.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val categoryDao: CategoryDao
) : ViewModel() {

    val categories: StateFlow<List<Category>> = categoryDao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            categoryRepository.initializeDefaultCategories()
        }
    }


    fun addCategory(name: String, color: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val newCategory = Category(
                name = name.trim(),
                order = categories.value.size,
                color = color
            )
            categoryDao.insert(newCategory)
            onSuccess()
        }
    }

    fun updateCategory(category: Category, newName: String, newColor: String) {
        viewModelScope.launch {
            categoryDao.update(category.copy(
                name = newName.trim(),
                color = newColor
            ))
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            categoryDao.delete(category)
        }
    }

    fun updateCategoryOrder(updatedCategories: List<Category>) {
        viewModelScope.launch {
            categoryDao.updateOrder(updatedCategories)
        }
    }
}