/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2020 Salt Edge Inc.
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
package com.saltedge.authenticator.features.connections.list

import com.saltedge.android.test_tools.ViewModelTest
import com.saltedge.authenticator.core.api.ERROR_CLASS_AUTHORIZATION_NOT_FOUND
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.ConnectionStatus
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.api.model.ConsentData
import com.saltedge.authenticator.sdk.api.model.ConsentSharedData
import com.saltedge.authenticator.sdk.constants.API_V1_VERSION
import com.saltedge.authenticator.sdk.tools.CryptoToolsV1Abs
import com.saltedge.authenticator.sdk.v2.ScaServiceClientAbs
import com.saltedge.authenticator.sdk.v2.api.API_V2_VERSION
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.verifyNoInteractions
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import java.security.PrivateKey

@RunWith(RobolectricTestRunner::class)
class ConnectionsListInteractorTest : ViewModelTest() {

    private lateinit var interactor: ConnectionsListInteractor
    private val mockConnectionsRepository = mock(ConnectionsRepositoryAbs::class.java)
    private val mockKeyStoreManager = mock(KeyManagerAbs::class.java)
    private val mockPrivateKey = mock(PrivateKey::class.java)
    private val mockCryptoToolsV1 = mock(CryptoToolsV1Abs::class.java)
    private val mockApiManagerV1 = mock(AuthenticatorApiManagerAbs::class.java)
    private val mockApiManagerV2 = mock(ScaServiceClientAbs::class.java)
    private val mockCallback = mock(ConnectionsListInteractorCallback::class.java)

    private val connection1 = Connection().apply {
        id = "1"
        guid = "guid1"
        code = "demobank1"
        name = "Demobank1"
        status = "${ConnectionStatus.ACTIVE}"
        accessToken = "token1"
        supportEmail = "example@example.com"
        createdAt = 100L
        updatedAt = 100L
        apiVersion = API_V1_VERSION
        geolocationRequired = true
    }
    private val connection2 = Connection().apply {
        id = "2"
        guid = "guid2"
        code = "demobank2"
        name = "Demobank2"
        status = "${ConnectionStatus.ACTIVE}"
        supportEmail = "example@example.com"
        accessToken = "token2"
        createdAt = 200L
        updatedAt = 200L
        apiVersion = API_V2_VERSION
    }
    private val connection3Inactive = Connection().apply {
        id = "3"
        guid = "guid3"
        code = "demobank3"
        name = "Demobank3"
        status = "${ConnectionStatus.INACTIVE}"
        supportEmail = "example@example.com"
        accessToken = "token3"
        createdAt = 300L
        updatedAt = 300L
        apiVersion = API_V1_VERSION
    }
    private val richConnection1 = RichConnection(connection1, mockPrivateKey)
    private val richConnection2 = RichConnection(connection2, mockPrivateKey)
    private val richConnection3 = RichConnection(connection3Inactive, mockPrivateKey)
    private val allConnections = listOf(connection1, connection2, connection3Inactive)
    private val allActiveConnections = listOf(connection1, connection2)
    private val consentData: List<ConsentData> = listOf(
        ConsentData(
            id = "555",
            connectionId = "1",
            userId = "1",
            tppName = "title",
            consentTypeString = "aisp",
            accounts = emptyList(),
            expiresAt = DateTime(0).withZone(DateTimeZone.UTC),
            createdAt = DateTime(0).withZone(DateTimeZone.UTC),
            sharedData = ConsentSharedData(balance = true, transactions = true)
        )
    )

    @Before
    fun setUp() {
        given(mockConnectionsRepository.getByGuid(connection1.guid)).willReturn(connection1)
        given(mockConnectionsRepository.getByGuid(connection2.guid)).willReturn(connection2)
        given(mockConnectionsRepository.getByGuid(connection3Inactive.guid)).willReturn(connection3Inactive)
        given(mockConnectionsRepository.getAllConnections()).willReturn(allConnections)
        given(mockKeyStoreManager.enrichConnection(connection1, addProviderKey = false)).willReturn(richConnection1)
        given(mockKeyStoreManager.enrichConnection(connection2, addProviderKey = true)).willReturn(richConnection2)
        given(mockKeyStoreManager.enrichConnection(connection3Inactive, addProviderKey = false)).willReturn(richConnection3)

        interactor = ConnectionsListInteractor(
            connectionsRepository = mockConnectionsRepository,
            keyStoreManager = mockKeyStoreManager,
            cryptoTools = mockCryptoToolsV1,
            apiManagerV1 = mockApiManagerV1,
            apiManagerV2 = mockApiManagerV2
        )
        interactor.contract = mockCallback
    }

    @Test
    @Throws(Exception::class)
    fun updateConnectionsTestCase1() {
        //given
        given(mockConnectionsRepository.getAllConnections()).willReturn(emptyList())

        //when
        interactor.updateConnections()

        //then
        Mockito.verify(mockCallback).onConnectionsDataChanged(emptyList())
    }

    @Test
    @Throws(Exception::class)
    fun updateNameAndSaveTestCase1() {
        //given
        val newName = "new name"
        val guid = connection2.guid

        //when
        interactor.updateNameAndSave(guid = guid, newName)

        //then
        Mockito.verify(mockConnectionsRepository).updateNameAndSave(connection2, newName)
    }

