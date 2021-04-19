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

import com.saltedge.authenticator.sdk.contract.FetchProviderConfigurationListener
import com.saltedge.authenticator.sdk.api.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.api.model.configuration.ProviderConfigurationData
import com.saltedge.authenticator.sdk.api.model.response.ProviderConfigurationResponse
import com.saltedge.authenticator.sdk.api.ApiInterface
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
        val connector = ProviderConfigurationConnector(mockApi, null)

        Assert.assertNull(connector.resultCallback)

        connector.resultCallback = mockCallback

        Assert.assertNotNull(connector.resultCallback)
    }

    @Test
    @Throws(Exception::class)
    fun fetchProviderDataTest_allSuccess() {
        val connector = ProviderConfigurationConnector(mockApi, mockCallback)
        connector.fetchProviderConfiguration(requestUrl)

        verify { mockCall.enqueue(connector) }

        connector.onResponse(
            mockCall,
            Response.success(
                ProviderConfigurationResponse(
                    ProviderConfigurationData(
                        connectUrl = "connectUrl",
                        name = "name",
                        code = "code",
                        logoUrl = "url",
                        version = "1",
                        supportEmail = "example@example.com",
                        consentManagementSupported = true,
                        geolocationRequired = true
                    )
                )
            )
        )

        verify {
            mockCallback.fetchProviderConfigurationDataResult(
                result = ProviderConfigurationData(
                    connectUrl = "connectUrl",
                    name = "name",
                    code = "code",
                    logoUrl = "url",
                    version = "1",
                    supportEmail = "example@example.com",
                    consentManagementSupported = true,
                    geolocationRequired = true
                ),
                error = null
            )
        }
        confirmVerified(mockCallback)
    }

    @Test
    @Throws(Exception::class)
    fun fetchProviderDataTest_withError() {
        val connector = ProviderConfigurationConnector(mockApi, mockCallback)
        connector.fetchProviderConfiguration(requestUrl)

        verify(exactly = 1) {
            mockApi.getProviderConfiguration(requestUrl = requestUrl)
        }
        verify { mockCall.enqueue(connector) }

        connector.onResponse(mockCall, get404Response())

        verify {
            mockCallback.fetchProviderConfigurationDataResult(
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
    private val mockCallback = mockkClass(FetchProviderConfigurationListener::class)
    private val mockCall: Call<ProviderConfigurationResponse> =
        mockkClass(Call::class) as Call<ProviderConfigurationResponse>

    @Before
    @Throws(Exception::class)
    fun setUp() {
        every {
            mockApi.getProviderConfiguration(requestUrl = requestUrl)
        } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
        every { mockCall.request() } returns Request.Builder().url(requestUrl).build()
        every { mockCallback.fetchProviderConfigurationDataResult(any(), any()) } returns Unit
    }
}
