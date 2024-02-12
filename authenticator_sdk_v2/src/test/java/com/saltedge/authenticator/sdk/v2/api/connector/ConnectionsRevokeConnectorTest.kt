/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.connector

import com.saltedge.android.test_tools.CommonTestTools
import com.saltedge.authenticator.core.api.ERROR_CLASS_API_RESPONSE
import com.saltedge.authenticator.core.api.ERROR_CLASS_HOST_UNREACHABLE
import com.saltedge.authenticator.core.api.HEADER_KEY_ACCESS_TOKEN
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.ConnectionAbs
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.sdk.v2.api.contract.ConnectionsV2RevokeListener
import com.saltedge.authenticator.sdk.v2.api.model.connection.RevokeConnectionResponse
import com.saltedge.authenticator.sdk.v2.api.model.connection.RevokeConnectionResponseData
import com.saltedge.authenticator.sdk.v2.api.retrofit.ApiInterface
import com.saltedge.authenticator.sdk.v2.defaultTestConnection
import com.saltedge.authenticator.sdk.v2.get404Response
import okhttp3.Request
import okhttp3.ResponseBody
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import retrofit2.Call
import retrofit2.Response
import java.net.ConnectException
import java.security.PrivateKey
import java.security.PublicKey

@RunWith(RobolectricTestRunner::class)
class ConnectionsRevokeConnectorTest {

    private val mockApi = mock<ApiInterface>()
    private val mockCallback = mock<ConnectionsV2RevokeListener>()
    private val mockCall = Mockito.mock(Call::class.java) as Call<RevokeConnectionResponse>
    private val requestConnection: ConnectionAbs = defaultTestConnection
    private val privateKey: PrivateKey = CommonTestTools.testPrivateKey
    private val publicKey: PublicKey = CommonTestTools.testPublicKey
    private val richConnection = RichConnection(requestConnection, privateKey, publicKey)
    private val requestUrl = "https://localhost/api/authenticator/v2/connections/${requestConnection.id}/revoke"
    private val request = Request.Builder().url(requestUrl).addHeader(HEADER_KEY_ACCESS_TOKEN, "accessToken").build()
    private lateinit var connector: ConnectionsRevokeConnector

    @Before
    @Throws(Exception::class)
    fun setUp() {
        BDDMockito.given(
            mockApi.revokeConnection(
                requestUrl = anyString(),
                headersMap = anyMap(),
                requestBody = any()
            )
        ).willReturn(mockCall)
        BDDMockito.given(mockCall.request()).willReturn(request)
        connector = ConnectionsRevokeConnector(mockApi, mockCallback)
    }

    @Test
    @Throws(Exception::class)
    fun valuesTest() {
        connector.callback = null

        Assert.assertNull(connector.callback)

        connector.callback = mockCallback

        Assert.assertNotNull(connector.callback)
    }

    @Test
    @Throws(Exception::class)
    fun revokeTokensForTest_allSuccess() {
        connector.revokeAccess(forConnections = listOf(richConnection))
        connector.onResponse(
            mockCall,
            Response.success(RevokeConnectionResponse(RevokeConnectionResponseData(
                revokedConnectionId = "333"
            )))
        )

        Mockito.verify(mockCallback).onConnectionsV2RevokeResult(
            revokedIDs = listOf("333"),
            apiErrors = emptyList()
        )
        Mockito.verifyNoMoreInteractions(mockCallback)
    }

    @Test
    @Throws(Exception::class)
    fun revokeTokensForTest_withError404() {
        connector.revokeAccess(listOf(richConnection))
        connector.onResponse(mockCall, get404Response())

        Mockito.verify(mockCallback).onConnectionsV2RevokeResult(
            revokedIDs = emptyList(),
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

        Mockito.verify(mockCallback).onConnectionsV2RevokeResult(
            revokedIDs = emptyList(),
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
            Response.error(404, ResponseBody.create(null, "{\"message\":\"Unknown error\"}"))
        )

        Mockito.verify(mockCallback).onConnectionsV2RevokeResult(
            revokedIDs = emptyList(),
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
        connector.onFailure(mockCall, ConnectException())

        Mockito.verify(mockCallback).onConnectionsV2RevokeResult(
            revokedIDs = emptyList(),
            apiErrors = listOf(ApiErrorData(
                errorClassName = ERROR_CLASS_HOST_UNREACHABLE,
                accessToken = "accessToken"
            ))
        )
        Mockito.verifyNoMoreInteractions(mockCallback)
    }
}
