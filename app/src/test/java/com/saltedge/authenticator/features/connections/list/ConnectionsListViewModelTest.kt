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
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.test.core.app.ApplicationProvider
import com.saltedge.android.test_tools.ViewModelTest
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.ConnectivityReceiverAbs
import com.saltedge.authenticator.app.guid
import com.saltedge.authenticator.core.api.KEY_NAME
import com.saltedge.authenticator.core.model.ConnectionStatus
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.features.connections.common.ConnectionItem
import com.saltedge.authenticator.features.connections.list.menu.MenuData
import com.saltedge.authenticator.features.menu.MenuItemData
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.location.DeviceLocationManagerAbs
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.api.model.ConsentData
import com.saltedge.authenticator.sdk.api.model.ConsentSharedData
import com.saltedge.authenticator.sdk.constants.API_V1_VERSION
import com.saltedge.authenticator.sdk.tools.CryptoToolsV1Abs
import com.saltedge.authenticator.sdk.v2.ScaServiceClientAbs
import com.saltedge.authenticator.sdk.v2.api.API_V2_VERSION
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Assert
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.verifyNoInteractions
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import java.security.PrivateKey

@RunWith(RobolectricTestRunner::class)
class ConnectionsListViewModelTest : ViewModelTest() {

    private lateinit var viewModel: ConnectionsListViewModel
    private lateinit var interactor: ConnectionsListInteractor
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val mockConnectionsRepository = mock(ConnectionsRepositoryAbs::class.java)
    private val mockKeyStoreManager = mock(KeyManagerAbs::class.java)
    private val mockPrivateKey = mock(PrivateKey::class.java)
    private val mockCryptoToolsV1 = mock(CryptoToolsV1Abs::class.java)
    private val mockApiManagerV1 = mock(AuthenticatorApiManagerAbs::class.java)
    private val mockApiManagerV2 = mock(ScaServiceClientAbs::class.java)
    private val mockLocationManager = mock(DeviceLocationManagerAbs::class.java)
    private val mockConnectivityReceiver = mock(ConnectivityReceiverAbs::class.java)

    private val connection1 = Connection().apply {
        id = "1"
        guid = "guid1"
        code = "demobank1"
        name = "Demobank1"
        status = "${ConnectionStatus.ACTIVE}"
        accessToken = "token1"
        supportEmail = "example@example.com"
        createdAt = 100L
        updatedAt = 100L
        apiVersion = API_V1_VERSION
        geolocationRequired = true
    }
    private val connection2 = Connection().apply {
        id = "2"
        guid = "guid2"
        code = "demobank2"
        name = "Demobank2"
        status = "${ConnectionStatus.ACTIVE}"
        supportEmail = "example@example.com"
        accessToken = "token2"
        createdAt = 200L
        updatedAt = 200L
        apiVersion = API_V2_VERSION
    }
    private val connection3Inactive = Connection().apply {
        id = "3"
        guid = "guid3"
        code = "demobank3"
        name = "Demobank3"
        status = "${ConnectionStatus.INACTIVE}"
        supportEmail = "example@example.com"
        accessToken = "token3"
        createdAt = 300L
        updatedAt = 300L
        apiVersion = API_V1_VERSION
    }
    private val richConnection1 = RichConnection(connection1, mockPrivateKey)
    private val richConnection2 = RichConnection(connection2, mockPrivateKey)
    private val richConnection3 = RichConnection(connection3Inactive, mockPrivateKey)
    private val allConnections = listOf(connection1, connection2, connection3Inactive)
    private val allActiveConnections = listOf(connection1, connection2)
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
        given(mockConnectionsRepository.getByGuid(connection1.guid)).willReturn(connection1)
        given(mockConnectionsRepository.getByGuid(connection2.guid)).willReturn(connection2)
        given(mockConnectionsRepository.getByGuid(connection3Inactive.guid)).willReturn(connection3Inactive)
        given(mockConnectionsRepository.getAllConnections()).willReturn(allConnections)
        given(mockConnectionsRepository.getAllActiveConnections()).willReturn(allActiveConnections)
        given(mockKeyStoreManager.enrichConnection(connection1, addProviderKey = false)).willReturn(richConnection1)
        given(mockKeyStoreManager.enrichConnection(connection2, addProviderKey = true)).willReturn(richConnection2)
        given(mockKeyStoreManager.enrichConnection(connection3Inactive, addProviderKey = false)).willReturn(richConnection3)

