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
import com.saltedge.authenticator.sdk.v2.api.contract.FetchAuthorizationListener
import com.saltedge.authenticator.sdk.v2.api.model.authorization.AuthorizationResponse
import com.saltedge.authenticator.sdk.v2.api.model.authorization.AuthorizationResponseData
import com.saltedge.authenticator.sdk.v2.api.model.connection.ConnectionV2Abs
import com.saltedge.authenticator.sdk.v2.api.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.v2.api.retrofit.ApiInterface
import com.saltedge.authenticator.sdk.v2.api.retrofit.HEADER_KEY_ACCESS_TOKEN
import com.saltedge.authenticator.sdk.v2.defaultTestConnection
import com.saltedge.authenticator.sdk.v2.get404Response
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.verify
import okhttp3.Request
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
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
class AuthorizationShowConnectorTest {

    @Test
    @Throws(Exception::class)
    fun valuesTest() {
        val connector = AuthorizationShowConnector(mockApi, null)

        Assert.assertNull(connector.resultCallback)

        connector.resultCallback = mockCallback

        Assert.assertNotNull(connector.resultCallback)
    }

    @Test
    @Throws(Exception::class)
    fun fetchAuthorizationTest_allSuccess() {
        val connector = AuthorizationShowConnector(mockApi, mockCallback)
        connector.showAuthorization(
            connection = requestConnection,
            authorizationId = requestAuthorizationId
        )

        verify { mockCall.enqueue(connector) }

        connector.onResponse(
            mockCall, Response.success(
            AuthorizationResponse(
                AuthorizationResponseData(
                    id = "444",
                    connectionId = "333",
                    status = "pending",
                    iv = "o3TDCc3rKYTx...RVH+aOFpS9NIg==\n",
                    key = "BtV7EB3Erv8xEQ.../jeBRyFa75A6po5XlwWiEiuzQ==\n",
                    data = "YlnrNOHvUIPem/O58rMzdsvkXidLvgGpdMalD9c1mlg=\n"
                )
            )
        )
        )

        verify {
            mockCallback.onFetchAuthorizationSuccess(
                result = AuthorizationResponseData(
                    id = "444",
                    connectionId = "333",
                    status = "pending",
                    iv = "o3TDCc3rKYTx...RVH+aOFpS9NIg==\n",
                    key = "BtV7EB3Erv8xEQ.../jeBRyFa75A6po5XlwWiEiuzQ==\n",
                    data = "YlnrNOHvUIPem/O58rMzdsvkXidLvgGpdMalD9c1mlg=\n"
                )
            )
        }
        confirmVerified(mockCallback)
    }

    @Test
    @Throws(Exception::class)
    fun fetchAuthorizationTest_withError() {
        val connector = AuthorizationShowConnector(mockApi, mockCallback)
        connector.showAuthorization(connection = defaultTestConnection, authorizationId = requestAuthorizationId)

        verify(exactly = 1) {
            mockApi.showAuthorization(
                requestUrl = requestUrl,
                headersMap = capturedHeaders.first()
            )
        }
        verify { mockCall.enqueue(connector) }
        assertThat(capturedHeaders.first()[HEADER_KEY_ACCESS_TOKEN], equalTo("accessToken"))

        connector.onResponse(mockCall, get404Response())

        verify {
            mockCallback.onFetchAuthorizationFailed(
                error = ApiErrorData(
                    errorMessage = "Resource not found",
                    errorClassName = "NotFound",
                    accessToken = "accessToken"
                )
            )
        }
        confirmVerified(mockCallback)
    }

    private val requestUrl = "https://localhost/api/authenticator/v2/authorizations/authId"
    private val requestConnection: ConnectionV2Abs = defaultTestConnection
    private val requestAuthorizationId = "authId"
    private val mockApi: ApiInterface = mockkClass(ApiInterface::class)
    private val mockCallback = mockkClass(FetchAuthorizationListener::class)
    private val mockCall = mockkClass(Call::class) as Call<AuthorizationResponse>
    private var capturedHeaders: MutableList<Map<String, String>> = mutableListOf()

    @Before
    @Throws(Exception::class)
    fun setUp() {
        capturedHeaders = mutableListOf()
        every {
            mockApi.showAuthorization(requestUrl = requestUrl, headersMap = capture(capturedHeaders))
        } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
        every { mockCall.request() } returns Request.Builder().url(requestUrl)
            .addHeader(HEADER_KEY_ACCESS_TOKEN, "accessToken").build()
        every { mockCallback.onFetchAuthorizationSuccess(any()) } returns Unit
        every { mockCallback.onFetchAuthorizationFailed(any()) } returns Unit
    }
}
