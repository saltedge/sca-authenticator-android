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

import com.saltedge.authenticator.core.api.model.EncryptedBundle
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.v2.api.contract.ConnectionCreateListener
import com.saltedge.authenticator.sdk.v2.api.model.connection.CreateConnectionRequest
import com.saltedge.authenticator.sdk.v2.api.model.connection.CreateConnectionRequestData
import com.saltedge.authenticator.sdk.v2.api.model.connection.CreateConnectionResponse
import com.saltedge.authenticator.sdk.v2.api.model.connection.CreateConnectionResponseData
import com.saltedge.authenticator.sdk.v2.api.retrofit.ApiInterface
import com.saltedge.authenticator.sdk.v2.config.ApiV2Config
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

@RunWith(RobolectricTestRunner::class)
class ConnectionCreateConnectorTest {

    @Test
    @Throws(Exception::class)
    fun valuesTest() {
        val connector = ConnectionCreateConnector(mockApi, null)

        Assert.assertNull(connector.resultCallback)

        connector.resultCallback = mockCallback

        Assert.assertNotNull(connector.resultCallback)
    }

    @Test
    @Throws(Exception::class)
    fun postConnectionDataTest_allSuccess() {
        val connector = ConnectionCreateConnector(mockApi, mockCallback)
        connector.postConnectionData(
            baseUrl = "https://localhost",
            providerId = "111",
            pushToken = "pushToken",
            encryptedRsaPublicKey = EncryptedBundle(
                encryptedAesKey = "o3TDCc3rKYTx...RVH+aOFpS9NIg==\n",
                encryptedAesIv = "BtV7EB3Erv8xEQ.../jeBRyFa75A6po5XlwWiEiuzQ==\n",
                encryptedData = "YlnrNOHvUIPem/O58rMzdsvkXidLvgGpdMalD9c1mlg=\n"
            ),
            connectQueryParam = "1234567890"
        )

        verify { mockCall.enqueue(connector) }

        connector.onResponse(
            mockCall,
            Response.success(CreateConnectionResponse(CreateConnectionResponseData(
                connectionId = "333", authenticationUrl = "url"
            )))
        )

        verify {
            mockCallback.onConnectionCreateSuccess(
                authenticationUrl = "url",
                connectionId = "333"
            )
        }
        confirmVerified(mockCallback)
    }

    @Test
    @Throws(Exception::class)
    fun postConnectionDataTest_withError() {
        val connector = ConnectionCreateConnector(mockApi, mockCallback)
        connector.postConnectionData(
            baseUrl = "https://localhost",
            providerId = "111",
            pushToken = "pushToken",
            encryptedRsaPublicKey = EncryptedBundle(
                encryptedAesKey = "o3TDCc3rKYTx...RVH+aOFpS9NIg==\n",
                encryptedAesIv = "BtV7EB3Erv8xEQ.../jeBRyFa75A6po5XlwWiEiuzQ==\n",
                encryptedData = "YlnrNOHvUIPem/O58rMzdsvkXidLvgGpdMalD9c1mlg=\n"
            ),
            connectQueryParam = "1234567890"
        )

        verify { mockApi.createConnection(requestUrl = requestUrl, body = requestData) }
        verify { mockCall.enqueue(connector) }

        connector.onResponse(mockCall, get404Response())

        verify {
            mockCallback.onConnectionCreateFailure(
                ApiErrorData(errorMessage = "Resource not found", errorClassName = "NotFound")
            )
        }
        confirmVerified(mockCallback)
    }

    private val mockApi: ApiInterface = mockkClass(ApiInterface::class)
    private val mockCallback = mockkClass(ConnectionCreateListener::class)
    private val mockCall = mockkClass(Call::class) as Call<CreateConnectionResponse>
    private val requestData = CreateConnectionRequest(
        data = CreateConnectionRequestData(
            providerId = "111",
            returnUrl = ApiV2Config.authenticationReturnUrl,
            platform = "android",
            pushToken = "pushToken",
            connectQueryParam = "1234567890",
            encryptedAppRsaPublicKey = EncryptedBundle(
                encryptedAesKey = "o3TDCc3rKYTx...RVH+aOFpS9NIg==\n",
                encryptedAesIv = "BtV7EB3Erv8xEQ.../jeBRyFa75A6po5XlwWiEiuzQ==\n",
                encryptedData = "YlnrNOHvUIPem/O58rMzdsvkXidLvgGpdMalD9c1mlg=\n"
            )
        )
    )
    private val requestUrl = "https://localhost/api/authenticator/v2/connections"

    @Before
    @Throws(Exception::class)
    fun setUp() {
        every {
            mockApi.createConnection(requestUrl = requestUrl, body = any())
        } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
        every { mockCall.request() } returns Request.Builder().url(requestUrl).build()
        every { mockCallback.onConnectionCreateSuccess(authenticationUrl = any(), connectionId = any()) } returns Unit
        every { mockCallback.onConnectionCreateFailure(any()) } returns Unit
    }
}
