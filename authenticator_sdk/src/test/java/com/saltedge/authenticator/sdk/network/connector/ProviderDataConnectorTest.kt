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

import com.saltedge.authenticator.sdk.contract.FetchProviderDataResult
import com.saltedge.authenticator.sdk.model.ApiErrorData
import com.saltedge.authenticator.sdk.model.ProviderData
import com.saltedge.authenticator.sdk.model.ProviderResponseData
import com.saltedge.authenticator.sdk.network.ApiInterface
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

@RunWith(RobolectricTestRunner::class)
class ProviderDataConnectorTest {

    @Test
    @Throws(Exception::class)
    fun valuesTest() {
        val connector = ProviderDataConnector(mockApi, null)

        Assert.assertNull(connector.resultCallback)

        connector.resultCallback = mockCallback

        Assert.assertNotNull(connector.resultCallback)
    }

    @Test
    @Throws(Exception::class)
    fun fetchProviderDataTest_allSuccess() {
        val connector = ProviderDataConnector(mockApi, mockCallback)
        connector.fetchProviderData(requestUrl)

        verify { mockCall.enqueue(connector) }

        connector.onResponse(
            mockCall,
            Response.success(
                ProviderResponseData(
                    ProviderData(
                        connectUrl = "connectUrl",
                        name = "name",
                        code = "code",
                        logoUrl = "url",
                        version = "1",
                        supportEmail = "example@example.com"
                    )
                )
            )
        )

        verify {
            mockCallback.fetchProviderResult(
                result = ProviderData(
                    connectUrl = "connectUrl",
                    name = "name",
                    code = "code",
                    logoUrl = "url",
                    version = "1",
                    supportEmail = "example@example.com"
                ),
                error = null
            )
        }
        confirmVerified(mockCallback)
    }

    @Test
    @Throws(Exception::class)
    fun fetchProviderDataTest_withError() {
        val connector = ProviderDataConnector(mockApi, mockCallback)
        connector.fetchProviderData(requestUrl)

        verify(exactly = 1) {
            mockApi.getProviderData(requestUrl = requestUrl)
        }
        verify { mockCall.enqueue(connector) }

        connector.onResponse(mockCall, get404Response())

        verify {
            mockCallback.fetchProviderResult(
                result = null,
                error = ApiErrorData(
                    errorMessage = "Resource not found",
                    errorClassName = "NotFound"
                )
            )
        }
        confirmVerified(mockCallback)
    }

    private val requestUrl = "https://localhost/api/authenticator/v1/authorizations/authId"
    private val mockApi: ApiInterface = mockkClass(ApiInterface::class)
    private val mockCallback = mockkClass(FetchProviderDataResult::class)
    private val mockCall: Call<ProviderResponseData> =
        mockkClass(Call::class) as Call<ProviderResponseData>

    @Before
    @Throws(Exception::class)
    fun setUp() {
        every {
            mockApi.getProviderData(requestUrl = requestUrl)
        } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
        every { mockCall.request() } returns Request.Builder().url(requestUrl).build()
        every { mockCallback.fetchProviderResult(any(), any()) } returns Unit
    }
}
