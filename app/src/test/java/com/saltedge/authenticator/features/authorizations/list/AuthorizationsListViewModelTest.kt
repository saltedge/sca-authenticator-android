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

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.test.core.app.ApplicationProvider
import com.saltedge.android.test_tools.CommonTestTools
import com.saltedge.android.test_tools.encryptWithTestKey
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.AppTools
import com.saltedge.authenticator.app.ConnectivityReceiverAbs
import com.saltedge.authenticator.app.KEY_ID
import com.saltedge.authenticator.app.KEY_OPTION_ID
import com.saltedge.authenticator.features.authorizations.common.ViewMode
import com.saltedge.authenticator.features.authorizations.common.toAuthorizationItemViewModel
import com.saltedge.authenticator.features.menu.BottomMenuDialog
import com.saltedge.authenticator.features.menu.MenuItemData
import com.saltedge.authenticator.interfaces.MenuItem
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.location.DeviceLocationManagerAbs
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.api.model.EncryptedData
import com.saltedge.authenticator.sdk.api.model.authorization.AuthorizationData
import com.saltedge.authenticator.sdk.api.model.connection.ConnectionAndKey
import com.saltedge.authenticator.sdk.api.model.connection.ConnectionStatus
import com.saltedge.authenticator.sdk.api.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.api.model.error.createRequestError
import com.saltedge.authenticator.sdk.api.model.response.ConfirmDenyResponseData
import com.saltedge.authenticator.sdk.constants.ERROR_CLASS_CONNECTION_NOT_FOUND
import com.saltedge.authenticator.sdk.polling.PollingServiceAbs
import com.saltedge.authenticator.sdk.tools.crypt.CryptoToolsAbs
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManagerAbs
import com.saltedge.authenticator.tools.getErrorMessage
import com.saltedge.authenticator.widget.security.ActivityUnlockType
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@ExperimentalCoroutinesApi
class AuthorizationsListViewModelTest {

    private val testDispatcher = TestCoroutineDispatcher()
    private lateinit var viewModel: AuthorizationsListViewModel
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val mockKeyStoreManager = mock(KeyStoreManagerAbs::class.java)
    private val mockConnectionsRepository = mock(ConnectionsRepositoryAbs::class.java)
    private val mockCryptoTools = mock(CryptoToolsAbs::class.java)
    private val mockApiManager = mock(AuthenticatorApiManagerAbs::class.java)
    private val mockPollingService = mock(PollingServiceAbs::class.java)
    private val mockConnectivityReceiver = Mockito.mock(ConnectivityReceiverAbs::class.java)
    private val mockLocationManager = mock(DeviceLocationManagerAbs::class.java)
    private val mockConnection = Connection().apply {
        guid = "guid1"
        id = "1"
        code = "demobank3"
        name = "Demobank3"
        status = "${ConnectionStatus.ACTIVE}"
        accessToken = "token1"
        logoUrl = "url"
        createdAt = 200L
        updatedAt = 200L
    }
    private val mockConnectionAndKey = ConnectionAndKey(mockConnection, CommonTestTools.testPrivateKey)
    private val authorizations: List<AuthorizationData> = listOf(createAuthorization(id = 1), createAuthorization(id = 2))
    private val encryptedAuthorizations = authorizations.map { it.encryptWithTestKey() }
    private val items = authorizations.map { it.toAuthorizationItemViewModel(mockConnection) }

    @Before
    fun setUp() {
        AppTools.lastUnlockType = ActivityUnlockType.BIOMETRICS
        doReturn("GEO:52.506931;13.144558").`when`(mockLocationManager).locationDescription
        doReturn(mockPollingService).`when`(mockApiManager).createAuthorizationsPollingService()
        given(mockConnectionsRepository.getAllActiveConnections()).willReturn(listOf(mockConnection))
        given(mockKeyStoreManager.createConnectionAndKeyModel(mockConnection)).willReturn(mockConnectionAndKey)
        encryptedAuthorizations.forEachIndexed { index, encryptedData ->
            given(mockCryptoTools.decryptAuthorizationData(encryptedData, mockConnectionAndKey.key))
                .willReturn(authorizations[index])
        }

        viewModel = AuthorizationsListViewModel(
            appContext = context,
            connectionsRepository = mockConnectionsRepository,
            keyStoreManager = mockKeyStoreManager,
            cryptoTools = mockCryptoTools,
            apiManager = mockApiManager,
            connectivityReceiver = mockConnectivityReceiver,
            locationManager = mockLocationManager,
            defaultDispatcher = testDispatcher
        )
    }

