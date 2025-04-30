package com.example.expenseit.ui.viewmodels

import com.example.expenseit.data.local.entities.Expense
import com.example.expenseit.ui.viewmodels.dao.FakeExpenseDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeDao: FakeExpenseDao
    private lateinit var vm: ExpenseViewModel

    @Before fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeDao = FakeExpenseDao()
        vm = ExpenseViewModel(fakeDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()  // restore the original Main dispatcher
    }

    @Test fun `initial expenses is empty`() = runTest {
        assertTrue(vm.expenses.value.isEmpty())
    }

    @Test fun `loadExpenseById emits correct expense`() = runTest {
        // arrange
        val e = Expense( id = 42L, title = "Ticket", amount = 10.00.toBigDecimal(),
            categoryId = 2L, description = "Bus", date = 11111L,
            receiptId = null )
        fakeDao.emitAllExpenses(listOf(e))

        // act
        vm.loadExpenseById(42L)

        // advance the test scheduler so that the viewModelScope.launch { … } actually runs
        advanceUntilIdle()
        // assert
        assertEquals(e, vm.expense.value)
    }

    @Test fun `addExpense inserts and emits new list`() = runTest {
        var successCalled = false
        vm.addExpense("Coffee", "3.50".toBigDecimal(), 1L, "Latte", 12345L) { successCalled = true }

        // give the  viewModelScope.launch { … } chance to finish
        advanceUntilIdle()

        val list = vm.expenses.value
        assertEquals(1, list.size)
        assertTrue(successCalled)
        assertEquals("Coffee", list.first().title)
    }



    @Test fun `updateExpense updates and emits new list`() = runTest {
        // seed
        val original = Expense(
            id = 1L,
            title = "Old",
            amount = BigDecimal("1.00"),
            categoryId = 1L,
            description = "",
            date = 0L,
            receiptId = null
        )
        fakeDao.emitAllExpenses(listOf(original))

        advanceUntilIdle()

        var updatedCalled = false
        vm.updateExpense(
            expenseId = 1L,
            title = "New",
            amount = BigDecimal("2.00"),
            categoryId = 1L,
            description = "desc",
            date = 999L
        ) { updatedCalled = true }

        advanceUntilIdle()

        val updatedList = vm.expenses.value
        assertEquals(1, updatedList.size)
        val updated = updatedList.first()
        assertEquals("New", updated.title)
        assertEquals(BigDecimal("2.00"), updated.amount)
        assertEquals(999L, updated.date)
        assertTrue(updatedCalled)
    }

    @Test fun `deleteExpense removes and emits new list`() = runTest {
        val a = Expense(id=1,title="A",amount=BigDecimal("1"),categoryId=0,description="",date=0,receiptId=null)
        val b = Expense(id=2,title="B",amount=BigDecimal("2"),categoryId=0,description="",date=0,receiptId=null)
        fakeDao.emitAllExpenses(listOf(a,b))

        var deletedCalled = false
        vm.deleteExpense(1L) { deletedCalled = true }

        advanceUntilIdle()

        val listAfter = vm.expenses.value
        assertEquals(1, listAfter.size)
        assertEquals("B", listAfter.first().title)
        assertTrue(deletedCalled)
    }

}
