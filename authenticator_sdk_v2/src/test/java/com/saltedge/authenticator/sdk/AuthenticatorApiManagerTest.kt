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
package com.saltedge.authenticator.sdk

import com.saltedge.android.test_tools.CommonTestTools
import com.saltedge.android.test_tools.TestConnection
import com.saltedge.authenticator.sdk.contract.*
import com.saltedge.authenticator.sdk.model.connection.ConnectionAbs
import com.saltedge.authenticator.sdk.model.connection.ConnectionAndKey
import com.saltedge.authenticator.sdk.model.response.*
import com.saltedge.authenticator.sdk.network.ApiInterface
import com.saltedge.authenticator.sdk.network.RestClient
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.verify
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Call
import java.security.PrivateKey

@RunWith(RobolectricTestRunner::class)
class AuthenticatorApiManagerTest {

    @Test
    @Throws(Exception::class)
    fun initConnectionRequestTest() {
        val mockCallback = mockkClass(ConnectionCreateListener::class)
        val mockCall: Call<CreateConnectionResponse> =
            mockkClass(Call::class) as Call<CreateConnectionResponse>
        every { mockApi.createConnection(requestUrl = any(), body = any()) } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
        AuthenticatorApiManager.createConnectionRequest(
            baseUrl = "",
            publicKey = "key",
            pushToken = "pushToken",
            connectQueryParam = "1234567890",
            providerCode = "demobank",
            resultCallback = mockCallback
        )

        verify { mockCall.enqueue(any()) }
    }

    @Test
    @Throws(Exception::class)
    fun revokeConnectionsTest() {
        val mockCallback = mockkClass(ConnectionsRevokeListener::class)
        val mockCall: Call<RevokeAccessTokenResponse> =
            mockkClass(Call::class) as Call<RevokeAccessTokenResponse>
        every { mockApi.revokeConnection(requestUrl = any(), headersMap = any()) } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
        AuthenticatorApiManager.revokeConnections(
            connectionsAndKeys = listOf(
                ConnectionAndKey(
                    requestConnection,
                    privateKey
                )
            ), resultCallback = mockCallback
        )

        verify { mockCall.enqueue(any()) }
    }

    @Test
    @Throws(Exception::class)
    fun createAuthorizationsPollingServiceTest() {
        Assert.assertNotNull(AuthenticatorApiManager.createAuthorizationsPollingService())
    }

    @Test
    @Throws(Exception::class)
    fun createSingleAuthorizationPollingServiceTest() {
        Assert.assertNotNull(AuthenticatorApiManager.createSingleAuthorizationPollingService())
    }

    @Test
    @Throws(Exception::class)
    fun getAuthorizationsTest() {
        val mockCallback = mockkClass(FetchEncryptedDataListener::class)
        val mockCall = mockkClass(Call::class) as Call<EncryptedListResponse>
        every { mockApi.getAuthorizations(requestUrl = any(), headersMap = any()) } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
        AuthenticatorApiManager.getAuthorizations(
            connectionsAndKeys = listOf(
                ConnectionAndKey(requestConnection, privateKey)
            ),
            resultCallback = mockCallback
        )

        verify { mockCall.enqueue(any()) }
    }

    @Test
    @Throws(Exception::class)
    fun getAuthorizationTest() {
        val mockCallback = mockkClass(FetchAuthorizationContract::class)
        val mockCall = mockkClass(Call::class) as Call<AuthorizationShowResponse>
        every { mockApi.getAuthorization(requestUrl = any(), headersMap = any()) } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
        AuthenticatorApiManager.getAuthorization(
            connectionAndKey = ConnectionAndKey(
                requestConnection,
                privateKey
            ), authorizationId = "444", resultCallback = mockCallback
        )

        verify { mockCall.enqueue(any()) }
    }

    @Test
    @Throws(Exception::class)
    fun confirmAuthorizationTest() {
        val mockCallback = mockkClass(ConfirmAuthorizationListener::class)
        val mockCall = mockkClass(Call::class) as Call<ConfirmDenyResponse>
        every {
            mockApi.updateAuthorization(
                requestUrl = any(),
                headersMap = any(),
                requestBody = any()
            )
        } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
        AuthenticatorApiManager.confirmAuthorization(
            connectionAndKey = ConnectionAndKey(
                requestConnection,
                privateKey
            ),
            authorizationId = "444",
            authorizationCode = "code",
            geolocation = "GEO:52.506931;13.144558",
            authorizationType = "biometrics",
            resultCallback = mockCallback
        )

        verify { mockCall.enqueue(any()) }
    }

    @Test
    @Throws(Exception::class)
    fun denyAuthorizationTest() {
        val mockCallback = mockkClass(ConfirmAuthorizationListener::class)
        val mockCall = mockkClass(Call::class) as Call<ConfirmDenyResponse>
        every {
            mockApi.updateAuthorization(
                requestUrl = any(),
                headersMap = any(),
                requestBody = any()
            )
        } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
        AuthenticatorApiManager.denyAuthorization(
            connectionAndKey = ConnectionAndKey(
                requestConnection,
                privateKey
            ),
            authorizationId = "444",
            authorizationCode = "code",
            geolocation = "GEO:52.506931;13.144558",
            authorizationType = "biometrics",
            resultCallback = mockCallback
        )

        verify { mockCall.enqueue(any()) }
    }

    @Test
    @Throws(Exception::class)
    fun sendActionTest() {
        val mockCallback = mockkClass(ActionSubmitListener::class)
        val mockCall = mockkClass(Call::class) as Call<SubmitActionResponse>
        every {
            mockApi.updateAction(
                requestUrl = any(),
                headersMap = any()
            )
        } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
        AuthenticatorApiManager.sendAction(
            actionUUID = "actionUUID",
            connectionAndKey = ConnectionAndKey(
                requestConnection,
                privateKey
            ),
            resultCallback = mockCallback
        )

        verify { mockCall.enqueue(any()) }
    }

    @Test
    @Throws(Exception::class)
    fun getConsentsTest() {
        val mockCallback = mockkClass(FetchEncryptedDataListener::class)
        val mockCall = mockkClass(Call::class) as Call<EncryptedListResponse>
        every { mockApi.getConsents(requestUrl = any(), headersMap = any()) } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
        AuthenticatorApiManager.getConsents(
            connectionsAndKeys = listOf(
                ConnectionAndKey(requestConnection, privateKey)
            ),
            resultCallback = mockCallback
        )

        verify { mockCall.enqueue(any()) }
    }

    @Test
    @Throws(Exception::class)
    fun revokeConsentTest() {
        val mockCallback = mockkClass(ConsentRevokeListener::class)
        val mockCall = mockkClass(Call::class) as Call<ConsentRevokeResponse>
        every {
            mockApi.revokeConsent(
                requestUrl = any(),
                headersMap = any()
            )
        } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
        AuthenticatorApiManager.revokeConsent(
            consentId = "consentId",
            connectionAndKey = ConnectionAndKey(
                requestConnection,
                privateKey
            ),
            resultCallback = mockCallback
        )

        verify { mockCall.enqueue(any()) }
    }

    private val mockApi: ApiInterface = mockkClass(ApiInterface::class)
    private var privateKey: PrivateKey = CommonTestTools.testPrivateKey
    private val requestConnection: ConnectionAbs =
        TestConnection(id = "333", guid = "test", connectUrl = "/", accessToken = "accessToken")

    @Before
    @Throws(Exception::class)
    fun setUp() {
        RestClient.apiInterface = mockApi
    }
}
