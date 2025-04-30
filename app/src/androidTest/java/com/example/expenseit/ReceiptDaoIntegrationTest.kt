package com.example.expenseit

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.expenseit.data.local.db.ReceiptDao
import com.example.expenseit.data.local.db.ExpenseDatabase
import com.example.expenseit.data.local.entities.Receipt
import com.example.expenseit.data.local.entities.ReceiptItem
import junit.framework.TestCase.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal

@RunWith(AndroidJUnit4::class)
class ReceiptDaoIntegrationTest {
    private lateinit var db: ExpenseDatabase
    private lateinit var dao: ReceiptDao

    @Before fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ExpenseDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        dao = db.receiptDao()
    }

    @After fun tearDown() {
        db.close()
    }

    @Test fun insert_and_getAllReceipts(): Unit = runBlocking {
        val r1 = Receipt(
            merchantName = "Store A",
            date = 1_600_000_000_000L,
            totalPrice = BigDecimal("12.34"),
            imageUrl = "urlA"
        )
        val r2 = Receipt(
            merchantName = "Store B",
            date = 1_600_000_100_000L,
            totalPrice = BigDecimal("56.78"),
            imageUrl = "urlB"
        )
        val id1 = dao.insertReceipt(r1)
        val id2 = dao.insertReceipt(r2)

        val all = dao.getAllReceipts()
        // should preserve insertion order
        assertEquals(2, all.size)
        assertEquals(id1.toInt(), all[0].id)
        assertEquals("Store A", all[0].merchantName)
        assertEquals(id2.toInt(), all[1].id)
        assertEquals("Store B", all[1].merchantName)
    }

    @Test fun getReceiptById_and_updateReceipt(): Unit = runBlocking {
        val r = Receipt(
            merchantName = "Cafe",
            date = 123L,
            totalPrice = BigDecimal("3.21"),
            imageUrl = "u"
        )
        val id = dao.insertReceipt(r).toInt()

        val loaded = dao.getReceiptById(id)
        assertNotNull(loaded)
        assertEquals("Cafe", loaded!!.merchantName)

        val updated = loaded.copy(merchantName = "NewCafe", totalPrice = BigDecimal("4.56"))
        dao.updateReceipt(updated)

        val reloaded = dao.getReceiptById(id)
        assertEquals("NewCafe", reloaded!!.merchantName)
        assertEquals(BigDecimal("4.56"), reloaded.totalPrice)
    }

    @Test fun insertReceiptItems_and_getItemsForReceipt(): Unit = runBlocking {
        // first we need a receipt
        val id = dao.insertReceipt(
            Receipt(
                merchantName = "X",
                date = 0L,
                totalPrice = BigDecimal.ZERO,
                imageUrl = ""
            )
        ).toInt()

        val items = listOf(
            ReceiptItem(receiptId = id, itemName = "Item1", quantity = 2, price = BigDecimal("1.00")),
            ReceiptItem(receiptId = id, itemName = "Item2", quantity = 3, price = BigDecimal("2.00"))
        )
        dao.insertReceiptItems(items)

        val fetched = dao.getItemsForReceipt(id)
        assertEquals(2, fetched.size)
        assertTrue(fetched.any { it.itemName == "Item1" && it.quantity == 2 })
        assertTrue(fetched.any { it.itemName == "Item2" && it.price == BigDecimal("2.00") })
    }

    @Test fun insertReceiptWithItems_getReceiptsWithItems_and_getReceiptWithItems(): Unit = runBlocking {
        val receipt = Receipt(
            merchantName = "Combo",
            date = 999L,
            totalPrice = BigDecimal("9.99"),
            imageUrl = "comboUrl"
        )
        val items = listOf(
            ReceiptItem(receiptId = 0, itemName = "A", quantity = 1, price = BigDecimal("5.00")),
            ReceiptItem(receiptId = 0, itemName = "B", quantity = 1, price = BigDecimal("4.99"))
        )

        dao.insertReceiptWithItems(receipt, items)
        // now there should be exactly one receipt with its two items
        val list = dao.getReceiptsWithItems()
        assertEquals(1, list.size)
        val rwi = list[0]
        assertEquals("Combo", rwi.receipt.merchantName)
        assertEquals(2, rwi.items.size)

        // and getReceiptWithItems by id
        val fetched = dao.getReceiptWithItems(rwi.receipt.id)
        assertNotNull(fetched)
        assertEquals(rwi.receipt.id, fetched!!.receipt.id)
        assertEquals(2, fetched.items.size)
    }

    @Test fun updateReceiptItem_modifiesItem(): Unit = runBlocking {
        val id = dao.insertReceipt(
            Receipt(merchantName = "T", date = 0L, totalPrice = BigDecimal.ZERO, imageUrl = "")
        ).toInt()
        val item = ReceiptItem(receiptId = id, itemName = "Orig", quantity = 1, price = BigDecimal("1.00"))
        dao.insertReceiptItems(listOf(item))

        val fetched0 = dao.getItemsForReceipt(id)[0]
        val upd = fetched0.copy(itemName = "Changed", price = BigDecimal("2.00"))
        dao.updateReceiptItem(upd)

        val fetched1 = dao.getItemsForReceipt(id)[0]
        assertEquals("Changed", fetched1.itemName)
        assertEquals(BigDecimal("2.00"), fetched1.price)
    }

    @Test fun deleteReceiptById_removesReceipt(): Unit = runBlocking {
        val id1 = dao.insertReceipt(
            Receipt(merchantName = "D1", date = 0L, totalPrice = BigDecimal.ZERO, imageUrl = "")
        ).toInt()
        val id2 = dao.insertReceipt(
            Receipt(merchantName = "D2", date = 0L, totalPrice = BigDecimal.ZERO, imageUrl = "")
        ).toInt()

        // confirm both
        assertEquals(2, dao.getAllReceipts().size)

        dao.deleteReceiptById(id1)
        val after = dao.getAllReceipts()
        assertEquals(1, after.size)
        assertEquals(id2, after[0].id)
        // also getReceiptById returns null
        assertNull(dao.getReceiptById(id1))
    }
}
