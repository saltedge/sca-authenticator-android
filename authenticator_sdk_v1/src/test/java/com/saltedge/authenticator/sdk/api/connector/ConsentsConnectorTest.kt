/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.api.connector

import com.saltedge.android.test_tools.CommonTestTools
import com.saltedge.android.test_tools.getDefaultTestConnection
import com.saltedge.authenticator.core.api.HEADER_KEY_ACCESS_TOKEN
import com.saltedge.authenticator.core.api.model.EncryptedData
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.ConnectionAbs
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.sdk.contract.FetchEncryptedDataListener
import com.saltedge.authenticator.core.api.model.EncryptedListResponse
import com.saltedge.authenticator.sdk.api.ApiInterface
import com.saltedge.authenticator.sdk.testTools.get404Response
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

@RunWith(RobolectricTestRunner::class)
class ConsentsConnectorTest {

    @Test
    @Throws(Exception::class)
    fun valuesTest() {
        val connector = ConsentsConnector(
            mockApi,
            listOf(RichConnection(requestConnection, privateKey)),
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
            listOf(RichConnection(requestConnection, privateKey)),
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
            listOf(RichConnection(requestConnection, privateKey)),
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
            listOf(RichConnection(requestConnection, privateKey)),
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
    private val mockCallback = mockk<FetchEncryptedDataListener>(relaxed = true)
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
    }
}
