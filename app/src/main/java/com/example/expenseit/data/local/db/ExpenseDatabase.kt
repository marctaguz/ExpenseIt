package com.example.expenseit.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.expenseit.data.local.db.ExpenseDatabase.Companion.getDatabase
import com.example.expenseit.data.local.entities.Category
import com.example.expenseit.data.local.entities.Expense
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Expense::class, Category::class], version = 5)
abstract class ExpenseDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: ExpenseDatabase? = null

        fun getDatabase(context: Context): ExpenseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExpenseDatabase::class.java,
                    "expense_database"
                )
                    .fallbackToDestructiveMigration() //temporary
                    .addMigrations(MIGRATION_3_5)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun initializeDefaultCategories(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                val database = getDatabase(context)
                val categoryDao = database.categoryDao()

                if (categoryDao.getAllCategories().isEmpty()) {
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
                    categoryDao.insertAll(defaultCategories)
                }
            }
        }
    }

}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create the 'categories' table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `categories` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL
            )
        """)
    }
}

val MIGRATION_3_5 = object : Migration(3, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE categories ADD COLUMN `order` INTEGER NOT NULL DEFAULT 0")
    }
}


