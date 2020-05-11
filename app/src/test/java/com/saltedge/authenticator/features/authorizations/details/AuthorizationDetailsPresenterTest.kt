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
import com.saltedge.authenticator.features.authorizations.common.AuthorizationViewModel
import com.saltedge.authenticator.features.authorizations.common.ViewMode
import com.saltedge.authenticator.features.authorizations.common.toAuthorizationViewModel
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.constants.ERROR_CLASS_AUTHORIZATION_NOT_FOUND
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
import com.saltedge.authenticator.sdk.tools.biometric.BiometricToolsAbs
import com.saltedge.authenticator.sdk.tools.crypt.CryptoToolsAbs
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManagerAbs
import com.saltedge.authenticator.testTools.TestAppTools
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
        presenter.setInitialData(AuthorizationIdentifier(connectionID = "", authorizationID = ""))
        presenter.onFragmentResume()

        Mockito.verify(mockView).startTimer()
        Mockito.verify(mockView).setHeaderVisibility(false)
        Mockito.verify(mockView).setContentViewMode(
            ViewMode.UNAVAILABLE,
            ignoreTimeUpdate = ViewMode.UNAVAILABLE.showProgress
        )
        Mockito.verify(mockView).setHeaderValues(
            logoUrl = "",
            title = "",
            startTime = DateTime(0L),
            endTime = DateTime(0L)
        )
        Mockito.verify(mockView).setContentTitleAndDescription(
            title = "",
            description = ""
        )
        Mockito.verifyNoMoreInteractions(mockPollingService, mockView)
    }

    @Test
    @Throws(Exception::class)
    fun onViewResumeTest_RunTimer() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(AuthorizationIdentifier(connectionID = "1", authorizationID = "1"))
        presenter.onFragmentResume()

        Mockito.verify(mockView).startTimer()
        Mockito.verify(mockView).setHeaderVisibility(true)
        Mockito.verify(mockView).setContentViewMode(
            ViewMode.LOADING,
            ignoreTimeUpdate = ViewMode.LOADING.showProgress
        )
        Mockito.verify(mockView).setHeaderValues(
            logoUrl = "",
            title = "",
            startTime = DateTime(0L),
            endTime = DateTime(0L)
        )
        Mockito.verify(mockView).setContentTitleAndDescription(
            title = "",
            description = ""
        )
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
    fun onViewClickTest_negativeActionView_case1() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(AuthorizationIdentifier(
            connectionID = viewModel1.connectionID,
            authorizationID = viewModel1.authorizationID
        ))
        presenter.currentViewModel = viewModel1

        presenter.onViewClick(R.id.negativeActionView)

        assertThat(presenter.currentViewModel!!.viewMode, equalTo(ViewMode.DENY_PROCESSING))
        Mockito.verify(mockApiManager).denyAuthorization(
            connectionAndKey = ConnectionAndKey(connection1, mockPrivateKey),
            authorizationId = "1",
            authorizationCode = "111",
            resultCallback = presenter
        )
        Mockito.verify(mockPollingService).stop()
        Mockito.verify(mockView).setContentViewMode(
            ViewMode.DENY_PROCESSING,
            ignoreTimeUpdate = ViewMode.DENY_PROCESSING.showProgress
        )
    }

    /**
     * no view contract
     */
    @Test
    @Throws(Exception::class)
    fun onViewClickTest_negativeActionView_case2() {
        val presenter = createPresenter(viewContract = null)
        presenter.setInitialData(AuthorizationIdentifier(
            connectionID = viewModel1.connectionID,
            authorizationID = viewModel1.authorizationID
        ))
        presenter.currentViewModel = viewModel1

        Mockito.clearInvocations(mockApiManager, mockPollingService, mockView)
        presenter.onViewClick(R.id.negativeActionView)

        Mockito.verifyNoMoreInteractions(mockView)
    }

    /**
     * invalid view id
     */
    @Test
    @Throws(Exception::class)
    fun onViewClickTest_negativeActionView_case3() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(AuthorizationIdentifier(
            connectionID = viewModel1.connectionID,
            authorizationID = viewModel1.authorizationID
        ))
        presenter.currentViewModel = viewModel1
        Mockito.clearInvocations(mockApiManager, mockPollingService, mockView)
        presenter.onViewClick(-1)

        Mockito.verifyNoMoreInteractions(mockApiManager, mockPollingService, mockView)
    }

    /**
     * no viewmodel
     */
    @Test
    @Throws(Exception::class)
    fun onViewClickTest_negativeActionView_case4() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(AuthorizationIdentifier(connectionID = "", authorizationID = ""))
        presenter.currentViewModel = null

        Mockito.clearInvocations(mockApiManager, mockPollingService, mockView)
        presenter.onViewClick(R.id.negativeActionView)

        Mockito.verifyNoMoreInteractions(mockApiManager, mockPollingService, mockView)
    }

    /**
     * non DEFAULT viewMode
     */
    @Test
    @Throws(Exception::class)
    fun onViewClickTest_negativeActionView_case5() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(AuthorizationIdentifier(
            connectionID = viewModel1.connectionID,
            authorizationID = viewModel1.authorizationID
        ))
        presenter.currentViewModel = viewModel1.copy(viewMode = ViewMode.DENY_PROCESSING)

        Mockito.clearInvocations(mockApiManager, mockPollingService, mockView)
        presenter.onViewClick(R.id.negativeActionView)

        Mockito.verifyNoMoreInteractions(mockApiManager, mockPollingService, mockView)
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTest_positiveActionView_case1() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(AuthorizationIdentifier(
            connectionID = viewModel1.connectionID,
            authorizationID = viewModel1.authorizationID
        ))
        presenter.currentViewModel = viewModel1

        Mockito.doReturn(true).`when`(mockBiometricTools).isBiometricReady(TestAppTools.applicationContext)
        presenter.onViewClick(R.id.positiveActionView)

        assertThat(presenter.currentViewModel!!.viewMode, equalTo(ViewMode.CONFIRM_PROCESSING))
        Mockito.verify(mockApiManager).confirmAuthorization(
            connectionAndKey = ConnectionAndKey(connection1, mockPrivateKey),
            authorizationId = "1",
            authorizationCode = "111",
            resultCallback = presenter
        )
        Mockito.verify(mockPollingService).stop()
        Mockito.verify(mockView).setContentViewMode(
            ViewMode.CONFIRM_PROCESSING,
            ignoreTimeUpdate = ViewMode.CONFIRM_PROCESSING.showProgress
        )
    }

    /**
     * Invalid Params
     */
    @Test
    @Throws(Exception::class)
    fun onViewClickTest_positiveActionView_case2() {
        val presenter = createPresenter(viewContract = null)
        presenter.setInitialData(AuthorizationIdentifier(
            connectionID = viewModel1.connectionID,
            authorizationID = viewModel1.authorizationID
        ))
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
        presenter.setInitialData(AuthorizationIdentifier(
            connectionID = viewModel1.connectionID,
            authorizationID = viewModel1.authorizationID
        ))
        presenter.currentViewModel =
            viewModel1.copy(expiresAt = DateTime.now().minusMinutes(1), viewMode = ViewMode.DEFAULT)

        presenter.onTimerTick()

        Mockito.verify(mockView).updateTimeViews()
        Mockito.verify(mockView).setContentViewMode(
            ViewMode.TIME_OUT,
            ignoreTimeUpdate = ViewMode.TIME_OUT.showProgress
        )
        assertThat(presenter.currentViewModel!!.viewMode, equalTo(ViewMode.TIME_OUT))
    }

    @Test
    @Throws(Exception::class)
    fun onTimerTickTest_shouldBeDestroyed() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(AuthorizationIdentifier(
            connectionID = viewModel1.connectionID,
            authorizationID = viewModel1.authorizationID
        ))
        presenter.currentViewModel = viewModel1.copy(viewMode = ViewMode.TIME_OUT).apply {
            destroyAt = DateTime.now().minusMinutes(1)
        }

        presenter.onTimerTick()

        Mockito.verify(mockView).closeView()
    }

    /**
     * Update time views when ignoreTimeUpdate in AuthorizationViewModel is false
     */
    @Test
    @Throws(Exception::class)
    fun onTimerTickTest_updateTimeViews_case1() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(AuthorizationIdentifier(
            connectionID = viewModel1.connectionID,
            authorizationID = viewModel1.authorizationID
        ))
        viewModel1.viewMode = ViewMode.DEFAULT
        presenter.currentViewModel = viewModel1
        presenter.onTimerTick()

        Mockito.verify(mockView).updateTimeViews()
    }

    /**
     * Not update time views when ignoreTimeUpdate in AuthorizationViewModel is true
     */
    @Test
    @Throws(Exception::class)
    fun onTimerTickTest_updateTimeViews_case2() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(AuthorizationIdentifier(
            connectionID = viewModel1.connectionID,
            authorizationID = viewModel1.authorizationID
        ))
        viewModel1.viewMode = ViewMode.LOADING
        presenter.currentViewModel = viewModel1
        presenter.onTimerTick()

        Mockito.verifyNoMoreInteractions(mockView)

        viewModel1.viewMode = ViewMode.CONFIRM_PROCESSING
        presenter.currentViewModel = viewModel1
        presenter.onTimerTick()

        Mockito.verifyNoMoreInteractions(mockView)

        viewModel1.viewMode = ViewMode.DENY_PROCESSING
        presenter.currentViewModel = viewModel1
        presenter.onTimerTick()

        Mockito.verifyNoMoreInteractions(mockView)

        viewModel1.viewMode = ViewMode.CONFIRM_SUCCESS
        presenter.currentViewModel = viewModel1
        presenter.onTimerTick()

        Mockito.verifyNoMoreInteractions(mockView)

        viewModel1.viewMode = ViewMode.DENY_SUCCESS
        presenter.currentViewModel = viewModel1
        presenter.onTimerTick()

        Mockito.verifyNoMoreInteractions(mockView)

        viewModel1.viewMode = ViewMode.ERROR
        presenter.currentViewModel = viewModel1
        presenter.onTimerTick()

        Mockito.verifyNoMoreInteractions(mockView)

        viewModel1.viewMode = ViewMode.TIME_OUT
        presenter.currentViewModel = viewModel1
        presenter.onTimerTick()

        Mockito.verifyNoMoreInteractions(mockView)

        viewModel1.viewMode = ViewMode.UNAVAILABLE
        presenter.currentViewModel = viewModel1
        presenter.onTimerTick()

        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    @Throws(Exception::class)
    fun onTimerTickTest_remainedTime_invalidParams() {
        val presenter = createPresenter(viewContract = null)
        presenter.setInitialData(AuthorizationIdentifier(
            connectionID = viewModel1.connectionID,
            authorizationID = viewModel1.authorizationID
        ))
        presenter.currentViewModel = viewModel1

        presenter.onTimerTick()

        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    @Throws(Exception::class)
    fun getRequestDataTest() {
        val presenter = createPresenter(viewContract = mockView)

        Assert.assertNull(presenter.getConnectionData())

        presenter.setInitialData(AuthorizationIdentifier(connectionID = "2_noKey", authorizationID = ""))

        Assert.assertNull(presenter.getConnectionData())

        presenter.setInitialData(AuthorizationIdentifier(connectionID = "1", authorizationID = "2_noKey"))

        assertThat(
            presenter.getConnectionData(),
            equalTo(ConnectionAndKey(connection1 as ConnectionAbs, mockPrivateKey))
        )
    }

    @Test
    @Throws(Exception::class)
    fun onFetchAuthorizationResultTest_processAuthorizationResult_case1() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(AuthorizationIdentifier(connectionID = "1", authorizationID = "1"))
        presenter.onFetchAuthorizationResult(result = encryptedData1, error = null)

        assertThat(presenter.currentViewModel, equalTo(viewModel1))
        Mockito.verify(mockView).setHeaderVisibility(true)
        Mockito.verify(mockView).setContentViewMode(
            ViewMode.DEFAULT,
            ignoreTimeUpdate = ViewMode.DEFAULT.showProgress
        )
        Mockito.verify(mockView).setHeaderValues(
            logoUrl = viewModel1.connectionLogoUrl ?: "",
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

    /**
     * no view contract
     */
    @Test
    @Throws(Exception::class)
    fun onFetchAuthorizationResultTest_processAuthorizationResult_case2() {
        val presenter = createPresenter(viewContract = null)
        presenter.onFetchAuthorizationResult(result = null, error = null)

        Mockito.never()

        presenter.setInitialData(AuthorizationIdentifier(connectionID = "1", authorizationID = ""))
        Mockito.clearInvocations(mockView, mockConnectionsRepository)
        presenter.onFetchAuthorizationResult(result = encryptedData1, error = null)

        Mockito.verify(mockView, Mockito.never()).updateViewsContent()

        Mockito.clearInvocations(mockView, mockConnectionsRepository)
        presenter.onFetchAuthorizationResult(result = null, error = createRequestError(404))

        Mockito.verifyNoMoreInteractions(mockView, mockConnectionsRepository)
    }

    /**
     * current viewmodel has processing state
     */
    @Test
    @Throws(Exception::class)
    fun onFetchAuthorizationResultTest_processAuthorizationResult_case3() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(AuthorizationIdentifier(connectionID = "1", authorizationID = "1"))
        presenter.currentViewModel = viewModel1.copy(viewMode = ViewMode.DENY_PROCESSING)
        Mockito.clearInvocations(mockView, mockConnectionsRepository)
        presenter.onFetchAuthorizationResult(result = encryptedData1, error = null)

        Mockito.verifyNoMoreInteractions(mockView, mockConnectionsRepository)
    }

    /**
     * 404 error
     */
    @Test
    @Throws(Exception::class)
    fun onFetchAuthorizationResultTest_processAuthorizationError_case1() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(AuthorizationIdentifier(connectionID = "1", authorizationID = ""))

        Mockito.clearInvocations(mockView, mockConnectionsRepository)
        presenter.onFetchAuthorizationResult(result = null, error = createRequestError(404))

        Mockito.verify(mockView).showError("Request Error (404)")
        Mockito.verifyNoMoreInteractions(mockConnectionsRepository)
    }

    /**
     * connectivity error
     */
    @Test
    @Throws(Exception::class)
    fun onFetchAuthorizationResultTest_processAuthorizationError_case2() {
        val error = ApiErrorData(errorClassName = ERROR_CLASS_SSL_HANDSHAKE, errorMessage = "ErrorMessage")
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(AuthorizationIdentifier(connectionID = "1", authorizationID = ""))

        Mockito.clearInvocations(mockView)
        Mockito.clearInvocations(mockConnectionsRepository)
        presenter.onFetchAuthorizationResult(result = null, error = error)

        Mockito.verify(mockView).showError("ErrorMessage")
        Mockito.verifyNoMoreInteractions(mockConnectionsRepository)
    }

    /**
     * ConnectionNotFound error
     */
    @Test
    @Throws(Exception::class)
    fun onFetchAuthorizationResultTest_processAuthorizationError_case3() {
        val error = ApiErrorData(
            errorClassName = ERROR_CLASS_CONNECTION_NOT_FOUND,
            errorMessage = "Not found"
        )
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(AuthorizationIdentifier(connectionID = "1", authorizationID = ""))
        presenter.currentViewModel = viewModel1

        presenter.onFetchAuthorizationResult(result = null, error = error)

        Mockito.verify(mockConnectionsRepository).invalidateConnectionsByTokens(
            accessTokens = listOf("token1")
        )
        assertThat(presenter.currentViewModel!!.viewMode, equalTo(ViewMode.ERROR))
        Mockito.verify(mockView).setContentViewMode(
            ViewMode.ERROR,
            ignoreTimeUpdate = ViewMode.ERROR.showProgress
        )

        presenter.setInitialData(AuthorizationIdentifier(connectionID = "X", authorizationID = ""))
        Mockito.clearInvocations(mockView, mockConnectionsRepository)
        presenter.onFetchAuthorizationResult(result = null, error = error)

        assertThat(presenter.currentViewModel!!.viewMode, equalTo(ViewMode.ERROR))
        Mockito.verify(mockView).setContentViewMode(
            ViewMode.ERROR,
            ignoreTimeUpdate = ViewMode.ERROR.showProgress
        )

        Mockito.verifyNoMoreInteractions(mockConnectionsRepository)
    }

    /**
     * 404 error
     */
    @Test
    @Throws(Exception::class)
    fun onConfirmDenyFailureTest_case1() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(AuthorizationIdentifier(connectionID = "1", authorizationID = ""))
        presenter.onConfirmDenyFailure(
            error = createRequestError(404),
            connectionID = "333",
            authorizationID = "444"
        )

        Mockito.verify(mockView).showError("Request Error (404)")
        Mockito.verify(mockPollingService).contract = null
        Mockito.verify(mockPollingService).stop()
        Mockito.verifyNoMoreInteractions(mockPollingService)

        presenter.setInitialData(AuthorizationIdentifier(connectionID = "1", authorizationID = "1"))
        Mockito.clearInvocations(mockView, mockPollingService)
        presenter.onConfirmDenyFailure(
            error = createRequestError(404),
            connectionID = "333",
            authorizationID = "444"
        )

        Mockito.verify(mockView).showError("Request Error (404)")
        Mockito.verify(mockPollingService).contract = null
        Mockito.verify(mockPollingService).stop()
        Mockito.verifyNoMoreInteractions(mockPollingService)

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

    /**
     * ConnectionNotFound error
     */
    @Test
    @Throws(Exception::class)
    fun onConfirmDenyFailureTest_case2() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(AuthorizationIdentifier(connectionID = "1", authorizationID = "1"))
        presenter.onConfirmDenyFailure(
            error = ApiErrorData(errorClassName = ERROR_CLASS_CONNECTION_NOT_FOUND),
            connectionID = "1",
            authorizationID = "1"
        )

        Mockito.verifyNoMoreInteractions(mockPollingService)
        Mockito.verify(mockConnectionsRepository).invalidateConnectionsByTokens(listOf("token1"))
        Mockito.verify(mockView).setContentViewMode(
            ViewMode.ERROR,
            ignoreTimeUpdate = ViewMode.ERROR.showProgress
        )
    }

    /**
     * AuthorizationNotFound error
     */
    @Test
    @Throws(Exception::class)
    fun onConfirmDenyFailureTest_case3() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(AuthorizationIdentifier(connectionID = "1", authorizationID = "1"))
        presenter.onConfirmDenyFailure(
            error = ApiErrorData(errorClassName = ERROR_CLASS_AUTHORIZATION_NOT_FOUND),
            connectionID = "1",
            authorizationID = "1"
        )

        Mockito.verify(mockPollingService).stop()
        Mockito.verify(mockView).setContentViewMode(
            ViewMode.UNAVAILABLE,
            ignoreTimeUpdate = ViewMode.UNAVAILABLE.showProgress
        )

        presenter.currentViewModel = viewModel1
        Mockito.clearInvocations(mockView, mockPollingService)

        presenter.onConfirmDenyFailure(
            error = ApiErrorData(errorClassName = ERROR_CLASS_AUTHORIZATION_NOT_FOUND),
            connectionID = "1",
            authorizationID = "1"
        )

        Mockito.verify(mockPollingService).contract = null
        Mockito.verify(mockPollingService).stop()
        Mockito.verifyNoMoreInteractions(mockPollingService)
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenySuccessTest_BasePresenter() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(AuthorizationIdentifier(connectionID = "1", authorizationID = ""))

        presenter.onConfirmDenySuccess(result = ConfirmDenyResponseData(), connectionID = "1")

        Mockito.verifyNoMoreInteractions(mockPollingService)
        Mockito.verify(mockView).setContentViewMode(
            ViewMode.DEFAULT,
            ignoreTimeUpdate = ViewMode.DEFAULT.showProgress
        )

        Mockito.clearInvocations(mockView, mockPollingService)
        presenter.onConfirmDenySuccess(
            result = ConfirmDenyResponseData(authorizationID = "1", success = true),
            connectionID = "1"
        )

        Mockito.verify(mockView).setContentViewMode(
            ViewMode.ERROR,
            ignoreTimeUpdate = ViewMode.ERROR.showProgress
        )
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenySuccessTest_successConfirm() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(AuthorizationIdentifier(connectionID = "1", authorizationID = "1"))
        presenter.currentViewModel = viewModel1.copy(viewMode = ViewMode.CONFIRM_PROCESSING)

        presenter.onConfirmDenySuccess(success = true, connectionID = "1", authorizationID = "1")

        assertThat(presenter.currentViewModel!!.viewMode, equalTo(ViewMode.CONFIRM_SUCCESS))
        Mockito.verify(mockView).setContentViewMode(
            ViewMode.CONFIRM_SUCCESS,
            ignoreTimeUpdate = ViewMode.CONFIRM_SUCCESS.showProgress
        )
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenySuccessTest_successDeny() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(AuthorizationIdentifier(connectionID = "1", authorizationID = "1"))
        presenter.currentViewModel = viewModel1.copy(viewMode = ViewMode.DENY_PROCESSING)

        presenter.onConfirmDenySuccess(success = true, connectionID = "1", authorizationID = "1")

        assertThat(presenter.currentViewModel!!.viewMode, equalTo(ViewMode.DENY_SUCCESS))
        Mockito.verify(mockView).setContentViewMode(
            ViewMode.DENY_SUCCESS,
            ignoreTimeUpdate = ViewMode.DENY_SUCCESS.showProgress
        )
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenySuccessTest_failedConfirm() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(AuthorizationIdentifier(connectionID = "1", authorizationID = "1"))
        presenter.currentViewModel = viewModel1.copy(viewMode = ViewMode.CONFIRM_PROCESSING)

        presenter.onConfirmDenySuccess(success = false, connectionID = "1", authorizationID = "1")

        assertThat(presenter.currentViewModel!!.viewMode, equalTo(ViewMode.DEFAULT))
        Mockito.verify(mockPollingService).start(authorizationId = "1")
        Mockito.verify(mockView).setContentViewMode(
            ViewMode.DEFAULT,
            ignoreTimeUpdate = ViewMode.DEFAULT.showProgress
        )
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenySuccessTest_failedDeny() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(AuthorizationIdentifier(connectionID = "1", authorizationID = "1"))
        presenter.currentViewModel = viewModel1.copy(viewMode = ViewMode.DENY_PROCESSING)

        presenter.onConfirmDenySuccess(success = false, connectionID = "1", authorizationID = "1")

        assertThat(presenter.currentViewModel!!.viewMode, equalTo(ViewMode.DEFAULT))
        Mockito.verify(mockPollingService).start(authorizationId = "1")
        Mockito.verify(mockView).setContentViewMode(
            ViewMode.DEFAULT,
            ignoreTimeUpdate = ViewMode.DEFAULT.showProgress
        )
    }

    @Test
    @Throws(Exception::class)
    fun biometricAuthFinishedTest() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(AuthorizationIdentifier(
            connectionID = viewModel1.connectionID,
            authorizationID = viewModel1.authorizationID
        ))
        presenter.currentViewModel = viewModel1
        presenter.biometricAuthFinished()

        Mockito.verify(mockPollingService).stop()
        Mockito.verify(mockApiManager).confirmAuthorization(
            connectionAndKey = ConnectionAndKey(connection1, mockPrivateKey),
            authorizationId = "1",
            authorizationCode = "111",
            resultCallback = presenter
        )
        Mockito.verify(mockView).setContentViewMode(
            ViewMode.CONFIRM_PROCESSING,
            ignoreTimeUpdate = ViewMode.CONFIRM_PROCESSING.showProgress
        )
    }

    @Test
    @Throws(Exception::class)
    fun biometricAuthFinishedTest_InvalidParams() {
        val presenter = createPresenter(viewContract = null)
        presenter.setInitialData(AuthorizationIdentifier(
            connectionID = viewModel1.connectionID,
            authorizationID = viewModel1.authorizationID
        ))
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

        presenter.setInitialData(AuthorizationIdentifier(connectionID = "", authorizationID = ""))
        presenter.currentViewModel = null
        Mockito.clearInvocations(mockView, mockPollingService, mockApiManager)
        presenter.biometricAuthFinished()

        Mockito.verifyNoMoreInteractions(mockView, mockPollingService, mockApiManager)
    }

    @Test
    @Throws(Exception::class)
    fun biometricsCanceledByUserTest() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(AuthorizationIdentifier(
            connectionID = viewModel1.connectionID,
            authorizationID = viewModel1.authorizationID
        ))
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
        presenter.setInitialData(AuthorizationIdentifier(
            connectionID = viewModel1.connectionID,
            authorizationID = viewModel1.authorizationID
        ))
        presenter.currentViewModel = viewModel1

        presenter.successAuthWithPasscode()

        Mockito.verify(mockApiManager).confirmAuthorization(
            connectionAndKey = ConnectionAndKey(connection1, mockPrivateKey),
            authorizationId = "1",
            authorizationCode = "111",
            resultCallback = presenter
        )
        Mockito.verify(mockPollingService).stop()
        Mockito.verify(mockView).setContentViewMode(
            ViewMode.CONFIRM_PROCESSING,
            ignoreTimeUpdate = ViewMode.CONFIRM_PROCESSING.showProgress
        )
    }

    @Test
    @Throws(Exception::class)
    fun setInitialDataTest() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(AuthorizationIdentifier("1", "1"))

        assertThat(presenter.currentViewModel, equalTo(AuthorizationViewModel(
            authorizationID = "",
            authorizationCode = "",
            title = "",
            description = "",
            validSeconds = 0,
            expiresAt = DateTime(0L),
            createdAt = DateTime(0L),
            connectionID = "",
            connectionName = "",
            connectionLogoUrl = "",
            viewMode = ViewMode.LOADING
        )))

        presenter.setInitialData(AuthorizationIdentifier("1", ""))

        assertThat(presenter.currentViewModel, equalTo(AuthorizationViewModel(
            authorizationID = "",
            authorizationCode = "",
            title = "",
            description = "",
            validSeconds = 0,
            expiresAt = DateTime(0L),
            createdAt = DateTime(0L),
            connectionID = "",
            connectionName = "",
            connectionLogoUrl = "",
            viewMode = ViewMode.UNAVAILABLE
        )))

        presenter.setInitialData(AuthorizationIdentifier("1", ""))

        assertThat(presenter.currentViewModel, equalTo(AuthorizationViewModel(
            authorizationID = "",
            authorizationCode = "",
            title = "",
            description = "",
            validSeconds = 0,
            expiresAt = DateTime(0L),
            createdAt = DateTime(0L),
            connectionID = "",
            connectionName = "",
            connectionLogoUrl = "",
            viewMode = ViewMode.UNAVAILABLE
        )))
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
        Mockito.doReturn(ConnectionAndKey(connection1, mockPrivateKey))
            .`when`(mockKeyStoreManager).createConnectionAndKeyModel(connection1)
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
    private val encryptedData1 = EncryptedData(id = "1", connectionId = "1")
    private val encryptedData2 = EncryptedData(id = "2_noKey", connectionId = "1")
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
