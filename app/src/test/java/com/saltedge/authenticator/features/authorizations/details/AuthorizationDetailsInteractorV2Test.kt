/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2021 Salt Edge Inc.
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

import com.saltedge.authenticator.app.AppTools
import com.saltedge.authenticator.core.api.ERROR_CLASS_AUTHORIZATION_NOT_FOUND
import com.saltedge.authenticator.core.api.ERROR_CLASS_CONNECTION_NOT_FOUND
import com.saltedge.authenticator.core.api.ERROR_CLASS_SSL_HANDSHAKE
import com.saltedge.authenticator.core.api.model.DescriptionData
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.api.model.error.createRequestError
import com.saltedge.authenticator.core.model.ConnectionAbs
import com.saltedge.authenticator.core.model.ConnectionStatus
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.features.authorizations.common.AuthorizationStatus
import com.saltedge.authenticator.features.authorizations.common.toAuthorizationItemViewModel
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.location.DeviceLocationManagerAbs
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.constants.API_V1_VERSION
import com.saltedge.authenticator.sdk.v2.ScaServiceClientAbs
import com.saltedge.authenticator.sdk.v2.api.API_V2_VERSION
import com.saltedge.authenticator.sdk.v2.api.model.authorization.AuthorizationResponseData
import com.saltedge.authenticator.sdk.v2.api.model.authorization.AuthorizationV2Data
import com.saltedge.authenticator.sdk.v2.api.model.authorization.UpdateAuthorizationResponseData
import com.saltedge.authenticator.sdk.v2.api.model.authorization.UpdateAuthorizationData
import com.saltedge.authenticator.sdk.v2.polling.SingleAuthorizationPollingService
import com.saltedge.authenticator.sdk.v2.tools.CryptoToolsV2Abs
import com.saltedge.authenticator.widget.security.ActivityUnlockType
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import java.security.PrivateKey

@RunWith(RobolectricTestRunner::class)
class AuthorizationDetailsInteractorV2Test {

    private lateinit var interactor: AuthorizationDetailsInteractorV2

    private val mockConnectionsRepository = mock(ConnectionsRepositoryAbs::class.java)
    private val mockKeyStoreManager = mock(KeyManagerAbs::class.java)
    private val mockCryptoToolsV2 = mock(CryptoToolsV2Abs::class.java)
    private val mockApiManagerV2 = mock(ScaServiceClientAbs::class.java)
    private val mockPollingServiceV1 = mock(SingleAuthorizationPollingService::class.java)
    private val mockLocationManager = mock(DeviceLocationManagerAbs::class.java)
    private val mockPrivateKey = mock(PrivateKey::class.java)
    private val mockCallback = mock(AuthorizationDetailsInteractorCallback::class.java)

    private val connection1 = Connection().apply {
        id = "1"
        guid = "guid1"
        code = "demobank1"
        name = "Demobank1"
        status = "${ConnectionStatus.ACTIVE}"
        accessToken = "token1"
        supportEmail = "example@example.com"
        createdAt = 100L
        updatedAt = 100L
        apiVersion = API_V1_VERSION
    }
    private val connection2 = Connection().apply {
        id = "2"
        guid = "guid2"
        code = "demobank2"
        name = "Demobank2"
        status = "${ConnectionStatus.ACTIVE}"
        supportEmail = "example@example.com"
        accessToken = "token2"
        createdAt = 200L
        updatedAt = 200L
        apiVersion = API_V2_VERSION
    }
    private val connection3Inactive = Connection().apply {
        id = "3"
        guid = "guid3"
        code = "demobank3"
        name = "Demobank3"
        status = "${ConnectionStatus.INACTIVE}"
        supportEmail = "example@example.com"
        accessToken = ""
        createdAt = 300L
        updatedAt = 300L
        apiVersion = API_V1_VERSION
    }
    private val richConnection1 = RichConnection(connection1, mockPrivateKey)
    private val richConnection2 = RichConnection(connection2, mockPrivateKey)
    private val richConnection3 = RichConnection(connection3Inactive, mockPrivateKey)
    private val encryptedData1 = AuthorizationResponseData(id = "1", connectionId = "1", status = "pending", iv = "", key = "", data = "")
    private val authorizationData1 = createAuthorizationData(id = 1)
    private val testViewItem1 = authorizationData1.toAuthorizationItemViewModel(connection1)!!

