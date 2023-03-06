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
import com.saltedge.authenticator.core.api.ERROR_CLASS_API_RESPONSE
import com.saltedge.authenticator.core.api.ERROR_CLASS_HOST_UNREACHABLE
import com.saltedge.authenticator.core.api.HEADER_KEY_ACCESS_TOKEN
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.contract.ConsentRevokeListener
import com.saltedge.authenticator.core.model.ConnectionAbs
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.sdk.v2.api.model.consent.ConsentRevokeResponse
import com.saltedge.authenticator.sdk.v2.api.model.consent.ConsentRevokeResponseData
import com.saltedge.authenticator.sdk.v2.api.retrofit.ApiInterface
import com.saltedge.authenticator.sdk.v2.defaultTestConnection
import com.saltedge.authenticator.sdk.v2.get404Response
import okhttp3.Request
import okhttp3.ResponseBody
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import retrofit2.Call
import retrofit2.Response
import java.net.ConnectException
import java.security.PrivateKey
import java.security.PublicKey

@RunWith(RobolectricTestRunner::class)
class ConsentRevokeConnectorTest {

    private val mockApi = mock<ApiInterface>()
    private val mockCallback = mock<ConsentRevokeListener>()
    private val mockCall = Mockito.mock(Call::class.java) as Call<ConsentRevokeResponse>
    private val requestConnection: ConnectionAbs = defaultTestConnection
    private val privateKey: PrivateKey = CommonTestTools.testPrivateKey
    private val publicKey: PublicKey = CommonTestTools.testPublicKey
    private val testRichConnection = RichConnection(requestConnection, privateKey, publicKey)
    private val testConsentID = "1"
    private val requestUrl = "https://localhost/api/authenticator/v2/consents/$testConsentID/revoke"
    private val request = Request.Builder().url(requestUrl).addHeader(HEADER_KEY_ACCESS_TOKEN, "accessToken").build()
    private lateinit var connector: ConsentRevokeConnector

    @Before
    @Throws(Exception::class)
    fun setUp() {
        BDDMockito.given(
            mockApi.revokeConsent(
                requestUrl = anyString(),
                headersMap = anyMap(),
                requestBody = any()
            )
        ).willReturn(mockCall)
        BDDMockito.given(mockCall.request()).willReturn(request)
        connector = ConsentRevokeConnector(mockApi, mockCallback)
    }

    @Test
    @Throws(Exception::class)
    fun valuesTest() {
        connector.callback = null

        Assert.assertNull(connector.callback)

        connector.callback = mockCallback

        Assert.assertNotNull(connector.callback)
    }

    @Test
    @Throws(Exception::class)
    fun revokeTokensForTest_allSuccess() {
        connector.revokeConsent(consentID = testConsentID, richConnection = testRichConnection)
        connector.onResponse(
            mockCall,
            Response.success(ConsentRevokeResponse(ConsentRevokeResponseData(consentId = testConsentID)))
        )

        Mockito.verify(mockCallback).onConsentRevokeSuccess(consentID = testConsentID)
        Mockito.verifyNoMoreInteractions(mockCallback)
    }

    @Test
    @Throws(Exception::class)
    fun revokeTokensForTest_withError404() {
        connector.revokeConsent(consentID = testConsentID, richConnection = testRichConnection)
        connector.onResponse(mockCall, get404Response())

        Mockito.verify(mockCallback).onConsentRevokeFailure(
            error = ApiErrorData(
                errorMessage = "Resource not found",
                errorClassName = "NotFound",
                accessToken = "accessToken"
            )
        )
        Mockito.verifyNoMoreInteractions(mockCallback)
    }

    @Test
    @Throws(Exception::class)
    fun revokeTokensForTest_successWithoutResponseBody() {
        connector.onResponse(mockCall, Response.success(null))

        Mockito.verify(mockCallback).onConsentRevokeFailure(
            error = ApiErrorData(
                errorMessage = "Request Error (200)",
                errorClassName = ERROR_CLASS_API_RESPONSE,
                accessToken = "accessToken"
            )
        )
        Mockito.verifyNoMoreInteractions(mockCallback)
    }

    @Test
    @Throws(Exception::class)
    fun revokeTokensForTest_withUnknownError() {
        connector.onResponse(
            mockCall,
            Response.error(404, ResponseBody.create(null, "{\"message\":\"Unknown error\"}"))
        )

        Mockito.verify(mockCallback).onConsentRevokeFailure(
            error = ApiErrorData(
                errorMessage = "Request Error (404)",
                errorClassName = ERROR_CLASS_API_RESPONSE,
                accessToken = "accessToken"
            )
        )
        Mockito.verifyNoMoreInteractions(mockCallback)
    }

    @Test
    @Throws(Exception::class)
    fun revokeTokensForTest_withException() {
        connector.onFailure(mockCall, ConnectException())

        Mockito.verify(mockCallback).onConsentRevokeFailure(
            error = ApiErrorData(
                errorClassName = ERROR_CLASS_HOST_UNREACHABLE,
                accessToken = "accessToken"
            )
        )
        Mockito.verifyNoMoreInteractions(mockCallback)
    }
}
