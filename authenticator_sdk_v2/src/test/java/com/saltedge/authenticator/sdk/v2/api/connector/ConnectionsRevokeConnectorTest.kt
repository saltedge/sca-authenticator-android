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
package com.saltedge.authenticator.sdk.v2.api.connector

import com.saltedge.android.test_tools.CommonTestTools
import com.saltedge.authenticator.sdk.v2.api.ERROR_CLASS_API_RESPONSE
import com.saltedge.authenticator.sdk.v2.api.ERROR_CLASS_HOST_UNREACHABLE
import com.saltedge.authenticator.sdk.v2.api.contract.ConnectionsRevokeListener
import com.saltedge.authenticator.sdk.v2.api.model.connection.RevokeConnectionResponse
import com.saltedge.authenticator.sdk.v2.api.model.connection.RevokeConnectionResponseData
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.v2.api.retrofit.ApiInterface
import com.saltedge.authenticator.sdk.v2.api.retrofit.HEADER_KEY_ACCESS_TOKEN
import com.saltedge.authenticator.sdk.v2.defaultTestConnection
import com.saltedge.authenticator.sdk.v2.get404Response
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.verify
import okhttp3.Request
import okhttp3.ResponseBody
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Call
import retrofit2.Response
import java.net.ConnectException
import java.security.PrivateKey
import java.security.PublicKey

@RunWith(RobolectricTestRunner::class)
class ConnectionsRevokeConnectorTest {

    @Test
    @Throws(Exception::class)
    fun valuesTest() {
        val connector = ConnectionsRevokeConnector(mockApi, null)

        Assert.assertNull(connector.resultCallback)

        connector.resultCallback = mockCallback

        Assert.assertNotNull(connector.resultCallback)
    }

    @Test
    @Throws(Exception::class)
    fun revokeTokensForTest_allSuccess() {
        val connector = ConnectionsRevokeConnector(mockApi, mockCallback)
        connector.revokeAccess(forConnections = listOf(richConnection))

        verify { mockCall.enqueue(connector) }

        connector.onResponse(
            mockCall,
            Response.success(RevokeConnectionResponse(RevokeConnectionResponseData(
                revokedConnectionId = "333"
            )))
        )

        verify {
            mockCallback.onConnectionsRevokeResult(apiError = null)
        }
        confirmVerified(mockCallback)
    }

    @Test
    @Throws(Exception::class)
    fun revokeTokensForTest_withError404() {
        val connector = ConnectionsRevokeConnector(mockApi, mockCallback)
        connector.revokeAccess(listOf(richConnection))

        verify { mockCall.enqueue(connector) }

        connector.onResponse(mockCall, get404Response())

        verify {
            mockCallback.onConnectionsRevokeResult(
                apiError = ApiErrorData(
                    errorMessage = "Resource not found",
                    errorClassName = "NotFound",
                    accessToken = "accessToken"
                )
            )
        }
        confirmVerified(mockCallback)
    }

    @Test
    @Throws(Exception::class)
    fun revokeTokensForTest_successWithoutResponseBody() {
        val connector = ConnectionsRevokeConnector(mockApi, mockCallback)
        connector.onResponse(mockCall, Response.success(null))

        verify {
            mockCallback.onConnectionsRevokeResult(
                apiError = ApiErrorData(
                    errorMessage = "Request Error (200)",
                    errorClassName = ERROR_CLASS_API_RESPONSE,
                    accessToken = "accessToken"
                )
            )
        }
        confirmVerified(mockCallback)
    }

    @Test
    @Throws(Exception::class)
    fun revokeTokensForTest_withUnknownError() {
        val connector = ConnectionsRevokeConnector(mockApi, mockCallback)
        connector.onResponse(
            mockCall,
            Response.error(404, ResponseBody.create(null, "{\"message\":\"Unknown error\"}"))
        )

        verify {
            mockCallback.onConnectionsRevokeResult(
                apiError = ApiErrorData(
                    errorMessage = "Request Error (404)",
                    errorClassName = ERROR_CLASS_API_RESPONSE,
                    accessToken = "accessToken"
                )
            )
        }
        confirmVerified(mockCallback)
    }

    @Test
    @Throws(Exception::class)
    fun revokeTokensForTest_withException() {
        val connector = ConnectionsRevokeConnector(mockApi, mockCallback)
        connector.onFailure(mockCall, ConnectException())

        verify {
            mockCallback.onConnectionsRevokeResult(
                apiError = ApiErrorData(
                    errorClassName = ERROR_CLASS_HOST_UNREACHABLE,
                    accessToken = "accessToken"
                )
            )
        }
        confirmVerified(mockCallback)
    }

    private val mockApi: ApiInterface = mockkClass(ApiInterface::class)
    private val mockCallback: ConnectionsRevokeListener = mockkClass(ConnectionsRevokeListener::class)
    private val mockCall = mockkClass(Call::class) as Call<RevokeConnectionResponse>
    private val requestConnection: ConnectionV2Abs = defaultTestConnection
    private val privateKey: PrivateKey = CommonTestTools.testPrivateKey
    private val publicKey: PublicKey = CommonTestTools.testPublicKey
    private val richConnection = RichConnection(requestConnection, privateKey, publicKey)
    private val requestUrl = "https://localhost/api/authenticator/v2/connections/${requestConnection.id}/revoke"

    @Before
    @Throws(Exception::class)
    fun setUp() {
        every {
            mockApi.revokeConnection(requestUrl = requestUrl, headersMap = any(), requestBody = any())
        } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
        every { mockCall.request() } returns Request.Builder().url(requestUrl)
            .addHeader(HEADER_KEY_ACCESS_TOKEN, "accessToken").build()
        every { mockCallback.onConnectionsRevokeResult(any()) } returns Unit
    }
}
