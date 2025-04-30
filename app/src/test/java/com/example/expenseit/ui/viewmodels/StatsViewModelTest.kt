package com.example.expenseit.ui.viewmodels

import app.cash.turbine.test
import com.example.expenseit.data.local.entities.CategoryTotal
import com.example.expenseit.data.local.entities.ExpenseSummary
import com.example.expenseit.ui.viewmodels.dao.FakeExpenseDao
import com.github.mikephil.charting.data.Entry
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class StatsViewModelTest {

    private lateinit var fakeDao: FakeExpenseDao
    private lateinit var vm: StatsViewModel

    @Before
    fun setup() {
        fakeDao = FakeExpenseDao()
        vm = StatsViewModel(fakeDao)
    }

    @Test
    fun `getEntriesForChart maps summaries to entries with correct x and y`() {
        val summaries = listOf(
            ExpenseSummary(month = "2025-01", total = BigDecimal("10.5")),
            ExpenseSummary(month = "2025-02", total = BigDecimal("20.0")),
        )
        val entries: List<Entry> = vm.getEntriesForChart(summaries)

        assertEquals(2, entries.size)
        assertEquals(0f, entries[0].x, 0.0f)
        assertEquals(10.5f, entries[0].y, 0.0f)
        assertEquals(1f, entries[1].x, 0.0f)
        assertEquals(20.0f, entries[1].y, 0.0f)
    }

    @Test
    fun `getMonthLabels formats each month string`() {
        val summaries = listOf(
            ExpenseSummary(month = "2025-01", total = BigDecimal("0")),
            ExpenseSummary(month = "2025-12", total = BigDecimal("0"))
        )
        val labels = vm.getMonthLabels(summaries)

        // Your DateUtils.formatMonthForChart("2025-01") should match this:
        assertEquals("Jan", labels[0])
        assertEquals("Dec", labels[1])
    }

    @Test
    fun `getMonthlyComparison with fewer than two items returns zeros`() {
        val onlyOne = listOf(ExpenseSummary("2025-01", BigDecimal("5")))
        val (cur, last) = vm.getMonthlyComparison(onlyOne)
        assertEquals(0f, cur,  0.0f)
        assertEquals(0f, last, 0.0f)
    }

    @Test
    fun `getMonthlyComparison computes correct pair`() {
        val data = listOf(
            ExpenseSummary("2025-03", BigDecimal("30")),
            ExpenseSummary("2025-02", BigDecimal("10")),
            ExpenseSummary("2025-01", BigDecimal("5")),
        )
        val (current, previous) = vm.getMonthlyComparison(data)
        assertEquals(30f, current,  0.0f)
        assertEquals(10f, previous, 0.0f)
    }

    @Test
    fun `getCategoryTotalsForMonth emits downstream`() = runTest {
        // Arrange
        val stub = listOf(
            CategoryTotal(categoryName = "Food", total = BigDecimal("12.34")),
            CategoryTotal(categoryName = "Travel", total = BigDecimal("5.00")),
        )
        // Act: start collecting
        vm.getCategoryTotalsForMonth("2025-03").test {
            // first emission is emptyList()
            assertEquals(emptyList<CategoryTotal>(), awaitItem())

            // now push our stub into the fake DAO
            fakeDao.emitCategoryTotals(stub)

            // Assert: viewModel flow re-emits
            val next = awaitItem()
            assertEquals(stub, next)

            cancelAndIgnoreRemainingEvents()
        }

        @Test
        fun `getEntries emits data`() {
            // Verify that the getEntries flow emits a non-empty list of ExpenseSummary.
            // TODO implement test
        }

        @Test
        fun `getEntries empty emission`() {
            // Verify that getEntries flow emits an empty list when there are no expenses.
            // TODO implement test
        }

        @Test
        fun `getEntries multiple emissions`() {
            // Verify that getEntries flow emits updated lists of ExpenseSummary
            // when underlying data changes.
            // TODO implement test
        }

        @Test
        fun `getMonthlyExpenses emits data`() {
            // Verify that the monthlyExpenses flow emits a non-empty list of ExpenseSummary.
            // TODO implement test
        }

        @Test
        fun `getMonthlyExpenses empty emission`() {
            // Verify that monthlyExpenses flow emits an empty list when there are no monthly
            // expenses.
            // TODO implement test
        }

        @Test
        fun `getMonthlyExpenses multiple emissions`() {
            // Verify that monthlyExpenses flow emits updated lists of ExpenseSummary
            // when underlying data changes.
            // TODO implement test
        }

        @Test
        fun `getEntriesForChart correct mapping`() {
            // Verify that getEntriesForChart correctly maps each ExpenseSummary to an Entry
            // with correct index and total.
            // TODO implement test
        }

        @Test
        fun `getEntriesForChart empty list`() {
            // Verify that getEntriesForChart returns an empty list when given an empty list
            // of ExpenseSummary.
            // TODO implement test
        }

        @Test
        fun `getEntriesForChart large number`() {
            // Test if the function can handle large values for expense summary.
            // (max float)
            // TODO implement test
        }

        @Test
        fun `getEntriesForChart zero amounts`() {
            // Test if the function can handle expense summary with zero amounts.
            // TODO implement test
        }

        @Test
        fun `getMonthLabels correct format`() {
            // Verify that getMonthLabels returns a list of formatted month strings as expected
            // by DateUtils.formatMonthForChart.
            // TODO implement test
        }

        @Test
        fun `getMonthLabels empty list`() {
            // Verify that getMonthLabels returns an empty list when given an empty list
            // of ExpenseSummary.
            // TODO implement test
        }

        @Test
        fun `getMonthLabels null month`() {
            // Test getMonthLabels with ExpenseSummary entries containing a null month.
            // TODO implement test
        }

        @Test
        fun `getMonthLabels invalid month`() {
            // Test getMonthLabels with ExpenseSummary entries containing an invalid month
            // string.
            // TODO implement test
        }

        @Test
        fun `getMonthlyComparison sufficient data`() {
            // Verify that getMonthlyComparison returns a Pair of floats for current and last
            // month when given at least two ExpenseSummary.
            // TODO implement test
        }

        @Test
        fun `getMonthlyComparison not enough data`() {
            // Verify that getMonthlyComparison returns a Pair of 0f, 0f when given a list
            // with fewer than two ExpenseSummary.
            // TODO implement test
        }

        @Test
        fun `getMonthlyComparison empty list`() {
            // Verify that getMonthlyComparison returns Pair(0f, 0f) when given an empty list.
            // TODO implement test
        }

        @Test
        fun `getMonthlyComparison large amounts`() {
            // Verify that getMonthlyComparison handles large expense amounts properly.
            // TODO implement test
        }

        @Test
        fun `getMonthlyComparison zero amounts`() {
            // Verify getMonthlyComparison works correctly when expense amounts are zero.
            // TODO implement test
        }

        @Test
        fun `getCategoryTotalsForMonth emits data`() {
            // Verify that getCategoryTotalsForMonth emits a non-empty list of CategoryTotal
            // when there are expenses for the specified month.
            // TODO implement test
        }

        @Test
        fun `getCategoryTotalsForMonth empty emission`() {
            // Verify that getCategoryTotalsForMonth emits an empty list when there are no
            // expenses for the specified month.
            // TODO implement test
        }

        @Test
        fun `getCategoryTotalsForMonth multiple emissions`() {
            // Verify that getCategoryTotalsForMonth emits updated lists of CategoryTotal
            // when underlying data changes for the specified month.
            // TODO implement test
        }

        @Test
        fun `getCategoryTotalsForMonth invalid month`() {
            // Verify that getCategoryTotalsForMonth handles invalid month string properly
            // like empty or a non-existent month.
            // TODO implement test
        }

        @Test
        fun `getCategoryTotalsForMonth valid month`() {
            // Verify that getCategoryTotalsForMonth handles valid month string
            // TODO implement test
        }
    }
}