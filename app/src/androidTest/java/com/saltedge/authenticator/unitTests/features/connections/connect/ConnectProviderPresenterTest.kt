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
package com.saltedge.authenticator.unitTests.features.connections.connect

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.connections.connect.ConnectProviderContract
import com.saltedge.authenticator.features.connections.connect.ConnectProviderPresenter
import com.saltedge.authenticator.features.connections.connect.ViewMode
import com.saltedge.authenticator.model.db.Connection
import com.saltedge.authenticator.model.db.ConnectionsRepositoryAbs
import com.saltedge.authenticator.model.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.constants.API_VERSION
import com.saltedge.authenticator.sdk.constants.ERROR_CLASS_API_RESPONSE
import com.saltedge.authenticator.sdk.model.ApiErrorData
import com.saltedge.authenticator.sdk.model.ConnectionStatus
import com.saltedge.authenticator.sdk.model.ProviderData
import com.saltedge.authenticator.sdk.model.response.AuthenticateConnectionData
import com.saltedge.authenticator.sdk.tools.KeyStoreManagerAbs
import com.saltedge.authenticator.testTools.*
import com.saltedge.authenticator.testTools.TestTools.getString
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
class ConnectProviderPresenterTest {

    @Before
    @Throws(Exception::class)
    fun setUp() {
        TestTools.setLocale("en")
    }

    @Test
    @Throws(Exception::class)
    fun constructorTest() {
        val presenter = createPresenter(viewContract = null)

        assertNull(presenter.viewContract)

        presenter.viewContract = mockView

        assertNotNull(presenter.viewContract)
    }

    /**
     * Test onConnectionInitFailure() when you get an error
     */
    @Test
    @Throws(Exception::class)
    fun onConnectionInitFailureTest() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.onConnectionInitFailure(
            ApiErrorData(
                errorMessage = "test error",
                errorClassName = ERROR_CLASS_API_RESPONSE
            )
        )

