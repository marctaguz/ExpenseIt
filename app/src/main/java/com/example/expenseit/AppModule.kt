package com.example.expenseit

import android.content.Context
import androidx.room.Room
import com.example.expenseit.data.local.db.CategoryDao
import com.example.expenseit.data.local.db.ExpenseDao
import com.example.expenseit.data.local.db.ExpenseDatabase
import com.example.expenseit.data.local.db.MIGRATION_1_2
import com.example.expenseit.data.local.db.MIGRATION_2_3
import com.example.expenseit.data.local.db.MIGRATION_3_4
import com.example.expenseit.data.local.db.MIGRATION_3_5
import com.example.expenseit.data.local.db.MIGRATION_5_6
import com.example.expenseit.data.local.db.MIGRATION_6_7
import com.example.expenseit.data.local.db.MIGRATION_7_8
import com.example.expenseit.data.local.db.ReceiptDao
import com.example.expenseit.data.repository.CategoryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Singleton scope
object AppModule {

    // ✅ Provide Application Context
    @Provides
    fun provideContext(application: android.app.Application): Context {
        return application.applicationContext
    }

    // ✅ Provide Database Instance
    @Provides
    @Singleton
    fun provideDatabase(context: Context): ExpenseDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            ExpenseDatabase::class.java,
            "expense_database"
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .fallbackToDestructiveMigration() // ⚠️ Resets database if schema changes
            .build()
    }

    @Provides
    fun provideExpenseDao(database: ExpenseDatabase): ExpenseDao {
        return database.expenseDao()
    }

    @Provides
    fun provideCategoryDao(database: ExpenseDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    fun provideReceiptDao(database: ExpenseDatabase): ReceiptDao {
        return database.receiptDao()
    }

    @Provides
    fun provideCategoryRepository(categoryDao: CategoryDao): CategoryRepository {
        return CategoryRepository(categoryDao)
    }
}