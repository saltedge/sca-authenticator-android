/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.consents.list

import com.saltedge.android.test_tools.CoroutineViewModelTest
import com.saltedge.authenticator.TestFactory
import com.saltedge.authenticator.core.tools.secure.BaseCryptoToolsAbs
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.v2.ScaServiceClientAbs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@ExperimentalCoroutinesApi
class ConsentsListInteractorTest : CoroutineViewModelTest() {

    private lateinit var interactor: ConsentsListInteractor
    private val mockConnectionsRepository = mock(ConnectionsRepositoryAbs::class.java)
    private val mockKeyStoreManager = mock(KeyManagerAbs::class.java)
    private val mockCryptoTools = mock(BaseCryptoToolsAbs::class.java)
    private val mockApiManagerV1 = mock(AuthenticatorApiManagerAbs::class.java)
    private val mockApiManagerV2 = mock(ScaServiceClientAbs::class.java)
    private val mockCallback = mock(ConsentsListInteractorCallback::class.java)
    private lateinit var testFactory: TestFactory

    @Before
    override fun setUp() {
        super.setUp()
        testFactory = TestFactory()
        given(mockCallback.coroutineScope).willReturn(TestCoroutineScope(testDispatcher))
        testFactory.mockConnections(mockConnectionsRepository)
        testFactory.mockRichConnections(mockKeyStoreManager)
        testFactory.mockConsents(mockCryptoTools)

        interactor = ConsentsListInteractor(
            connectionsRepository = mockConnectionsRepository,
            keyStoreManager = mockKeyStoreManager,
            cryptoTools = mockCryptoTools,
            v1ApiManager = mockApiManagerV1,
            v2ApiManager = mockApiManagerV2,
            defaultDispatcher = testDispatcher,
        )
        interactor.contract = mockCallback
    }

    @Test
    @Throws(Exception::class)
    fun updateConsentsTestCase1() {
        //given
        interactor.updateConnection(connectionGuid = testFactory.connection1.guid)
        Mockito.clearInvocations(mockConnectionsRepository, mockKeyStoreManager)

        //when
        interactor.updateConsents()

        //then
        Mockito.verify(mockApiManagerV1)
            .getConsents(connectionsAndKeys = listOf(testFactory.richConnection1), resultCallback = interactor)
    }

    @Test
    @Throws(Exception::class)
    fun updateConsentsTestCase2() {
        //given
        interactor.updateConnection(connectionGuid = testFactory.connection2.guid)
        Mockito.clearInvocations(mockConnectionsRepository, mockKeyStoreManager)

        //when
        interactor.updateConsents()

        //then
        Mockito.verify(mockApiManagerV2)
            .fetchConsents(richConnections = listOf(testFactory.richConnection2), callback = interactor)
    }

    @Test
    @Throws(Exception::class)
    fun updateConsentsTestCase3() {
        //when
        interactor.updateConsents()

        //then
        Mockito.verifyNoMoreInteractions(mockApiManagerV1, mockApiManagerV2)
    }

    @Test
    @Throws(Exception::class)
    fun getConsentTestCase1() {
        //given
        interactor.consents = testFactory.v2Consents

        //when
        val result = interactor.getConsent(consentId = testFactory.v2ConsentData.id)

        //then
        assertThat(result, equalTo(testFactory.v2ConsentData))
    }

    @Test
    @Throws(Exception::class)
    fun getConsentTestCase2() {
        //when
        val result = interactor.getConsent(consentId = testFactory.v2ConsentData.id)

        //then
        Assert.assertNull(result)
    }

    @Test
    @Throws(Exception::class)
    fun removeConsentTestCase1() {
        //given
        interactor.consents = testFactory.v1Consents
        assertThat(interactor.consents.size, equalTo(3))

        //when
        val result = interactor.removeConsent(consentId = testFactory.v1Consents.first().id)

        //then
        assertThat(result, equalTo(testFactory.v1Consents.first()))
        assertThat(interactor.consents.size, equalTo(2))
        verify(mockCallback)
            .onDatasetChanged(listOf(
                testFactory.v1PispFutureConsentData,
                testFactory.v1PispRecurringConsentData
            ))
    }

    @Test
    @Throws(Exception::class)
    fun removeConsentTestCase2() {
        //given
        interactor.consents = testFactory.v1Consents
        assertThat(interactor.consents.size, equalTo(3))

        //when
        val result = interactor.removeConsent(consentId = "x")

        //then
        Assert.assertNull(result)
        assertThat(interactor.consents.size, equalTo(3))
        verifyNoMoreInteractions(mockCallback)
    }

    @Test
    @Throws(Exception::class)
    fun onFetchEncryptedDataResultTest() {
        //given
        interactor.updateConnection(connectionGuid = testFactory.connection1.guid)
        Mockito.clearInvocations(mockConnectionsRepository, mockKeyStoreManager)

        //when
        interactor.onFetchEncryptedDataResult(testFactory.encV1Consents, emptyList())

        //then
        assertThat(interactor.consents.size, equalTo(3))
        verify(mockCallback).onDatasetChanged(testFactory.v1Consents)
    }

    @Test
    @Throws(Exception::class)
    fun onFetchConsentsV2ResultTest() {
        //given
        interactor.updateConnection(connectionGuid = testFactory.connection2.guid)
        Mockito.clearInvocations(mockConnectionsRepository, mockKeyStoreManager)

        //when
        interactor.onFetchConsentsV2Result(testFactory.encV2Consents, emptyList())

        //then
        assertThat(interactor.consents.size, equalTo(1))
        verify(mockCallback).onDatasetChanged(testFactory.v2Consents)
    }
}
