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
import com.saltedge.authenticator.sdk.contract.FetchAuthorizationsContract
import com.saltedge.authenticator.sdk.model.ApiErrorData
import com.saltedge.authenticator.sdk.model.ConnectionAbs
import com.saltedge.authenticator.sdk.model.ConnectionAndKey
import com.saltedge.authenticator.sdk.model.EncryptedAuthorizationData
import com.saltedge.authenticator.sdk.model.response.AuthorizationsResponseData
import com.saltedge.authenticator.sdk.network.ApiInterface
import com.saltedge.authenticator.sdk.network.HEADER_KEY_ACCESS_TOKEN
import com.saltedge.authenticator.sdk.network.RestClient
import com.saltedge.authenticator.sdk.testTools.TestConnection
import com.saltedge.authenticator.sdk.testTools.get404Response
import com.saltedge.authenticator.sdk.tools.KeyStoreManager
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.verify
import okhttp3.Request
import okhttp3.ResponseBody
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Call
import retrofit2.Response
import java.security.PrivateKey

@RunWith(AndroidJUnit4::class)
class AuthorizationsConnectorTest {

    @Test
    @Throws(Exception::class)
    fun valuesTest() {
        val connector = AuthorizationsConnector(RestClient.apiInterface, null)

        Assert.assertNull(connector.resultCallback)

        connector.resultCallback = mockCallback

        Assert.assertNotNull(connector.resultCallback)
    }

    @Test
    @Throws(Exception::class)
    fun fetchAuthorizationsTest_allSuccess() {
        val connector = AuthorizationsConnector(mockApi, mockCallback)
        connector.fetchAuthorizations(listOf(ConnectionAndKey(requestConnection, privateKey)))

        verify { mockCall.enqueue(connector) }

        connector.onResponse(
            mockCall, Response.success(
            AuthorizationsResponseData(
                data = listOf(
                    EncryptedAuthorizationData(
                        id = "444",
                        connectionId = "333",
                        algorithm = "AES-256-CBC",
                        iv = "o3TDCc3rKYTx...RVH+aOFpS9NIg==\n",
                        key = "BtV7EB3Erv8xEQ.../jeBRyFa75A6po5XlwWiEiuzQ==\n",
                        data = "YlnrNOHvUIPem/O58rMzdsvkXidLvgGpdMalD9c1mlg=\n"
                    )
                )
            )
        )
        )

        verify {
            mockCallback.onFetchAuthorizationsResult(
                result = listOf(
                    EncryptedAuthorizationData(
                        id = "444",
                        connectionId = "333",
                        algorithm = "AES-256-CBC",
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
        val connector = AuthorizationsConnector(mockApi, mockCallback)
        connector.fetchAuthorizations(listOf(ConnectionAndKey(requestConnection, privateKey)))

        verify(exactly = 1) {
            mockApi.getAuthorizations(
                requestUrl = requestUrl,
                headersMap = capturedHeaders.first()
            )
        }
        verify { mockCall.enqueue(connector) }
        assertThat(capturedHeaders.first()[HEADER_KEY_ACCESS_TOKEN], equalTo("accessToken"))

        connector.onResponse(
            mockCall,
            Response.error(404, ResponseBody.create(null, get404Response()))
        )

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

    private val requestUrl = "https://localhost/api/authenticator/v1/authorizations"
    private var privateKey: PrivateKey = KeyStoreManager.createOrReplaceRsaKeyPair("test")!!.private
    private val requestConnection: ConnectionAbs = TestConnection(
        id = "333",
        guid = "test",
        connectUrl = "https://localhost",
        accessToken = "accessToken"
    )
    private val mockApi: ApiInterface = mockkClass(ApiInterface::class)
    private val mockCallback = mockkClass(FetchAuthorizationsContract::class)
    private val mockCall: Call<AuthorizationsResponseData> =
        mockkClass(Call::class) as Call<AuthorizationsResponseData>
    private var capturedHeaders: MutableList<Map<String, String>> = mutableListOf()

    @Before
    @Throws(Exception::class)
    fun setUp() {
        capturedHeaders = mutableListOf()
        every {
            mockApi.getAuthorizations(
                requestUrl = requestUrl,
                headersMap = capture(capturedHeaders)
            )
        } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
        every { mockCall.request() } returns Request.Builder().url(requestUrl).addHeader(
            HEADER_KEY_ACCESS_TOKEN,
            "accessToken"
        ).build()
        every { mockCallback.getConnectionsData() } returns null
        every { mockCallback.onFetchAuthorizationsResult(any(), any()) } returns Unit
    }
}
