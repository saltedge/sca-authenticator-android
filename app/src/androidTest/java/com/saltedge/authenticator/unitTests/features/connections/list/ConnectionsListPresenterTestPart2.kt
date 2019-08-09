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

import android.app.Activity
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.saltedge.authenticator.app.*
import com.saltedge.authenticator.features.connections.common.ConnectionOptions
import com.saltedge.authenticator.features.connections.list.ConnectionsListContract
import com.saltedge.authenticator.features.connections.list.ConnectionsListPresenter
import com.saltedge.authenticator.model.db.Connection
import com.saltedge.authenticator.model.db.ConnectionsRepositoryAbs
import com.saltedge.authenticator.model.db.isActive
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.constants.KEY_NAME
import com.saltedge.authenticator.sdk.model.ConnectionAndKey
import com.saltedge.authenticator.sdk.model.ConnectionStatus
import com.saltedge.authenticator.sdk.tools.KeyStoreManagerAbs
import com.saltedge.authenticator.testTools.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import java.security.KeyPair
import java.security.PrivateKey

/**
 * Testing onActivityResult
 */
@RunWith(AndroidJUnit4::class)
class ConnectionsListPresenterTestPart2 {

    /**
     * test onActivityResult,
     * when resultCode != Activity.RESULT_OK or data == null or unknown requestCode
     */
    @Test
    @Throws(Exception::class)
    fun onActivityResultTest() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.onActivityResult(
                requestCode = ITEM_OPTIONS_REQUEST_CODE,
                resultCode = Activity.RESULT_CANCELED,
                data = Intent())

        Mockito.never()

        presenter.onActivityResult(
                requestCode = ITEM_OPTIONS_REQUEST_CODE,
                resultCode = Activity.RESULT_OK,
                data = null)

        Mockito.never()

        presenter.onActivityResult(
                requestCode = -1,
                resultCode = Activity.RESULT_OK,
                data = Intent())

