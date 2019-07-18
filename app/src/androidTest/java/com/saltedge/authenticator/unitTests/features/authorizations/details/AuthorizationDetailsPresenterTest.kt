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
package com.saltedge.authenticator.unitTests.features.authorizations.details

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.authorizations.common.remainedSecondsTillExpire
import com.saltedge.authenticator.features.authorizations.common.remainedTimeTillExpire
import com.saltedge.authenticator.features.authorizations.common.toAuthorizationViewModel
import com.saltedge.authenticator.features.authorizations.details.AuthorizationDetailsContract
import com.saltedge.authenticator.features.authorizations.details.AuthorizationDetailsPresenter
import com.saltedge.authenticator.model.db.Connection
import com.saltedge.authenticator.model.db.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.constants.ERROR_CLASS_CONNECTION_NOT_FOUND
import com.saltedge.authenticator.sdk.model.*
import com.saltedge.authenticator.sdk.model.response.ConfirmDenyResultData
import com.saltedge.authenticator.sdk.polling.SingleAuthorizationPollingService
import com.saltedge.authenticator.sdk.tools.CryptoToolsAbs
import com.saltedge.authenticator.sdk.tools.KeyStoreManagerAbs
import com.saltedge.authenticator.testTools.*
import com.saltedge.authenticator.tool.secure.fingerprint.BiometricToolsAbs
import org.hamcrest.CoreMatchers.anyOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import java.security.KeyPair
import java.security.PrivateKey

@RunWith(AndroidJUnit4::class)
class AuthorizationDetailsPresenterTest {

    @Test
    @Throws(Exception::class)
    fun constructorTest() {
        Assert.assertNull(createPresenter(viewContract = null).viewContract)
        Assert.assertNotNull(createPresenter(viewContract = mockView).viewContract)
    }

