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

import com.saltedge.android.test_tools.CoroutineViewModelTest
import com.saltedge.authenticator.TestFactory
import com.saltedge.authenticator.core.api.ERROR_CLASS_AUTHORIZATION_NOT_FOUND
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.ConnectionStatus
import com.saltedge.authenticator.core.tools.secure.BaseCryptoToolsAbs
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.v2.ScaServiceClientAbs
import com.saltedge.authenticator.sdk.v2.api.model.configuration.ConfigurationDataV2
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.verifyNoInteractions
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.kotlin.verifyNoMoreInteractions
import org.robolectric.RobolectricTestRunner

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class ConnectionsListInteractorTest : CoroutineViewModelTest() {

    private lateinit var interactor: ConnectionsListInteractor
    private val mockConnectionsRepository = mock(ConnectionsRepositoryAbs::class.java)
    private val mockKeyStoreManager = mock(KeyManagerAbs::class.java)
    private val mockCryptoTools = mock(BaseCryptoToolsAbs::class.java)
    private val mockApiManagerV1 = mock(AuthenticatorApiManagerAbs::class.java)
    private val mockApiManagerV2 = mock(ScaServiceClientAbs::class.java)
    private val mockCallback = mock(ConnectionsListInteractorCallback::class.java)
    private lateinit var testFactory: TestFactory

    @Before
    override fun setUp() {
        super.setUp()
        given(mockCallback.coroutineScope).willReturn(TestCoroutineScope(testDispatcher))
        testFactory = TestFactory()
        testFactory.mockConnections(mockConnectionsRepository)
        testFactory.mockRichConnections(mockKeyStoreManager)
        testFactory.mockConsents(mockCryptoTools)

        interactor = ConnectionsListInteractor(
            connectionsRepository = mockConnectionsRepository,
            keyStoreManager = mockKeyStoreManager,
            cryptoTools = mockCryptoTools,
            v1ApiManager = mockApiManagerV1,
            v2ApiManager = mockApiManagerV2,
            defaultDispatcher = testDispatcher
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
        Mockito.verify(mockCallback).onDatasetChanged(emptyList(), emptyList())
    }

    @Test
    @Throws(Exception::class)
    fun updateNameAndSaveTestCase1() {
        //given
        val newName = "new name"
        val guid = testFactory.connection2.guid

        //when
        interactor.updateNameAndSave(connectionGuid = guid, newConnectionName = newName)

        //then
        Mockito.verify(mockConnectionsRepository).updateNameAndSave(testFactory.connection2, newName)
    }

    @Test
    @Throws(Exception::class)
    fun updateNameAndSaveTestCase2() {
        //given
        val newName = "new name"
        val guid = "guidX"

        //when
        interactor.updateNameAndSave(connectionGuid = guid, newConnectionName = newName)

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
            connectionsAndKeys = listOf(testFactory.richConnection1),
            resultCallback = interactor
        )
        Mockito.verify(mockApiManagerV2).fetchConsents(
            richConnections = listOf(testFactory.richConnection2, testFactory.richConnection4),
            callback = interactor
        )
        verifyNoMoreInteractions(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)
    }

    @Test
    @Throws(Exception::class)
    fun updateConnectionConfigurationCase1() {
        //given
        interactor.updateConnections()
        Mockito.clearInvocations(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)

        //when
        interactor.updateConnectionConfiguration()

        //then
        Mockito.verify(mockApiManagerV2).showConnectionConfiguration(
            richConnection = testFactory.richConnection2,
            providerId = "demobank2",
            callback = interactor
        )
        Mockito.verify(mockApiManagerV2).showConnectionConfiguration(
            richConnection = testFactory.richConnection4,
            providerId = "demobank4",
            callback = interactor
        )
        verifyNoMoreInteractions(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)
    }

    @Test
    @Throws(Exception::class)
    fun updateConnectionConfigurationCase2() {
        //given
        interactor.updateConnections()
        testFactory.richConnection2.connection.apply {
            accessToken = ""
            status = "${ConnectionStatus.INACTIVE}"
        }
        Mockito.clearInvocations(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)

        //when
        interactor.updateConnectionConfiguration()

        //then
        Mockito.verify(mockApiManagerV2).showConnectionConfiguration(
            richConnection = testFactory.richConnection4,
            providerId = testFactory.richConnection4.connection.code,
            callback = interactor
        )
        verifyNoMoreInteractions(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)
    }

    @Test
    @Throws(Exception::class)
    fun updateConnectionConfigurationCase3() {
        //given
        interactor.updateConnections()
        testFactory.richConnection2.connection.apply {
            code = "demobank4"
        }
        Mockito.clearInvocations(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)

        //when
        interactor.updateConnectionConfiguration()

        //then
        Mockito.verify(mockApiManagerV2).showConnectionConfiguration(
            richConnection = testFactory.richConnection2,
            providerId = "demobank4",
            callback = interactor
        )
        verifyNoMoreInteractions(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)
    }

    @Test
    @Throws(Exception::class)
    fun onShowConnectionConfigurationSuccessCase1() {
        //given
        interactor.updateConnections()

        Mockito.clearInvocations(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)
        assertThat(testFactory.richConnection4.connection.logoUrl, equalTo("https://www.saltedge.com/"))

        //when
        interactor.onShowConnectionConfigurationSuccess(
            result = ConfigurationDataV2(
                scaServiceUrl = "connectUrl",
                providerName = "name",
                providerId = "demobank4",
                providerLogoUrl = "https://www.saltedge.com/",
                apiVersion = "2",
                providerSupportEmail = "example@example.com",
                providerPublicKey = "-----BEGIN PUBLIC KEY-----",
                geolocationRequired = true
            )
        )

        //then
        assertThat(testFactory.richConnection4.connection.logoUrl, equalTo("https://www.saltedge.com/"))
        Mockito.verifyNoInteractions(mockConnectionsRepository)
    }

    @Test
    @Throws(Exception::class)
    fun onShowConnectionConfigurationSuccessCase2() {
        //given
        interactor.updateConnections()

        Mockito.clearInvocations(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)
        assertThat(testFactory.richConnection4.connection.logoUrl, equalTo("https://www.saltedge.com/"))

        //when
        interactor.onShowConnectionConfigurationSuccess(
            result = ConfigurationDataV2(
                scaServiceUrl = "connectUrl",
                providerName = "name",
                providerId = "demobank4",
                providerLogoUrl = "updatedUrl",
                apiVersion = "2",
                providerSupportEmail = "example@example.com",
                providerPublicKey = "-----BEGIN PUBLIC KEY-----",
                geolocationRequired = true
            )
        )

        //then
        assertThat(testFactory.richConnection4.connection.logoUrl, equalTo("updatedUrl"))
        Mockito.verify(mockConnectionsRepository).saveModel(testFactory.connection4)
    }

    @Test
    @Throws(Exception::class)
    fun revokeConnectionTestCase1() {
        //given
        interactor.updateConnections()
        Mockito.clearInvocations(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)
        val guid = testFactory.connection1.guid

        //when
        interactor.revokeConnection(guid)

        //then
        Mockito.verify(mockConnectionsRepository).getByGuid(guid)
        Mockito.verify(mockApiManagerV1).revokeConnections(listOf(testFactory.richConnection1), resultCallback = interactor)
        Mockito.verifyNoMoreInteractions(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)
    }

    @Test
    @Throws(Exception::class)
    fun revokeConnectionTestCase2() {
        //given
        interactor.updateConnections()
        Mockito.clearInvocations(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)
        given(mockConnectionsRepository.getAllConnections()).willReturn(testFactory.allActiveConnections)
        val guid = testFactory.connection3Inactive.guid

        //when
        interactor.revokeConnection(guid)

        //then
        Mockito.verify(mockConnectionsRepository).getByGuid(guid)
        Mockito.verify(mockKeyStoreManager).deleteKeyPairIfExist(guid)
        Mockito.verify(mockConnectionsRepository).deleteConnection(guid)
        Mockito.verify(mockConnectionsRepository).getAllConnections()
        Mockito.verify(mockCallback).onDatasetChanged(testFactory.allActiveConnections, emptyList())
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

        val revokedTokens = listOf(testFactory.connection1.accessToken)
        val apiErrors = listOf(ApiErrorData(errorClassName = ERROR_CLASS_AUTHORIZATION_NOT_FOUND, accessToken = testFactory.connection3Inactive.accessToken))

        //when
        interactor.onConnectionsRevokeResult(revokedTokens, apiErrors)

        //then
        Mockito.verify(mockKeyStoreManager).deleteKeyPairIfExist(testFactory.connection1.guid)
        Mockito.verify(mockConnectionsRepository).deleteConnection(testFactory.connection1.guid)
        Mockito.verify(mockKeyStoreManager).deleteKeyPairIfExist(testFactory.connection3Inactive.guid)
        Mockito.verify(mockConnectionsRepository).deleteConnection(testFactory.connection3Inactive.guid)
        Mockito.verify(mockConnectionsRepository).getAllConnections()
        Mockito.verify(mockCallback).onDatasetChanged(testFactory.allConnections, emptyList())
        Mockito.verifyNoMoreInteractions(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)
    }

    @Test
    @Throws(Exception::class)
    fun onConnectionsV2RevokeResultTestCase1() {
        //given
        interactor.updateConnections()
        Mockito.clearInvocations(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2, mockCallback)

        val revokedIDs = listOf(testFactory.connection2.id)
        val apiErrors = listOf(ApiErrorData(errorClassName = ERROR_CLASS_AUTHORIZATION_NOT_FOUND, accessToken = testFactory.connection3Inactive.accessToken))

        //when
        interactor.onConnectionsV2RevokeResult(revokedIDs, apiErrors)

        //then
        Mockito.verify(mockKeyStoreManager).deleteKeyPairIfExist(testFactory.connection2.guid)
        Mockito.verify(mockConnectionsRepository).deleteConnection(testFactory.connection2.guid)
        Mockito.verify(mockKeyStoreManager).deleteKeyPairIfExist(testFactory.connection3Inactive.guid)
        Mockito.verify(mockConnectionsRepository).deleteConnection(testFactory.connection3Inactive.guid)
        Mockito.verify(mockConnectionsRepository).getAllConnections()
        Mockito.verify(mockCallback).onDatasetChanged(testFactory.allConnections, emptyList())
        Mockito.verifyNoMoreInteractions(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)
    }

    @Test
    @Throws(Exception::class)
    fun onFetchEncryptedDataResultTestCase1() {
        //given
        interactor.updateConnections()
        Mockito.clearInvocations(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2, mockCallback)

        //when
        interactor.onFetchEncryptedDataResult(testFactory.encV1Consents, emptyList())

        //then
        Mockito.verify(mockCallback).coroutineScope
        Mockito.verify(mockCallback).onDatasetChanged(testFactory.allConnections, testFactory.v1Consents)
        Mockito.verifyNoMoreInteractions(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)
    }

    @Test
    @Throws(Exception::class)
    fun onFetchEncryptedDataResultTestCase2() {
        //given
        interactor.updateConnections()
        Mockito.clearInvocations(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2, mockCallback)

        //when
        interactor.onFetchConsentsV2Result(testFactory.encV2Consents, emptyList())

        //then
        Mockito.verify(mockCallback).coroutineScope
        Mockito.verify(mockCallback).onDatasetChanged(testFactory.allConnections, testFactory.v2Consents)
        Mockito.verifyNoMoreInteractions(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)
    }

    @Test
    @Throws(Exception::class)
    fun getConsentsTestCase1() {
        //given
        interactor.updateConnections()
        interactor.onFetchEncryptedDataResult(testFactory.encV1Consents, emptyList())
        interactor.onFetchConsentsV2Result(testFactory.encV2Consents, emptyList())
        Mockito.clearInvocations(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2, mockCallback)
        assertThat(interactor.allConsents.size, equalTo(4))

        //when
        val result = interactor.getConsents(connectionGuid = testFactory.connection1.guid)

        //then
        assertThat(result, equalTo(testFactory.v1Consents))
        Mockito.verifyNoMoreInteractions(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)
    }

    @Test
    @Throws(Exception::class)
    fun getConsentsTestCase2() {
        //given
        interactor.updateConnections()
        interactor.onFetchEncryptedDataResult(testFactory.encV1Consents, emptyList())
        interactor.onFetchConsentsV2Result(testFactory.encV2Consents, emptyList())
        Mockito.clearInvocations(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2, mockCallback)

        //when
        val result = interactor.getConsents(connectionGuid = testFactory.connection2.guid)

        //then
        assertThat(result, equalTo(testFactory.v2Consents))
        Mockito.verifyNoMoreInteractions(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)
    }

    @Test
    @Throws(Exception::class)
    fun getConsentsTestCase3() {
        //given
        interactor.updateConnections()
        interactor.onFetchEncryptedDataResult(testFactory.encV1Consents, emptyList())
        interactor.onFetchEncryptedDataResult(testFactory.encV2Consents, emptyList())
        Mockito.clearInvocations(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2, mockCallback)

        //when
        val result = interactor.getConsents(connectionGuid = testFactory.connection3Inactive.guid)

        //then
        assertThat(result, equalTo(emptyList()))
        Mockito.verifyNoMoreInteractions(mockConnectionsRepository, mockApiManagerV1, mockApiManagerV2)
    }
}