    @Test
    @Throws(Exception::class)
    fun onFragmentStartTest() {
        //given
        val lifecycle = LifecycleRegistry(mock(LifecycleOwner::class.java))
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
        val lifecycle = LifecycleRegistry(mock(LifecycleOwner::class.java))
        viewModel.bindLifecycleObserver(lifecycle)

        //when
        lifecycle.currentState = Lifecycle.State.RESUMED
        lifecycle.currentState =
            Lifecycle.State.CREATED //move to stop state (possible only after RESUMED state)

        //then
        verify(mockConnectivityReceiver).removeNetworkStateChangeListener(viewModel)
    }

    @Test
    @Throws(Exception::class)
    fun onFragmentResumeCase1() {
        //given onResume event, no connection, no items
        given(mockConnectionsRepository.getAllActiveConnections()).willReturn(emptyList())
        val lifecycle = LifecycleRegistry(mock(LifecycleOwner::class.java))
        viewModel.bindLifecycleObserver(lifecycle)

        //when
        lifecycle.currentState = Lifecycle.State.RESUMED

        //then
        assertThat(viewModel.emptyViewVisibility.value, equalTo(View.VISIBLE))
        assertThat(viewModel.listVisibility.value, equalTo(View.GONE))
        assertThat(
            viewModel.emptyViewActionText.value,
            equalTo(R.string.actions_connect)
        )
        assertThat(
            viewModel.emptyViewTitleText.value,
            equalTo(R.string.connections_list_empty_title)
        )
        assertThat(
            viewModel.emptyViewDescriptionText.value,
            equalTo(R.string.connections_list_empty_description)
        )
    }

