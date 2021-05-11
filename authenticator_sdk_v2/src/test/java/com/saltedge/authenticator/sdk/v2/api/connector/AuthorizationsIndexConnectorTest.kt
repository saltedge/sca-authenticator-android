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
import com.saltedge.authenticator.core.api.HEADER_KEY_ACCESS_TOKEN
import com.saltedge.authenticator.sdk.v2.api.contract.FetchAuthorizationsListener
import com.saltedge.authenticator.sdk.v2.api.model.authorization.AuthorizationResponseData
import com.saltedge.authenticator.sdk.v2.api.model.authorization.AuthorizationsListResponse
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.ConnectionAbs
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.sdk.v2.api.retrofit.ApiInterface
import com.saltedge.authenticator.sdk.v2.api.retrofit.RestClient
import com.saltedge.authenticator.sdk.v2.defaultTestConnection
import com.saltedge.authenticator.sdk.v2.get404Response
import io.mockk.*
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
class AuthorizationsIndexConnectorTest {

    @Test
    @Throws(Exception::class)
    fun valuesTest() {
        val connector = AuthorizationsIndexConnector(RestClient.apiInterface, null)

        Assert.assertNull(connector.resultCallback)

        connector.resultCallback = mockCallback

        Assert.assertNotNull(connector.resultCallback)
    }

    @Test
    @Throws(Exception::class)
    fun fetchAuthorizationsTest_allSuccess() {
        val connector = AuthorizationsIndexConnector(mockApi, mockCallback)
        connector.fetchActiveAuthorizations(listOf(RichConnection(requestConnection, privateKey, publicKey)))

        verify { mockCall.enqueue(connector) }

        connector.onResponse(
            mockCall,
            Response.success(AuthorizationsListResponse(data = listOf(
                AuthorizationResponseData(
                    id = "444",
                    connectionId = "333",
                    status = "pending",
                    iv = "o3TDCc3rKYTx...RVH+aOFpS9NIg==\n",
                    key = "BtV7EB3Erv8xEQ.../jeBRyFa75A6po5XlwWiEiuzQ==\n",
                    data = "YlnrNOHvUIPem/O58rMzdsvkXidLvgGpdMalD9c1mlg=\n"
                )
            )))
        )

        verify {
            mockCallback.onFetchAuthorizationsResult(
                result = listOf(
                    AuthorizationResponseData(
                        id = "444",
                        connectionId = "333",
                        status = "pending",
                        iv = "o3TDCc3rKYTx...RVH+aOFpS9NIg==\n",
                        key = "BtV7EB3Erv8xEQ.../jeBRyFa75A6po5XlwWiEiuzQ==\n",
                        data = "YlnrNOHvUIPem/O58rMzdsvkXidLvgGpdMalD9c1mlg=\n"
                    )
                ),
                errors = emptyList()
            )
        }
        confirmVerified(mockCallback)
    }

    @Test
    @Throws(Exception::class)
    fun fetchAuthorizationsTest_withError() {
        val connector = AuthorizationsIndexConnector(mockApi, mockCallback)
        connector.fetchActiveAuthorizations(listOf(RichConnection(requestConnection, privateKey, publicKey)))

        verify(exactly = 1) {
            mockApi.activeAuthorizations(
                requestUrl = requestUrl,
                headersMap = capturedHeaders.first()
            )
        }
        verify { mockCall.enqueue(connector) }
        assertThat(capturedHeaders.first()[HEADER_KEY_ACCESS_TOKEN], equalTo("accessToken"))

        connector.onResponse(mockCall, get404Response())

        verify {
            mockCallback.onFetchAuthorizationsResult(
                result = emptyList(),
                errors = listOf(
                    ApiErrorData(
                        errorMessage = "Resource not found",
                        errorClassName = "NotFound",
                        accessToken = "accessToken"
                    )
                )
            )
        }
        confirmVerified(mockCallback)
    }

    private val requestUrl = "https://localhost/api/authenticator/v2/authorizations"
    private val requestConnection: ConnectionAbs = defaultTestConnection
    private val mockApi: ApiInterface = mockkClass(ApiInterface::class)
    private val mockCallback = mockk<FetchAuthorizationsListener>(relaxed = true)
    private val mockCall = mockkClass(Call::class) as Call<AuthorizationsListResponse>
    private var capturedHeaders: MutableList<Map<String, String>> = mutableListOf()
    private var privateKey: PrivateKey = CommonTestTools.testPrivateKey
    private var publicKey: PublicKey = CommonTestTools.testPublicKey

    @Before
    @Throws(Exception::class)
    fun setUp() {
        capturedHeaders = mutableListOf()
        every {
            mockApi.activeAuthorizations(
                requestUrl = requestUrl,
                headersMap = capture(capturedHeaders)
            )
        } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
        every { mockCall.request() } returns Request.Builder().url(requestUrl).addHeader(
            HEADER_KEY_ACCESS_TOKEN,
            "accessToken"
        ).build()
    }
}
