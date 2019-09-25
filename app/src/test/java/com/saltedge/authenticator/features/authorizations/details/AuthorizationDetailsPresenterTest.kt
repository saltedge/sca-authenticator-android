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
package com.saltedge.authenticator.features.authorizations.details

import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.authorizations.common.AuthorizationContentView
import com.saltedge.authenticator.features.authorizations.common.toAuthorizationViewModel
import com.saltedge.authenticator.model.db.Connection
import com.saltedge.authenticator.model.db.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.constants.ERROR_CLASS_CONNECTION_NOT_FOUND
import com.saltedge.authenticator.sdk.constants.ERROR_CLASS_SSL_HANDSHAKE
import com.saltedge.authenticator.sdk.model.*
import com.saltedge.authenticator.sdk.model.response.ConfirmDenyResultData
import com.saltedge.authenticator.sdk.polling.SingleAuthorizationPollingService
import com.saltedge.authenticator.sdk.tools.CryptoToolsAbs
import com.saltedge.authenticator.sdk.tools.KeyStoreManagerAbs
import com.saltedge.authenticator.testTools.TestAppTools
import com.saltedge.authenticator.tool.secure.fingerprint.BiometricToolsAbs
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import java.security.KeyPair
import java.security.PrivateKey

@RunWith(RobolectricTestRunner::class)
class AuthorizationDetailsPresenterTest {

    @Test
    @Throws(Exception::class)
    fun constructorTest() {
        Assert.assertNull(createPresenter(viewContract = null).viewContract)
        Assert.assertNotNull(createPresenter(viewContract = mockView).viewContract)
    }

