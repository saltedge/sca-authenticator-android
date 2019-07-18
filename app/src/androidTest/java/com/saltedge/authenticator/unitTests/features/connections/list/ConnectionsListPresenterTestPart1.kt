/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2019 Salt Edge Inc.
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
package com.saltedge.authenticator.unitTests.features.connections.list

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.DELETE_ALL_REQUEST_CODE
import com.saltedge.authenticator.app.ITEM_OPTIONS_REQUEST_CODE
import com.saltedge.authenticator.features.connections.common.ConnectionOptions
import com.saltedge.authenticator.features.connections.common.ConnectionViewModel
import com.saltedge.authenticator.features.connections.list.ConnectionsListContract
import com.saltedge.authenticator.features.connections.list.ConnectionsListPresenter
import com.saltedge.authenticator.model.db.Connection
import com.saltedge.authenticator.model.db.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.model.ConnectionStatus
import com.saltedge.authenticator.sdk.model.createInvalidResponseError
import com.saltedge.authenticator.sdk.tools.KeyStoreManagerAbs
import com.saltedge.authenticator.testTools.*
import com.saltedge.authenticator.tool.toDateTime
import com.saltedge.authenticator.tool.toLongDateString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
class ConnectionsListPresenterTestPart1 {

    @Test
    @Throws(Exception::class)
    fun viewContractTest() {
        assertThat(createPresenter(viewContract = mockView).viewContract, equalTo(mockView))
        Assert.assertNull(createPresenter(viewContract = null).viewContract)
    }

    /**
     * test getListItems when db is empty
     */
    @Test
    @Throws(Exception::class)
    fun getListItemsTestCase1() {
        Mockito.doReturn(emptyList<Connection>()).`when`(mockConnectionsRepository).getAllConnections()

        assertThat(createPresenter(viewContract = mockView).getListItems(), equalTo(emptyList()))
    }

