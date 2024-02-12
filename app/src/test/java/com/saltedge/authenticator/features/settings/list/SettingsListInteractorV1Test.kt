/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.settings.list

import com.saltedge.authenticator.core.model.ConnectionStatus
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.constants.API_V1_VERSION
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import java.security.PrivateKey

@RunWith(RobolectricTestRunner::class)
class SettingsListInteractorV1Test {

    private lateinit var interactor: SettingsListInteractorV1

    private val mockConnectionsRepository = Mockito.mock(ConnectionsRepositoryAbs::class.java)
    private val mockKeyStoreManager = Mockito.mock(KeyManagerAbs::class.java)
    private val mockApiManagerV1 = Mockito.mock(AuthenticatorApiManagerAbs::class.java)
    private val mockPrivateKey = Mockito.mock(PrivateKey::class.java)

    private val connectionV1 = Connection().apply {
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
    private val richConnectionV1 = RichConnection(connectionV1, mockPrivateKey)

    @Before
    fun setUp() {
        given(mockConnectionsRepository.getById(connectionV1.id)).willReturn(connectionV1)
        given(mockConnectionsRepository.getAllActiveConnections()).willReturn(listOf(connectionV1))
        given(mockKeyStoreManager.enrichConnection(connectionV1, addProviderKey = false)).willReturn(richConnectionV1)

        interactor = SettingsListInteractorV1(
            apiManager = mockApiManagerV1,
            connectionsRepository = mockConnectionsRepository,
            keyStoreManager = mockKeyStoreManager,
        )
    }

    @Test
    @Throws(Exception::class)
    fun wipeApplicationTest() {
        interactor.deleteAllConnectionsAndKeys()

        Mockito.verify(mockKeyStoreManager).deleteKeyPairsIfExist(
            mockConnectionsRepository.getAllConnections().map { it.guid }
        )
        Mockito.verify(mockConnectionsRepository).deleteAllConnections()
    }

    @Test
    @Throws(Exception::class)
    fun sendRevokeRequestForConnectionsTest() {
        interactor.sendRevokeRequestForConnections()

        Mockito.verify(mockApiManagerV1).revokeConnections(
            connectionsAndKeys = listOf(richConnectionV1),
            resultCallback = null
        )
    }
}
