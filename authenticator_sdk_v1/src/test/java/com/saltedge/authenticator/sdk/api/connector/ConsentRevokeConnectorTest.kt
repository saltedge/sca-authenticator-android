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
import com.saltedge.authenticator.sdk.contract.ConsentRevokeListener
import com.saltedge.authenticator.sdk.api.model.connection.ConnectionAndKey

import com.saltedge.authenticator.sdk.api.model.response.ConsentRevokeResponse
import com.saltedge.authenticator.sdk.api.model.response.ConsentRevokeResponseData
import com.saltedge.authenticator.sdk.api.ApiInterface
import com.saltedge.authenticator.sdk.api.HEADER_KEY_ACCESS_TOKEN
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
class ConsentRevokeConnectorTest {

    @Test
    @Throws(Exception::class)
    fun valuesTest() {
        val connector = ConsentRevokeConnector(mockApi, mockCallback)

        Assert.assertNotNull(connector.resultCallback)
    }

    @Test
    @Throws(Exception::class)
    fun givenConnector_whenFetchConsents_thenEnqueueCall() {
        //given
        val connector = ConsentRevokeConnector(mockApi, mockCallback)

        //when
        connector.revokeConsent("123", ConnectionAndKey(requestConnection, privateKey))

        //then
        verify { mockCall.enqueue(connector) }
        verify(exactly = 1) {
            mockApi.revokeConsent(
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
        val connector = ConsentRevokeConnector(mockApi, mockCallback)
        val response = Response.success(
            ConsentRevokeResponse(ConsentRevokeResponseData(true, "123"))
        )

        //when
        connector.onResponse(mockCall, response)

        //then
        verify {
            mockCallback.onConsentRevokeSuccess(ConsentRevokeResponseData(true, "123"))
        }
        confirmVerified(mockCallback)
    }

    @Test
    @Throws(Exception::class)
    fun givenConnectorAnd404Response_whenOnResponse_thenReturnErrors() {
        //given
        val connector = ConsentRevokeConnector(mockApi, mockCallback)
        val response: Response<ConsentRevokeResponse> = get404Response()

        //when
        connector.onResponse(mockCall, response)

        //then
        verify {
            mockCallback.onConsentRevokeFailure(
                error = ApiErrorData(
                    errorMessage = "Resource not found",
                    errorClassName = "NotFound",
                    accessToken = "accessToken"
                )
            )
        }
        confirmVerified(mockCallback)
    }

    private val requestUrl = "https://localhost/api/authenticator/v1/consents/123"
    private val requestConnection: ConnectionAbs = getDefaultTestConnection()
    private val mockApi: ApiInterface = mockkClass(ApiInterface::class)
    private val mockCallback = mockkClass(ConsentRevokeListener::class)
    private val mockCall: Call<ConsentRevokeResponse> =
        mockkClass(Call::class) as Call<ConsentRevokeResponse>
    private var capturedHeaders: MutableList<Map<String, String>> = mutableListOf()
    private var privateKey: PrivateKey = CommonTestTools.testPrivateKey

    @Before
    @Throws(Exception::class)
    fun setUp() {
        capturedHeaders = mutableListOf()
        every {
            mockApi.revokeConsent(
                requestUrl = requestUrl,
                headersMap = capture(capturedHeaders)
            )
        } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
        every { mockCall.request() } returns Request.Builder().url(requestUrl)
            .addHeader(HEADER_KEY_ACCESS_TOKEN, "accessToken")
            .build()
        every { mockCallback.onConsentRevokeSuccess(any()) } returns Unit
        every { mockCallback.onConsentRevokeFailure(any()) } returns Unit
    }
}
