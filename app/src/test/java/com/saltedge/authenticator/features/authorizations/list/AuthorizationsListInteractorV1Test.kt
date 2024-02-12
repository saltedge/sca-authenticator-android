/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.authorizations.list

import com.saltedge.android.test_tools.CommonTestTools
import com.saltedge.android.test_tools.CoroutineViewModelTest
import com.saltedge.android.test_tools.encryptWithTestKey
import com.saltedge.authenticator.app.AppTools
import com.saltedge.authenticator.core.api.ERROR_CLASS_CONNECTION_NOT_FOUND
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.api.model.error.createRequestError
import com.saltedge.authenticator.core.model.ConnectionStatus
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.polling.PollingServiceAbs
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.features.authorizations.common.AuthorizationItemViewModel
import com.saltedge.authenticator.features.authorizations.common.toAuthorizationItemViewModel
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.api.model.authorization.AuthorizationData
import com.saltedge.authenticator.sdk.api.model.response.ConfirmDenyResponseData
import com.saltedge.authenticator.sdk.constants.API_V1_VERSION
import com.saltedge.authenticator.sdk.polling.FetchAuthorizationsContract
import com.saltedge.authenticator.sdk.tools.CryptoToolsV1Abs
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
import org.mockito.BDDMockito.verifyNoInteractions
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.robolectric.RobolectricTestRunner

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class AuthorizationsListInteractorV1Test : CoroutineViewModelTest() {

    private lateinit var interactor: AuthorizationsListInteractorV1
    private val mockContract = mock<AuthorizationsListInteractorCallback>()
    private val mockKeyStoreManager = mock<KeyManagerAbs>()
    private val mockConnectionsRepository = mock<ConnectionsRepositoryAbs>()
    private val mockCryptoToolsV1 = mock<CryptoToolsV1Abs>()
    private val mockApiManagerV1 = mock<AuthenticatorApiManagerAbs>()
    private val mockPollingServiceV1 = mock<PollingServiceAbs<FetchAuthorizationsContract>>()
    private val mockConnectionV1 = Connection().apply {
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
        geolocationRequired = true//TODO
    }
    private val richConnectionV1 = RichConnection(mockConnectionV1, CommonTestTools.testPrivateKey)
    private val authorizations: List<AuthorizationData> = listOf(createAuthorization(id = 1), createAuthorization(id = 2))
    private val encryptedAuthorizations = authorizations.map { it.encryptWithTestKey() }
    private val items: List<AuthorizationItemViewModel> = authorizations.map { it.toAuthorizationItemViewModel(mockConnectionV1)!! }

    @Before
    override fun setUp() {
        super.setUp()
        given(mockContract.coroutineScope).willReturn(TestCoroutineScope(testDispatcher))
        AppTools.lastUnlockType = ActivityUnlockType.BIOMETRICS
        given(mockApiManagerV1.createAuthorizationsPollingService()).willReturn(mockPollingServiceV1)
        given(mockConnectionsRepository.getAllActiveConnectionsByApi(API_V1_VERSION)).willReturn(listOf(mockConnectionV1))
        given(mockKeyStoreManager.enrichConnection(mockConnectionV1, addProviderKey = false)).willReturn(richConnectionV1)
        encryptedAuthorizations.forEachIndexed { index, encryptedData ->
            given(mockCryptoToolsV1.decryptAuthorizationData(encryptedData, richConnectionV1.private))
                .willReturn(authorizations[index])
        }
        interactor = AuthorizationsListInteractorV1(
            connectionsRepository = mockConnectionsRepository,
            keyStoreManager = mockKeyStoreManager,
            cryptoTools = mockCryptoToolsV1,
            apiManager = mockApiManagerV1,
            defaultDispatcher = testDispatcher
        )
        interactor.contract = mockContract
        Mockito.clearInvocations(mockConnectionsRepository, mockKeyStoreManager, mockCryptoToolsV1, mockApiManagerV1)
    }

    @Test
    @Throws(Exception::class)
    fun onStopTest() {
        //when
        interactor.onStop()

        //then
        verify(mockPollingServiceV1).contract = null
        verify(mockPollingServiceV1).stop()
    }

    @Test
    @Throws(Exception::class)
    fun onResumeCase1() {
        //given onResume event, no connection, no items
        given(mockConnectionsRepository.getAllActiveConnectionsByApi(API_V1_VERSION)).willReturn(emptyList())

        //when
        interactor.onResume()

        //then
        Assert.assertTrue(interactor.noConnections)
        verify(mockPollingServiceV1).contract = interactor
        verify(mockPollingServiceV1).start()
    }

    @Test
    @Throws(Exception::class)
    fun onResumeCase2() {
        //when
        interactor.onResume()

        //then
        Assert.assertFalse(interactor.noConnections)
    }

    @Test
    @Throws(Exception::class)
    fun onFetchEncryptedDataResultTestCase1() {
        //when
        interactor.onFetchEncryptedDataResult(
            result = emptyList(),
            errors = listOf(createRequestError(404))
        )

        //then
        Mockito.verifyNoMoreInteractions(mockConnectionsRepository)
    }

    @Test
    @Throws(Exception::class)
    fun onFetchEncryptedDataResultTestCase2() {
        //when
        interactor.onFetchEncryptedDataResult(
            result = emptyList(),
            errors = listOf(ApiErrorData(errorClassName = ERROR_CLASS_CONNECTION_NOT_FOUND))
        )

        //then
        Mockito.verifyNoMoreInteractions(mockConnectionsRepository)
    }

    @Test
    @Throws(Exception::class)
    fun onFetchEncryptedDataResultTestCase3() {
        //when
        interactor.onFetchEncryptedDataResult(
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
        verify(mockConnectionsRepository).getAllActiveConnectionsByApi(API_V1_VERSION)
    }

    @Test
    @Throws(Exception::class)
    fun onFetchEncryptedDataResultTestCase4() {
        //when
        interactor.onFetchEncryptedDataResult(result = encryptedAuthorizations, errors = emptyList())

        //then
        verify(mockContract).onAuthorizationsReceived(data = items, newModelsApiVersion = API_V1_VERSION)
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
        verify(mockApiManagerV1).confirmAuthorization(
            connectionAndKey = richConnectionV1,
            authorizationId = items[0].authorizationID,
            authorizationCode = items[0].authorizationCode,
            geolocation = "GEO:52.506931;13.144558",
            authorizationType = "biometrics",
            resultCallback = interactor
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
        verify(mockApiManagerV1).denyAuthorization(
            connectionAndKey = richConnectionV1,
            authorizationId = items[0].authorizationID,
            authorizationCode = items[0].authorizationCode,
            geolocation = "GEO:52.506931;13.144558",
            authorizationType = "biometrics",
            resultCallback = interactor
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
        verifyNoMoreInteractions(mockApiManagerV1)
        Assert.assertFalse(result)
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenySuccessTestCase1() {
        //given
        val confirmResult = ConfirmDenyResponseData(success = true, authorizationID = items[1].authorizationID)
        val connectionID = items[1].connectionID

        //when
        interactor.onConfirmDenySuccess(result = confirmResult, connectionID = connectionID)

        //then
        verify(mockContract).onConfirmDenySuccess(
            connectionID = connectionID,
            authorizationID = confirmResult.authorizationID!!
        )
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenySuccessTestCase2() {
        //given
        val confirmResult = ConfirmDenyResponseData(success = true, authorizationID = null)
        val connectionID = items[1].connectionID

        //when
        interactor.onConfirmDenySuccess(result = confirmResult, connectionID = connectionID)

        //then
        verifyNoInteractions(mockContract)
    }

    @Test
    @Throws(Exception::class)
    fun onConfirmDenyFailureTest() {
        //given
        val error = createRequestError(404)


        //when
        interactor.onConfirmDenyFailure(error = error, connectionID = "1", authorizationID = "2")

        //then
        verify(mockContract).onConfirmDenyFailure(
            error = error,
            connectionID = "1",
            authorizationID = "2"
        )
    }

    private fun createAuthorization(id: Int): AuthorizationData {
        val createdAt = DateTime.now(DateTimeZone.UTC)
        return AuthorizationData(
            id = "$id",
            authorizationCode = "$id$id$id",
            createdAt = createdAt,
            expiresAt = createdAt.plusMinutes(3),
            title = "title$id",
            description = "desc$id",
            connectionId = mockConnectionV1.id,
        )
    }
}
