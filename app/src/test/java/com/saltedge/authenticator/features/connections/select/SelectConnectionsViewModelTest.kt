/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.connections.select

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.saltedge.android.test_tools.ViewModelTest
import com.saltedge.authenticator.core.model.ConnectionStatus
import com.saltedge.authenticator.features.connections.common.convertConnectionsToViewItems
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.location.DeviceLocationManagerAbs
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SelectConnectionsViewModelTest : ViewModelTest() {

    private lateinit var viewModel: SelectConnectionsViewModel
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val mockLocationManager = Mockito.mock(DeviceLocationManagerAbs::class.java)
    private val connections = listOf(
        Connection().apply {
            guid = "guid1"
            code = "demobank1"
            name = "Demobank1"
            status = "${ConnectionStatus.INACTIVE}"
            accessToken = "token1"
            supportEmail = "example@example.com"
            createdAt = 100L
            updatedAt = 100L
        },
        Connection().apply {
            guid = "guid2"
            code = "demobank2"
            name = "Demobank2"
            status = "${ConnectionStatus.ACTIVE}"
            accessToken = "token2"
            createdAt = 300L
            updatedAt = 300L
        }
    )

    @Before
    fun setUp() {
        viewModel = SelectConnectionsViewModel()
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTest() {
        //given
        val connection = connections.convertConnectionsToViewItems(context, mockLocationManager)
        viewModel.listItems.postValue(connection)

        assertNull(viewModel.onListItemClickEvent.value)

        //when
        viewModel.onListItemClick(1)

        //then
        assertThat(viewModel.onListItemClickEvent.value, equalTo(ViewModelEvent(content = 1)))
    }

    @Test
    @Throws(Exception::class)
    fun proceedConnectionTest() {
        //given
        val connection = connections.convertConnectionsToViewItems(context, mockLocationManager)
        viewModel.listItems.postValue(connection)

        assertNull(viewModel.onProceedClickEvent.value)

        //when
        viewModel.proceedConnection("guid2")

        //then
        assertThat(viewModel.onProceedClickEvent.value, equalTo("guid2"))
    }

    @Test
    @Throws(Exception::class)
    fun changeStateItemTest() {
        //given
        val connection = connections.convertConnectionsToViewItems(context, mockLocationManager)
        viewModel.listItems.postValue(connection)
        viewModel.listItems.value = connection

        assertFalse(connection[0].isChecked)
        assertFalse(connection[1].isChecked)

        //when
        viewModel.changeStateItem(connection[1])

        //then
        assertFalse(connection[0].isChecked)
        assertTrue(connection[1].isChecked)
    }

    @Test
    @Throws(Exception::class)
    fun setInitialDataTest() {
        val connection = connections.convertConnectionsToViewItems(context, mockLocationManager)

        viewModel.setInitialData(connection)

        assertNotNull(viewModel.listItems)
    }
}
