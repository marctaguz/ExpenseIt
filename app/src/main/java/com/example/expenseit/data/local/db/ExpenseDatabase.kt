package com.example.expenseit.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.expenseit.data.local.entities.Category
import com.example.expenseit.data.local.entities.Expense
import com.example.expenseit.data.local.entities.Receipt
import com.example.expenseit.data.local.entities.ReceiptItem

@Database(
    entities = [Expense::class, Category::class, Receipt::class, ReceiptItem::class],
    version = 7,
    exportSchema = false
)
abstract class ExpenseDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun receiptDao(): ReceiptDao
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `receipts` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `merchantName` TEXT NOT NULL,
                `transactionDate` TEXT NOT NULL,
                `totalPrice` REAL NOT NULL
            )
            """
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `receipt_items` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `receiptId` INTEGER NOT NULL,
                `itemName` TEXT NOT NULL,
                `quantity` INTEGER NOT NULL,
                `price` REAL NOT NULL,
                FOREIGN KEY(`receiptId`) REFERENCES `receipts`(`id`) ON DELETE CASCADE
            )
            """
        )
    }
}

val MIGRATION_3_5 = object : Migration(3, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE categories ADD COLUMN `order` INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // ✅ Create a new table with the correct schema
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `receipts_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `merchantName` TEXT NOT NULL,
                `transactionDate` TEXT NOT NULL,
                `totalPrice` REAL NOT NULL,
                `imageUrl` TEXT NOT NULL DEFAULT ''
            )
            """
        )

        // ✅ Copy existing data to the new table
        db.execSQL(
            """
            INSERT INTO `receipts_new` (id, merchantName, transactionDate, totalPrice, imageUrl)
            SELECT id, merchantName, transactionDate, totalPrice, COALESCE(imageUrl, '') FROM receipts
            """
        )

        // ✅ Remove old table and rename the new table
        db.execSQL("DROP TABLE receipts")
        db.execSQL("ALTER TABLE receipts_new RENAME TO receipts")
    }
}

