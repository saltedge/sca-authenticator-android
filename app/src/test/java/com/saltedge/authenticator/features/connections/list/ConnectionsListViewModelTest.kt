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
package com.saltedge.authenticator.features.connections.list

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.test.core.app.ApplicationProvider
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.DELETE_REQUEST_CODE
import com.saltedge.authenticator.app.KEY_GUID
import com.saltedge.authenticator.app.RENAME_REQUEST_CODE
import com.saltedge.authenticator.features.connections.common.ConnectionItemViewModel
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.constants.KEY_NAME
import com.saltedge.authenticator.sdk.model.ConsentData
import com.saltedge.authenticator.sdk.model.ConsentSharedData
import com.saltedge.authenticator.sdk.model.connection.ConnectionAndKey
import com.saltedge.authenticator.sdk.model.connection.ConnectionStatus
import com.saltedge.authenticator.sdk.tools.crypt.CryptoToolsAbs
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManagerAbs
import org.hamcrest.Matchers.equalTo
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import java.security.PrivateKey

@RunWith(RobolectricTestRunner::class)
class ConnectionsListViewModelTest {

    private lateinit var viewModel: ConnectionsListViewModel
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val mockConnectionsRepository = mock(ConnectionsRepositoryAbs::class.java)
    private val mockKeyStoreManager = mock(KeyStoreManagerAbs::class.java)
    private val mockApiManager = mock(AuthenticatorApiManagerAbs::class.java)
    private val mockPrivateKey = Mockito.mock(PrivateKey::class.java)
    private val mockCryptoTools = mock(CryptoToolsAbs::class.java)
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
    private val mockConnectionAndKey = ConnectionAndKey(connections[1], mockPrivateKey)
    private val consentData: List<ConsentData> = listOf(
        ConsentData(
            id = "555",
            connectionId = "1",
            userId = "1",
            tppName = "title",
            consentTypeString = "aisp",
            accounts = emptyList(),
            expiresAt = DateTime(0).withZone(DateTimeZone.UTC),
            createdAt = DateTime(0).withZone(DateTimeZone.UTC),
            sharedData = ConsentSharedData(balance = true, transactions = true)
        )
    )

    @Before
    fun setUp() {
        Mockito.doReturn(connections).`when`(mockConnectionsRepository).getAllConnections()
        Mockito.doReturn(connections[0]).`when`(mockConnectionsRepository).getByGuid("guid1")
        Mockito.doReturn(connections[1]).`when`(mockConnectionsRepository).getByGuid("guid2")
        given(mockConnectionsRepository.getAllActiveConnections()).willReturn(listOf(connections[1]))
        given(mockKeyStoreManager.createConnectionAndKeyModel(connections[1])).willReturn(
            mockConnectionAndKey
        )

        viewModel = ConnectionsListViewModel(
            appContext = context,
            connectionsRepository = mockConnectionsRepository,
            keyStoreManager = mockKeyStoreManager,
            apiManager = mockApiManager,
            cryptoTools = mockCryptoTools
        )
    }

    /**
     * Test onStart when db is empty
     */
    @Test
    @Throws(Exception::class)
    fun onStartTestCase1() {
        //given
        Mockito.doReturn(listOf<Connection>()).`when`(mockConnectionsRepository).getAllConnections()

        //when
        viewModel.onStart()

        //than
        assertThat(viewModel.emptyViewVisibility.value, equalTo(View.VISIBLE))
        assertThat(viewModel.listVisibility.value, equalTo(View.GONE))
    }

    /**
     * Test onStart when db isn't empty
     */
    @Test
    @Throws(Exception::class)
    fun onStartTestCase2() {
        //given
        val connection: List<ConnectionItemViewModel> =
            connections.convertConnectionsToViewModels(context)
        viewModel.listItems.value = connection

        //when
        viewModel.onStart()

        //than
        assertThat(viewModel.listItemsValues, equalTo(connection))
        assertThat(viewModel.emptyViewVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.listVisibility.value, equalTo(View.VISIBLE))
    }

    @Test
    @Throws(Exception::class)
    fun onStartTestCase3() {
        //given
        val connection: List<ConnectionItemViewModel> =
            listOf(connections[1]).convertConnectionsToViewModels(context)
        viewModel.listItems.value = connection

        //when
        viewModel.onStart()

        //than
        Mockito.verify(mockApiManager).getConsents(
            connectionsAndKeys = listOf(ConnectionAndKey(connections[1], mockPrivateKey)),
            resultCallback = viewModel
        )
    }

