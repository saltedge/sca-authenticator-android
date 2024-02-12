/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.actions

import android.net.Uri
import android.os.Bundle
import android.view.View
import com.saltedge.android.test_tools.ViewModelTest
import com.saltedge.authenticator.R
import com.saltedge.authenticator.TestAppTools
import com.saltedge.authenticator.core.api.ERROR_CLASS_API_RESPONSE
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.ActionAppLinkData
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.features.connections.common.ConnectionItem
import com.saltedge.authenticator.features.connections.select.SelectConnectionsFragment
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.location.DeviceLocationManagerAbs
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.api.model.authorization.AuthorizationIdentifier
import com.saltedge.authenticator.sdk.api.model.response.SubmitActionResponseData
import com.saltedge.authenticator.sdk.constants.API_V1_VERSION
import com.saltedge.authenticator.sdk.v2.ScaServiceClientAbs
import com.saltedge.authenticator.sdk.v2.api.API_V2_VERSION
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import java.security.PrivateKey

@RunWith(RobolectricTestRunner::class)
class SubmitActionViewModelTest : ViewModelTest() {

    private lateinit var viewModel: SubmitActionViewModel
    private val mockConnectionsRepository = mock(ConnectionsRepositoryAbs::class.java)
    private val mockKeyStoreManager = mock(KeyManagerAbs::class.java)
    private val mockApiManagerV1 = mock(AuthenticatorApiManagerAbs::class.java)
    private val mockApiManagerV2 = mock(ScaServiceClientAbs::class.java)
    private val mockPrivateKey = mock(PrivateKey::class.java)
    private val mockLocationManager = mock(DeviceLocationManagerAbs::class.java)

    private val connectionV1 = Connection().apply {
        guid = "guid1"
        accessToken = "token"
        code = "demobank1"
        name = "Demobank1"
        connectUrl = "https://www.fentury.com/"
        apiVersion = API_V1_VERSION
    }
    private val richConnectionV1 = RichConnection(connectionV1, mockPrivateKey)
    private val connectionV2 = Connection().apply {
        guid = "guid2"
        accessToken = "token"
        code = "2"
        name = "Demobank2"
        connectUrl = "https://www.fentury.com/"
        apiVersion = API_V2_VERSION
    }
    private val richConnectionV2 = RichConnection(connectionV2, mockPrivateKey)
    private val appLinkDataV1 = ActionAppLinkData(
        apiVersion = API_V1_VERSION,
        providerID = null,
        actionIdentifier = "123456",
        connectUrl = "https://www.fentury.com/",
        returnTo = "https://www.saltedge.com/"
    )
    private val appLinkDataV2 = ActionAppLinkData(
        apiVersion = API_V2_VERSION,
        providerID = connectionV2.code,
        actionIdentifier = "987",
        connectUrl = null,
        returnTo = null
    )

