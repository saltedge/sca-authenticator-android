/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.main

import com.saltedge.authenticator.cloud.PushTokenUpdater
import com.saltedge.authenticator.core.model.ConnectionStatus
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.models.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.constants.API_V1_VERSION
import com.saltedge.authenticator.sdk.v2.ScaServiceClientAbs
import com.saltedge.authenticator.sdk.v2.api.API_V2_VERSION
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import java.security.PrivateKey

@RunWith(RobolectricTestRunner::class)
class MainActivityInteractorTest {

    private lateinit var interactor: MainActivityInteractor

    private val mockConnectionsRepository = Mockito.mock(ConnectionsRepositoryAbs::class.java)
    private val mockKeyStoreManager = Mockito.mock(KeyManagerAbs::class.java)
    private val mockApiManagerV1 = Mockito.mock(AuthenticatorApiManagerAbs::class.java)
    private val mockApiManagerV2 = Mockito.mock(ScaServiceClientAbs::class.java)
    private val mockPreferenceRepository = Mockito.mock(PreferenceRepositoryAbs::class.java)
    private val mockPushTokenUpdater = Mockito.mock(PushTokenUpdater::class.java)
    private val mockPrivateKey = Mockito.mock(PrivateKey::class.java)

    private val connection1 = Connection().apply {
        guid = "guid1"
        id = "1"
        code = "demobank3"
        name = "Demobank3"
        status = "${ConnectionStatus.ACTIVE}"
        accessToken = "token1"
        logoUrl = "url"
        createdAt = 200L
        updatedAt = 200L
        apiVersion = API_V1_VERSION
    }

    private val connection2 = Connection().apply {
        guid = "guid2"
        id = "2_noKey"
        code = "demobank2"
        name = "Demobank2"
        status = "${ConnectionStatus.ACTIVE}"
        accessToken = "token2"
        logoUrl = ""
        createdAt = 300L
        updatedAt = 300L
        apiVersion = API_V2_VERSION
    }

    @Before
    fun setUp() {
        Mockito.doReturn(connection1).`when`(mockConnectionsRepository).getById("1")
        Mockito.doReturn(connection2).`when`(mockConnectionsRepository).getById("2_noKey")

        interactor = MainActivityInteractor(
            apiManagerV1 = mockApiManagerV1,
            apiManagerV2 = mockApiManagerV2,
            connectionsRepository = mockConnectionsRepository,
            keyStoreManager = mockKeyStoreManager,
            preferenceRepository = mockPreferenceRepository,
            pushTokenUpdater = mockPushTokenUpdater
        )
    }

    @Test
    @Throws(Exception::class)
    fun noConnections() {
        Mockito.doReturn(true).`when`(mockConnectionsRepository).isEmpty()

        assertTrue(interactor.noConnections)

        Mockito.doReturn(false).`when`(mockConnectionsRepository).isEmpty()

        assertFalse(interactor.noConnections)
    }

    @Test
    @Throws(Exception::class)
    fun wipeApplicationTest() {
        Mockito.doReturn(listOf(connection1)).`when`(mockConnectionsRepository).getAllActiveConnections()

        interactor.wipeApplication()

        Mockito.verify(mockPreferenceRepository).clearUserPreferences()
        Mockito.verify(mockKeyStoreManager).deleteKeyPairsIfExist(
            mockConnectionsRepository.getAllConnections().map { it.guid })
        Mockito.verify(mockConnectionsRepository).deleteAllConnections()
    }

    @Test
    @Throws(Exception::class)
    fun sendRevokeRequestForConnectionsTestCase1() {
        val mockConnectionAndKeyV1 = RichConnection(connection1, mockPrivateKey)
        Mockito.doReturn(mockConnectionAndKeyV1).`when`(mockKeyStoreManager).enrichConnection(connection1, addProviderKey = false)
        given(mockConnectionsRepository.getAllActiveConnections()).willReturn(listOf(connection1))

        interactor.sendRevokeRequestForConnections()

        Mockito.verify(mockApiManagerV1).revokeConnections(
            connectionsAndKeys = listOf(
                mockConnectionAndKeyV1
            ), resultCallback = null
        )
        Mockito.verify(mockApiManagerV2).revokeConnections(
            richConnections = emptyList(),
            callback = null
        )
    }

    @Test
    @Throws(Exception::class)
    fun sendRevokeRequestForConnectionsTestCase2() {
        val mockConnectionAndKeyV2 = RichConnection(connection2, mockPrivateKey)
        Mockito.doReturn(mockConnectionAndKeyV2).`when`(mockKeyStoreManager).enrichConnection(connection2)
        given(mockConnectionsRepository.getAllActiveConnections()).willReturn(listOf(connection2))

        interactor.sendRevokeRequestForConnections()

        Mockito.verify(mockApiManagerV1).revokeConnections(
            connectionsAndKeys = emptyList(), resultCallback = null
        )
        Mockito.verify(mockApiManagerV2).revokeConnections(
            richConnections = listOf(mockConnectionAndKeyV2),
            callback = null
        )
    }
}
