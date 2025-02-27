package com.example.expenseit.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expenseit.data.local.db.CategoryDao
import com.example.expenseit.data.local.entities.Category
import com.example.expenseit.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val categoryDao: CategoryDao

) : ViewModel() {

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    init {
        categoryRepository.initializeDefaultCategories()
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryDao.getAllCategories().collect { categoryList ->
                _categories.value = categoryList
                Log.d("CategoryViewModel", "Categories Loaded: $categoryList")  // Debugging log

            }
        }
    }

    fun addCategory(name: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val newCategory = Category(name = name.trim(), order = _categories.value.size)
            categoryDao.insert(newCategory)
            loadCategories() // Refresh list
            onSuccess()
        }
    }

    fun updateCategory(category: Category, newName: String) {
        viewModelScope.launch {
            categoryDao.update(category.copy(name = newName.trim()))
            loadCategories() // Refresh list
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            categoryDao.delete(category)
            loadCategories() // Refresh list
        }
    }

    fun updateCategoryOrder(updatedCategories: List<Category>) {
        viewModelScope.launch {
            categoryDao.updateOrder(updatedCategories)
            loadCategories() // Refresh list
        }
    }

}