    @Test
    @Throws(Exception::class)
    fun onViewResumeTest_invalidInitialData() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = "", authorizationId = "")
        presenter.onFragmentResume()

        Mockito.verify(mockView).startTimer()
        Mockito.verify(mockView).setHeaderVisibility(false)
        Mockito.verify(mockView).setContentViewMode(AuthorizationContentView.Mode.UNAVAILABLE)
        Mockito.verifyNoMoreInteractions(mockPollingService, mockView)
    }

    @Test
    @Throws(Exception::class)
    fun onViewResumeTest_RunTimer() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = "1", authorizationId = "1")
        presenter.onFragmentResume()

        Mockito.verify(mockPollingService).start("1")
        Mockito.verify(mockView).startTimer()
        Mockito.verify(mockView).setHeaderVisibility(false)
        Mockito.verify(mockView).setContentViewMode(AuthorizationContentView.Mode.LOADING)
        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    @Throws(Exception::class)
    fun onViewPauseTest() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.onFragmentPause()

        Mockito.verify(mockPollingService).stop()
        Mockito.verify(mockView).stopTimer()
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTest_negativeActionView() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = viewModel1.connectionID, authorizationId = viewModel1.authorizationID)
        presenter.currentViewModel = viewModel1

        presenter.onViewClick(R.id.negativeActionView)

        assertThat(presenter.currentViewModel!!.viewMode, equalTo(AuthorizationContentView.Mode.DENY_PROCESSING))
        Mockito.verify(mockApiManager).denyAuthorization(
            connectionAndKey = ConnectionAndKey(connection1, mockPrivateKey),
            authorizationId = "1",
            authorizationCode = "111",
            resultCallback = presenter
        )
        Mockito.verify(mockPollingService).stop()
        Mockito.verify(mockView).setContentViewMode(AuthorizationContentView.Mode.DENY_PROCESSING)
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTest_negativeActionView_InvalidParams() {
        val presenter = createPresenter(viewContract = null)
        presenter.setInitialData(connectionId = viewModel1.connectionID, authorizationId = viewModel1.authorizationID)
        presenter.currentViewModel = viewModel1

        Mockito.clearInvocations(mockApiManager, mockPollingService, mockView)
        presenter.onViewClick(R.id.negativeActionView)

        Mockito.verifyNoMoreInteractions(mockView)

        presenter.viewContract = mockView
        Mockito.clearInvocations(mockApiManager, mockPollingService, mockView)
        presenter.onViewClick(-1)

        Mockito.verifyNoMoreInteractions(mockApiManager, mockPollingService, mockView)

        presenter.setInitialData(connectionId = "", authorizationId = "")
        presenter.currentViewModel = null

        Mockito.clearInvocations(mockApiManager, mockPollingService, mockView)
        presenter.onViewClick(R.id.negativeActionView)

        Mockito.verifyNoMoreInteractions(mockApiManager, mockPollingService, mockView)
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTest_positiveActionView() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = viewModel1.connectionID, authorizationId = viewModel1.authorizationID)
        presenter.currentViewModel = viewModel1

        Mockito.doReturn(true).`when`(mockBiometricTools).isBiometricReady(TestAppTools.applicationContext)
        presenter.onViewClick(R.id.positiveActionView)

        assertThat(presenter.currentViewModel!!.viewMode, equalTo(AuthorizationContentView.Mode.CONFIRM_PROCESSING))
        Mockito.verify(mockApiManager).confirmAuthorization(
            connectionAndKey = ConnectionAndKey(connection1, mockPrivateKey),
            authorizationId = "1",
            authorizationCode = "111",
            resultCallback = presenter
        )
        Mockito.verify(mockPollingService).stop()
        Mockito.verify(mockView).setContentViewMode(AuthorizationContentView.Mode.CONFIRM_PROCESSING)
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTest_positiveActionView_InvalidParams() {
        val presenter = createPresenter(viewContract = null)
        presenter.setInitialData(connectionId = viewModel1.connectionID, authorizationId = viewModel1.authorizationID)
        presenter.currentViewModel = viewModel1

        Mockito.doReturn(true).`when`(mockBiometricTools).isBiometricReady(TestAppTools.applicationContext)
        presenter.onViewClick(R.id.positiveActionView)

        Mockito.verify(mockView, Mockito.never()).askUserBiometricConfirmation()

        Mockito.doReturn(false).`when`(mockBiometricTools).isBiometricReady(TestAppTools.applicationContext)
        presenter.onViewClick(R.id.positiveActionView)

        Mockito.verify(mockView, Mockito.never()).askUserPasscodeConfirmation()
    }

    @Test
    @Throws(Exception::class)
    fun onTimerTickTest_noModel() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.currentViewModel = null
        presenter.onTimerTick()

        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    @Throws(Exception::class)
    fun onTimerTickTest_shouldBeSetTimeOutMode() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = viewModel1.connectionID, authorizationId = viewModel1.authorizationID)
        presenter.currentViewModel = viewModel1.copy(expiresAt = DateTime.now().minusMinutes(1), viewMode = AuthorizationContentView.Mode.DEFAULT)

        presenter.onTimerTick()

        Mockito.verify(mockView).updateTimeViews()
        Mockito.verify(mockView).setContentViewMode(AuthorizationContentView.Mode.TIME_OUT)
        assertThat(presenter.currentViewModel!!.viewMode, equalTo(AuthorizationContentView.Mode.TIME_OUT))
    }

    @Test
    @Throws(Exception::class)
    fun onTimerTickTest_shouldBeDestroyed() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = viewModel1.connectionID, authorizationId = viewModel1.authorizationID)
        presenter.currentViewModel = viewModel1.copy(viewMode = AuthorizationContentView.Mode.TIME_OUT).apply {
            destroyAt = DateTime.now().minusMinutes(1)
        }

        presenter.onTimerTick()

        Mockito.verify(mockView).closeView()
    }

    @Test
    @Throws(Exception::class)
    fun onTimerTickTest_updateTimeViews() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = viewModel1.connectionID, authorizationId = viewModel1.authorizationID)
        presenter.currentViewModel = viewModel1

        presenter.onTimerTick()

        Mockito.verify(mockView).updateTimeViews()
    }

    @Test
    @Throws(Exception::class)
    fun onTimerTickTest_remainedTime_invalidParams() {
        val presenter = createPresenter(viewContract = null)
        presenter.setInitialData(connectionId = viewModel1.connectionID, authorizationId = viewModel1.authorizationID)
        presenter.currentViewModel = viewModel1

        presenter.onTimerTick()

        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    @Throws(Exception::class)
    fun getRequestDataTest() {
        val presenter = createPresenter(viewContract = mockView)

        Assert.assertNull(presenter.getConnectionData())

        presenter.setInitialData(connectionId = "2_noKey", authorizationId = null)

        Assert.assertNull(presenter.getConnectionData())

        presenter.setInitialData(connectionId = "1", authorizationId = "2_noKey")

        assertThat(
            presenter.getConnectionData(),
            equalTo(ConnectionAndKey(connection1 as ConnectionAbs, mockPrivateKey))
        )
    }

    @Test
    @Throws(Exception::class)
    fun onFetchAuthorizationResultTest_processAuthorizationResult() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = "1", authorizationId = "1")

        Assert.assertNull(presenter.currentViewModel)

        presenter.onFetchAuthorizationResult(result = encryptedData1, error = null)

        assertThat(presenter.currentViewModel, equalTo(viewModel1))
        Mockito.verify(mockView).setHeaderVisibility(true)
        Mockito.verify(mockView).setContentViewMode(AuthorizationContentView.Mode.DEFAULT)
        Mockito.verify(mockView).setHeaderValues(
            logo = viewModel1.connectionLogoUrl ?: "",
            title = viewModel1.connectionName,
            startTime = viewModel1.createdAt,
            endTime = viewModel1.expiresAt
        )
        Mockito.verify(mockView).setContentTitleAndDescription(
            title = viewModel1.title,
            description = viewModel1.description
        )
        Mockito.verify(mockView).startTimer()

        Mockito.clearInvocations(mockView)
        presenter.onFetchAuthorizationResult(result = encryptedData1, error = null)

        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    @Throws(Exception::class)
    fun onFetchAuthorizationResultTest_processAuthorizationResult_InvalidParams() {
        val presenter = createPresenter(viewContract = null)
        presenter.onFetchAuthorizationResult(result = null, error = null)

        Mockito.never()

        presenter.setInitialData(connectionId = "1", authorizationId = "")
        Mockito.clearInvocations(mockView, mockConnectionsRepository)
        presenter.onFetchAuthorizationResult(result = encryptedData1, error = null)

        Mockito.verify(mockView, Mockito.never()).updateViewsContent()

        Mockito.clearInvocations(mockView, mockConnectionsRepository)
        presenter.onFetchAuthorizationResult(result = null, error = createRequestError(404))

        Mockito.verifyNoMoreInteractions(mockView, mockConnectionsRepository)
    }

    @Test
    @Throws(Exception::class)
    fun onFetchAuthorizationResultTest_processAuthorizationError_anyError() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = "1", authorizationId = "")

        Mockito.clearInvocations(mockView, mockConnectionsRepository)
        presenter.onFetchAuthorizationResult(result = null, error = createRequestError(404))

        Mockito.verify(mockView).showError("Request Error (404)")
        Mockito.verifyNoMoreInteractions(mockConnectionsRepository)
    }

    @Test
    @Throws(Exception::class)
    fun onFetchAuthorizationResultTest_processAuthorizationError_connectivityError() {
        val error = ApiErrorData(errorClassName = ERROR_CLASS_SSL_HANDSHAKE, errorMessage = "ErrorMessage")
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = "1", authorizationId = "")

        Mockito.clearInvocations(mockView)
        Mockito.clearInvocations(mockConnectionsRepository)
        presenter.onFetchAuthorizationResult(result = null, error = error)

        Mockito.verify(mockView).showError("ErrorMessage")
        Mockito.verifyNoMoreInteractions(mockConnectionsRepository)
    }

    @Test
    @Throws(Exception::class)
    fun onFetchAuthorizationResultTest_processAuthorizationError_connectionNotFound() {
        val error = ApiErrorData(errorClassName = ERROR_CLASS_CONNECTION_NOT_FOUND, errorMessage = "Not found")
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = "1", authorizationId = "")
        presenter.currentViewModel = viewModel1

        presenter.onFetchAuthorizationResult(result = null, error = error)

        Mockito.verify(mockConnectionsRepository).invalidateConnectionsByTokens(
            accessTokens = listOf("token1")
        )
        assertThat(presenter.currentViewModel!!.viewMode, equalTo(AuthorizationContentView.Mode.ERROR))
        Mockito.verify(mockView).setContentViewMode(AuthorizationContentView.Mode.ERROR)

        presenter.setInitialData(connectionId = "X", authorizationId = "")
        Mockito.clearInvocations(mockView, mockConnectionsRepository)
        presenter.onFetchAuthorizationResult(result = null, error = error)

        assertThat(presenter.currentViewModel!!.viewMode, equalTo(AuthorizationContentView.Mode.ERROR))
        Mockito.verify(mockView).setContentViewMode(AuthorizationContentView.Mode.ERROR)

        Mockito.verifyNoMoreInteractions(mockConnectionsRepository)
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenyFailureTest() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = "1", authorizationId = "")
        presenter.onConfirmDenyFailure(
            error = createRequestError(404),
            connectionID = "333",
            authorizationID = "444"
        )

        Mockito.verifyNoMoreInteractions(mockPollingService)
        Mockito.verify(mockView).showError("Request Error (404)")

        presenter.setInitialData(connectionId = "1", authorizationId = "1")
        Mockito.clearInvocations(mockView, mockPollingService)
        presenter.onConfirmDenyFailure(
            error = createRequestError(404),
            connectionID = "333",
            authorizationID = "444"
        )

        Mockito.verifyNoMoreInteractions(mockPollingService)
        Mockito.verify(mockView).showError("Request Error (404)")

        presenter.viewContract = null
        Mockito.clearInvocations(mockView, mockPollingService)
        presenter.onConfirmDenyFailure(
            error = createRequestError(404),
            connectionID = "333",
            authorizationID = "444"
        )

        Mockito.verifyNoMoreInteractions(mockPollingService)
        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenySuccessTest_BasePresenter() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = "1", authorizationId = "")

        presenter.onConfirmDenySuccess(result = ConfirmDenyResultData(), connectionID = "1")

        Mockito.verifyNoMoreInteractions(mockPollingService)
        Mockito.verify(mockView).setContentViewMode(AuthorizationContentView.Mode.DEFAULT)

        Mockito.clearInvocations(mockView, mockPollingService)
        presenter.onConfirmDenySuccess(
            result = ConfirmDenyResultData(authorizationId = "1", success = true),
            connectionID = "1"
        )

        Mockito.verify(mockView).setContentViewMode(AuthorizationContentView.Mode.ERROR)
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenySuccessTest_successConfirm() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = "1", authorizationId = "1")
        presenter.currentViewModel = viewModel1.copy(viewMode = AuthorizationContentView.Mode.CONFIRM_PROCESSING)

        presenter.onConfirmDenySuccess(success = true, connectionID = "1", authorizationID = "1")

        assertThat(presenter.currentViewModel!!.viewMode, equalTo(AuthorizationContentView.Mode.CONFIRM_SUCCESS))
        Mockito.verify(mockView).setContentViewMode(AuthorizationContentView.Mode.CONFIRM_SUCCESS)
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenySuccessTest_successDeny() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = "1", authorizationId = "1")
        presenter.currentViewModel = viewModel1.copy(viewMode = AuthorizationContentView.Mode.DENY_PROCESSING)

        presenter.onConfirmDenySuccess(success = true, connectionID = "1", authorizationID = "1")

        assertThat(presenter.currentViewModel!!.viewMode, equalTo(AuthorizationContentView.Mode.DENY_SUCCESS))
        Mockito.verify(mockView).setContentViewMode(AuthorizationContentView.Mode.DENY_SUCCESS)
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenySuccessTest_failedConfirm() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = "1", authorizationId = "1")
        presenter.currentViewModel = viewModel1.copy(viewMode = AuthorizationContentView.Mode.CONFIRM_PROCESSING)

        presenter.onConfirmDenySuccess(success = false, connectionID = "1", authorizationID = "1")

        assertThat(presenter.currentViewModel!!.viewMode, equalTo(AuthorizationContentView.Mode.DEFAULT))
        Mockito.verify(mockPollingService).start(authorizationId = "1")
        Mockito.verify(mockView).setContentViewMode(AuthorizationContentView.Mode.DEFAULT)
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenySuccessTest_failedDeny() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = "1", authorizationId = "1")
        presenter.currentViewModel = viewModel1.copy(viewMode = AuthorizationContentView.Mode.DENY_PROCESSING)

        presenter.onConfirmDenySuccess(success = false, connectionID = "1", authorizationID = "1")

        assertThat(presenter.currentViewModel!!.viewMode, equalTo(AuthorizationContentView.Mode.DEFAULT))
        Mockito.verify(mockPollingService).start(authorizationId = "1")
        Mockito.verify(mockView).setContentViewMode(AuthorizationContentView.Mode.DEFAULT)
    }

    @Test
    @Throws(Exception::class)
    fun biometricAuthFinishedTest() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = viewModel1.connectionID, authorizationId = viewModel1.authorizationID)
        presenter.currentViewModel = viewModel1
        presenter.biometricAuthFinished()

        Mockito.verify(mockPollingService).stop()
        Mockito.verify(mockApiManager).confirmAuthorization(
            connectionAndKey = ConnectionAndKey(connection1, mockPrivateKey),
            authorizationId = "1",
            authorizationCode = "111",
            resultCallback = presenter
        )
        Mockito.verify(mockView).setContentViewMode(AuthorizationContentView.Mode.CONFIRM_PROCESSING)
    }

    @Test
    @Throws(Exception::class)
    fun biometricAuthFinishedTest_InvalidParams() {
        val presenter = createPresenter(viewContract = null)
        presenter.setInitialData(connectionId = viewModel1.connectionID, authorizationId = viewModel1.authorizationID)
        presenter.currentViewModel = viewModel1

        presenter.biometricAuthFinished()

        Mockito.verify(mockApiManager).confirmAuthorization(
            connectionAndKey = ConnectionAndKey(connection1, mockPrivateKey),
            authorizationId = "1",
            authorizationCode = "111",
            resultCallback = presenter
        )
        Mockito.verify(mockPollingService).stop()
        Mockito.verifyNoMoreInteractions(mockView)

        presenter.setInitialData(connectionId = "", authorizationId = null)
        presenter.currentViewModel = null
        Mockito.clearInvocations(mockView, mockPollingService, mockApiManager)
        presenter.biometricAuthFinished()

        Mockito.verifyNoMoreInteractions(mockView, mockPollingService, mockApiManager)
    }

    @Test
    @Throws(Exception::class)
    fun biometricsCanceledByUserTest() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = viewModel1.connectionID, authorizationId = viewModel1.authorizationID)
        presenter.currentViewModel = viewModel1

        presenter.biometricsCanceledByUser()

        Mockito.verify(mockView).askUserPasscodeConfirmation()

        presenter.viewContract = null
        presenter.biometricsCanceledByUser()

        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    @Throws(Exception::class)
    fun successAuthWithPasscodeTest() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = viewModel1.connectionID, authorizationId = viewModel1.authorizationID)
        presenter.currentViewModel = viewModel1

        presenter.successAuthWithPasscode()

        Mockito.verify(mockApiManager).confirmAuthorization(
            connectionAndKey = ConnectionAndKey(connection1, mockPrivateKey),
            authorizationId = "1",
            authorizationCode = "111",
            resultCallback = presenter
        )
        Mockito.verify(mockPollingService).stop()
        Mockito.verify(mockView).setContentViewMode(AuthorizationContentView.Mode.CONFIRM_PROCESSING)
    }

    @Test
    @Throws(Exception::class)
    fun passcodePromptCanceledByUserTest() {
        createPresenter(viewContract = mockView).passcodePromptCanceledByUser()

        Mockito.never()
    }

    @Before
    fun setUp() {
        Mockito.doReturn(connection1).`when`(mockConnectionsRepository).getById("1")
        Mockito.doReturn(connection2).`when`(mockConnectionsRepository).getById("2_noKey")
        Mockito.doReturn(mockPollingService).`when`(mockApiManager)
            .createSingleAuthorizationPollingService()
        Mockito.doReturn(KeyPair(null, mockPrivateKey))
            .`when`(mockKeyStoreManager).getKeyPair("guid1")
        Mockito.doReturn(null).`when`(mockKeyStoreManager).getKeyPair("guid2")
        Mockito.doReturn(authorizationData1).`when`(mockCryptoTools)
            .decryptAuthorizationData(
                encryptedData = encryptedData1,
                rsaPrivateKey = mockPrivateKey
            )
        Mockito.doReturn(authorizationData2).`when`(mockCryptoTools)
            .decryptAuthorizationData(
                encryptedData = encryptedData2,
                rsaPrivateKey = mockPrivateKey
            )
    }

    private val mockKeyStoreManager = Mockito.mock(KeyStoreManagerAbs::class.java)
    private val mockConnectionsRepository = Mockito.mock(ConnectionsRepositoryAbs::class.java)
    private val mockBiometricTools = Mockito.mock(BiometricToolsAbs::class.java)
    private val mockCryptoTools = Mockito.mock(CryptoToolsAbs::class.java)
    private val mockApiManager = Mockito.mock(AuthenticatorApiManagerAbs::class.java)
    private val mockPollingService = Mockito.mock(SingleAuthorizationPollingService::class.java)
    private val mockView = Mockito.mock(AuthorizationDetailsContract.View::class.java)
    private val mockPrivateKey = Mockito.mock(PrivateKey::class.java)

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
    private val encryptedData1 = EncryptedAuthorizationData(id = "1", connectionId = "1")
    private val encryptedData2 = EncryptedAuthorizationData(id = "2_noKey", connectionId = "1")
    private val authorizationData1 = createAuthorizationData(id = 1)
    private val authorizationData2 = createAuthorizationData(id = 2)
    private val viewModel1 = authorizationData1.toAuthorizationViewModel(connection1)

    private fun createPresenter(viewContract: AuthorizationDetailsContract.View? = null):
        AuthorizationDetailsPresenter {
        return AuthorizationDetailsPresenter(
            appContext = TestAppTools.applicationContext,
            connectionsRepository = mockConnectionsRepository,
            keyStoreManager = mockKeyStoreManager,
            biometricTools = mockBiometricTools,
            cryptoTools = mockCryptoTools,
            apiManager = mockApiManager
        ).apply { this.viewContract = viewContract }
    }

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
}
