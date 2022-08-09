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

    @Before
    override fun setUp() {
        super.setUp()
        given(mockCallback.coroutineScope).willReturn(TestCoroutineScope(testDispatcher))
        TestFactory.mockConnections(mockConnectionsRepository)
        TestFactory.mockRichConnections(mockKeyStoreManager)
        TestFactory.mockConsents(mockCryptoTools)

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
        interactor.updateConnection(connectionGuid = TestFactory.connection1.guid)
        Mockito.clearInvocations(mockConnectionsRepository, mockKeyStoreManager)

        //when
        interactor.updateConsents()

        //then
        Mockito.verify(mockApiManagerV1)
            .getConsents(connectionsAndKeys = listOf(TestFactory.richConnection1), resultCallback = interactor)
    }

    @Test
    @Throws(Exception::class)
    fun updateConsentsTestCase2() {
        //given
        interactor.updateConnection(connectionGuid = TestFactory.connection2.guid)
        Mockito.clearInvocations(mockConnectionsRepository, mockKeyStoreManager)

        //when
        interactor.updateConsents()

        //then
        Mockito.verify(mockApiManagerV2)
            .fetchConsents(richConnections = listOf(TestFactory.richConnection2), callback = interactor)
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
        interactor.consents = TestFactory.v2Consents

        //when
        val result = interactor.getConsent(consentId = TestFactory.v2ConsentData.id)

        //then
        assertThat(result, equalTo(TestFactory.v2ConsentData))
    }

    @Test
    @Throws(Exception::class)
    fun getConsentTestCase2() {
        //when
        val result = interactor.getConsent(consentId = TestFactory.v2ConsentData.id)

        //then
        Assert.assertNull(result)
    }

    @Test
    @Throws(Exception::class)
    fun removeConsentTestCase1() {
        //given
        interactor.consents = TestFactory.v1Consents
        assertThat(interactor.consents.size, equalTo(3))

        //when
        val result = interactor.removeConsent(consentId = TestFactory.v1Consents.first().id)

        //then
        assertThat(result, equalTo(TestFactory.v1Consents.first()))
        assertThat(interactor.consents.size, equalTo(2))
        verify(mockCallback)
            .onDatasetChanged(listOf(
                TestFactory.v1PispFutureConsentData,
                TestFactory.v1PispRecurringConsentData
            ))
    }

    @Test
    @Throws(Exception::class)
    fun removeConsentTestCase2() {
        //given
        interactor.consents = TestFactory.v1Consents
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
        interactor.updateConnection(connectionGuid = TestFactory.connection1.guid)
        Mockito.clearInvocations(mockConnectionsRepository, mockKeyStoreManager)

        //when
        interactor.onFetchEncryptedDataResult(TestFactory.encV1Consents, emptyList())

        //then
        assertThat(interactor.consents.size, equalTo(3))
        verify(mockCallback).onDatasetChanged(TestFactory.v1Consents)
    }

    @Test
    @Throws(Exception::class)
    fun onFetchConsentsV2ResultTest() {
        //given
        interactor.updateConnection(connectionGuid = TestFactory.connection2.guid)
        Mockito.clearInvocations(mockConnectionsRepository, mockKeyStoreManager)

        //when
        interactor.onFetchConsentsV2Result(TestFactory.encV2Consents, emptyList())

        //then
        assertThat(interactor.consents.size, equalTo(1))
        verify(mockCallback).onDatasetChanged(TestFactory.v2Consents)
    }
}
