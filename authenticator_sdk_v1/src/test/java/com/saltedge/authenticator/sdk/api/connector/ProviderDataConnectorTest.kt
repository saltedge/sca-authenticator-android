/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.api.connector

import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.contract.FetchProviderConfigurationListener

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
            mockCallback.onFetchProviderConfigurationSuccess(
                result = ProviderConfigurationData(
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
            mockCallback.onFetchProviderConfigurationFailure(
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
        every { mockCallback.onFetchProviderConfigurationSuccess(any()) } returns Unit
        every { mockCallback.onFetchProviderConfigurationFailure(any()) } returns Unit
    }
}
