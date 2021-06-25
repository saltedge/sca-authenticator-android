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
package com.saltedge.authenticator.sdk.api.connector

import com.saltedge.android.test_tools.CommonTestTools
import com.saltedge.android.test_tools.getDefaultTestConnection
import com.saltedge.authenticator.core.api.ERROR_CLASS_API_RESPONSE
import com.saltedge.authenticator.core.api.ERROR_CLASS_HOST_UNREACHABLE
import com.saltedge.authenticator.core.api.HEADER_KEY_ACCESS_TOKEN
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.ConnectionAbs
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.sdk.api.ApiInterface
import com.saltedge.authenticator.sdk.api.model.response.RevokeAccessTokenResponse
import com.saltedge.authenticator.sdk.api.model.response.RevokeAccessTokenResponseData
import com.saltedge.authenticator.sdk.contract.ConnectionsRevokeListener
import com.saltedge.authenticator.sdk.testTools.get404Response
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.mockito.kotlin.argumentCaptor
import org.robolectric.RobolectricTestRunner
import retrofit2.Call
import retrofit2.Response
import java.net.UnknownHostException
import java.security.PrivateKey

@RunWith(RobolectricTestRunner::class)
class ConnectionsRevokeConnectorTest {

    private val mockApi: ApiInterface = Mockito.mock(ApiInterface::class.java)
    private val mockCallback: ConnectionsRevokeListener = Mockito.mock(ConnectionsRevokeListener::class.java)
    private val mockCall: Call<RevokeAccessTokenResponse> = Mockito.mock(Call::class.java) as Call<RevokeAccessTokenResponse>
    private val requestConnection: ConnectionAbs = getDefaultTestConnection()
    private val requestUrl = "https://localhost/api/authenticator/v1/connections"
    private val request = Request.Builder().url(requestUrl).addHeader(HEADER_KEY_ACCESS_TOKEN, "accessToken").build()
    private var privateKey: PrivateKey = CommonTestTools.testPrivateKey
    private lateinit var connector: ConnectionsRevokeConnector

    @Before
    @Throws(Exception::class)
    fun setUp() {
        given(mockApi.revokeConnection(
            requestUrl = Mockito.anyString(),
            headersMap = Mockito.anyMap()
        )).willReturn(mockCall)
        given(mockCall.request()).willReturn(request)

        connector = ConnectionsRevokeConnector(mockApi, mockCallback)
    }

    @Test
    @Throws(Exception::class)
    fun valuesTest() {
        connector.resultCallback = null

        Assert.assertNull(connector.resultCallback)

        connector.resultCallback = mockCallback

        Assert.assertNotNull(connector.resultCallback)
    }

    @Test
    @Throws(Exception::class)
    fun revokeTokensForTest_allSuccess() {
        connector.revokeTokensFor(listOf(RichConnection(requestConnection, privateKey)))
        connector.onResponse(
            mockCall,
            Response.success(
                RevokeAccessTokenResponse(
                    RevokeAccessTokenResponseData(success = true, accessToken = "accessToken")
                )
            )
        )

        val requestUrlCaptor = argumentCaptor<String>()
        Mockito.verify(mockApi).revokeConnection(
            requestUrl = requestUrlCaptor.capture(),
            headersMap = Mockito.anyMap()
        )
        assertThat(requestUrlCaptor.firstValue, equalTo(requestUrl))
        Mockito.verify(mockCallback).onConnectionsRevokeResult(
            revokedTokens = listOf("accessToken"),
            apiErrors = emptyList()
        )
        Mockito.verifyNoMoreInteractions(mockCallback)
    }

    @Test
    @Throws(Exception::class)
    fun revokeTokensForTest_withError404() {
        connector.revokeTokensFor(listOf(RichConnection(requestConnection, privateKey)))
        connector.onResponse(mockCall, get404Response())

        Mockito.verify(mockCallback).onConnectionsRevokeResult(
            revokedTokens = emptyList(),
            apiErrors = listOf(ApiErrorData(
                errorMessage = "Resource not found",
                errorClassName = "NotFound",
                accessToken = "accessToken"
            ))
        )
        Mockito.verifyNoMoreInteractions(mockCallback)
    }

    @Test
    @Throws(Exception::class)
    fun revokeTokensForTest_successWithoutResponseBody() {
        connector.onResponse(mockCall, Response.success(null))

        Mockito.verify(mockCallback).onConnectionsRevokeResult(
            revokedTokens = emptyList(),
            apiErrors = listOf(ApiErrorData(
                errorMessage = "Request Error (200)",
                errorClassName = ERROR_CLASS_API_RESPONSE,
                accessToken = "accessToken"
            ))
        )
        Mockito.verifyNoMoreInteractions(mockCallback)
    }

    @Test
    @Throws(Exception::class)
    fun revokeTokensForTest_withUnknownError() {
        connector.onResponse(
            mockCall,
            Response.error(404, "{\"message\":\"Unknown error\"}".toResponseBody(null))
        )

        Mockito.verify(mockCallback).onConnectionsRevokeResult(
            revokedTokens = emptyList(),
            apiErrors = listOf(ApiErrorData(
                errorMessage = "Request Error (404)",
                errorClassName = ERROR_CLASS_API_RESPONSE,
                accessToken = "accessToken"
            ))
        )
        Mockito.verifyNoMoreInteractions(mockCallback)
    }

    @Test
    @Throws(Exception::class)
    fun revokeTokensForTest_withException() {
        //given
        val exception = UnknownHostException()


        //when
        connector.onFailure(mockCall, exception)

        //then
        Mockito.verify(mockCallback).onConnectionsRevokeResult(
            revokedTokens = emptyList(),
            apiErrors = listOf(ApiErrorData(
                errorClassName = ERROR_CLASS_HOST_UNREACHABLE,
                accessToken = "accessToken"
            ))
        )
        Mockito.verifyNoMoreInteractions(mockCallback)
    }
}
