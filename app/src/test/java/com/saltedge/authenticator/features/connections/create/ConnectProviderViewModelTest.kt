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
package com.saltedge.authenticator.features.connections.create

import android.content.DialogInterface
import android.view.View
import com.saltedge.authenticator.R
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.models.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.constants.ERROR_CLASS_API_RESPONSE
import com.saltedge.authenticator.sdk.model.appLink.ConnectAppLinkData
import com.saltedge.authenticator.sdk.model.connection.ConnectionStatus
import com.saltedge.authenticator.sdk.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.model.response.CreateConnectionResponseData
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManagerAbs
import com.saltedge.authenticator.testTools.TestAppTools
import net.danlew.android.joda.JodaTimeAndroid
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ConnectProviderViewModelTest {

    private lateinit var viewModel: ConnectProviderViewModel
    private val mockPreferenceRepository = mock(PreferenceRepositoryAbs::class.java)
    private val mockConnectionsRepository = mock(ConnectionsRepositoryAbs::class.java)
    private val mockKeyStoreManager = mock(KeyStoreManagerAbs::class.java)
    private val mockApiManager = mock(AuthenticatorApiManagerAbs::class.java)

    @Before
    fun setUp() {
        JodaTimeAndroid.init(TestAppTools.applicationContext)
        given(mockPreferenceRepository.cloudMessagingToken).willReturn("push_token")

        viewModel = ConnectProviderViewModel(
            appContext = TestAppTools.applicationContext,
            preferenceRepository = mockPreferenceRepository,
            connectionsRepository = mockConnectionsRepository,
            keyStoreManager = mockKeyStoreManager,
            apiManager = mockApiManager
        )
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTest() {
        given(mockConnectionsRepository.getByGuid("guid1")).willReturn(null)
        viewModel.setInitialData(
            initialConnectData = ConnectAppLinkData("connectConfigurationLink", null),
            connectionGuid = "guid1"
        )

        viewModel.onViewClick(-1)

        assertNull(viewModel.onCloseEvent.value)

        viewModel.onViewClick(R.id.actionView)

        assertNotNull(viewModel.onCloseEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onDialogActionIdClickTest() {
        viewModel.fetchProviderConfigurationDataResult(result = null, error = null)

        assertThat(
            viewModel.onShowErrorEvent.value,
            equalTo(ViewModelEvent(TestAppTools.applicationContext.getString(R.string.errors_unable_connect_provider)))
        )

        viewModel.onDialogActionIdClick(DialogInterface.BUTTON_POSITIVE)

        assertNotNull(viewModel.onCloseEvent.value)
    }

    /**
     * When access_token is not null or empty
     */
    @Test
    @Throws(Exception::class)
    fun authFinishedWithSuccessTest() {
        val connection = Connection().apply {
            guid = "guid1"
            status = "${ConnectionStatus.ACTIVE}"
            accessToken = "access_token"
            code = "demobank1"
            name = "Demobank1"
        }
        given(mockConnectionsRepository.getByGuid("guid1")).willReturn(connection)
        given(mockConnectionsRepository.connectionExists(connection)).willReturn(false)
        given(mockConnectionsRepository.getConnectionsCount("demobank1")).willReturn(1L)

        viewModel.setInitialData(initialConnectData = null, connectionGuid = "guid1")
        clearInvocations(mockConnectionsRepository)

        viewModel.authFinishedWithSuccess(connectionId = "1", accessToken = "access_token")

        verify(mockConnectionsRepository).fixNameAndSave(connection)

        assertThat(viewModel.statusIconRes.value, equalTo(R.drawable.ic_status_success))
        assertThat(
            viewModel.completeTitle.value.toString(),
            equalTo(TestAppTools.getString(R.string.connect_status_provider_success).format(connection.name))
        )
        assertThat(
            viewModel.completeDescription.value,
            equalTo(TestAppTools.getString(R.string.connect_status_provider_success_description))
        )
        assertThat(viewModel.mainActionTextRes.value, equalTo(R.string.actions_done))

        assertThat(viewModel.webViewVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.progressViewVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.completeViewVisibility.value, equalTo(View.VISIBLE))
    }

    /**
     * Test fetchProviderConfigurationDataResult() when no data and error
     */
    @Test
    @Throws(Exception::class)
    fun fetchProviderConfigurationDataResultTest_case1() {
        viewModel.fetchProviderConfigurationDataResult(result = null, error = null)

        assertThat(
            viewModel.onShowErrorEvent.value,
            equalTo(ViewModelEvent(TestAppTools.applicationContext.getString(R.string.errors_unable_connect_provider)))
        )
    }

    /**
     * Test fetchProviderConfigurationDataResult() when received error
     */
    @Test
    @Throws(Exception::class)
    fun fetchProviderConfigurationDataResultTest_case2() {
        viewModel.fetchProviderConfigurationDataResult(
            result = null,
            error = ApiErrorData(
                errorMessage = "test error",
                errorClassName = ERROR_CLASS_API_RESPONSE
            )
        )

        assertThat(
            viewModel.onShowErrorEvent.value,
            equalTo(ViewModelEvent("test error"))
        )
    }

        /**
         * Test fetchProviderConfigurationDataResult() when received provider data
         */
//        @Test
//        @Throws(Exception::class)
//        fun fetchProviderConfigurationDataResultTest_case3() { //TODO: Refactor this test, when I run it together with other tests, I get an error
//            viewModel.fetchProviderConfigurationDataResult(
//                result = ProviderConfigurationData(
//                    connectUrl = "https://demo.saltedge.com",
//                    code = "demobank",
//                    name = "Demobank",
//                    logoUrl = "logoUrl",
//                    version = API_VERSION,
//                    supportEmail = "example@saltedge.com"
//                ),
//                error = null
//            )
//            val captor: ArgumentCaptor<Connection?> =
//                ArgumentCaptor.forClass(Connection::class.java)
//
//            captor.capture()?.let {
//                verify(mockApiManager).createConnectionRequest(
//                    appContext = TestAppTools.applicationContext,
//                    connection = it,
//                    pushToken = "push_token",
//                    connectQueryParam = null,
//                    resultCallback = viewModel
//                )
//            }
//        }

    /**
     * Test onConnectionInitFailure() when you get an error
     */
    @Test
    @Throws(Exception::class)
    fun onConnectionInitFailureTest() {
        viewModel.onConnectionCreateFailure(
            error = ApiErrorData(
                errorMessage = "test error",
                errorClassName = ERROR_CLASS_API_RESPONSE
            )
        )

        assertThat(
            viewModel.onShowErrorEvent.value,
            equalTo(ViewModelEvent("test error"))
        )
    }

    @Test
    @Throws(Exception::class)
    fun onConnectionInitSuccessTestCase1() {
        val connectUrlData = CreateConnectionResponseData(
            redirectUrl = "https://www.fentury.com",
            connectionId = "connectionId"
        )
        viewModel.onConnectionCreateSuccess(response = connectUrlData)

        assertThat(
            viewModel.onUrlChangedEvent.value,
            equalTo(ViewModelEvent("https://www.fentury.com"))
        )

        assertThat(viewModel.statusIconRes.value, equalTo(R.drawable.ic_status_error))
        assertThat(
            viewModel.completeTitle.value.toString(),
            equalTo(TestAppTools.getString(R.string.errors_connection_failed))
        )
        assertThat(
            viewModel.completeDescription.value,
            equalTo(TestAppTools.getString(R.string.errors_connection_failed_description))
        )
        assertThat(viewModel.mainActionTextRes.value, equalTo(R.string.actions_try_again))

        assertThat(viewModel.webViewVisibility.value, equalTo(View.VISIBLE))
        assertThat(viewModel.progressViewVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.completeViewVisibility.value, equalTo(View.GONE))
    }

    @Test
    @Throws(Exception::class)
    fun onConnectionInitSuccessTestCase2() {
        val connectUrlData = CreateConnectionResponseData(
            redirectUrl = "invalidUrl",
            connectionId = "connectionId"
        )
        viewModel.onConnectionCreateSuccess(response = connectUrlData)

        verifyNoMoreInteractions(
            mockApiManager,
            mockConnectionsRepository,
            mockPreferenceRepository
        )
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
        given(mockConnectionsRepository.getByGuid("guid2")).willReturn(connection)

        viewModel.setInitialData(initialConnectData = null, connectionGuid = "guid2")

        viewModel.onDestroy()

        verify(mockKeyStoreManager).deleteKeyPair("guid2")
    }

    /**
     * Test onDestroyView() when guid and accessToken is not empty
     */
    @Test
    @Throws(Exception::class)
    fun onDestroyViewTestCase2() {
        given(mockConnectionsRepository.getByGuid("")).willReturn(null)

        viewModel.setInitialData(initialConnectData = null, connectionGuid = "")
        clearInvocations(mockConnectionsRepository)

        viewModel.onDestroy()

        verifyNoMoreInteractions(
            mockApiManager,
            mockConnectionsRepository,
            mockPreferenceRepository
        )
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
        given(mockConnectionsRepository.getByGuid("guid2")).willReturn(connection)

        viewModel.setInitialData(initialConnectData = null, connectionGuid = "guid2")
        clearInvocations(mockConnectionsRepository)

        viewModel.onDestroy()

        verifyNoMoreInteractions(
            mockApiManager,
            mockConnectionsRepository,
            mockPreferenceRepository
        )
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
        given(mockConnectionsRepository.getByGuid("guid2")).willReturn(connection)

        viewModel.setInitialData(initialConnectData = null, connectionGuid = "guid2")
        clearInvocations(mockConnectionsRepository)

        viewModel.onDestroy()

        verifyNoMoreInteractions(
            mockApiManager,
            mockConnectionsRepository,
            mockPreferenceRepository
        )
    }

    @Test
    @Throws(Exception::class)
    fun webAuthFinishErrorTest() {
        viewModel.webAuthFinishError(errorClass = "WRONG_URL", errorMessage = "not relevant url")

        assertThat(viewModel.statusIconRes.value, equalTo(R.drawable.ic_status_error))
        assertThat(
            viewModel.completeTitle.value.toString(),
            equalTo(TestAppTools.getString(R.string.errors_connection_failed))
        )
        assertThat(viewModel.completeDescription.value, equalTo("not relevant url"))
        assertThat(viewModel.mainActionTextRes.value, equalTo(R.string.actions_try_again))

        assertThat(viewModel.webViewVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.progressViewVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.completeViewVisibility.value, equalTo(View.VISIBLE))
    }

    @Test
    @Throws(Exception::class)
    fun shouldShowWebViewTest() {
        assertThat(viewModel.webViewVisibility.value, equalTo(View.GONE))

        val connectUrlData = CreateConnectionResponseData(
            redirectUrl = "https://www.fentury.com",
            connectionId = "connectionId"
        )
        viewModel.onConnectionCreateSuccess(response = connectUrlData)

        assertThat(viewModel.webViewVisibility.value, equalTo(View.VISIBLE))
    }

    @Test
    @Throws(Exception::class)
    fun onBackPressTest() {
        assertFalse(viewModel.onBackPress(webViewCanGoBack = false))

        val connectUrlData = CreateConnectionResponseData(
            redirectUrl = "https://www.fentury.com",
            connectionId = "connectionId"
        )
        viewModel.onConnectionCreateSuccess(response = connectUrlData)

        assertTrue(viewModel.onBackPress(webViewCanGoBack = true))
    }

    /**
     * Test statusIconRes when isCompleteWithSuccess is false
     */
    @Test
    @Throws(Exception::class)
    fun statusIconResTestCase1() {
        assertThat(viewModel.statusIconRes.value, equalTo(R.drawable.ic_status_error))
    }

    /**
     * Test statusIconRes when isCompleteWithSuccess is true
     */
    @Test
    @Throws(Exception::class)
    fun statusIconResTestCase2() {
        val connection = Connection().apply {
            guid = "guid1"
            status = "${ConnectionStatus.ACTIVE}"
            accessToken = "accessToken"
            code = "demobank1"
            name = "Demobank1"
        }
        given(mockConnectionsRepository.getByGuid("guid1")).willReturn(connection)
        given(mockConnectionsRepository.getConnectionsCount("demobank1")).willReturn(1L)

        viewModel.setInitialData(initialConnectData = null, connectionGuid = "guid1")

        viewModel.authFinishedWithSuccess(connectionId = "1", accessToken = "access_token")

        assertThat(viewModel.statusIconRes.value, equalTo(R.drawable.ic_status_success))
    }

    /**
     * Test mainActionTextResId when isCompleteWithSuccess is false
     */
    @Test
    @Throws(Exception::class)
    fun mainActionTextResCase1() {
        assertThat(viewModel.mainActionTextRes.value, equalTo(R.string.actions_try_again))
    }

    /**
     * Test mainActionTextResId when isCompleteWithSuccess is true
     */
    @Test
    @Throws(Exception::class)
    fun mainActionTextResCase2() {
        val connection = Connection().apply {
            guid = "guid1"
            status = "${ConnectionStatus.ACTIVE}"
            accessToken = "accessToken"
            code = "demobank1"
            name = "Demobank1"
        }
        given(mockConnectionsRepository.getByGuid("guid1")).willReturn(connection)
        given(mockConnectionsRepository.getConnectionsCount("demobank1")).willReturn(1L)

        viewModel.setInitialData(initialConnectData = null, connectionGuid = "guid1")
        viewModel.authFinishedWithSuccess(connectionId = "1", accessToken = "access_token")

        assertThat(viewModel.mainActionTextRes.value, equalTo(R.string.actions_done))
    }

    /**
     * Test completeTitle when isCompleteWithSuccess is false
     */
    @Test
    @Throws(Exception::class)
    fun completeTitleCase1() {
        assertThat(
            viewModel.completeTitle.value.toString(),
            equalTo("")
        )
    }

    /**
     * Test completeTitle when isCompleteWithSuccess is true
     */
    @Test
    @Throws(Exception::class)
    fun completeTitleCase2() {
        val connection = Connection().apply {
            guid = "guid1"
            status = "${ConnectionStatus.ACTIVE}"
            accessToken = "accessToken"
            code = "demobank1"
            name = "Demobank1"
        }
        given(mockConnectionsRepository.getByGuid("guid1")).willReturn(connection)
        given(mockConnectionsRepository.getConnectionsCount("demobank1")).willReturn(1L)

        viewModel.setInitialData(initialConnectData = null, connectionGuid = "guid1")
        viewModel.authFinishedWithSuccess(connectionId = "1", accessToken = "access_token")

        assertThat(
            viewModel.completeTitle.value.toString(),
            equalTo(
                TestAppTools.getString(R.string.connect_status_provider_success).format(
                    connection.name
                )
            )
        )
    }

    /**
     * Test completeDescription when isCompleteWithSuccess is false
     */
    @Test
    @Throws(Exception::class)
    fun completeDescriptionCase1() {
        assertThat(
            viewModel.completeDescription.value,
            equalTo("")
        )

        val connection = Connection().apply {
            guid = "guid1"
            status = "${ConnectionStatus.ACTIVE}"
            accessToken = "accessToken"
            code = "demobank1"
            name = "Demobank1"
        }
        given(mockConnectionsRepository.getByGuid("guid1")).willReturn(connection)
        given(mockConnectionsRepository.getConnectionsCount("demobank1")).willReturn(1L)

        viewModel.setInitialData(initialConnectData = null, connectionGuid = "guid1")
        viewModel.webAuthFinishError(errorClass = "ERROR", errorMessage = null)

        assertThat(
            viewModel.completeDescription.value,
            equalTo(TestAppTools.getString(R.string.errors_connection_failed_description))
        )
    }

    /**
     * Test completeDescription when isCompleteWithSuccess is true
     */
    @Test
    @Throws(Exception::class)
    fun completeDescriptionCase2() {
        val connection = Connection().apply {
            guid = "guid1"
            status = "${ConnectionStatus.ACTIVE}"
            accessToken = "accessToken"
            code = "demobank1"
            name = "Demobank1"
        }
        given(mockConnectionsRepository.getByGuid("guid1")).willReturn(connection)
        given(mockConnectionsRepository.getConnectionsCount("demobank1")).willReturn(1L)

        viewModel.setInitialData(initialConnectData = null, connectionGuid = "guid1")
        viewModel.authFinishedWithSuccess(connectionId = "1", accessToken = "access_token")

        assertThat(
            viewModel.completeDescription.value,
            equalTo(TestAppTools.getString(R.string.connect_status_provider_success_description))
        )
    }

    /**
     * test onViewCreated when ViewMode is WEB_ENROLL
     */
    @Test
    @Throws(Exception::class)
    fun onViewCreatedCase1() {
        val connectUrlData = CreateConnectionResponseData(
            redirectUrl = "https://www.fentury.com",
            connectionId = "connectionId"
        )
        viewModel.onConnectionCreateSuccess(response = connectUrlData)

        viewModel.onResume()

        assertThat(
            viewModel.onUrlChangedEvent.value,
            equalTo(ViewModelEvent("https://www.fentury.com"))
        )
    }

    /**
     * test onViewCreated when ViewMode is START_NEW_CONNECT(initial state)
     */
    @Test
    @Throws(Exception::class)
    fun onViewCreatedCase2() {
        clearInvocations(mockConnectionsRepository)

        viewModel.setInitialData(
            initialConnectData = ConnectAppLinkData(
                configurationUrl = "url",
                connectQuery = null
            ),
            connectionGuid = "guid1"
        )
        viewModel.onResume()

        assertNull(viewModel.onUrlChangedEvent.value)
    }

    /**
     * test onViewCreated when ViewMode is COMPLETE_SUCCESS
     */
    @Test
    @Throws(Exception::class)
    fun onViewCreatedCase3() {
        val connection = Connection().apply {
            guid = "guid1"
            status = "${ConnectionStatus.ACTIVE}"
            accessToken = "accessToken"
            code = "demobank1"
            name = "Demobank1"
        }
        given(mockConnectionsRepository.getByGuid("guid1")).willReturn(connection)
        given(mockConnectionsRepository.getConnectionsCount("demobank1")).willReturn(1L)

        viewModel.setInitialData(initialConnectData = null, connectionGuid = "guid1")
        viewModel.authFinishedWithSuccess(connectionId = "1", accessToken = "access_token")
        clearInvocations(mockConnectionsRepository)

        viewModel.onResume()

        verifyNoMoreInteractions(
            mockApiManager,
            mockConnectionsRepository,
            mockPreferenceRepository
        )
    }

    /**
     * test onViewCreated when ViewMode is COMPLETE_ERROR
     */
    @Test
    @Throws(Exception::class)
    fun onAuthFinishedTestCase4() {
        val connection = Connection().apply {
            guid = "guid1"
            status = "${ConnectionStatus.ACTIVE}"
            accessToken = "accessToken"
            code = "demobank1"
            name = "Demobank1"
        }
        given(mockConnectionsRepository.getByGuid("guid1")).willReturn(connection)
        given(mockConnectionsRepository.getConnectionsCount("demobank1")).willReturn(1L)

        viewModel.setInitialData(initialConnectData = null, connectionGuid = "guid1")
        viewModel.webAuthFinishError(errorClass = "ERROR", errorMessage = "ERROR")

        clearInvocations(mockConnectionsRepository)

        viewModel.onResume()

        verifyNoMoreInteractions(
            mockApiManager,
            mockConnectionsRepository,
            mockPreferenceRepository
        )
    }

    @Test
    @Throws(Exception::class)
    fun progressViewVisibilityTest() {
        assertThat(viewModel.progressViewVisibility.value, equalTo(View.VISIBLE))

        val connectUrlData = CreateConnectionResponseData(
            redirectUrl = "https://www.fentury.com",
            connectionId = "connectionId"
        )
        viewModel.onConnectionCreateSuccess(response = connectUrlData)

        assertThat(viewModel.progressViewVisibility.value, equalTo(View.GONE))
    }

    /**
     * test shouldShowCompleteView when ViewMode is COMPLETE_SUCCESS
     */
    @Test
    @Throws(Exception::class)
    fun progressViewVisibilityTestCase1() {
        assertThat(viewModel.progressViewVisibility.value, equalTo(View.GONE))

        val connection = Connection().apply {
            guid = "guid1"
            status = "${ConnectionStatus.ACTIVE}"
            accessToken = "accessToken"
            code = "demobank1"
            name = "Demobank1"
        }
        given(mockConnectionsRepository.getByGuid("guid1")).willReturn(connection)
        given(mockConnectionsRepository.getConnectionsCount("demobank1")).willReturn(1L)

        viewModel.setInitialData(initialConnectData = null, connectionGuid = "guid1")
        viewModel.authFinishedWithSuccess(connectionId = "1", accessToken = "access_token")
        viewModel.onResume()

        assertThat(viewModel.completeViewVisibility.value, equalTo(View.VISIBLE))
    }

    /**
     * test shouldShowCompleteView when ViewMode is COMPLETE_ERROR
     */
    @Test
    @Throws(Exception::class)
    fun progressViewVisibilityTestCase2() {
        assertThat(viewModel.completeViewVisibility.value, equalTo(View.GONE))

        val connection = Connection().apply {
            guid = "guid1"
            status = "${ConnectionStatus.ACTIVE}"
            accessToken = "accessToken"
            code = "demobank1"
            name = "Demobank1"
        }
        given(mockConnectionsRepository.getByGuid("guid1")).willReturn(connection)
        given(mockConnectionsRepository.getConnectionsCount("demobank1")).willReturn(1L)

        viewModel.setInitialData(initialConnectData = null, connectionGuid = "guid1")
        viewModel.webAuthFinishError(errorClass = "ERROR", errorMessage = "ERROR")
        viewModel.onResume()

        assertThat(viewModel.completeViewVisibility.value, equalTo(View.VISIBLE))
    }

    @Test
    @Throws(Exception::class)
    fun titleResTestCase1() {
        //given
        val connection = Connection().apply {
            guid = "guid1"
            status = "${ConnectionStatus.ACTIVE}"
            accessToken = "accessToken"
            code = "demobank1"
            name = "Demobank1"
        }
        given(mockConnectionsRepository.getByGuid("guid1")).willReturn(connection)

        //when
        viewModel.setInitialData(initialConnectData = null, connectionGuid = "guid1")

        //then
        assertThat(viewModel.titleRes, equalTo(R.string.actions_reconnect))
    }

    @Test
    @Throws(Exception::class)
    fun titleResTestCase2() {
        //when
        viewModel.setInitialData(initialConnectData = null, connectionGuid = null)

        //then
        assertThat(viewModel.titleRes, equalTo(R.string.connections_new_connection))
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
}
