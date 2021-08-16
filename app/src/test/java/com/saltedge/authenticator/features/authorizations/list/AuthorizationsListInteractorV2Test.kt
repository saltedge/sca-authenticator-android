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
package com.saltedge.authenticator.features.authorizations.list

import com.saltedge.android.test_tools.CommonTestTools
import com.saltedge.android.test_tools.CoroutineViewModelTest
import com.saltedge.android.test_tools.encryptWithTestKey
import com.saltedge.authenticator.app.AppTools
import com.saltedge.authenticator.app.KEY_STATUS_CLOSED
import com.saltedge.authenticator.core.api.*
import com.saltedge.authenticator.core.api.model.DescriptionData
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.api.model.error.createRequestError
import com.saltedge.authenticator.core.model.ConnectionStatus
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.polling.PollingServiceAbs
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.features.authorizations.common.*
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.v2.ScaServiceClientAbs
import com.saltedge.authenticator.sdk.v2.api.API_V2_VERSION
import com.saltedge.authenticator.sdk.v2.api.model.authorization.AuthorizationResponseData
import com.saltedge.authenticator.sdk.v2.api.model.authorization.AuthorizationV2Data
import com.saltedge.authenticator.sdk.v2.api.model.authorization.UpdateAuthorizationData
import com.saltedge.authenticator.sdk.v2.api.model.authorization.UpdateAuthorizationResponseData
import com.saltedge.authenticator.sdk.v2.polling.PollingAuthorizationsContract
import com.saltedge.authenticator.sdk.v2.tools.CryptoToolsV2Abs
import com.saltedge.authenticator.widget.security.ActivityUnlockType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.robolectric.RobolectricTestRunner

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class AuthorizationsListInteractorV2Test : CoroutineViewModelTest() {

    private lateinit var interactor: AuthorizationsListInteractorV2
    private val mockContract = mock<AuthorizationsListInteractorCallback>()
    private val mockKeyStoreManager = mock<KeyManagerAbs>()
    private val mockConnectionsRepository = mock<ConnectionsRepositoryAbs>()
    private val mockCryptoToolsV2 = mock<CryptoToolsV2Abs>()
    private val mockApiManagerV2 = mock<ScaServiceClientAbs>()
    private val mockPollingServiceV2 = mock<PollingServiceAbs<PollingAuthorizationsContract>>()
    private val mockConnectionV2 = Connection().apply {
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
        geolocationRequired = true//TODO
    }
    private val richConnectionV2 = RichConnection(mockConnectionV2, CommonTestTools.testPrivateKey, CommonTestTools.testPublicKey)
    private val authorizations: List<AuthorizationV2Data> = listOf(
        createAuthorization(id = 1),
        createAuthorization(id = 2),
        createAuthorization(id = 3, status = KEY_STATUS_CLOSED)
    )
    private val encryptedAuthorizations: List<AuthorizationResponseData> = authorizations.map { it.encryptWithTestKey() }
    private val items: List<AuthorizationItemViewModel> = authorizations
        .filterNot { KEY_STATUS_CLOSED == it.status }
        .map { it.toAuthorizationItemViewModel(mockConnectionV2)!! }

    @Before
    override fun setUp() {
        super.setUp()
        AppTools.lastUnlockType = ActivityUnlockType.BIOMETRICS
        given(mockContract.coroutineScope).willReturn(TestCoroutineScope(testDispatcher))
        given(mockApiManagerV2.createAuthorizationsPollingService()).willReturn(mockPollingServiceV2)
        given(mockConnectionsRepository.getAllActiveConnectionsByApi(API_V2_VERSION)).willReturn(listOf(mockConnectionV2))
        given(mockKeyStoreManager.enrichConnection(mockConnectionV2, addProviderKey = true)).willReturn(richConnectionV2)
        encryptedAuthorizations.forEachIndexed { index, encryptedData ->
            given(mockCryptoToolsV2.decryptAuthorizationData(encryptedData, richConnectionV2.private))
                .willReturn(authorizations[index])
        }
        interactor = AuthorizationsListInteractorV2(
            connectionsRepository = mockConnectionsRepository,
            keyStoreManager = mockKeyStoreManager,
            cryptoTools = mockCryptoToolsV2,
            apiManager = mockApiManagerV2,
            defaultDispatcher = testDispatcher
        )
        interactor.contract = mockContract
        Mockito.clearInvocations(mockConnectionsRepository, mockKeyStoreManager, mockCryptoToolsV2, mockApiManagerV2)
    }

    @Test
    @Throws(Exception::class)
    fun onStopTest() {
        //when
        interactor.onStop()

        //then
        verify(mockPollingServiceV2).contract = null
        verify(mockPollingServiceV2).stop()
    }

    @Test
    @Throws(Exception::class)
    fun onResumeCase1() {
        //given onResume event, no connection, no items
        given(mockConnectionsRepository.getAllActiveConnectionsByApi(API_V2_VERSION)).willReturn(emptyList())

        //when
        interactor.onResume()

        //then
        Assert.assertTrue(interactor.noConnections)
        verify(mockPollingServiceV2).contract = interactor
        verify(mockPollingServiceV2).start()
    }

    @Test
    @Throws(Exception::class)
    fun onResumeCase2() {
        //when
        interactor.onResume()

        //then
        verify(mockConnectionsRepository).getAllActiveConnectionsByApi(API_V2_VERSION)
        Assert.assertFalse(interactor.noConnections)
    }

    @Test
    @Throws(Exception::class)
    fun onFetchAuthorizationsResultTestCase1() {
        //when
        interactor.onFetchAuthorizationsResult(
            result = emptyList(),
            errors = listOf(createRequestError(404))
        )

        //then
        Mockito.verifyNoMoreInteractions(mockConnectionsRepository)
    }

    @Test
    @Throws(Exception::class)
    fun onFetchAuthorizationsResultTestCase2() {
        //when
        interactor.onFetchAuthorizationsResult(
            result = emptyList(),
            errors = listOf(ApiErrorData(errorClassName = ERROR_CLASS_CONNECTION_NOT_FOUND))
        )

        //then
        Mockito.verifyNoMoreInteractions(mockConnectionsRepository)
    }

    @Test
    @Throws(Exception::class)
    fun onFetchAuthorizationsResultTestCase3() {
        //when
        interactor.onFetchAuthorizationsResult(
            result = emptyList(),
            errors = listOf(
                ApiErrorData(
                    errorClassName = ERROR_CLASS_CONNECTION_NOT_FOUND,
                    accessToken = "token"
                )
            )
        )

        //then
        verify(mockConnectionsRepository).invalidateConnectionsByTokens(listOf("token"))
        verify(mockConnectionsRepository).getAllActiveConnectionsByApi(API_V2_VERSION)
    }

    @Test
    @Throws(Exception::class)
    fun onFetchAuthorizationsResultTestCase4() {
        //given
        val fetchResult = encryptedAuthorizations

        //when
        interactor.onFetchAuthorizationsResult(result = fetchResult, errors = emptyList())

        //then
        verify(mockContract).onAuthorizationsReceived(data = items, newModelsApiVersion = API_V2_VERSION)
    }

    @Test
    @Throws(Exception::class)
    fun onFetchAuthorizationsResultTestCase5() {
        //given
        val finalResponseModel = AuthorizationResponseData(
            id = "111",
            connectionId = mockConnectionV2.id,
            status = "confirmed",
            iv = "",
            key = "",
            data = "",
            finishedAt = DateTime.now().minusSeconds(1)
        )
        val finalResponseModelWithOutFinishedAt = finalResponseModel
            .copy(id = "222", finishedAt = null)
        val finalResponseModelWithExpiredFinishedAt = finalResponseModel
            .copy(id = "333", finishedAt = DateTime.now().minusSeconds(LIFE_TIME_OF_FINAL_MODEL + 100))
        val fetchResult = encryptedAuthorizations + listOf(finalResponseModel, finalResponseModelWithOutFinishedAt, finalResponseModelWithExpiredFinishedAt)

        //when
        interactor.onFetchAuthorizationsResult(result = fetchResult, errors = emptyList())

        //then
        val finalItem = AuthorizationItemViewModel(
            title = "",
            description = DescriptionData(text = null),
            connectionID = finalResponseModel.connectionId,
            connectionName = mockConnectionV2.name,
            connectionLogoUrl = mockConnectionV2.logoUrl,
            validSeconds = 0,
            endTime = DateTime(0),
            startTime = DateTime(0L),
            authorizationID = finalResponseModel.id,
            authorizationCode = "",
            apiVersion = API_V2_VERSION,
            status = AuthorizationStatus.CONFIRMED,
            geolocationRequired = mockConnectionV2.geolocationRequired!!
        ).apply {
            updateDestroyAt(finalResponseModel.finishedAt)
        }
        val expectedResult = items + finalItem
        verify(mockContract).onAuthorizationsReceived(data = expectedResult, newModelsApiVersion = API_V2_VERSION)
    }

    @Test
    @Throws(Exception::class)
    fun updateAuthorizationTestCase1() {
        //when
        val result = interactor.updateAuthorization(
            connectionID = items[0].connectionID,
            authorizationID = items[0].authorizationID,
            authorizationCode = items[0].authorizationCode,
            confirm = true,
            locationDescription = "GEO:52.506931;13.144558"
        )

        //then
        verify(mockApiManagerV2).confirmAuthorization(
            richConnection = richConnectionV2,
            authorizationID= items[0].authorizationID,
            authorizationData = UpdateAuthorizationData(
                authorizationCode = items[0].authorizationCode,
                geolocation = "GEO:52.506931;13.144558",
                userAuthorizationType = "biometrics",
            ),
            callback = interactor
        )
        Assert.assertTrue(result)
    }

    @Test
    @Throws(Exception::class)
    fun updateAuthorizationTestCase2() {
        //when
        val result = interactor.updateAuthorization(
            connectionID = items[0].connectionID,
            authorizationID = items[0].authorizationID,
            authorizationCode = items[0].authorizationCode,
            confirm = false,
            locationDescription = "GEO:52.506931;13.144558"
        )

        //then
        verify(mockApiManagerV2).denyAuthorization(
            richConnection = richConnectionV2,
            authorizationID= items[0].authorizationID,
            authorizationData = UpdateAuthorizationData(
                authorizationCode = items[0].authorizationCode,
                geolocation = "GEO:52.506931;13.144558",
                userAuthorizationType = "biometrics",
            ),
            callback = interactor
        )
        Assert.assertTrue(result)
    }

    @Test
    @Throws(Exception::class)
    fun updateAuthorizationTestCase3() {
        //when
        val result = interactor.updateAuthorization(
            connectionID = "999",
            authorizationID = items[0].authorizationID,
            authorizationCode = items[0].authorizationCode,
            confirm = false,
            locationDescription = "GEO:52.506931;13.144558"
        )

        //then
        verifyNoMoreInteractions(mockApiManagerV2)
        Assert.assertFalse(result)
    }

    @Test
    @Throws(Exception::class)
    fun onAuthorizationConfirmSuccessTest() {
        //given
        val confirmResult = UpdateAuthorizationResponseData(status = "confirmed", authorizationID = items[1].authorizationID)
        val connectionID = items[1].connectionID

        //when
        interactor.onAuthorizationConfirmSuccess(result = confirmResult, connectionID = connectionID)

        //then
        verify(mockContract).onConfirmDenySuccess(
            connectionID = connectionID,
            authorizationID = confirmResult.authorizationID,
            newStatus = AuthorizationStatus.CONFIRMED
        )
    }

    @Test
    @Throws(Exception::class)
    fun onAuthorizationConfirmFailureTest() {
        //given
        val error = createRequestError(404)

        //when
        interactor.onAuthorizationConfirmFailure(error = error, connectionID = "1", authorizationID = "2")

        //then
        verify(mockContract).onConfirmDenyFailure(
            error = error,
            connectionID = "1",
            authorizationID = "2"
        )
    }

    @Test
    @Throws(Exception::class)
    fun onAuthorizationDenySuccessTest() {
        //given
        val denyResult = UpdateAuthorizationResponseData(status = "denied", authorizationID = items[1].authorizationID)
        val connectionID = items[1].connectionID

        //when
        interactor.onAuthorizationDenySuccess(result = denyResult, connectionID = connectionID)

        //then
        verify(mockContract).onConfirmDenySuccess(
            connectionID = connectionID,
            authorizationID = denyResult.authorizationID,
            newStatus = AuthorizationStatus.DENIED
        )
    }

    @Test
    @Throws(Exception::class)
    fun onAuthorizationDenyFailureTest() {
        //given
        val error = createRequestError(404)

        //when
        interactor.onAuthorizationDenyFailure(error = error, connectionID = "1", authorizationID = "2")

        //then
        verify(mockContract).onConfirmDenyFailure(
            error = error,
            connectionID = "1",
            authorizationID = "2"
        )
    }

    private fun createAuthorization(id: Int, status: String = "pending"): AuthorizationV2Data {
        val createdAt = DateTime.now(DateTimeZone.UTC)
        return AuthorizationV2Data(
            authorizationID = "$id",
            authorizationCode = "$id$id$id",
            createdAt = createdAt,
            expiresAt = createdAt.plusMinutes(3),
            title = "title$id",
            description = DescriptionData(text = "desc$id"),
            connectionID = mockConnectionV2.id,
            status = status
        )
    }
}
