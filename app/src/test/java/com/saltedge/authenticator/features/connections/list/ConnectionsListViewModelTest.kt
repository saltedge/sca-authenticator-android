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
import com.saltedge.authenticator.TestFactory
import com.saltedge.authenticator.app.CAMERA_PERMISSION_REQUEST_CODE
import com.saltedge.authenticator.app.ConnectivityReceiverAbs
import com.saltedge.authenticator.app.LOCATION_PERMISSION_REQUEST_CODE
import com.saltedge.authenticator.app.guid
import com.saltedge.authenticator.core.api.KEY_NAME
import com.saltedge.authenticator.core.api.model.ConsentData
import com.saltedge.authenticator.core.api.model.ConsentSharedData
import com.saltedge.authenticator.core.model.ConnectionStatus
import com.saltedge.authenticator.features.connections.common.ConnectionItem
import com.saltedge.authenticator.features.connections.list.menu.ConnectionsListMenuItemType
import com.saltedge.authenticator.features.connections.list.menu.MenuData
import com.saltedge.authenticator.features.menu.MenuItemData
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.location.DeviceLocationManagerAbs
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
import java.lang.ref.WeakReference

@RunWith(RobolectricTestRunner::class)
class ConnectionsListViewModelTest : ViewModelTest() {

    private lateinit var viewModel: ConnectionsListViewModel

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val mockInteractor = mock(ConnectionsListInteractorAbs::class.java)
    private val mockLocationManager = mock(DeviceLocationManagerAbs::class.java)
    private val mockConnectivityReceiver = mock(ConnectivityReceiverAbs::class.java)

