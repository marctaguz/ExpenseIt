package com.example.expenseit.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.OnConflictStrategy
import androidx.room.Update
import com.example.expenseit.data.local.entities.CategoryTotal
import com.example.expenseit.data.local.entities.Expense
import com.example.expenseit.data.local.entities.ExpenseSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense): Long

    @Update
    suspend fun update(expense: Expense)

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteExpenseById(expenseId: Long)

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("""
    SELECT strftime('%Y-%m', datetime(date / 1000, 'unixepoch')) AS month, SUM(amount) AS total
    FROM expenses
    GROUP BY month
    ORDER BY month
""")
    fun getAllExpensesByMonth(): Flow<List<ExpenseSummary>>

    @Query("SELECT * FROM expenses WHERE id = :expenseId LIMIT 1")
    suspend fun getExpenseById(expenseId: Long): Expense?

    @Query("""
    SELECT 
        strftime('%Y-%m', date / 1000, 'unixepoch') AS month,
        SUM(amount) AS total
    FROM expenses
    GROUP BY month
    ORDER BY month DESC
""")
    fun getMonthlyExpenses(): Flow<List<ExpenseSummary>>

    @Query("""
    SELECT categories.name AS categoryName, SUM(expenses.amount) AS total
    FROM expenses
    INNER JOIN categories ON expenses.categoryId = categories.id
    GROUP BY categoryId
""")
    fun getCategoryTotals(): Flow<List<CategoryTotal>>

    @Query("""
        SELECT categories.name AS categoryName, SUM(expenses.amount) AS total
        FROM expenses
        INNER JOIN categories ON expenses.categoryId = categories.id
        WHERE strftime('%Y-%m', datetime(date / 1000, 'unixepoch')) = :month
        GROUP BY categoryId
        ORDER BY total DESC
    """)
    fun getCategoryTotalsForMonth(month: String): Flow<List<CategoryTotal>>
}