    @Test
    @Throws(Exception::class)
    fun updateNameAndSaveTestCase2() {
        //given
        val newName = "new name"
        val guid = "guidX"

        //when
        interactor.updateNameAndSave(guid = guid, newName)

        //then
        Mockito.verify(mockConnectionsRepository).getByGuid(guid)
        Mockito.verifyNoMoreInteractions(mockConnectionsRepository)
    }

    @Test
    @Throws(Exception::class)
    fun updateConsentsTestCase1() {
        //given
        given(mockConnectionsRepository.getAllConnections()).willReturn(emptyList())
        interactor.updateConnections()
        Mockito.clearInvocations(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)

        //when
        interactor.updateConsents()

        //then
        verifyNoInteractions(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)
    }

    @Test
    @Throws(Exception::class)
    fun updateConsentsTestCase2() {
        //given
        interactor.updateConnections()
        Mockito.clearInvocations(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)

        //when
        interactor.updateConsents()

        //then
        Mockito.verify(mockApiManagerV1).getConsents(
            connectionsAndKeys = listOf(richConnection1, richConnection3),
            resultCallback = interactor
        )
        verifyNoInteractions(mockApiManagerV2)
    }

    @Test
    @Throws(Exception::class)
    fun revokeConnectionTestCase1() {
        //given
        interactor.updateConnections()
        Mockito.clearInvocations(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)
        val guid = connection1.guid

        //when
        interactor.revokeConnection(guid)

        //then
        Mockito.verify(mockConnectionsRepository).getByGuid(guid)
        Mockito.verify(mockApiManagerV1).revokeConnections(listOf(richConnection1), resultCallback = interactor)
        Mockito.verifyNoMoreInteractions(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)
    }

    @Test
    @Throws(Exception::class)
    fun revokeConnectionTestCase2() {
        //given
        interactor.updateConnections()
        Mockito.clearInvocations(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)
        given(mockConnectionsRepository.getAllConnections()).willReturn(allActiveConnections)
        val guid = connection3Inactive.guid

        //when
        interactor.revokeConnection(guid)

        //then
        Mockito.verify(mockConnectionsRepository).getByGuid(guid)
        Mockito.verify(mockKeyStoreManager).deleteKeyPairIfExist(guid)
        Mockito.verify(mockConnectionsRepository).deleteConnection(guid)
        Mockito.verify(mockConnectionsRepository).getAllConnections()
        Mockito.verify(mockCallback).onConnectionsDataChanged(allActiveConnections)
        Mockito.verifyNoMoreInteractions(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)
    }

    @Test
    @Throws(Exception::class)
    fun revokeConnectionTestCase3() {
        //given
        interactor.updateConnections()
        Mockito.clearInvocations(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)
        val guid = "guidX"

        //when
        interactor.revokeConnection(guid)

        //then
        Mockito.verify(mockConnectionsRepository).getByGuid(guid)
        Mockito.verifyNoMoreInteractions(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)
    }

    @Test
    @Throws(Exception::class)
    fun onConnectionsRevokeResultTestCase1() {
        //given
        interactor.updateConnections()
        Mockito.clearInvocations(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2, mockCallback)

        val revokedTokens = listOf(connection1.accessToken)
        val apiErrors = listOf(ApiErrorData(errorClassName = ERROR_CLASS_AUTHORIZATION_NOT_FOUND, accessToken = connection3Inactive.accessToken))

        //when
        interactor.onConnectionsRevokeResult(revokedTokens, apiErrors)

        //then
        Mockito.verify(mockKeyStoreManager).deleteKeyPairIfExist(connection1.guid)
        Mockito.verify(mockConnectionsRepository).deleteConnection(connection1.guid)
        Mockito.verify(mockKeyStoreManager).deleteKeyPairIfExist(connection3Inactive.guid)
        Mockito.verify(mockConnectionsRepository).deleteConnection(connection3Inactive.guid)
        Mockito.verify(mockConnectionsRepository).getAllConnections()
        Mockito.verify(mockCallback).onConnectionsDataChanged(allConnections)
        Mockito.verifyNoMoreInteractions(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)
    }

    @Test
    @Throws(Exception::class)
    fun onConnectionsV2RevokeResultTestCase1() {
        //given
        interactor.updateConnections()
        Mockito.clearInvocations(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2, mockCallback)

        val revokedIDs = listOf(connection2.id)
        val apiErrors = listOf(ApiErrorData(errorClassName = ERROR_CLASS_AUTHORIZATION_NOT_FOUND, accessToken = connection3Inactive.accessToken))

        //when
        interactor.onConnectionsV2RevokeResult(revokedIDs, apiErrors)

        //then
        Mockito.verify(mockKeyStoreManager).deleteKeyPairIfExist(connection2.guid)
        Mockito.verify(mockConnectionsRepository).deleteConnection(connection2.guid)
        Mockito.verify(mockKeyStoreManager).deleteKeyPairIfExist(connection3Inactive.guid)
        Mockito.verify(mockConnectionsRepository).deleteConnection(connection3Inactive.guid)
        Mockito.verify(mockConnectionsRepository).getAllConnections()
        Mockito.verify(mockCallback).onConnectionsDataChanged(allConnections)
        Mockito.verifyNoMoreInteractions(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)
    }
}
