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
package com.saltedge.authenticator.features.authorizations.details

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.authorizations.common.AuthorizationViewModel
import com.saltedge.authenticator.features.authorizations.common.ViewMode
import com.saltedge.authenticator.features.authorizations.common.toAuthorizationViewModel
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.constants.ERROR_CLASS_CONNECTION_NOT_FOUND
import com.saltedge.authenticator.sdk.constants.ERROR_CLASS_SSL_HANDSHAKE
import com.saltedge.authenticator.sdk.model.EncryptedData
import com.saltedge.authenticator.sdk.model.authorization.AuthorizationData
import com.saltedge.authenticator.sdk.model.authorization.AuthorizationIdentifier
import com.saltedge.authenticator.sdk.model.connection.ConnectionAbs
import com.saltedge.authenticator.sdk.model.connection.ConnectionAndKey
import com.saltedge.authenticator.sdk.model.connection.ConnectionStatus
import com.saltedge.authenticator.sdk.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.model.error.createRequestError
import com.saltedge.authenticator.sdk.model.response.ConfirmDenyResponseData
import com.saltedge.authenticator.sdk.polling.SingleAuthorizationPollingService
import com.saltedge.authenticator.sdk.tools.crypt.CryptoToolsAbs
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManagerAbs
import com.saltedge.authenticator.testTools.TestAppTools
import junit.framework.Assert.assertTrue
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import java.security.PrivateKey

@RunWith(RobolectricTestRunner::class)
class AuthorizationDetailsViewModelTest {

    private lateinit var viewModel: AuthorizationDetailsViewModel

    private val mockKeyStoreManager = mock(KeyStoreManagerAbs::class.java)
    private val mockConnectionsRepository = mock(ConnectionsRepositoryAbs::class.java)
    private val mockCryptoTools = mock(CryptoToolsAbs::class.java)
    private val mockApiManager = mock(AuthenticatorApiManagerAbs::class.java)
    private val mockPollingService = mock(SingleAuthorizationPollingService::class.java)
    private val mockPrivateKey = mock(PrivateKey::class.java)

