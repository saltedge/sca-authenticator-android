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
package com.saltedge.authenticator.sdk

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.saltedge.authenticator.sdk.contract.ConfirmAuthorizationResult
import com.saltedge.authenticator.sdk.contract.ConnectionInitResult
import com.saltedge.authenticator.sdk.contract.ConnectionsRevokeResult
import com.saltedge.authenticator.sdk.contract.FetchAuthorizationContract
import com.saltedge.authenticator.sdk.model.ConnectionAbs
import com.saltedge.authenticator.sdk.model.ConnectionAndKey
import com.saltedge.authenticator.sdk.model.response.AuthorizationShowResponseData
import com.saltedge.authenticator.sdk.model.response.ConfirmDenyResponseData
import com.saltedge.authenticator.sdk.model.response.CreateConnectionResponseData
import com.saltedge.authenticator.sdk.model.response.RevokeAccessTokenResponseData
import com.saltedge.authenticator.sdk.network.ApiInterface
import com.saltedge.authenticator.sdk.network.RestClient
import com.saltedge.authenticator.sdk.testTools.TestConnection
import com.saltedge.authenticator.sdk.tools.KeyStoreManager
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.verify
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Call
import java.security.PrivateKey

@RunWith(AndroidJUnit4::class)
class AuthenticatorApiManagerTest {

    @Test
    @Throws(Exception::class)
    fun initConnectionRequestTest() {
        val mockCallback = mockkClass(ConnectionInitResult::class)
        val mockCall: Call<CreateConnectionResponseData> =
            mockkClass(Call::class) as Call<CreateConnectionResponseData>
        every { mockApi.postNewConnectionData(requestUrl = any(), body = any()) } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
        AuthenticatorApiManager.initConnectionRequest(
            baseUrl = "",
            publicKey = "key",
            pushToken = "pushToken",
            resultCallback = mockCallback
        )

        verify { mockCall.enqueue(any()) }
    }

    @Test
    @Throws(Exception::class)
    fun revokeConnectionsTest() {
        val mockCallback = mockkClass(ConnectionsRevokeResult::class)
        val mockCall: Call<RevokeAccessTokenResponseData> =
            mockkClass(Call::class) as Call<RevokeAccessTokenResponseData>
        every { mockApi.deleteAccessToken(requestUrl = any(), headersMap = any()) } returns mockCall
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
    fun getAuthorizationTest() {
        val mockCallback = mockkClass(FetchAuthorizationContract::class)
        val mockCall = mockkClass(Call::class) as Call<AuthorizationShowResponseData>
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
        val mockCallback = mockkClass(ConfirmAuthorizationResult::class)
        val mockCall = mockkClass(Call::class) as Call<ConfirmDenyResponseData>
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
            ), authorizationId = "444", authorizationCode = "code", resultCallback = mockCallback
        )

        verify { mockCall.enqueue(any()) }
    }

    @Test
    @Throws(Exception::class)
    fun denyAuthorizationTest() {
        val mockCallback = mockkClass(ConfirmAuthorizationResult::class)
        val mockCall = mockkClass(Call::class) as Call<ConfirmDenyResponseData>
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
            ), authorizationId = "444", authorizationCode = "code", resultCallback = mockCallback
        )

        verify { mockCall.enqueue(any()) }
    }

    private val mockApi: ApiInterface = mockkClass(ApiInterface::class)
    private var privateKey: PrivateKey = KeyStoreManager.createOrReplaceRsaKeyPair("test")!!.private
    private val requestConnection: ConnectionAbs =
        TestConnection(id = "333", guid = "test", connectUrl = "/", accessToken = "accessToken")

    @Before
    @Throws(Exception::class)
    fun setUp() {
        RestClient.apiInterface = mockApi
    }
}
