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
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.test.core.app.ApplicationProvider
import com.saltedge.android.test_tools.ViewModelTest
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.CAMERA_PERMISSION_REQUEST_CODE
import com.saltedge.authenticator.app.ConnectivityReceiverAbs
import com.saltedge.authenticator.app.LOCATION_PERMISSION_REQUEST_CODE
import com.saltedge.authenticator.app.guid
import com.saltedge.authenticator.core.api.KEY_NAME
import com.saltedge.authenticator.core.model.ConnectionStatus
import com.saltedge.authenticator.features.connections.common.ConnectionItem
import com.saltedge.authenticator.features.connections.list.menu.MenuData
import com.saltedge.authenticator.features.menu.MenuItemData
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.location.DeviceLocationManagerAbs
import com.saltedge.authenticator.sdk.api.model.ConsentData
import com.saltedge.authenticator.sdk.api.model.ConsentSharedData
import com.saltedge.authenticator.sdk.constants.API_V1_VERSION
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
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ConnectionsListViewModelTest : ViewModelTest() {

    private lateinit var viewModel: ConnectionsListViewModel

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val mockInteractor = mock(ConnectionsListInteractorAbs::class.java)
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
    private val allConnections = listOf(connection1, connection2, connection3Inactive)
    private val consents: List<ConsentData> = listOf(
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
        viewModel = ConnectionsListViewModel(
            appContext = context,
            interactor = mockInteractor,
            locationManager = mockLocationManager,
            connectivityReceiver = mockConnectivityReceiver
        )
        Mockito.clearInvocations(mockInteractor)
    }

    @Test
    @Throws(Exception::class)
    fun onStartTest() {
        //when
        viewModel.onStart()

        //then
        Mockito.verify(mockConnectivityReceiver).addNetworkStateChangeListener(viewModel)
        Mockito.verify(mockInteractor).updateConnections()
        Mockito.verify(mockInteractor).updateConsents()
    }

    @Test
    @Throws(Exception::class)
    fun onStopTest() {
        //when
        viewModel.onStop()

        //then
        Mockito.verify(mockConnectivityReceiver).removeNetworkStateChangeListener(viewModel)
        Mockito.verifyNoInteractions(mockInteractor)
    }

    @Test
    @Throws(Exception::class)
    fun onDestroyTest() {
        //when
        viewModel.onDestroy()

        //then
        Mockito.verify(mockInteractor).onDestroy()
    }

    @Test
    @Throws(Exception::class)
    fun refreshConsentsTestCase1() {
        //when
        viewModel.refreshConsents()

        //then
        Mockito.verify(mockInteractor).updateConsents()
    }

    @Test
    @Throws(Exception::class)
    fun onItemNameChangedTestCase1() {
        //given
        viewModel.onConnectionsDataChanged(allConnections)
        val newName = "new name"
        val guid = connection2.guid
        given(mockInteractor.updateNameAndSave(guid = guid, newConnectionName = newName)).willReturn(true)

        //when
        viewModel.onItemNameChanged(data = Bundle().apply { this.putString(KEY_NAME, newName); this.guid = guid })

        //then
        assertNotNull(viewModel.updateListItemEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onItemNameChangedTestCase2() {
        //given
        viewModel.onConnectionsDataChanged(allConnections)
        val newName = "new name"
        val guid = "guidX"

        //when
        viewModel.onItemNameChanged(data = Bundle().apply { this.putString(KEY_NAME, newName); this.guid = guid })

        //then
        assertNull(viewModel.updateListItemEvent.value)
        Mockito.never()
    }

    @Test
    @Throws(Exception::class)
    fun onItemNameChangedTestCase3() {
        //given
        viewModel.onConnectionsDataChanged(allConnections)
        val newName = ""
        val guid = connection2.guid

        //when
        viewModel.onItemNameChanged(data = Bundle().apply { this.putString(KEY_NAME, newName); this.guid = guid })

        //then
        assertNull(viewModel.updateListItemEvent.value)
        Mockito.never()
    }

    @Test
    @Throws(Exception::class)
    fun onItemNameChangedTestCase4() {
        //given
        viewModel.onConnectionsDataChanged(allConnections)
        val newName = connection2.name
        val guid = connection2.guid

        //when
        viewModel.onItemNameChanged(data = Bundle().apply { this.putString(KEY_NAME, newName); this.guid = guid })

        //then
        assertNull(viewModel.updateListItemEvent.value)
        Mockito.never()
    }

    @Test
    @Throws(Exception::class)
    fun onItemNameChangedTestCase5() {
        //given
        viewModel.onConnectionsDataChanged(allConnections)
        val newName = "new name"
        val guid = connection2.guid
        given(mockInteractor.updateNameAndSave(guid = guid, newConnectionName = newName)).willReturn(false)

        //when
        viewModel.onItemNameChanged(data = Bundle().apply { this.putString(KEY_NAME, newName); this.guid = guid })

        //then
        assertNull(viewModel.updateListItemEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun deleteItemTestCase1() {
        //when
        viewModel.onConnectionsDataChanged(allConnections)

        //when
        viewModel.deleteItem(guid = connection1.guid)

        //then
        Mockito.verify(mockInteractor).revokeConnection(connection1.guid)
    }

    @Test
    @Throws(Exception::class)
    fun deleteItemTestCase2() {
        //when
        viewModel.onConnectionsDataChanged(allConnections)

        //when
        viewModel.deleteItem(guid = "guidX")

        //then
        Mockito.verifyNoInteractions(mockInteractor)
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTestCase1() {
        //when
        viewModel.onViewClick(-1)

        //then
        assertNull(viewModel.onQrScanClickEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTestCase2() {
        //when
        viewModel.onViewClick(R.id.actionView)

        //then
        assertNotNull(viewModel.onQrScanClickEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTestCase1() {
        //given list of items, list of consents and index of active item
        viewModel.onConnectionsDataChanged(allConnections)
        viewModel.onConsentsDataChanged(consents)
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
        viewModel.onConnectionsDataChanged(allConnections)
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
    fun onRequestPermissionsResultTestCase1() {
        //given
        val requestCode = LOCATION_PERMISSION_REQUEST_CODE
        val grantResults = IntArray(1, init = { PackageManager.PERMISSION_GRANTED })

        //when
        viewModel.onRequestPermissionsResult(requestCode, grantResults)

        //then
        Mockito.verify(mockLocationManager).startLocationUpdates()
        Mockito.verify(mockInteractor).updateConnections()
    }

    @Test
    @Throws(Exception::class)
    fun onRequestPermissionsResultTestCase2() {
        //given
        val requestCode = CAMERA_PERMISSION_REQUEST_CODE
        val grantResults = IntArray(1, init = { PackageManager.PERMISSION_DENIED })

        //when
        viewModel.onRequestPermissionsResult(requestCode, grantResults)

        //then
        Mockito.verifyNoInteractions(mockLocationManager)
        Mockito.verifyNoInteractions(mockInteractor)
    }

    @Test
    @Throws(Exception::class)
    fun popupMenuItemEnumTest() {
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
    fun onMenuItemClickTestCase1() {
        //given
        viewModel.onConnectionsDataChanged(allConnections)
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
        //given
        viewModel.onConnectionsDataChanged(allConnections)
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
        //given
        viewModel.onConnectionsDataChanged(allConnections)
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
        //given
        viewModel.onConnectionsDataChanged(allConnections)
        val activeItemIndex = 1
        val itemId = ConnectionsListViewModel.PopupMenuItem.DELETE.ordinal

        //when
        viewModel.onMenuItemClick(menuId = activeItemIndex, itemId = itemId)

        //then
        assertThat(viewModel.onDeleteClickEvent.value!!.peekContent(), equalTo("guid2"))
    }

    @Test
    @Throws(Exception::class)
    fun onMenuItemClickTestCase6() {
        //given
        viewModel.onConnectionsDataChanged(allConnections)
        val activeItemIndex = 1
        val itemId = ConnectionsListViewModel.PopupMenuItem.LOCATION.ordinal

        //when
        viewModel.onMenuItemClick(menuId = activeItemIndex, itemId = itemId)

        //then
        assertNotNull(viewModel.onAccessToLocationClickEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onConnectionsDataChangedTestCase1() {
        //given
        Assert.assertTrue(viewModel.listItems.value!!.isEmpty())

        //when
        viewModel.onConnectionsDataChanged(allConnections)

        //then
        Assert.assertTrue(viewModel.listItems.value!!.isNotEmpty())
        assertThat(viewModel.emptyViewVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.listVisibility.value, equalTo(View.VISIBLE))
    }

    @Test
    @Throws(Exception::class)
    fun onConnectionsDataChangedTestCase2() {
        //given
        Assert.assertTrue(viewModel.listItems.value!!.isEmpty())

        //when
        viewModel.onConnectionsDataChanged(emptyList())

        //then
        Assert.assertTrue(viewModel.listItems.value!!.isEmpty())
        assertThat(viewModel.emptyViewVisibility.value, equalTo(View.VISIBLE))
        assertThat(viewModel.listVisibility.value, equalTo(View.GONE))
    }

    @Test
    @Throws(Exception::class)
    fun onConsentsDataChangedTestCase1() {
        //given
        Assert.assertTrue(viewModel.listItems.value!!.isEmpty())

        //when
        viewModel.onConsentsDataChanged(consents)

        //then
        Assert.assertTrue(viewModel.listItems.value!!.isEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun onConsentsDataChangedTestCase2() {
        //given
        viewModel.onConnectionsDataChanged(allConnections)

        //when
        viewModel.onConsentsDataChanged(consents)

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
        viewModel.onConnectionsDataChanged(allConnections)
        val isConnected = true
        viewModel.onNetworkConnectionChanged(isConnected = isConnected)


        //when
        viewModel.onDialogActionClick(
            dialogActionId = DialogInterface.BUTTON_NEGATIVE,
            actionResId = R.string.actions_retry,
            guid = connection1.guid
        )

        //then
        assertNull(viewModel.onDeleteClickEvent.value)
        Mockito.verifyNoInteractions(mockInteractor)
    }

    @Test
    @Throws(Exception::class)
    fun onDialogActionClickCase6() {
        //given
        viewModel.onConnectionsDataChanged(allConnections)
        val isConnected = true
        viewModel.onNetworkConnectionChanged(isConnected = isConnected)

        //when
        viewModel.onDialogActionClick(
            dialogActionId = DialogInterface.BUTTON_POSITIVE,
            actionResId = R.string.actions_retry,
            guid = connection1.guid
        )

        //then
        Mockito.verify(mockInteractor).revokeConnection(connection1.guid)
    }

    @Test
    @Throws(Exception::class)
    fun onDialogActionClickCase7() {
        //given
        viewModel.onConnectionsDataChanged(allConnections)
        val isConnected = false
        viewModel.onNetworkConnectionChanged(isConnected = isConnected)


        //when
        viewModel.onDialogActionClick(
            dialogActionId = DialogInterface.BUTTON_NEGATIVE,
            actionResId = R.string.actions_retry,
            guid = connection1.guid
        )

        //then
        assertNull(viewModel.onDeleteClickEvent.value)
        Mockito.verifyNoInteractions(mockInteractor)
    }

    @Test
    @Throws(Exception::class)
    fun onDialogActionClickCase8() {
        //given
        viewModel.onConnectionsDataChanged(allConnections)
        val isConnected = false
        viewModel.onNetworkConnectionChanged(isConnected = isConnected)

        //when
        viewModel.onDialogActionClick(
            dialogActionId = DialogInterface.BUTTON_POSITIVE,
            actionResId = R.string.actions_retry,
            guid = connection1.guid
        )

        //then
        assertNull(viewModel.onDeleteClickEvent.value)
    }
}
