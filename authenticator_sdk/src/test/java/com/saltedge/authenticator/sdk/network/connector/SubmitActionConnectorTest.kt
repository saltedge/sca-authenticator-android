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

import com.saltedge.authenticator.sdk.contract.ActionSubmitListener
import com.saltedge.authenticator.sdk.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.model.connection.ConnectionAbs
import com.saltedge.authenticator.sdk.model.connection.ConnectionAndKey
import com.saltedge.authenticator.sdk.model.error.createInvalidResponseError
import com.saltedge.authenticator.sdk.model.response.SubmitActionResponseData
import com.saltedge.authenticator.sdk.model.response.SubmitActionResponse
import com.saltedge.authenticator.sdk.network.ApiInterface
import com.saltedge.authenticator.sdk.testTools.get404Response
import com.saltedge.authenticator.sdk.testTools.getDefaultTestConnection
import com.saltedge.authenticator.sdk.testTools.getTestPrivateKey
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

@RunWith(RobolectricTestRunner::class)
class SubmitActionConnectorTest {

    private val mockApi: ApiInterface = mockkClass(ApiInterface::class)
    private val mockCallback = mockkClass(ActionSubmitListener::class)
    private val mockCall: Call<SubmitActionResponse> =
        mockkClass(Call::class) as Call<SubmitActionResponse>
    private val requestUrl = "https://localhost/api/authenticator/v1/actions/uuid-1234"
    private val requestConnection: ConnectionAbs = getDefaultTestConnection()
    private var privateKey: PrivateKey = this.getTestPrivateKey()


    @Before
    @Throws(Exception::class)
    fun setUp() {
        every {
            mockApi.updateAction(requestUrl = requestUrl, headersMap = any())
        } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
        every { mockCall.request() } returns Request.Builder().url(requestUrl).build()
        every { mockCallback.onActionInitSuccess(any()) } returns Unit
        every { mockCallback.onActionInitFailure(any()) } returns Unit
    }

    @Test
    @Throws(Exception::class)
    fun valuesTest() {
        val connector = SubmitActionConnector(mockApi, null)

        Assert.assertNull(connector.resultCallback)

        connector.resultCallback = mockCallback

        Assert.assertNotNull(connector.resultCallback)
    }

    @Test
    @Throws(Exception::class)
    fun updateActionTest_emptyResponse() {
        val connector = SubmitActionConnector(mockApi, mockCallback)
        connector.updateAction(
            actionUUID = "uuid-1234",
            connectionAndKey = ConnectionAndKey(requestConnection, privateKey)
        )

        verify { mockCall.enqueue(connector) }

        connector.onResponse(mockCall, Response.success(SubmitActionResponse()))

        verify { mockCallback.onActionInitFailure(createInvalidResponseError()) }
        confirmVerified(mockCallback)
    }

    @Test
    @Throws(Exception::class)
    fun postConnectionDataTest_withError() {
        val connector = SubmitActionConnector(mockApi, mockCallback)
        connector.updateAction(
            actionUUID = "uuid-1234",
            connectionAndKey = ConnectionAndKey(requestConnection, privateKey)
        )

        verify { mockApi.updateAction(requestUrl = requestUrl, headersMap = any()) }
        verify { mockCall.enqueue(connector) }

        connector.onResponse(mockCall, get404Response())

        verify {
            mockCallback.onActionInitFailure(
                ApiErrorData(
                    errorMessage = "Resource not found",
                    errorClassName = "NotFound"
                )
            )
        }
        confirmVerified(mockCallback)
    }

    @Test
    @Throws(Exception::class)
    fun postConnectionDataTest_allSuccess() {
        val connector = SubmitActionConnector(mockApi, mockCallback)
        connector.updateAction(
            actionUUID = "uuid-1234",
            connectionAndKey = ConnectionAndKey(requestConnection, privateKey)
        )

        verify { mockCall.enqueue(connector) }

        connector.onResponse(
            mockCall, Response.success(
            SubmitActionResponse(
                SubmitActionResponseData(
                    success = true,
                    connectionId = "connectionId",
                    authorizationId = "authorizationId"
                )
            )
        )
        )

        verify {
            mockCallback.onActionInitSuccess(
                SubmitActionResponseData(
                    success = true,
                    connectionId = "connectionId",
                    authorizationId = "authorizationId"
                )
            )
        }
        confirmVerified(mockCallback)
    }
}
