package com.example.expenseit.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.expenseit.data.local.entities.Category
import com.example.expenseit.data.local.entities.Expense
import com.example.expenseit.data.local.entities.Receipt
import com.example.expenseit.data.local.entities.ReceiptItem
import com.example.expenseit.utils.BigDecimalConverter

@Database(
    entities = [Expense::class, Category::class, Receipt::class, ReceiptItem::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(BigDecimalConverter::class)
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

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Step 1: Add new column 'date' with a default value (e.g., Unix timestamp 0)
        db.execSQL("ALTER TABLE receipts ADD COLUMN date INTEGER NOT NULL DEFAULT 0")

        // Step 2: Convert 'transactionDate' (TEXT) to 'date' (INTEGER)
        db.execSQL("""
            UPDATE receipts 
            SET date = CASE 
                WHEN transactionDate IS NOT NULL AND transactionDate != '' 
                THEN strftime('%s', transactionDate) * 1000 
                ELSE 0 
            END
        """)

        // Step 3: Remove old column (SQLite doesn't support DROP COLUMN directly)
        db.execSQL("""
            CREATE TABLE receipts_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                merchantName TEXT NOT NULL,
                date INTEGER NOT NULL,
                totalPrice REAL NOT NULL,
                imageUrl TEXT NOT NULL
            )
        """)

        // Step 4: Copy existing data to the new table
        db.execSQL("""
            INSERT INTO receipts_new (id, merchantName, date, totalPrice, imageUrl)
            SELECT id, merchantName, date, totalPrice, imageUrl FROM receipts
        """)

        // Step 5: Drop old table
        db.execSQL("DROP TABLE receipts")

        // Step 6: Rename new table to original name
        db.execSQL("ALTER TABLE receipts_new RENAME TO receipts")
    }
}


val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // ✅ Add new column "receiptId" (nullable by default)
        db.execSQL("ALTER TABLE expenses ADD COLUMN receiptId INTEGER NULL")
    }
}

val MIGRATION_2_3 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE receipt_item ADD COLUMN price TEXT DEFAULT '0.00'")
    }
}
