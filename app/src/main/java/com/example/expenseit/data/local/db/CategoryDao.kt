package com.example.expenseit.data.local.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.expenseit.data.local.entities.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert
    suspend fun insert(category: Category)

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategoryById(categoryId: Long)

    @Query("SELECT * FROM categories ORDER BY `order` ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<Category>)

    @Query("UPDATE categories SET `order` = :newOrder WHERE id = :categoryId")
    suspend fun updateCategoryOrder(categoryId: Long, newOrder: Int)

    @Transaction
    suspend fun updateOrder(categories: List<Category>) {
        categories.forEachIndexed { index, category ->
            updateCategoryOrder(category.id, index)
        }
    }

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    fun getCategoryById(categoryId: Long): Flow<Category?>
}