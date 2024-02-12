/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.connector

import com.saltedge.authenticator.sdk.v2.api.contract.FetchConfigurationListener
import com.saltedge.authenticator.sdk.v2.api.model.configuration.ConfigurationDataV2
import com.saltedge.authenticator.sdk.v2.api.model.configuration.ConfigurationResponse
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.v2.api.retrofit.ApiInterface
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
class ConfigurationConnectorTest {

    @Test
    @Throws(Exception::class)
    fun valuesTest() {
        val connector = ConfigurationConnector(mockApi, null)

        Assert.assertNull(connector.callback)

        connector.callback = mockCallback

        Assert.assertNotNull(connector.callback)
    }

    @Test
    @Throws(Exception::class)
    fun fetchProviderDataTest_allSuccess() {
        val connector = ConfigurationConnector(mockApi, mockCallback)
        connector.fetchProviderConfiguration(requestUrl)

        verify { mockCall.enqueue(connector) }

        connector.onResponse(
            mockCall,
            Response.success(ConfigurationResponse(ConfigurationDataV2(
                scaServiceUrl = "connectUrl",
                providerName = "name",
                providerId = "code",
                providerLogoUrl = "url",
                apiVersion = "1",
                providerSupportEmail = "example@example.com",
                providerPublicKey = "-----BEGIN PUBLIC KEY-----",
                geolocationRequired = true
            )))
        )

        verify {
            mockCallback.onFetchProviderConfigurationSuccess(
                result = ConfigurationDataV2(
                    scaServiceUrl = "connectUrl",
                    providerName = "name",
                    providerId = "code",
                    providerLogoUrl = "url",
                    apiVersion = "1",
                    providerSupportEmail = "example@example.com",
                    providerPublicKey = "-----BEGIN PUBLIC KEY-----",
                    geolocationRequired = true
                )
            )
        }
        confirmVerified(mockCallback)
    }

    @Test
    @Throws(Exception::class)
    fun fetchProviderDataTest_withError() {
        val connector = ConfigurationConnector(mockApi, mockCallback)
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

    private val requestUrl = "https://localhost/api/authenticator/v2/authorizations/authId"
    private val mockApi: ApiInterface = mockkClass(ApiInterface::class)
    private val mockCallback = mockkClass(FetchConfigurationListener::class)
    private val mockCall = mockkClass(Call::class) as Call<ConfigurationResponse>

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