    private fun createAuthorizationData(id: Int): AuthorizationV2Data {
        val createdAt = DateTime.now(DateTimeZone.UTC)
        return AuthorizationV2Data(
            authorizationID = "$id",
            connectionID = "$id",
            status = "pending",
            authorizationCode = "$id$id$id",
            title = "title$id",
            description = DescriptionData(text = "desc$id"),
            createdAt = createdAt,
            expiresAt = createdAt.plusHours(id)
        )
    }

    @Before
    fun setUp() {
        AppTools.lastUnlockType = ActivityUnlockType.BIOMETRICS
        doReturn("GEO:52.506931;13.144558").`when`(mockLocationManager).locationDescription
        doReturn(connection1).`when`(mockConnectionsRepository).getById(connection1.id)
        doReturn(connection2).`when`(mockConnectionsRepository).getById(connection2.id)
        doReturn(connection3Inactive).`when`(mockConnectionsRepository).getById(connection3Inactive.id)
        doReturn(mockPollingServiceV1).`when`(mockApiManagerV2)
            .createSingleAuthorizationPollingService()
        doReturn(richConnection1).`when`(mockKeyStoreManager).enrichConnection(connection1, addProviderKey = false)
        doReturn(richConnection2).`when`(mockKeyStoreManager).enrichConnection(connection2, addProviderKey = true)
        doReturn(richConnection3).`when`(mockKeyStoreManager).enrichConnection(connection3Inactive, addProviderKey = false)
        doReturn(null).`when`(mockKeyStoreManager).getKeyPair("guid2")
        doReturn(authorizationData1).`when`(mockCryptoToolsV2)
            .decryptAuthorizationData(
                encryptedData = encryptedData1,
                rsaPrivateKey = mockPrivateKey
            )

        interactor = AuthorizationDetailsInteractorV2(
            connectionsRepository = mockConnectionsRepository,
            keyStoreManager = mockKeyStoreManager,
            cryptoTools = mockCryptoToolsV2,
            apiManager = mockApiManagerV2,
            locationManager = mockLocationManager
        )
        interactor.contract = mockCallback
    }

    @Test
    @Throws(Exception::class)
    fun startPollingTest() {
        //given
        val authorizationID = "1"

        //when
        interactor.startPolling(authorizationID = authorizationID)

        //then
        verify(mockPollingServiceV1).contract = interactor
        verify(mockPollingServiceV1).start(authorizationID = authorizationID)
    }

    @Test
    @Throws(Exception::class)
    fun stopPollingTest() {
        //when
        interactor.stopPolling()

        //then
        verify(mockPollingServiceV1).contract = null
        verify(mockPollingServiceV1).stop()
    }

    @Test
    @Throws(Exception::class)
    fun getConnectionDataForAuthorizationPollingTest() {
        Assert.assertNull(interactor.getConnectionDataForAuthorizationPolling())

        interactor.setInitialData(connectionID = "2_noKey")

        Assert.assertNull(interactor.getConnectionDataForAuthorizationPolling())

        interactor.setInitialData(connectionID = "1")

        assertThat(
            interactor.getConnectionDataForAuthorizationPolling(),
            equalTo(RichConnection(connection1 as ConnectionAbs, mockPrivateKey))
        )
    }

    @Test
    @Throws(Exception::class)
    fun onFetchAuthorizationSuccessTest() {
        //given
        interactor.setInitialData(connectionID = "1")

        //when
        interactor.onFetchAuthorizationSuccess(result = encryptedData1)

        //then
        verify(mockCallback).onAuthorizationReceived(testViewItem1, API_V2_VERSION)
    }

    @Test
    @Throws(Exception::class)
    fun onFetchAuthorizationFailedTestCase2() {
        //given 404 error
        val error = createRequestError(404)
        interactor.setInitialData(connectionID = "1")

        //when
        interactor.onFetchAuthorizationFailed(error = error)

        //then
        verify(mockCallback).onError(error)
    }

    @Test
    @Throws(Exception::class)
    fun onFetchAuthorizationFailedTestCase3() {
        //given Connectivity error
        val error = ApiErrorData(errorClassName = ERROR_CLASS_SSL_HANDSHAKE, errorMessage = "ErrorMessage")
        interactor.setInitialData(connectionID = "1")

        //when
        interactor.onFetchAuthorizationFailed(error = error)

        //then
        verify(mockCallback).onConnectivityError(error)
    }

