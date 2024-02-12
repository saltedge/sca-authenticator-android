/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.settings.list

import com.saltedge.authenticator.core.model.ConnectionStatus
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.v2.ScaServiceClientAbs
import com.saltedge.authenticator.sdk.v2.api.API_V2_VERSION
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import java.security.PrivateKey

@RunWith(RobolectricTestRunner::class)
class SettingsListInteractorV2Test {

    private lateinit var interactor: SettingsListInteractorV2

    private val mockConnectionsRepository = Mockito.mock(ConnectionsRepositoryAbs::class.java)
    private val mockKeyStoreManager = Mockito.mock(KeyManagerAbs::class.java)
    private val mockApiManagerV2 = Mockito.mock(ScaServiceClientAbs::class.java)
    private val mockPrivateKey = Mockito.mock(PrivateKey::class.java)

    private val connection = Connection().apply {
        guid = "guid1"
        id = "1"
        code = "demobank3"
        name = "Demobank3"
        status = "${ConnectionStatus.ACTIVE}"
        accessToken = "token1"
        logoUrl = "url"
        createdAt = 200L
        updatedAt = 200L
        apiVersion = API_V2_VERSION
    }

    @Before
    fun setUp() {
        Mockito.doReturn(connection).`when`(mockConnectionsRepository).getById("1")

        interactor = SettingsListInteractorV2(
            apiManager = mockApiManagerV2,
            connectionsRepository = mockConnectionsRepository,
            keyStoreManager = mockKeyStoreManager,
        )
    }

    @Test
    @Throws(Exception::class)
    fun deleteAllConnectionsAndKeysTest() {
        Mockito.doReturn(listOf(connection)).`when`(mockConnectionsRepository).getAllActiveConnections()

        interactor.deleteAllConnectionsAndKeys()

        Mockito.verify(mockKeyStoreManager).deleteKeyPairsIfExist(
            mockConnectionsRepository.getAllConnections().map { it.guid }
        )
        Mockito.verify(mockConnectionsRepository).deleteAllConnections()
    }

    @Test
    @Throws(Exception::class)
    fun sendRevokeRequestForConnectionsTest() {
        val mockConnectionAndKeyV2 = RichConnection(connection, mockPrivateKey)
        Mockito.doReturn(mockConnectionAndKeyV2).`when`(mockKeyStoreManager).enrichConnection(connection)
        BDDMockito.given(mockConnectionsRepository.getAllActiveConnections()).willReturn(listOf(connection))

        interactor.sendRevokeRequestForConnections()

        Mockito.verify(mockApiManagerV2).revokeConnections(
            richConnections = listOf(mockConnectionAndKeyV2),
            callback = null
        )
    }
}
