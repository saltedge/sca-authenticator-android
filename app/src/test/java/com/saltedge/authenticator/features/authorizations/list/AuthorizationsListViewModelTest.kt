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
package com.saltedge.authenticator.features.authorizations.list

import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.saltedge.android.test_tools.CoroutineViewModelTest
import com.saltedge.authenticator.R
import com.saltedge.authenticator.TestAppTools
import com.saltedge.authenticator.app.AppTools
import com.saltedge.authenticator.app.ConnectivityReceiverAbs
import com.saltedge.authenticator.app.KEY_OPTION_ID
import com.saltedge.authenticator.app.LOCATION_PERMISSION_REQUEST_CODE
import com.saltedge.authenticator.core.api.KEY_ID
import com.saltedge.authenticator.core.api.model.DescriptionData
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.api.model.error.createRequestError
import com.saltedge.authenticator.features.authorizations.common.AuthorizationItemViewModel
import com.saltedge.authenticator.features.authorizations.common.AuthorizationStatus
import com.saltedge.authenticator.features.menu.BottomMenuDialog
import com.saltedge.authenticator.features.menu.MenuItemData
import com.saltedge.authenticator.interfaces.MenuItem
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.location.DeviceLocationManagerAbs
import com.saltedge.authenticator.sdk.constants.API_V1_VERSION
import com.saltedge.authenticator.sdk.v2.api.API_V2_VERSION
import com.saltedge.authenticator.widget.security.ActivityUnlockType
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Assert
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.*
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class AuthorizationsListViewModelTest : CoroutineViewModelTest() {

    private lateinit var viewModel: AuthorizationsListViewModel
    private val mockInteractorV1 = mock<AuthorizationsListInteractorAbs>()
    private val mockInteractorV2 = mock<AuthorizationsListInteractorAbs>()
    private val mockConnectivityReceiver = mock<ConnectivityReceiverAbs>()
    private val mockLocationManager = mock<DeviceLocationManagerAbs>()
    private val items: List<AuthorizationItemViewModel> = listOf(createItem(id = 1), createItem(id = 2))

    @Before
    override fun setUp() {
        super.setUp()
        AppTools.lastUnlockType = ActivityUnlockType.BIOMETRICS
        given(mockLocationManager.locationDescription).willReturn("GEO:52.506931;13.144558")
        viewModel = AuthorizationsListViewModel(
            appContext = TestAppTools.applicationContext,
            interactorV1 = mockInteractorV1,
            interactorV2 = mockInteractorV2,
            connectivityReceiver = mockConnectivityReceiver,
            locationManager = mockLocationManager
        )
        Mockito.clearInvocations(mockInteractorV1, mockInteractorV2)
    }

    @Test
    @Throws(Exception::class)
    fun onFragmentStartTest() {
        //given
        val lifecycle = LifecycleRegistry(mock<LifecycleOwner>())
        viewModel.bindLifecycleObserver(lifecycle)

        //when
        lifecycle.currentState = Lifecycle.State.STARTED

        //then
        verify(mockConnectivityReceiver).addNetworkStateChangeListener(viewModel)
    }

    @Test
    @Throws(Exception::class)
    fun onFragmentStopTest() {
        //given
        val lifecycle = LifecycleRegistry(mock<LifecycleOwner>())
        viewModel.bindLifecycleObserver(lifecycle)

        //when
        lifecycle.currentState = Lifecycle.State.RESUMED
        lifecycle.currentState = Lifecycle.State.CREATED //move to stop state (possible only after RESUMED state)

        //then
        verify(mockConnectivityReceiver).removeNetworkStateChangeListener(viewModel)
    }

    @Test
    @Throws(Exception::class)
    fun onFragmentResumeCase1() {
        val lifecycle = LifecycleRegistry(mock<LifecycleOwner>())
        viewModel.bindLifecycleObserver(lifecycle)

        //when
        lifecycle.currentState = Lifecycle.State.RESUMED

        //then
        assertThat(viewModel.emptyViewVisibility.value, equalTo(View.VISIBLE))
        assertThat(viewModel.listVisibility.value, equalTo(View.GONE))
        assertThat(
            viewModel.emptyViewActionText.value,
            equalTo(R.string.actions_scan_qr)
        )
        assertThat(
            viewModel.emptyViewTitleText.value,
            equalTo(R.string.authorizations_empty_title)
        )
        assertThat(
            viewModel.emptyViewDescriptionText.value,
            equalTo(R.string.authorizations_empty_description)
        )
    }

    @Test
    @Throws(Exception::class)
    fun onFragmentResumeCase2() {
        //given onResume event, with connection and items
        viewModel.listItems.value = items
        val lifecycle = LifecycleRegistry(mock<LifecycleOwner>())
        viewModel.bindLifecycleObserver(lifecycle)

        //when
        lifecycle.currentState = Lifecycle.State.RESUMED

        //then
        assertThat(viewModel.emptyViewVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.listVisibility.value, equalTo(View.VISIBLE))
        assertThat(
            viewModel.emptyViewActionText.value,
            equalTo(R.string.actions_scan_qr)
        )
        assertThat(
            viewModel.emptyViewTitleText.value,
            equalTo(R.string.authorizations_empty_title)
        )
        assertThat(
            viewModel.emptyViewDescriptionText.value,
            equalTo(R.string.authorizations_empty_description)
        )
    }

    @Test
    @Throws(Exception::class)
    fun onTimeUpdateTestCase1() {
        //given list with expired item
        viewModel.listItems.postValue(listOf(items[0], items[1].copy(endTime = DateTime(0))))

        //when
        viewModel.onTimeUpdate()

        //then
        assertThat(
            viewModel.listItemsValues[1],
            equalTo(items[1].copy(endTime = DateTime(0), status = AuthorizationStatus.TIME_OUT))
        )
    }

    @Test
    @Throws(Exception::class)
    fun onTimeUpdateTestCase2() {
        //given list with expired item
        viewModel.listItems.postValue(listOf(
            items[0],
            items[1].copy(status = AuthorizationStatus.TIME_OUT).apply { destroyAt = DateTime(0) }
        ))

        //when
        viewModel.onTimeUpdate()

        //then
        assertThat(viewModel.listItemsValues, equalTo(listOf(items[0])))
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTestCase1() {
        //given invalid itemIndex
        viewModel.listItems.postValue(items)

        //when
        viewModel.onListItemClick(itemIndex = 5, itemCode = "", itemViewId = 1)

        //then
        Mockito.verifyNoMoreInteractions(mockInteractorV1, mockInteractorV2)
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTestCase2() {
        //given invalid itemViewId
        viewModel.listItems.postValue(items)

        //when
        viewModel.onListItemClick(itemIndex = 0, itemCode = "", itemViewId = R.id.titleTextView)

        //then
        Mockito.verifyNoMoreInteractions(mockInteractorV1, mockInteractorV2)
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTestCase4() {
        //given itemViewId = R.id.positiveActionView
        given(mockLocationManager.locationPermissionsGranted()).willReturn(true)
        given(mockLocationManager.isLocationProviderActive()).willReturn(true)
        given(mockInteractorV1.updateAuthorization(
            connectionID = anyString(),
            authorizationID = anyString(),
            authorizationCode = anyString(),
            confirm = anyBoolean(),
            locationDescription = anyString(),
        )).willReturn(true)
        viewModel.listItems.postValue(items)
        Assert.assertTrue(viewModel.listItemsValues.all { !it.isV2Api })

        //when
        viewModel.onListItemClick(
            itemIndex = 0,
            itemCode = "",
            itemViewId = R.id.positiveActionView
        )

        //then
        verify(mockInteractorV1).updateAuthorization(
            connectionID = items[0].connectionID,
            authorizationID = items[0].authorizationID,
            authorizationCode = items[0].authorizationCode,
            confirm = true,
            locationDescription = "GEO:52.506931;13.144558",
        )
        assertThat(viewModel.listItemUpdateEvent.value, equalTo(ViewModelEvent(0)))
        assertThat(
            viewModel.listItemsValues.first(),
            equalTo(items[0].copy(status = AuthorizationStatus.CONFIRM_PROCESSING))
        )
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTestCase5() {
        //given itemViewId = R.id.positiveActionView
        given(mockLocationManager.isLocationProviderActive()).willReturn(false)
        viewModel.listItems.postValue(items)

        //when
        viewModel.onListItemClick(
            itemIndex = 0,
            itemCode = "",
            itemViewId = R.id.positiveActionView
        )

        //then
        assertThat(viewModel.listItems.value!![0].apiVersion, equalTo(API_V1_VERSION))
        Mockito.verifyNoMoreInteractions(mockInteractorV1, mockInteractorV2)
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTestCase6() {
        //given itemViewId = R.id.negativeActionView
        given(mockLocationManager.locationPermissionsGranted()).willReturn(true)
        given(mockLocationManager.isLocationProviderActive()).willReturn(true)
        given(mockInteractorV1.updateAuthorization(
            connectionID = anyString(),
            authorizationID = anyString(),
            authorizationCode = anyString(),
            confirm = anyBoolean(),
            locationDescription = anyString(),
        )).willReturn(true)
        viewModel.listItems.postValue(items)

        //when
        viewModel.onListItemClick(
            itemIndex = 0,
            itemCode = "",
            itemViewId = R.id.negativeActionView
        )

        //then
        assertThat(viewModel.listItemUpdateEvent.value, equalTo(ViewModelEvent(0)))
        assertThat(
            viewModel.listItemsValues.first(),
            equalTo(items[0].copy(status = AuthorizationStatus.DENY_PROCESSING))
        )
        verify(mockInteractorV1).updateAuthorization(
            connectionID = items[0].connectionID,
            authorizationID = items[0].authorizationID,
            authorizationCode = items[0].authorizationCode,
            confirm = false,
            locationDescription = "GEO:52.506931;13.144558",
        )
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTestCase7() {
        //given itemViewId = R.id.negativeActionView
        given(mockLocationManager.isLocationProviderActive()).willReturn(false)
        viewModel.listItems.postValue(items)

        //when
        viewModel.onListItemClick(
            itemIndex = 0,
            itemCode = "",
            itemViewId = R.id.negativeActionView
        )

        //then
        assertThat(viewModel.listItems.value!![0].apiVersion, equalTo(API_V1_VERSION))
        Mockito.verifyNoMoreInteractions(mockInteractorV1, mockInteractorV2)
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTestCase8() {
        //given
        given(mockLocationManager.isLocationProviderActive()).willReturn(false)
        val listItems = mutableListOf<AuthorizationItemViewModel>()
        listItems.addAll(items)
        listItems[0] = items[0].copy(geolocationRequired = true)
        viewModel.listItems.value = listItems

        assertNull(viewModel.onRequestPermissionEvent.value)

        //when
        viewModel.onListItemClick(
            itemIndex = 0,
            itemCode = "",
            itemViewId = R.id.positiveActionView
        )

        //then
        assertNotNull(viewModel.onRequestPermissionEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onRequestPermissionsResultTestCase1() {
        //given
        val requestCode = LOCATION_PERMISSION_REQUEST_CODE
        val grantResults = intArrayOf(PackageManager.PERMISSION_GRANTED)

        viewModel.onRequestPermissionsResult(requestCode = requestCode, grantResults = grantResults)

        verify(mockLocationManager).startLocationUpdates()
    }

    @Test
    @Throws(Exception::class)
    fun onRequestPermissionsResultTestCase2() {
        //given
        val requestCode = -1
        val grantResults = intArrayOf(PackageManager.PERMISSION_GRANTED)

        viewModel.onRequestPermissionsResult(requestCode = requestCode, grantResults = grantResults)

        Mockito.verifyNoInteractions(mockLocationManager)
    }

    @Test
    @Throws(Exception::class)
    fun onRequestPermissionsResultTestCase3() {
        //given
        val requestCode = LOCATION_PERMISSION_REQUEST_CODE
        val grantResults = intArrayOf(-1)

        viewModel.onRequestPermissionsResult(requestCode = requestCode, grantResults = grantResults)

        Mockito.verifyNoInteractions(mockLocationManager)
    }

    @Test
    @Throws(Exception::class)
    fun onNetworkConnectionChangedTestCase1() {
        //given
        val isConnected = false

        //when
        viewModel.onNetworkConnectionChanged(isConnected = isConnected)

        //then
        assertThat(viewModel.emptyViewVisibility.value, equalTo(View.VISIBLE))
        assertThat(viewModel.listVisibility.value, equalTo(View.GONE))

        assertThat(
            viewModel.emptyViewImage.value,
            equalTo(R.drawable.ic_internet_connection)
        )
        assertNull(viewModel.emptyViewActionText.value)
        assertThat(
            viewModel.emptyViewTitleText.value,
            equalTo(R.string.authorizations_no_internet_title)
        )
        assertThat(
            viewModel.emptyViewDescriptionText.value,
            equalTo(R.string.authorizations_no_internet_description)
        )
    }

    @Test
    @Throws(Exception::class)
    fun onNetworkConnectionChangedTestCase2() {
        //given
        val isConnected = true

        //when
        viewModel.onNetworkConnectionChanged(isConnected = isConnected)

        //then
        assertThat(viewModel.emptyViewVisibility.value, equalTo(View.VISIBLE))
        assertThat(viewModel.listVisibility.value, equalTo(View.GONE))

        assertThat(
            viewModel.emptyViewImage.value,
            equalTo(R.drawable.ic_authorizations_empty)
        )
        assertThat(
            viewModel.emptyViewActionText.value,
            equalTo(R.string.actions_scan_qr)
        )
        assertThat(
            viewModel.emptyViewTitleText.value,
            equalTo(R.string.authorizations_empty_title)
        )
        assertThat(
            viewModel.emptyViewDescriptionText.value,
            equalTo(R.string.authorizations_empty_description)
        )
    }

    @Test
    @Throws(Exception::class)
    fun onEmptyViewActionClickTest() {
        //given
        //when
        viewModel.onEmptyViewActionClick()

        //then
        assertThat(viewModel.onQrScanClickEvent.value, equalTo(ViewModelEvent(Unit)))
    }

    @Test
    @Throws(Exception::class)
    fun onAppbarMenuItemClickTestCase1() {
        //given
        val menuItem = MenuItem.SCAN_QR

        //when
        viewModel.onAppbarMenuItemClick(menuItem)

        //then
        assertThat(viewModel.onQrScanClickEvent.value, equalTo(ViewModelEvent(Unit)))
        assertNull(viewModel.onMoreMenuClickEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onAppbarMenuItemClickTestCase2() {
        //given
        val menuItem = MenuItem.MORE_MENU

        //when
        viewModel.onAppbarMenuItemClick(menuItem)

        //then
        assertNull(viewModel.onQrScanClickEvent.value)

        val bundle = viewModel.onMoreMenuClickEvent.value?.peekContent()

        assertThat(bundle?.getString(KEY_ID), equalTo(""))
        assertThat(
            bundle?.getSerializable(BottomMenuDialog.KEY_ITEMS) as List<MenuItemData>,
            equalTo(
                listOf<MenuItemData>(
                    MenuItemData(
                        id = R.string.connections_feature_title,
                        iconRes = R.drawable.ic_menu_action_connections,
                        textRes = R.string.connections_feature_title
                    ),
                    MenuItemData(
                        id = R.string.settings_feature_title,
                        iconRes = R.drawable.ic_menu_action_settings,
                        textRes = R.string.settings_feature_title
                    )
                )
            )
        )
    }

    @Test
    @Throws(Exception::class)
    fun onAppbarMenuItemClickTestCase3() {
        //given
        val menuItem = MenuItem.CUSTOM_NIGHT_MODE

        //when
        viewModel.onAppbarMenuItemClick(menuItem)

        //then
        assertNull(viewModel.onQrScanClickEvent.value)
        assertNull(viewModel.onMoreMenuClickEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onActivityResultTestCase1() {
        //given
        val data = Bundle().apply {
            putInt(KEY_OPTION_ID, 0)
        }

        //when
        viewModel.onItemMenuClicked(data)

        //then
        assertNull(viewModel.onShowConnectionsListEvent.value)
        assertNull(viewModel.onShowSettingsListEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onActivityResultTestCase2() {
        //given
        val data = null

        //when
        viewModel.onItemMenuClicked(data)

        //then
        assertNull(viewModel.onShowConnectionsListEvent.value)
        assertNull(viewModel.onShowSettingsListEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onActivityResultTestCase3() {
        //given
        val data = Bundle().apply {
            putInt(KEY_OPTION_ID, R.string.connections_feature_title)
        }

        //when
        viewModel.onItemMenuClicked(data)

        //then
        assertThat(viewModel.onShowConnectionsListEvent.value, equalTo(ViewModelEvent(Unit)))
        assertNull(viewModel.onShowSettingsListEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onActivityResultTestCase4() {
        //given
        val data = Bundle().apply {
            putInt(KEY_OPTION_ID, R.string.settings_feature_title)
        }

        //when
        viewModel.onItemMenuClicked(data)

        //then
        assertNull(viewModel.onShowConnectionsListEvent.value)
        assertThat(viewModel.onShowSettingsListEvent.value, equalTo(ViewModelEvent(Unit)))
    }

    @Test
    @Throws(Exception::class)
    fun onDialogActionIdClickCase1() {
        viewModel.onPermissionRationaleDialogActionClick(dialogActionId = DialogInterface.BUTTON_NEGATIVE, actionResId = R.string.actions_proceed)

        assertNull(viewModel.onAskPermissionsEvent.value)

        viewModel.onPermissionRationaleDialogActionClick(dialogActionId = DialogInterface.BUTTON_POSITIVE, actionResId= R.string.actions_proceed)

        assertNotNull(viewModel.onAskPermissionsEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onDialogActionIdClickCase2() {
        viewModel.onPermissionRationaleDialogActionClick(dialogActionId = DialogInterface.BUTTON_NEGATIVE, actionResId = R.string.actions_go_to_settings)

        assertNull(viewModel.onGoToSystemSettingsEvent.value)

        viewModel.onPermissionRationaleDialogActionClick(dialogActionId = DialogInterface.BUTTON_POSITIVE, actionResId= R.string.actions_go_to_settings)

        assertNotNull(viewModel.onGoToSystemSettingsEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onAuthorizationsReceivedTestCase1() {
        //given Authorizations errors
        assertThat(viewModel.listItemsValues, equalTo(emptyList()))

        //when
        viewModel.onAuthorizationsReceived(data = emptyList(), newModelsApiVersion = API_V1_VERSION)

        //then
        assertThat(viewModel.listItemsValues, equalTo(emptyList()))
    }

    @Test
    @Throws(Exception::class)
    fun onAuthorizationsReceivedTestCase2() {
        //given
        viewModel.listItems.value = items
        val newV2Data = listOf(createItem(id = 12, apiVersion = API_V2_VERSION), createItem(id = 11, apiVersion = API_V2_VERSION))
        Assert.assertTrue(viewModel.listItemsValues.all { !it.isV2Api })

        //when
        viewModel.onAuthorizationsReceived(data = newV2Data, newModelsApiVersion = API_V2_VERSION)

        //then
        assertThat(viewModel.emptyViewVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.listVisibility.value, equalTo(View.VISIBLE))
        assertThat(
            viewModel.listItemsValues.map { it.authorizationID },
            equalTo(listOf("1", "2", "11", "12"))
        )
        assertThat(
            viewModel.listItemsValues,
            equalTo(items + newV2Data[1] + newV2Data[0])
        )
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenySuccessTestCase1() {
        //given success result of CONFIRM
        val listItems = listOf(
            items.first().copy(),
            items[1].copy(status = AuthorizationStatus.CONFIRM_PROCESSING)
        )
        viewModel.listItems.postValue(listItems)

        //when
        viewModel.onConfirmDenySuccess(connectionID = listItems[1].connectionID, authorizationID = listItems[1].authorizationID, newStatus = null)

        //then
        assertThat(
            viewModel.listItemsValues,
            equalTo(listOf(listItems.first(), listItems[1].copy(status = AuthorizationStatus.CONFIRMED)))
        )
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenySuccessTestCase2() {
        //given success result of DENY
        val listItems = listOf(
            items.first().copy(),
            items[1].copy(status = AuthorizationStatus.DENY_PROCESSING)
        )
        viewModel.listItems.postValue(listItems)

        //when
        viewModel.onConfirmDenySuccess(connectionID = listItems[1].connectionID, authorizationID = listItems[1].authorizationID, newStatus = null)

        //then
        assertThat(
            viewModel.listItemsValues,
            equalTo(listOf(listItems.first(), listItems[1].copy(status = AuthorizationStatus.DENIED)))
        )
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenySuccessTestCase3() {
        //given
        viewModel.listItems.postValue(items)
        Assert.assertTrue(viewModel.listItemsValues.all { it.status == AuthorizationStatus.PENDING })

        //when
        viewModel.onConfirmDenySuccess(connectionID = items[1].connectionID, authorizationID = items[1].authorizationID, newStatus = AuthorizationStatus.CONFIRMED)

        //then
        assertThat(
            viewModel.listItemsValues,
            equalTo(listOf(items.first(), items[1].copy(status = AuthorizationStatus.CONFIRMED)))
        )
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenySuccessTestCase4() {
        //given invalid params
        val listItems = listOf(
            items.first().copy(),
            items[1].copy(status = AuthorizationStatus.DENY_PROCESSING)
        )
        viewModel.listItems.postValue(listItems)

        //when
        viewModel.onConfirmDenySuccess(connectionID = listItems[1].connectionID, authorizationID = "101", newStatus = null)

        //then
        assertThat(viewModel.listItemsValues, equalTo(listItems))
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenyFailureTestCase1() {
        //given
        val listItems = listOf(
            items.first().copy(),
            items[1].copy(status = AuthorizationStatus.DENY_PROCESSING)
        )
        viewModel.listItems.postValue(listItems)
        val error = createRequestError(404)

        //when
        viewModel.onConfirmDenyFailure(error = error, connectionID = listItems[1].connectionID, authorizationID = listItems[1].authorizationID)

        //then
        assertThat(
            viewModel.listItemsValues,
            equalTo(listOf(listItems.first(), listItems[1].copy(status = AuthorizationStatus.ERROR)))
        )
        assertThat(viewModel.errorEvent.value, equalTo(ViewModelEvent(error)))
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenyFailureTestCase2() {
        //given invalid params
        val listItems = listOf(
            items.first().copy(),
            items[1].copy(status = AuthorizationStatus.DENY_PROCESSING)
        )
        viewModel.listItems.postValue(listItems)
        val error = createRequestError(404)

        //when
        viewModel.onConfirmDenyFailure(error = error, connectionID = "101", authorizationID = "102")

        //then
        assertThat(viewModel.listItemsValues, equalTo(listItems))
        assertThat(
            viewModel.errorEvent.value,
            equalTo(ViewModelEvent(ApiErrorData(errorClassName = "ApiResponseError", errorMessage = "Request Error (404)")))
        )
    }

    private fun createItem(id: Int, apiVersion: String = API_V1_VERSION): AuthorizationItemViewModel {//TODO move down
        val createdAt = DateTime.now(DateTimeZone.UTC).minusMinutes(1)
        return AuthorizationItemViewModel(
            authorizationID = "$id",
            authorizationCode = "code-$id",
            startTime = createdAt.plusMillis(id),
            endTime = createdAt.plusMinutes(3),
            validSeconds = 180,
            title = "title-$id",
            description = DescriptionData(text = "desc-$id"),
            connectionID = "$id",
            connectionLogoUrl = "logo",
            connectionName = "connection-$id",
            apiVersion = apiVersion,
            geolocationRequired = true
        )
    }
}