    @Test
    @Throws(Exception::class)
    fun refreshConsentsTest() {
        //given
        val connection: List<ConnectionItemViewModel> =
            listOf(connections[1]).convertConnectionsToViewModels(context)
        viewModel.listItems.value = connection

        //when
        viewModel.refreshConsents()

        //than
        Mockito.verify(mockApiManager).getConsents(
            connectionsAndKeys = listOf(ConnectionAndKey(connections[1], mockPrivateKey)),
            resultCallback = viewModel
        )
    }

    @Test
    @Throws(Exception::class)
    fun processDecryptedConsentsResultTestCase1() {
        //given
        viewModel.listItems.postValue(emptyList())

        //when
        viewModel.processDecryptedConsentsResult(result = consentData)

        //then
        assertThat(viewModel.listItems.value, equalTo(emptyList()))
    }

    @Test
    @Throws(Exception::class)
    fun processDecryptedConsentsResultTestCase2() {
        //given
        val connection: List<ConnectionItemViewModel> = listOf(
            ConnectionItemViewModel(
                guid = "guid1",
                connectionId = "1",
                name = "Demobank1",
                statusDescription = "Inactive. Please reconnect.",
                statusDescriptionColorRes = R.color.red_and_red_light,
                logoUrl = "",
                reconnectMenuItemIsVisible = true,
                deleteMenuItemText = R.string.actions_remove,
                deleteMenuItemImage = R.drawable.ic_menu_remove_24dp,
                isChecked = false
            )
        )
        viewModel.listItems.value = connection

        //when
        viewModel.processDecryptedConsentsResult(result = consentData)

        //then
        assertThat(
            viewModel.listItems.value, equalTo(
            listOf(
                ConnectionItemViewModel(
                    guid = "guid1",
                    connectionId = "1",
                    name = "Demobank1",
                    statusDescription = "Inactive. Please reconnect.",
                    statusDescriptionColorRes = R.color.red_and_red_light,
                    logoUrl = "",
                    consentsDescription = "1 consent ·",
                    consentMenuItemIsVisible = true,
                    reconnectMenuItemIsVisible = true,
                    deleteMenuItemText = R.string.actions_remove,
                    deleteMenuItemImage = R.drawable.ic_menu_remove_24dp,
                    isChecked = false
                )
            )
        )
        )
    }

    @Test
    @Throws(Exception::class)
    fun processDecryptedConsentsResultTestCase3() {
        //given
        val connection: List<ConnectionItemViewModel> = listOf(
            ConnectionItemViewModel(
                guid = "guid1",
                connectionId = "",
                name = "Demobank1",
                statusDescription = "Inactive. Please reconnect.",
                statusDescriptionColorRes = R.color.red_and_red_light,
                logoUrl = "",
                reconnectMenuItemIsVisible = true,
                deleteMenuItemText = R.string.actions_remove,
                deleteMenuItemImage = R.drawable.ic_menu_remove_24dp,
                isChecked = false
            )
        )
        viewModel.listItems.value = connection

        //when
        viewModel.processDecryptedConsentsResult(result = emptyList())

        //then
        assertThat(
            viewModel.listItems.value, equalTo(
            listOf(
                ConnectionItemViewModel(
                    guid = "guid1",
                    connectionId = "",
                    name = "Demobank1",
                    statusDescription = "Inactive. Please reconnect.",
                    statusDescriptionColorRes = R.color.red_and_red_light,
                    logoUrl = "",
                    reconnectMenuItemIsVisible = true,
                    deleteMenuItemText = R.string.actions_remove,
                    deleteMenuItemImage = R.drawable.ic_menu_remove_24dp,
                    isChecked = false
                )
            )
        )
        )
    }

    @Test
    @Throws(Exception::class)
    fun getConnectionStatusDescriptionTestCase1() {
        //given
        val connection: List<ConnectionItemViewModel> =
            listOf(connections[0]).convertConnectionsToViewModels(context)
        viewModel.listItems.value = connection

        //than
        assertThat(
            viewModel.listItems.value, equalTo(
            listOf(
                ConnectionItemViewModel(
                    guid = "guid1",
                    connectionId = "",
                    name = "Demobank1",
                    statusDescription = "Inactive. Please reconnect.",
                    statusDescriptionColorRes = R.color.red_and_red_light,
                    logoUrl = "",
                    reconnectMenuItemIsVisible = true,
                    deleteMenuItemText = R.string.actions_remove,
                    deleteMenuItemImage = R.drawable.ic_menu_remove_24dp,
                    isChecked = false
                )
            )
        )
        )
    }

