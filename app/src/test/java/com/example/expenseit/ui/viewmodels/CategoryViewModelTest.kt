package com.example.expenseit.ui.viewmodels

import com.example.expenseit.data.local.entities.Category
import com.example.expenseit.data.repository.CategoryRepository
import com.example.expenseit.ui.viewmodels.dao.FakeCategoryDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CategoryViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeCategoryDao: FakeCategoryDao
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var viewModel: CategoryViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeCategoryDao = FakeCategoryDao()
        categoryRepository = CategoryRepository(fakeCategoryDao)
        viewModel = CategoryViewModel(categoryRepository, fakeCategoryDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // restore the original Main dispatcher
    }

    @Test
    fun `initial categories list contains default categories`() = runTest {
        // Let the initialization coroutine complete
        advanceUntilIdle()

        // Verify default categories were created
        assertTrue(viewModel.categories.value.isNotEmpty())
        assertEquals(10, viewModel.categories.value.size)
        assertEquals("Uncategorized", viewModel.categories.value[0].name)
    }

    @Test
    fun `addCategory adds new category and invokes success callback`() = runTest {
        // Complete initialization first
        advanceUntilIdle()

        val initialCount = viewModel.categories.value.size

        var successCalled = false
        viewModel.addCategory("New Category", "categoryColour10") {
            successCalled = true
        }

        advanceUntilIdle()

        // Verify category was added
        assertEquals(initialCount + 1, viewModel.categories.value.size)
        assertTrue(viewModel.categories.value.any { it.name == "New Category" })
        assertTrue(successCalled)
    }

    @Test
    fun `updateCategory modifies existing category`() = runTest {
        // Setup with initial data
        val category = Category(id = 1, name = "Test", order = 0, color = "categoryColour1")
        fakeCategoryDao.emitCategories(listOf(category))
        advanceUntilIdle()

        // Update the category
        viewModel.updateCategory(category, "Updated Category", "categoryColour2")
        advanceUntilIdle()

        // Verify changes
        val updatedCategory = viewModel.categories.value.find { it.id == 1L }
        assertNotNull(updatedCategory)
        assertEquals("Updated Category", updatedCategory?.name)
        assertEquals("categoryColour2", updatedCategory?.color)
    }

    @Test
    fun `deleteCategory removes category from list`() = runTest {
        // Setup with initial data
        val category1 = Category(id = 1, name = "Category 1", order = 0, color = "categoryColour1")
        val category2 = Category(id = 2, name = "Category 2", order = 1, color = "categoryColour2")
        fakeCategoryDao.emitCategories(listOf(category1, category2))
        advanceUntilIdle()

        // Delete one category
        viewModel.deleteCategory(category1)
        advanceUntilIdle()

        // Verify it was removed
        assertEquals(1, viewModel.categories.value.size)
        assertEquals(category2.id, viewModel.categories.value[0].id)
    }

    @Test
    fun `updateCategoryOrder changes order of categories`() = runTest {
        // Setup initial categories
        val categories = listOf(
            Category(id = 1, name = "First", order = 0, color = "categoryColour1"),
            Category(id = 2, name = "Second", order = 1, color = "categoryColour2"),
            Category(id = 3, name = "Third", order = 2, color = "categoryColour3")
        )
        fakeCategoryDao.emitCategories(categories)
        advanceUntilIdle()

        // Reorder the categories (move "Third" to the first position)
        val reorderedCategories = listOf(
            categories[2].copy(order = 0),
            categories[0].copy(order = 1),
            categories[1].copy(order = 2)
        )

        viewModel.updateCategoryOrder(reorderedCategories)
        advanceUntilIdle()

        // Verify the new order
        assertEquals("Third", viewModel.categories.value[0].name)
        assertEquals("First", viewModel.categories.value[1].name)
        assertEquals("Second", viewModel.categories.value[2].name)
    }

    @Test
    fun `getCategoryById returns correct category`() = runTest {
        // Setup with initial data
        val category = Category(id = 42L, name = "Test Category", order = 0, color = "categoryColour1")
        fakeCategoryDao.emitCategories(listOf(category))
        advanceUntilIdle()

        // Get category by ID
        val result = viewModel.getCategoryById(42L).first()

        // Verify correct category was returned
        assertNotNull(result)
        assertEquals(42L, result?.id)
        assertEquals("Test Category", result?.name)
    }

    @Test
    fun `categoryCount reflects the number of categories`() = runTest {
        // Setup with initial data
        val categories = listOf(
            Category(id = 1, name = "One", order = 0, color = "categoryColour1"),
            Category(id = 2, name = "Two", order = 1, color = "categoryColour2"),
            Category(id = 3, name = "Three", order = 2, color = "categoryColour3")
        )
        fakeCategoryDao.emitCategories(categories)
        advanceUntilIdle()

        // Verify count is updated
        assertEquals(3, viewModel.categoryCount.value)
    }
}