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
    private val ioScope = CoroutineScope(Dispatchers.IO)  // ✅ Define a scope for background tasks

    fun initializeDefaultCategories() {
        ioScope.launch {
            val existingCategories = categoryDao.getAllCategories().first() // ✅ Wait for first emission

            if (existingCategories.isEmpty()) {
                val defaultCategories = listOf(
                    Category(name = "Entertainment", order = 1),
                    Category(name = "Transport", order = 2),
                    Category(name = "Grocery", order = 3),
                    Category(name = "Food", order = 4),
                    Category(name = "Shopping", order = 5),
                    Category(name = "Bills", order = 6),
                    Category(name = "Education", order = 7),
                    Category(name = "Health", order = 8),
                    Category(name = "Other", order = 9)
                )

                categoryDao.insertAll(defaultCategories) // ✅ Insert all at once
            }
        }
    }
}