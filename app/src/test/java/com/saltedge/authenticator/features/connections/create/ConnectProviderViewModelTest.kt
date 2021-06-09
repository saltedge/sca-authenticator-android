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
import android.content.pm.PackageManager
import android.view.View
import com.saltedge.android.test_tools.ViewModelTest
import com.saltedge.authenticator.R
import com.saltedge.authenticator.TestAppTools
import com.saltedge.authenticator.app.CAMERA_PERMISSION_REQUEST_CODE
import com.saltedge.authenticator.app.LOCATION_PERMISSION_REQUEST_CODE
import com.saltedge.authenticator.core.api.ERROR_CLASS_API_RESPONSE
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.ConnectAppLinkData
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.location.DeviceLocationManagerAbs
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
class ConnectProviderViewModelTest : ViewModelTest() {

    private lateinit var viewModel: ConnectProviderViewModel
    private val mockInteractor = mock(ConnectProviderInteractorAbs::class.java)
    private val mockLocationManager = mock(DeviceLocationManagerAbs::class.java)

    @Before
    fun setUp() {
        JodaTimeAndroid.init(TestAppTools.applicationContext)
        viewModel = ConnectProviderViewModel(
            appContext = TestAppTools.applicationContext,
            interactor = mockInteractor,
            locationManager = mockLocationManager,
        )
    }

    @Test
    @Throws(Exception::class)
    fun setInitialDataTestCase1() {
        //given
        val initialConnectData = ConnectAppLinkData(configurationUrl = "https://localhost/api/authenticator/v2/configuration")
        val connectionGuid = null
        given(mockInteractor.hasConnection).willReturn(false)
        given(mockInteractor.hasConfigUrl).willReturn(true)

        //when
        viewModel.setInitialData(initialConnectData = initialConnectData, connectionGuid = connectionGuid)


        //then
        verify(mockInteractor).contract = viewModel
        verify(mockInteractor).setInitialData(initialConnectData, connectionGuid)
        assertThat(viewModel.titleRes, equalTo(R.string.connections_new_connection))
    }

    @Test
    @Throws(Exception::class)
    fun setInitialDataTestCase2() {
        //given
        val initialConnectData = ConnectAppLinkData(configurationUrl = "https://localhost/api/authenticator/v2/configuration")
        val connectionGuid = "guid1"
        given(mockInteractor.hasConnection).willReturn(true)
        given(mockInteractor.hasConfigUrl).willReturn(true)

        //when
        viewModel.setInitialData(initialConnectData = initialConnectData, connectionGuid = connectionGuid)


        //then
        verify(mockInteractor).contract = viewModel
        verify(mockInteractor).setInitialData(initialConnectData, connectionGuid)
        assertThat(viewModel.titleRes, equalTo(R.string.actions_reconnect))
    }

    @Test
    @Throws(Exception::class)
    fun onBackPressTestCase1() {
        //given
        assertNull(viewModel.goBackEvent.value)

        //when
        val result = viewModel.onBackPress(webViewCanGoBack = false)

        //then
        assertFalse(result)
        assertNotNull(viewModel.goBackEvent.value)

    }