        interactor = ConnectionsListInteractor(
            connectionsRepository = mockConnectionsRepository,
            keyStoreManager = mockKeyStoreManager,
            cryptoTools = mockCryptoToolsV1,
            apiManagerV1 = mockApiManagerV1,
            apiManagerV2 = mockApiManagerV2
        )
        viewModel = ConnectionsListViewModel(
            appContext = context,
            interactor = interactor,
            locationManager = mockLocationManager,
            connectivityReceiver = mockConnectivityReceiver
        )
    }

    @Test
    @Throws(Exception::class)
    fun onStartTestCase1() {
        //given
        given(mockConnectionsRepository.getAllConnections()).willReturn(emptyList())

        //when
        viewModel.onStart()

        //then
        assertThat(viewModel.emptyViewVisibility.value, equalTo(View.VISIBLE))
        assertThat(viewModel.listVisibility.value, equalTo(View.GONE))
    }

    @Test
    @Throws(Exception::class)
    fun onStartTestCase2() {
        //given
        val expectedItems: List<ConnectionItem> = allConnections.convertConnectionsToViewModels(context, mockLocationManager)

        //when
        viewModel.onStart()

        //then
        assertThat(viewModel.listItemsValues, equalTo(expectedItems))
        assertThat(viewModel.emptyViewVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.listVisibility.value, equalTo(View.VISIBLE))

        Mockito.verify(mockConnectionsRepository).getAllActiveConnections()
        Mockito.verify(mockConnectionsRepository).getAllConnections()
        Mockito.verify(mockApiManagerV1).getConsents(
            connectionsAndKeys = listOf(richConnection1),
            resultCallback = interactor
        )
        verifyNoInteractions(mockApiManagerV2)
    }

    @Test
    @Throws(Exception::class)
    fun refreshConsentsTestCase1() {
        //given
        given(mockConnectionsRepository.getAllActiveConnections()).willReturn(emptyList())
        viewModel.onStart()
        Mockito.clearInvocations(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)

        //when
        viewModel.refreshConsents()

        //then
        verifyNoInteractions(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)
    }

    @Test
    @Throws(Exception::class)
    fun refreshConsentsTestCase2() {
        //given
        viewModel.onStart()
        Mockito.clearInvocations(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)

        //when
        viewModel.refreshConsents()

        //then
        Mockito.verify(mockApiManagerV1).getConsents(connectionsAndKeys = listOf(richConnection1), resultCallback = interactor)
        verifyNoInteractions(mockApiManagerV2)
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
        viewModel.onStart()
        Mockito.clearInvocations(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)

        //when
        viewModel.processDecryptedConsentsResult(result = consentData)

        //then
        assertThat(
            viewModel.listItems.value,
            equalTo(
                listOf(
                    ConnectionItem(
                        guid = "guid1",
                        connectionId = "1",
                        name = "Demobank1",
                        statusDescription = "Grant access to location data",
                        statusDescriptionColorRes = R.color.yellow,
                        logoUrl = "",
                        consentsDescription = "1 consent\u30FB",
                        isActive = true,
                        isChecked = false,
                        apiVersion = API_V1_VERSION,
                        email = "example@example.com",
                        locationPermissionRequired = true
                    ),
                    ConnectionItem(
                        guid = "guid2",
                        connectionId = "2",
                        name = "Demobank2",
                        statusDescription = "Linked on 1 January 1970",
                        statusDescriptionColorRes = R.color.dark_60_and_grey_100,
                        logoUrl = "",
                        consentsDescription = "",
                        isActive = true,
                        isChecked = false,
                        apiVersion = API_V2_VERSION,
                        email = "example@example.com",
                        locationPermissionRequired = false
                    ),
                    ConnectionItem(
                        guid = "guid3",
                        connectionId = "3",
                        name = "Demobank3",
                        statusDescription = "Inactive. Please reconnect.",
                        statusDescriptionColorRes = R.color.red_and_red_light,
                        logoUrl = "",
                        consentsDescription = "",
                        isActive = false,
                        isChecked = false,
                        apiVersion = API_V1_VERSION,
                        email = "example@example.com",
                        locationPermissionRequired = false
                    )
                )
            )
        )
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTest() {
        viewModel.onViewClick(-1)

        Assert.assertNull(viewModel.onQrScanClickEvent.value)

        viewModel.onViewClick(R.id.actionView)

        Assert.assertNotNull(viewModel.onQrScanClickEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTestCase1() {
        //given list of items, list of consents and index of active item
        viewModel.onStart()
        viewModel.processDecryptedConsentsResult(result = consentData)
        Mockito.clearInvocations(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)
        val activeItemIndex = 0

        assertNull(viewModel.onListItemClickEvent.value)

        //when
        viewModel.onListItemClick(activeItemIndex)

        //then
        assertThat(
            viewModel.onListItemClickEvent.value,
            equalTo(
                ViewModelEvent(
                    MenuData(
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
                                id = ConnectionsListViewModel.PopupMenuItem.LOCATION.ordinal,
                                iconRes = R.drawable.ic_view_location_24dp,
                                textRes = R.string.actions_view_location
                            ),
                            MenuItemData(
                                id = ConnectionsListViewModel.PopupMenuItem.DELETE.ordinal,
                                iconRes = R.drawable.ic_menu_delete_24dp,
                                textRes = R.string.actions_delete
                            )
                        )
                    )
                )
            )
        )
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTestCase2() {
        //given list of items, empty list of consents and index of inactive item
        viewModel.onStart()
        Mockito.clearInvocations(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)
        val inactiveItemIndex = 2

        assertNull(viewModel.onListItemClickEvent.value)

        //when
        viewModel.onListItemClick(inactiveItemIndex)

        //then
        val menuData: MenuData = viewModel.onListItemClickEvent.value!!.peekContent()
        val expectedMenuData = MenuData(
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
        )
        assertThat(menuData.items.size, equalTo(4))
        assertThat(menuData, equalTo(expectedMenuData))
    }

    @Test
    @Throws(Exception::class)
    fun onMenuItemClickTestVase1() {
        //given itemId RECONNECT
        viewModel.onStart()
        Mockito.clearInvocations(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)
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
        viewModel.onStart()
        Mockito.clearInvocations(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)
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
        viewModel.onStart()
        Mockito.clearInvocations(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)
        val activeItemIndex = 1
        val itemId = ConnectionsListViewModel.PopupMenuItem.SUPPORT.ordinal

        //when
        viewModel.onMenuItemClick(menuId = activeItemIndex, itemId = itemId)

        //then
        assertThat(
            viewModel.onSupportClickEvent.value!!.peekContent(),
            equalTo("example@example.com")
        )
    }

    @Test
    @Throws(Exception::class)
    fun onMenuItemClickTestCase5() {
        //given itemId DELETE
        viewModel.onStart()
        Mockito.clearInvocations(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)
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
    fun onItemNameChangedTestCase1() {
        viewModel.onItemNameChanged(data = Bundle().apply { putString(KEY_NAME, "new name") })

        Mockito.never()

        viewModel.onItemNameChanged(
            data = Bundle().apply {
                putString(KEY_NAME, "new name")
                guid = "guidX"
            }
        )

        Mockito.never()

        viewModel.onItemNameChanged(
            data = Bundle().apply {
                putString(KEY_NAME, "new name")
                guid = "guid2"
            }
        )

        Mockito.never()

        viewModel.onItemNameChanged(
            data = Bundle().apply {
                putString(KEY_NAME, "")
                guid = "guid2"
            }
        )

        Mockito.never()

        viewModel.onItemNameChanged(
            data = Bundle().apply {
                putString(KEY_NAME, "Demobank2")
                guid = "guid2"
            }
        )

        Mockito.never()
    }

    /**
     * test onItemNameChanged,
     * GUID != null.
     *
     * User entered new Connection name
     */
    @Test
    @Throws(Exception::class)
    fun onItemNameChangedTestCase2() {
        //given
        viewModel.onStart()
        Mockito.clearInvocations(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)

        //when
        viewModel.onItemNameChanged(
            data = Bundle().apply {
                putString(KEY_NAME, "new name")
                guid = "guid2"
            }
        )

        //then
        Assert.assertNotNull(viewModel.updateListItemEvent.value)
        Mockito.verify(mockConnectionsRepository).updateNameAndSave(connection = connection2, newName = "new name")
    }

    @Test
    @Throws(Exception::class)
    fun onDeleteItemResultTestCase1() {
        //when
        viewModel.onStart()
        Mockito.clearInvocations(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)
        Mockito.doReturn(true).`when`(mockConnectionsRepository).deleteConnection("guid1")

        //when
        viewModel.deleteItem(guid = "guid1")

        //then
        Mockito.verify(mockApiManagerV1).revokeConnections(listOf(richConnection1), null)
        Mockito.verify(mockConnectionsRepository).deleteConnection("guid1")
        Mockito.verify(mockKeyStoreManager).deleteKeyPairIfExist("guid1")
    }

    @Test
    @Throws(Exception::class)
    fun onDeleteItemResultTestCase2() {
        //when
        viewModel.onStart()
        Mockito.clearInvocations(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)
        Mockito.doReturn(true).`when`(mockConnectionsRepository).deleteConnection("guid2")

        //when
        viewModel.deleteItem(guid = "guid2")

        //then
        Mockito.verify(mockApiManagerV2).revokeConnections(listOf(richConnection2), null)
        Mockito.verify(mockConnectionsRepository).deleteConnection("guid2")
        Mockito.verify(mockKeyStoreManager).deleteKeyPairIfExist("guid2")
    }

    @Test
    @Throws(Exception::class)
    fun onDeleteItemResultTestCase3() {
        //given
        viewModel.onStart()
        Mockito.clearInvocations(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)
        Mockito.doReturn(true).`when`(mockConnectionsRepository).deleteConnection("guid3")

        //when
        viewModel.deleteItem(guid = "guid3")

        //then
        Mockito.verifyNoInteractions(mockApiManagerV1, mockApiManagerV2)
        Mockito.verify(mockConnectionsRepository).deleteConnection("guid3")
        Mockito.verify(mockKeyStoreManager).deleteKeyPairIfExist("guid3")
    }

    @Test
    @Throws(Exception::class)
    fun menuItemTest() {
        assertThat(
            ConnectionsListViewModel.PopupMenuItem.values(),
            equalTo(
                arrayOf(
                    ConnectionsListViewModel.PopupMenuItem.RECONNECT,
                    ConnectionsListViewModel.PopupMenuItem.RENAME,
                    ConnectionsListViewModel.PopupMenuItem.SUPPORT,
                    ConnectionsListViewModel.PopupMenuItem.CONSENTS,
                    ConnectionsListViewModel.PopupMenuItem.DELETE,
                    ConnectionsListViewModel.PopupMenuItem.LOCATION
                )
            )
        )
    }

    @Test
    @Throws(Exception::class)
    fun onDialogActionClickCase1() {
        //when
        viewModel.onDialogActionClick(
            dialogActionId = DialogInterface.BUTTON_NEGATIVE,
            actionResId = R.string.actions_proceed
        )

        //then
        assertNull(viewModel.onAskPermissionsEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onDialogActionClickCase2() {
        //when
        viewModel.onDialogActionClick(
            dialogActionId = DialogInterface.BUTTON_POSITIVE,
            actionResId = R.string.actions_proceed
        )

        //then
        assertNotNull(viewModel.onAskPermissionsEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onDialogActionClickCase3() {
        //when
        viewModel.onDialogActionClick(
            dialogActionId = DialogInterface.BUTTON_NEGATIVE,
            actionResId = R.string.actions_go_to_settings
        )

        //then
        assertNull(viewModel.onGoToSettingsEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onDialogActionClickCase4() {
        //when
        viewModel.onDialogActionClick(
            dialogActionId = DialogInterface.BUTTON_POSITIVE,
            actionResId = R.string.actions_go_to_settings
        )

        //then
        assertNotNull(viewModel.onGoToSettingsEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onDialogActionClickCase5() {
        //given
        viewModel.onStart()
        Mockito.clearInvocations(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)
        val isConnected = true
        viewModel.onNetworkConnectionChanged(isConnected = isConnected)


        //when
        viewModel.onDialogActionClick(
            dialogActionId = DialogInterface.BUTTON_NEGATIVE,
            actionResId = R.string.actions_retry,
            guid = "guid1"
        )

        //then
        assertNull(viewModel.onDeleteClickEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onDialogActionClickCase6() {
        //given
        viewModel.onStart()
        Mockito.clearInvocations(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)
        val isConnected = true
        viewModel.onNetworkConnectionChanged(isConnected = isConnected)

        //when
        viewModel.onDialogActionClick(
            dialogActionId = DialogInterface.BUTTON_POSITIVE,
            actionResId = R.string.actions_retry,
            guid = "guid1"
        )

        //then
        Mockito.verify(mockApiManagerV1).revokeConnections(listOf(richConnection1), null)
        Mockito.verify(mockConnectionsRepository).deleteConnection("guid1")
        Mockito.verify(mockKeyStoreManager).deleteKeyPairIfExist("guid1")
    }

    @Test
    @Throws(Exception::class)
    fun onDialogActionClickCase7() {
        //given
        viewModel.onStart()
        Mockito.clearInvocations(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)
        val isConnected = false
        viewModel.onNetworkConnectionChanged(isConnected = isConnected)


        //when
        viewModel.onDialogActionClick(
            dialogActionId = DialogInterface.BUTTON_NEGATIVE,
            actionResId = R.string.actions_retry,
            guid = "guid1"
        )

        //then
        assertNull(viewModel.onDeleteClickEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onDialogActionClickCase8() {
        //given
        viewModel.onStart()
        Mockito.clearInvocations(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)
        val isConnected = false
        viewModel.onNetworkConnectionChanged(isConnected = isConnected)

        //when
        viewModel.onDialogActionClick(
            dialogActionId = DialogInterface.BUTTON_POSITIVE,
            actionResId = R.string.actions_retry,
            guid = "guid1"
        )

        //then
        assertNull(viewModel.onDeleteClickEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onStartTestCase() {
        //given
        val lifecycle = LifecycleRegistry(mock(LifecycleOwner::class.java))
        lifecycle.addObserver(viewModel)

        //when
        lifecycle.currentState = Lifecycle.State.STARTED

        //then
        Mockito.verify(mockConnectivityReceiver).addNetworkStateChangeListener(viewModel)
    }

    @Test
    @Throws(Exception::class)
    fun onStopTestCase() {
        //given
        val lifecycle = LifecycleRegistry(mock(LifecycleOwner::class.java))
        lifecycle.addObserver(viewModel)

        //when
        lifecycle.currentState = Lifecycle.State.RESUMED
        lifecycle.currentState =
            Lifecycle.State.CREATED //move to stop state (possible only after RESUMED state)

        //then
        Mockito.verify(mockConnectivityReceiver).removeNetworkStateChangeListener(viewModel)
    }
}
