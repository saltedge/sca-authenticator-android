/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2020 Salt Edge Inc.
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

import com.saltedge.android.test_tools.CommonTestTools
import com.saltedge.android.test_tools.getDefaultTestConnection
import com.saltedge.authenticator.sdk.contract.FetchEncryptedDataListener
import com.saltedge.authenticator.sdk.model.EncryptedData
import com.saltedge.authenticator.sdk.model.connection.ConnectionAbs
import com.saltedge.authenticator.sdk.model.connection.ConnectionAndKey
import com.saltedge.authenticator.sdk.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.model.response.EncryptedListResponse
import com.saltedge.authenticator.sdk.network.ApiInterface
import com.saltedge.authenticator.sdk.network.HEADER_KEY_ACCESS_TOKEN
import com.saltedge.authenticator.sdk.testTools.get404Response
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

@RunWith(RobolectricTestRunner::class)
class ConsentsConnectorTest {

    @Test
    @Throws(Exception::class)
    fun valuesTest() {
        val connector = ConsentsConnector(
            mockApi,
            listOf(ConnectionAndKey(requestConnection, privateKey)),
            mockCallback
        )

        Assert.assertNotNull(connector.resultCallback)
        Assert.assertNotNull(connector.connectionsAndKeys)
    }

    @Test
    @Throws(Exception::class)
    fun givenConnector_whenFetchConsents_thenEnqueueCall() {
        //given
        val connector = ConsentsConnector(
            mockApi,
            listOf(ConnectionAndKey(requestConnection, privateKey)),
            mockCallback
        )

        //when
        connector.fetchConsents()

        //then
        verify { mockCall.enqueue(connector) }
        verify(exactly = 1) {
            mockApi.getConsents(
                requestUrl = requestUrl,
                headersMap = capturedHeaders.first()
            )
        }

        assertThat(capturedHeaders.first()[HEADER_KEY_ACCESS_TOKEN], equalTo("accessToken"))
        confirmVerified(mockCallback)
    }

    @Test
    @Throws(Exception::class)
    fun givenConnectorAndSuccessResponse_whenOnResponse_thenReturnListOfEncryptedData() {
        //given
        val connector = ConsentsConnector(
            mockApi,
            listOf(ConnectionAndKey(requestConnection, privateKey)),
            mockCallback
        )
        val response = Response.success(
            EncryptedListResponse(
                data = listOf(
                    EncryptedData(
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

        //when
        connector.onResponse(mockCall, response)

        //then
        verify {
            mockCallback.onFetchEncryptedDataResult(
                result = listOf(
                    EncryptedData(
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
    fun givenConnectorAnd404Response_whenOnResponse_thenReturnErrors() {
        //given
        val connector = ConsentsConnector(
            mockApi,
            listOf(ConnectionAndKey(requestConnection, privateKey)),
            mockCallback
        )
        val response: Response<EncryptedListResponse> = get404Response()

        //when
        connector.onResponse(mockCall, response)

        //then
        verify {
            mockCallback.onFetchEncryptedDataResult(
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

    private val requestUrl = "https://localhost/api/authenticator/v1/consents"
    private val requestConnection: ConnectionAbs = getDefaultTestConnection()
    private val mockApi: ApiInterface = mockkClass(ApiInterface::class)
    private val mockCallback = mockkClass(FetchEncryptedDataListener::class)
    private val mockCall: Call<EncryptedListResponse> =
        mockkClass(Call::class) as Call<EncryptedListResponse>
    private var capturedHeaders: MutableList<Map<String, String>> = mutableListOf()
    private var privateKey: PrivateKey = CommonTestTools.testPrivateKey

    @Before
    @Throws(Exception::class)
    fun setUp() {
        capturedHeaders = mutableListOf()
        every {
            mockApi.getConsents(
                requestUrl = requestUrl,
                headersMap = capture(capturedHeaders)
            )
        } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
        every { mockCall.request() } returns Request.Builder().url(requestUrl)
            .addHeader(HEADER_KEY_ACCESS_TOKEN, "accessToken")
            .build()
        every { mockCallback.onFetchEncryptedDataResult(any(), any()) } returns Unit
    }
}
