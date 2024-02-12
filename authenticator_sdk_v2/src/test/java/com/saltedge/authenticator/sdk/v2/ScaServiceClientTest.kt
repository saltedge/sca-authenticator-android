/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2

import com.saltedge.android.test_tools.CommonTestTools
import com.saltedge.authenticator.core.api.model.EncryptedBundle
import com.saltedge.authenticator.core.api.model.EncryptedListResponse
import com.saltedge.authenticator.core.contract.ConsentRevokeListener
import com.saltedge.authenticator.core.model.ConnectionAbs
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.sdk.v2.api.contract.*
import com.saltedge.authenticator.sdk.v2.api.model.authorization.*
import com.saltedge.authenticator.sdk.v2.api.model.connection.CreateConnectionResponse
import com.saltedge.authenticator.sdk.v2.api.model.connection.RevokeConnectionResponse
import com.saltedge.authenticator.sdk.v2.api.model.consent.ConsentRevokeResponse
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
class ScaServiceClientTest {

    @Test
    @Throws(Exception::class)
    fun initConnectionRequestTest() {
        val mockCallback = mockkClass(ConnectionCreateListener::class)
        val mockCall = mockkClass(Call::class) as Call<CreateConnectionResponse>
        every { mockApi.createConnection(requestUrl = any(), body = any()) } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
        ScaServiceClient().requestCreateConnection(
            baseUrl = "",
            rsaPublicKeyEncryptedBundle = EncryptedBundle(
                encryptedAesKey = "key",
                encryptedAesIv = "iv",
                encryptedData = "data"
            ),
            providerId = "1",
            pushToken = "pushToken",
            connectQueryParam = "1234567890",
            callback = mockCallback
        )

        verify { mockCall.enqueue(any()) }
    }

    @Test
    @Throws(Exception::class)
    fun revokeConnectionsTest() {
        val mockCallback = mockkClass(ConnectionsV2RevokeListener::class)
        val mockCall = mockkClass(Call::class) as Call<RevokeConnectionResponse>
        every {
            mockApi.revokeConnection(
                requestUrl = any(),
                headersMap = any(),
                requestBody = any()
            )
        } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
        ScaServiceClient().revokeConnections(
            richConnections = listOf(RichConnection(requestConnection, privateKey, publicKey)),
            callback = mockCallback
        )

        verify { mockCall.enqueue(any()) }
    }

    @Test
    @Throws(Exception::class)
    fun createAuthorizationsPollingServiceTest() {
        Assert.assertNotNull(ScaServiceClient().createAuthorizationsPollingService())
    }

    @Test
    @Throws(Exception::class)
    fun createSingleAuthorizationPollingServiceTest() {
        Assert.assertNotNull(ScaServiceClient().createSingleAuthorizationPollingService())
    }

    @Test
    @Throws(Exception::class)
    fun getAuthorizationsTest() {
        val mockCallback = mockkClass(FetchAuthorizationsListener::class)
        val mockCall = mockkClass(Call::class) as Call<AuthorizationsListResponse>
        every { mockApi.activeAuthorizations(requestUrl = any(), headersMap = any()) } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
        ScaServiceClient().fetchAuthorizations(
            richConnections = listOf(RichConnection(requestConnection, privateKey, publicKey)),
            callback = mockCallback
        )

        verify { mockCall.enqueue(any()) }
    }

    @Test
    @Throws(Exception::class)
    fun getAuthorizationTest() {
        val mockCallback = mockkClass(FetchAuthorizationListener::class)
        val mockCall = mockkClass(Call::class) as Call<AuthorizationResponse>
        every { mockApi.showAuthorization(requestUrl = any(), headersMap = any()) } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
        ScaServiceClient().fetchAuthorization(
            richConnection = RichConnection(requestConnection, privateKey, publicKey),
            authorizationID = "444",
            callback = mockCallback
        )

        verify { mockCall.enqueue(any()) }
    }

