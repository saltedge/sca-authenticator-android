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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.test.core.app.ApplicationProvider
import com.saltedge.android.test_tools.CommonTestTools
import com.saltedge.android.test_tools.encryptWithTestKey
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.CONSENT_REQUEST_CODE
import com.saltedge.authenticator.features.consents.common.countOfDays
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.api.model.ConsentData
import com.saltedge.authenticator.sdk.api.model.ConsentSharedData
import com.saltedge.authenticator.sdk.tools.crypt.CryptoToolsAbs
import com.saltedge.authenticator.tools.daysTillExpire
import com.saltedge.authenticator.app.guid
import com.saltedge.authenticator.core.api.KEY_DATA
import com.saltedge.authenticator.core.api.KEY_ID
import com.saltedge.authenticator.core.model.ConnectionStatus
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.hamcrest.CoreMatchers.equalTo
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@ExperimentalCoroutinesApi
class ConsentsListViewModelTest {

    private val testDispatcher = TestCoroutineDispatcher()
    private lateinit var viewModel: ConsentsListViewModel
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val mockConnectionsRepository = mock(ConnectionsRepositoryAbs::class.java)
    private val mockKeyStoreManager = mock(KeyManagerAbs::class.java)
    private val mockApiManager = mock(AuthenticatorApiManagerAbs::class.java)
    private val mockCryptoTools = mock(CryptoToolsAbs::class.java)
    private val connection = Connection().apply {
        id = "2"
        guid = "guid2"
        code = "demobank2"
        name = "Demobank2"
        status = "${ConnectionStatus.ACTIVE}"
        accessToken = "token2"
        createdAt = 300L
        updatedAt = 300L
        logoUrl = "https://www.fentury.com/"
    }
    private val mockConnectionAndKey = RichConnection(connection, CommonTestTools.testPrivateKey)
    private val aispConsent = ConsentData(
            id = "555",
            connectionId = "2",
            userId = "1",
            tppName = "title",
            consentTypeString = "aisp",
            accounts = emptyList(),
            expiresAt = DateTime(0).withZone(DateTimeZone.UTC),
            createdAt = DateTime(0).withZone(DateTimeZone.UTC),
            sharedData = ConsentSharedData(balance = true, transactions = true)
        )
    private val pispFutureConsent = ConsentData(
            id = "777",
            userId = "1",
            connectionId = "2",
            tppName = "title",
            consentTypeString = "pisp_future",
            accounts = emptyList(),
            expiresAt = DateTime(0).withZone(DateTimeZone.UTC),
            createdAt = DateTime(0).withZone(DateTimeZone.UTC),
            sharedData = ConsentSharedData(balance = true, transactions = true)
        )
    private val pispRecurringConsent = ConsentData(
        id = "999",
        userId = "1",
        connectionId = "2",
        tppName = "title",
        consentTypeString = "pisp_recurring",
        accounts = emptyList(),
        expiresAt = DateTime(0).withZone(DateTimeZone.UTC),
        createdAt = DateTime(0).withZone(DateTimeZone.UTC),
        sharedData = ConsentSharedData(balance = true, transactions = true)
    )
    private val consents: List<ConsentData> = listOf(aispConsent, pispFutureConsent, pispRecurringConsent)
    private val encryptedConsents = consents.map { it.encryptWithTestKey() }
    private val daysLeftCount = DateTime(0).withZone(DateTimeZone.UTC).daysTillExpire()
    private val daysTillExpireDescription = countOfDays(daysLeftCount, context)
    private val spanned = SpannableStringBuilder(
        "${context.getString(R.string.expires_in)} $daysTillExpireDescription"
    )

    @Before
    fun setUp() {
        given(mockConnectionsRepository.getByGuid("guid2")).willReturn(connection)
        given(mockKeyStoreManager.enrichConnection(connection))
            .willReturn(mockConnectionAndKey)
        encryptedConsents.forEachIndexed { index, encryptedData ->
            given(mockCryptoTools.decryptConsentData(encryptedData, mockConnectionAndKey.private))
                .willReturn(consents[index])
        }

        viewModel = ConsentsListViewModel(
            appContext = context,
            connectionsRepository = mockConnectionsRepository,
            keyStoreManager = mockKeyStoreManager,
            apiManager = mockApiManager,
            cryptoTools = mockCryptoTools,
            defaultDispatcher = testDispatcher
        )
    }

    @Test
    @Throws(Exception::class)
    fun refreshConsentsTestCase1() {
        //given
        val bundle = Bundle().apply { guid = "guid2" }
        viewModel.setInitialData(bundle)

        //when
        viewModel.refreshConsents()

        //then
        Mockito.verify(mockApiManager).getConsents(
            connectionsAndKeys = listOf(mockConnectionAndKey),
            resultCallback = viewModel
        )
    }

    @Test
    @Throws(Exception::class)
    fun refreshConsentsTestCase2() {
        //given
        viewModel.setInitialData(Bundle())

        //when
        viewModel.refreshConsents()

        //then
        Mockito.never()
    }

    @Test
    @Throws(Exception::class)
    fun setInitialDataCase1() {
        //given
        val bundle = Bundle().apply {
            guid = "guid2"
            putSerializable(KEY_DATA, ArrayList<ConsentData>(consents))
        }

        //when
        viewModel.setInitialData(bundle)

        //then
        assertThat(viewModel.logoUrl.value, equalTo("https://www.fentury.com/"))
        assertThat(viewModel.connectionTitle.value, equalTo("Demobank2"))
        assertThat(viewModel.consentsCount.value, equalTo("3 consents"))
    }

