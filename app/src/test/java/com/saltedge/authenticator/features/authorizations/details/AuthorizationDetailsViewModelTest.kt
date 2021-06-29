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
package com.saltedge.authenticator.features.authorizations.details

import android.content.DialogInterface
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.saltedge.android.test_tools.ViewModelTest
import com.saltedge.authenticator.R
import com.saltedge.authenticator.TestAppTools
import com.saltedge.authenticator.core.api.ERROR_CLASS_SSL_HANDSHAKE
import com.saltedge.authenticator.core.api.model.DescriptionData
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.api.model.error.createRequestError
import com.saltedge.authenticator.features.authorizations.common.AuthorizationItemViewModel
import com.saltedge.authenticator.features.authorizations.common.AuthorizationStatus
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.location.DeviceLocationManagerAbs
import com.saltedge.authenticator.sdk.api.model.authorization.AuthorizationIdentifier
import com.saltedge.authenticator.sdk.constants.API_V1_VERSION
import com.saltedge.authenticator.sdk.v2.api.API_V2_VERSION
import junit.framework.Assert.assertTrue
import junit.framework.TestCase
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.joda.time.DateTime
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AuthorizationDetailsViewModelTest : ViewModelTest() {

    private lateinit var viewModel: AuthorizationDetailsViewModel

    private val mockInteractorV1 = mock(AuthorizationDetailsInteractorAbs::class.java)
    private val mockInteractorV2 = mock(AuthorizationDetailsInteractorAbs::class.java)
    private val viewModel1 = AuthorizationItemViewModel(
        authorizationID = "1",
        authorizationCode = "111",
        title = "Test Authorization",
        description = DescriptionData(text = "Authorization Description"),
        validSeconds = 0,
        endTime = DateTime(0L),
        startTime = DateTime(0L),
        connectionID = "1",
        connectionName = "DemoBank",
        connectionLogoUrl = "",
        status = AuthorizationStatus.PENDING,
        apiVersion = "2",
        geolocationRequired = false
    )
    private val mockLocationManager = mock(DeviceLocationManagerAbs::class.java)

    @Before
    fun setUp() {
        doReturn("GEO:52.506931;13.144558").`when`(mockLocationManager).locationDescription
        viewModel = AuthorizationDetailsViewModel(
            appContext = TestAppTools.applicationContext,
            interactorV1 = mockInteractorV1,
            interactorV2 = mockInteractorV2,
            locationManager = mockLocationManager
        )
        initViewModel(API_V2_VERSION)
    }

    @Test
    @Throws(Exception::class)
    fun setInitialDataTestCase1() {
        //given valid identifier
        val identifier = AuthorizationIdentifier(authorizationID = "1", connectionID = "1")
        val connectionApiVersion = API_V1_VERSION
        doReturn(connectionApiVersion).`when`(mockInteractorV1).connectionApiVersion
        doReturn(connectionApiVersion).`when`(mockInteractorV2).connectionApiVersion
        val testViewModel = AuthorizationDetailsViewModel(
            appContext = TestAppTools.applicationContext,
            interactorV1 = mockInteractorV1,
            interactorV2 = mockInteractorV2,
            locationManager = mockLocationManager
        )

        //when
        testViewModel.setInitialData(identifier = identifier, closeAppOnBackPress = true, titleRes = null)

        //then
        verify(mockInteractorV2).setInitialData(connectionID = "1")
        verify(mockInteractorV2).connectionApiVersion
        verify(mockInteractorV1).setInitialData(connectionID = "1")
        verify(mockInteractorV1).contract = testViewModel
        verify(mockInteractorV1).noConnection
        verify(mockInteractorV1).connectionApiVersion
        verifyNoMoreInteractions(mockInteractorV1)
        verifyNoMoreInteractions(mockInteractorV2)
        assertThat(
            testViewModel.authorizationModel.value,
            equalTo(AuthorizationItemViewModel(
                authorizationID = "1",
                authorizationCode = "",
                title = "",
                description = DescriptionData(),
                validSeconds = 0,
                endTime = DateTime(0L),
                startTime = DateTime(0L),
                connectionID = "1",
                connectionName = "",
                connectionLogoUrl = "",
                status = AuthorizationStatus.LOADING,
                apiVersion = "1",
                geolocationRequired = false
            ))
        )
        assertThat(testViewModel.titleRes, equalTo(R.string.authorization_feature_title))
    }

    @Test
    @Throws(Exception::class)
    fun setInitialDataTestCase2() {
        //given valid identifier
        val identifier = AuthorizationIdentifier(authorizationID = "1", connectionID = "1")
        val connectionApiVersion = API_V2_VERSION
        doReturn(connectionApiVersion).`when`(mockInteractorV1).connectionApiVersion
        doReturn(connectionApiVersion).`when`(mockInteractorV2).connectionApiVersion
        val testViewModel = AuthorizationDetailsViewModel(
            appContext = TestAppTools.applicationContext,
            interactorV1 = mockInteractorV1,
            interactorV2 = mockInteractorV2,
            locationManager = mockLocationManager
        )

        //when
        testViewModel.setInitialData(identifier = identifier, closeAppOnBackPress = true, titleRes = null)

        //then
        verify(mockInteractorV2).setInitialData(connectionID = "1")
        verify(mockInteractorV2, times(2)).connectionApiVersion
        verify(mockInteractorV2).contract = testViewModel
        verify(mockInteractorV2).noConnection
        verifyNoMoreInteractions(mockInteractorV1)
        verifyNoMoreInteractions(mockInteractorV2)
        assertThat(
            testViewModel.authorizationModel.value,
            equalTo(AuthorizationItemViewModel(
                authorizationID = "1",
                authorizationCode = "",
                title = "",
                description = DescriptionData(),
                validSeconds = 0,
                endTime = DateTime(0L),
                startTime = DateTime(0L),
                connectionID = "1",
                connectionName = "",
                connectionLogoUrl = "",
                status = AuthorizationStatus.LOADING,
                apiVersion = "2",
                geolocationRequired = false
            ))
        )
        assertThat(testViewModel.titleRes, equalTo(R.string.authorization_feature_title))
    }

    @Test
    @Throws(Exception::class)
    fun setInitialDataTestCase3() {
        //given invalid identifier
        val identifier = AuthorizationIdentifier(authorizationID = "1", connectionID = "")
        val connectionApiVersion = API_V1_VERSION
        doReturn(connectionApiVersion).`when`(mockInteractorV1).connectionApiVersion
        doReturn(connectionApiVersion).`when`(mockInteractorV2).connectionApiVersion
        doReturn(true).`when`(mockInteractorV1).noConnection
        val testViewModel = AuthorizationDetailsViewModel(
            appContext = TestAppTools.applicationContext,
            interactorV1 = mockInteractorV1,
            interactorV2 = mockInteractorV2,
            locationManager = mockLocationManager
        )

        //when
        testViewModel.setInitialData(identifier = identifier, closeAppOnBackPress = true, titleRes = null)

        //then
        assertThat(testViewModel.authorizationModel.value, equalTo(AuthorizationItemViewModel(
            authorizationID = "1",
            authorizationCode = "",
            title = "",
            description = DescriptionData(),
            validSeconds = 0,
            endTime = DateTime(0L),
            startTime = DateTime(0L),
            connectionID = "",
            connectionName = "",
            connectionLogoUrl = "",
            status = AuthorizationStatus.UNAVAILABLE,
            apiVersion = "1",
            geolocationRequired = false
        )))
    }

    @Test
    @Throws(Exception::class)
    fun setInitialDataTestCase4() {
        //given invalid identifier
        val identifier = AuthorizationIdentifier(authorizationID = "", connectionID = "1")
        val connectionApiVersion = API_V2_VERSION
        doReturn(connectionApiVersion).`when`(mockInteractorV1).connectionApiVersion
        doReturn(connectionApiVersion).`when`(mockInteractorV2).connectionApiVersion
        doReturn(false).`when`(mockInteractorV1).noConnection
        val testViewModel = AuthorizationDetailsViewModel(
            appContext = TestAppTools.applicationContext,
            interactorV1 = mockInteractorV1,
            interactorV2 = mockInteractorV2,
            locationManager = mockLocationManager
        )

        //when
        testViewModel.setInitialData(identifier = identifier, closeAppOnBackPress = true, titleRes = null)

        //then
        assertThat(testViewModel.authorizationModel.value, equalTo(AuthorizationItemViewModel(
            authorizationID = "",
            authorizationCode = "",
            title = "",
            description = DescriptionData(),
            validSeconds = 0,
            endTime = DateTime(0L),
            startTime = DateTime(0L),
            connectionID = "1",
            connectionName = "",
            connectionLogoUrl = "",
            status = AuthorizationStatus.UNAVAILABLE,
            apiVersion = "2",
            geolocationRequired = false
        )))
    }

    @Test
    @Throws(Exception::class)
    fun setInitialDataTestCase5() {
        //given null identifier
        val identifier = null
        val connectionApiVersion = API_V2_VERSION
        doReturn(connectionApiVersion).`when`(mockInteractorV1).connectionApiVersion
        doReturn(connectionApiVersion).`when`(mockInteractorV2).connectionApiVersion
        doReturn(false).`when`(mockInteractorV1).noConnection
        val testViewModel = AuthorizationDetailsViewModel(
            appContext = TestAppTools.applicationContext,
            interactorV1 = mockInteractorV1,
            interactorV2 = mockInteractorV2,
            locationManager = mockLocationManager
        )

        //when
        testViewModel.setInitialData(identifier = identifier, closeAppOnBackPress = true, titleRes = null)

        //then
        assertThat(testViewModel.authorizationModel.value, equalTo(AuthorizationItemViewModel(
            authorizationID = "",
            authorizationCode = "",
            title = "",
            description = DescriptionData(),
            validSeconds = 0,
            endTime = DateTime(0L),
            startTime = DateTime(0L),
            connectionID = "",
            connectionName = "",
            connectionLogoUrl = "",
            status = AuthorizationStatus.UNAVAILABLE,
            apiVersion = "2",
            geolocationRequired = false
        )))
    }

    @Test
    @Throws(Exception::class)
    fun onFragmentResume() {
        //given valid identifier
        val lifecycle = LifecycleRegistry(mock(LifecycleOwner::class.java))
        viewModel.bindLifecycleObserver(lifecycle)

        //when
        lifecycle.currentState = Lifecycle.State.RESUMED

        //then
        verify(mockInteractorV2).startPolling(authorizationID = "1")
        verifyNoMoreInteractions(mockInteractorV1)
    }

    @Test
    @Throws(Exception::class)
    fun onFragmentPauseTest() {
        //given
        val lifecycle = LifecycleRegistry(mock(LifecycleOwner::class.java))
        viewModel.bindLifecycleObserver(lifecycle)

        //when
        lifecycle.currentState = Lifecycle.State.RESUMED
        lifecycle.currentState = Lifecycle.State.STARTED//move to pause state (possible only after RESUMED state)

        //then
        verify(mockInteractorV2).stopPolling()
        verifyNoMoreInteractions(mockInteractorV1)
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTestCase1() {
        //given positive action
        val id = R.id.positiveActionView
        viewModel.authorizationModel.value = viewModel1

        //when
        viewModel.onViewClick(id)

        //then
        assertThat(viewModel.authorizationModel.value!!.status, equalTo(AuthorizationStatus.CONFIRM_PROCESSING))
        verify(mockInteractorV2).updateAuthorization(
            authorizationID = "1",
            authorizationCode = "111",
            confirm = true,
            locationDescription = mockLocationManager.locationDescription
        )
        verifyNoMoreInteractions(mockInteractorV1)
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTestCase2() {
        //given negative action
        val id = R.id.negativeActionView
        viewModel.authorizationModel.value = viewModel1

        //when
        viewModel.onViewClick(id)

        //then
        assertThat(viewModel.authorizationModel.value!!.status, equalTo(AuthorizationStatus.DENY_PROCESSING))
        verify(mockInteractorV2).updateAuthorization(
            authorizationID = "1",
            authorizationCode = "111",
            confirm = false,
            locationDescription = mockLocationManager.locationDescription
        )
        verifyNoMoreInteractions(mockInteractorV1)
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTestCase3() {
        //given unknown action
        val id = R.id.actionView
        viewModel.authorizationModel.value = viewModel1

        //when
        viewModel.onViewClick(id)

        //then
        verifyNoMoreInteractions(mockInteractorV1, mockInteractorV2)
        assertThat(viewModel.authorizationModel.value!!.status, equalTo(AuthorizationStatus.PENDING))
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTestCase4() {
        //given positive action
        doReturn(false).`when`(mockLocationManager).isLocationProviderActive(TestAppTools.applicationContext)
        viewModel.authorizationModel.value = viewModel1.copy(
            geolocationRequired = true
        )

        TestCase.assertNull(viewModel.onRequestPermissionEvent.value)

        //when
        viewModel.onViewClick(itemViewId = R.id.positiveActionView)

        //then
        assertNotNull(viewModel.onRequestPermissionEvent.value)
        verifyNoMoreInteractions(mockInteractorV1)
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTestCase5() {
        //given positive action
        BDDMockito.given(mockLocationManager.locationPermissionsGranted(TestAppTools.applicationContext)).willReturn(true)
        BDDMockito.given(mockLocationManager.isLocationProviderActive(TestAppTools.applicationContext)).willReturn(false)
        viewModel.authorizationModel.value = viewModel1.copy(
            geolocationRequired = true
        )

        assertNull(viewModel.onRequestGPSProviderEvent.value)

        //when
        viewModel.onViewClick(itemViewId = R.id.positiveActionView)

        //then
        assertNotNull(viewModel.onRequestGPSProviderEvent.value)
        verifyNoMoreInteractions(mockInteractorV1)
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTestCase6() {
        //given negative action
        doReturn(false).`when`(mockLocationManager).isLocationProviderActive(TestAppTools.applicationContext)
        viewModel.authorizationModel.value = viewModel1.copy(
            geolocationRequired = true
        )

        TestCase.assertNull(viewModel.onRequestPermissionEvent.value)

        //when
        viewModel.onViewClick(itemViewId = R.id.negativeActionView)

        //then
        assertNotNull(viewModel.onRequestPermissionEvent.value)
        verifyNoMoreInteractions(mockInteractorV1)
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTestCase7() {
        //given negative action
        BDDMockito.given(mockLocationManager.locationPermissionsGranted(TestAppTools.applicationContext)).willReturn(true)
        BDDMockito.given(mockLocationManager.isLocationProviderActive(TestAppTools.applicationContext)).willReturn(false)
        viewModel.authorizationModel.value = viewModel1.copy(
            geolocationRequired = true
        )

        assertNull(viewModel.onRequestGPSProviderEvent.value)

        //when
        viewModel.onViewClick(itemViewId = R.id.negativeActionView)

        //then
        assertNotNull(viewModel.onRequestGPSProviderEvent.value)
        verifyNoMoreInteractions(mockInteractorV1)
    }

    @Test
    @Throws(Exception::class)
    fun onTimerTickTestCase1() {
        //given null model
        viewModel.authorizationModel.value = null

        //when
        viewModel.onTimerTick()

        //then
        verifyNoMoreInteractions(mockInteractorV1, mockInteractorV2)
    }

    @Test
    @Throws(Exception::class)
    fun onTimerTickTestCase2() {
        //given expired authorization that should marked as TIME_OUT
        viewModel.authorizationModel.value = viewModel1.copy(
            endTime = DateTime.now().minusMinutes(1),
            status = AuthorizationStatus.PENDING
        )

        //when
        viewModel.onTimerTick()

        //then
        verifyNoMoreInteractions(mockInteractorV1)
        verify(mockInteractorV2).stopPolling()
        assertThat(viewModel.onTimeUpdateEvent.value, equalTo(ViewModelEvent(Unit)))
        assertThat(viewModel.authorizationModel.value!!.status, equalTo(AuthorizationStatus.TIME_OUT))
    }

    @Test
    @Throws(Exception::class)
    fun onTimerTickTestCase3() {
        //given authorization that should be destroyed (has destroyAt param)
        viewModel.authorizationModel.value = viewModel1
            .copy(status = AuthorizationStatus.TIME_OUT)
            .apply { destroyAt = DateTime.now().minusMinutes(1) }

        //when
        viewModel.onTimerTick()

        //then
        verifyNoMoreInteractions(mockInteractorV1, mockInteractorV2)
        assertThat(viewModel.onCloseAppEvent.value, equalTo(ViewModelEvent(Unit)))
    }

    @Test
    @Throws(Exception::class)
    fun onTimerTickTestCase4() {
        //given DEFAULT authorization
        viewModel.authorizationModel.value = viewModel1.copy(endTime = DateTime.now().plusMinutes(1))

        //when
        viewModel.onTimerTick()

        //then
        verifyNoMoreInteractions(mockInteractorV1, mockInteractorV2)
        assertThat(viewModel.onTimeUpdateEvent.value, equalTo(ViewModelEvent(Unit)))
    }

    @Test
    @Throws(Exception::class)
    fun onTimerTickTestCase5() {
        //given LOADING authorization
        viewModel.authorizationModel.value = viewModel1.copy(status = AuthorizationStatus.LOADING)

        //when
        viewModel.onTimerTick()

        //then
        verifyNoMoreInteractions(mockInteractorV1, mockInteractorV2)
        assertThat(viewModel.onTimeUpdateEvent.value, `is`(nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun onTimerTickTestCase6() {
        //given CONFIRM_PROCESSING authorization
        viewModel.authorizationModel.value = viewModel1.copy(
            status = AuthorizationStatus.CONFIRM_PROCESSING,
            endTime = DateTime.now().plusMinutes(1)
        )

        //when
        viewModel.onTimerTick()

        //then
        verifyNoMoreInteractions(mockInteractorV1, mockInteractorV2)
        assertThat(viewModel.onTimeUpdateEvent.value, `is`(nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun onTimerTickTestCase7() {
        //given DENY_PROCESSING authorization
        viewModel.authorizationModel.value = viewModel1.copy(
            status = AuthorizationStatus.DENY_PROCESSING,
            endTime = DateTime.now().plusMinutes(1)
        )

        //when
        viewModel.onTimerTick()

        //then
        verifyNoMoreInteractions(mockInteractorV1, mockInteractorV2)
        assertThat(viewModel.onTimeUpdateEvent.value, `is`(nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun onTimerTickTestCase8() {
        //given CONFIRM_SUCCESS authorization
        viewModel.authorizationModel.value = viewModel1.copy(status = AuthorizationStatus.CONFIRMED)

        //when
        viewModel.onTimerTick()

        //then
        verifyNoMoreInteractions(mockInteractorV1, mockInteractorV2)
        assertThat(viewModel.onTimeUpdateEvent.value, `is`(nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun onTimerTickTestCase9() {
        //given DENY_SUCCESS authorization
        viewModel.authorizationModel.value = viewModel1.copy(status = AuthorizationStatus.DENIED)

        //when
        viewModel.onTimerTick()

        //then
        verifyNoMoreInteractions(mockInteractorV1, mockInteractorV2)
        assertThat(viewModel.onTimeUpdateEvent.value, `is`(nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun onTimerTickTestCase10() {
        //given ERROR authorization
        viewModel.authorizationModel.value = viewModel1.copy(status = AuthorizationStatus.ERROR)

        //when
        viewModel.onTimerTick()

        //then
        verifyNoMoreInteractions(mockInteractorV1, mockInteractorV2)
        assertThat(viewModel.onTimeUpdateEvent.value, `is`(nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun onTimerTickTestCase11() {
        //given TIME_OUT authorization
        viewModel.authorizationModel.value = viewModel1.copy(status = AuthorizationStatus.TIME_OUT)

        //when
        viewModel.onTimerTick()

        //then
        verifyNoMoreInteractions(mockInteractorV1, mockInteractorV2)
        assertThat(viewModel.onTimeUpdateEvent.value, `is`(nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun onTimerTickTestCase12() {
        //given UNAVAILABLE authorization
        viewModel.authorizationModel.value = viewModel1.copy(status = AuthorizationStatus.UNAVAILABLE)

        //when
        viewModel.onTimerTick()

        //then
        verifyNoMoreInteractions(mockInteractorV1, mockInteractorV2)
        assertThat(viewModel.onTimeUpdateEvent.value, `is`(nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun onBackPressTestCase1() {
        //given closeAppOnBackPress = null
        viewModel.setInitialData(
            identifier = AuthorizationIdentifier(connectionID = "1", authorizationID = "1"),
            closeAppOnBackPress = null,
            titleRes = null
        )

        //when
        val onBackPressResult = viewModel.onBackPress()

        //then
        assertTrue(onBackPressResult)
        assertThat(viewModel.onCloseAppEvent.value, equalTo(ViewModelEvent(Unit)))
    }

    @Test
    @Throws(Exception::class)
    fun onBackPressTestCase2() {
        //given closeAppOnBackPress = true
        viewModel.setInitialData(
            identifier = AuthorizationIdentifier(connectionID = "1", authorizationID = "1"),
            closeAppOnBackPress = true,
            titleRes = null
        )

        //when
        val onBackPressResult = viewModel.onBackPress()

        //then
        assertTrue(onBackPressResult)
        assertThat(viewModel.onCloseAppEvent.value, equalTo(ViewModelEvent(Unit)))
    }

    @Test
    @Throws(Exception::class)
    fun onBackPressTestCase3() {
        //given closeAppOnBackPress = false
        viewModel.setInitialData(
            identifier = AuthorizationIdentifier(connectionID = "1", authorizationID = "1"),
            closeAppOnBackPress = false,
            titleRes = null
        )

        //when
        val onBackPressResult = viewModel.onBackPress()

        //then
        assertTrue(onBackPressResult)
        assertThat(viewModel.onCloseViewEvent.value, equalTo(ViewModelEvent(Unit)))
    }

    @Test
    @Throws(Exception::class)
    fun onAuthorizationReceivedTestCase1() {
        //given initial authorization and success result
        viewModel.setInitialData(identifier = AuthorizationIdentifier(connectionID = "1", authorizationID = "1"), closeAppOnBackPress = true, titleRes = null)

        //when
        viewModel.onAuthorizationReceived(data = viewModel1, newModelApiVersion = API_V2_VERSION)

        //then
        assertThat(viewModel.authorizationModel.value, equalTo(viewModel1))
    }

    @Test
    @Throws(Exception::class)
    fun onFetchAuthorizationResultTestCase2() {
        //given DENY_PROCESSING authorization and success result
        viewModel.authorizationModel.value = viewModel1.copy(status = AuthorizationStatus.DENY_PROCESSING)

        //when
        viewModel.onAuthorizationReceived(data = viewModel1, newModelApiVersion = API_V2_VERSION)

        //then
        assertThat(viewModel.authorizationModel.value, equalTo(viewModel1.copy(status = AuthorizationStatus.DENY_PROCESSING)))
    }

    @Test
    @Throws(Exception::class)
    fun onErrorTest() {
        //given 404 error
        val error = createRequestError(404)

        //when
        viewModel.onError(error)

        //then
        assertThat(viewModel.onErrorEvent.value, equalTo(ViewModelEvent(error)))
        verifyNoMoreInteractions(mockInteractorV1, mockInteractorV2)
    }

    @Test
    @Throws(Exception::class)
    fun onConnectivityErrorTest() {
        //given Connectivity error
        val error = ApiErrorData(errorClassName = ERROR_CLASS_SSL_HANDSHAKE, errorMessage = "ErrorMessage")

        //when
        viewModel.onConnectivityError(error = error)

        //then
        assertThat(viewModel.onErrorEvent.value, equalTo(ViewModelEvent(error)))
        verifyNoMoreInteractions(mockInteractorV1, mockInteractorV2)
    }

    @Test
    @Throws(Exception::class)
    fun onConnectionNotFoundErrorTest() {
        //given ConnectionNotFound error
        viewModel.authorizationModel.value = viewModel1

        //when
        viewModel.onConnectionNotFoundError()

        //then
        assertThat(viewModel.authorizationModel.value!!.status, equalTo(AuthorizationStatus.ERROR))
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenySuccessTestCase1() {
        //given
        viewModel.authorizationModel.value = viewModel1

        //when
        viewModel.onConfirmDenySuccess(newStatus = AuthorizationStatus.CONFIRMED)

        //then
        assertThat(viewModel.authorizationModel.value, equalTo(viewModel1.copy(status = AuthorizationStatus.CONFIRMED)))
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenySuccessTestCase2() {
        //given TIME_OUT result
        viewModel.authorizationModel.value = viewModel1.copy(status = AuthorizationStatus.TIME_OUT)

        //when
        viewModel.onConfirmDenySuccess(newStatus = null)

        //then
        assertThat(viewModel.authorizationModel.value, equalTo(viewModel1.copy(status = AuthorizationStatus.ERROR)))
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenySuccessTestCase3() {
        //given
        viewModel.authorizationModel.value = viewModel1.copy(status = AuthorizationStatus.CONFIRM_PROCESSING)

        //when
        viewModel.onConfirmDenySuccess(newStatus = null)

        //then
        assertThat(viewModel.authorizationModel.value!!.status, equalTo(AuthorizationStatus.CONFIRMED))
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenySuccessTest_case4() {
        //given
        viewModel.authorizationModel.value = viewModel1.copy(status = AuthorizationStatus.DENY_PROCESSING)

        //when
        viewModel.onConfirmDenySuccess(newStatus = null)

        //then
        assertThat(viewModel.authorizationModel.value!!.status, equalTo(AuthorizationStatus.DENIED))
    }

    @Test
    @Throws(Exception::class)
    fun onDialogActionIdClickCase1() {
        viewModel.onPermissionRationaleDialogActionClick(dialogActionId = DialogInterface.BUTTON_NEGATIVE, actionResId = R.string.actions_proceed)

        assertNull(viewModel.onAskPermissionsEvent.value)

        viewModel.onPermissionRationaleDialogActionClick(dialogActionId = DialogInterface.BUTTON_POSITIVE, actionResId= R.string.actions_proceed)

        assertNotNull(viewModel.onAskPermissionsEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onDialogActionIdClickCase2() {
        viewModel.onPermissionRationaleDialogActionClick(dialogActionId = DialogInterface.BUTTON_NEGATIVE, actionResId = R.string.actions_go_to_settings)

        assertNull(viewModel.onGoToSystemSettingsEvent.value)

        viewModel.onPermissionRationaleDialogActionClick(dialogActionId = DialogInterface.BUTTON_POSITIVE, actionResId= R.string.actions_go_to_settings)

        assertNotNull(viewModel.onGoToSystemSettingsEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onDialogActionIdClickCase3() {
        viewModel.onPermissionRationaleDialogActionClick(dialogActionId = DialogInterface.BUTTON_NEGATIVE, actionResId = R.string.actions_enable)

        assertNull(viewModel.onEnableGpsEvent.value)

        viewModel.onPermissionRationaleDialogActionClick(dialogActionId = DialogInterface.BUTTON_POSITIVE, actionResId= R.string.actions_enable)

        assertNotNull(viewModel.onEnableGpsEvent.value)
    }

    private fun initViewModel(connectionApiVersion: String) {
        val identifier = AuthorizationIdentifier(authorizationID = "1", connectionID = "1")
        doReturn(connectionApiVersion).`when`(mockInteractorV1).connectionApiVersion
        doReturn(connectionApiVersion).`when`(mockInteractorV2).connectionApiVersion
        viewModel.setInitialData(identifier = identifier, closeAppOnBackPress = true, titleRes = null)
        clearInvocations(mockInteractorV1, mockInteractorV2)
    }
}
