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
package com.saltedge.authenticator.features.actions

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.View
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.CONNECTIONS_REQUEST_CODE
import com.saltedge.authenticator.app.KEY_CONNECTION_GUID
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.constants.ERROR_CLASS_API_RESPONSE
import com.saltedge.authenticator.sdk.model.appLink.ActionAppLinkData
import com.saltedge.authenticator.sdk.model.authorization.AuthorizationIdentifier
import com.saltedge.authenticator.sdk.model.connection.ConnectionAndKey
import com.saltedge.authenticator.sdk.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.model.response.SubmitActionResponseData
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManagerAbs
import com.saltedge.authenticator.testTools.TestAppTools
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import java.security.KeyPair
import java.security.PrivateKey

@RunWith(RobolectricTestRunner::class)
class SubmitActionViewModelTest {

    private lateinit var viewModel: SubmitActionViewModel
    private val mockConnectionsRepository = mock(ConnectionsRepositoryAbs::class.java)
    private val mockKeyStoreManager = mock(KeyStoreManagerAbs::class.java)
    private val mockApiManager = mock(AuthenticatorApiManagerAbs::class.java)
    private val mockPrivateKey = Mockito.mock(PrivateKey::class.java)

    @Before
    fun setUp() {
        viewModel = SubmitActionViewModel(
            appContext = TestAppTools.applicationContext,
            connectionsRepository = mockConnectionsRepository,
            keyStoreManager = mockKeyStoreManager,
            apiManager = mockApiManager
        )
    }

    /**
     * Show complete view when authorizationId and connectionId are empty
     */
    @Test
    @Throws(Exception::class)
    fun onActionInitSuccessCase1() {
        //given
        val connectUrlData = SubmitActionResponseData(
            success = true,
            authorizationId = "",
            connectionId = ""
        )

        //when
        viewModel.onActionInitSuccess(response = connectUrlData)

        //then
        assertThat(viewModel.completeViewVisibility.value, equalTo(View.VISIBLE))
        assertThat(viewModel.actionProcessingVisibility.value, equalTo(View.GONE))
//        assertThat(viewModel.iconResId.value, equalTo(R.drawable.ic_status_success))
//        assertThat(viewModel.completeTitleResId.value, equalTo(R.string.action_feature_title))
//        assertThat(
//            viewModel.completeDescriptionResId.value,
//            equalTo(R.string.action_feature_description)
//        )
//        assertThat(viewModel.mainActionTextResId.value, equalTo(android.R.string.ok))
    }

    /**
     * Test onActionInitSuccess when success is true and connectionId with authorizationId are not empty
     */
    @Test
    @Throws(Exception::class)
    fun onActionInitSuccessCase2() {
        //given
        val connectUrlData = SubmitActionResponseData(
            success = true,
            authorizationId = "authorizationId",
            connectionId = "connectionId"
        )

        //when
        viewModel.onActionInitSuccess(response = connectUrlData)

        //then
        assertThat(
            viewModel.setResultAuthorizationIdentifier.value,
            equalTo(
                AuthorizationIdentifier(
                    authorizationID = "authorizationId",
                    connectionID = "connectionId"
                )
            )
        )
    }

    /**
     * Test onActionInitSuccess when success is false and connectionId with authorizationId are empty
     */
    @Test
    @Throws(Exception::class)
    fun onConnectionInitSuccessTestCase3() {
        //given
        val actionData = SubmitActionResponseData(
            success = false,
            connectionId = "",
            authorizationId = ""
        )

        //when
        viewModel.onActionInitSuccess(response = actionData)

        //then
        Mockito.never()
    }

    @Test
    @Throws(Exception::class)
    fun onConnectionInitFailureTest() {
        //when
        viewModel.onActionInitFailure(
            error = ApiErrorData(
                errorMessage = "test error",
                errorClassName = ERROR_CLASS_API_RESPONSE
            )
        )

        //then
        assertThat(viewModel.completeViewVisibility.value, equalTo(View.VISIBLE))
        assertThat(viewModel.actionProcessingVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.iconResId.value, equalTo(R.drawable.ic_status_error))
        assertThat(viewModel.completeTitleResId.value, equalTo(R.string.action_error_title))
        assertThat(
            viewModel.completeDescriptionResId.value,
            equalTo(R.string.action_error_description)
        )
        assertThat(viewModel.mainActionTextResId.value, equalTo(R.string.actions_done))
        assertThat(
            viewModel.onShowErrorEvent.value,
            equalTo(ViewModelEvent("test error"))
        )
    }

    @Test
    @Throws(Exception::class)
    fun viewModeClassTest() {
        val targetArray = arrayOf(
            ViewMode.START,
            ViewMode.PROCESSING,
            ViewMode.ACTION_ERROR,
            ViewMode.SELECT
        )
        assertThat(ViewMode.values(), equalTo(targetArray))
        assertThat(ViewMode.valueOf("START"), equalTo(ViewMode.START))
        assertThat(ViewMode.valueOf("PROCESSING"), equalTo(ViewMode.PROCESSING))
        assertThat(ViewMode.valueOf("ACTION_ERROR"), equalTo(ViewMode.ACTION_ERROR))
        assertThat(ViewMode.valueOf("SELECT"), equalTo(ViewMode.SELECT))
    }

