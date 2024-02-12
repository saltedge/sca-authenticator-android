/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.polling

import com.saltedge.android.test_tools.CommonTestTools
import com.saltedge.android.test_tools.TestConnection
import com.saltedge.authenticator.core.model.ConnectionAbs
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.sdk.v2.api.model.authorization.AuthorizationResponse
import com.saltedge.authenticator.sdk.v2.api.retrofit.ApiInterface
import com.saltedge.authenticator.sdk.v2.api.retrofit.RestClient
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
import java.security.PublicKey

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
        every {
            mockContract.getConnectionDataForAuthorizationPolling()
        } returns RichConnection(requestConnection, privateKey, publicKey)
        service.start(authorizationID = "1")

        Assert.assertTrue(service.isRunning())
        Assert.assertNotNull(service.connector)
    }

    @Test
    @Throws(Exception::class)
    fun forcedFetchTest() {
        val service = SingleAuthorizationPollingService()
        service.contract = mockContract
        every {
            mockContract.getConnectionDataForAuthorizationPolling()
        } returns RichConnection(requestConnection, privateKey, publicKey)
        service.forcedFetch()

        verify(atLeast = 1) { mockContract.getConnectionDataForAuthorizationPolling() }
    }

    @Test
    @Throws(Exception::class)
    fun stopTest() {
        val service = SingleAuthorizationPollingService()
        service.contract = mockContract
        every {
            mockContract.getConnectionDataForAuthorizationPolling()
        } returns RichConnection(requestConnection, privateKey, publicKey)
        service.stop()
        service.forcedFetch()

        verify(atLeast = 0) { mockContract.getConnectionDataForAuthorizationPolling() }

        service.start()
        service.stop()
        service.forcedFetch()

        verify(atLeast = 0) { mockContract.getConnectionDataForAuthorizationPolling() }
    }

    private var privateKey: PrivateKey = CommonTestTools.testPrivateKey
    private var publicKey: PublicKey = CommonTestTools.testPublicKey
    private val mockApi: ApiInterface = mockkClass(ApiInterface::class)
    private val mockContract: PollingAuthorizationContract =
        mockkClass(PollingAuthorizationContract::class)
    private val mockCall = mockkClass(Call::class) as Call<AuthorizationResponse>
    private val requestConnection: ConnectionAbs =
        TestConnection(id = "333", guid = "test", connectUrl = "/", accessToken = "accessToken")

    @Before
    @Throws(Exception::class)
    fun setUp() {
        RestClient.apiInterface = mockApi
        every { mockApi.showAuthorization(requestUrl = any(), headersMap = any()) } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
    }
}