    @Test
    @Throws(Exception::class)
    fun setInitialDataCase2() {
        //when
        viewModel.setInitialData(Bundle())

        //then
        assertNull(viewModel.logoUrl.value)
        assertNull(viewModel.connectionTitle.value)
        assertThat(viewModel.consentsCount.value, equalTo(""))
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTestCase1() {
        //given
        viewModel.setInitialData(Bundle().apply { guid = "guid2" })
        viewModel.onFetchEncryptedDataResult(result = encryptedConsents, errors = emptyList())

        //when
        viewModel.onListItemClick(0)

        //then
        assertNotNull(viewModel.onListItemClickEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTestCase2() {
        //given empty lists
        viewModel.setInitialData(Bundle())
        viewModel.onFetchEncryptedDataResult(result = encryptedConsents, errors = emptyList())

        //when
        viewModel.onListItemClick(0)

        //then
        assertNull(viewModel.onListItemClickEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onActivityResultCase1() {
        val requestCode = CONSENT_REQUEST_CODE
        val resultCode = Activity.RESULT_OK
        val intent: Intent = Intent().putExtra(KEY_ID, "")

        viewModel.onActivityResult(
            requestCode = requestCode,
            resultCode = resultCode,
            data = intent
        )

        assertNull(viewModel.listItems.value)

        viewModel.onActivityResult(
            requestCode = requestCode,
            resultCode = Activity.RESULT_CANCELED,
            data = intent
        )

        assertNull(viewModel.listItems.value)

        viewModel.onActivityResult(
            requestCode = -1,
            resultCode = resultCode,
            data = intent
        )

        assertNull(viewModel.listItems.value)

        viewModel.onActivityResult(
            requestCode = requestCode,
            resultCode = resultCode,
            data = null
        )

        assertNull(viewModel.listItems.value)
    }

    @Test
    @Throws(Exception::class)
    fun onActivityResultCase2() {
        //given
        val requestCode = CONSENT_REQUEST_CODE
        val resultCode = Activity.RESULT_OK
        val intent: Intent = Intent().putExtra(KEY_ID, "guid2")

        val bundle = Bundle().apply {
            guid = "guid2"
        }
        viewModel.setInitialData(bundle)

        //when
        viewModel.onActivityResult(
            requestCode = requestCode,
            resultCode = resultCode,
            data = intent
        )

        //then
        assertThat(viewModel.listItems.value, equalTo(emptyList()))
    }

    @Test
    @Throws(Exception::class)
    fun onActivityResultCase3() {
        //given
        val requestCode = CONSENT_REQUEST_CODE
        val resultCode = Activity.RESULT_OK
        val intent: Intent = Intent().putExtra(KEY_ID, "555")
        viewModel.setInitialData(Bundle().apply { guid = "guid2" })
        viewModel.onFetchEncryptedDataResult(result = encryptedConsents, errors = emptyList())

        assertThat(viewModel.listItems.value?.size, equalTo(3))

        //when
        viewModel.onActivityResult(
            requestCode = requestCode,
            resultCode = resultCode,
            data = intent
        )

        //then
        assertThat(viewModel.listItems.value?.size, equalTo(2))
        assertThat(
            viewModel.listItems.value,
            equalTo(
                listOf(
                    ConsentItemViewModel(
                        id = "777",
                        tppName = "title",
                        consentTypeDescription = "Consent for future payment",
                        expiresAtDescription = spanned
                    ),
                    ConsentItemViewModel(
                        id = "999",
                        tppName = "title",
                        consentTypeDescription = "Consent for recurring payment",
                        expiresAtDescription = spanned
                    )
                )
            )
        )
    }

    @Test
    @Throws(Exception::class)
    fun onFetchEncryptedDataResultTestCase1() = runBlocking {
        //given not empty encrypted list
        val bundle = Bundle().apply { guid = "guid2" }
        viewModel.setInitialData(bundle)

        assertThat(encryptedConsents.size, equalTo(3))
        assertThat(viewModel.listItems.value!!.size, equalTo(0))

        //when
        viewModel.onFetchEncryptedDataResult(result = encryptedConsents, errors = emptyList())

        //then
        assertThat(
            viewModel.listItems.value,
            equalTo(listOf(
                ConsentItemViewModel(
                    id = "555",
                    tppName = "title",
                    consentTypeDescription = "Access to account information",
                    expiresAtDescription = spanned
                ),
                ConsentItemViewModel(
                    id = "777",
                    tppName = "title",
                    consentTypeDescription = "Consent for future payment",
                    expiresAtDescription = spanned
                ),
                ConsentItemViewModel(
                    id = "999",
                    tppName = "title",
                    consentTypeDescription = "Consent for recurring payment",
                    expiresAtDescription = spanned
                )
        )))
        assertThat(viewModel.consentsCount.value, equalTo("3 consents"))
    }

    @Test
    @Throws(Exception::class)
    fun onFetchEncryptedDataResultTestCase2() = runBlocking {
        //given empty encrypted list
        val bundle = Bundle().apply { guid = "guid2" }
        viewModel.setInitialData(bundle)

        //when
        viewModel.onFetchEncryptedDataResult(result = emptyList(), errors = emptyList())

        //then
        assertThat(viewModel.listItems.value, equalTo(emptyList()))
        assertThat(viewModel.consentsCount.value, equalTo(""))
    }
}