    @Test
    @Throws(Exception::class)
    fun remainedTimeDescriptionTest() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = "1", authorizationId = "",
                viewModel = null, quickConfirmMode = false)

        assertThat(presenter.remainedTimeDescription, equalTo(""))

        val authorizationData = createAuthorizationData(1)
        presenter.setInitialData(connectionId = "1", authorizationId = "",
                viewModel = authorizationData.toAuthorizationViewModel(connection1), quickConfirmMode = false)

        assertThat(presenter.remainedTimeDescription, anyOf(equalTo("1:00:00"), equalTo("59:59")))
    }

    @Test
    @Throws(Exception::class)
    fun remainedSecondsTillExpireTest() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = "", authorizationId = "",
                viewModel = null, quickConfirmMode = false)

        assertThat(presenter.remainedSecondsTillExpire, equalTo(0))

        val authorizationData = createAuthorizationData(1)
        presenter.setInitialData(connectionId = "", authorizationId = "",
                viewModel = authorizationData.toAuthorizationViewModel(connection1), quickConfirmMode = false)

        assertThat(presenter.remainedSecondsTillExpire, anyOf(equalTo(3600), equalTo(3599)))
    }

    @Test
    @Throws(Exception::class)
    fun maxProgressSecondsTest() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = "", authorizationId = "",
                viewModel = null, quickConfirmMode = false)

        assertThat(presenter.maxProgressSeconds, equalTo(0))

        val authorizationData = createAuthorizationData(1)
        val viewModel = authorizationData.toAuthorizationViewModel(connection1)
        presenter.setInitialData(connectionId = "", authorizationId = "",
                viewModel = viewModel, quickConfirmMode = false)

        assertThat(presenter.maxProgressSeconds, equalTo(viewModel.validSeconds))
    }

    @Test
    @Throws(Exception::class)
    fun providerNameTest() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = "", authorizationId = "",
                viewModel = null, quickConfirmMode = false)

        assertThat(presenter.providerName, equalTo(""))

        presenter.setInitialData(connectionId = "1", authorizationId = "",
                viewModel = viewModel1, quickConfirmMode = false)

        assertThat(presenter.providerName, equalTo("Demobank3"))
    }

    @Test
    @Throws(Exception::class)
    fun providerLogoTest() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = "", authorizationId = "",
                viewModel = null, quickConfirmMode = false)

        assertThat(presenter.providerLogo, equalTo(""))

        presenter.setInitialData(connectionId = "1", authorizationId = "",
                viewModel = viewModel1, quickConfirmMode = false)

        assertThat(presenter.providerLogo, equalTo("url"))
    }

    @Test
    @Throws(Exception::class)
    fun titleTest() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = "1", authorizationId = "",
                viewModel = null, quickConfirmMode = false)

        assertThat(presenter.title, equalTo(TestTools.getString(R.string.authorizations_fetching)))

        val authorizationData = createAuthorizationData(1)
        presenter.setInitialData(connectionId = "1", authorizationId = "",
                viewModel = authorizationData.toAuthorizationViewModel(connection1), quickConfirmMode = false)

        assertThat(presenter.title, equalTo("title1"))

        presenter.onViewClick(R.id.negativeActionView)

        assertThat(presenter.title, equalTo(TestTools.getString(R.string.authorizations_processing)))
    }

    @Test
    @Throws(Exception::class)
    fun descriptionTest() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = "1", authorizationId = "",
                viewModel = null, quickConfirmMode = false)

        assertThat(presenter.description, equalTo(""))

        val authorizationData = createAuthorizationData(1)
        presenter.setInitialData(connectionId = "1", authorizationId = "",
                viewModel = authorizationData.toAuthorizationViewModel(connection1), quickConfirmMode = false)

        assertThat(presenter.description, equalTo("desc1"))
    }

    @Test
    @Throws(Exception::class)
    fun shouldShowTimeViewTest() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = "1", authorizationId = "", viewModel = null, quickConfirmMode = false)

        Assert.assertFalse(presenter.shouldShowTimeView)

        val authorizationData = createAuthorizationData(1)
        presenter.setInitialData(connectionId = "1", authorizationId = "",
                viewModel = authorizationData.toAuthorizationViewModel(connection1), quickConfirmMode = false)

        Assert.assertTrue(presenter.shouldShowTimeView)

        presenter.onViewClick(R.id.negativeActionView)

        Assert.assertFalse(presenter.shouldShowTimeView)
    }

    @Test
    @Throws(Exception::class)
    fun shouldShowProgressViewTest() {
        val presenter = createPresenter(viewContract = mockView)
        val authorizationData = createAuthorizationData(1)
        presenter.setInitialData(connectionId = "1", authorizationId = "",
                viewModel = authorizationData.toAuthorizationViewModel(connection1), quickConfirmMode = false)

        Assert.assertFalse(presenter.shouldShowProgressView)

        presenter.onViewClick(R.id.negativeActionView)

        Assert.assertTrue(presenter.shouldShowProgressView)
    }

    @Test
    @Throws(Exception::class)
    fun shouldShowActionsLayoutTest() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = "1", authorizationId = "",
                viewModel = createAuthorizationData(1).toAuthorizationViewModel(connection1), quickConfirmMode = false)

        Assert.assertTrue(presenter.shouldShowActionsLayout)

        presenter.setInitialData(connectionId = "1", authorizationId = "",
                viewModel = createAuthorizationData(0).toAuthorizationViewModel(connection1), quickConfirmMode = false)

        Assert.assertFalse(presenter.shouldShowActionsLayout)

        presenter.onViewClick(R.id.negativeActionView)

        Assert.assertFalse(presenter.shouldShowActionsLayout)

        presenter.setInitialData(connectionId = "1", authorizationId = "",
                viewModel = createAuthorizationData(1).toAuthorizationViewModel(connection1), quickConfirmMode = false)

        Assert.assertFalse(presenter.shouldShowActionsLayout)
    }

    @Test
    @Throws(Exception::class)
    fun sessionIsNotExpiredTest() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = "1", authorizationId = "",
                viewModel = createAuthorizationData(0).toAuthorizationViewModel(connection1), quickConfirmMode = false)

        Assert.assertFalse(presenter.sessionIsNotExpired)

        presenter.setInitialData(connectionId = "1", authorizationId = "",
                viewModel = createAuthorizationData(1).toAuthorizationViewModel(connection1), quickConfirmMode = false)

        Assert.assertTrue(presenter.sessionIsNotExpired)
    }

    @Test
    @Throws(Exception::class)
    fun shouldShowProviderLogoTest() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = "1", authorizationId = "",
                viewModel = createAuthorizationData(0).toAuthorizationViewModel(connection1), quickConfirmMode = false)

        Assert.assertTrue(presenter.shouldShowProviderLogo)

        presenter.setInitialData(connectionId = "", authorizationId = "", viewModel = null, quickConfirmMode = false)

        Assert.assertFalse(presenter.shouldShowProviderLogo)
    }

    @Test
    @Throws(Exception::class)
    fun shouldShowDescriptionWebViewTest() {
        val presenter = createPresenter(viewContract = mockView)
        var viewModel = createAuthorizationData(0).apply {
            description = "<html><h1>Test</h1></html>"
        }.toAuthorizationViewModel(connection1)
        presenter.setInitialData(connectionId = "1", authorizationId = "", viewModel = viewModel, quickConfirmMode = false)

        Assert.assertTrue(presenter.shouldShowDescriptionWebView)

        viewModel = createAuthorizationData(0).apply {
            description = "Test"
        }.toAuthorizationViewModel(connection1)
        presenter.setInitialData(connectionId = "1", authorizationId = "", viewModel = viewModel, quickConfirmMode = false)

        Assert.assertFalse(presenter.shouldShowDescriptionWebView)
    }

    @Test
    @Throws(Exception::class)
    fun shouldShowDescriptionTextViewTest() {
        val presenter = createPresenter(viewContract = mockView)
        var viewModel = createAuthorizationData(0).apply {
            description = "<html><h1>Test</h1></html>"
        }.toAuthorizationViewModel(connection1)
        presenter.setInitialData(connectionId = "1", authorizationId = "", viewModel = viewModel, quickConfirmMode = false)

        Assert.assertFalse(presenter.shouldShowDescriptionTextView)

        viewModel = createAuthorizationData(0).apply {
            description = "Test"
        }.toAuthorizationViewModel(connection1)
        presenter.setInitialData(connectionId = "1", authorizationId = "", viewModel = viewModel, quickConfirmMode = false)

        Assert.assertTrue(presenter.shouldShowDescriptionTextView)
    }

    @Test
    @Throws(Exception::class)
    fun onViewResumeTest() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = "1", authorizationId = "1",
                viewModel = createAuthorizationData(0).toAuthorizationViewModel(connection1), quickConfirmMode = false)
        presenter.onViewResume()

        Mockito.verify(mockPollingService).start("1")
        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    @Throws(Exception::class)
    fun onViewResumeTest_RunTimer() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = "1", authorizationId = "",
                viewModel = createAuthorizationData(1).toAuthorizationViewModel(connection1), quickConfirmMode = false)
        presenter.onViewResume()

        Assert.assertTrue(presenter.sessionIsNotExpired)
        Mockito.verify(mockPollingService).start("1")
        Mockito.verify(mockView).startTimer()
    }

    @Test
    @Throws(Exception::class)
    fun onViewPauseTest() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = "1", authorizationId = "",
                viewModel = createAuthorizationData(0).toAuthorizationViewModel(connection1), quickConfirmMode = false)
        presenter.onViewPause()

        Mockito.verify(mockPollingService).stop()
        Mockito.verify(mockView).stopTimer()
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTest_negativeActionView() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = "1", authorizationId = "",
                viewModel = createAuthorizationData(1).toAuthorizationViewModel(connection1), quickConfirmMode = false)
        presenter.onViewClick(R.id.negativeActionView)

        Mockito.verify(mockApiManager).denyAuthorization(
                connectionAndKey = ConnectionAndKey(connection1, mockPrivateKey),
                authorizationId = "1",
                authorizationCode = "111",
                resultCallback = presenter
        )
        Mockito.verify(mockPollingService).stop()
        Mockito.verify(mockView).updateViewContent()
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTest_negativeActionView_InvalidParams() {
        val presenter = createPresenter(viewContract = null)
        presenter.setInitialData(connectionId = "1", authorizationId = "", viewModel = createAuthorizationData(1).toAuthorizationViewModel(connection1), quickConfirmMode = false)
        Mockito.clearInvocations(mockApiManager, mockPollingService, mockView)
        presenter.onViewClick(R.id.negativeActionView)

        Mockito.verifyNoMoreInteractions(mockView)

        presenter.viewContract = mockView
        Mockito.clearInvocations(mockApiManager, mockPollingService, mockView)
        presenter.onViewClick(-1)

        Mockito.verifyNoMoreInteractions(mockApiManager, mockPollingService, mockView)

        presenter.setInitialData(connectionId = "1", authorizationId = "", viewModel = null, quickConfirmMode = false)
        Mockito.clearInvocations(mockApiManager, mockPollingService, mockView)
        presenter.onViewClick(R.id.negativeActionView)

        Mockito.verifyNoMoreInteractions(mockApiManager, mockPollingService, mockView)

        presenter.setInitialData(connectionId = "2_noKey", authorizationId = "", viewModel = null, quickConfirmMode = false)
        Mockito.clearInvocations(mockApiManager, mockPollingService, mockView)
        presenter.onViewClick(R.id.negativeActionView)

        Mockito.verifyNoMoreInteractions(mockApiManager, mockPollingService, mockView)

        presenter.setInitialData(connectionId = "no_connections", authorizationId = "", viewModel = null, quickConfirmMode = false)
        Mockito.clearInvocations(mockApiManager, mockPollingService, mockView)
        presenter.onViewClick(R.id.negativeActionView)

        Mockito.verifyNoMoreInteractions(mockApiManager, mockPollingService, mockView)
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTest_positiveActionView() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = "1", authorizationId = "",
                viewModel = createAuthorizationData(1).toAuthorizationViewModel(connection1),
                quickConfirmMode = false)
        Mockito.doReturn(true).`when`(mockBiometricTools).isBiometricReady(TestTools.applicationContext)
        presenter.onViewClick(R.id.positiveActionView)

        Mockito.verify(mockView).askUserBiometricConfirmation()

        Mockito.doReturn(false).`when`(mockBiometricTools).isBiometricReady(TestTools.applicationContext)
        presenter.onViewClick(R.id.positiveActionView)

        Mockito.verify(mockView).askUserPasscodeConfirmation()

        presenter.setInitialData(connectionId = "1", authorizationId = "",
                viewModel = createAuthorizationData(1).toAuthorizationViewModel(connection1),
                quickConfirmMode = true)
        presenter.onViewClick(R.id.positiveActionView)

        Mockito.verify(mockApiManager).confirmAuthorization(
                connectionAndKey = ConnectionAndKey(connection1, mockPrivateKey),
                authorizationId = "1",
                authorizationCode = "111",
                resultCallback = presenter
        )
        Mockito.verify(mockPollingService).stop()
        Mockito.verify(mockView).updateViewContent()
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTest_positiveActionView_InvalidParams() {
        val presenter = createPresenter(viewContract = null)
        presenter.setInitialData(connectionId = "1", authorizationId = "",
                viewModel = createAuthorizationData(1).toAuthorizationViewModel(connection1),
                quickConfirmMode = false)

        Mockito.doReturn(true).`when`(mockBiometricTools).isBiometricReady(TestTools.applicationContext)
        presenter.onViewClick(R.id.positiveActionView)

        Mockito.verify(mockView, Mockito.never()).askUserBiometricConfirmation()

        Mockito.doReturn(false).`when`(mockBiometricTools).isBiometricReady(TestTools.applicationContext)
        presenter.onViewClick(R.id.positiveActionView)

        Mockito.verify(mockView, Mockito.never()).askUserPasscodeConfirmation()
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTest_closeActionView() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = "1", authorizationId = "", viewModel = createAuthorizationData(1).toAuthorizationViewModel(connection1), quickConfirmMode = false)
        presenter.onViewClick(R.id.closeActionView)

        Mockito.verify(mockView).closeView()
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTest_closeActionView_InvalidParams() {
        val presenter = createPresenter(viewContract = null)
        presenter.setInitialData(connectionId = "1", authorizationId = "", viewModel = createAuthorizationData(1).toAuthorizationViewModel(connection1), quickConfirmMode = false)
        presenter.onViewClick(R.id.closeActionView)

        Mockito.verify(mockView, Mockito.never()).closeView()
    }

    @Test
    @Throws(Exception::class)
    fun onTimerTickTest_notRemainedTime() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = "1", authorizationId = "", viewModel = null, quickConfirmMode = false)
        Mockito.clearInvocations(mockView)
        presenter.onTimerTick()

        Mockito.verify(mockView).closeView()
    }

    @Test
    @Throws(Exception::class)
    fun onTimerTickTest_notRemainedTime_InvalidParams() {
        val presenter = createPresenter(viewContract = null)
        presenter.setInitialData(connectionId = "1", authorizationId = "", viewModel = null, quickConfirmMode = false)
        presenter.onTimerTick()

        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    @Throws(Exception::class)
    fun onTimerTickTest_remainedTime() {
        val presenter = createPresenter(viewContract = mockView)
        val authData = createAuthorizationData(1)
        val viewModel = authData.toAuthorizationViewModel(connection1)
        presenter.setInitialData(connectionId = "1", authorizationId = "",
                viewModel = viewModel, quickConfirmMode = false)
        Mockito.clearInvocations(mockView)
        presenter.onTimerTick()

        Mockito.verify(mockView).updateTimeView(
                viewModel.remainedSecondsTillExpire(),
                viewModel.remainedTimeTillExpire())
    }

    @Test
    @Throws(Exception::class)
    fun onTimerTickTest_remainedTime_InvalidParams() {
        val presenter = createPresenter(viewContract = null)
        val viewModel = createAuthorizationData(1).toAuthorizationViewModel(connection1)
        presenter.setInitialData(connectionId = "1", authorizationId = "",
                viewModel = viewModel, quickConfirmMode = false)
        presenter.onTimerTick()

        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    @Throws(Exception::class)
    fun getRequestDataTest() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = "", authorizationId = null,
                viewModel = null, quickConfirmMode = false)

        Assert.assertNull(presenter.getConnectionData())

        presenter.setInitialData(connectionId = "2_noKey", authorizationId = null,
                viewModel = null, quickConfirmMode = false)

        Assert.assertNull(presenter.getConnectionData())

        presenter.setInitialData(connectionId = "1", authorizationId = "2_noKey", viewModel = null, quickConfirmMode = false)

        assertThat(presenter.getConnectionData(), equalTo(ConnectionAndKey(connection1 as ConnectionAbs, mockPrivateKey)))

        presenter.setInitialData(connectionId = "1", authorizationId = "",
                viewModel = createAuthorizationData(1).toAuthorizationViewModel(connection1), quickConfirmMode = false)

        assertThat(presenter.getConnectionData(), equalTo(ConnectionAndKey(connection1 as ConnectionAbs, mockPrivateKey)))
    }

    @Test
    @Throws(Exception::class)
    fun fetchAuthorizationResultTest_processAuthorizationResult() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.fetchAuthorizationResult(result = encryptedData1, error = null)

        Mockito.never()

        presenter.setInitialData(connectionId = "1", authorizationId = "", viewModel = null, quickConfirmMode = false)

        assertThat(presenter.description, equalTo(""))

        Mockito.clearInvocations(mockView)
        presenter.fetchAuthorizationResult(result = encryptedData1, error = null)

        Mockito.verify(mockView).updateViewContent()
        Mockito.verify(mockView).startTimer()
        assertThat(presenter.description, equalTo("desc1"))

        Mockito.clearInvocations(mockView)
        presenter.fetchAuthorizationResult(result = encryptedData1, error = null)

        Mockito.verifyNoMoreInteractions(mockView)
        assertThat(presenter.description, equalTo("desc1"))
    }

    @Test
    @Throws(Exception::class)
    fun fetchAuthorizationResultTest_processAuthorizationResult_InvalidParams() {
        val presenter = createPresenter(viewContract = null)
        presenter.fetchAuthorizationResult(result = null, error = null)

        Mockito.never()

        presenter.setInitialData(connectionId = "1", authorizationId = "", viewModel = null, quickConfirmMode = false)
        Mockito.clearInvocations(mockView)
        presenter.fetchAuthorizationResult(result = encryptedData1, error = null)

        Mockito.verify(mockView, Mockito.never()).updateViewContent()
    }

    @Test
    @Throws(Exception::class)
    fun fetchAuthorizationResultTest_processAuthorizationError() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = "1", authorizationId = "", viewModel = null, quickConfirmMode = false)
        Mockito.clearInvocations(mockView)
        presenter.fetchAuthorizationResult(result = null, error = createRequestError(404))

        Mockito.verifyNoMoreInteractions(mockView)

        presenter.fetchAuthorizationResult(result = null, error = ApiErrorData(errorClassName = ERROR_CLASS_CONNECTION_NOT_FOUND))

        Mockito.verify(mockView).closeView()
        Mockito.verify(mockConnectionsRepository).invalidateConnectionsByTokens(accessTokens = listOf("token1"))

        presenter.setInitialData(connectionId = "X", authorizationId = "", viewModel = null, quickConfirmMode = false)
        Mockito.clearInvocations(mockView, mockConnectionsRepository)
        presenter.fetchAuthorizationResult(result = null, error = ApiErrorData(errorClassName = ERROR_CLASS_CONNECTION_NOT_FOUND))

        Mockito.verifyNoMoreInteractions(mockView, mockConnectionsRepository)

        presenter.viewContract = null
        presenter.setInitialData(connectionId = "1", authorizationId = "", viewModel = null, quickConfirmMode = false)
        Mockito.clearInvocations(mockView, mockConnectionsRepository)
        presenter.fetchAuthorizationResult(result = null, error = ApiErrorData(errorClassName = ERROR_CLASS_CONNECTION_NOT_FOUND))

        Mockito.verifyNoMoreInteractions(mockView)
        Mockito.verify(mockConnectionsRepository).invalidateConnectionsByTokens(accessTokens = listOf("token1"))
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenyFailureTest() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = "1", authorizationId = "", viewModel = null, quickConfirmMode = false)
        presenter.onConfirmDenyFailure(error = createRequestError(404))

        Mockito.verifyNoMoreInteractions(mockPollingService)
        Mockito.verify(mockView).updateViewContent()

        presenter.setInitialData(connectionId = "1", authorizationId = "1", viewModel = null, quickConfirmMode = false)
        Mockito.clearInvocations(mockView, mockPollingService)
        presenter.onConfirmDenyFailure(error = createRequestError(404))

        Mockito.verify(mockPollingService).start("1")
        Mockito.verify(mockView).updateViewContent()

        presenter.viewContract = null
        Mockito.clearInvocations(mockView, mockPollingService)
        presenter.onConfirmDenyFailure(error = createRequestError(404))

        Mockito.verify(mockPollingService).start("1")
        Mockito.verify(mockView, Mockito.never()).updateViewContent()
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenySuccessTest() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = "1", authorizationId = "",
                viewModel = authorizationData1.toAuthorizationViewModel(connection1), quickConfirmMode = false)
        Mockito.clearInvocations(mockView, mockPollingService)
        presenter.onConfirmDenySuccess(result = ConfirmDenyResultData())

        Mockito.verify(mockView).updateViewContent()
        Mockito.verify(mockPollingService).start("")

        Mockito.clearInvocations(mockView, mockPollingService)
        presenter.onConfirmDenySuccess(result = ConfirmDenyResultData(authorizationId = "1", success = true))

        Mockito.verify(mockView).closeViewWithSuccessResult(authorizationId = "1")

        presenter.viewContract = null
        Mockito.clearInvocations(mockView)
        presenter.onConfirmDenySuccess(result = ConfirmDenyResultData(authorizationId = "1", success = true))

        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    @Throws(Exception::class)
    fun biometricAuthFinishedTest() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = "1", authorizationId = "",
                viewModel = createAuthorizationData(1).toAuthorizationViewModel(connection1), quickConfirmMode = false)
        presenter.biometricAuthFinished()

        Mockito.verify(mockPollingService).stop()
        Mockito.verify(mockApiManager).confirmAuthorization(
                connectionAndKey = ConnectionAndKey(connection1, mockPrivateKey),
                authorizationId = "1",
                authorizationCode = "111",
                resultCallback = presenter
        )
        Mockito.verify(mockView).updateViewContent()
    }

    @Test
    @Throws(Exception::class)
    fun biometricAuthFinishedTest_InvalidParams() {
        val presenter = createPresenter(viewContract = null)
        presenter.setInitialData(connectionId = "1", authorizationId = "",
                viewModel = createAuthorizationData(1).toAuthorizationViewModel(connection1),
                quickConfirmMode = false)
        Mockito.clearInvocations(mockView, mockPollingService, mockApiManager)
        presenter.biometricAuthFinished()

        Mockito.verify(mockApiManager).confirmAuthorization(
                connectionAndKey = ConnectionAndKey(connection1, mockPrivateKey),
                authorizationId = "1",
                authorizationCode = "111",
                resultCallback = presenter
        )
        Mockito.verify(mockPollingService).stop()
        Mockito.verifyNoMoreInteractions(mockView)

        presenter.setInitialData(connectionId = "1", authorizationId = null,
                viewModel = null, quickConfirmMode = false)
        Mockito.clearInvocations(mockView, mockPollingService, mockApiManager)
        presenter.biometricAuthFinished()

        Mockito.verifyNoMoreInteractions(mockView, mockPollingService, mockApiManager)

        presenter.setInitialData(connectionId = "2_noKey", authorizationId = null,
                viewModel = null, quickConfirmMode = false)
        Mockito.clearInvocations(mockView, mockPollingService, mockApiManager)
        presenter.biometricAuthFinished()

        Mockito.verifyNoMoreInteractions(mockView, mockPollingService, mockApiManager)

        presenter.setInitialData(connectionId = "no_connection", authorizationId = null,
                viewModel = null, quickConfirmMode = false)
        Mockito.clearInvocations(mockView, mockPollingService, mockApiManager)
        presenter.biometricAuthFinished()

        Mockito.verifyNoMoreInteractions(mockView, mockPollingService, mockApiManager)
    }

    @Test
    @Throws(Exception::class)
    fun biometricsCanceledByUserTest() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(connectionId = "1", authorizationId = "",
                viewModel = createAuthorizationData(1).toAuthorizationViewModel(connection1),
                quickConfirmMode = false)
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
        presenter.setInitialData(connectionId = "1", authorizationId = "",
                viewModel = createAuthorizationData(1).toAuthorizationViewModel(connection1), quickConfirmMode = false)
        presenter.successAuthWithPasscode()

        Mockito.verify(mockApiManager).confirmAuthorization(
                connectionAndKey = ConnectionAndKey(connection1, mockPrivateKey),
                authorizationId = "1",
                authorizationCode = "111",
                resultCallback = presenter
        )
        Mockito.verify(mockPollingService).stop()
        Mockito.verify(mockView).updateViewContent()
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
                .decryptAuthorizationData(encryptedData = encryptedData1, rsaPrivateKey = mockPrivateKey)
        Mockito.doReturn(authorizationData2).`when`(mockCryptoTools)
                .decryptAuthorizationData(encryptedData = encryptedData2, rsaPrivateKey = mockPrivateKey)
    }

    private val mockKeyStoreManager = Mockito.mock(KeyStoreManagerAbs::class.java)
    private val mockConnectionsRepository = Mockito.mock(ConnectionsRepositoryAbs::class.java)
    private val mockBiometricTools = Mockito.mock(BiometricToolsAbs::class.java)
    private val mockCryptoTools = Mockito.mock(CryptoToolsAbs::class.java)
    private val mockApiManager = Mockito.mock(AuthenticatorApiManagerAbs::class.java)
    private val mockPollingService = Mockito.mock(SingleAuthorizationPollingService::class.java)
    private val mockView = Mockito.mock(AuthorizationDetailsContract.View::class.java)
    private val mockPrivateKey = Mockito.mock(PrivateKey::class.java)

    private val connection1 = Connection().setGuid("guid1").setId("1")
            .setCode("demobank3").setName("Demobank3")
            .setStatus(ConnectionStatus.ACTIVE).setAccessToken("token1").setLogoUrl("url")
            .setCreatedAt(200L).setUpdatedAt(200L)
    private val connection2 = Connection().setGuid("guid2").setId("2_noKey")
            .setCode("demobank2").setName("Demobank2")
            .setStatus(ConnectionStatus.ACTIVE).setAccessToken("").setLogoUrl("")
            .setCreatedAt(300L).setUpdatedAt(300L)
    private val encryptedData1 = EncryptedAuthorizationData(id = "1", connectionId = "1")
    private val encryptedData2 = EncryptedAuthorizationData(id = "2_noKey", connectionId = "1")
    private val authorizationData1 = createAuthorizationData(id = 1)
    private val authorizationData2 = createAuthorizationData(id = 2)
    private val viewModel1 = authorizationData1.toAuthorizationViewModel(connection1)

    private fun createPresenter(viewContract: AuthorizationDetailsContract.View? = null):
            AuthorizationDetailsPresenter {
        return AuthorizationDetailsPresenter(
                appContext = TestTools.applicationContext,
                connectionsRepository = mockConnectionsRepository,
                keyStoreManager = mockKeyStoreManager,
                biometricTools = mockBiometricTools,
                cryptoTools = mockCryptoTools,
                apiManager = mockApiManager).apply { this.viewContract = viewContract }
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
