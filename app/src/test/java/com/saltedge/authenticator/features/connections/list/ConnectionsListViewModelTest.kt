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

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.test.core.app.ApplicationProvider
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.guid
import com.saltedge.authenticator.core.api.KEY_NAME
import com.saltedge.authenticator.core.model.ConnectionStatus
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.features.connections.common.ConnectionItemViewModel
import com.saltedge.authenticator.features.connections.list.menu.MenuData
import com.saltedge.authenticator.features.menu.MenuItemData
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.api.model.ConsentData
import com.saltedge.authenticator.sdk.api.model.ConsentSharedData
import com.saltedge.authenticator.sdk.tools.crypt.CryptoToolsAbs
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
    private val mockKeyStoreManager = mock(KeyManagerAbs::class.java)
    private val mockApiManager = mock(AuthenticatorApiManagerAbs::class.java)
    private val mockPrivateKey = Mockito.mock(PrivateKey::class.java)
    private val mockCryptoTools = mock(CryptoToolsAbs::class.java)
    private val connections = listOf(
        Connection().apply {
            id = "1"
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
            id = "2"
            guid = "guid2"
            code = "demobank2"
            name = "Demobank2"
            status = "${ConnectionStatus.ACTIVE}"
            supportEmail = "example@example.com"
            accessToken = "token2"
            createdAt = 300L
            updatedAt = 300L
        }
    )
    private val mockConnectionAndKey = RichConnection(connections[1], mockPrivateKey)
    private val consentData: List<ConsentData> = listOf(
        ConsentData(
            id = "555",
            connectionId = "2",
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
        given(mockKeyStoreManager.enrichConnection(connections[1])).willReturn(mockConnectionAndKey)

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

        //then
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

        //then
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

        //then
        Mockito.verify(mockApiManager).getConsents(
            connectionsAndKeys = listOf(RichConnection(connections[1], mockPrivateKey)),
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

        //then
        Mockito.verify(mockApiManager).getConsents(
            connectionsAndKeys = listOf(RichConnection(connections[1], mockPrivateKey)),
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
        viewModel.listItems.value = listOf(
            ConnectionItemViewModel(
                guid = "guid2",
                connectionId = "2",
                name = "Demobank2",
                statusDescription = "Linked on 1 January 1970",
                statusDescriptionColorRes = R.color.dark_60_and_grey_100,
                logoUrl = "",
                isActive = true,
                isChecked = false
            )
        )

        //when
        viewModel.processDecryptedConsentsResult(result = consentData)

        //then
        assertThat(
            viewModel.listItems.value,
            equalTo(listOf(
                ConnectionItemViewModel(
                    guid = "guid2",
                    connectionId = "2",
                    name = "Demobank2",
                    statusDescription = "Linked on 1 January 1970",
                    statusDescriptionColorRes = R.color.dark_60_and_grey_100,
                    logoUrl = "",
                    consentsDescription = "1 consent\u30FB",
                    isActive = true,
                    isChecked = false
                )
            ))
        )
    }

    @Test
    @Throws(Exception::class)
    fun processDecryptedConsentsResultTestCase3() {
        //given
        val connection: List<ConnectionItemViewModel> = listOf(
            ConnectionItemViewModel(
                guid = "guid1",
                connectionId = "1",
                name = "Demobank1",
                statusDescription = "Inactive. Please reconnect.",
                statusDescriptionColorRes = R.color.red_and_red_light,
                logoUrl = "",
                isActive = true,
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
                    connectionId = "1",
                    name = "Demobank1",
                    statusDescription = "Inactive. Please reconnect.",
                    statusDescriptionColorRes = R.color.red_and_red_light,
                    logoUrl = "",
                    isActive = true,
                    isChecked = false
                )
            )
        )
        )
    }

    @Test
    @Throws(Exception::class)
    fun getConnectionStatusDescriptionTestCase1() {
        //given inactive connection
        viewModel.listItems.value = listOf(connections[0]).convertConnectionsToViewModels(context)

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
                    isActive = false,
                    isChecked = false
                )
            )
        )
        )
    }

    @Test
    @Throws(Exception::class)
    fun getConnectionStatusDescriptionTestCase2() {
        //given active connection
        viewModel.listItems.value = listOf(connections[1]).convertConnectionsToViewModels(context)

        //then
        assertThat(
            viewModel.listItems.value, equalTo(
            listOf(
                ConnectionItemViewModel(
                    guid = "guid2",
                    connectionId = "2",
                    name = "Demobank2",
                    statusDescription = "Linked on 1 January 1970",
                    statusDescriptionColorRes = R.color.dark_60_and_grey_100,
                    logoUrl = "",
                    isActive = true,
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
    fun onListItemClickTestCase1() {
        //given list of items, list of consents and index of active item
        viewModel.listItems.value = connections.convertConnectionsToViewModels(context)
        viewModel.processDecryptedConsentsResult(result = consentData)
        val activeItemIndex = 1

        assertNull(viewModel.onListItemClickEvent.value)

        //when
        viewModel.onListItemClick(activeItemIndex)

        //then
        assertThat(
            viewModel.onListItemClickEvent.value,
            equalTo(ViewModelEvent(MenuData(
                menuId = activeItemIndex,
                items = listOf(
                    MenuItemData(
                        id = ConnectionsListViewModel.PopupMenuItem.RENAME.ordinal,
                        iconRes = R.drawable.ic_menu_edit_24dp,
                        textRes = R.string.actions_rename
                    ),
                    MenuItemData(
                        id = ConnectionsListViewModel.PopupMenuItem.SUPPORT.ordinal,
                        iconRes = R.drawable.ic_contact_support_24dp,
                        textRes = R.string.actions_contact_support
                    ),
                    MenuItemData(
                        id = ConnectionsListViewModel.PopupMenuItem.CONSENTS.ordinal,
                        iconRes = R.drawable.ic_view_consents_24dp,
                        textRes = R.string.actions_view_consents
                    ),
                    MenuItemData(
                        id = ConnectionsListViewModel.PopupMenuItem.DELETE.ordinal,
                        iconRes = R.drawable.ic_menu_delete_24dp,
                        textRes = R.string.actions_delete
                    )
                )
            )))
        )
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTestCase2() {
        //given list of items, empty list of consents and index of active item
        viewModel.listItems.value = connections.convertConnectionsToViewModels(context)
        val inactiveItemIndex = 0

        assertNull(viewModel.onListItemClickEvent.value)

        //when
        viewModel.onListItemClick(inactiveItemIndex)

        //then
        assertThat(
            viewModel.onListItemClickEvent.value,
            equalTo(ViewModelEvent(MenuData(
                menuId = inactiveItemIndex,
                items = listOf(
                    MenuItemData(
                        id = ConnectionsListViewModel.PopupMenuItem.RECONNECT.ordinal,
                        iconRes = R.drawable.ic_menu_reconnect_24dp,
                        textRes = R.string.actions_reconnect
                    ),
                    MenuItemData(
                        id = ConnectionsListViewModel.PopupMenuItem.RENAME.ordinal,
                        iconRes = R.drawable.ic_menu_edit_24dp,
                        textRes = R.string.actions_rename
                    ),
                    MenuItemData(
                        id = ConnectionsListViewModel.PopupMenuItem.SUPPORT.ordinal,
                        iconRes = R.drawable.ic_contact_support_24dp,
                        textRes = R.string.actions_contact_support
                    ),
                    MenuItemData(
                        id = ConnectionsListViewModel.PopupMenuItem.DELETE.ordinal,
                        iconRes = R.drawable.ic_menu_remove_24dp,
                        textRes = R.string.actions_remove
                    )
                )
            )))
        )
    }

    @Test
    @Throws(Exception::class)
    fun onReconnectOptionSelectedTest() {
        //given itemId RECONNECT
        viewModel.listItems.value = connections.convertConnectionsToViewModels(context)
        val activeItemIndex = 1
        val itemId = ConnectionsListViewModel.PopupMenuItem.RECONNECT.ordinal

        //when
        viewModel.onMenuItemClick(menuId = activeItemIndex, itemId = itemId)

        //then
        assertThat(viewModel.onReconnectClickEvent.value, equalTo(ViewModelEvent("guid2")))
    }

    @Test
    @Throws(Exception::class)
    fun onMenuItemClickTestCase2() {
        //given itemId RENAME
        viewModel.listItems.value = connections.convertConnectionsToViewModels(context)
        val activeItemIndex = 1
        val itemId = ConnectionsListViewModel.PopupMenuItem.RENAME.ordinal

        //when
        viewModel.onMenuItemClick(menuId = activeItemIndex, itemId = itemId)

        //then
        val eventContent = viewModel.onRenameClickEvent.value!!.peekContent()
        assertThat(eventContent.guid, equalTo("guid2"))
        assertThat(eventContent.getString(KEY_NAME), equalTo("Demobank2"))
    }

    @Test
    @Throws(Exception::class)
    fun onMenuItemClickTestCase3() {
        //given itemId SUPPORT
        viewModel.listItems.value = connections.convertConnectionsToViewModels(context)
        val activeItemIndex = 1
        val itemId = ConnectionsListViewModel.PopupMenuItem.SUPPORT.ordinal

        //when
        viewModel.onMenuItemClick(menuId = activeItemIndex, itemId = itemId)

        //then
        assertThat(viewModel.onSupportClickEvent.value!!.peekContent(), equalTo("example@example.com"))
    }

    @Test
    @Throws(Exception::class)
    fun onMenuItemClickTestCase5() {
        //given itemId DELETE
        viewModel.listItems.value = connections.convertConnectionsToViewModels(context)
        val activeItemIndex = 1
        val itemId = ConnectionsListViewModel.PopupMenuItem.DELETE.ordinal

        //when
        viewModel.onMenuItemClick(menuId = activeItemIndex, itemId = itemId)

        //then
        assertThat(viewModel.onDeleteClickEvent.value!!.peekContent(), equalTo("guid2"))
    }

    /**
     * test onEditNameResult,
     * GUID == null or No connection associated with GUID or viewContract == null.
     *
     * User entered new Connection name
     */
    @Test
    @Throws(Exception::class)
    fun onEditNameResultTestCase1() {
        viewModel.onEditNameResult(
            data = Bundle().apply { putString(KEY_NAME, "new name") }
        )

        Mockito.never()

        viewModel.onEditNameResult(
            data = Bundle().apply {
                putString(KEY_NAME, "new name")
                guid = "guidX"
            }
        )

        Mockito.never()

        viewModel.onEditNameResult(
            data = Bundle().apply {
                putString(KEY_NAME, "new name")
                guid = "guid2"
            }
        )

        Mockito.never()

        viewModel.onEditNameResult(
            data = Bundle().apply {
                putString(KEY_NAME, "")
                guid = "guid2"
            }
        )

        Mockito.never()

        viewModel.onEditNameResult(
            data = Bundle().apply {
                putString(KEY_NAME, "Demobank2")
                guid = "guid2"
            }
        )

        Mockito.never()
    }

    /**
     * test onEditNameResult,
     * GUID != null.
     *
     * User entered new Connection name
     */
    @Test
    @Throws(Exception::class)
    fun onEditNameResultTestCase2() {
        //given
        val connection: List<ConnectionItemViewModel> =
            connections.convertConnectionsToViewModels(context)
        viewModel.listItems.value = connection

        //when
        viewModel.onEditNameResult(
            data = Bundle().apply {
                putString(KEY_NAME, "new name")
                guid = "guid2"
            }
        )

        //then
        assertNotNull(viewModel.updateListItemEvent.value)
        Mockito.verify(mockConnectionsRepository)
            .updateNameAndSave(connection = connections[1], newName = "new name")
    }

    /**
     * test onDeleteItemResult,
     * GUID != null.
     * viewContract == null
     *
     * User confirmed single Connection deletion
     */
    @Test
    @Throws(Exception::class)
    fun onDeleteItemResultTestCase1() {
        //when
        val connection: List<ConnectionItemViewModel> =
            connections.convertConnectionsToViewModels(context)
        viewModel.listItems.value = connection
        Mockito.doReturn(true).`when`(mockConnectionsRepository).deleteConnection("guid2")
        Mockito.doReturn(RichConnection(connections[1], mockPrivateKey)).`when`(mockKeyStoreManager)
            .enrichConnection(connections[1])

        //when
        viewModel.onDeleteItemResult(guid = "guid2")

        //then
        Mockito.verify(mockApiManager).revokeConnections(
            listOf(RichConnection(connections[1], mockPrivateKey)),
            viewModel
        )
        Mockito.verify(mockConnectionsRepository).deleteConnection("guid2")
        Mockito.verify(mockKeyStoreManager).deleteKeyPairIfExist("guid2")
    }

    /**
     * test onDeleteItemResult,
     * GUID != null.
     *
     * User confirmed single Connection deletion
     */
    @Test
    @Throws(Exception::class)
    fun onDeleteItemResultTestCase2() {
        //given
        val connection: List<ConnectionItemViewModel> =
            connections.convertConnectionsToViewModels(context)
        viewModel.listItems.value = connection
        Mockito.doReturn(true).`when`(mockConnectionsRepository).deleteConnection("guid2")
        Mockito.doReturn(RichConnection(connections[1], mockPrivateKey)).`when`(
            mockKeyStoreManager
        ).enrichConnection(connections[1])

        //when
        viewModel.onDeleteItemResult(guid = "guid2")

        //then
        Mockito.verify(mockApiManager).revokeConnections(
            listOf(RichConnection(connections[1], mockPrivateKey)),
            viewModel
        )
        Mockito.verify(mockConnectionsRepository).deleteConnection("guid2")
        Mockito.verify(mockKeyStoreManager).deleteKeyPairIfExist("guid2")
    }

    @Test
    @Throws(Exception::class)
    fun menuItemTest() {
        assertThat(
            ConnectionsListViewModel.PopupMenuItem.values(),
            equalTo(arrayOf(
                ConnectionsListViewModel.PopupMenuItem.RECONNECT,
                ConnectionsListViewModel.PopupMenuItem.RENAME,
                ConnectionsListViewModel.PopupMenuItem.SUPPORT,
                ConnectionsListViewModel.PopupMenuItem.CONSENTS,
                ConnectionsListViewModel.PopupMenuItem.DELETE
            ))
        )
    }
}
