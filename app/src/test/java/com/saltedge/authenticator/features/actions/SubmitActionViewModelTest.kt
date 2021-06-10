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

import android.net.Uri
import android.view.View
import com.saltedge.android.test_tools.ViewModelTest
import com.saltedge.authenticator.R
import com.saltedge.authenticator.TestAppTools
import com.saltedge.authenticator.core.api.ERROR_CLASS_API_RESPONSE
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.ActionAppLinkData
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.api.model.authorization.AuthorizationIdentifier
import com.saltedge.authenticator.sdk.api.model.response.SubmitActionResponseData
import com.saltedge.authenticator.sdk.constants.API_V1_VERSION
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import java.security.KeyPair
import java.security.PrivateKey

@RunWith(RobolectricTestRunner::class)
class SubmitActionViewModelTest : ViewModelTest() {

    private lateinit var viewModel: SubmitActionViewModel
    private val mockConnectionsRepository = mock(ConnectionsRepositoryAbs::class.java)
    private val mockKeyStoreManager = mock(KeyManagerAbs::class.java)
    private val mockApiManager = mock(AuthenticatorApiManagerAbs::class.java)
    private val mockPrivateKey = mock(PrivateKey::class.java)

    private val connectionV1 = Connection().apply {
        guid = "guid1"
        accessToken = "token"
        code = "demobank1"
        name = "Demobank1"
        connectUrl = "https://www.fentury.com/"
        apiVersion = API_V1_VERSION
    }
    private val richConnectionV1 = RichConnection(connectionV1, mockPrivateKey)

        @Before
    fun setUp() {
        Mockito.`when`(mockConnectionsRepository.getByConnectUrl(connectionV1.connectUrl)).thenReturn(listOf(connectionV1))
        Mockito.`when`(mockConnectionsRepository.getByConnectUrl("invalid_url")).thenReturn(emptyList())
        Mockito.`when`(mockConnectionsRepository.getByGuid(connectionV1.guid)).thenReturn(connectionV1)
        Mockito.`when`(mockKeyStoreManager.enrichConnection(connectionV1, addProviderKey = false)).thenReturn(richConnectionV1)

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
        assertThat(viewModel.iconResId.value, equalTo(R.drawable.ic_status_error))
        assertThat(viewModel.completeTitleResId.value, equalTo(R.string.action_error_title))
        assertThat(
            viewModel.completeDescription.value,
            equalTo(TestAppTools.applicationContext.getString(R.string.errors_actions_not_success))
        )
        assertThat(viewModel.mainActionTextResId.value, equalTo(R.string.actions_done))
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
            viewModel.completeDescription.value,
            equalTo("test error")
        )
        assertThat(viewModel.mainActionTextResId.value, equalTo(R.string.actions_done))
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
        viewModel.setInitialData(
            actionAppLinkData = ActionAppLinkData(
                actionUUID = "123456",
                connectUrl = connectionV1.connectUrl,
                returnTo = "https://www.saltedge.com/"
            )
        )

        //when
        viewModel.onViewClick(R.id.actionView)

        //then
        Assert.assertNotNull(viewModel.onCloseEvent.value)
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
                connectUrl = connectionV1.connectUrl,
                returnTo = ""
            )
        )

        //when
        viewModel.onViewClick(R.id.actionView)

        //then
        Assert.assertNotNull(viewModel.onCloseEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTestCase3() {
        //given invalid viewId
        viewModel.setInitialData(
            actionAppLinkData = ActionAppLinkData(
                actionUUID = "123456",
                connectUrl = connectionV1.connectUrl,
                returnTo = ""
            )
        )

        //when
        viewModel.onViewClick(R.id.altActionView)

        //then
        Assert.assertNull(viewModel.onCloseEvent.value)
    }

    /**
     * Test onViewCreated when ViewMode is ACTION_ERROR and no connections in db
     */
    @Test
    @Throws(Exception::class)
    fun onViewCreatedTestCase1() {
        //given
        viewModel.setInitialData(
            actionAppLinkData = ActionAppLinkData(
                actionUUID = "123456",
                connectUrl = "invalid_url",
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
            viewModel.completeDescription.value,
            equalTo(TestAppTools.applicationContext.getString(R.string.errors_actions_no_connections_link_app))
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
            equalTo(AuthorizationIdentifier(authorizationID = "authorizationId", connectionID = "connectionId"))
        )

        //when
        viewModel.onViewCreated()

        //then
        assertThat(viewModel.completeViewVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.actionProcessingVisibility.value, equalTo(View.VISIBLE))
    }

    /**
     * Test onViewCreated when ViewMode is START
     */
    @Test
    @Throws(Exception::class)
    fun onViewCreatedTestCase4() {
        //given
        viewModel.setInitialData(
            actionAppLinkData = ActionAppLinkData(
                actionUUID = "123456",
                connectUrl = connectionV1.connectUrl,
                returnTo = "https://www.saltedge.com/"
            )
        )

        //when
        viewModel.onViewCreated()

        //then
        Mockito.verify(mockApiManager).sendAction(
            actionUUID = "123456",
            connectionAndKey = richConnectionV1,
            resultCallback = viewModel
        )
    }

    @Test
    @Throws(Exception::class)
    fun onActivityResultTestCase1() {
        //given
        viewModel.setInitialData(
            actionAppLinkData = ActionAppLinkData(
                actionUUID = "123456",
                connectUrl = connectionV1.connectUrl,
                returnTo = "https://www.saltedge.com/"
            )
        )

        //when
        viewModel.showConnectionSelector("guid2")

        //then
        assertThat(viewModel.completeViewVisibility.value, equalTo(View.VISIBLE))
    }

    @Test
    @Throws(Exception::class)
    fun onActivityResultTestCase2() {
        //given
        viewModel.setInitialData(
            actionAppLinkData = ActionAppLinkData(
                actionUUID = "123456",
                connectUrl = connectionV1.connectUrl,
                returnTo = "https://www.saltedge.com/"
            )
        )

        //when
        viewModel.showConnectionSelector("guid1")

        //then
        assertThat(viewModel.actionProcessingVisibility.value, equalTo(View.VISIBLE))
        Mockito.verify(mockApiManager).sendAction(
            actionUUID = "123456",
            connectionAndKey = RichConnection(connection = connectionV1, private = mockPrivateKey),
            resultCallback = viewModel
        )
    }
}
