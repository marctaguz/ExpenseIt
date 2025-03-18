package com.example.expenseit.data.repository

import com.example.expenseit.data.local.db.CategoryDao
import com.example.expenseit.data.local.entities.Category
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    suspend fun initializeDefaultCategories() {
        val existingCategories = categoryDao.getAllCategories().first()

        if (existingCategories.isEmpty()) {
            val defaultCategories = listOf(
                Category(id = 1, name = "Uncategorized", order = 0, color = "categoryColour1"),
                Category(name = "Entertainment", order = 1, color = "categoryColour1"),
                Category(name = "Transport", order = 2, color = "categoryColour2"),
                Category(name = "Grocery", order = 3, color = "categoryColour3"),
                Category(name = "Food", order = 4, color = "categoryColour4"),
                Category(name = "Shopping", order = 5, color = "categoryColour5"),
                Category(name = "Bills", order = 6, color = "categoryColour6"),
                Category(name = "Education", order = 7, color = "categoryColour7"),
                Category(name = "Health", order = 8, color = "categoryColour8"),
                Category(name = "Other", order = 9, color = "categoryColour9")
            )

            categoryDao.insertAll(defaultCategories)
        }
    }

    suspend fun getCategoryByName(name: String): Category? {
        return categoryDao.getAllCategories().first().find { it.name == name }
    }
}