    @Test
    @Throws(Exception::class)
    fun confirmAuthorizationTest() {
        val mockCallback = mockkClass(AuthorizationConfirmListener::class)
        val mockCall = mockkClass(Call::class) as Call<UpdateAuthorizationResponse>
        every {
            mockApi.confirmAuthorization(
                requestUrl = any(),
                headersMap = any(),
                requestBody = any()
            )
        } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
        ScaServiceClient().confirmAuthorization(
            richConnection = RichConnection(requestConnection, privateKey, publicKey),
            authorizationID = "444",
            authorizationData = UpdateAuthorizationData(
                authorizationCode = "Code123",
                userAuthorizationType = "biometrics",
                geolocation = "GEO:52.506931;13.144558"
            ),
            callback = mockCallback
        )

        verify { mockCall.enqueue(any()) }
    }

    @Test
    @Throws(Exception::class)
    fun denyAuthorizationTest() {
        val mockCallback = mockkClass(AuthorizationDenyListener::class)
        val mockCall = mockkClass(Call::class) as Call<UpdateAuthorizationResponse>
        every {
            mockApi.denyAuthorization(
                requestUrl = any(),
                headersMap = any(),
                requestBody = any()
            )
        } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
        ScaServiceClient().denyAuthorization(
            richConnection = RichConnection(requestConnection, privateKey, publicKey),
            authorizationID = "444",
            authorizationData = UpdateAuthorizationData(
                authorizationCode = "Code123",
                userAuthorizationType = "biometrics",
                geolocation = "GEO:52.506931;13.144558"
            ),
            callback = mockCallback
        )

        verify { mockCall.enqueue(any()) }
    }

    @Test
    @Throws(Exception::class)
    fun createAuthorizationTest() {
        val mockCallback = mockkClass(AuthorizationCreateListener::class)
        val mockCall = mockkClass(Call::class) as Call<CreateAuthorizationResponse>
        every {
            mockApi.createAuthorizationForAction(
                requestUrl = any(),
                headersMap = any(),
                requestBody = any()
            )
        } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
        ScaServiceClient().requestCreateAuthorizationForAction(
            richConnection = RichConnection(requestConnection, privateKey, publicKey),
            actionID = "444",
            callback = mockCallback
        )

        verify { mockCall.enqueue(any()) }
    }

    @Test
    @Throws(Exception::class)
    fun getConsentsTest() {
        val mockCallback = mockkClass(FetchConsentsListener::class)
        val mockCall = mockkClass(Call::class) as Call<EncryptedListResponse>
        every { mockApi.activeConsents(requestUrl = any(), headersMap = any()) } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
        ScaServiceClient().fetchConsents(
            richConnections = listOf(RichConnection(requestConnection, privateKey, publicKey)),
            callback = mockCallback
        )

        verify { mockCall.enqueue(any()) }
    }

    @Test
    @Throws(Exception::class)
    fun revokeConsentTest() {
        val mockCallback = mockkClass(ConsentRevokeListener::class)
        val mockCall = mockkClass(Call::class) as Call<ConsentRevokeResponse>
        every {
            mockApi.revokeConsent(
                requestUrl = any(),
                headersMap = any(),
                requestBody = any()
            )
        } returns mockCall
        every { mockCall.enqueue(any()) } returns Unit
        ScaServiceClient().revokeConsent(
            consentID = "1",
            richConnection = RichConnection(requestConnection, privateKey, publicKey),
            callback = mockCallback
        )

        verify { mockCall.enqueue(any()) }
    }

    private val mockApi: ApiInterface = mockkClass(ApiInterface::class)
    private var privateKey: PrivateKey = CommonTestTools.testPrivateKey
    private var publicKey: PublicKey = CommonTestTools.testPublicKey
    private val requestConnection: ConnectionAbs = defaultTestConnection

    @Before
    @Throws(Exception::class)
    fun setUp() {
        RestClient.apiInterface = mockApi
    }
}
