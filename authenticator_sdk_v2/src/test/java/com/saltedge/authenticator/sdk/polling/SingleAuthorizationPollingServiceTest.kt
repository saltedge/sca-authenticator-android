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
package com.saltedge.authenticator.sdk.polling

import com.saltedge.android.test_tools.CommonTestTools
import com.saltedge.android.test_tools.TestConnection
import com.saltedge.authenticator.sdk.contract.FetchAuthorizationContract
import com.saltedge.authenticator.sdk.api.model.connection.ConnectionAbs
import com.saltedge.authenticator.sdk.api.model.connection.ConnectionAndKey
import com.saltedge.authenticator.sdk.api.model.response.AuthorizationShowResponse
import com.saltedge.authenticator.sdk.api.ApiInterface
import com.saltedge.authenticator.sdk.api.RestClient
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.verify
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Call
import java.security.PrivateKey

@RunWith(RobolectricTestRunner::class)
class SingleAuthorizationPollingServiceTest {

    @Test
    @Throws(Exception::class)
    fun valuesTest() {
        val service = SingleAuthorizationPollingService()
        Assert.assertNull(service.contract)

        service.contract = mockContract

        Assert.assertNotNull(service.contract)
    }

    @Test
    @Throws(Exception::class)
    fun startTest() {
        val service = SingleAuthorizationPollingService()
        service.contract = mockContract
        every { mockContract.getConnectionDataForAuthorizationPolling() } returns ConnectionAndKey(
            requestConnection,
            privateKey
        )
        service.start(authorizationId = "1")

        Assert.assertTrue(service.isRunning())
        Assert.assertNotNull(service.connector)
    }

    @Test
    @Throws(Exception::class)
    fun forcedFetchTest() {
        val service = SingleAuthorizationPollingService()
        service.contract = mockContract
        every { mockContract.getConnectionDataForAuthorizationPolling() } returns ConnectionAndKey(
            requestConnection,
            privateKey
        )
        service.forcedFetch()

        verify(atLeast = 1) { mockContract.getConnectionDataForAuthorizationPolling() }
    }

    @Test
    @Throws(Exception::class)
    fun stopTest() {
        val service = SingleAuthorizationPollingService()
        service.contract = mockContract
        every { mockContract.getConnectionDataForAuthorizationPolling() } returns ConnectionAndKey(
            requestConnection,
            privateKey
        )
        service.stop()
        service.forcedFetch()

        verify(atLeast = 0) { mockContract.getConnectionDataForAuthorizationPolling() }

        service.start()
        service.stop()
        service.forcedFetch()

        verify(atLeast = 0) { mockContract.getConnectionDataForAuthorizationPolling() }
    }

    private var privateKey: PrivateKey = CommonTestTools.testPrivateKey
    private val mockApi: ApiInterface = mockkClass(ApiInterface::class)
    private val mockContract: FetchAuthorizationContract =
        mockkClass(FetchAuthorizationContract::class)
    private val mockCall = mockkClass(Call::class) as Call<AuthorizationShowResponse>
    private val requestConnection: ConnectionAbs =
        TestConnection(id = "333", guid = "test", connectUrl = "/", accessToken = "accessToken")

    @Before
    @Throws(Exception::class)
    fun setUp() {
        RestClient.apiInterface = mockApi
        every { mockApi.getAuthorization(requestUrl = any(), headersMap = any()) } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
    }
}