    @Test
    @Throws(Exception::class)
    fun onBackPressTestCase2() {
        //given
        assertNull(viewModel.goBackEvent.value)

        //when
        val result = viewModel.onBackPress(webViewCanGoBack = true)

        //then
        assertFalse(result)
        assertNotNull(viewModel.goBackEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onBackPressTestCase3() {
        //given
        assertNull(viewModel.goBackEvent.value)
        viewModel.onReceiveAuthenticationUrl()

        //when
        val result = viewModel.onBackPress(webViewCanGoBack = false)

        //then
        assertFalse(result)
        assertNotNull(viewModel.goBackEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onBackPressTestCase4() {
        //given
        assertNull(viewModel.goBackEvent.value)
        viewModel.onReceiveAuthenticationUrl()

        //when
        val result = viewModel.onBackPress(webViewCanGoBack = true)

        //then
        assertTrue(result)
        assertNotNull(viewModel.goBackEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onRequestPermissionsResultTestCase1() {
        //given
        val requestCode = LOCATION_PERMISSION_REQUEST_CODE
        val grantResults = IntArray(2) { PackageManager.PERMISSION_GRANTED }

        //when
        viewModel.onRequestPermissionsResult(requestCode = requestCode, grantResults = grantResults)

        //then
        verify(mockLocationManager).startLocationUpdates(TestAppTools.applicationContext)
    }

    @Test
    @Throws(Exception::class)
    fun onRequestPermissionsResultTestCase2() {
        //given
        val requestCode = CAMERA_PERMISSION_REQUEST_CODE
        val grantResults = IntArray(2) { PackageManager.PERMISSION_GRANTED }

        //when
        viewModel.onRequestPermissionsResult(requestCode = requestCode, grantResults = grantResults)

        //then
        verifyNoInteractions(mockLocationManager)
    }

    @Test
    @Throws(Exception::class)
    fun onRequestPermissionsResultTestCase3() {
        //given
        val requestCode = LOCATION_PERMISSION_REQUEST_CODE
        val grantResults = IntArray(2) { PackageManager.PERMISSION_DENIED }

        //when
        viewModel.onRequestPermissionsResult(requestCode = requestCode, grantResults = grantResults)

        //then
        verifyNoInteractions(mockLocationManager)
    }

    @Test
    @Throws(Exception::class)
    fun onResumeTestCase1() {
        //given
        assertNull(viewModel.goBackEvent.value)
        val authenticationUrl = "https://www.fentury.com"
        given(mockInteractor.authenticationUrl).willReturn(authenticationUrl)
        viewModel.onReceiveAuthenticationUrl()//ViewMode == WEB_ENROLL

        //when
        viewModel.onResume()

        //then
        assertThat(viewModel.onUrlChangedEvent.value, equalTo(ViewModelEvent(authenticationUrl)))
    }

    @Test
    @Throws(Exception::class)
    fun onResumeTestCase2() {
        //given
        val initialConnectData = ConnectAppLinkData(configurationUrl = "https://localhost/api/authenticator/v2/configuration")
        val connectionGuid = null
        given(mockInteractor.hasConnection).willReturn(false)
        given(mockInteractor.hasConfigUrl).willReturn(true)
        viewModel.setInitialData(initialConnectData = initialConnectData, connectionGuid = connectionGuid)//ViewMode == START_NEW_CONNECT

        //when
        viewModel.onResume()

        //then
        verify(mockInteractor).fetchScaConfiguration()
    }

    @Test
    @Throws(Exception::class)
    fun onResumeTestCase3() {
        //given
        val initialConnectData = ConnectAppLinkData(configurationUrl = "https://localhost/api/authenticator/v2/configuration")
        val connectionGuid = "guid1"
        given(mockInteractor.hasConnection).willReturn(true)
        given(mockInteractor.hasConfigUrl).willReturn(true)
        viewModel.setInitialData(initialConnectData = initialConnectData, connectionGuid = connectionGuid)//ViewMode == START_RECONNECT

        //when
        viewModel.onResume()

        //then
        verify(mockInteractor).requestCreateConnection()
    }

    @Test
    @Throws(Exception::class)
    fun onResumeTestCase4() {
        //given
        given(mockInteractor.connectionName).willReturn("connectionName")
        viewModel.onConnectionSuccessAuthentication()//ViewMode == COMPLETE_SUCCESS
        clearInvocations(mockInteractor)

        //when
        viewModel.onResume()

        //then
        verify(mockInteractor).connectionName
        verifyNoMoreInteractions(mockInteractor)
    }

    @Test
    @Throws(Exception::class)
    fun onDestroyTest() {
        //when
        viewModel.onDestroy()

        //then
        verify(mockInteractor).destroyConnectionIfNotAuthorized()
        verify(mockInteractor).contract = null
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTest() {
        viewModel.onViewClick(-1)

        assertNull(viewModel.onCloseEvent.value)

        viewModel.onViewClick(R.id.actionView)

        assertNotNull(viewModel.onCloseEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onDialogActionIdClickTest() {
        viewModel.onDialogActionIdClick(DialogInterface.BUTTON_NEGATIVE)

        assertNull(viewModel.onCloseEvent.value)

        viewModel.onDialogActionIdClick(DialogInterface.BUTTON_POSITIVE)

        assertNotNull(viewModel.onCloseEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onReturnToRedirectTest() {
        //when
        viewModel.onReturnToRedirect(url = "url")

        //then
        verify(mockInteractor).onReceiveReturnToUrl(url = "url")
    }

    @Test
    @Throws(Exception::class)
    fun onReceiveApiErrorTest() {
        //given
        assertNull(viewModel.onShowErrorEvent.value)
        val error = ApiErrorData(errorMessage = "test_message", errorClassName = ERROR_CLASS_API_RESPONSE)

        //when
        viewModel.onReceiveApiError(error)

        //then
        assertThat(viewModel.onShowErrorEvent.value, equalTo(ViewModelEvent(error.errorMessage)))
    }

    @Test
    @Throws(Exception::class)
    fun onReceiveAuthenticationUrlTest() {
        //given
        val authenticationUrl = "https://www.fentury.com"
        given(mockInteractor.authenticationUrl).willReturn(authenticationUrl)

        //when
        viewModel.onReceiveAuthenticationUrl()

        //then
        assertThat(viewModel.webViewVisibility.value, equalTo(View.VISIBLE))
        assertThat(viewModel.progressViewVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.completeViewVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.backActionIconRes.value, equalTo(R.drawable.ic_appbar_action_close))
    }

    @Test
    @Throws(Exception::class)
    fun onConnectionFailAuthenticationTest() {
        //given
        val errorMessage = "not relevant url"

        //when
        viewModel.onConnectionFailAuthentication(errorClass = "WRONG_URL", errorMessage = errorMessage)

        //then
        assertThat(viewModel.statusIconRes.value, equalTo(R.drawable.ic_status_error))
        assertThat(
            viewModel.completeTitle.value.toString(),
            equalTo(TestAppTools.getString(R.string.errors_connection_failed))
        )
        assertThat(viewModel.completeDescription.value, equalTo(errorMessage))
        assertThat(viewModel.mainActionTextRes.value, equalTo(R.string.actions_try_again))

        assertThat(viewModel.webViewVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.progressViewVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.completeViewVisibility.value, equalTo(View.VISIBLE))
        assertNull(viewModel.backActionIconRes.value)
    }

    @Test
    @Throws(Exception::class)
    fun onConnectionSuccessAuthenticationTestCase1() {
        //given
        given(mockInteractor.connectionName).willReturn("ConnectionName")
        given(mockInteractor.geolocationRequired).willReturn(true)
        given(mockLocationManager.locationPermissionsGranted(TestAppTools.applicationContext)).willReturn(true)

        //when
        viewModel.onConnectionSuccessAuthentication()

        //then
        verify(mockLocationManager).startLocationUpdates(TestAppTools.applicationContext)

        assertThat(viewModel.statusIconRes.value, equalTo(R.drawable.ic_status_success))
        assertThat(
            viewModel.completeTitle.value.toString(),
            equalTo(TestAppTools.getString(R.string.connect_status_provider_success).format("ConnectionName"))
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

    @Test
    @Throws(Exception::class)
    fun onConnectionSuccessAuthenticationTestCase2() {
        //given
        given(mockInteractor.connectionName).willReturn("ConnectionName")
        given(mockInteractor.geolocationRequired).willReturn(null)
        given(mockLocationManager.locationPermissionsGranted(TestAppTools.applicationContext)).willReturn(true)

        //when
        viewModel.onConnectionSuccessAuthentication()

        //then
        verifyNoMoreInteractions(mockLocationManager)
    }

    @Test
    @Throws(Exception::class)
    fun onConnectionSuccessAuthenticationTestCase3() {
        //given
        given(mockInteractor.connectionName).willReturn("ConnectionName")
        given(mockInteractor.geolocationRequired).willReturn(true)
        given(mockLocationManager.locationPermissionsGranted(TestAppTools.applicationContext)).willReturn(false)

        //when
        viewModel.onConnectionSuccessAuthentication()

        //then
        verify(mockLocationManager).locationPermissionsGranted(TestAppTools.applicationContext)
        verifyNoMoreInteractions(mockLocationManager)
        assertThat(viewModel.onAskPermissionsEvent.value, equalTo(ViewModelEvent(Unit)))
    }

    @Test
    @Throws(Exception::class)
    fun statusIconResTestCase1() {
        assertThat(viewModel.statusIconRes.value, equalTo(R.drawable.ic_status_error))
    }

    @Test
    @Throws(Exception::class)
    fun statusIconResTestCase2() {
        given(mockInteractor.connectionName).willReturn("connectionName")

        viewModel.onConnectionSuccessAuthentication()

        assertThat(viewModel.statusIconRes.value, equalTo(R.drawable.ic_status_success))
    }

    @Test
    @Throws(Exception::class)
    fun mainActionTextResCase1() {
        assertThat(viewModel.mainActionTextRes.value, equalTo(R.string.actions_try_again))
    }

    @Test
    @Throws(Exception::class)
    fun mainActionTextResCase2() {
        given(mockInteractor.connectionName).willReturn("connectionName")

        viewModel.onConnectionSuccessAuthentication()

        assertThat(viewModel.mainActionTextRes.value, equalTo(R.string.actions_done))
    }

    @Test
    @Throws(Exception::class)
    fun completeTitleCase1() {
        assertThat(viewModel.completeTitle.value.toString(), equalTo(""))
    }

    /**
     * Test completeTitle when isCompleteWithSuccess is true
     */
    @Test
    @Throws(Exception::class)
    fun completeTitleCase2() {
        given(mockInteractor.connectionName).willReturn("ConnectionName")
        viewModel.onConnectionSuccessAuthentication()

        assertThat(
            viewModel.completeTitle.value.toString(),
            equalTo(TestAppTools.getString(R.string.connect_status_provider_success).format("ConnectionName"))
        )
    }

    @Test
    @Throws(Exception::class)
    fun completeDescriptionCase1() {
        assertThat(viewModel.completeDescription.value, equalTo(""))
    }

    @Test
    @Throws(Exception::class)
    fun completeDescriptionCase2() {
        given(mockInteractor.connectionName).willReturn("connectionName")

        viewModel.onConnectionSuccessAuthentication()

        assertThat(
            viewModel.completeDescription.value,
            equalTo(TestAppTools.getString(R.string.connect_status_provider_success_description))
        )
    }

    @Test
    @Throws(Exception::class)
    fun completeDescriptionCase3() {
        given(mockInteractor.connectionName).willReturn("connectionName")

        viewModel.onConnectionFailAuthentication(errorClass = "CLASS", errorMessage = null)

        assertThat(
            viewModel.completeDescription.value,
            equalTo(TestAppTools.getString(R.string.errors_connection_failed_description))
        )
    }

    @Test
    @Throws(Exception::class)
    fun progressViewVisibilityTest() {
        given(mockInteractor.connectionName).willReturn("connectionName")

        assertThat(viewModel.progressViewVisibility.value, equalTo(View.VISIBLE))

        viewModel.onConnectionSuccessAuthentication()

        assertThat(viewModel.progressViewVisibility.value, equalTo(View.GONE))
    }

    @Test
    @Throws(Exception::class)
    fun completeViewVisibilityTest() {
        given(mockInteractor.connectionName).willReturn("connectionName")

        assertThat(viewModel.completeViewVisibility.value, equalTo(View.GONE))

        viewModel.onConnectionSuccessAuthentication()

        assertThat(viewModel.completeViewVisibility.value, equalTo(View.VISIBLE))

        viewModel.onConnectionFailAuthentication(errorClass = "ERROR", errorMessage = "ERROR")

        assertThat(viewModel.completeViewVisibility.value, equalTo(View.VISIBLE))
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
