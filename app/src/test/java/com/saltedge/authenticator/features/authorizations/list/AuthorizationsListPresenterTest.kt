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
package com.saltedge.authenticator.features.authorizations.list

import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.authorizations.common.ViewMode
import com.saltedge.authenticator.features.authorizations.common.toAuthorizationViewModel
import com.saltedge.authenticator.model.db.Connection
import com.saltedge.authenticator.model.db.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.constants.ERROR_CLASS_CONNECTION_NOT_FOUND
import com.saltedge.authenticator.sdk.model.authorization.AuthorizationData
import com.saltedge.authenticator.sdk.model.authorization.EncryptedAuthorizationData
import com.saltedge.authenticator.sdk.model.connection.ConnectionAbs
import com.saltedge.authenticator.sdk.model.connection.ConnectionAndKey
import com.saltedge.authenticator.sdk.model.connection.ConnectionStatus
import com.saltedge.authenticator.sdk.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.model.error.createRequestError
import com.saltedge.authenticator.sdk.polling.PollingServiceAbs
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
import java.security.KeyPair
import java.security.PrivateKey

@RunWith(RobolectricTestRunner::class)
class AuthorizationsListPresenterTest {

    @Test
    @Throws(Exception::class)
    fun constructorTest() {
        Assert.assertNull(createPresenter(viewContract = null).viewContract)
        Assert.assertNotNull(createPresenter(viewContract = mockView).viewContract)
    }

    @Test
    @Throws(Exception::class)
    fun showEmptyViewTest() {
        val presenter = createPresenter(viewContract = mockView)

        Assert.assertTrue(presenter.showEmptyView)

        presenter.viewModels = listOf(viewModel1)

        Assert.assertFalse(presenter.showEmptyView)
    }

    @Test
    @Throws(Exception::class)
    fun showContentViewsTest() {
        val presenter = createPresenter(viewContract = mockView)

        Assert.assertFalse(presenter.showContentViews)

        presenter.viewModels = listOf(viewModel1)

        Assert.assertTrue(presenter.showContentViews)
    }

