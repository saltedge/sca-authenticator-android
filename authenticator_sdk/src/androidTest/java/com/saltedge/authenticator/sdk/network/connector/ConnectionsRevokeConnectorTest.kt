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
package com.saltedge.authenticator.sdk.network.connector

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.saltedge.authenticator.sdk.constants.ERROR_CLASS_API_RESPONSE
import com.saltedge.authenticator.sdk.constants.ERROR_CLASS_HOST_UNREACHABLE
import com.saltedge.authenticator.sdk.contract.ConnectionsRevokeResult
import com.saltedge.authenticator.sdk.model.ApiErrorData
import com.saltedge.authenticator.sdk.model.ConnectionAbs
import com.saltedge.authenticator.sdk.model.ConnectionAndKey
import com.saltedge.authenticator.sdk.model.response.RevokeAccessTokenResponseData
import com.saltedge.authenticator.sdk.model.response.RevokeAccessTokenResultData
import com.saltedge.authenticator.sdk.network.ApiInterface
import com.saltedge.authenticator.sdk.network.HEADER_KEY_ACCESS_TOKEN
import com.saltedge.authenticator.sdk.testTools.TestConnection
import com.saltedge.authenticator.sdk.testTools.get404Response
import com.saltedge.authenticator.sdk.tools.KeyStoreManager
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
import retrofit2.Call
import retrofit2.Response
import java.net.ConnectException
import java.security.PrivateKey

@RunWith(AndroidJUnit4::class)
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
        connector.revokeTokensFor(listOf(ConnectionAndKey(requestConnection, privateKey)))

        verify { mockCall.enqueue(connector) }

        connector.onResponse(
            mockCall,
            Response.success(
                RevokeAccessTokenResponseData(
                    RevokeAccessTokenResultData(
                        success = true,
                        accessToken = "accessToken"
                    )
                )
            )
        )

        verify {
            mockCallback.onConnectionsRevokeResult(
                revokedTokens = listOf("accessToken"),
                apiError = null
            )
        }
        confirmVerified(mockCallback)
    }

    @Test
    @Throws(Exception::class)
    fun revokeTokensForTest_withError404() {
        val connector = ConnectionsRevokeConnector(mockApi, mockCallback)
        connector.revokeTokensFor(listOf(ConnectionAndKey(requestConnection, privateKey)))

        verify { mockCall.enqueue(connector) }

        connector.onResponse(
            mockCall,
            Response.error(404, ResponseBody.create(null, get404Response()))
        )

        verify {
            mockCallback.onConnectionsRevokeResult(
                revokedTokens = emptyList(),
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
                revokedTokens = emptyList(),
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
                revokedTokens = emptyList(),
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
                revokedTokens = emptyList(),
                apiError = ApiErrorData(
                    errorClassName = ERROR_CLASS_HOST_UNREACHABLE,
                    accessToken = "accessToken"
                )
            )
        }
        confirmVerified(mockCallback)
    }

    private var privateKey: PrivateKey = KeyStoreManager.createOrReplaceRsaKeyPair("test")!!.private
    private val mockApi: ApiInterface = mockkClass(ApiInterface::class)
    private val mockCallback: ConnectionsRevokeResult = mockkClass(ConnectionsRevokeResult::class)
    private val mockCall: Call<RevokeAccessTokenResponseData> =
        mockkClass(Call::class) as Call<RevokeAccessTokenResponseData>
    private val requestConnection: ConnectionAbs = TestConnection(
        id = "333",
        guid = "test",
        connectUrl = "https://localhost",
        accessToken = "accessToken"
    )
    private val requestUrl = "https://localhost/api/authenticator/v1/connections"

    @Before
    @Throws(Exception::class)
    fun setUp() {
        every {
            mockApi.deleteAccessToken(
                requestUrl = requestUrl,
                headersMap = any()
            )
        } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
        every { mockCall.request() } returns Request.Builder().url(requestUrl).addHeader(
            HEADER_KEY_ACCESS_TOKEN,
            "accessToken"
        ).build()
        every { mockCallback.onConnectionsRevokeResult(any(), any()) } returns Unit
    }
}
