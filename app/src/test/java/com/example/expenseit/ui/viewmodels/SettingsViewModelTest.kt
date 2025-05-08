package com.example.expenseit.ui.viewmodels

import com.example.expenseit.ui.viewmodels.dao.FakeCurrencyDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeDataSource: FakeCurrencyDataSource
    private lateinit var vm: SettingsViewModel

    @Before fun setup() {
        fakeDataSource = FakeCurrencyDataSource()
        vm = SettingsViewModel(fakeDataSource)
    }

    @Test fun `initial currency is what dataSource provides`() = runTest {
        // collect kicked off in init
        advanceUntilIdle()
        assertEquals("$", vm.currency.value)
    }

    @Test fun `setCurrency updates dataSource and vm state`() = runTest {
        vm.setCurrency("€")
        advanceUntilIdle()

        // Data source saw it:
        assertEquals("€", fakeDataSource.currency.first())
        // ViewModel sees it:
        assertEquals("€", vm.currency.value)
    }

    @Test fun `external dataSource emission updates vm`() = runTest {
        advanceUntilIdle()
        fakeDataSource.emit("£")
        advanceUntilIdle()
        assertEquals("£", vm.currency.value)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description?) {
        Dispatchers.setMain(dispatcher)
    }
    override fun finished(description: Description?) {
        Dispatchers.resetMain()
    }
}