    @Test
    @Throws(Exception::class)
    fun onFragmentStartTest() {
        createPresenter(viewContract = mockView).onFragmentResume()

        Mockito.verify(mockView).updateViewsContent()
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTest_invalidParams() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.viewModels = listOf(viewModel1, viewModel2)

        Mockito.clearInvocations(mockApiManager)
        presenter.onListItemClick(itemIndex = 5, itemCode = "333", itemViewId = 1)

        Mockito.verifyNoMoreInteractions(mockApiManager)

        presenter.onListItemClick(itemIndex = 1, itemCode = "1", itemViewId = -1)

        Mockito.verifyNoMoreInteractions(mockApiManager)
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTest_negativeActionView() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.viewModels = listOf(viewModel1, viewModel2)

        Assert.assertNull(presenter.currentViewModel)
        assertThat(presenter.viewModels[0].viewMode, equalTo(ViewMode.DEFAULT))

        presenter.onListItemClick(
            itemIndex = 0,
            itemCode = "1",
            itemViewId = R.id.negativeActionView
        )

        Mockito.verify(mockApiManager).denyAuthorization(
            connectionAndKey = ConnectionAndKey(connection1, mockPrivateKey),
            authorizationId = viewModel1.authorizationID,
            authorizationCode = viewModel1.authorizationCode,
            resultCallback = presenter
        )
        Mockito.verify(mockPollingService).stop()
        assertThat(presenter.currentViewModel, equalTo(viewModel1))
        assertThat(presenter.viewModels[0].viewMode, equalTo(ViewMode.DENY_PROCESSING))
        Mockito.verify(mockView).updateItem(presenter.viewModels[0], 0)
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTest_positiveActionView() {
        Mockito.doReturn(true).`when`(mockBiometricTools).isBiometricReady(TestAppTools.applicationContext)
        val presenter = createPresenter(viewContract = mockView)
        presenter.viewModels = listOf(viewModel1, viewModel2)
        Mockito.clearInvocations(mockView, mockApiManager, mockPollingService)

        presenter.onListItemClick(
            itemIndex = 0,
            itemCode = viewModel1.authorizationID,
            itemViewId = R.id.positiveActionView
        )

        Mockito.verify(mockView).updateItem(
            viewModel = viewModel1,
            itemId = 0
        )

        Mockito.clearInvocations(mockView, mockApiManager, mockPollingService)

        Mockito.verifyNoMoreInteractions(mockApiManager, mockPollingService)
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTest_positiveActionView_invalidParams() {
        Mockito.doReturn(true).`when`(mockBiometricTools).isBiometricReady(TestAppTools.applicationContext)
        val presenter = createPresenter(viewContract = null)
        presenter.onFetchAuthorizationsResult(
            result = listOf(encryptedData1, encryptedData2),
            errors = emptyList()
        )
        Mockito.clearInvocations(mockApiManager, mockPollingService)
        presenter.onListItemClick(
            itemCode = viewModel1.authorizationID,
            itemViewId = R.id.positiveActionView
        )

        Mockito.verifyNoMoreInteractions(mockApiManager, mockPollingService)

        Mockito.doReturn(false).`when`(mockBiometricTools).isBiometricReady(TestAppTools.applicationContext)

        Mockito.clearInvocations(mockView, mockApiManager, mockPollingService)
        presenter.onListItemClick(
            itemIndex = 1,
            itemCode = viewModel1.authorizationID,
            itemViewId = R.id.positiveActionView
        )

        Mockito.verify(mockView, Mockito.never()).askUserPasscodeConfirmation()
    }

    @Test
    @Throws(Exception::class)
    fun getConnectionsDataTest_noConnections() {
        Mockito.doReturn(emptyList<Connection>()).`when`(mockConnectionsRepository).getAllActiveConnections()
        val presenter = createPresenter(viewContract = mockView)

        Assert.assertNull(presenter.getConnectionsData())
    }

    @Test
    @Throws(Exception::class)
    fun getConnectionsDataTest() {
        val presenter = createPresenter(viewContract = mockView)

        assertThat(
            presenter.getConnectionsData(),
            equalTo(listOf(ConnectionAndKey(connection1 as ConnectionAbs, mockPrivateKey)))
        )
    }

    @Test
    @Throws(Exception::class)
    fun onFetchAuthorizationsResultTest_processAuthorizationsErrors() {
        val presenter = createPresenter(viewContract = mockView)
        Mockito.clearInvocations(mockConnectionsRepository)
        presenter.onFetchAuthorizationsResult(
            result = emptyList(),
            errors = listOf(createRequestError(404))
        )

        Mockito.verifyNoMoreInteractions(mockConnectionsRepository)

        presenter.onFetchAuthorizationsResult(
            result = emptyList(),
            errors = listOf(ApiErrorData(errorClassName = ERROR_CLASS_CONNECTION_NOT_FOUND))
        )

        Mockito.verifyNoMoreInteractions(mockConnectionsRepository)

        presenter.onFetchAuthorizationsResult(
            result = emptyList(),
            errors = listOf(
                ApiErrorData(
                    errorClassName = ERROR_CLASS_CONNECTION_NOT_FOUND,
                    accessToken = "token"
                )
            )
        )

        Mockito.verify(mockConnectionsRepository).invalidateConnectionsByTokens(listOf("token"))
        Mockito.verify(mockConnectionsRepository).getAllActiveConnections()
    }

    //TODO INVESTIGATE TEST OF COROUTINES (https://github.com/saltedge/sca-authenticator-android/issues/39)
//    @Test
//    @Throws(Exception::class)
//    fun onFetchAuthorizationsResultTest_processEncryptedAuthorizationsResult() {
//
//    }

    /**
     * Received list (empty) different than stored
     */
    @Test
    @Throws(Exception::class)
    fun processDecryptedAuthorizationsResultTest_Case1() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.viewModels = listOf(viewModel1, viewModel2)

        presenter.processDecryptedAuthorizationsResult(result = emptyList())

        Mockito.verify(mockView).updateViewsContent()
        Assert.assertTrue(presenter.viewModels.isEmpty())
    }

    /**
     * Received list (not empty) different than stored
     */
    @Test
    @Throws(Exception::class)
    fun processDecryptedAuthorizationsResultTest_Case2() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.viewModels = emptyList()

        presenter.processDecryptedAuthorizationsResult(result = listOf(authorizationData1, authorizationData2))

        Mockito.verify(mockView).updateViewsContent()
        assertThat(presenter.viewModels, equalTo(listOf(viewModel1, viewModel2)))
    }

    /**
     * Received the same list
     */
    @Test
    @Throws(Exception::class)
    fun processDecryptedAuthorizationsResultTest_Case3() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.viewModels = listOf(viewModel1, viewModel2)

        presenter.processDecryptedAuthorizationsResult(result = listOf(authorizationData1, authorizationData2))