    @Test
    @Throws(Exception::class)
    fun getListItemsTestCase2() {
        val presenterContract = createPresenter(viewContract = mockView)

        assertThat(presenterContract.getListItems(), equalTo(listOf(
                ConnectionViewModel(
                        guid = "guid1", code = "demobank", name = "Demobank1",
                        statusDescription = TestTools.getString(R.string.connection_status_inactive),
                        statusColorResId = R.color.red,
                        logoUrl = ""),
                ConnectionViewModel(
                        guid = "guid3", code = "demobank3", name = "Demobank3",
                        statusDescription = "${TestTools.getString(R.string.connection_status_connected_on)} ${300L.toDateTime().toLongDateString(TestTools.applicationContext)}",
                        statusColorResId = R.color.gray_dark,
                        logoUrl = ""),
                ConnectionViewModel(
                        guid = "guid2", code = "demobank", name = "Demobank2",
                        statusDescription = "${TestTools.getString(R.string.connection_status_connected_on)} ${200L.toDateTime().toLongDateString(TestTools.applicationContext)}",
                        statusColorResId = R.color.gray_dark,
                        logoUrl = "")
                )
        ))
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTest() {
        createPresenter(viewContract = null).onListItemClick("guid3")

        Mockito.never()

        val presenter = createPresenter(viewContract = mockView)
        presenter.onListItemClick(connectionGuid = "guidX")

        Mockito.verify(mockView).showOptionsView(
                connectionGuid = "guidX",
                options = emptyArray(),
                requestCode = ITEM_OPTIONS_REQUEST_CODE)

        presenter.onListItemClick("guid2")

        Mockito.verify(mockView).showOptionsView(
                connectionGuid = "guid2",
                options = arrayOf(ConnectionOptions.RENAME, ConnectionOptions.REPORT_PROBLEM, ConnectionOptions.DELETE),
                requestCode = ITEM_OPTIONS_REQUEST_CODE)

        Mockito.reset(mockView)
        presenter.onListItemClick("guid1")

        Mockito.verify(mockView).showOptionsView(
                connectionGuid = "guid1",
                options = arrayOf(ConnectionOptions.RECONNECT, ConnectionOptions.RENAME, ConnectionOptions.REPORT_PROBLEM, ConnectionOptions.DELETE),
                requestCode = ITEM_OPTIONS_REQUEST_CODE)
    }

    @Test
    @Throws(Exception::class)
    fun onMenuItemClickTest() {
        Assert.assertFalse(createPresenter(viewContract = null).onMenuItemClick(menuItemId = -1))

        Mockito.never()

        val presenter = createPresenter(viewContract = mockView)

        Assert.assertFalse(presenter.onMenuItemClick(menuItemId = -1))

        Mockito.never()

        Assert.assertTrue(presenter.onMenuItemClick(menuItemId = R.id.menu_delete_all))

        Mockito.verify(mockView).showDeleteConnectionView(connectionGuid = null, requestCode = DELETE_ALL_REQUEST_CODE)
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTestCase1() {
        createPresenter(viewContract = null).onViewClick(viewId = -1)

        Mockito.never()

        createPresenter(viewContract = null).onViewClick(viewId = R.id.connectionsFabView)

        Mockito.never()

        createPresenter(viewContract = mockView).onViewClick(viewId = R.id.connectionsFabView)

        Mockito.verify(mockView).showQrScanView()
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTestCase2() {
        createPresenter(viewContract = null).onViewClick(viewId = R.id.mainActionView)

        Mockito.never()

        createPresenter(viewContract = mockView).onViewClick(viewId = R.id.mainActionView)

        Mockito.verify(mockView).showQrScanView()
    }

    @Test
    @Throws(Exception::class)
    fun onConnectionsRevokeResultTest() {
        createPresenter(viewContract = mockView).onConnectionsRevokeResult(
                revokedTokens = emptyList(), apiError = createInvalidResponseError())

        Mockito.never()
    }

    @Before
    fun setUp() {
        Mockito.doReturn(connections).`when`(mockConnectionsRepository).getAllConnections()
        Mockito.doReturn(connections[0]).`when`(mockConnectionsRepository).getByGuid("guid1")
        Mockito.doReturn(connections[1]).`when`(mockConnectionsRepository).getByGuid("guid2")
        Mockito.doReturn(connections[2]).`when`(mockConnectionsRepository).getByGuid("guid3")
    }

    private val mockKeyStoreManager = Mockito.mock(KeyStoreManagerAbs::class.java)
    private val mockConnectionsRepository = Mockito.mock(ConnectionsRepositoryAbs::class.java)
    private val mockApiManager = Mockito.mock(AuthenticatorApiManagerAbs::class.java)
    private val mockView = Mockito.mock(ConnectionsListContract.View::class.java)
    private val connections = listOf(
        Connection().setGuid("guid1").setCode("demobank").setName("Demobank1")
                .setStatus(ConnectionStatus.INACTIVE).setAccessToken("token1")
                .setCreatedAt(100L).setUpdatedAt(100L),
        Connection().setGuid("guid2").setCode("demobank").setName("Demobank2")
                .setStatus(ConnectionStatus.ACTIVE).setAccessToken("token1")
                .setCreatedAt(300L).setUpdatedAt(300L),
        Connection().setGuid("guid3").setCode("demobank3").setName("Demobank3")
                .setStatus(ConnectionStatus.ACTIVE).setAccessToken("")
                .setCreatedAt(200L).setUpdatedAt(200L)
    )

    private fun createPresenter(viewContract: ConnectionsListContract.View? = null): ConnectionsListPresenter {
        return ConnectionsListPresenter(
                appContext = TestTools.applicationContext,
                keyStoreManager = mockKeyStoreManager,
                connectionsRepository = mockConnectionsRepository,
                apiManager = mockApiManager).apply { this.viewContract = viewContract }
    }
}