    private val connection1 = Connection().apply {
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
    private val connection2 = Connection().apply {
        guid = "guid2"
        id = "2_noKey"
        code = "demobank2"
        name = "Demobank2"
        status = "${ConnectionStatus.ACTIVE}"
        accessToken = ""
        logoUrl = ""
        createdAt = 300L
        updatedAt = 300L
    }
    private val encryptedData1 = EncryptedData(id = "1", connectionId = "1")
    private val encryptedData2 = EncryptedData(id = "2_noKey", connectionId = "1")
    private val authorizationData1 = createAuthorizationData(id = 1)
    private val authorizationData2 = createAuthorizationData(id = 2)
    private val viewModel1 = authorizationData1.toAuthorizationViewModel(connection1)

    private fun createAuthorizationData(id: Int): AuthorizationData {
        val createdAt = DateTime.now(DateTimeZone.UTC)
        return AuthorizationData(
            id = "$id",
            authorizationCode = "$id$id$id",
            title = "title$id",
            description = "desc$id",
            connectionId = "1",
            createdAt = createdAt,
            expiresAt = createdAt.plusHours(id)
        )
    }

    @Before
    fun setUp() {
        doReturn(connection1).`when`(mockConnectionsRepository).getById("1")
        doReturn(connection2).`when`(mockConnectionsRepository).getById("2_noKey")
        doReturn(mockPollingService).`when`(mockApiManager)
            .createSingleAuthorizationPollingService()
        doReturn(ConnectionAndKey(connection1, mockPrivateKey))
            .`when`(mockKeyStoreManager).createConnectionAndKeyModel(connection1)
        doReturn(null).`when`(mockKeyStoreManager).getKeyPair("guid2")
        doReturn(authorizationData1).`when`(mockCryptoTools)
            .decryptAuthorizationData(
                encryptedData = encryptedData1,
                rsaPrivateKey = mockPrivateKey
            )
        doReturn(authorizationData2).`when`(mockCryptoTools)
            .decryptAuthorizationData(
                encryptedData = encryptedData2,
                rsaPrivateKey = mockPrivateKey
            )

        viewModel = AuthorizationDetailsViewModel(
            appContext = TestAppTools.applicationContext,
            connectionsRepository = mockConnectionsRepository,
            keyStoreManager = mockKeyStoreManager,
            cryptoTools = mockCryptoTools,
            apiManager = mockApiManager
        )
    }

    @Test
    @Throws(Exception::class)
    fun setInitialDataTest_case1() {
        //given valid identifier
        val identifier = AuthorizationIdentifier(authorizationID = "1", connectionID = "1")

        //when
        viewModel.setInitialData(identifier = identifier, closeAppOnBackPress = true, titleRes = null)

        //then
        assertThat(viewModel.authorizationModel.value, equalTo(AuthorizationViewModel(
            authorizationID = "1",
            authorizationCode = "",
            title = "",
            description = "",
            validSeconds = 0,
            endTime = DateTime(0L),
            startTime = DateTime(0L),
            connectionID = "1",
            connectionName = "",
            connectionLogoUrl = "",
            viewMode = ViewMode.LOADING
        )))

        viewModel.setInitialData(identifier = AuthorizationIdentifier("1", ""), closeAppOnBackPress = true, titleRes = null)

        assertThat(viewModel.authorizationModel.value, equalTo(AuthorizationViewModel(
            authorizationID = "1",
            authorizationCode = "",
            title = "",
            description = "",
            validSeconds = 0,
            endTime = DateTime(0L),
            startTime = DateTime(0L),
            connectionID = "",
            connectionName = "",
            connectionLogoUrl = "",
            viewMode = ViewMode.UNAVAILABLE
        )))

        viewModel.setInitialData(identifier = AuthorizationIdentifier("1", ""), closeAppOnBackPress = true, titleRes = null)

        assertThat(viewModel.authorizationModel.value, equalTo(AuthorizationViewModel(
            authorizationID = "1",
            authorizationCode = "",
            title = "",
            description = "",
            validSeconds = 0,
            endTime = DateTime(0L),
            startTime = DateTime(0L),
            connectionID = "",
            connectionName = "",
            connectionLogoUrl = "",
            viewMode = ViewMode.UNAVAILABLE
        )))
    }

    @Test
    @Throws(Exception::class)
    fun setInitialDataTest_case2() {
        //given invalid identifier
        val identifier = AuthorizationIdentifier(authorizationID = "1", connectionID = "")

        //when
        viewModel.setInitialData(identifier = identifier, closeAppOnBackPress = true, titleRes = null)

        //then
        assertThat(viewModel.authorizationModel.value, equalTo(AuthorizationViewModel(
            authorizationID = "1",
            authorizationCode = "",
            title = "",
            description = "",
            validSeconds = 0,
            endTime = DateTime(0L),
            startTime = DateTime(0L),
            connectionID = "",
            connectionName = "",
            connectionLogoUrl = "",
            viewMode = ViewMode.UNAVAILABLE
        )))
    }

    @Test
    @Throws(Exception::class)
    fun setInitialDataTest_case3() {
        //given invalid identifier
        val identifier = AuthorizationIdentifier(authorizationID = "", connectionID = "1")

        //when
        viewModel.setInitialData(identifier = identifier, closeAppOnBackPress = true, titleRes = null)

        //then
        assertThat(viewModel.authorizationModel.value, equalTo(AuthorizationViewModel(
            authorizationID = "",
            authorizationCode = "",
            title = "",
            description = "",
            validSeconds = 0,
            endTime = DateTime(0L),
            startTime = DateTime(0L),
            connectionID = "1",
            connectionName = "",
            connectionLogoUrl = "",
            viewMode = ViewMode.UNAVAILABLE
        )))
    }

    @Test
    @Throws(Exception::class)
    fun setInitialDataTest_case4() {
        //given null identifier
        val identifier = null

        //when
        viewModel.setInitialData(identifier = identifier, closeAppOnBackPress = true, titleRes = null)

        //then
        assertThat(viewModel.authorizationModel.value, equalTo(AuthorizationViewModel(
            authorizationID = "",
            authorizationCode = "",
            title = "",
            description = "",
            validSeconds = 0,
            endTime = DateTime(0L),
            startTime = DateTime(0L),
            connectionID = "",
            connectionName = "",
            connectionLogoUrl = "",
            viewMode = ViewMode.UNAVAILABLE
        )))
    }

    @Test
    @Throws(Exception::class)
    fun onFragmentResume() {
        //given valid identifier
        val identifier = AuthorizationIdentifier(authorizationID = "1", connectionID = "1")
        val lifecycle = LifecycleRegistry(mock(LifecycleOwner::class.java))
        viewModel.bindLifecycleObserver(lifecycle)
        viewModel.setInitialData(identifier = identifier, closeAppOnBackPress = true, titleRes = null)
        clearInvocations(mockPollingService)

        //when
        lifecycle.currentState = Lifecycle.State.RESUMED

        //then
        verify(mockPollingService).contract = viewModel
        verify(mockPollingService).start(authorizationId = "1")
    }

    @Test
    @Throws(Exception::class)
    fun onFragmentPauseTest() {
        //given
        val lifecycle = LifecycleRegistry(mock(LifecycleOwner::class.java))
        viewModel.bindLifecycleObserver(lifecycle)

        //when
        lifecycle.currentState = Lifecycle.State.RESUMED
        lifecycle.currentState = Lifecycle.State.STARTED//move to pause state (possible only after RESUMED state)

        //then
        verify(mockPollingService).start()
        verify(mockPollingService, times(2)).stop()
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTest_case1() {
        //given positive action
        val id = R.id.positiveActionView
        viewModel.setInitialData(identifier = AuthorizationIdentifier(authorizationID = "1", connectionID = "1"), closeAppOnBackPress = true, titleRes = null)
        viewModel.authorizationModel.value = viewModel1

        //when
        viewModel.onViewClick(id)

        //then
        verify(mockPollingService).stop()
        assertThat(viewModel.authorizationModel.value!!.viewMode, equalTo(ViewMode.CONFIRM_PROCESSING))
        verify(mockApiManager).confirmAuthorization(
            connectionAndKey = ConnectionAndKey(connection1, mockPrivateKey),
            authorizationId = "1",
            authorizationCode = "111",
            resultCallback = viewModel
        )
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTest_case2() {
        //given negative action
        val id = R.id.negativeActionView
        viewModel.setInitialData(identifier = AuthorizationIdentifier(authorizationID = "1", connectionID = "1"), closeAppOnBackPress = true, titleRes = null)
        viewModel.authorizationModel.value = viewModel1

        //when
        viewModel.onViewClick(id)

        //then
        verify(mockPollingService).stop()
        assertThat(viewModel.authorizationModel.value!!.viewMode, equalTo(ViewMode.DENY_PROCESSING))
        verify(mockApiManager).denyAuthorization(
            connectionAndKey = ConnectionAndKey(connection1, mockPrivateKey),
            authorizationId = "1",
            authorizationCode = "111",
            resultCallback = viewModel
        )
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTest_case3() {
        //given unknown action
        val id = R.id.actionView
        viewModel.setInitialData(identifier = AuthorizationIdentifier(authorizationID = "1", connectionID = "1"), closeAppOnBackPress = true, titleRes = null)
        viewModel.authorizationModel.value = viewModel1
        clearInvocations(mockConnectionsRepository, mockPollingService, mockApiManager)

        //when
        viewModel.onViewClick(id)

        //then
        verifyNoMoreInteractions(mockConnectionsRepository, mockPollingService, mockApiManager)
        assertThat(viewModel.authorizationModel.value!!.viewMode, equalTo(ViewMode.DEFAULT))
    }

    @Test
    @Throws(Exception::class)
    fun onTimerTickTest_case1() {
        //given null model
        viewModel.authorizationModel.value = null
        clearInvocations(mockConnectionsRepository, mockPollingService, mockApiManager)

        //when
        viewModel.onTimerTick()

        //then
        verifyNoMoreInteractions(mockConnectionsRepository, mockPollingService, mockApiManager)
    }

    @Test
    @Throws(Exception::class)
    fun onTimerTickTest_case2() {
        //given expired authorization that should marked as TIME_OUT
        viewModel.setInitialData(identifier = AuthorizationIdentifier(authorizationID = "1", connectionID = "1"), closeAppOnBackPress = true, titleRes = null)
        viewModel.authorizationModel.value = viewModel1
            .copy(endTime = DateTime.now().minusMinutes(1), viewMode = ViewMode.DEFAULT)
        clearInvocations(mockConnectionsRepository, mockPollingService, mockApiManager)

        //when
        viewModel.onTimerTick()

        //then
        verifyNoMoreInteractions(mockConnectionsRepository, mockApiManager)
        verify(mockPollingService).stop()
        assertThat(viewModel.onTimeUpdateEvent.value, equalTo(ViewModelEvent(Unit)))
        assertThat(viewModel.authorizationModel.value!!.viewMode, equalTo(ViewMode.TIME_OUT))
    }

    @Test
    @Throws(Exception::class)
    fun onTimerTickTest_case3() {
        //given authorization that should be destroyed (has destroyAt param)
        viewModel.setInitialData(identifier = AuthorizationIdentifier(authorizationID = "1", connectionID = "1"), closeAppOnBackPress = true, titleRes = null)
        viewModel.authorizationModel.value = viewModel1.copy(viewMode = ViewMode.TIME_OUT).apply {
            destroyAt = DateTime.now().minusMinutes(1)
        }
        clearInvocations(mockConnectionsRepository, mockPollingService, mockApiManager)

        //when
        viewModel.onTimerTick()

        //then
        verifyNoMoreInteractions(mockConnectionsRepository, mockPollingService, mockApiManager)
        assertThat(viewModel.onCloseAppEvent.value, equalTo(ViewModelEvent(Unit)))
    }

    @Test
    @Throws(Exception::class)
    fun onTimerTickTest_case4() {
        //given DEFAULT authorization
        viewModel.setInitialData(identifier = AuthorizationIdentifier(authorizationID = "1", connectionID = "1"), closeAppOnBackPress = true, titleRes = null)
        viewModel.authorizationModel.value = viewModel1.copy(viewMode = ViewMode.DEFAULT)
        clearInvocations(mockConnectionsRepository, mockPollingService, mockApiManager)

        //when
        viewModel.onTimerTick()

        //then
        verifyNoMoreInteractions(mockConnectionsRepository, mockPollingService, mockApiManager)
        assertThat(viewModel.onTimeUpdateEvent.value, equalTo(ViewModelEvent(Unit)))
    }

    @Test
    @Throws(Exception::class)
    fun onTimerTickTest_case5() {
        //given LOADING authorization
        viewModel.setInitialData(identifier = AuthorizationIdentifier(authorizationID = "1", connectionID = "1"), closeAppOnBackPress = true, titleRes = null)
        viewModel.authorizationModel.value = viewModel1.copy(viewMode = ViewMode.LOADING)
        clearInvocations(mockConnectionsRepository, mockPollingService, mockApiManager)

        //when
        viewModel.onTimerTick()

        //then
        verifyNoMoreInteractions(mockConnectionsRepository, mockPollingService, mockApiManager)
        assertThat(viewModel.onTimeUpdateEvent.value, `is`(nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun onTimerTickTest_case6() {
        //given CONFIRM_PROCESSING authorization
        viewModel.setInitialData(identifier = AuthorizationIdentifier(authorizationID = "1", connectionID = "1"), closeAppOnBackPress = true, titleRes = null)
        viewModel.authorizationModel.value = viewModel1.copy(viewMode = ViewMode.CONFIRM_PROCESSING)
        clearInvocations(mockConnectionsRepository, mockPollingService, mockApiManager)

        //when
        viewModel.onTimerTick()

        //then
        verifyNoMoreInteractions(mockConnectionsRepository, mockPollingService, mockApiManager)
        assertThat(viewModel.onTimeUpdateEvent.value, `is`(nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun onTimerTickTest_case7() {
        //given DENY_PROCESSING authorization
        viewModel.setInitialData(identifier = AuthorizationIdentifier(authorizationID = "1", connectionID = "1"), closeAppOnBackPress = true, titleRes = null)
        viewModel.authorizationModel.value = viewModel1.copy(viewMode = ViewMode.DENY_PROCESSING)
        clearInvocations(mockConnectionsRepository, mockPollingService, mockApiManager)

        //when
        viewModel.onTimerTick()

        //then
        verifyNoMoreInteractions(mockConnectionsRepository, mockPollingService, mockApiManager)
        assertThat(viewModel.onTimeUpdateEvent.value, `is`(nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun onTimerTickTest_case8() {
        //given CONFIRM_SUCCESS authorization
        viewModel.setInitialData(identifier = AuthorizationIdentifier(authorizationID = "1", connectionID = "1"), closeAppOnBackPress = true, titleRes = null)
        viewModel.authorizationModel.value = viewModel1.copy(viewMode = ViewMode.CONFIRM_SUCCESS)
        clearInvocations(mockConnectionsRepository, mockPollingService, mockApiManager)

        //when
        viewModel.onTimerTick()

        //then
        verifyNoMoreInteractions(mockConnectionsRepository, mockPollingService, mockApiManager)
        assertThat(viewModel.onTimeUpdateEvent.value, `is`(nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun onTimerTickTest_case9() {
        //given DENY_SUCCESS authorization
        viewModel.setInitialData(identifier = AuthorizationIdentifier(authorizationID = "1", connectionID = "1"), closeAppOnBackPress = true, titleRes = null)
        viewModel.authorizationModel.value = viewModel1.copy(viewMode = ViewMode.DENY_SUCCESS)
        clearInvocations(mockConnectionsRepository, mockPollingService, mockApiManager)

        //when
        viewModel.onTimerTick()

        //then
        verifyNoMoreInteractions(mockConnectionsRepository, mockPollingService, mockApiManager)
        assertThat(viewModel.onTimeUpdateEvent.value, `is`(nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun onTimerTickTest_case10() {
        //given ERROR authorization
        viewModel.setInitialData(identifier = AuthorizationIdentifier(authorizationID = "1", connectionID = "1"), closeAppOnBackPress = true, titleRes = null)
        viewModel.authorizationModel.value = viewModel1.copy(viewMode = ViewMode.ERROR)
        clearInvocations(mockConnectionsRepository, mockPollingService, mockApiManager)

        //when
        viewModel.onTimerTick()

        //then
        verifyNoMoreInteractions(mockConnectionsRepository, mockPollingService, mockApiManager)
        assertThat(viewModel.onTimeUpdateEvent.value, `is`(nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun onTimerTickTest_case11() {
        //given TIME_OUT authorization
        viewModel.setInitialData(identifier = AuthorizationIdentifier(authorizationID = "1", connectionID = "1"), closeAppOnBackPress = true, titleRes = null)
        viewModel.authorizationModel.value = viewModel1.copy(viewMode = ViewMode.TIME_OUT)
        clearInvocations(mockConnectionsRepository, mockPollingService, mockApiManager)

        //when
        viewModel.onTimerTick()

        //then
        verifyNoMoreInteractions(mockConnectionsRepository, mockPollingService, mockApiManager)
        assertThat(viewModel.onTimeUpdateEvent.value, `is`(nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun onTimerTickTest_case12() {
        //given UNAVAILABLE authorization
        viewModel.setInitialData(identifier = AuthorizationIdentifier(authorizationID = "1", connectionID = "1"), closeAppOnBackPress = true, titleRes = null)
        viewModel.authorizationModel.value = viewModel1.copy(viewMode = ViewMode.UNAVAILABLE)
        clearInvocations(mockConnectionsRepository, mockPollingService, mockApiManager)

        //when
        viewModel.onTimerTick()

        //then
        verifyNoMoreInteractions(mockConnectionsRepository, mockPollingService, mockApiManager)
        assertThat(viewModel.onTimeUpdateEvent.value, `is`(nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun getConnectionDataForAuthorizationPollingTest() {
        Assert.assertNull(viewModel.getConnectionDataForAuthorizationPolling())

        viewModel.setInitialData(identifier = AuthorizationIdentifier(connectionID = "2_noKey", authorizationID = ""), closeAppOnBackPress = true, titleRes = null)

        Assert.assertNull(viewModel.getConnectionDataForAuthorizationPolling())

        viewModel.setInitialData(identifier = AuthorizationIdentifier(connectionID = "1", authorizationID = "2_noKey"), closeAppOnBackPress = true, titleRes = null)

        assertThat(
            viewModel.getConnectionDataForAuthorizationPolling(),
            equalTo(ConnectionAndKey(connection1 as ConnectionAbs, mockPrivateKey))
        )
    }

    @Test
    @Throws(Exception::class)
    fun onFetchAuthorizationResultTest_case1() {
        //given initial authorization and success result
        viewModel.setInitialData(identifier = AuthorizationIdentifier(connectionID = "1", authorizationID = "1"), closeAppOnBackPress = true, titleRes = null)

        //when
        viewModel.onFetchAuthorizationResult(result = encryptedData1, error = null)

        //then
        assertThat(viewModel.authorizationModel.value, equalTo(viewModel1))
    }

    /**
     * current viewmodel has processing state
     */
    @Test
    @Throws(Exception::class)
    fun onFetchAuthorizationResultTest_case2() {
        //given DENY_PROCESSING authorization and success result
        viewModel.setInitialData(identifier = AuthorizationIdentifier(connectionID = "1", authorizationID = "1"), closeAppOnBackPress = true, titleRes = null)
        viewModel.authorizationModel.value = viewModel1.copy(viewMode = ViewMode.DENY_PROCESSING)

        //when
        viewModel.onFetchAuthorizationResult(result = encryptedData1, error = null)

        //then
        assertThat(viewModel.authorizationModel.value, equalTo(viewModel1.copy(viewMode = ViewMode.DENY_PROCESSING)))
    }

    @Test
    @Throws(Exception::class)
    fun onFetchAuthorizationResultTest_case3() {
        //given 404 error
        val error = createRequestError(404)
        viewModel.setInitialData(identifier = AuthorizationIdentifier(connectionID = "1", authorizationID = "1"), closeAppOnBackPress = true, titleRes = null)
        clearInvocations(mockConnectionsRepository)

        //when
        viewModel.onFetchAuthorizationResult(result = null, error = error)

        //then
        assertThat(viewModel.onErrorEvent.value, equalTo(ViewModelEvent("Request Error (404)")))
        verifyNoMoreInteractions(mockConnectionsRepository)
    }

    @Test
    @Throws(Exception::class)
    fun onFetchAuthorizationResultTest_case4() {
        //given Connectivity error
        val error = ApiErrorData(errorClassName = ERROR_CLASS_SSL_HANDSHAKE, errorMessage = "ErrorMessage")
        viewModel.setInitialData(identifier = AuthorizationIdentifier(connectionID = "1", authorizationID = "1"), closeAppOnBackPress = true, titleRes = null)
        clearInvocations(mockConnectionsRepository)

        //when
        viewModel.onFetchAuthorizationResult(result = null, error = error)

        //then
        assertThat(viewModel.onErrorEvent.value, equalTo(ViewModelEvent("ErrorMessage")))
        verifyNoMoreInteractions(mockConnectionsRepository)
    }

    @Test
    @Throws(Exception::class)
    fun onFetchAuthorizationResultTest_case5() {
        //given ConnectionNotFound error
        val error = ApiErrorData(
            errorClassName = ERROR_CLASS_CONNECTION_NOT_FOUND,
            errorMessage = "Not found"
        )
        viewModel.setInitialData(identifier = AuthorizationIdentifier(connectionID = "1", authorizationID = "1"), closeAppOnBackPress = true, titleRes = null)
        viewModel.authorizationModel.value = viewModel1
        clearInvocations(mockConnectionsRepository)

        //when
        viewModel.onFetchAuthorizationResult(result = null, error = error)

        //then
        verify(mockConnectionsRepository).invalidateConnectionsByTokens(
            accessTokens = listOf("token1")
        )
        assertThat(viewModel.authorizationModel.value!!.viewMode, equalTo(ViewMode.ERROR))
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenyFailureTest() {
        //given 404 error
        val error = createRequestError(404)
        viewModel.setInitialData(identifier = AuthorizationIdentifier(connectionID = "1", authorizationID = "1"), closeAppOnBackPress = true, titleRes = null)

        //when
        viewModel.onConfirmDenyFailure(error = error, connectionID = "333", authorizationID = "444")

        //then
        assertThat(viewModel.onErrorEvent.value, equalTo(ViewModelEvent("Request Error (404)")))
        verify(mockPollingService).contract = null
        verify(mockPollingService).stop()
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenySuccessTest_case1() {
        //given invalid result
        viewModel.setInitialData(identifier = AuthorizationIdentifier(connectionID = "1", authorizationID = "1"), closeAppOnBackPress = true, titleRes = null)
        viewModel.authorizationModel.value = viewModel1
        clearInvocations(mockPollingService)

        //when
        viewModel.onConfirmDenySuccess(result = ConfirmDenyResponseData(), connectionID = "1")

        //then
        verify(mockPollingService).start("1")
        assertThat(viewModel.authorizationModel.value, equalTo(viewModel1))
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenySuccessTest_case2() {
        //given TIME_OUT result
        viewModel.setInitialData(identifier = AuthorizationIdentifier(connectionID = "1", authorizationID = "1"), closeAppOnBackPress = true, titleRes = null)
        viewModel.authorizationModel.value = viewModel1.copy(viewMode = ViewMode.TIME_OUT)
        clearInvocations(mockPollingService)

        //when
        viewModel.onConfirmDenySuccess(
            result = ConfirmDenyResponseData(authorizationID = "1", success = true),
            connectionID = "1"
        )

        //then
        verifyNoMoreInteractions(mockPollingService)
        assertThat(viewModel.authorizationModel.value, equalTo(viewModel1.copy(viewMode = ViewMode.ERROR)))
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenySuccessTest_case3() {
        //given CONFIRM_PROCESSING authorization
        viewModel.setInitialData(identifier = AuthorizationIdentifier(connectionID = "1", authorizationID = "1"), closeAppOnBackPress = true, titleRes = null)
        viewModel.authorizationModel.value = viewModel1.copy(viewMode = ViewMode.CONFIRM_PROCESSING)

        //when
        viewModel.onConfirmDenySuccess(
            result = ConfirmDenyResponseData(authorizationID = "1", success = true),
            connectionID = "1"
        )

        //then
        assertThat(viewModel.authorizationModel.value!!.viewMode, equalTo(ViewMode.CONFIRM_SUCCESS))
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenySuccessTest_case4() {
        //given DENY_PROCESSING authorization
        viewModel.setInitialData(identifier = AuthorizationIdentifier(connectionID = "1", authorizationID = "1"), closeAppOnBackPress = true, titleRes = null)
        viewModel.authorizationModel.value = viewModel1.copy(viewMode = ViewMode.DENY_PROCESSING)

        //when
        viewModel.onConfirmDenySuccess(
            result = ConfirmDenyResponseData(authorizationID = "1", success = true),
            connectionID = "1"
        )

        //then
        assertThat(viewModel.authorizationModel.value!!.viewMode, equalTo(ViewMode.DENY_SUCCESS))
    }

    @Test
    @Throws(Exception::class)
    fun onBackPressTestCase1() {
        //given closeAppOnBackPress = null
        viewModel.setInitialData(
            identifier = AuthorizationIdentifier(connectionID = "1", authorizationID = "1"),
            closeAppOnBackPress = null,
            titleRes = null
        )

        //when
        val onBackPressResult = viewModel.onBackPress()

        //then
        assertTrue(onBackPressResult)
        assertThat(viewModel.onCloseAppEvent.value, equalTo(ViewModelEvent(Unit)))
    }

    @Test
    @Throws(Exception::class)
    fun onBackPressTestCase2() {
        //given closeAppOnBackPress = true
        viewModel.setInitialData(
            identifier = AuthorizationIdentifier(connectionID = "1", authorizationID = "1"),
            closeAppOnBackPress = true,
            titleRes = null
        )

        //when
        val onBackPressResult = viewModel.onBackPress()

        //then
        assertTrue(onBackPressResult)
        assertThat(viewModel.onCloseAppEvent.value, equalTo(ViewModelEvent(Unit)))
    }

    @Test
    @Throws(Exception::class)
    fun onBackPressTestCase3() {
        //given closeAppOnBackPress = false
        viewModel.setInitialData(
            identifier = AuthorizationIdentifier(connectionID = "1", authorizationID = "1"),
            closeAppOnBackPress = false,
            titleRes = null
        )

        //when
        val onBackPressResult = viewModel.onBackPress()

        //then
        assertTrue(onBackPressResult)
        assertThat(viewModel.onCloseViewEvent.value, equalTo(ViewModelEvent(Unit)))
    }
}