    @Test
    @Throws(Exception::class)
    fun onFetchAuthorizationFailedTestCase4() {
        //given ConnectionNotFound error
        val error = ApiErrorData(
            errorClassName = ERROR_CLASS_CONNECTION_NOT_FOUND,
            errorMessage = "Not found"
        )
        interactor.setInitialData(connectionID = "1")

        //when
        interactor.onFetchAuthorizationFailed(error = error)

        //then
        verify(mockConnectionsRepository).invalidateConnectionsByTokens(
            accessTokens = listOf("token1")
        )
        verify(mockPollingServiceV1).stop()
        verify(mockCallback).onConnectionNotFoundError()
    }

    @Test
    @Throws(Exception::class)
    fun onFetchAuthorizationFailedTestCase5() {
        //given AuthorizationNotFound error
        val error = ApiErrorData(
            errorClassName = ERROR_CLASS_AUTHORIZATION_NOT_FOUND,
            errorMessage = "Not found"
        )
        interactor.setInitialData(connectionID = "1")

        //when
        interactor.onFetchAuthorizationFailed(error = error)

        //then
        verify(mockPollingServiceV1).stop()
        verify(mockCallback).onAuthorizationNotFoundError()
    }

    @Test
    @Throws(Exception::class)
    fun onFetchAuthorizationFailedCase6() {
        //given NULL error
        interactor.setInitialData(connectionID = "1")

        //when
        interactor.onFetchAuthorizationFailed(error = null)

        //then
        verify(mockCallback).onAuthorizationNotFoundError()
    }

    @Test
    @Throws(Exception::class)
    fun updateAuthorizationTestCase1() {
        //given
        val confirm = true
        interactor.setInitialData(connectionID = "1")

        //when
        val result = interactor.updateAuthorization(
            authorizationID = "1",
            authorizationCode = "111",
            confirm = confirm
        )

        //then
        verify(mockApiManagerV2).confirmAuthorization(
            connection = RichConnection(connection1, mockPrivateKey),
            authorizationID = "1",
            authorizationData = UpdateAuthorizationData(
                authorizationCode = "111",
                geolocation = "GEO:52.506931;13.144558",
                userAuthorizationType = "biometrics"
            ),
            callback = interactor
        )
        Assert.assertTrue(result)
    }

    @Test
    @Throws(Exception::class)
    fun updateAuthorizationTestCase2() {
        //given
        val confirm = false
        interactor.setInitialData(connectionID = "1")

        //when
        val result = interactor.updateAuthorization(
            authorizationID = "1",
            authorizationCode = "111",
            confirm = confirm
        )

        //then
        verify(mockApiManagerV2).denyAuthorization(
            connection = RichConnection(connection1, mockPrivateKey),
            authorizationID = "1",
            authorizationData = UpdateAuthorizationData(
                authorizationCode = "111",
                geolocation = "GEO:52.506931;13.144558",
                userAuthorizationType = "biometrics"
            ),
            callback = interactor
        )
        Assert.assertTrue(result)
    }

    @Test
    @Throws(Exception::class)
    fun updateAuthorizationTestCase3() {
        //given
        val confirm = false
        interactor.setInitialData(connectionID = "wrong")
        clearInvocations(mockApiManagerV2)

        //when
        val result = interactor.updateAuthorization(
            authorizationID = "1",
            authorizationCode = "111",
            confirm = confirm
        )

        //then
        verifyNoInteractions(mockApiManagerV2)
        Assert.assertFalse(result)
    }

    @Test
    @Throws(Exception::class)
    fun onAuthorizationConfirmFailureTest() {
        //given 404 error
        val error = createRequestError(404)
        interactor.setInitialData(connectionID = "1")

        //when
        interactor.onAuthorizationConfirmFailure(error = error, connectionID = "1", authorizationID = "1")

        //then
        verify(mockCallback).onError(error)
    }

    @Test
    @Throws(Exception::class)
    fun onAuthorizationConfirmSuccessTest() {
        //when
        interactor.onAuthorizationConfirmSuccess(
            result = UpdateAuthorizationResponseData(
                authorizationID = "1",
                status = "confirmed"
            ),
            connectionID = "1"
        )

        //then
        verify(mockCallback).onConfirmDenySuccess(newStatus = AuthorizationStatus.CONFIRMED)
    }
}
