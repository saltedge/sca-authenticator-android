/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.connector

import com.saltedge.android.test_tools.CommonTestTools
import com.saltedge.authenticator.core.api.HEADER_KEY_ACCESS_TOKEN
import com.saltedge.authenticator.core.api.model.EncryptedData
import com.saltedge.authenticator.core.api.model.EncryptedListResponse
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.ConnectionAbs
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.sdk.v2.api.contract.FetchConsentsListener
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
class ConsentsIndexConnectorTest {

    @Test
    @Throws(Exception::class)
    fun valuesTest() {
        val connector = ConsentsIndexConnector(RestClient.apiInterface, null)

        Assert.assertNull(connector.callback)

        connector.callback = mockCallback

        Assert.assertNotNull(connector.callback)
    }

    @Test
    @Throws(Exception::class)
    fun fetchActiveConsentsTest_allSuccess() {
        val connector = ConsentsIndexConnector(mockApi, mockCallback)
        connector.fetchActiveConsents(listOf(RichConnection(requestConnection, privateKey, publicKey)))

        verify { mockCall.enqueue(connector) }

        connector.onResponse(
            mockCall,
            Response.success(EncryptedListResponse(data = listOf(
                EncryptedData(
                    id = "444",
                    connectionId = "333",
                    iv = "o3TDCc3rKYTx...RVH+aOFpS9NIg==\n",
                    key = "BtV7EB3Erv8xEQ.../jeBRyFa75A6po5XlwWiEiuzQ==\n",
                    data = "YlnrNOHvUIPem/O58rMzdsvkXidLvgGpdMalD9c1mlg=\n"
                )
            )))
        )

        verify {
            mockCallback.onFetchConsentsV2Result(
                result = listOf(
                    EncryptedData(
                        id = "444",
                        connectionId = "333",
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
    fun fetchActiveConsentsTest_withError() {
        val connector = ConsentsIndexConnector(mockApi, mockCallback)
        connector.fetchActiveConsents(listOf(RichConnection(requestConnection, privateKey, publicKey)))

        verify(exactly = 1) {
            mockApi.activeConsents(
                requestUrl = requestUrl,
                headersMap = capturedHeaders.first()
            )
        }
        verify { mockCall.enqueue(connector) }
        assertThat(capturedHeaders.first()[HEADER_KEY_ACCESS_TOKEN], equalTo("accessToken"))

        connector.onResponse(mockCall, get404Response())

        verify {
            mockCallback.onFetchConsentsV2Result(
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

    private val requestUrl = "https://localhost/api/authenticator/v2/consents"
    private val requestConnection: ConnectionAbs = defaultTestConnection
    private val mockApi: ApiInterface = mockkClass(ApiInterface::class)
    private val mockCallback = mockk<FetchConsentsListener>(relaxed = true)
    private val mockCall = mockkClass(Call::class) as Call<EncryptedListResponse>
    private var capturedHeaders: MutableList<Map<String, String>> = mutableListOf()
    private var privateKey: PrivateKey = CommonTestTools.testPrivateKey
    private var publicKey: PublicKey = CommonTestTools.testPublicKey

    @Before
    @Throws(Exception::class)
    fun setUp() {
        capturedHeaders = mutableListOf()
        every {
            mockApi.activeConsents(
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
