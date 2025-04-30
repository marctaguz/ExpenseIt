package com.example.expenseit.ui.viewmodels.dao

import com.example.expenseit.data.local.db.CategoryDao
import com.example.expenseit.data.local.entities.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeCategoryDao : CategoryDao {
    private val _categories = MutableStateFlow<List<Category>>(emptyList())

    // Helper method for tests to emit categories
    fun emitCategories(categories: List<Category>) {
        _categories.value = categories
    }

    override fun getAllCategories(): Flow<List<Category>> = _categories

    override suspend fun insert(category: Category) {
        // Generate ID if needed
        val categoryWithId = if (category.id == 0L) {
            val newId = (_categories.value.maxOfOrNull { it.id } ?: 0) + 1
            category.copy(id = newId)
        } else {
            category
        }

        // Add to list
        _categories.value = _categories.value + categoryWithId
    }

    override suspend fun update(category: Category) {
        _categories.value = _categories.value.map {
            if (it.id == category.id) category else it
        }
    }

    override suspend fun delete(category: Category) {
        _categories.value = _categories.value.filter { it.id != category.id }
    }

    override suspend fun deleteCategoryById(categoryId: Long) {
        _categories.value = _categories.value.filter { it.id != categoryId }
    }

    override suspend fun insertAll(categories: List<Category>) {
        // For simplicity, just replace all categories
        _categories.value = categories
    }

    override suspend fun updateCategoryOrder(categoryId: Long, newOrder: Int) {
        _categories.value = _categories.value.map {
            if (it.id == categoryId) it.copy(order = newOrder) else it
        }
    }

    override suspend fun updateOrder(categories: List<Category>) {
        val updatedCategories = _categories.value.toMutableList()

        // Update each category's order based on the provided list
        categories.forEachIndexed { index, category ->
            val existingIndex = updatedCategories.indexOfFirst { it.id == category.id }
            if (existingIndex != -1) {
                updatedCategories[existingIndex] = updatedCategories[existingIndex].copy(order = index)
            }
        }

        // Sort by order to simulate real DAO behavior
        _categories.value = updatedCategories.sortedBy { it.order }
    }

    override fun getCategoryById(categoryId: Long): Flow<Category?> {
        return _categories.map { categories ->
            categories.find { it.id == categoryId }
        }
    }
}