    @Before
    fun setUp() {
        viewModel = ConnectionsListViewModel(
            weakContext = WeakReference(context),
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
        Mockito.verify(mockInteractor).updateConnections()
        Mockito.verify(mockInteractor).updateConsents()
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
        viewModel.onDatasetChanged(TestFactory.allConnections, TestFactory.allConsents)
        val newName = "new name"
        val guid = TestFactory.connection2.guid
        given(mockInteractor.updateNameAndSave(connectionGuid = guid, newConnectionName = newName)).willReturn(true)

        //when
        viewModel.onItemNameChanged(data = Bundle().apply { this.putString(KEY_NAME, newName); this.guid = guid })

        //then
        assertNotNull(viewModel.updateListItemEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onItemNameChangedTestCase2() {
        //given
        viewModel.onDatasetChanged(TestFactory.allConnections, TestFactory.allConsents)
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
        viewModel.onDatasetChanged(TestFactory.allConnections, TestFactory.allConsents)
        val newName = ""
        val guid = TestFactory.connection2.guid

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
        viewModel.onDatasetChanged(TestFactory.allConnections, TestFactory.allConsents)
        val newName = TestFactory.connection2.name
        val guid = TestFactory.connection2.guid

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
        viewModel.onDatasetChanged(TestFactory.allConnections, TestFactory.allConsents)
        val newName = "new name"
        val guid = TestFactory.connection2.guid
        given(mockInteractor.updateNameAndSave(connectionGuid = guid, newConnectionName = newName)).willReturn(false)

        //when
        viewModel.onItemNameChanged(data = Bundle().apply { this.putString(KEY_NAME, newName); this.guid = guid })

        //then
        assertNull(viewModel.updateListItemEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun deleteItemTestCase1() {
        //when
        viewModel.onDatasetChanged(TestFactory.allConnections, TestFactory.allConsents)

        //when
        viewModel.deleteItem(guid = TestFactory.connection1.guid)

        //then
        Mockito.verify(mockInteractor).revokeConnection(TestFactory.connection1.guid)
    }

    @Test
    @Throws(Exception::class)
    fun deleteItemTestCase2() {
        //when
        viewModel.onDatasetChanged(TestFactory.allConnections, TestFactory.allConsents)

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
        viewModel.onDatasetChanged(TestFactory.allConnections, TestFactory.allConsents)
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
                                id = ConnectionsListMenuItemType.RENAME.ordinal,
                                iconRes = R.drawable.ic_menu_edit_24dp,
                                textRes = R.string.actions_rename
                            ),
                            MenuItemData(
                                id = ConnectionsListMenuItemType.SUPPORT.ordinal,
                                iconRes = R.drawable.ic_contact_support_24dp,
                                textRes = R.string.actions_contact_support
                            ),
                            MenuItemData(
                                id = ConnectionsListMenuItemType.CONSENTS.ordinal,
                                iconRes = R.drawable.ic_view_consents_24dp,
                                textRes = R.string.actions_view_consents
                            ),
                            MenuItemData(
                                id = ConnectionsListMenuItemType.LOCATION.ordinal,
                                iconRes = R.drawable.ic_view_location_24dp,
                                textRes = R.string.actions_view_location
                            ),
                            MenuItemData(
                                id = ConnectionsListMenuItemType.DELETE.ordinal,
                                iconRes = R.drawable.ic_menu_delete_24dp,
                                textRes = R.string.actions_delete
                            ),
                            MenuItemData(
                                id = ConnectionsListMenuItemType.INFO.ordinal,
                                iconRes = R.drawable.ic_menu_id,
                                text = "ID: 1",
                                isActive = false
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
        viewModel.onDatasetChanged(TestFactory.allConnections, TestFactory.allConsents)
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
                    id = ConnectionsListMenuItemType.RECONNECT.ordinal,
                    iconRes = R.drawable.ic_menu_reconnect_24dp,
                    textRes = R.string.actions_reconnect
                ),
                MenuItemData(
                    id = ConnectionsListMenuItemType.RENAME.ordinal,
                    iconRes = R.drawable.ic_menu_edit_24dp,
                    textRes = R.string.actions_rename
                ),
                MenuItemData(
                    id = ConnectionsListMenuItemType.SUPPORT.ordinal,
                    iconRes = R.drawable.ic_contact_support_24dp,
                    textRes = R.string.actions_contact_support
                ),
                MenuItemData(
                    id = ConnectionsListMenuItemType.DELETE.ordinal,
                    iconRes = R.drawable.ic_menu_remove_24dp,
                    textRes = R.string.actions_remove
                ),
                MenuItemData(
                    id = ConnectionsListMenuItemType.INFO.ordinal,
                    iconRes = R.drawable.ic_menu_id,
                    text = "ID: 3",
                    isActive = false
                )
            )
        )
        assertThat(menuData.items.size, equalTo(5))
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
            ConnectionsListMenuItemType.values(),
            equalTo(
                arrayOf(
                    ConnectionsListMenuItemType.RECONNECT,
                    ConnectionsListMenuItemType.RENAME,
                    ConnectionsListMenuItemType.SUPPORT,
                    ConnectionsListMenuItemType.CONSENTS,
                    ConnectionsListMenuItemType.DELETE,
                    ConnectionsListMenuItemType.LOCATION,
                    ConnectionsListMenuItemType.INFO
                )
            )
        )
    }

    @Test
    @Throws(Exception::class)
    fun onMenuItemClickTestCase1() {
        //given
        viewModel.onDatasetChanged(TestFactory.allConnections, TestFactory.allConsents)
        val activeItemIndex = 1
        val itemId = ConnectionsListMenuItemType.RECONNECT.ordinal

        //when
        viewModel.onMenuItemClick(menuId = activeItemIndex, itemId = itemId)

        //then
        assertThat(viewModel.onReconnectClickEvent.value, equalTo(ViewModelEvent("guid2")))
    }

    @Test
    @Throws(Exception::class)
    fun onMenuItemClickTestCase2() {
        //given
        viewModel.onDatasetChanged(TestFactory.allConnections, TestFactory.allConsents)
        val activeItemIndex = 1
        val itemId = ConnectionsListMenuItemType.RENAME.ordinal

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
        viewModel.onDatasetChanged(TestFactory.allConnections, TestFactory.allConsents)
        val activeItemIndex = 1
        val itemId = ConnectionsListMenuItemType.SUPPORT.ordinal

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
        viewModel.onDatasetChanged(TestFactory.allConnections, TestFactory.allConsents)
        val activeItemIndex = 1
        val itemId = ConnectionsListMenuItemType.LOCATION.ordinal

        //when
        viewModel.onMenuItemClick(menuId = activeItemIndex, itemId = itemId)

        //then
        assertNotNull(viewModel.onAccessToLocationClickEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onMenuItemClickTestCase6_noConnection() {
        //given
        given(mockConnectivityReceiver.hasNetworkConnection).willReturn(false)
        viewModel.onDatasetChanged(TestFactory.allConnections, TestFactory.allConsents)
        val activeItemIndex = 1
        val itemId = ConnectionsListMenuItemType.DELETE.ordinal

        //when
        viewModel.onMenuItemClick(menuId = activeItemIndex, itemId = itemId)

        //then
        assertThat(viewModel.onShowNoInternetConnectionDialogEvent.value!!.peekContent(), equalTo("guid2"))
        Mockito.verifyNoMoreInteractions(mockInteractor)
    }

    @Test
    @Throws(Exception::class)
    fun onMenuItemClickTestCase6_active() {
        //given
        given(mockConnectivityReceiver.hasNetworkConnection).willReturn(true)
        viewModel.onDatasetChanged(TestFactory.allConnections, TestFactory.allConsents)
        val activeItemIndex = 1
        val itemId = ConnectionsListMenuItemType.DELETE.ordinal

        //when
        viewModel.onMenuItemClick(menuId = activeItemIndex, itemId = itemId)

        //then
        assertThat(viewModel.onDeleteClickEvent.value!!.peekContent(), equalTo("guid2"))
        Mockito.verifyNoMoreInteractions(mockInteractor)
    }

    @Test
    @Throws(Exception::class)
    fun onMenuItemClickTestCase6_inactive() {
        //given
        given(mockConnectivityReceiver.hasNetworkConnection).willReturn(true)
        viewModel.onDatasetChanged(TestFactory.allConnections, TestFactory.allConsents)
        val activeItemIndex = 2
        val itemId = ConnectionsListMenuItemType.DELETE.ordinal

        //when
        viewModel.onMenuItemClick(menuId = activeItemIndex, itemId = itemId)

        //then
        Mockito.verify(mockInteractor).revokeConnection(TestFactory.connection3Inactive.guid)
    }

    @Test
    @Throws(Exception::class)
    fun onConnectionsDataChangedTestCase1() {
        //given
        Assert.assertTrue(viewModel.listItems.value!!.isEmpty())

        //when
        viewModel.onDatasetChanged(TestFactory.allConnections, TestFactory.allConsents)

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
        viewModel.onDatasetChanged(emptyList(), TestFactory.allConsents)

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
        viewModel.onDatasetChanged(emptyList(), TestFactory.allConsents)

        //then
        Assert.assertTrue(viewModel.listItems.value!!.isEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun onConsentsDataChangedTestCase2() {
        //given
        viewModel.onDatasetChanged(TestFactory.allConnections, emptyList())

        //when
        viewModel.onDatasetChanged(TestFactory.allConnections, TestFactory.allConsents)

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
                        logoUrl = "https://www.fentury.com/",
                        consentsCount = 3,
                        isActive = true,
                        isChecked = false,
                        apiVersion = API_V1_VERSION,
                        email = "example@example.com",
                        shouldRequestLocationPermission = true
                    ),
                    ConnectionItem(
                        guid = "guid2",
                        connectionId = "2",
                        name = "Demobank2",
                        statusDescription = "Linked on 1 January 1970",
                        statusDescriptionColorRes = R.color.dark_60_and_grey_100,
                        logoUrl = "https://www.fentury.com/",
                        consentsCount = 1,
                        isActive = true,
                        isChecked = false,
                        apiVersion = API_V2_VERSION,
                        email = "example@example.com",
                        shouldRequestLocationPermission = false
                    ),
                    ConnectionItem(
                        guid = "guid3",
                        connectionId = "3",
                        name = "Demobank3",
                        statusDescription = "Inactive. Please reconnect.",
                        statusDescriptionColorRes = R.color.red_and_red_light,
                        logoUrl = "https://www.fentury.com/",
                        consentsCount = 0,
                        isActive = false,
                        isChecked = false,
                        apiVersion = API_V1_VERSION,
                        email = "example@example.com",
                        shouldRequestLocationPermission = false
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
        viewModel.onDatasetChanged(TestFactory.allConnections, TestFactory.allConsents)
        given(mockConnectivityReceiver.hasNetworkConnection).willReturn(true)


        //when
        viewModel.onDialogActionClick(
            dialogActionId = DialogInterface.BUTTON_NEGATIVE,
            actionResId = R.string.actions_retry,
            guid = TestFactory.connection1.guid
        )

        //then
        assertNull(viewModel.onDeleteClickEvent.value)
        Mockito.verifyNoInteractions(mockInteractor)
    }

    @Test
    @Throws(Exception::class)
    fun onDialogActionClickCase6() {
        //given
        viewModel.onDatasetChanged(TestFactory.allConnections, TestFactory.allConsents)
        given(mockConnectivityReceiver.hasNetworkConnection).willReturn(true)

        //when
        viewModel.onDialogActionClick(
            dialogActionId = DialogInterface.BUTTON_POSITIVE,
            actionResId = R.string.actions_retry,
            guid = TestFactory.connection1.guid
        )

        //then
        Mockito.verify(mockInteractor).revokeConnection(TestFactory.connection1.guid)
    }

    @Test
    @Throws(Exception::class)
    fun onDialogActionClickCase7() {
        //given
        viewModel.onDatasetChanged(TestFactory.allConnections, TestFactory.allConsents)
        given(mockConnectivityReceiver.hasNetworkConnection).willReturn(false)


        //when
        viewModel.onDialogActionClick(
            dialogActionId = DialogInterface.BUTTON_NEGATIVE,
            actionResId = R.string.actions_retry,
            guid = TestFactory.connection1.guid
        )

        //then
        assertNull(viewModel.onDeleteClickEvent.value)
        Mockito.verifyNoInteractions(mockInteractor)
    }

    @Test
    @Throws(Exception::class)
    fun onDialogActionClickCase8() {
        //given
        viewModel.onDatasetChanged(TestFactory.allConnections, TestFactory.allConsents)
        given(mockConnectivityReceiver.hasNetworkConnection).willReturn(false)

        //when
        viewModel.onDialogActionClick(
            dialogActionId = DialogInterface.BUTTON_POSITIVE,
            actionResId = R.string.actions_retry,
            guid = TestFactory.connection1.guid
        )

        //then
        assertNull(viewModel.onDeleteClickEvent.value)
    }
}