    @Before
    fun setUp() {
        Mockito.`when`(mockConnectionsRepository.getAllActiveByConnectUrl(connectionV1.connectUrl)).thenReturn(listOf(connectionV1))
        Mockito.`when`(mockConnectionsRepository.getAllActiveByConnectUrl("invalid_url")).thenReturn(emptyList())
        Mockito.`when`(mockConnectionsRepository.getAllActiveByProvider(connectionV2.code)).thenReturn(listOf(connectionV2))
        Mockito.`when`(mockConnectionsRepository.getByGuid(connectionV1.guid)).thenReturn(connectionV1)
        Mockito.`when`(mockConnectionsRepository.getByGuid(connectionV2.guid)).thenReturn(connectionV2)
        Mockito.`when`(mockKeyStoreManager.enrichConnection(connectionV1, addProviderKey = false)).thenReturn(richConnectionV1)
        Mockito.`when`(mockKeyStoreManager.enrichConnection(connectionV2, addProviderKey = true)).thenReturn(richConnectionV2)

        viewModel = SubmitActionViewModel(
            appContext = TestAppTools.applicationContext,
            connectionsRepository = mockConnectionsRepository,
            keyStoreManager = mockKeyStoreManager,
            apiManagerV1 = mockApiManagerV1,
            apiManagerV2 = mockApiManagerV2,
            locationManager = mockLocationManager
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

    @Test
    @Throws(Exception::class)
    fun onViewCreatedTestCase1() {
        //given invalid connect url
        viewModel.setInitialData(actionAppLinkData = appLinkDataV1.copy(connectUrl = "invalid_url"))

        //when
        viewModel.onViewCreated()

        //then
        assertThat(viewModel.completeViewVisibility.value, equalTo(View.VISIBLE))
        assertThat(viewModel.actionProcessingVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.iconResId.value, equalTo(R.drawable.ic_status_error))
        assertThat(viewModel.completeTitleResId.value, equalTo(R.string.action_error_title))
        assertThat(viewModel.completeDescription.value, equalTo(TestAppTools.applicationContext.getString(R.string.errors_actions_no_connections_link_app)))
        assertThat(viewModel.mainActionTextResId.value, equalTo(R.string.actions_done))
        Mockito.verifyNoInteractions(mockApiManagerV1, mockApiManagerV2)
    }

    /**
     * Test onViewCreated when ViewMode is PROCESSING
     */
    @Test
    @Throws(Exception::class)
    fun onViewCreatedTestCase2() {
        //given
        viewModel.setInitialData(actionAppLinkData = appLinkDataV1)
        viewModel.onViewCreated()
        Mockito.clearInvocations(mockApiManagerV1, mockApiManagerV2)

        //when
        viewModel.onViewCreated()

        //then
        assertThat(viewModel.completeViewVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.actionProcessingVisibility.value, equalTo(View.VISIBLE))
        Mockito.verifyNoInteractions(mockApiManagerV1, mockApiManagerV2)
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
        Mockito.verifyNoInteractions(mockApiManagerV1, mockApiManagerV2)
    }

    @Test
    @Throws(Exception::class)
    fun onViewCreatedTestCase4() {
        //given
        viewModel.setInitialData(actionAppLinkData = appLinkDataV1)

        //when
        viewModel.onViewCreated()

        //then
        Mockito.verify(mockApiManagerV1).sendAction(
            actionUUID = appLinkDataV1.actionIdentifier,
            connectionAndKey = richConnectionV1,
            resultCallback = viewModel
        )
        Mockito.verifyNoInteractions(mockApiManagerV2)
    }

    @Test
    @Throws(Exception::class)
    fun onViewCreatedTestCase5() {
        //given
        viewModel.setInitialData(actionAppLinkData = appLinkDataV2)

        //when
        viewModel.onViewCreated()

        //then
        Mockito.verify(mockApiManagerV2).requestCreateAuthorizationForAction(
            actionID = appLinkDataV2.actionIdentifier,
            richConnection = richConnectionV2,
            callback = viewModel
        )
        Mockito.verifyNoInteractions(mockApiManagerV1)
    }

    @Test
    @Throws(Exception::class)
    fun onViewCreatedTestCase6() {
        //given
        Mockito.`when`(mockConnectionsRepository.getAllActiveByProvider(connectionV2.code)).thenReturn(listOf(connectionV1, connectionV2))
        viewModel.setInitialData(actionAppLinkData = appLinkDataV2)

        //when
        viewModel.onViewCreated()

        //then
        Mockito.verifyNoInteractions(mockApiManagerV1, mockApiManagerV2)
        val bundle: Bundle = viewModel.showConnectionsSelectorFragmentEvent.value!!.peekContent()
        val connections = bundle.getSerializable(SelectConnectionsFragment.KEY_CONNECTIONS) as List<ConnectionItem>
        assertThat(connections.size, equalTo(2))
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
        viewModel.setInitialData(actionAppLinkData = appLinkDataV1)

        //when
        viewModel.onViewClick(R.id.actionView)

        //then
        Assert.assertNotNull(viewModel.onCloseEvent.value)
        assertThat(viewModel.onOpenLinkEvent.value, equalTo(ViewModelEvent(Uri.parse(appLinkDataV1.returnTo))))
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
        viewModel.setInitialData(actionAppLinkData = appLinkDataV1.copy(returnTo = ""))

        //when
        viewModel.onViewClick(R.id.actionView)

        //then
        Assert.assertNotNull(viewModel.onCloseEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTestCase3() {
        //given invalid viewId
        viewModel.setInitialData(actionAppLinkData = appLinkDataV1.copy(returnTo = ""))

        //when
        viewModel.onViewClick(R.id.altActionView)

        //then
        Assert.assertNull(viewModel.onCloseEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onConnectionSelectedTestCase1() {
        //given
        viewModel.setInitialData(actionAppLinkData = appLinkDataV1)

        //when
        viewModel.onConnectionSelected(guid = "invalid")

        //then
        assertThat(viewModel.completeViewVisibility.value, equalTo(View.VISIBLE))
        Mockito.verifyNoInteractions(mockApiManagerV1, mockApiManagerV2)
    }

    @Test
    @Throws(Exception::class)
    fun onConnectionSelectedTestCase2() {
        //given
        viewModel.setInitialData(actionAppLinkData = appLinkDataV1)

        //when
        viewModel.onConnectionSelected(connectionV1.guid)

        //then
        assertThat(viewModel.actionProcessingVisibility.value, equalTo(View.VISIBLE))
        Mockito.verify(mockApiManagerV1).sendAction(
            actionUUID = appLinkDataV1.actionIdentifier,
            connectionAndKey = richConnectionV1,
            resultCallback = viewModel
        )
        Mockito.verifyNoInteractions(mockApiManagerV2)
    }

    @Test
    @Throws(Exception::class)
    fun onConnectionSelectedTestCase3() {
        //given
        viewModel.setInitialData(actionAppLinkData = appLinkDataV1)

        //when
        viewModel.onConnectionSelected("")

        //then
        assertThat(viewModel.actionProcessingVisibility.value, equalTo(View.VISIBLE))
        Mockito.verifyNoInteractions(mockApiManagerV1, mockApiManagerV2)
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
        assertThat(viewModel.completeDescription.value, equalTo(TestAppTools.applicationContext.getString(R.string.errors_actions_not_success)))
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
            equalTo(AuthorizationIdentifier(authorizationID = "authorizationId", connectionID = "connectionId"))
        )
    }

    @Test
    @Throws(Exception::class)
    fun onConnectionInitFailureTest() {
        //when
        viewModel.onActionInitFailure(error = ApiErrorData(errorMessage = "test error", errorClassName = ERROR_CLASS_API_RESPONSE))

        //then
        assertThat(viewModel.completeViewVisibility.value, equalTo(View.VISIBLE))
        assertThat(viewModel.actionProcessingVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.iconResId.value, equalTo(R.drawable.ic_status_error))
        assertThat(viewModel.completeTitleResId.value, equalTo(R.string.action_error_title))
        assertThat(viewModel.completeDescription.value, equalTo("test error"))
        assertThat(viewModel.mainActionTextResId.value, equalTo(R.string.actions_done))
    }

    @Test
    @Throws(Exception::class)
    fun onAuthorizationCreateSuccessCase1() {
        //when
        viewModel.onAuthorizationCreateSuccess(authorizationID = "", connectionID = "")

        //then
        assertThat(viewModel.completeViewVisibility.value, equalTo(View.VISIBLE))
        assertThat(viewModel.actionProcessingVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.iconResId.value, equalTo(R.drawable.ic_status_error))
        assertThat(viewModel.completeTitleResId.value, equalTo(R.string.action_error_title))
        assertThat(viewModel.completeDescription.value, equalTo(TestAppTools.applicationContext.getString(R.string.errors_actions_not_success)))
        assertThat(viewModel.mainActionTextResId.value, equalTo(R.string.actions_done))
    }

    @Test
    @Throws(Exception::class)
    fun onAuthorizationCreateSuccessCase2() {
        //when
        viewModel.onAuthorizationCreateSuccess(authorizationID = "authorizationId", connectionID = "connectionId")

        //then
        assertThat(
            viewModel.setResultAuthorizationIdentifier.value,
            equalTo(AuthorizationIdentifier(authorizationID = "authorizationId", connectionID = "connectionId"))
        )
    }

    @Test
    @Throws(Exception::class)
    fun onAuthorizationCreateFailureTest() {
        //when
        viewModel.onAuthorizationCreateFailure(error = ApiErrorData(errorMessage = "test error", errorClassName = ERROR_CLASS_API_RESPONSE))

        //then
        assertThat(viewModel.completeViewVisibility.value, equalTo(View.VISIBLE))
        assertThat(viewModel.actionProcessingVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.iconResId.value, equalTo(R.drawable.ic_status_error))
        assertThat(viewModel.completeTitleResId.value, equalTo(R.string.action_error_title))
        assertThat(viewModel.completeDescription.value, equalTo("test error"))
        assertThat(viewModel.mainActionTextResId.value, equalTo(R.string.actions_done))
    }
}
