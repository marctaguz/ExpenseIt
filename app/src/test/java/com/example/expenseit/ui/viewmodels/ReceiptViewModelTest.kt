package com.example.expenseit.ui.viewmodels

import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.expenseit.data.local.entities.Receipt
import com.example.expenseit.data.local.entities.ReceiptItem
import com.example.expenseit.data.local.entities.ReceiptWithItems
import com.example.expenseit.ui.viewmodels.dao.FakeReceiptDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ReceiptViewModelTest {
    // swap LiveData executions & Compose’s main dispatcher
    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeDao: FakeReceiptDao
    private lateinit var vm: ReceiptViewModel

    @Before fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeDao = FakeReceiptDao()
        vm = ReceiptViewModel(fakeDao)
    }

    @After fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test fun `initial state is empty and not loading`() = runTest {
        // at construction, init calls loadAllReceipts()/loadAllReceiptsWithItems()
        assertTrue(vm.receipts.value.isEmpty())
        assertTrue(vm.receiptsWithItems.value.isEmpty())
        assertFalse(vm.isLoading.value)
    }

    @Test fun `loadAllReceipts populates receipts and toggles loading`() = runTest {
        // seed
        val r1 = Receipt(id = 1, merchantName="A", date=0, totalPrice=0.toBigDecimal(), imageUrl="")
        val r2 = r1.copy(id=2)
        fakeDao.emitReceipts(listOf(r1, r2))

        vm.loadAllReceipts()
        // still queued
        assertTrue(vm.isLoading.value)

        advanceUntilIdle()
        // after run
        assertFalse(vm.isLoading.value)
        assertEquals(listOf(r1, r2), vm.receipts.value)
    }

    @Test fun `loadAllReceiptsWithItems populates receiptsWithItems`() = runTest {
        // arrange
        val item = ReceiptItem(1,1,"X",1,1.toBigDecimal())
        val rwi  = ReceiptWithItems(
            receipt = Receipt(1, "A", 0, 0.toBigDecimal(), imageUrl="", /* ... */),
            items   = listOf(item)
        )
        fakeDao.emitReceiptsWithItems(listOf(rwi))

        // act
        vm.loadAllReceiptsWithItems()
        advanceUntilIdle()     // run the coroutine

        // assert
        assertFalse(vm.isLoading.value)             // ended up false
        assertEquals(listOf(rwi), vm.receiptsWithItems.value)
    }

    @Test fun `insertReceipt calls DAO and reloads receipts`() = runTest {
        val r = Receipt(id=0, merchantName="M", date=0, totalPrice=0.toBigDecimal(), imageUrl="")
        val items = listOf(
            ReceiptItem(id=0, receiptId=0, itemName="i", quantity=1, price=1.toBigDecimal())
        )

        vm.insertReceipt(r, items)
        advanceUntilIdle()

        // now fakeDao has one new receipt with id = 1
        val loaded = vm.receipts.value
        assertEquals(1, loaded.size)
        assertEquals("M", loaded.first().merchantName)
    }

    @Test fun `getReceiptById invokes onResult callback`() = runTest {
        val r = Receipt(id=42, merchantName="Z", date=0, totalPrice=0.toBigDecimal(), imageUrl="")
        fakeDao.emitReceipts(listOf(r))
        // also add an item for it:
        fakeDao.insertReceiptItems(listOf(
            ReceiptItem(id=1, receiptId=42, itemName="foo", quantity=2, price=2.toBigDecimal())
        ))

        var called = false
        vm.getReceiptById(42) { fetched, items ->
            called = true
            assertEquals(r, fetched)
            assertEquals(1, items.size)
            assertEquals("foo", items[0].itemName)
        }
        advanceUntilIdle()
        assertTrue(called)
    }

    @Test fun `updateReceipt updates existing one`() = runTest {
        val original = Receipt(id=5, merchantName="Old", date=0, totalPrice=0.toBigDecimal(), imageUrl="")
        fakeDao.emitReceipts(listOf(original))

        val updated = original.copy(merchantName="New")
        vm.updateReceipt(updated)
        advanceUntilIdle()

        assertEquals("New", fakeDao.getAllReceipts().first { it.id==5 }.merchantName)
    }

    @Test fun `updateReceiptItems calls DAO for each item`() = runTest {
        val itemA = ReceiptItem(id=2, receiptId=7, itemName="A", quantity=1, price=1.toBigDecimal())
        fakeDao.insertReceiptItems(listOf(itemA))
        val updated = itemA.copy(itemName="ZZZ")
        vm.updateReceiptItems(listOf(updated))
        advanceUntilIdle()

        val fetched = fakeDao.getItemsForReceipt(7)
        assertEquals(1, fetched.size)
        assertEquals("ZZZ", fetched[0].itemName)
    }

    @Test fun `deleteReceipt removes and reloads`() = runTest {
        val r1 = Receipt(id=1, merchantName="A", date=0, totalPrice=0.toBigDecimal(), imageUrl="")
        val r2 = r1.copy(id=2)
        fakeDao.emitReceipts(listOf(r1, r2))

        vm.deleteReceipt(1)
        advanceUntilIdle()

        assertEquals(listOf(r2), vm.receipts.value)
    }
}
