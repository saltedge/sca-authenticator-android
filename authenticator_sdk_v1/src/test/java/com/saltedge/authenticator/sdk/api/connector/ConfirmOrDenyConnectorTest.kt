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
import com.saltedge.authenticator.sdk.contract.ConfirmAuthorizationListener
import com.saltedge.authenticator.sdk.api.model.connection.ConnectionAndKey

import com.saltedge.authenticator.sdk.api.model.error.createInvalidResponseError
import com.saltedge.authenticator.sdk.api.model.request.ConfirmDenyRequestData
import com.saltedge.authenticator.sdk.api.model.response.ConfirmDenyResponse
import com.saltedge.authenticator.sdk.api.model.response.ConfirmDenyResponseData
import com.saltedge.authenticator.sdk.api.ApiInterface
import com.saltedge.authenticator.sdk.api.HEADER_KEY_ACCESS_TOKEN
import com.saltedge.authenticator.sdk.testTools.get404Response
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
class ConfirmOrDenyConnectorTest {

    @Test
    @Throws(Exception::class)
    fun valuesTest() {
        val connector = ConfirmOrDenyConnector(mockApi, null)

        Assert.assertNull(connector.resultCallback)

        connector.resultCallback = mockCallback

        Assert.assertNotNull(connector.resultCallback)
    }

    @Test
    @Throws(Exception::class)
    fun postConfirmOrDenyTest_allSuccess() {
        val connector = ConfirmOrDenyConnector(mockApi, mockCallback)
        connector.updateAuthorization(
            connectionAndKey = ConnectionAndKey(requestConnection, privateKey),
            authorizationId = requestAuthorizationId,
            payloadData = ConfirmDenyRequestData(confirm = true, authorizationCode = "authorizationCode"),
            geolocationHeader = "GEO:52.506931;13.144558",
            authorizationTypeHeader = "biometrics"
        )

        verify { mockCall.enqueue(connector) }

        connector.onResponse(
            mockCall,
            Response.success(
                ConfirmDenyResponse(
                    ConfirmDenyResponseData(
                        success = true,
                        authorizationID = requestAuthorizationId
                    )
                )
            )
        )

        verify {
            mockCallback.onConfirmDenySuccess(
                ConfirmDenyResponseData(
                    success = true,
                    authorizationID = requestAuthorizationId
                ),
                connectionID = "333"
            )
        }
        confirmVerified(mockCallback)
    }

    @Test
    @Throws(Exception::class)
    fun postConfirmOrDenyTest_emptyResponse() {
        val connector = ConfirmOrDenyConnector(mockApi, mockCallback)
        connector.updateAuthorization(
            connectionAndKey = ConnectionAndKey(requestConnection, privateKey),
            authorizationId = requestAuthorizationId,
            payloadData = ConfirmDenyRequestData(confirm = true, authorizationCode = "authorizationCode"),
            geolocationHeader = "GEO:52.506931;13.144558",
            authorizationTypeHeader = "biometrics"
        )

        verify { mockCall.enqueue(connector) }

        connector.onResponse(mockCall, Response.success(ConfirmDenyResponse()))

        verify { mockCallback.onConfirmDenyFailure(createInvalidResponseError(), authorizationID = "444", connectionID = "333") }
        confirmVerified(mockCallback)
    }

    @Test
    @Throws(Exception::class)
    fun postConfirmOrDenyTest_withError() {
        val connector = ConfirmOrDenyConnector(mockApi, mockCallback)
        connector.updateAuthorization(
            connectionAndKey = ConnectionAndKey(requestConnection, privateKey),
            authorizationId = requestAuthorizationId,
            payloadData = ConfirmDenyRequestData(confirm = true, authorizationCode = "authorizationCode"),
            geolocationHeader = "GEO:52.506931;13.144558",
            authorizationTypeHeader = "biometrics"
        )

        verify { mockCall.enqueue(connector) }

        connector.onResponse(mockCall, get404Response())

        verify {
            mockCallback.onConfirmDenyFailure(
                ApiErrorData(
                    errorMessage = "Resource not found",
                    errorClassName = "NotFound",
                    accessToken = "accessToken"
                ),
                authorizationID = "444",
                connectionID = "333"
            )
        }
        confirmVerified(mockCallback)
    }

    private val mockApi: ApiInterface = mockkClass(ApiInterface::class)
    private val mockCallback: ConfirmAuthorizationListener =
        mockkClass(ConfirmAuthorizationListener::class)
    private val mockCall: Call<ConfirmDenyResponse> =
        mockkClass(Call::class) as Call<ConfirmDenyResponse>
    private val requestConnection: ConnectionAbs = getDefaultTestConnection()
    private val requestAuthorizationId = "444"
    private val requestUrl = "https://localhost/api/authenticator/v1/authorizations/444"
    private var privateKey: PrivateKey = CommonTestTools.testPrivateKey

    @Before
    @Throws(Exception::class)
    fun setUp() {
        every {
            mockApi.updateAuthorization(
                requestUrl = requestUrl,
                headersMap = any(),
                requestBody = any()
            )
        } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
        every { mockCall.request() } returns Request.Builder().url(requestUrl).addHeader(
            HEADER_KEY_ACCESS_TOKEN,
            "accessToken"
        ).build()
        every {
            mockCallback.onConfirmDenyFailure(
                error = any(),
                connectionID = "333",
                authorizationID = "444"
            )
        } returns Unit
        every {
            mockCallback.onConfirmDenySuccess(
                result = any(),
                connectionID = "333"
            )
        } returns Unit
    }
}