        Mockito.never()
    }

    /**
     * test onActivityResult,
     * requestCode = ITEM_OPTIONS_REQUEST_CODE, resultCode = Activity.RESULT_OK,
     * OPTION_ID = UNKNOWN or GUID = null or No connection associated with GUID or viewContract == null.
     */
    @Test
    @Throws(Exception::class)
    fun onActivityResultTest_Options_InvalidParams() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.onActivityResult(resultCode = Activity.RESULT_OK,
                requestCode = ITEM_OPTIONS_REQUEST_CODE,
                data = Intent()
                        .putExtra(KEY_OPTION_ID, -1)
                        .putExtra(KEY_GUID, "guid1"))

        Mockito.never()

        presenter.onActivityResult(resultCode = Activity.RESULT_OK,
                requestCode = ITEM_OPTIONS_REQUEST_CODE,
                data = Intent().putExtra(KEY_OPTION_ID, ConnectionOptions.REPORT_PROBLEM.ordinal))

        Mockito.never()

        presenter.onActivityResult(resultCode = Activity.RESULT_OK,
                requestCode = ITEM_OPTIONS_REQUEST_CODE,
                data = Intent()
                        .putExtra(KEY_OPTION_ID, ConnectionOptions.RENAME.ordinal)
                        .putExtra(KEY_GUID, "guidX"))

        Mockito.never()

        createPresenter(viewContract = null).onActivityResult(resultCode = Activity.RESULT_OK,
                requestCode = ITEM_OPTIONS_REQUEST_CODE,
                data = Intent().putExtra(KEY_OPTION_ID, ConnectionOptions.REPORT_PROBLEM.ordinal))

        Mockito.never()
    }

    /**
     * test onActivityResult,
     * requestCode = ITEM_OPTIONS_REQUEST_CODE, resultCode = Activity.RESULT_OK,
     * OPTION_ID = ConnectionOptions.REPORT_PROBLEM, GUID != null.
     *
     * User clicked Option REPORT_PROBLEM
     */
    @Test
    @Throws(Exception::class)
    fun onActivityResultTest_OptionsReport() {
        createPresenter(viewContract = null).onActivityResult(resultCode = Activity.RESULT_OK,
                requestCode = ITEM_OPTIONS_REQUEST_CODE,
                data = Intent()
                        .putExtra(KEY_OPTION_ID, ConnectionOptions.REPORT_PROBLEM.ordinal)
                        .putExtra(KEY_GUID, "guid1"))

        Mockito.never()

        createPresenter(viewContract = mockView).onActivityResult(resultCode = Activity.RESULT_OK,
                requestCode = ITEM_OPTIONS_REQUEST_CODE,
                data = Intent()
                        .putExtra(KEY_OPTION_ID, ConnectionOptions.REPORT_PROBLEM.ordinal)
                        .putExtra(KEY_GUID, "guid1"))

        Mockito.verify(mockView).showSupportView(supportEmail = "example@example.com")
    }

    /**
     * test onActivityResult,
     * requestCode = ITEM_OPTIONS_REQUEST_CODE, resultCode = Activity.RESULT_OK,
     * GUID != null.
     *
     * User clicked Option RENAME
     */
    @Test
    @Throws(Exception::class)
    fun onActivityResultTest_OptionsRename() {
        createPresenter(viewContract = null).onActivityResult(resultCode = Activity.RESULT_OK,
                requestCode = ITEM_OPTIONS_REQUEST_CODE,
                data = Intent()
                        .putExtra(KEY_OPTION_ID, ConnectionOptions.RENAME.ordinal)
                        .putExtra(KEY_GUID, "guid1"))

        Mockito.never()

        createPresenter(viewContract = mockView).onActivityResult(resultCode = Activity.RESULT_OK,
                requestCode = ITEM_OPTIONS_REQUEST_CODE,
                data = Intent()
                        .putExtra(KEY_OPTION_ID, ConnectionOptions.RENAME.ordinal)
                        .putExtra(KEY_GUID, "guid1"))

        Mockito.verify(mockView).showConnectionNameEditView(
                connectionGuid = "guid1",
                connectionName = "Demobank1",
                requestCode = RENAME_REQUEST_CODE
        )
    }

    /**
     * test onActivityResult,
     * requestCode = ITEM_OPTIONS_REQUEST_CODE, resultCode = Activity.RESULT_OK,
     * GUID != null.
     *
     * User clicked Option DELETE
     */
    @Test
    @Throws(Exception::class)
    fun onActivityResultTest_OptionsDelete() {
        createPresenter(viewContract = null).onActivityResult(resultCode = Activity.RESULT_OK,
                requestCode = ITEM_OPTIONS_REQUEST_CODE,
                data = Intent()
                        .putExtra(KEY_OPTION_ID, ConnectionOptions.DELETE.ordinal)
                        .putExtra(KEY_GUID, "guid1"))

        Mockito.never()

        createPresenter(viewContract = mockView).onActivityResult(resultCode = Activity.RESULT_OK,
                requestCode = ITEM_OPTIONS_REQUEST_CODE,
                data = Intent()
                        .putExtra(KEY_OPTION_ID, ConnectionOptions.DELETE.ordinal)
                        .putExtra(KEY_GUID, "guid1"))

        Mockito.verify(mockView).showDeleteConnectionView(
                connectionGuid = "guid1",
                requestCode = DELETE_REQUEST_CODE
        )
    }

    /**
     * test onActivityResult,
     * requestCode = ITEM_OPTIONS_REQUEST_CODE, resultCode = Activity.RESULT_OK,
     * GUID != null.
     *
     * User clicked Option RECONNECT
     */
    @Test
    @Throws(Exception::class)
    fun onActivityResultTest_OptionsReconnect() {
        createPresenter(viewContract = null).onActivityResult(resultCode = Activity.RESULT_OK,
                requestCode = ITEM_OPTIONS_REQUEST_CODE,
                data = Intent()
                        .putExtra(KEY_OPTION_ID, ConnectionOptions.RECONNECT.ordinal)
                        .putExtra(KEY_GUID, "guid1"))

        Mockito.never()

        createPresenter(viewContract = mockView).onActivityResult(resultCode = Activity.RESULT_OK,
                requestCode = ITEM_OPTIONS_REQUEST_CODE,
                data = Intent()
                        .putExtra(KEY_OPTION_ID, ConnectionOptions.RECONNECT.ordinal)
                        .putExtra(KEY_GUID, "guid1"))

        Mockito.verify(mockView).showConnectView(connectionGuid = "guid1")
    }

    /**
     * test onActivityResult,
     * requestCode = RENAME_REQUEST_CODE,
     * GUID == null or No connection associated with GUID or viewContract == null.
     *
     * User entered new Connection name
     */
    @Test
    @Throws(Exception::class)
    fun onActivityResultTest_Rename_InvalidParams() {
        createPresenter(viewContract = mockView).onActivityResult(resultCode = Activity.RESULT_OK,
                requestCode = RENAME_REQUEST_CODE,
                data = Intent().putExtra(KEY_NAME, "new name"))

        Mockito.never()

        createPresenter(viewContract = mockView).onActivityResult(resultCode = Activity.RESULT_OK,
                requestCode = RENAME_REQUEST_CODE,
                data = Intent()
                        .putExtra(KEY_NAME, "new name")
                        .putExtra(KEY_GUID, "guidX"))

        Mockito.never()

        createPresenter(viewContract = null).onActivityResult(resultCode = Activity.RESULT_OK,
                requestCode = RENAME_REQUEST_CODE,
                data = Intent()
                        .putExtra(KEY_NAME, "new name")
                        .putExtra(KEY_GUID, "guid2"))

        Mockito.never()

        createPresenter(viewContract = mockView).onActivityResult(resultCode = Activity.RESULT_OK,
                requestCode = RENAME_REQUEST_CODE,
                data = Intent()
                        .putExtra(KEY_NAME, "")
                        .putExtra(KEY_GUID, "guid2"))

        Mockito.never()

        createPresenter(viewContract = mockView).onActivityResult(resultCode = Activity.RESULT_OK,
                requestCode = RENAME_REQUEST_CODE,
                data = Intent()
                        .putExtra(KEY_NAME, "Demobank2")
                        .putExtra(KEY_GUID, "guid2"))

        Mockito.never()
    }

    /**
     * test onActivityResult,
     * requestCode = RENAME_REQUEST_CODE,
     * GUID != null.
     *
     * User entered new Connection name
     */
    @Test
    @Throws(Exception::class)
    fun onActivityResultTest_Rename() {
        createPresenter(viewContract = mockView).onActivityResult(resultCode = Activity.RESULT_OK,
                requestCode = RENAME_REQUEST_CODE,
                data = Intent()
                        .putExtra(KEY_NAME, "new name")
                        .putExtra(KEY_GUID, "guid2"))

        Mockito.verify(mockConnectionsRepository)
                .updateNameAndSave(connection = allConnections[1], newName = "new name")
        Mockito.verify(mockView)
                .updateListItemName(connectionGuid = "guid2", name = "new name")
    }

    /**
     * test onActivityResult, requestCode = DELETE_REQUEST_CODE, GUID != null.
     * viewContract == null
     *
     * User confirmed single Connection deletion
     */
    @Test
    @Throws(Exception::class)
    fun onActivityResultTest_DeleteSingleConnection_InvalidParams_Case1() {
        val presenter = createPresenter(viewContract = null)
        Mockito.doReturn(true).`when`(mockConnectionsRepository).deleteConnection("guid2")
        presenter.onActivityResult(resultCode = Activity.RESULT_OK, requestCode = DELETE_REQUEST_CODE,
                data = Intent().putExtra(KEY_GUID, "guid2"))

        Mockito.verify(mockApiManager).revokeConnections(listOf(ConnectionAndKey(allConnections[1], mockPrivateKey)), presenter)
        Mockito.verify(mockConnectionsRepository).deleteConnection("guid2")
        Mockito.verify(mockKeyStoreManager).deleteKeyPair("guid2")
        Mockito.verifyNoMoreInteractions(mockView)
    }

    /**
     * test onActivityResult, requestCode = DELETE_REQUEST_CODE, GUID != null.
     *
     * User confirmed single Connection deletion
     */
    @Test
    @Throws(Exception::class)
    fun onActivityResultTest_DeleteSingleConnection() {
        Mockito.doReturn(true).`when`(mockConnectionsRepository).deleteConnection("guid2")
        val presenter = createPresenter(viewContract = mockView)
        presenter.onActivityResult(resultCode = Activity.RESULT_OK,
                requestCode = DELETE_REQUEST_CODE,
                data = Intent().putExtra(KEY_GUID, "guid2"))

        Mockito.verify(mockApiManager).revokeConnections(listOf(ConnectionAndKey(allConnections[1], mockPrivateKey)), presenter)
        Mockito.verify(mockConnectionsRepository).deleteConnection("guid2")
        Mockito.verify(mockKeyStoreManager).deleteKeyPair("guid2")
        Mockito.verify(mockView).updateViewContent()
    }

    /**
     * test onActivityResult, requestCode = DELETE_ALL_REQUEST_CODE, GUID == null.
     * User confirmed all Connections deletion
     */
    @Test
    @Throws(Exception::class)
    fun onActivityResultTest_DeleteAllConnections() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.onActivityResult(resultCode = Activity.RESULT_OK,
                requestCode = DELETE_ALL_REQUEST_CODE,
                data = Intent())

        Mockito.verify(mockApiManager).revokeConnections(listOf(
                ConnectionAndKey(activeConnections.first(), mockPrivateKey)
        ), presenter)
        Mockito.verify(mockConnectionsRepository).deleteAllConnections()
        Mockito.verify(mockKeyStoreManager).deleteKeyPairs(listOf("guid1", "guid2", "guid3"))
        Mockito.verify(mockView).updateViewContent()
    }

    @Before
    fun setUp() {
        Mockito.doReturn(allConnections).`when`(mockConnectionsRepository).getAllConnections()
        Mockito.doReturn(activeConnections).`when`(mockConnectionsRepository).getAllActiveConnections()
        Mockito.doReturn(null).`when`(mockConnectionsRepository).getByGuid("guidX")
        Mockito.doReturn(allConnections[0]).`when`(mockConnectionsRepository).getByGuid("guid1")
        Mockito.doReturn(allConnections[1]).`when`(mockConnectionsRepository).getByGuid("guid2")
        Mockito.doReturn(allConnections[2]).`when`(mockConnectionsRepository).getByGuid("guid3")
        Mockito.doReturn(KeyPair(null, mockPrivateKey)).`when`(mockKeyStoreManager).getKeyPair(Mockito.anyString())
    }

    private val mockKeyStoreManager = Mockito.mock(KeyStoreManagerAbs::class.java)
    private val mockConnectionsRepository = Mockito.mock(ConnectionsRepositoryAbs::class.java)
    private val mockApiManager = Mockito.mock(AuthenticatorApiManagerAbs::class.java)
    private val mockView = Mockito.mock(ConnectionsListContract.View::class.java)
    private val mockPrivateKey = Mockito.mock(PrivateKey::class.java)
    private val allConnections = listOf(
            Connection().setGuid("guid1").setCode("demobank").setName("Demobank1")
                    .setStatus(ConnectionStatus.INACTIVE).setAccessToken("token1")
                    .setCreatedAt(100L).setUpdatedAt(100L).setSupportEmail("example@example.com"),
            Connection().setGuid("guid2").setCode("demobank").setName("Demobank2")
                    .setStatus(ConnectionStatus.ACTIVE).setAccessToken("token2")
                    .setCreatedAt(300L).setUpdatedAt(300L),
            Connection().setGuid("guid3").setCode("test").setName("Demobank3")
                    .setStatus(ConnectionStatus.ACTIVE).setAccessToken("")
                    .setCreatedAt(200L).setUpdatedAt(200L)
    )
    private val activeConnections = allConnections.filter { it.isActive() }

    private fun createPresenter(viewContract: ConnectionsListContract.View? = null): ConnectionsListPresenter {
        return ConnectionsListPresenter(
                appContext = TestTools.applicationContext,
                keyStoreManager = mockKeyStoreManager,
                connectionsRepository = mockConnectionsRepository,
                apiManager = mockApiManager).apply { this.viewContract = viewContract }
    }
}