    @Test
    @Throws(Exception::class)
    fun onFragmentResumeCase2() {
        //given onResume event, with connection, no items
        val lifecycle = LifecycleRegistry(mock(LifecycleOwner::class.java))
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
    fun onFragmentResumeCase3() {
        //given onResume event, with connection and items
        viewModel.listItems.value = items
        val lifecycle = LifecycleRegistry(mock(LifecycleOwner::class.java))
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
    fun onFetchEncryptedDataResultTestCase1() {
        //given Authorizations errors
        clearInvocations(mockConnectionsRepository)

        //when
        viewModel.onFetchEncryptedDataResult(
            result = emptyList(),
            errors = listOf(createRequestError(404))
        )

        //then
        verifyNoMoreInteractions(mockConnectionsRepository)
    }

    @Test
    @Throws(Exception::class)
    fun onFetchEncryptedDataResultTestCase2() {
        //given Authorizations errors
        clearInvocations(mockConnectionsRepository)

        //when
        viewModel.onFetchEncryptedDataResult(
            result = emptyList(),
            errors = listOf(ApiErrorData(errorClassName = ERROR_CLASS_CONNECTION_NOT_FOUND))
        )

        //then
        verifyNoMoreInteractions(mockConnectionsRepository)
    }

    @Test
    @Throws(Exception::class)
    fun onFetchEncryptedDataResultTestCase3() {
        //given Authorizations errors
        clearInvocations(mockConnectionsRepository)

        //when
        viewModel.onFetchEncryptedDataResult(
            result = emptyList(),
            errors = listOf(
                ApiErrorData(
                    errorClassName = ERROR_CLASS_CONNECTION_NOT_FOUND,
                    accessToken = "token"
                )
            )
        )

        //then
        verify(mockConnectionsRepository).invalidateConnectionsByTokens(listOf("token"))
        verify(mockConnectionsRepository).getAllActiveConnections()
    }

    @Test
    @Throws(Exception::class)
    fun onFetchEncryptedDataResultTestCase4() {
        //given empty initial list and not empty result
        viewModel.listItems.value = emptyList()
        val requestResult = encryptedAuthorizations

        //when
        viewModel.onFetchEncryptedDataResult(result = requestResult, errors = emptyList())

        //then
        assertThat(viewModel.listItemsValues, equalTo(items))
        assertThat(viewModel.emptyViewVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.listVisibility.value, equalTo(View.VISIBLE))
    }

    @Test
    @Throws(Exception::class)
    fun onFetchEncryptedDataResultTestCase5() {
        //given not empty initial list and empty result
        viewModel.listItems.value = items
        val requestResult = emptyList<EncryptedData>()

        //when
        viewModel.onFetchEncryptedDataResult(result = requestResult, errors = emptyList())

        //then
        assertThat(viewModel.listItemsValues, equalTo(emptyList()))
        assertThat(viewModel.emptyViewVisibility.value, equalTo(View.VISIBLE))
        assertThat(viewModel.listVisibility.value, equalTo(View.GONE))
    }

    @Test
    @Throws(Exception::class)
    fun onFetchEncryptedDataResultTestCase6() {
        //given not empty initial list and not empty result
        viewModel.listItems.value = items
        val requestResult = encryptedAuthorizations

        //when
        viewModel.onFetchEncryptedDataResult(result = requestResult, errors = emptyList())

        //then
        assertThat(viewModel.listItemsValues, equalTo(items))
    }

    @Test
    @Throws(Exception::class)
    fun onFetchEncryptedDataResultTestCase7() {
        //given initial list with denied item and not empty result
        val initialItems = listOf(items[0], items[1].copy(viewMode = ViewMode.DENY_SUCCESS))
        viewModel.listItems.value = initialItems
        val requestResult = encryptedAuthorizations

        //when
        viewModel.onFetchEncryptedDataResult(result = requestResult, errors = emptyList())

        //then
        assertThat(viewModel.listItemsValues.size, equalTo(2))
        assertThat(viewModel.listItemsValues, equalTo(initialItems))
    }

    @Test
    @Throws(Exception::class)
    fun getCurrentConnectionsAndKeysForPollingTestCase1() {
        //given
        given(mockConnectionsRepository.getAllActiveConnections()).willReturn(emptyList<Connection>())
        viewModel.onResume()

        //when
        val result = viewModel.getCurrentConnectionsAndKeysForPolling()

        //then
        assertThat(result, `is`(nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun getCurrentConnectionsAndKeysForPollingTestCase2() {
        //given view model

        //when
        val result = viewModel.getCurrentConnectionsAndKeysForPolling()

        //then
        assertThat(result, equalTo(listOf(mockConnectionAndKey)))
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
            equalTo(items[1].copy(endTime = DateTime(0), viewMode = ViewMode.TIME_OUT))
        )
    }

    @Test
    @Throws(Exception::class)
    fun onTimeUpdateTestCase2() {
        //given list with expired item
        viewModel.listItems.postValue(listOf(
            items[0],
            items[1].copy(viewMode = ViewMode.TIME_OUT).apply { destroyAt = DateTime(0) }
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
        verify(mockApiManager).createAuthorizationsPollingService()
        verifyNoMoreInteractions(mockApiManager)
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTestCase2() {
        //given no connections
        given(mockConnectionsRepository.getAllActiveConnections()).willReturn(emptyList<Connection>())
        viewModel.listItems.postValue(items)

        //when
        viewModel.onListItemClick(itemIndex = 0, itemCode = "", itemViewId = R.id.titleTextView)

        //then
        verify(mockApiManager).createAuthorizationsPollingService()
        verifyNoMoreInteractions(mockApiManager)
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTestCase3() {
        //given invalid itemViewId
        viewModel.listItems.postValue(items)

        //when
        viewModel.onListItemClick(itemIndex = 0, itemCode = "", itemViewId = R.id.titleTextView)

        //then
        verify(mockApiManager).createAuthorizationsPollingService()
        verifyNoMoreInteractions(mockApiManager)
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTestCase4() {
        //given itemViewId = R.id.positiveActionView
        viewModel.listItems.postValue(items)

        //when
        viewModel.onListItemClick(
            itemIndex = 0,
            itemCode = "",
            itemViewId = R.id.positiveActionView
        )

        //then
        assertThat(viewModel.listItemUpdateEvent.value, equalTo(ViewModelEvent(0)))
        assertThat(
            viewModel.listItemsValues.first(),
            equalTo(items[0].copy(viewMode = ViewMode.CONFIRM_PROCESSING))
        )
        verify(mockApiManager).confirmAuthorization(
            connectionAndKey = mockConnectionAndKey,
            authorizationId = items[0].authorizationID,
            authorizationCode = items[0].authorizationCode,
            geolocation = "GEO:52.506931;13.144558",
            authorizationType = "biometrics",
            resultCallback = viewModel
        )
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTestCase5() {
        //given itemViewId = R.id.negativeActionView
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
            equalTo(items[0].copy(viewMode = ViewMode.DENY_PROCESSING))
        )
        verify(mockApiManager).denyAuthorization(
            connectionAndKey = mockConnectionAndKey,
            authorizationId = items[0].authorizationID,
            authorizationCode = items[0].authorizationCode,
            geolocation = "GEO:52.506931;13.144558",
            authorizationType = "biometrics",
            resultCallback = viewModel
        )
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenySuccessTestCase1() {
        //given success result of CONFIRM
        viewModel.listItems.postValue(
            listOf(
                items.first().copy(),
                items[1].copy(viewMode = ViewMode.CONFIRM_PROCESSING)
            )
        )
        val result = ConfirmDenyResponseData(success = true, authorizationID = "2")

        //when
        viewModel.onConfirmDenySuccess(result = result, connectionID = "1")

        //then
        assertThat(
            viewModel.listItemsValues,
            equalTo(listOf(items.first(), items[1].copy(viewMode = ViewMode.CONFIRM_SUCCESS)))
        )
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenySuccessTestCase2() {
        //given success result of DENY
        viewModel.listItems.postValue(
            listOf(
                items.first().copy(),
                items[1].copy(viewMode = ViewMode.DENY_PROCESSING)
            )
        )
        val result = ConfirmDenyResponseData(success = true, authorizationID = "2")

        //when
        viewModel.onConfirmDenySuccess(result = result, connectionID = "1")

        //then
        assertThat(
            viewModel.listItemsValues,
            equalTo(listOf(items.first(), items[1].copy(viewMode = ViewMode.DENY_SUCCESS)))
        )
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenySuccessTestCase3() {
        //given invalid params
        viewModel.listItems.postValue(
            listOf(
                items.first().copy(),
                items[1].copy(viewMode = ViewMode.DENY_PROCESSING)
            )
        )
        val result = ConfirmDenyResponseData(success = true, authorizationID = "101")

        //when
        viewModel.onConfirmDenySuccess(result = result, connectionID = "1")

        //then
        assertThat(
            viewModel.listItemsValues,
            equalTo(
                listOf(
                    items.first(),
                    items[1].copy(viewMode = ViewMode.DENY_PROCESSING)
                )
            )
        )
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenySuccessTestCase4() {
        //given invalid params
        viewModel.listItems.postValue(
            listOf(
                items.first().copy(),
                items[1].copy(viewMode = ViewMode.DENY_PROCESSING)
            )
        )
        val result = ConfirmDenyResponseData(success = true, authorizationID = "1")

        //when
        viewModel.onConfirmDenySuccess(result = result, connectionID = "101")

        //then
        assertThat(
            viewModel.listItemsValues,
            equalTo(
                listOf(
                    items.first().copy(),
                    items[1].copy(viewMode = ViewMode.DENY_PROCESSING)
                )
            )
        )
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenyFailureTestCase1() {
        //given
        viewModel.listItems.postValue(
            listOf(
                items.first().copy(),
                items[1].copy(viewMode = ViewMode.DENY_PROCESSING)
            )
        )
        val error = createRequestError(404)

        //when
        viewModel.onConfirmDenyFailure(error = error, connectionID = "1", authorizationID = "2")

        //then
        assertThat(
            viewModel.listItemsValues,
            equalTo(listOf(items.first(), items[1].copy(viewMode = ViewMode.ERROR)))
        )
        assertThat(
            viewModel.onConfirmErrorEvent.value,
            equalTo(ViewModelEvent(error.getErrorMessage(context)))
        )
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenyFailureTestCase2() {
        //given invalid params
        viewModel.listItems.postValue(
            listOf(
                items.first().copy(),
                items[1].copy(viewMode = ViewMode.DENY_PROCESSING)
            )
        )
        val error = createRequestError(404)

        //when
        viewModel.onConfirmDenyFailure(error = error, connectionID = "101", authorizationID = "102")

        //then
        assertThat(
            viewModel.listItemsValues,
            equalTo(listOf(items.first(), items[1].copy(viewMode = ViewMode.DENY_PROCESSING)))
        )
        assertThat(
            viewModel.onConfirmErrorEvent.value,
            equalTo(ViewModelEvent("Request Error (404)"))
        )
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

    private fun createAuthorization(id: Int): AuthorizationData {
        val createdAt = DateTime.now(DateTimeZone.UTC)
        return AuthorizationData(
            id = "$id",
            authorizationCode = "$id$id$id",
            createdAt = createdAt,
            expiresAt = createdAt.plusMinutes(3),
            title = "title$id",
            description = "desc$id",
            connectionId = "1"
        )
    }
}