    /**
     * Test onViewClick when ActionAppLinkData has returnTo
     *
     * @see ActionAppLinkData
     */
    @Test
    @Throws(Exception::class)
    fun onViewClickTestCase1() {
        //given
        val connection = Connection().apply {
            guid = "guid1"
            accessToken = ""
            code = "demobank1"
            name = "Demobank1"
        }
        Mockito.`when`(mockConnectionsRepository.getByConnectUrl("https://www.fentury.com/")).thenReturn(listOf(connection))
        viewModel.setInitialData(
            actionAppLinkData = ActionAppLinkData(
                actionUUID = "123456",
                connectUrl = "https://www.fentury.com/",
                returnTo = "https://www.saltedge.com/"
            )
        )

        //when
        viewModel.onViewClick(R.id.actionView)

        //then
        assertNotNull(viewModel.onCloseEvent.value)
        assertThat(
            viewModel.onOpenLinkEvent.value,
            equalTo(ViewModelEvent(Uri.parse("https://www.saltedge.com/")))
        )
    }

    /**
     * Test onViewClick when ActionAppLinkData hasn't returnTo
     *
     * @see ActionAppLinkData
     */
    @Test
    @Throws(Exception::class)
    fun onViewClickTestCase2() {
        //given
        viewModel.setInitialData(
            actionAppLinkData = ActionAppLinkData(
                actionUUID = "123456",
                connectUrl = "https://www.fentury.com/",
                returnTo = ""
            )
        )

        //when
        viewModel.onViewClick(R.id.actionView)

        //then
        assertNotNull(viewModel.onCloseEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTestCase3() {
        //given invalid viewId
        viewModel.setInitialData(
            actionAppLinkData = ActionAppLinkData(
                actionUUID = "123456",
                connectUrl = "https://www.fentury.com/",
                returnTo = ""
            )
        )

        //when
        viewModel.onViewClick(R.id.altActionView)

        //then
        assertNull(viewModel.onCloseEvent.value)
    }

    /**
     * Test onViewCreated when ViewMode is ACTION_ERROR
     */
    @Test
    @Throws(Exception::class)
    fun onViewCreatedTestCase1() {
        //given
        viewModel.setInitialData(
            actionAppLinkData = ActionAppLinkData(
                actionUUID = "123456",
                connectUrl = "https://www.fentury.com/",
                returnTo = "https://www.saltedge.com/"
            )
        )

        //when
        viewModel.onViewCreated()

        //then
        assertThat(viewModel.completeViewVisibility.value, equalTo(View.VISIBLE))
        assertThat(viewModel.actionProcessingVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.iconResId.value, equalTo(R.drawable.ic_status_error))
        assertThat(viewModel.completeTitleResId.value, equalTo(R.string.action_error_title))
        assertThat(
            viewModel.completeDescriptionResId.value,
            equalTo(R.string.action_error_description)
        )
        assertThat(viewModel.mainActionTextResId.value, equalTo(R.string.actions_done))
    }

    /**
     * Test onViewCreated when ViewMode is PROCESSING
     */
    @Test
    @Throws(Exception::class)
    fun onViewCreatedTestCase2() {
        //given
        val connection = Connection().apply {
            guid = "guid1"
            accessToken = "accessToken"
            code = "demobank1"
            name = "Demobank1"
        }
        Mockito.`when`(mockConnectionsRepository.getByConnectUrl("https://www.fentury.com/")).thenReturn(listOf(connection))
        Mockito.`when`(mockKeyStoreManager.createConnectionAndKeyModel(connection)).thenReturn(
            ConnectionAndKey(connection, mockPrivateKey)
        )
        Mockito.doReturn(KeyPair(null, mockPrivateKey)).`when`(mockKeyStoreManager).getKeyPair(
            Mockito.anyString()
        )
        viewModel.setInitialData(
            actionAppLinkData = ActionAppLinkData(
                actionUUID = "123456",
                connectUrl = "https://www.fentury.com/",
                returnTo = "https://www.saltedge.com/"
            )
        )

        //when
        viewModel.onViewCreated()

        //then
        assertThat(viewModel.completeViewVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.actionProcessingVisibility.value, equalTo(View.VISIBLE))
    }

    /**
     * Test onViewCreated when ViewMode is ACTION_SUCCESS
     */
    @Test
    @Throws(Exception::class)
    fun onViewCreatedTestCase3() {
        //given
        val connectUrlData = SubmitActionResponseData(
            success = true,
            authorizationId = "authorizationId",
            connectionId = "connectionId"
        )
        viewModel.onActionInitSuccess(response = connectUrlData)

        assertThat(
            viewModel.setResultAuthorizationIdentifier.value,
            equalTo(
                AuthorizationIdentifier(
                    authorizationID = "authorizationId",
                    connectionID = "connectionId"
                )
            )
        )

        //when
        viewModel.onViewCreated()

        //then
//        assertThat(viewModel.completeViewVisibility.value, equalTo(View.VISIBLE))
//        assertThat(viewModel.actionProcessingVisibility.value, equalTo(View.GONE))
//        assertThat(viewModel.iconResId.value, equalTo(R.drawable.ic_status_success))
//        assertThat(viewModel.completeTitleResId.value, equalTo(R.string.action_feature_title))
//        assertThat(
//            viewModel.completeDescriptionResId.value,
//            equalTo(R.string.action_feature_description)
//        )
//        assertThat(viewModel.mainActionTextResId.value, equalTo(android.R.string.ok))
    }

    /**
     * Test onViewCreated when ViewMode is START
     */
    @Test
    @Throws(Exception::class)
    fun onViewCreatedTestCase4() {
        //given
        val connection = Connection().apply {
            guid = "guid1"
            accessToken = ""
            code = "demobank1"
            name = "Demobank1"
        }
        Mockito.`when`(mockConnectionsRepository.getByConnectUrl("https://www.fentury.com/")).thenReturn(listOf(connection))
        Mockito.`when`(mockKeyStoreManager.createConnectionAndKeyModel(connection)).thenReturn(ConnectionAndKey(connection, mockPrivateKey))
        viewModel.setInitialData(
            actionAppLinkData = ActionAppLinkData(
                actionUUID = "123456",
                connectUrl = "https://www.fentury.com/",
                returnTo = "https://www.saltedge.com/"
            )
        )

        //when
        viewModel.onViewCreated()

        //then
        Mockito.verify(mockApiManager).sendAction(
            actionUUID = "123456",
            connectionAndKey = ConnectionAndKey(
                connection = connection,
                key = mockPrivateKey
            ),
            resultCallback = viewModel
        )
    }

    @Test
    @Throws(Exception::class)
    fun onActivityResultTestCase1() {
        //given wrong requestCode
        val requestCode = 0
        val resultCode = Activity.RESULT_OK
        val intent: Intent = Intent().putExtra(KEY_CONNECTION_GUID, "1")

        //when
        viewModel.onActivityResult(requestCode, resultCode, intent)

        //then
        assertNotNull(viewModel.onCloseEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onActivityResultTestCase2() {
        //given wrong resultCode
        val requestCode = CONNECTIONS_REQUEST_CODE
        val resultCode = Activity.RESULT_CANCELED
        val intent: Intent = Intent().putExtra(KEY_CONNECTION_GUID, "1")

        //when
        viewModel.onActivityResult(requestCode, resultCode, intent)

        //then
        assertNotNull(viewModel.onCloseEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onActivityResultTestCase3() {
        //given wrong Intent
        val requestCode = CONNECTIONS_REQUEST_CODE
        val resultCode = Activity.RESULT_OK
        val intent: Intent? = null

        //when
        viewModel.onActivityResult(requestCode, resultCode, intent)

        //then
        assertNotNull(viewModel.onCloseEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onActivityResultTestCase4() {
        //given wrong connection guid
        val requestCode = CONNECTIONS_REQUEST_CODE
        val resultCode = Activity.RESULT_OK
        val intent: Intent = Intent().putExtra(KEY_CONNECTION_GUID, "")

        //when
        viewModel.onActivityResult(requestCode, resultCode, intent)

        //then
        assertThat(viewModel.completeViewVisibility.value, equalTo(View.VISIBLE))
    }

    @Test
    @Throws(Exception::class)
    fun onActivityResultTestCase5() {
        //given
        val connection = Connection().apply {
            guid = "guid1"
            accessToken = ""
            code = "demobank1"
            name = "Demobank1"
        }
        Mockito.`when`(mockConnectionsRepository.getByConnectUrl("https://www.fentury.com/")).thenReturn(listOf(connection))
        Mockito.`when`(mockConnectionsRepository.getByGuid("guid1")).thenReturn(connection)
        Mockito.`when`(mockKeyStoreManager.createConnectionAndKeyModel(connection)).thenReturn(ConnectionAndKey(connection, mockPrivateKey))
        viewModel.setInitialData(
            actionAppLinkData = ActionAppLinkData(
                actionUUID = "123456",
                connectUrl = "https://www.fentury.com/",
                returnTo = "https://www.saltedge.com/"
            )
        )
        val requestCode = CONNECTIONS_REQUEST_CODE
        val resultCode = Activity.RESULT_OK
        val intent: Intent = Intent().putExtra(KEY_CONNECTION_GUID, "guid1")

        //when
        viewModel.onActivityResult(requestCode, resultCode, intent)

        //then
        assertThat(viewModel.actionProcessingVisibility.value, equalTo(View.VISIBLE))
        Mockito.verify(mockApiManager).sendAction(
            actionUUID = "123456",
            connectionAndKey = ConnectionAndKey(
                connection = connection,
                key = mockPrivateKey
            ),
            resultCallback = viewModel
        )
    }
}
