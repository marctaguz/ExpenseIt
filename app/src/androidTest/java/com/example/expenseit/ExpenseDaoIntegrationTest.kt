package com.example.expenseit

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.expenseit.data.local.db.CategoryDao
import com.example.expenseit.data.local.db.ExpenseDao
import com.example.expenseit.data.local.db.ExpenseDatabase
import com.example.expenseit.data.local.entities.Category
import com.example.expenseit.data.local.entities.CategoryTotal
import com.example.expenseit.data.local.entities.Expense
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal

@RunWith(AndroidJUnit4::class)
class ExpenseDaoIntegrationTest {
    private lateinit var db: ExpenseDatabase
    private lateinit var expenseDao: ExpenseDao
    private lateinit var categoryDao: CategoryDao

    @Before fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ExpenseDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        expenseDao = db.expenseDao()
        categoryDao = db.categoryDao()

    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test fun insert_update_delete_Expense_lifecycle() = runBlocking {
        categoryDao.insert(Category(id = 1L, name = "TestCat", color = "blue", order = 1))
        // ---- insert
        val e0 = Expense(
            title = "Coffee",
            amount = BigDecimal("3.50"),
            categoryId = 1L,
            description= "Latte",
            date = 1_600_000_000_000L
        )
        val id = expenseDao.insert(e0)
        val loaded = expenseDao.getExpenseById(id)
        assertNotNull(loaded)
        assertEquals("Coffee", loaded!!.title)

        // ---- update
        val e1 = loaded.copy(title = "Tea")
        expenseDao.update(e1)
        val loaded2 = expenseDao.getExpenseById(id)
        assertEquals("Tea", loaded2!!.title)

        // ---- delete
        expenseDao.deleteExpenseById(id)
        assertNull(expenseDao.getExpenseById(id))
    }

    @Test
    fun insert_and_getById_returnsSame() = runBlocking {
        categoryDao.insert(Category(id = 1L, name = "TestCat", color = "blue", order = 1))

        val expense = Expense(
            title = "Test Coffee",
            amount = BigDecimal("2.50"),
            categoryId = 1L,
            description = "Latte",
            date = 1620000000000L
        )
        val generatedId: Long = expenseDao.insert(expense)
        val loaded: Expense? = expenseDao.getExpenseById(generatedId)
        Assert.assertNotNull(loaded)
        Assert.assertEquals("Test Coffee", loaded!!.title)
        Assert.assertEquals(BigDecimal("2.50"), loaded.amount)
    }

    @Test
    fun getAllExpensesByMonth_sumsCorrectly() = runBlocking {
        categoryDao.insert(Category(id = 1L, name = "Stub", color = "gray", order = 1))

        val baseTime   = 1620000000000L // May 3 2021
        val otherMonth = 1622505600000L // June 1 2021

        expenseDao.insert(Expense(title="A", amount=BigDecimal("1.00"), categoryId=1, description="", date=baseTime))
        expenseDao.insert(Expense(title="B", amount=BigDecimal("2.00"), categoryId=1, description="", date=baseTime + 1000))
        expenseDao.insert(Expense(title="C", amount=BigDecimal("5.00"), categoryId=1, description="", date=otherMonth))

        val summaries = expenseDao.getAllExpensesByMonth().first()

        assertTrue(summaries.any { it.month == "2021-05" && it.total == BigDecimal("3.00") })
        assertTrue(summaries.any { it.month == "2021-06" && it.total == BigDecimal("5.00") })
    }

    @Test fun getCategoryTotals_sums_across_categories() = runBlocking {
        // first seed categories (so FK passes)
        categoryDao.insert(Category(id = 1L, name = "Food",  color = "red", order = 1))
        categoryDao.insert(Category(id = 2L, name = "Fuel",  color = "blue", order = 2))

        // three expenses: two food, one fuel
        expenseDao.insert(Expense(title = "Burger", amount = BigDecimal("10.00"), categoryId=1L, description="", date=1))
        expenseDao.insert(Expense(title = "Fries",  amount = BigDecimal("5.00"),  categoryId=1L, description="", date=1))
        expenseDao.insert(Expense(title = "Gas",    amount = BigDecimal("20.00"), categoryId=2L, description="", date=1))

        val totals: List<CategoryTotal> = expenseDao
            .getCategoryTotals()
            .first()

        // verify sums per category name
        assertEquals(2, totals.size)
        assertTrue(totals.any { it.categoryName=="Food" && it.total==BigDecimal("15.00") })
        assertTrue(totals.any { it.categoryName=="Fuel" && it.total==BigDecimal("20.00") })
    }

    @Test fun getCategoryTotalsForMonth_filters_to_one_month_only() = runBlocking {
        categoryDao.insert(Category(id = 1L, name="Food", color="red", order = 1))

        // May and June
        val mayTime = 1_620_000_000_000L
        val junTime = 1_622_505_600_000L
        expenseDao.insert(Expense(title = "M",  amount = BigDecimal("7.00"),  categoryId=1L, description="", date=mayTime))
        expenseDao.insert(Expense(title = "J",  amount = BigDecimal("3.00"),  categoryId=1L, description="", date=junTime))

        val mayTotals = expenseDao
            .getCategoryTotalsForMonth("2021-05")
            .first()

        assertEquals(1, mayTotals.size)
        assertEquals(BigDecimal("7.00"), mayTotals[0].total)
    }
}