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
import com.saltedge.authenticator.sdk.contract.ConnectionInitResult
import com.saltedge.authenticator.sdk.model.ApiErrorData
import com.saltedge.authenticator.sdk.model.createInvalidResponseError
import com.saltedge.authenticator.sdk.model.request.CreateConnectionData
import com.saltedge.authenticator.sdk.model.request.CreateConnectionRequestData
import com.saltedge.authenticator.sdk.model.response.AuthenticateConnectionData
import com.saltedge.authenticator.sdk.model.response.CreateConnectionResponseData
import com.saltedge.authenticator.sdk.network.ApiInterface
import com.saltedge.authenticator.sdk.testTools.get404Response
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.verify
import okhttp3.Request
import okhttp3.ResponseBody
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Call
import retrofit2.Response

@RunWith(AndroidJUnit4::class)
class ConnectionInitConnectorTest {

    @Test
    @Throws(Exception::class)
    fun valuesTest() {
        val connector = ConnectionInitConnector(mockApi, null)

        Assert.assertNull(connector.resultCallback)

        connector.resultCallback = mockCallback

        Assert.assertNotNull(connector.resultCallback)
    }

    @Test
    @Throws(Exception::class)
    fun postConnectionDataTest_allSuccess() {
        val connector = ConnectionInitConnector(mockApi, mockCallback)
        connector.postConnectionData(baseUrl = "https://localhost", publicKey = "key", pushToken = "pushToken")

        verify { mockCall.enqueue(connector) }

        connector.onResponse(mockCall, Response.success(
                        CreateConnectionResponseData(
                                AuthenticateConnectionData(connectionId = "333", authenticateUrl = "url"))))

        verify { mockCallback.onConnectionInitSuccess(AuthenticateConnectionData(connectionId = "333", authenticateUrl = "url")) }
        confirmVerified(mockCallback)
    }

    @Test
    @Throws(Exception::class)
    fun postConnectionDataTest_emptyResponse() {
        val connector = ConnectionInitConnector(mockApi, mockCallback)
        connector.postConnectionData(baseUrl = "https://localhost", publicKey = "key", pushToken = "pushToken")

        verify { mockCall.enqueue(connector) }

        connector.onResponse(mockCall, Response.success(CreateConnectionResponseData()))

        verify { mockCallback.onConnectionInitFailure(createInvalidResponseError()) }
        confirmVerified(mockCallback)
    }

    @Test
    @Throws(Exception::class)
    fun postConnectionDataTest_withError() {
        val connector = ConnectionInitConnector(mockApi, mockCallback)
        connector.postConnectionData(baseUrl = "https://localhost", publicKey = "key", pushToken = "pushToken")

        verify { mockApi.postNewConnectionData(requestUrl = requestUrl, body = requestData) }
        verify { mockCall.enqueue(connector) }

        connector.onResponse(mockCall, Response.error(404, ResponseBody.create(null, get404Response())))

        verify { mockCallback.onConnectionInitFailure(ApiErrorData(errorMessage = "Resource not found", errorClassName="NotFound")) }
        confirmVerified(mockCallback)
    }

    private val mockApi: ApiInterface = mockkClass(ApiInterface::class)
    private val mockCallback = mockkClass(ConnectionInitResult::class)
    private val mockCall: Call<CreateConnectionResponseData> = mockkClass(Call::class) as Call<CreateConnectionResponseData>
    private val requestData = CreateConnectionRequestData(data = CreateConnectionData(publicKey = "key", pushToken = "pushToken"))
    private val requestUrl = "https://localhost/api/authenticator/v1/connections"

    @Before
    @Throws(Exception::class)
    fun setUp() {
        every {
            mockApi.postNewConnectionData(requestUrl = requestUrl, body = any())
        } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
        every { mockCall.request() } returns Request.Builder().url(requestUrl).build()
        every { mockCallback.onConnectionInitSuccess(any()) } returns Unit
        every { mockCallback.onConnectionInitFailure(any()) } returns Unit
    }
}