    @Test
    @Throws(Exception::class)
    fun getConnectionStatusDescriptionTestCase2() {
        //given
        val connection: List<ConnectionItemViewModel> =
            listOf(connections[1]).convertConnectionsToViewModels(context)
        viewModel.listItems.value = connection

        //than
        assertThat(
            viewModel.listItems.value, equalTo(
            listOf(
                ConnectionItemViewModel(
                    guid = "guid2",
                    connectionId = "",
                    name = "Demobank2",
                    statusDescription = "Linked on 1 January 1970",
                    statusDescriptionColorRes = R.color.dark_60_and_grey_100,
                    logoUrl = "",
                    reconnectMenuItemIsVisible = false,
                    deleteMenuItemText = R.string.actions_delete,
                    deleteMenuItemImage = R.drawable.ic_menu_delete_24dp,
                    isChecked = false
                )
            )
        )
        )
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTest() {
        viewModel.onViewClick(-1)

        assertNull(viewModel.onQrScanClickEvent.value)

        viewModel.onViewClick(R.id.actionView)

        assertNotNull(viewModel.onQrScanClickEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTest() {
        //given
        val connection: List<ConnectionItemViewModel> =
            connections.convertConnectionsToViewModels(context)
        viewModel.listItems.postValue(connection)

        assertNull(viewModel.onListItemClickEvent.value)

        //when
        viewModel.onListItemClick(1)

        //than
        assertThat(viewModel.onListItemClickEvent.value, equalTo(ViewModelEvent(content = 1)))
    }

    @Test
    @Throws(Exception::class)
    fun onRenameOptionSelectedTest() {
        //given
        val connection: List<ConnectionItemViewModel> =
            connections.convertConnectionsToViewModels(context)

        viewModel.listItems.value = connection
        viewModel.onListItemClickEvent.value = ViewModelEvent<Int>(1)

        //when
        viewModel.onRenameOptionSelected()

        //than
        assertThat(
            viewModel.onRenameClickEvent.value!!.peekContent().getString(KEY_GUID),
            equalTo("guid2")
        )
        assertThat(
            viewModel.onRenameClickEvent.value!!.peekContent().getString(KEY_NAME),
            equalTo("Demobank2")
        )
    }

    @Test
    @Throws(Exception::class)
    fun onContactSupportOptionSelectedTest() {
        //given
        val connection: List<ConnectionItemViewModel> =
            connections.convertConnectionsToViewModels(context)

        viewModel.listItems.value = connection
        viewModel.onListItemClickEvent.value = ViewModelEvent<Int>(1)

        //when
        viewModel.onContactSupportOptionSelected()

        //than
        assertThat(viewModel.onSupportClickEvent.value, equalTo(ViewModelEvent(null) ?: ""))
        assertNotNull(viewModel.onSupportClickEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onDeleteOptionsSelectedTest() {
        //given
        val connection: List<ConnectionItemViewModel> =
            connections.convertConnectionsToViewModels(context)

        viewModel.listItems.value = connection
        viewModel.onListItemClickEvent.value = ViewModelEvent<Int>(1)

        //when
        viewModel.onDeleteOptionsSelected()

        //than
        assertThat(
            viewModel.onDeleteClickEvent.value!!.peekContent().getString(KEY_GUID),
            equalTo("guid2")
        )
    }

    @Test
    @Throws(Exception::class)
    fun onReconnectOptionSelectedTest() {
        //given
        val connection: List<ConnectionItemViewModel> =
            connections.convertConnectionsToViewModels(context)

        viewModel.listItems.value = connection
        viewModel.onListItemClickEvent.value = ViewModelEvent<Int>(1)

        //when
        viewModel.onReconnectOptionSelected()

        //than
        assertThat(viewModel.onReconnectClickEvent.value, equalTo(ViewModelEvent("guid2")))
        assertNotNull(viewModel.onReconnectClickEvent.value)
    }

    /**
     * test onActivityResult,
     * when resultCode != Activity.RESULT_OK or data == null or unknown requestCode
     */
    @Test
    @Throws(Exception::class)
    fun onActivityResultTest() {
        viewModel.onActivityResult(
            requestCode = RENAME_REQUEST_CODE,
            resultCode = Activity.RESULT_CANCELED,
            data = Intent()
        )

        Mockito.never()

        viewModel.onActivityResult(
            requestCode = RENAME_REQUEST_CODE,
            resultCode = Activity.RESULT_OK,
            data = null
        )

        Mockito.never()

        viewModel.onActivityResult(
            requestCode = -1,
            resultCode = Activity.RESULT_OK,
            data = Intent()
        )

        Mockito.never()
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
        viewModel.onActivityResult(
            resultCode = Activity.RESULT_OK,
            requestCode = RENAME_REQUEST_CODE,
            data = Intent().putExtra(KEY_NAME, "new name")
        )

        Mockito.never()

        viewModel.onActivityResult(
            resultCode = Activity.RESULT_OK,
            requestCode = RENAME_REQUEST_CODE,
            data = Intent()
                .putExtra(KEY_NAME, "new name")
                .putExtra(KEY_GUID, "guidX")
        )

        Mockito.never()

        viewModel.onActivityResult(
            resultCode = Activity.RESULT_OK,
            requestCode = RENAME_REQUEST_CODE,
            data = Intent()
                .putExtra(KEY_NAME, "new name")
                .putExtra(KEY_GUID, "guid2")
        )

        Mockito.never()

        viewModel.onActivityResult(
            resultCode = Activity.RESULT_OK,
            requestCode = RENAME_REQUEST_CODE,
            data = Intent()
                .putExtra(KEY_NAME, "")
                .putExtra(KEY_GUID, "guid2")
        )

        Mockito.never()

        viewModel.onActivityResult(
            resultCode = Activity.RESULT_OK,
            requestCode = RENAME_REQUEST_CODE,
            data = Intent()
                .putExtra(KEY_NAME, "Demobank2")
                .putExtra(KEY_GUID, "guid2")
        )

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
        val connection: List<ConnectionItemViewModel> =
            connections.convertConnectionsToViewModels(context)

        viewModel.listItems.value = connection

        viewModel.onActivityResult(
            resultCode = Activity.RESULT_OK,
            requestCode = RENAME_REQUEST_CODE,
            data = Intent()
                .putExtra(KEY_NAME, "new name")
                .putExtra(KEY_GUID, "guid2")
        )

        assertNotNull(viewModel.updateListItemEvent.value)
        Mockito.verify(mockConnectionsRepository)
            .updateNameAndSave(connection = connections[1], newName = "new name")
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
        val connection: List<ConnectionItemViewModel> =
            connections.convertConnectionsToViewModels(context)

        viewModel.listItems.value = connection

        Mockito.doReturn(true).`when`(mockConnectionsRepository).deleteConnection("guid2")
        Mockito.doReturn(ConnectionAndKey(connections[1], mockPrivateKey)).`when`(
            mockKeyStoreManager
        ).createConnectionAndKeyModel(connections[1])
        val presenter = viewModel
        presenter.onActivityResult(
            resultCode = Activity.RESULT_OK, requestCode = DELETE_REQUEST_CODE,
            data = Intent().putExtra(KEY_GUID, "guid2")
        )

        Mockito.verify(mockApiManager).revokeConnections(
            listOf(
                ConnectionAndKey(
                    connections[1],
                    mockPrivateKey
                )
            ), presenter
        )
        Mockito.verify(mockConnectionsRepository).deleteConnection("guid2")
        Mockito.verify(mockKeyStoreManager).deleteKeyPair("guid2")
    }

    /**
     * test onActivityResult, requestCode = DELETE_REQUEST_CODE, GUID != null.
     *
     * User confirmed single Connection deletion
     */
    @Test
    @Throws(Exception::class)
    fun onActivityResultTest_DeleteSingleConnection() {
        val connection: List<ConnectionItemViewModel> =
            connections.convertConnectionsToViewModels(context)

        viewModel.listItems.value = connection

        Mockito.doReturn(true).`when`(mockConnectionsRepository).deleteConnection("guid2")
        Mockito.doReturn(ConnectionAndKey(connections[1], mockPrivateKey)).`when`(
            mockKeyStoreManager
        ).createConnectionAndKeyModel(connections[1])
        viewModel.onActivityResult(
            resultCode = Activity.RESULT_OK,
            requestCode = DELETE_REQUEST_CODE,
            data = Intent().putExtra(KEY_GUID, "guid2")
        )

        Mockito.verify(mockApiManager).revokeConnections(
            listOf(ConnectionAndKey(connections[1], mockPrivateKey)),
            viewModel
        )
        Mockito.verify(mockConnectionsRepository).deleteConnection("guid2")
        Mockito.verify(mockKeyStoreManager).deleteKeyPair("guid2")
    }
}
