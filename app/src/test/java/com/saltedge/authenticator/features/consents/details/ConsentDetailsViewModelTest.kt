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
package com.saltedge.authenticator.features.consents.details

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.test.core.app.ApplicationProvider
import com.saltedge.android.test_tools.ViewModelTest
import com.saltedge.authenticator.app.KEY_GUID
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.api.model.ConsentData
import com.saltedge.authenticator.sdk.api.model.ConsentSharedData
import com.saltedge.authenticator.sdk.api.model.response.ConsentRevokeResponseData
import com.saltedge.authenticator.TestAppTools
import com.saltedge.authenticator.core.api.KEY_DATA
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.ConnectionStatus
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.sdk.constants.API_V1_VERSION
import com.saltedge.authenticator.tools.toDateFormatString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.joda.time.DateTime
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.robolectric.RobolectricTestRunner
import java.security.PrivateKey

@RunWith(RobolectricTestRunner::class)
class ConsentDetailsViewModelTest : ViewModelTest() {

    private lateinit var viewModel: ConsentDetailsViewModel
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val mockConnectionsRepository = mock(ConnectionsRepositoryAbs::class.java)
    private val mockKeyStoreManager = mock(KeyManagerAbs::class.java)
    private val mockApiManager = mock(AuthenticatorApiManagerAbs::class.java)
    private val mockPrivateKey = Mockito.mock(PrivateKey::class.java)
    private val defaultConnectionV1 = Connection().apply {
            guid = "connection1"
            code = "demobank"
            name = "Demobank"
            status = "${ConnectionStatus.ACTIVE}"
            accessToken = "token"
            createdAt = 0L
            updatedAt = 0L
            apiVersion = API_V1_VERSION
        }
    private val richConnection = RichConnection(defaultConnectionV1, mockPrivateKey)
    private val defaultConsent = ConsentData(
        id = "consent1",
        userId = "user1",
        createdAt = DateTime.now(),
        expiresAt = DateTime.now().plusDays(1),
        tppName = "tppName",
        consentTypeString = "aisp",
        accounts = emptyList(),
        sharedData = ConsentSharedData(balance = true, transactions = true),
        connectionId = "connection1"
    )

    @Before
    fun setUp() {
        Mockito.doReturn(defaultConnectionV1).`when`(mockConnectionsRepository).getByGuid(defaultConnectionV1.guid)
        given(mockKeyStoreManager.enrichConnection(defaultConnectionV1, addProviderKey = false)).willReturn(richConnection)

        viewModel = ConsentDetailsViewModel(
            appContext = context,
            connectionsRepository = mockConnectionsRepository,
            keyStoreManager = mockKeyStoreManager,
            apiManager = mockApiManager
        )
    }

    @Test
    @Throws(Exception::class)
    fun onConsentRevokeFailureTest() {
        //given
        val apiError = ApiErrorData(errorClassName = "", errorMessage = "message")

        //when
        viewModel.onConsentRevokeFailure(apiError)

        //then
        assertThat(viewModel.revokeErrorEvent.value!!.peekContent(), equalTo(apiError.errorMessage))
    }

