/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2020 Salt Edge Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 or later.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * For the additional permissions granted for Salt Edge Authenticator
 * under Section 7 of the GNU General Public License see THIRD_PARTY_NOTICES.md
 */
package com.saltedge.authenticator.features.connections.select

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.saltedge.authenticator.features.connections.list.convertConnectionsToViewModels
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.sdk.model.connection.ConnectionStatus
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SelectConnectionsViewModelTest {

    private lateinit var viewModel: SelectConnectionsViewModel
    private val context: Context = ApplicationProvider.getApplicationContext()
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
        val connection = connections.convertConnectionsToViewModels(context)
        viewModel.listItems.postValue(connection)

        assertNull(viewModel.onListItemClickEvent.value)

        //when
        viewModel.onListItemClick(1)

        //than
        assertThat(viewModel.onListItemClickEvent.value, equalTo(ViewModelEvent(content = 1)))
    }

    @Test
    @Throws(Exception::class)
    fun proceedConnectionTest() {
        //given
        val connection = connections.convertConnectionsToViewModels(context)
        viewModel.listItems.postValue(connection)

        assertNull(viewModel.onProceedClickEvent.value)

        //when
        viewModel.proceedConnection("guid2")

        //than
        assertThat(viewModel.onProceedClickEvent.value, equalTo("guid2"))
    }

    @Test
    @Throws(Exception::class)
    fun changeStateItemTest() {
        //given
        val connection = connections.convertConnectionsToViewModels(context)
        viewModel.listItems.postValue(connection)
        viewModel.listItems.value = connection

        assertFalse(connection[0].isChecked)
        assertFalse(connection[1].isChecked)

        //when
        viewModel.changeStateItem(connection[1])

        //than
        assertFalse(connection[0].isChecked)
        assertTrue(connection[1].isChecked)
    }

    @Test
    @Throws(Exception::class)
    fun setInitialDataTest() {
        val connection = connections.convertConnectionsToViewModels(context)

        viewModel.setInitialData(connection)

        assertNotNull(viewModel.listItems)
    }
}
