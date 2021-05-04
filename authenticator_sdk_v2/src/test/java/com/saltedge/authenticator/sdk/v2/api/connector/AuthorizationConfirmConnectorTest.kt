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
import com.saltedge.authenticator.sdk.v2.api.contract.AuthorizationConfirmListener
import com.saltedge.authenticator.sdk.v2.api.model.EncryptedBundle
import com.saltedge.authenticator.sdk.v2.api.model.authorization.ConfirmDenyResponse
import com.saltedge.authenticator.sdk.v2.api.model.authorization.ConfirmDenyResponseData
import com.saltedge.authenticator.sdk.v2.api.model.connection.ConnectionV2Abs
import com.saltedge.authenticator.sdk.v2.api.model.connection.RichConnection
import com.saltedge.authenticator.sdk.v2.api.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.v2.api.retrofit.ApiInterface
import com.saltedge.authenticator.sdk.v2.api.retrofit.HEADER_KEY_ACCESS_TOKEN
import com.saltedge.authenticator.sdk.v2.defaultTestConnection
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
import java.security.PrivateKey
import java.security.PublicKey

@RunWith(RobolectricTestRunner::class)
class AuthorizationConfirmConnectorTest {

    @Test
    @Throws(Exception::class)
    fun valuesTest() {
        val connector = AuthorizationConfirmConnector(mockApi, requestAuthorizationId,null)

        Assert.assertNull(connector.callback)

        connector.callback = mockCallback

        Assert.assertNotNull(connector.callback)
    }

    @Test
    @Throws(Exception::class)
    fun postConfirmTest_allSuccess() {
        val connector = AuthorizationConfirmConnector(mockApi, requestAuthorizationId, mockCallback)
        connector.confirmAuthorization(
            connection = RichConnection(requestConnection, privateKey, publicKey),
            encryptedPayload = EncryptedBundle(
                encryptedAesKey = "o3TDCc3rKYTx...RVH+aOFpS9NIg==\n",
                encryptedAesIv = "BtV7EB3Erv8xEQ.../jeBRyFa75A6po5XlwWiEiuzQ==\n",
                encryptedData = "YlnrNOHvUIPem/O58rMzdsvkXidLvgGpdMalD9c1mlg=\n"
            )
        )

        verify { mockCall.enqueue(connector) }

        connector.onResponse(
            mockCall,
            Response.success(ConfirmDenyResponse(ConfirmDenyResponseData(
                status = "processing",
                authorizationID = requestAuthorizationId
            )))
        )

        verify {
            mockCallback.onAuthorizationConfirmSuccess(ConfirmDenyResponseData(
                status = "processing",
                authorizationID = requestAuthorizationId
            ))
        }
        confirmVerified(mockCallback)
    }

    @Test
    @Throws(Exception::class)
    fun postConfirmTest_withError() {
        val connector = AuthorizationConfirmConnector(mockApi, requestAuthorizationId, mockCallback)
        connector.confirmAuthorization(
            connection = RichConnection(requestConnection, privateKey, publicKey),
            encryptedPayload = EncryptedBundle(
                encryptedAesKey = "o3TDCc3rKYTx...RVH+aOFpS9NIg==\n",
                encryptedAesIv = "BtV7EB3Erv8xEQ.../jeBRyFa75A6po5XlwWiEiuzQ==\n",
                encryptedData = "YlnrNOHvUIPem/O58rMzdsvkXidLvgGpdMalD9c1mlg=\n"
            )
        )

        verify { mockCall.enqueue(connector) }

        connector.onResponse(mockCall, get404Response())

        verify {
            mockCallback.onAuthorizationConfirmFailure(
                ApiErrorData(
                    errorMessage = "Resource not found",
                    errorClassName = "NotFound",
                    accessToken = "accessToken"
                ),
                authorizationID = requestAuthorizationId
            )
        }
        confirmVerified(mockCallback)
    }

    private val mockApi: ApiInterface = mockkClass(ApiInterface::class)
    private val mockCallback = mockkClass(AuthorizationConfirmListener::class)
    private val mockCall = mockkClass(Call::class) as Call<ConfirmDenyResponse>
    private val requestConnection: ConnectionV2Abs = defaultTestConnection
    private val requestAuthorizationId = "444"
    private var privateKey: PrivateKey = CommonTestTools.testPrivateKey
    private var publicKey: PublicKey = CommonTestTools.testPublicKey
    private val requestUrl = "https://localhost/api/authenticator/v2/authorizations/$requestAuthorizationId/confirm"

    @Before
    @Throws(Exception::class)
    fun setUp() {
        every {
            mockApi.confirmAuthorization(
                requestUrl = requestUrl,
                headersMap = any(),
                requestBody = any()
            )
        } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
        every { mockCall.request() } returns Request.Builder().url(requestUrl)
            .addHeader(HEADER_KEY_ACCESS_TOKEN, "accessToken").build()
        every {
            mockCallback.onAuthorizationConfirmFailure(error = any(), authorizationID = requestAuthorizationId)
        } returns Unit
        every { mockCallback.onAuthorizationConfirmSuccess(result = any()) } returns Unit
    }
}