    @Test
    @Throws(Exception::class)
    fun onConsentRevokeSuccessTestCase1() {
        //given invalid response params
        val response = ConsentRevokeResponseData()

        //when
        viewModel.onConsentRevokeSuccess(response)

        //then
        Assert.assertNull(viewModel.revokeSuccessEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onConsentRevokeSuccessTestCase2() {
        //given valid response params
        val response = ConsentRevokeResponseData(consentId = "1")

        //when
        viewModel.onConsentRevokeSuccess(response)

        //then
        assertThat(viewModel.revokeSuccessEvent.value!!.peekContent(), equalTo("1"))
    }

    @Test
    @Throws(Exception::class)
    fun setInitialDataTestCase1() {
        //given arguments
        val arguments = Bundle().apply {
            putString(KEY_GUID, "connection1")
            putSerializable(KEY_DATA, defaultConsent)
        }

        //when
        viewModel.setInitialData(arguments)

        //then
        assertThat(viewModel.fragmentTitle.value, equalTo("tppName"))
        assertThat(viewModel.daysLeft.value, equalTo("1 day left"))
        assertThat(viewModel.consentTitle.value, equalTo("Access to account information"))
        assertThat(viewModel.consentDescription.value.toString(), equalTo(
            "Consent granted to tppName application on the following accounts from Demobank"
        ))
        assertThat(viewModel.consentGranted.value, equalTo(
            "Granted: ${defaultConsent.createdAt.toDateFormatString(TestAppTools.applicationContext)}"
        ))
        assertThat(viewModel.consentExpires.value, equalTo(
            "Expires: ${defaultConsent.expiresAt.toDateFormatString(TestAppTools.applicationContext)}"
        ))
        assertThat(viewModel.accounts.value, equalTo(emptyList()))
        assertThat(viewModel.sharedDataVisibility.value, equalTo(View.VISIBLE))
        assertThat(viewModel.sharedBalanceVisibility.value, equalTo(View.VISIBLE))
        assertThat(viewModel.sharedTransactionsVisibility.value, equalTo(View.VISIBLE))
    }

    @Test
    @Throws(Exception::class)
    fun setInitialDataTestCase2() {
        //given arguments pisp_future
        val consent = defaultConsent.copy(
            consentTypeString = "pisp_future",
            sharedData = ConsentSharedData(balance = false, transactions = false)
        )
        val arguments = Bundle().apply {
            putString(KEY_GUID, "connection1")
            putSerializable(KEY_DATA, consent)
        }

        //when
        viewModel.setInitialData(arguments)

        //then
        assertThat(viewModel.consentTitle.value, equalTo("Consent for future payment"))
        assertThat(viewModel.sharedDataVisibility.value, equalTo(View.VISIBLE))
        assertThat(viewModel.sharedBalanceVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.sharedTransactionsVisibility.value, equalTo(View.GONE))
    }

    @Test
    @Throws(Exception::class)
    fun setInitialDataTestCase3() {
        //given arguments pisp_recurring
        val consent = defaultConsent.copy(
            consentTypeString = "pisp_recurring",
            sharedData = null
        )
        val arguments = Bundle().apply {
            putString(KEY_GUID, "connection1")
            putSerializable(KEY_DATA, consent)
        }

        //when
        viewModel.setInitialData(arguments)

        //then
        assertThat(viewModel.consentTitle.value, equalTo("Consent for recurring payment"))
        assertThat(viewModel.sharedDataVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.sharedBalanceVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.sharedTransactionsVisibility.value, equalTo(View.GONE))
    }

    @Test
    @Throws(Exception::class)
    fun setInitialDataTestCase4() {
        //given arguments with invalid guid
        val arguments = Bundle().apply {
            putSerializable(KEY_DATA, defaultConsent)
        }

        //when
        viewModel.setInitialData(arguments)

        //then
        assertThat(viewModel.fragmentTitle.value, equalTo("Active Consent"))
        assertThat(viewModel.daysLeft.value, equalTo(""))
        assertThat(viewModel.consentTitle.value, equalTo(""))
        assertThat(viewModel.consentDescription.value.toString(), equalTo(""))
        assertThat(viewModel.consentGranted.value, equalTo(""))
        assertThat(viewModel.consentExpires.value, equalTo(""))
        Assert.assertNull(viewModel.accounts.value)
        assertThat(viewModel.sharedDataVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.sharedBalanceVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.sharedTransactionsVisibility.value, equalTo(View.GONE))
    }

    @Test
    @Throws(Exception::class)
    fun setInitialDataTestCase5() {
        //given arguments with invalid params
        val arguments = Bundle()

        //when
        viewModel.setInitialData(arguments)

        //then
        assertThat(viewModel.fragmentTitle.value, equalTo("Active Consent"))
        assertThat(viewModel.daysLeft.value, equalTo(""))
        assertThat(viewModel.consentTitle.value, equalTo(""))
        assertThat(viewModel.consentDescription.value.toString(), equalTo(""))
        assertThat(viewModel.consentGranted.value, equalTo(""))
        assertThat(viewModel.consentExpires.value, equalTo(""))
        Assert.assertNull(viewModel.accounts.value)
        assertThat(viewModel.sharedDataVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.sharedBalanceVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.sharedTransactionsVisibility.value, equalTo(View.GONE))
    }

    @Test
    @Throws(Exception::class)
    fun onRevokeClickTest() {
        //given
        viewModel.setInitialData(Bundle().apply {
            putString(KEY_GUID, "connection1")
            putSerializable(KEY_DATA, defaultConsent)
        })

        //when
        viewModel.onRevokeClick()

        //then
        assertThat(viewModel.revokeAlertEvent.value!!.peekContent(), equalTo(
            "tppName service that is provided to you may be interrupted. Are you sure you want to revoke consent?"
        ))
    }

    @Test
    @Throws(Exception::class)
    fun onRevokeConfirmedTestCase1() {
        //given
        viewModel.setInitialData(Bundle().apply {
            putString(KEY_GUID, "connection1")
            putSerializable(KEY_DATA, defaultConsent)
        })

        //when
        viewModel.onRevokeConfirmed()

        //then
        verify(mockApiManager).revokeConsent(
            consentId = "consent1",
            connectionAndKey = richConnection,
            resultCallback = viewModel
        )
    }

    @Test
    @Throws(Exception::class)
    fun onRevokeConfirmedTestCase2() {
        //given invalid initial data
        viewModel.setInitialData(Bundle())

        //when
        viewModel.onRevokeConfirmed()

        //then
        Mockito.verifyNoInteractions(mockApiManager)
    }
}
