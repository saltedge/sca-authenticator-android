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
package com.saltedge.authenticator.sdk.v2

import com.saltedge.android.test_tools.CommonTestTools
import com.saltedge.authenticator.core.api.model.EncryptedBundle
import com.saltedge.authenticator.core.model.ConnectionAbs
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.sdk.v2.api.contract.*
import com.saltedge.authenticator.sdk.v2.api.model.authorization.AuthorizationResponse
import com.saltedge.authenticator.sdk.v2.api.model.authorization.AuthorizationsListResponse
import com.saltedge.authenticator.sdk.v2.api.model.authorization.ConfirmDenyResponse
import com.saltedge.authenticator.sdk.v2.api.model.authorization.UpdateAuthorizationData
import com.saltedge.authenticator.sdk.v2.api.model.connection.CreateConnectionResponse
import com.saltedge.authenticator.sdk.v2.api.model.connection.RevokeConnectionResponse
import com.saltedge.authenticator.sdk.v2.api.retrofit.ApiInterface
import com.saltedge.authenticator.sdk.v2.api.retrofit.RestClient
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
import java.security.PublicKey

@RunWith(RobolectricTestRunner::class)
class ScaServiceClientTest {

    @Test
    @Throws(Exception::class)
    fun initConnectionRequestTest() {
        val mockCallback = mockkClass(ConnectionCreateListener::class)
        val mockCall = mockkClass(Call::class) as Call<CreateConnectionResponse>
        every { mockApi.createConnection(requestUrl = any(), body = any()) } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
        ScaServiceClient().createConnectionRequest(
            baseUrl = "",
            rsaPublicKeyEncryptedBundle = EncryptedBundle(
                encryptedAesKey = "key",
                encryptedAesIv = "iv",
                encryptedData = "data"
            ),
            providerId = "1",
            pushToken = "pushToken",
            connectQueryParam = "1234567890",
            callback = mockCallback
        )

        verify { mockCall.enqueue(any()) }
    }

    @Test
    @Throws(Exception::class)
    fun revokeConnectionsTest() {
        val mockCallback = mockkClass(ConnectionsRevokeListener::class)
        val mockCall = mockkClass(Call::class) as Call<RevokeConnectionResponse>
        every {
            mockApi.revokeConnection(
                requestUrl = any(),
                headersMap = any(),
                requestBody = any()
            )
        } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
        ScaServiceClient().revokeConnections(
            connections = listOf(RichConnection(requestConnection, privateKey, publicKey)),
            callback = mockCallback
        )

        verify { mockCall.enqueue(any()) }
    }

    @Test
    @Throws(Exception::class)
    fun createAuthorizationsPollingServiceTest() {
        Assert.assertNotNull(ScaServiceClient().createAuthorizationsPollingService())
    }

    @Test
    @Throws(Exception::class)
    fun createSingleAuthorizationPollingServiceTest() {
        Assert.assertNotNull(ScaServiceClient().createSingleAuthorizationPollingService())
    }

    @Test
    @Throws(Exception::class)
    fun getAuthorizationsTest() {
        val mockCallback = mockkClass(FetchAuthorizationsListener::class)
        val mockCall = mockkClass(Call::class) as Call<AuthorizationsListResponse>
        every { mockApi.activeAuthorizations(requestUrl = any(), headersMap = any()) } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
        ScaServiceClient().getAuthorizations(
            connections = listOf(RichConnection(requestConnection, privateKey, publicKey)),
            callback = mockCallback
        )

        verify { mockCall.enqueue(any()) }
    }

    @Test
    @Throws(Exception::class)
    fun getAuthorizationTest() {
        val mockCallback = mockkClass(FetchAuthorizationListener::class)
        val mockCall = mockkClass(Call::class) as Call<AuthorizationResponse>
        every { mockApi.showAuthorization(requestUrl = any(), headersMap = any()) } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
        ScaServiceClient().getAuthorization(
            connection = RichConnection(requestConnection, privateKey, publicKey),
            authorizationID = "444",
            callback = mockCallback
        )

        verify { mockCall.enqueue(any()) }
    }

    @Test
    @Throws(Exception::class)
    fun confirmAuthorizationTest() {
        val mockCallback = mockkClass(AuthorizationConfirmListener::class)
        val mockCall = mockkClass(Call::class) as Call<ConfirmDenyResponse>
        every {
            mockApi.confirmAuthorization(
                requestUrl = any(),
                headersMap = any(),
                requestBody = any()
            )
        } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
        ScaServiceClient().confirmAuthorization(
            connection = RichConnection(requestConnection, privateKey, publicKey),
            authorizationID = "444",
            authorizationData = UpdateAuthorizationData(
                authorizationCode = "Code123",
                userAuthorizationType = "biometrics",
                geolocation = "GEO:52.506931;13.144558"
            ),
            callback = mockCallback
        )

        verify { mockCall.enqueue(any()) }
    }

    @Test
    @Throws(Exception::class)
    fun denyAuthorizationTest() {
        val mockCallback = mockkClass(AuthorizationDenyListener::class)
        val mockCall = mockkClass(Call::class) as Call<ConfirmDenyResponse>
        every {
            mockApi.denyAuthorization(
                requestUrl = any(),
                headersMap = any(),
                requestBody = any()
            )
        } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
        ScaServiceClient().denyAuthorization(
            connection = RichConnection(requestConnection, privateKey, publicKey),
            authorizationID = "444",
            authorizationData = UpdateAuthorizationData(
                authorizationCode = "Code123",
                userAuthorizationType = "biometrics",
                geolocation = "GEO:52.506931;13.144558"
            ),
            callback = mockCallback
        )

        verify { mockCall.enqueue(any()) }
    }

    private val mockApi: ApiInterface = mockkClass(ApiInterface::class)
    private var privateKey: PrivateKey = CommonTestTools.testPrivateKey
    private var publicKey: PublicKey = CommonTestTools.testPublicKey
    private val requestConnection: ConnectionAbs = defaultTestConnection

    @Before
    @Throws(Exception::class)
    fun setUp() {
        RestClient.apiInterface = mockApi
    }
}