        Mockito.verify(mockView).showErrorAndFinish("test error")
    }

    @Test
    @Throws(Exception::class)
    fun onConnectionInitSuccessTestCase1() {
        val presenter = createPresenter(viewContract = mockView)
        val connectUrlData = AuthenticateConnectionData(
            redirectUrl = "https://www.fentury.com",
            connectionId = "connectionId"
        )
        presenter.onConnectionInitSuccess(response = connectUrlData)

        Mockito.verify(mockView).loadUrlInWebView("https://www.fentury.com")
        Mockito.verify(mockView).updateViewsContent()
    }

    @Test
    @Throws(Exception::class)
    fun onConnectionInitSuccessTestCase2() {
        val presenter = createPresenter(viewContract = mockView)
        val connectUrlData = AuthenticateConnectionData(
            redirectUrl = "invalidUrl",
            connectionId = "connectionId"
        )
        presenter.onConnectionInitSuccess(response = connectUrlData)

        Mockito.never()
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTest() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(
            connectConfigurationLink = "connectConfigurationLink",
            connectionGuid = "guid1"
        )

        presenter.onViewClick(R.id.mainActionView)

        Mockito.verify(mockView).closeView()
    }

    @Test
    @Throws(Exception::class)
    fun enumClassTest() {
        assertThat(
            ViewMode.values(), equalTo(
            arrayOf(
                ViewMode.START,
                ViewMode.WEB_ENROLL,
                ViewMode.COMPLETE_SUCCESS,
                ViewMode.COMPLETE_ERROR
            )
        )
        )
        assertThat(ViewMode.valueOf("START"), equalTo(ViewMode.START))
        assertThat(ViewMode.valueOf("WEB_ENROLL"), equalTo(ViewMode.WEB_ENROLL))
        assertThat(ViewMode.valueOf("COMPLETE_SUCCESS"), equalTo(ViewMode.COMPLETE_SUCCESS))
        assertThat(
            ViewMode.valueOf("COMPLETE_ERROR"),
            equalTo(ViewMode.COMPLETE_ERROR)
        )
    }

    /**
     * Test onDestroyView() when guid is not empty and accessToken is empty
     */
    @Test
    @Throws(Exception::class)
    fun onDestroyViewTestCase1() {
        val connection =
            Connection().setGuid("guid2").setAccessToken("").setCode("demobank1").setName("Demobank1")
        Mockito.`when`(mockConnectionsRepository.getByGuid("guid2")).thenReturn(connection)
        val presenter = createPresenter()
        presenter.setInitialData(connectConfigurationLink = null, connectionGuid = "guid2")
        presenter.onDestroyView()

        Mockito.verify(mockKeyStoreManager).deleteKeyPair("guid2")
    }

    /**
     * Test onDestroyView() when guid and accessToken is not empty
     */
    @Test
    @Throws(Exception::class)
    fun onDestroyViewTestCase2() {
        val connection =
            Connection().setGuid("guid2").setAccessToken("accessToken").setCode("demobank1").setName(
                "Demobank1"
            )
        Mockito.`when`(mockConnectionsRepository.getByGuid("guid2")).thenReturn(connection)
        val presenter = createPresenter()
        presenter.setInitialData(connectConfigurationLink = null, connectionGuid = "")
        presenter.onDestroyView()

        Mockito.never()
    }

    /**
     * Test onDestroyView() when guid and accessToken are empty
     */
    @Test
    @Throws(Exception::class)
    fun onDestroyViewTestCase3() {
        val connection =
            Connection().setGuid("").setAccessToken("").setCode("demobank1").setName("Demobank1")
        Mockito.`when`(mockConnectionsRepository.getByGuid("guid2")).thenReturn(connection)
        val presenter = createPresenter()
        presenter.setInitialData(connectConfigurationLink = null, connectionGuid = "guid2")
        presenter.onDestroyView()

        Mockito.never()
    }

    /**
     * Test onDestroyView() when accessToken is not empty and guid is empty
     */
    @Test
    @Throws(Exception::class)
    fun onDestroyViewTestCase4() {
        val connection =
            Connection().setGuid("").setAccessToken("accessToken").setCode("demobank1").setName("Demobank1")
        Mockito.`when`(mockConnectionsRepository.getByGuid("guid2")).thenReturn(connection)
        val presenter = createPresenter()
        presenter.setInitialData(connectConfigurationLink = null, connectionGuid = "guid2")
        presenter.onDestroyView()

        Mockito.never()
    }

    @Test
    @Throws(Exception::class)
    fun getLogoUrlTest() {
        val presenter = createPresenter(viewContract = mockView)

        assertTrue(presenter.logoUrl.isEmpty())

        presenter.setInitialData(
            connectConfigurationLink = "connectConfigurationLink",
            connectionGuid = "guid1"
        )

        assertTrue(presenter.logoUrl.isEmpty())

        presenter.fetchProviderResult(
            result = ProviderData(
                connectUrl = "https://www.fentury.com",
                code = "demobank",
                name = "Demobank",
                logoUrl = "logoUrl",
                version = API_VERSION,
                supportEmail = "example@saltedge.com"
            ),
            error = null
        )

        assertThat(presenter.logoUrl, equalTo("logoUrl"))
    }

    @Test
    @Throws(Exception::class)
    fun webAuthFinishErrorTest() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.webAuthFinishError(errorClass = "WRONG_URL", errorMessage = "not relevant url")

        Mockito.verify(mockView).updateViewsContent()
    }

    /**
     * When access_token is not null or empty
     */
    @Test
    @Throws(Exception::class)
    fun webAuthFinishSuccessTest() {
        val presenter = createPresenter(viewContract = mockView)
        val connection = Connection().setGuid("guid1").setStatus(ConnectionStatus.ACTIVE)
            .setAccessToken("access_token").setCode("demobank1").setName("Demobank1")
        Mockito.doReturn(connection).`when`(mockConnectionsRepository).getByGuid("guid1")

        presenter.setInitialData(connectConfigurationLink = null, connectionGuid = "guid1")

        Mockito.doReturn(1L).`when`(mockConnectionsRepository).getConnectionsCount("demobank1")
        Mockito.clearInvocations(mockView, mockConnectionsRepository)

        presenter.webAuthFinishSuccess(id = "1", accessToken = "access_token")

        Mockito.verify(mockView).updateViewsContent()
        Mockito.verify(mockConnectionsRepository).fixNameAndSave(connection)
    }

    @Test
    @Throws(Exception::class)
    fun shouldShowWebViewTest() {
        val presenter = createPresenter(viewContract = mockView)

        assertFalse(presenter.shouldShowWebView)

        val connectUrlData = AuthenticateConnectionData(
            redirectUrl = "https://www.fentury.com",
            connectionId = "connectionId"
        )
        presenter.onConnectionInitSuccess(response = connectUrlData)

        assertTrue(presenter.shouldShowWebView)
    }

    /**
     * Test iconResId when isCompleteWithSuccess is false
     */
    @Test
    @Throws(Exception::class)
    fun getIconResIdTestCase1() {
        val presenter = createPresenter(viewContract = mockView)

        assertThat(presenter.iconResId, equalTo(R.drawable.ic_complete_error_70))
    }

    /**
     * Test iconResId when isCompleteWithSuccess is true
     */
    @Test
    @Throws(Exception::class)
    fun getIconResIdTestCase2() {
        val presenter = createPresenter(viewContract = mockView)
        val connection = Connection().setGuid("guid1").setStatus(ConnectionStatus.ACTIVE)
            .setAccessToken("access_token").setCode("demobank1").setName("Demobank1")
        Mockito.doReturn(connection).`when`(mockConnectionsRepository).getByGuid("guid1")

        presenter.setInitialData(connectConfigurationLink = null, connectionGuid = "guid1")

        Mockito.doReturn(1L).`when`(mockConnectionsRepository).getConnectionsCount("demobank1")

        presenter.webAuthFinishSuccess(id = "1", accessToken = "access_token")

        assertThat(presenter.iconResId, equalTo(R.drawable.ic_complete_ok_70))
    }

    /**
     * Test mainActionTextResId when isCompleteWithSuccess is false
     */
    @Test
    @Throws(Exception::class)
    fun mainActionTextResIdCase1() {
        val presenter = createPresenter(viewContract = mockView)

        assertThat(presenter.mainActionTextResId, equalTo(R.string.actions_try_again))
    }

    /**
     * Test mainActionTextResId when isCompleteWithSuccess is true
     */
    @Test
    @Throws(Exception::class)
    fun mainActionTextResIdCase2() {
        val presenter = createPresenter(viewContract = mockView)
        val connection = Connection().setGuid("guid1").setStatus(ConnectionStatus.ACTIVE)
            .setAccessToken("access_token").setCode("demobank1").setName("Demobank1")
        Mockito.doReturn(connection).`when`(mockConnectionsRepository).getByGuid("guid1")

        presenter.setInitialData(connectConfigurationLink = null, connectionGuid = "guid1")

        Mockito.doReturn(1L).`when`(mockConnectionsRepository).getConnectionsCount("demobank1")

        presenter.webAuthFinishSuccess(id = "1", accessToken = "access_token")

        assertThat(presenter.mainActionTextResId, equalTo(R.string.actions_proceed))
    }

    /**
     * Test altActionTextResId when isCompleteWithSuccess is false
     */
    @Test
    @Throws(Exception::class)
    fun getAltActionTextResIdCase1() {
        val presenter = createPresenter(viewContract = mockView)

        assertThat(presenter.reportProblemActionText, equalTo(R.string.actions_contact_support))
    }

    /**
     * Test altActionTextResId when isCompleteWithSuccess is true
     */
    @Test
    @Throws(Exception::class)
    fun getAltActionTextResIdCase2() {
        val presenter = createPresenter(viewContract = mockView)
        val connection = Connection().setGuid("guid1").setStatus(ConnectionStatus.ACTIVE)
            .setAccessToken("access_token").setCode("demobank1").setName("Demobank1")
        Mockito.doReturn(connection).`when`(mockConnectionsRepository).getByGuid("guid1")

        presenter.setInitialData(connectConfigurationLink = null, connectionGuid = "guid1")

        Mockito.doReturn(1L).`when`(mockConnectionsRepository).getConnectionsCount("demobank1")

        presenter.webAuthFinishSuccess(id = "1", accessToken = "access_token")

        assertNull(presenter.reportProblemActionText)
    }

    /**
     * Test completeTitle when isCompleteWithSuccess is false
     */
    @Test
    @Throws(Exception::class)
    fun getCompleteTitleCase1() {
        val presenter = createPresenter(viewContract = mockView)

        assertThat(
            presenter.completeTitle,
            equalTo(getString(R.string.errors_connection_failed))
        )
    }

    /**
     * Test completeTitle when isCompleteWithSuccess is true
     */
    @Test
    @Throws(Exception::class)
    fun getCompleteTitleCase2() {
        val presenter = createPresenter(viewContract = mockView)
        val connection = Connection().setGuid("guid1").setStatus(ConnectionStatus.ACTIVE)
            .setAccessToken("access_token").setCode("demobank1").setName("Demobank1")
        Mockito.doReturn(connection).`when`(mockConnectionsRepository).getByGuid("guid1")

        presenter.setInitialData(connectConfigurationLink = null, connectionGuid = "guid1")

        Mockito.doReturn(1L).`when`(mockConnectionsRepository).getConnectionsCount("demobank1")

        presenter.webAuthFinishSuccess(id = "1", accessToken = "access_token")

        assertThat(
            presenter.completeTitle,
            equalTo(getString(R.string.connect_status_provider_success).format(connection.name))
        )
    }

    /**
     * Test completeMessage when isCompleteWithSuccess is false
     */
    @Test
    @Throws(Exception::class)
    fun getCompleteMessageCase1() {
        val presenter = createPresenter(viewContract = mockView)

        assertThat(
            presenter.completeMessage,
            equalTo(getString(R.string.errors_connection_failed_description))
        )

        val connection = Connection().setGuid("guid1").setStatus(ConnectionStatus.ACTIVE)
            .setAccessToken("access_token").setCode("demobank1").setName("Demobank1")
        Mockito.doReturn(connection).`when`(mockConnectionsRepository).getByGuid("guid1")

        presenter.setInitialData(connectConfigurationLink = null, connectionGuid = "guid1")

        Mockito.doReturn(1L).`when`(mockConnectionsRepository).getConnectionsCount("demobank1")

        presenter.webAuthFinishError(errorClass = "ERROR", errorMessage = null)

        assertThat(
            presenter.completeMessage,
            equalTo(getString(R.string.errors_connection_failed_description))
        )
    }

    /**
     * Test completeMessage when isCompleteWithSuccess is true
     */
    @Test
    @Throws(Exception::class)
    fun getCompleteMessageCase2() {
        val presenter = createPresenter(viewContract = mockView)
        val connection = Connection().setGuid("guid1").setStatus(ConnectionStatus.ACTIVE)
            .setAccessToken("access_token").setCode("demobank1").setName("Demobank1")
        Mockito.doReturn(connection).`when`(mockConnectionsRepository).getByGuid("guid1")

        presenter.setInitialData(connectConfigurationLink = null, connectionGuid = "guid1")

        Mockito.doReturn(1L).`when`(mockConnectionsRepository).getConnectionsCount("demobank1")

        presenter.webAuthFinishSuccess(id = "1", accessToken = "access_token")

        assertThat(
            presenter.completeMessage,
            equalTo(getString(R.string.connect_status_provider_success_description))
        )
    }

    /**
     * test onViewCreated when ViewMode is WEB_ENROLL
     */
    @Test
    @Throws(Exception::class)
    fun onViewCreatedCase1() {
        val presenter = createPresenter(viewContract = mockView)
        val connectUrlData = AuthenticateConnectionData(
            redirectUrl = "https://www.fentury.com",
            connectionId = "connectionId"
        )
        presenter.onConnectionInitSuccess(response = connectUrlData)

        Mockito.clearInvocations(mockView)

        presenter.onViewCreated()

        Mockito.verify(mockView).loadUrlInWebView("https://www.fentury.com")
    }

    /**
     * test onViewCreated when ViewMode is START
     */
    @Test
    @Throws(Exception::class)
    fun onViewCreatedCase2() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.onViewCreated()

        Mockito.verify(mockApiManager).getProviderData("", presenter)
    }

    /**
     * test onViewCreated when ViewMode is COMPLETE_SUCCESS
     */
    @Test
    @Throws(Exception::class)
    fun onViewCreatedCase3() {
        val presenter = createPresenter(viewContract = mockView)
        val connection = Connection().setGuid("guid1").setStatus(ConnectionStatus.ACTIVE)
            .setAccessToken("access_token").setCode("demobank1").setName("Demobank1")
        Mockito.doReturn(connection).`when`(mockConnectionsRepository).getByGuid("guid1")

        presenter.setInitialData(connectConfigurationLink = null, connectionGuid = "guid1")

        Mockito.doReturn(1L).`when`(mockConnectionsRepository).getConnectionsCount("demobank1")

        presenter.webAuthFinishSuccess(id = "1", accessToken = "access_token")
        presenter.onViewCreated()

        Mockito.never()
    }

    /**
     * test onViewCreated when ViewMode is COMPLETE_ERROR
     */
    @Test
    @Throws(Exception::class)
    fun onAuthFinishedTestCase4() {
        val presenter = createPresenter(viewContract = mockView)
        val connection = Connection().setGuid("guid1").setStatus(ConnectionStatus.ACTIVE)
            .setAccessToken("access_token").setCode("demobank1").setName("Demobank1")
        Mockito.doReturn(connection).`when`(mockConnectionsRepository).getByGuid("guid1")

        presenter.setInitialData(connectConfigurationLink = null, connectionGuid = "guid1")

        Mockito.doReturn(1L).`when`(mockConnectionsRepository).getConnectionsCount("demobank1")

        presenter.webAuthFinishError(errorClass = "ERROR", errorMessage = "ERROR")
        presenter.onViewCreated()

        Mockito.never()
    }

    @Test
    @Throws(Exception::class)
    fun getShouldShowProgressView() {
        val presenter = createPresenter(viewContract = mockView)

        assertTrue(presenter.shouldShowProgressView)

        val connectUrlData = AuthenticateConnectionData(
            redirectUrl = "https://www.fentury.com",
            connectionId = "connectionId"
        )
        presenter.onConnectionInitSuccess(response = connectUrlData)

        assertFalse(presenter.shouldShowProgressView)
    }

    /**
     * test shouldShowCompleteView when ViewMode is COMPLETE_SUCCESS
     */
    @Test
    @Throws(Exception::class)
    fun shouldShowCompleteViewTestCase1() {
        val presenter = createPresenter(viewContract = mockView)

        assertFalse(presenter.shouldShowCompleteView)

        val connection = Connection().setGuid("guid1").setStatus(ConnectionStatus.ACTIVE)
            .setAccessToken("access_token").setCode("demobank1").setName("Demobank1")
        Mockito.doReturn(connection).`when`(mockConnectionsRepository).getByGuid("guid1")

        presenter.setInitialData(connectConfigurationLink = null, connectionGuid = "guid1")

        Mockito.doReturn(1L).`when`(mockConnectionsRepository).getConnectionsCount("demobank1")

        presenter.webAuthFinishSuccess(id = "1", accessToken = "access_token")
        presenter.onViewCreated()

        assertTrue(presenter.shouldShowCompleteView)
    }

    /**
     * test shouldShowCompleteView when ViewMode is COMPLETE_ERROR
     */
    @Test
    @Throws(Exception::class)
    fun shouldShowCompleteViewTestCase2() {
        val presenter = createPresenter(viewContract = mockView)

        assertFalse(presenter.shouldShowCompleteView)

        val connection = Connection().setGuid("guid1").setStatus(ConnectionStatus.ACTIVE)
            .setAccessToken("access_token").setCode("demobank1").setName("Demobank1")
        Mockito.doReturn(connection).`when`(mockConnectionsRepository).getByGuid("guid1")

        presenter.setInitialData(connectConfigurationLink = null, connectionGuid = "guid1")

        Mockito.doReturn(1L).`when`(mockConnectionsRepository).getConnectionsCount("demobank1")

        presenter.webAuthFinishError(errorClass = "ERROR", errorMessage = "ERROR")
        presenter.onViewCreated()

        assertTrue(presenter.shouldShowCompleteView)
    }

    @Test
    @Throws(Exception::class)
    fun initConnectionTest() {
        val presenter = createPresenter(viewContract = mockView)

        val connection = Connection().setGuid("guid1").setStatus(ConnectionStatus.ACTIVE)
            .setAccessToken("access_token").setCode("demobank1").setName("Demobank1")
        Mockito.doReturn(connection).`when`(mockConnectionsRepository).getByGuid("guid1")

        presenter.setInitialData(connectConfigurationLink = null, connectionGuid = "guid1")
        presenter.onViewCreated()

        Mockito.doReturn("").`when`(mockPreferenceRepository).cloudMessagingToken
    }

    private val mockPreferenceRepository = Mockito.mock(PreferenceRepositoryAbs::class.java)
    private val mockConnectionsRepository = Mockito.mock(ConnectionsRepositoryAbs::class.java)
    private val mockKeyStoreManager = Mockito.mock(KeyStoreManagerAbs::class.java)
    private val mockApiManager = Mockito.mock(AuthenticatorApiManagerAbs::class.java)
    private val mockView = Mockito.mock(ConnectProviderContract.View::class.java)

    private fun createPresenter(viewContract: ConnectProviderContract.View? = null): ConnectProviderPresenter {
        return ConnectProviderPresenter(
            appContext = TestTools.applicationContext,
            preferenceRepository = mockPreferenceRepository,
            connectionsRepository = mockConnectionsRepository,
            keyStoreManager = mockKeyStoreManager,
            apiManager = mockApiManager
        ).apply { this.viewContract = viewContract }
    }
}