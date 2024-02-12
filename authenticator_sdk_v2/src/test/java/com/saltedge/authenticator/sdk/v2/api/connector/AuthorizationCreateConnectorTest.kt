/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.connector

import com.saltedge.android.test_tools.CommonTestTools
import com.saltedge.authenticator.core.api.HEADER_KEY_ACCESS_TOKEN
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.ConnectionAbs
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.sdk.v2.api.contract.AuthorizationCreateListener
import com.saltedge.authenticator.sdk.v2.api.model.authorization.CreateAuthorizationResponse
import com.saltedge.authenticator.sdk.v2.api.model.authorization.CreateAuthorizationResponseData
import com.saltedge.authenticator.sdk.v2.api.retrofit.ApiInterface
import com.saltedge.authenticator.sdk.v2.defaultTestConnection
import com.saltedge.authenticator.sdk.v2.get404Response
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.verify
import okhttp3.Request
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Call
import retrofit2.Response
import java.security.PrivateKey
import java.security.PublicKey

@RunWith(RobolectricTestRunner::class)
class AuthorizationCreateConnectorTest {

    @Test
    @Throws(Exception::class)
    fun valuesTest() {
        val connector = AuthorizationCreateConnector(mockApi, null)

        Assert.assertNull(connector.callback)

        connector.callback = mockCallback

        Assert.assertNotNull(connector.callback)
    }

    @Test
    @Throws(Exception::class)
    fun postCreateTest_allSuccess() {
        val connector = AuthorizationCreateConnector(mockApi, mockCallback)
        connector.createAuthorizationForAction(
            richConnection = testRichConnection,
            actionID = testActionId
        )

        verify { mockCall.enqueue(connector) }

        connector.onResponse(
            mockCall,
            Response.success(CreateAuthorizationResponse(
                CreateAuthorizationResponseData(
                    connectionID = testConnection.id,
                    authorizationID = testAuthorizationId
                )
            ))
        )

        verify {
            mockCallback.onAuthorizationCreateSuccess(
                authorizationID = testAuthorizationId,
                connectionID = testConnection.id
            )
        }
        confirmVerified(mockCallback)
    }

    @Test
    @Throws(Exception::class)
    fun postCreateTest_withError() {
        val connector = AuthorizationCreateConnector(mockApi, mockCallback)
        connector.createAuthorizationForAction(
            richConnection = testRichConnection,
            actionID = testActionId
        )

        verify { mockCall.enqueue(connector) }

        connector.onResponse(mockCall, get404Response())

        verify {
            mockCallback.onAuthorizationCreateFailure(
                ApiErrorData(
                    errorMessage = "Resource not found",
                    errorClassName = "NotFound",
                    accessToken = "accessToken"
                )
            )
        }
        confirmVerified(mockCallback)
    }

    private val mockApi: ApiInterface = mockkClass(ApiInterface::class)
    private val mockCallback = mockkClass(AuthorizationCreateListener::class)
    private val mockCall = mockkClass(Call::class) as Call<CreateAuthorizationResponse>

    private var testPrivateKey: PrivateKey = CommonTestTools.testPrivateKey
    private var testPublicKey: PublicKey = CommonTestTools.testPublicKey
    private val testConnection: ConnectionAbs = defaultTestConnection
    private val testRichConnection = RichConnection(testConnection, testPrivateKey, testPublicKey)
    private val testAuthorizationId = "333"
    private val testActionId = "444"
    private val requestUrl = "https://localhost/api/authenticator/v2/authorizations"

    @Before
    @Throws(Exception::class)
    fun setUp() {
        every {
            mockApi.createAuthorizationForAction(
                requestUrl = requestUrl,
                headersMap = any(),
                requestBody = any()
            )
        } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
        every { mockCall.request() } returns Request.Builder().url(requestUrl)
            .addHeader(HEADER_KEY_ACCESS_TOKEN, "accessToken").build()
        every {
            mockCallback.onAuthorizationCreateFailure(error = any())
        } returns Unit
        every { mockCallback.onAuthorizationCreateSuccess(authorizationID = testAuthorizationId, connectionID = testConnection.id) } returns Unit
    }
}