        Mockito.verifyNoMoreInteractions(mockView)
        assertThat(presenter.viewModels, equalTo(listOf(viewModel1, viewModel2)))
    }

    /**
     * Received the same list but one of the stored models has modified viewMode
     */
    @Test
    @Throws(Exception::class)
    fun processDecryptedAuthorizationsResultTest_Case4() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.viewModels = listOf(viewModel1, viewModel2.copy(viewMode = ViewMode.DENY_SUCCESS))
        presenter.processDecryptedAuthorizationsResult(result = listOf(authorizationData1, authorizationData2))

        assertThat(presenter.viewModels,
            equalTo(listOf(viewModel1, viewModel2.copy(viewMode = ViewMode.DENY_SUCCESS))))
        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenyFailureTest() {
        val presenter = createPresenter(viewContract = mockView)
        val error = createRequestError(404)
        presenter.onConfirmDenyFailure(error = error, connectionID = "333", authorizationID = "444")

        Mockito.verify(mockPollingService).start()
        Mockito.verify(mockView).showError(error)

        presenter.viewContract = null
        Mockito.clearInvocations(mockView, mockPollingService)
        presenter.onConfirmDenyFailure(error = error, connectionID = "333", authorizationID = "444")

        Mockito.verifyNoMoreInteractions(mockView)
        Mockito.verify(mockPollingService).start()
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenySuccessTest_ConfirmSuccess() {
        val presenter: AuthorizationsListPresenter = createPresenter(viewContract = mockView)
        presenter.viewModels = listOf(viewModel1.copy(), viewModel2.copy(viewMode = ViewMode.CONFIRM_PROCESSING))

        presenter.onConfirmDenySuccess(success = true, authorizationID = "2", connectionID = "1")

        assertThat(presenter.viewModels,
            equalTo(listOf(viewModel1, viewModel2.copy(viewMode = ViewMode.CONFIRM_SUCCESS))))
        Mockito.verify(mockView).updateItem(viewModel2.copy(viewMode = ViewMode.CONFIRM_SUCCESS), 1)
        Mockito.verify(mockPollingService).start()
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenySuccessTest_DenySuccess() {
        val presenter: AuthorizationsListPresenter = createPresenter(viewContract = mockView)
        presenter.viewModels = listOf(viewModel1.copy(), viewModel2.copy(viewMode = ViewMode.DENY_PROCESSING))

        presenter.onConfirmDenySuccess(success = true, authorizationID = "2", connectionID = "1")

        assertThat(presenter.viewModels,
            equalTo(listOf(viewModel1, viewModel2.copy(viewMode = ViewMode.DENY_SUCCESS))))
        Mockito.verify(mockView).updateItem(viewModel2.copy(viewMode = ViewMode.DENY_SUCCESS), 1)
        Mockito.verify(mockPollingService).start()
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenySuccessTest_invalidParams() {
        val presenter: AuthorizationsListPresenter = createPresenter(viewContract = mockView)
        presenter.onConfirmDenySuccess(authorizationID = "1", connectionID = "1", success = false)

        Mockito.verify(mockPollingService).start()
        Mockito.verifyNoMoreInteractions(mockView)

        Mockito.clearInvocations(mockView, mockPollingService)
        presenter.onConfirmDenySuccess(authorizationID = "1", connectionID = "1", success = true)

        Mockito.verify(mockPollingService).start()
        Mockito.verifyNoMoreInteractions(mockView)

        Mockito.clearInvocations(mockView, mockPollingService)
        presenter.viewContract = null
        presenter.onConfirmDenySuccess(authorizationID = "1", connectionID = "1", success = true)

        Mockito.verify(mockPollingService).start()
        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    @Throws(Exception::class)
    fun biometricAuthFinishedTest_invalidParams() {
        val presenter = createPresenter(viewContract = null)
        presenter.onFetchAuthorizationsResult(
            result = listOf(encryptedData1, encryptedData2),
            errors = emptyList()
        )
        presenter.biometricAuthFinished()

        Mockito.verifyNoMoreInteractions(mockView)

        presenter.biometricAuthFinished()

        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    @Throws(Exception::class)
    fun biometricAuthFinishedTest() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.viewModels = listOf(viewModel1, viewModel2)
        presenter.currentViewModel = viewModel1
        presenter.currentConnectionAndKey = connectionAndKey
        Mockito.clearInvocations(mockView, mockApiManager, mockPollingService)

        presenter.biometricAuthFinished()

        Mockito.verify(mockApiManager).confirmAuthorization(
            connectionAndKey = ConnectionAndKey(connection1, mockPrivateKey),
            authorizationId = "1",
            authorizationCode = "111",
            resultCallback = presenter
        )
        Mockito.verify(mockPollingService).stop()
        Mockito.verify(mockView).updateItem(
            viewModel = viewModel1.copy(viewMode = ViewMode.CONFIRM_PROCESSING),
            itemId = 0
        )
        assertThat(presenter.viewModels[0].viewMode, equalTo(ViewMode.CONFIRM_PROCESSING))
    }

    @Test
    @Throws(Exception::class)
    fun biometricsCanceledByUserTest() {
        val presenter = createPresenter(viewContract = mockView)
        Mockito.clearInvocations(mockView)
        presenter.biometricsCanceledByUser()

        Mockito.verify(mockView).askUserPasscodeConfirmation()

        presenter.viewContract = null
        Mockito.clearInvocations(mockView)
        presenter.biometricsCanceledByUser()

        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    @Throws(Exception::class)
    fun passcodeAuthFinishedTest() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.viewModels = listOf(viewModel1, viewModel2)
        presenter.currentViewModel = viewModel1
        presenter.currentConnectionAndKey = connectionAndKey
        Mockito.clearInvocations(mockView, mockApiManager, mockPollingService)
        presenter.successAuthWithPasscode()

        Mockito.verify(mockApiManager).confirmAuthorization(
            connectionAndKey = ConnectionAndKey(connection1, mockPrivateKey),
            authorizationId = "1",
            authorizationCode = "111",
            resultCallback = presenter
        )
        Mockito.verify(mockPollingService).stop()
        Mockito.verify(mockView).updateItem(
            viewModel = viewModel1,
            itemId = 0
        )
    }

    @Test
    @Throws(Exception::class)
    fun passcodePromptCanceledByUserTest() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.passcodePromptCanceledByUser()

        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Before
    fun setUp() {
        Mockito.doReturn(listOf(connection1)).`when`(mockConnectionsRepository).getAllActiveConnections()
        Mockito.doReturn(connection1).`when`(mockConnectionsRepository).getById("1")
        Mockito.doReturn(connection2).`when`(mockConnectionsRepository).getById("2_noKey")
        Mockito.doReturn(
            KeyPair(
                null,
                mockPrivateKey
            )
        ).`when`(mockKeyStoreManager).getKeyPair("guid1")
        Mockito.doReturn(null).`when`(mockKeyStoreManager).getKeyPair("guid2")
        Mockito.doReturn(authorizationData1).`when`(mockCryptoTools).decryptAuthorizationData(
            encryptedData = encryptedData1,
            rsaPrivateKey = mockPrivateKey
        )
        Mockito.doReturn(authorizationData2).`when`(mockCryptoTools).decryptAuthorizationData(
            encryptedData = encryptedData2,
            rsaPrivateKey = mockPrivateKey
        )
        Mockito.doReturn(mockPollingService).`when`(mockApiManager).createAuthorizationsPollingService()
    }

    private val mockKeyStoreManager = Mockito.mock(KeyStoreManagerAbs::class.java)
    private val mockConnectionsRepository = Mockito.mock(ConnectionsRepositoryAbs::class.java)
    private val mockBiometricTools = Mockito.mock(BiometricToolsAbs::class.java)
    private val mockCryptoTools = Mockito.mock(CryptoToolsAbs::class.java)
    private val mockApiManager = Mockito.mock(AuthenticatorApiManagerAbs::class.java)
    private val mockPollingService = Mockito.mock(PollingServiceAbs::class.java)
    private val mockView = Mockito.mock(AuthorizationsListContract.View::class.java)
    private val mockPrivateKey = Mockito.mock(PrivateKey::class.java)

    @Test
    @Throws(Exception::class)
    fun getValuesTest() {
        val presenter = createPresenter()

        assertThat(presenter.biometricTools, equalTo(mockBiometricTools))
        assertThat(presenter.apiManager, equalTo(mockApiManager))
    }

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
    private val connectionAndKey = ConnectionAndKey(connection1, mockPrivateKey)
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
    private val encryptedData2 = EncryptedAuthorizationData(id = "2", connectionId = "1")
    private val authorizationData1 = createAuthorization(id = 1)
    private val authorizationData2 = createAuthorization(id = 2)
    private val viewModel1 = authorizationData1.toAuthorizationViewModel(connection1)
    private val viewModel2 = authorizationData2.toAuthorizationViewModel(connection1)

    private fun createPresenter(viewContract: AuthorizationsListContract.View? = null): AuthorizationsListPresenter {
        return AuthorizationsListPresenter(
            appContext = TestAppTools.applicationContext,
            connectionsRepository = mockConnectionsRepository,
            keyStoreManager = mockKeyStoreManager,
            biometricTools = mockBiometricTools,
            cryptoTools = mockCryptoTools,
            apiManager = mockApiManager
        ).apply { this.viewContract = viewContract }
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
