/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.android.test_tools

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.mockito.BDDMockito

abstract class ViewModelTest {
    /**
     * A JUnit Test Rule that swaps the background executor used by the Architecture Components with a different one which executes each task synchronously.
     */
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
}

@ExperimentalCoroutinesApi
abstract class CoroutineViewModelTest : ViewModelTest() {
    protected val testDispatcher = TestCoroutineDispatcher()

    @Before
    open fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    open fun tearDown() {
        // Resets state of the [Dispatchers.Main] to the original main dispatcher.
        // For example, in Android Main thread dispatcher will be set as [Dispatchers.Main].
        Dispatchers.resetMain()

        // Clean up the TestCoroutineDispatcher to make sure no other work is running.
        testDispatcher.cleanupTestCoroutines()
    }
}
