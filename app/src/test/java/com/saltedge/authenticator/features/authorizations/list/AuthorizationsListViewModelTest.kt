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

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.saltedge.android.test_tools.CommonTestTools
import com.saltedge.android.test_tools.CoroutineViewModelTest
import com.saltedge.android.test_tools.encryptWithTestKey
import com.saltedge.authenticator.R
import com.saltedge.authenticator.TestAppTools
import com.saltedge.authenticator.app.AppTools
import com.saltedge.authenticator.app.ConnectivityReceiverAbs
import com.saltedge.authenticator.app.KEY_OPTION_ID
import com.saltedge.authenticator.core.api.ERROR_CLASS_CONNECTION_NOT_FOUND
import com.saltedge.authenticator.core.api.KEY_ID
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.api.model.error.createRequestError
import com.saltedge.authenticator.core.model.ConnectionStatus
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.polling.PollingServiceAbs
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.features.authorizations.common.AuthorizationItemViewModel
import com.saltedge.authenticator.features.authorizations.common.AuthorizationStatus
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
import com.saltedge.authenticator.sdk.api.model.response.ConfirmDenyResponseData
import com.saltedge.authenticator.sdk.constants.API_V1_VERSION
import com.saltedge.authenticator.sdk.tools.CryptoToolsV1Abs
import com.saltedge.authenticator.sdk.v2.ScaServiceClientAbs
import com.saltedge.authenticator.sdk.v2.api.API_V2_VERSION
import com.saltedge.authenticator.sdk.v2.tools.CryptoToolsV2Abs
import com.saltedge.authenticator.widget.security.ActivityUnlockType
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.CoreMatchers.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class AuthorizationsListViewModelTest : CoroutineViewModelTest() {

    private lateinit var viewModel: AuthorizationsListViewModel
    private lateinit var v1Interactor: AuthorizationsListInteractorV1
    private lateinit var v2Interactor: AuthorizationsListInteractorV2
    private val mockKeyStoreManager = mock(KeyManagerAbs::class.java)
    private val mockConnectionsRepository = mock(ConnectionsRepositoryAbs::class.java)
    private val mockCryptoToolsV1 = mock(CryptoToolsV1Abs::class.java)
    private val mockCryptoToolsV2 = mock(CryptoToolsV2Abs::class.java)
    private val mockApiManagerV1 = mock(AuthenticatorApiManagerAbs::class.java)
    private val mockApiManagerV2 = mock(ScaServiceClientAbs::class.java)
    private val mockPollingServiceV1 = mock(PollingServiceAbs::class.java)
    private val mockPollingServiceV2 = mock(PollingServiceAbs::class.java)
    private val mockConnectivityReceiver = mock(ConnectivityReceiverAbs::class.java)
    private val mockLocationManager = mock(DeviceLocationManagerAbs::class.java)
    private val mockConnectionV1 = Connection().apply {
        guid = "guid1"
        id = "1"
        code = "demobank3"
        name = "Demobank3"
        status = "${ConnectionStatus.ACTIVE}"
        accessToken = "token1"
        logoUrl = "url"
        createdAt = 200L
        updatedAt = 200L
        apiVersion = API_V1_VERSION
    }
    private val mockConnectionV2 = Connection().apply {
        guid = "guid2"
        id = "2"
        code = "demobank1"
        name = "Demobank1"
        status = "${ConnectionStatus.ACTIVE}"
        accessToken = "token2"
        logoUrl = "url"
        createdAt = 200L
        updatedAt = 200L
        apiVersion = API_V2_VERSION
    }
    private val mockConnectionAndKeyV1 = RichConnection(mockConnectionV1, CommonTestTools.testPrivateKey)
    private val mockConnectionAndKeyV2 = RichConnection(mockConnectionV2, CommonTestTools.testPrivateKey)
    private val authorizations: List<AuthorizationData> = listOf(createAuthorization(id = 1), createAuthorization(id = 2))
    private val encryptedAuthorizations = authorizations.map { it.encryptWithTestKey() }
    private val items: List<AuthorizationItemViewModel> = authorizations.map { it.toAuthorizationItemViewModel(mockConnectionV1)!! }

    @Before
    override fun setUp() {
        super.setUp()
        AppTools.lastUnlockType = ActivityUnlockType.BIOMETRICS
        doReturn("GEO:52.506931;13.144558").`when`(mockLocationManager).locationDescription
        doReturn(mockPollingServiceV1).`when`(mockApiManagerV1).createAuthorizationsPollingService()
        doReturn(mockPollingServiceV2).`when`(mockApiManagerV2).createAuthorizationsPollingService()
        given(mockConnectionsRepository.getAllActiveConnections(API_V1_VERSION)).willReturn(listOf(mockConnectionV1))
        given(mockConnectionsRepository.getAllActiveConnections(API_V2_VERSION)).willReturn(listOf(mockConnectionV2))
        given(mockKeyStoreManager.enrichConnection(mockConnectionV1)).willReturn(mockConnectionAndKeyV1)
        encryptedAuthorizations.forEachIndexed { index, encryptedData ->
            given(mockCryptoToolsV1.decryptAuthorizationData(encryptedData, mockConnectionAndKeyV1.private))
                .willReturn(authorizations[index])
        }
        v1Interactor = AuthorizationsListInteractorV1(
            connectionsRepository = mockConnectionsRepository,
            keyStoreManager = mockKeyStoreManager,
            cryptoTools = mockCryptoToolsV1,
            apiManager = mockApiManagerV1,
            locationManager = mockLocationManager,
            defaultDispatcher = testDispatcher
        )
        v2Interactor = AuthorizationsListInteractorV2(
            connectionsRepository = mockConnectionsRepository,
            keyStoreManager = mockKeyStoreManager,
            cryptoTools = mockCryptoToolsV2,
            apiManager = mockApiManagerV2,
            defaultDispatcher = testDispatcher
        )
        viewModel = AuthorizationsListViewModel(
            appContext = TestAppTools.applicationContext,
            interactorV1 = v1Interactor,
            interactorV2 = v2Interactor,
            connectivityReceiver = mockConnectivityReceiver,
            locationManager = mockLocationManager
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
        v1Interactor.onFetchEncryptedDataResult(
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
        v1Interactor.onFetchEncryptedDataResult(
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
        v1Interactor.onFetchEncryptedDataResult(
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
        verify(mockConnectionsRepository).getAllActiveConnections(API_V1_VERSION)
    }

    @Test
    @Throws(Exception::class)
    fun onFetchEncryptedDataResultTestCase4() {
        //given empty initial list and not empty result
        viewModel.listItems.value = emptyList()
        val requestResult = encryptedAuthorizations

        //when
        v1Interactor.onFetchEncryptedDataResult(result = requestResult, errors = emptyList())

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
        v1Interactor.onFetchEncryptedDataResult(result = requestResult, errors = emptyList())

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
        v1Interactor.onFetchEncryptedDataResult(result = requestResult, errors = emptyList())

        //then
        assertThat(viewModel.listItemsValues, equalTo(items))
    }

    @Test
    @Throws(Exception::class)
    fun onFetchEncryptedDataResultTestCase7() {
        //given initial list with denied item and not empty result
        val initialItems = listOf(items[0], items[1].copy(status = AuthorizationStatus.DENIED))
        viewModel.listItems.value = initialItems
        val requestResult = encryptedAuthorizations

        //when
        v1Interactor.onFetchEncryptedDataResult(result = requestResult, errors = emptyList())

        //then
        assertThat(viewModel.listItemsValues.size, equalTo(2))
        assertThat(viewModel.listItemsValues, equalTo(initialItems))
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
        verify(mockApiManagerV1).createAuthorizationsPollingService()
        verifyNoMoreInteractions(mockApiManagerV1)
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTestCase2() {
        //given no connections
        given(mockConnectionsRepository.getAllActiveConnections()).willReturn(emptyList())
        viewModel.listItems.postValue(items)

        //when
        viewModel.onListItemClick(itemIndex = 0, itemCode = "", itemViewId = R.id.titleTextView)

        //then
        verify(mockApiManagerV1).createAuthorizationsPollingService()
        verifyNoMoreInteractions(mockApiManagerV1)
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTestCase3() {
        //given invalid itemViewId
        viewModel.listItems.postValue(items)

        //when
        viewModel.onListItemClick(itemIndex = 0, itemCode = "", itemViewId = R.id.titleTextView)

        //then
        verify(mockApiManagerV1).createAuthorizationsPollingService()
        verifyNoMoreInteractions(mockApiManagerV1)
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
        assertThat(viewModel.listItems.value!![0].apiVersion, equalTo(API_V1_VERSION))
        assertThat(viewModel.listItemUpdateEvent.value, equalTo(ViewModelEvent(0)))
        assertThat(
            viewModel.listItemsValues.first(),
            equalTo(items[0].copy(status = AuthorizationStatus.CONFIRM_PROCESSING))
        )
        verify(mockApiManagerV1).confirmAuthorization(
            connectionAndKey = mockConnectionAndKeyV1,
            authorizationId = items[0].authorizationID,
            authorizationCode = items[0].authorizationCode,
            geolocation = "GEO:52.506931;13.144558",
            authorizationType = "biometrics",
            resultCallback = v1Interactor
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
            equalTo(items[0].copy(status = AuthorizationStatus.DENY_PROCESSING))
        )
        verify(mockApiManagerV1).denyAuthorization(
            connectionAndKey = mockConnectionAndKeyV1,
            authorizationId = items[0].authorizationID,
            authorizationCode = items[0].authorizationCode,
            geolocation = "GEO:52.506931;13.144558",
            authorizationType = "biometrics",
            resultCallback = v1Interactor
        )
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenySuccessTestCase1() {
        //given success result of CONFIRM
        viewModel.listItems.postValue(
            listOf(
                items.first().copy(),
                items[1].copy(status = AuthorizationStatus.CONFIRM_PROCESSING)
            )
        )
        val result = ConfirmDenyResponseData(success = true, authorizationID = "2")

        //when
        v1Interactor.onConfirmDenySuccess(result = result, connectionID = "1")

        //then
        assertThat(
            viewModel.listItemsValues,
            equalTo(listOf(items.first(), items[1].copy(status = AuthorizationStatus.CONFIRMED)))
        )
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenySuccessTestCase2() {
        //given success result of DENY
        viewModel.listItems.postValue(
            listOf(
                items.first().copy(),
                items[1].copy(status = AuthorizationStatus.DENY_PROCESSING)
            )
        )
        val result = ConfirmDenyResponseData(success = true, authorizationID = "2")

        //when
        v1Interactor.onConfirmDenySuccess(result = result, connectionID = "1")

        //then
        assertThat(
            viewModel.listItemsValues,
            equalTo(listOf(items.first(), items[1].copy(status = AuthorizationStatus.DENIED)))
        )
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenySuccessTestCase3() {
        //given invalid params
        viewModel.listItems.postValue(
            listOf(
                items.first().copy(),
                items[1].copy(status = AuthorizationStatus.DENY_PROCESSING)
            )
        )
        val result = ConfirmDenyResponseData(success = true, authorizationID = "101")

        //when
        v1Interactor.onConfirmDenySuccess(result = result, connectionID = "1")

        //then
        assertThat(
            viewModel.listItemsValues,
            equalTo(
                listOf(
                    items.first(),
                    items[1].copy(status = AuthorizationStatus.DENY_PROCESSING)
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
                items[1].copy(status = AuthorizationStatus.DENY_PROCESSING)
            )
        )
        val result = ConfirmDenyResponseData(success = true, authorizationID = "1")

        //when
        v1Interactor.onConfirmDenySuccess(result = result, connectionID = "101")

        //then
        assertThat(
            viewModel.listItemsValues,
            equalTo(
                listOf(
                    items.first().copy(),
                    items[1].copy(status = AuthorizationStatus.DENY_PROCESSING)
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
                items[1].copy(status = AuthorizationStatus.DENY_PROCESSING)
            )
        )
        val error = createRequestError(404)

        //when
        viewModel.onConfirmDenyFailure(error = error, connectionID = "1", authorizationID = "2")

        //then
        assertThat(
            viewModel.listItemsValues,
            equalTo(listOf(items.first(), items[1].copy(status = AuthorizationStatus.ERROR)))
        )
        assertThat(viewModel.errorEvent.value, equalTo(ViewModelEvent(error)))
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenyFailureTestCase2() {
        //given invalid params
        viewModel.listItems.postValue(
            listOf(
                items.first().copy(),
                items[1].copy(status = AuthorizationStatus.DENY_PROCESSING)
            )
        )
        val error = createRequestError(404)

        //when
        viewModel.onConfirmDenyFailure(error = error, connectionID = "101", authorizationID = "102")

        //then
        assertThat(
            viewModel.listItemsValues,
            equalTo(listOf(items.first(), items[1].copy(status = AuthorizationStatus.DENY_PROCESSING)))
        )
        assertThat(
            viewModel.errorEvent.value,
            equalTo(ViewModelEvent(ApiErrorData(errorClassName = "ApiResponseError", errorMessage = "Request Error (404)")))
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
            connectionId = "1",
        )
    }
}
