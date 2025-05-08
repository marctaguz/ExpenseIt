package com.example.expenseit

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.expenseit.data.local.db.CategoryDao
import com.example.expenseit.data.local.db.ExpenseDatabase
import com.example.expenseit.data.local.entities.Category
import junit.framework.TestCase.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CCategoryDaoIntegrationTest {
    private lateinit var db: ExpenseDatabase
    private lateinit var categoryDao: CategoryDao

    @Before fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ExpenseDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        categoryDao = db.categoryDao()
    }

    @After fun tearDown() {
        db.close()
    }

    @Test fun insert_and_getAllCategories_returnsInserted() = runBlocking {
        val cat = Category(id = 1L, name = "Food", color = "red", order = 0)
        categoryDao.insert(cat)

        val all = categoryDao.getAllCategories().first()
        assertEquals(1, all.size)
        assertEquals(cat, all[0])
    }

    @Test fun insertAll_and_getAllCategories_returnsAll() = runBlocking {
        val cats = listOf(
            Category(id = 1L, name = "A", color = "c1", order = 0),
            Category(id = 2L, name = "B", color = "c2", order = 1),
            Category(id = 3L, name = "C", color = "c3", order = 2)
        )
        categoryDao.insertAll(cats)

        val all = categoryDao.getAllCategories().first()
        assertEquals(3, all.size)
        // Should be ordered by 'order' ASC
        assertEquals(listOf("A","B","C"), all.map { it.name })
    }

    @Test fun update_category_modifiesExisting() = runBlocking {
        val cat = Category(id = 1L, name = "Old", color = "grey", order = 0)
        categoryDao.insert(cat)

        val updated = cat.copy(name = "New", color = "blue")
        categoryDao.update(updated)

        val fetched = categoryDao.getAllCategories().first().first()
        assertEquals("New", fetched.name)
        assertEquals("blue", fetched.color)
    }

    @Test fun delete_and_deleteById_removesCategory() = runBlocking {
        val cat1 = Category(id = 1L, name = "X", color = "c", order = 0)
        val cat2 = Category(id = 2L, name = "Y", color = "d", order = 1)
        categoryDao.insertAll(listOf(cat1, cat2))

        // delete by entity
        categoryDao.delete(cat1)
        var remaining = categoryDao.getAllCategories().first()
        assertEquals(1, remaining.size)
        assertEquals(2L, remaining[0].id)

        // delete by id
        categoryDao.deleteCategoryById(2L)
        remaining = categoryDao.getAllCategories().first()
        assertTrue(remaining.isEmpty())
    }

    @Test fun updateOrder_reordersCorrectly() = runBlocking {
        // initial order is [0:A,1:B,2:C]
        val cats = listOf(
            Category(id = 1L, name = "A", color="", order = 0),
            Category(id = 2L, name = "B", color="", order = 1),
            Category(id = 3L, name = "C", color="", order = 2)
        )
        categoryDao.insertAll(cats)

        // swap B and C: new order list is [A,C,B]
        val reordered = listOf(
            cats[0].copy(order = 0),
            cats[2].copy(order = 1),
            cats[1].copy(order = 2)
        )
        categoryDao.updateOrder(reordered)

        val fetched = categoryDao.getAllCategories().first()
        assertEquals(listOf("A","C","B"), fetched.map { it.name })
        assertEquals(listOf(0,1,2), fetched.map { it.order })
    }

    @Test fun getCategoryById_emitsCorrectCategoryOrNull() = runBlocking {
        val cat = Category(id = 42L, name = "Special", color="x", order = 5)
        categoryDao.insert(cat)

        val found = categoryDao.getCategoryById(42L).first()
        assertNotNull(found)
        assertEquals("Special", found!!.name)

        val missing = categoryDao.getCategoryById(99L).first()
        assertNull(missing)
    }
}
