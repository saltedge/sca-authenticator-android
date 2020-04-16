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
package com.saltedge.authenticator.features.connections.create

import com.saltedge.authenticator.R
import com.saltedge.authenticator.model.db.Connection
import com.saltedge.authenticator.model.db.ConnectionsRepositoryAbs
import com.saltedge.authenticator.model.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.constants.API_VERSION
import com.saltedge.authenticator.sdk.constants.ERROR_CLASS_API_RESPONSE
import com.saltedge.authenticator.sdk.model.configuration.ProviderConfigurationData
import com.saltedge.authenticator.sdk.model.appLink.ConnectAppLinkData
import com.saltedge.authenticator.sdk.model.connection.ConnectionStatus
import com.saltedge.authenticator.sdk.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.model.response.CreateConnectionResponseData
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManagerAbs
import com.saltedge.authenticator.testTools.TestAppTools
import io.mockk.*
import net.danlew.android.joda.JodaTimeAndroid
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ConnectProviderPresenterTest {

    private val mockPreferenceRepository = mockk<PreferenceRepositoryAbs>()
    private val mockConnectionsRepository = mockk<ConnectionsRepositoryAbs>(relaxed = true)
    private val mockKeyStoreManager = mockk<KeyStoreManagerAbs>(relaxUnitFun = true)
    private val mockApiManager = mockk<AuthenticatorApiManagerAbs>(relaxUnitFun = true)
    private val mockView = mockk<ConnectProviderContract.View>(relaxUnitFun = true)

    @Before
    fun setUp() {
        JodaTimeAndroid.init(TestAppTools.applicationContext)
        every { mockPreferenceRepository.cloudMessagingToken } returns "push_token"
    }

    @Test
    @Throws(Exception::class)
    fun constructorTest() {
        val presenter = createPresenter(viewContract = null)

        assertNull(presenter.viewContract)

        presenter.viewContract = mockView

        assertNotNull(presenter.viewContract)
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTest() {
        every { mockConnectionsRepository.getByGuid("guid1") } returns null
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(
            initialConnectData = ConnectAppLinkData("connectConfigurationLink", null),
            connectionGuid = "guid1"
        )

        presenter.onViewClick(R.id.mainActionView)

        verify { mockView.closeView() }
        confirmVerified(mockView, mockApiManager)
    }

    /**
     * Test fetchProviderConfigurationDataResult() when no data and error
     */
    @Test
    @Throws(Exception::class)
    fun fetchProviderConfigurationDataResultTest_case1() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.fetchProviderConfigurationDataResult(result = null, error = null)

        verify { mockView.showErrorAndFinish(TestAppTools.applicationContext.getString(R.string.errors_unable_connect_provider)) }
        confirmVerified(mockView, mockApiManager)
    }

    /**
     * Test fetchProviderConfigurationDataResult() when received error
     */
    @Test
    @Throws(Exception::class)
    fun fetchProviderConfigurationDataResultTest_case2() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.fetchProviderConfigurationDataResult(
            result = null,
            error = ApiErrorData(
                errorMessage = "test error",
                errorClassName = ERROR_CLASS_API_RESPONSE
            )
        )

        verify { mockView.showErrorAndFinish("test error") }
        confirmVerified(mockView, mockApiManager)
    }

    /**
     * Test fetchProviderConfigurationDataResult() when received provider data
     */
    @Test
    @Throws(Exception::class)
    fun fetchProviderConfigurationDataResultTest_case3() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.fetchProviderConfigurationDataResult(
            result = ProviderConfigurationData(
                connectUrl = "https://demo.saltedge.com",
                code = "demobank",
                name = "Demobank",
                logoUrl = "logoUrl",
                version = API_VERSION,
                supportEmail = "example@saltedge.com"
            ),
            error = null
        )

        verify { mockApiManager.createConnectionRequest(
            appContext = TestAppTools.applicationContext,
            connection = any(),
            pushToken = "push_token",
            connectQueryParam = null,
            resultCallback = presenter
        ) }
        confirmVerified(mockView, mockApiManager)
    }

    /**
     * Test onConnectionInitFailure() when you get an error
     */
    @Test
    @Throws(Exception::class)
    fun onConnectionInitFailureTest() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.onConnectionCreateFailure(
            error = ApiErrorData(
                errorMessage = "test error",
                errorClassName = ERROR_CLASS_API_RESPONSE
            )
        )

        verify { mockView.showErrorAndFinish("test error") }
        confirmVerified(mockView, mockApiManager)
    }

    @Test
    @Throws(Exception::class)
    fun onConnectionInitSuccessTestCase1() {
        val presenter = createPresenter(viewContract = mockView)
        val connectUrlData = CreateConnectionResponseData(
            redirectUrl = "https://www.fentury.com",
            connectionId = "connectionId"
        )
        presenter.onConnectionCreateSuccess(response = connectUrlData)

        verify { mockView.loadUrlInWebView("https://www.fentury.com") }
        verify { mockView.updateViewsContent() }
        confirmVerified(mockView, mockApiManager)
    }

    @Test
    @Throws(Exception::class)
    fun onConnectionInitSuccessTestCase2() {
        val presenter = createPresenter(viewContract = mockView)
        val connectUrlData = CreateConnectionResponseData(
            redirectUrl = "invalidUrl",
            connectionId = "connectionId"
        )
        presenter.onConnectionCreateSuccess(response = connectUrlData)

        verify { listOf(mockView, mockApiManager, mockConnectionsRepository, mockPreferenceRepository) wasNot Called }
    }

    /**
     * Test onDestroyView() when guid is not empty and accessToken is empty
     */
    @Test
    @Throws(Exception::class)
    fun onDestroyViewTestCase1() {
        val connection = Connection().apply {
            guid = "guid2"
            accessToken = ""
            code = "demobank1"
            name = "Demobank1"
        }
        every { mockConnectionsRepository.getByGuid("guid2") } returns connection
        val presenter = createPresenter()
        presenter.setInitialData(initialConnectData = null, connectionGuid = "guid2")

        presenter.onDestroyView()

        verify { mockKeyStoreManager.deleteKeyPair("guid2") }
        confirmVerified(mockView, mockApiManager)
    }

    /**
     * Test onDestroyView() when guid and accessToken is not empty
     */
    @Test
    @Throws(Exception::class)
    fun onDestroyViewTestCase2() {
        val connection = Connection().apply {
            guid = "guid2"
            accessToken = "accessToken"
            code = "demobank1"
            name = "Demobank1"
        }
        every { mockConnectionsRepository.getByGuid("") } returns null
        val presenter = createPresenter()
        presenter.setInitialData(initialConnectData = null, connectionGuid = "")
        clearMocks(mockView, mockConnectionsRepository)

        presenter.onDestroyView()

        verify { listOf(mockView, mockApiManager, mockConnectionsRepository, mockPreferenceRepository) wasNot Called }
    }

    /**
     * Test onDestroyView() when guid and accessToken are empty
     */
    @Test
    @Throws(Exception::class)
    fun onDestroyViewTestCase3() {
        val connection = Connection().apply {
            guid = "guid2"
            accessToken = ""
            code = "demobank1"
            name = "Demobank1"
        }
        every { mockConnectionsRepository.getByGuid("guid2") } returns connection
        val presenter = createPresenter()
        presenter.setInitialData(initialConnectData = null, connectionGuid = "guid2")
        clearMocks(mockView, mockConnectionsRepository)

        presenter.onDestroyView()

        verify { listOf(mockView, mockApiManager, mockConnectionsRepository, mockPreferenceRepository) wasNot Called }
    }

    /**
     * Test onDestroyView() when accessToken is not empty and guid is empty
     */
    @Test
    @Throws(Exception::class)
    fun onDestroyViewTestCase4() {
        val connection = Connection().apply {
            guid = "guid2"
            accessToken = "accessToken"
            code = "demobank1"
            name = "Demobank1"
        }
        every { mockConnectionsRepository.getByGuid("guid2") } returns connection
        val presenter = createPresenter()
        presenter.setInitialData(initialConnectData = null, connectionGuid = "guid2")
        clearMocks(mockConnectionsRepository)

        presenter.onDestroyView()

        verify { listOf(mockView, mockApiManager, mockConnectionsRepository, mockPreferenceRepository) wasNot Called }
    }

    @Test
    @Throws(Exception::class)
    fun getLogoUrlTest() {
        every { mockConnectionsRepository.getByGuid("guid1") } returns null
        val presenter = createPresenter(viewContract = mockView)

        assertTrue(presenter.logoUrl.isEmpty())

        presenter.setInitialData(
            initialConnectData = ConnectAppLinkData("connectConfigurationLink", null),
            connectionGuid = "guid1"
        )

        assertTrue(presenter.logoUrl.isEmpty())

        presenter.fetchProviderConfigurationDataResult(
            result = ProviderConfigurationData(
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

        verify { mockView.updateViewsContent() }
        confirmVerified(mockView, mockApiManager)
    }

    /**
     * When access_token is not null or empty
     */
    @Test
    @Throws(Exception::class)
    fun webAuthFinishSuccessTest() {
        val presenter = createPresenter(viewContract = mockView)
        val connection = Connection().apply {
            guid = "guid1"
            status = "${ConnectionStatus.ACTIVE}"
            accessToken = "accessToken"
            code = "demobank1"
            name = "Demobank1"
        }
        every { mockConnectionsRepository.getByGuid("guid1") } returns connection
        every { mockConnectionsRepository.connectionExists(connection) } returns true
        every { mockConnectionsRepository.getConnectionsCount("demobank1") } returns 1L
        presenter.setInitialData(initialConnectData = null, connectionGuid = "guid1")
        clearMocks(mockView, mockConnectionsRepository)

        presenter.webAuthFinishSuccess(id = "1", accessToken = "access_token")

        verify { mockView.updateViewsContent() }
        verify { mockConnectionsRepository.fixNameAndSave(connection) }
    }

    @Test
    @Throws(Exception::class)
    fun shouldShowWebViewTest() {
        val presenter = createPresenter(viewContract = mockView)

        assertFalse(presenter.shouldShowWebView)

        val connectUrlData = CreateConnectionResponseData(
            redirectUrl = "https://www.fentury.com",
            connectionId = "connectionId"
        )
        presenter.onConnectionCreateSuccess(response = connectUrlData)

        assertTrue(presenter.shouldShowWebView)
    }

    /**
     * Test iconResId when isCompleteWithSuccess is false
     */
    @Test
    @Throws(Exception::class)
    fun getIconResIdTestCase1() {
        val presenter = createPresenter(viewContract = mockView)

        assertThat(presenter.iconResId, equalTo(R.drawable.ic_auth_error_70))
    }

    /**
     * Test iconResId when isCompleteWithSuccess is true
     */
    @Test
    @Throws(Exception::class)
    fun getIconResIdTestCase2() {
        val presenter = createPresenter(viewContract = mockView)
        val connection = Connection().apply {
            guid = "guid1"
            status = "${ConnectionStatus.ACTIVE}"
            accessToken = "accessToken"
            code = "demobank1"
            name = "Demobank1"
        }
        every { mockConnectionsRepository.getByGuid("guid1") } returns connection
        every { mockConnectionsRepository.getConnectionsCount("demobank1") } returns 1L
        presenter.setInitialData(initialConnectData = null, connectionGuid = "guid1")

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
        val connection = Connection().apply {
            guid = "guid1"
            status = "${ConnectionStatus.ACTIVE}"
            accessToken = "accessToken"
            code = "demobank1"
            name = "Demobank1"
        }
        every { mockConnectionsRepository.getByGuid("guid1") } returns connection
        every { mockConnectionsRepository.getConnectionsCount("demobank1") } returns 1L
        presenter.setInitialData(initialConnectData = null, connectionGuid = "guid1")

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
     * Test reportProblemActionText when isCompleteWithSuccess is true
     */
    @Test
    @Throws(Exception::class)
    fun reportProblemActionTextCase2() {
        val presenter = createPresenter(viewContract = mockView)
        val connection = Connection().apply {
            guid = "guid1"
            status = "${ConnectionStatus.ACTIVE}"
            accessToken = "accessToken"
            code = "demobank1"
            name = "Demobank1"
        }
        every { mockConnectionsRepository.getByGuid("guid1") } returns connection
        every { mockConnectionsRepository.connectionExists(connection) } returns true
        every { mockConnectionsRepository.getConnectionsCount("demobank1") } returns 1L
        presenter.setInitialData(initialConnectData = null, connectionGuid = "guid1")

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
            equalTo(TestAppTools.getString(R.string.errors_connection_failed))
        )
    }

    /**
     * Test completeTitle when isCompleteWithSuccess is true
     */
    @Test
    @Throws(Exception::class)
    fun getCompleteTitleCase2() {
        val presenter = createPresenter(viewContract = mockView)
        val connection = Connection().apply {
            guid = "guid1"
            status = "${ConnectionStatus.ACTIVE}"
            accessToken = "accessToken"
            code = "demobank1"
            name = "Demobank1"
        }
        every { mockConnectionsRepository.getByGuid("guid1") } returns connection
        every { mockConnectionsRepository.getConnectionsCount("demobank1") } returns 1L
        presenter.setInitialData(initialConnectData = null, connectionGuid = "guid1")

        presenter.webAuthFinishSuccess(id = "1", accessToken = "access_token")

        assertThat(
            presenter.completeTitle,
            equalTo(TestAppTools.getString(R.string.connect_status_provider_success).format(connection.name))
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
            equalTo(TestAppTools.getString(R.string.errors_connection_failed_description))
        )

        val connection = Connection().apply {
            guid = "guid1"
            status = "${ConnectionStatus.ACTIVE}"
            accessToken = "accessToken"
            code = "demobank1"
            name = "Demobank1"
        }
        every { mockConnectionsRepository.getByGuid("guid1") } returns connection
        every { mockConnectionsRepository.getConnectionsCount("demobank1") } returns 1L
        presenter.setInitialData(initialConnectData = null, connectionGuid = "guid1")

        presenter.webAuthFinishError(errorClass = "ERROR", errorMessage = null)

        assertThat(
            presenter.completeMessage,
            equalTo(TestAppTools.getString(R.string.errors_connection_failed_description))
        )
    }

    /**
     * Test completeMessage when isCompleteWithSuccess is true
     */
    @Test
    @Throws(Exception::class)
    fun getCompleteMessageCase2() {
        val presenter = createPresenter(viewContract = mockView)
        val connection = Connection().apply {
            guid = "guid1"
            status = "${ConnectionStatus.ACTIVE}"
            accessToken = "accessToken"
            code = "demobank1"
            name = "Demobank1"
        }
        every { mockConnectionsRepository.getByGuid("guid1") } returns connection
        every { mockConnectionsRepository.getConnectionsCount("demobank1") } returns 1L
        presenter.setInitialData(initialConnectData = null, connectionGuid = "guid1")

        presenter.webAuthFinishSuccess(id = "1", accessToken = "access_token")

        assertThat(
            presenter.completeMessage,
            equalTo(TestAppTools.getString(R.string.connect_status_provider_success_description))
        )
    }

    /**
     * test onViewCreated when ViewMode is WEB_ENROLL
     */
    @Test
    @Throws(Exception::class)
    fun onViewCreatedCase1() {
        val presenter = createPresenter(viewContract = mockView)
        val connectUrlData = CreateConnectionResponseData(
            redirectUrl = "https://www.fentury.com",
            connectionId = "connectionId"
        )
        presenter.onConnectionCreateSuccess(response = connectUrlData)

        clearMocks(mockView)

        presenter.onViewCreated()

        verify { mockView.loadUrlInWebView("https://www.fentury.com") }
    }

    /**
     * test onViewCreated when ViewMode is START
     */
    @Test
    @Throws(Exception::class)
    fun onViewCreatedCase2() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setInitialData(ConnectAppLinkData("url"), null)
        clearMocks(mockView, mockConnectionsRepository)

        presenter.onViewCreated()

        verify { mockApiManager.getProviderConfigurationData("url", presenter) }
    }

    /**
     * test onViewCreated when ViewMode is COMPLETE_SUCCESS
     */
    @Test
    @Throws(Exception::class)
    fun onViewCreatedCase3() {
        val presenter = createPresenter(viewContract = mockView)
        val connection = Connection().apply {
            guid = "guid1"
            status = "${ConnectionStatus.ACTIVE}"
            accessToken = "accessToken"
            code = "demobank1"
            name = "Demobank1"
        }
        every { mockConnectionsRepository.getByGuid("guid1") } returns connection
        every { mockConnectionsRepository.getConnectionsCount("demobank1") } returns 1L
        presenter.setInitialData(initialConnectData = null, connectionGuid = "guid1")
        presenter.webAuthFinishSuccess(id = "1", accessToken = "access_token")
        clearMocks(mockView, mockConnectionsRepository)

        presenter.onViewCreated()

        verify { listOf(mockView, mockApiManager, mockConnectionsRepository, mockPreferenceRepository) wasNot Called }
    }

    /**
     * test onViewCreated when ViewMode is COMPLETE_ERROR
     */
    @Test
    @Throws(Exception::class)
    fun onAuthFinishedTestCase4() {
        val presenter = createPresenter(viewContract = mockView)
        val connection = Connection().apply {
            guid = "guid1"
            status = "${ConnectionStatus.ACTIVE}"
            accessToken = "accessToken"
            code = "demobank1"
            name = "Demobank1"
        }
        every { mockConnectionsRepository.getByGuid("guid1") } returns connection
        every { mockConnectionsRepository.getConnectionsCount("demobank1") } returns 1L
        presenter.setInitialData(initialConnectData = null, connectionGuid = "guid1")
        presenter.webAuthFinishError(errorClass = "ERROR", errorMessage = "ERROR")
        clearMocks(mockView, mockConnectionsRepository)

        presenter.onViewCreated()

        verify { listOf(mockView, mockApiManager, mockConnectionsRepository, mockPreferenceRepository) wasNot Called }
    }

    @Test
    @Throws(Exception::class)
    fun getShouldShowProgressView() {
        val presenter = createPresenter(viewContract = mockView)

        assertTrue(presenter.shouldShowProgressView)

        val connectUrlData = CreateConnectionResponseData(
            redirectUrl = "https://www.fentury.com",
            connectionId = "connectionId"
        )
        presenter.onConnectionCreateSuccess(response = connectUrlData)

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

        val connection = Connection().apply {
            guid = "guid1"
            status = "${ConnectionStatus.ACTIVE}"
            accessToken = "accessToken"
            code = "demobank1"
            name = "Demobank1"
        }
        every { mockConnectionsRepository.getByGuid("guid1") } returns connection
        every { mockConnectionsRepository.getConnectionsCount("demobank1") } returns 1L
        presenter.setInitialData(initialConnectData = null, connectionGuid = "guid1")

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

        val connection = Connection().apply {
            guid = "guid1"
            status = "${ConnectionStatus.ACTIVE}"
            accessToken = "accessToken"
            code = "demobank1"
            name = "Demobank1"
        }
        every { mockConnectionsRepository.getByGuid("guid1") } returns connection
        every { mockConnectionsRepository.getConnectionsCount("demobank1") } returns 1L
        presenter.setInitialData(initialConnectData = null, connectionGuid = "guid1")

        presenter.webAuthFinishError(errorClass = "ERROR", errorMessage = "ERROR")
        presenter.onViewCreated()

        assertTrue(presenter.shouldShowCompleteView)
    }

    /**
     * Returns a title depending on the connection type
     *
     * @return titleResId as Int
     */
    @Test
    @Throws(Exception::class)
    fun getTitleResIdTest() {
        val presenter = createPresenter(viewContract = mockView)

        assertThat(presenter.getTitleResId(), equalTo(R.string.connections_new_connection))

        val connection = Connection().apply {
            guid = "guid1"
            status = "${ConnectionStatus.ACTIVE}"
            accessToken = "accessToken"
            code = "demobank1"
            name = "Demobank1"
        }
        every { mockConnectionsRepository.getByGuid("guid1") } returns connection

        presenter.setInitialData(initialConnectData = null, connectionGuid = "guid1")

        assertThat(presenter.getTitleResId(), equalTo(R.string.actions_reconnect))
    }

    @Test
    @Throws(Exception::class)
    fun viewModeClassTest() {
        val targetArray = arrayOf(
            ViewMode.START_NEW_CONNECT,
            ViewMode.START_RECONNECT,
            ViewMode.WEB_ENROLL,
            ViewMode.COMPLETE_SUCCESS,
            ViewMode.COMPLETE_ERROR
        )
        assertThat(ViewMode.values(), equalTo(targetArray))
        assertThat(ViewMode.valueOf("START_NEW_CONNECT"), equalTo(ViewMode.START_NEW_CONNECT))
        assertThat(ViewMode.valueOf("START_RECONNECT"), equalTo(ViewMode.START_RECONNECT))
        assertThat(ViewMode.valueOf("WEB_ENROLL"), equalTo(ViewMode.WEB_ENROLL))
        assertThat(ViewMode.valueOf("COMPLETE_SUCCESS"), equalTo(ViewMode.COMPLETE_SUCCESS))
        assertThat(ViewMode.valueOf("COMPLETE_ERROR"), equalTo(ViewMode.COMPLETE_ERROR))
    }

    private fun createPresenter(viewContract: ConnectProviderContract.View? = null): ConnectProviderPresenter {
        return ConnectProviderPresenter(
            appContext = TestAppTools.applicationContext,
            preferenceRepository = mockPreferenceRepository,
            connectionsRepository = mockConnectionsRepository,
            keyStoreManager = mockKeyStoreManager,
            apiManager = mockApiManager
        ).apply { this.viewContract = viewContract }
    }
}
