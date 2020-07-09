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
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.CONSENT_REQUEST_CODE
import com.saltedge.authenticator.app.KEY_ID
import com.saltedge.authenticator.features.consents.common.countOfDays
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.constants.KEY_DATA
import com.saltedge.authenticator.sdk.model.ConsentData
import com.saltedge.authenticator.sdk.model.ConsentSharedData
import com.saltedge.authenticator.sdk.model.connection.ConnectionAndKey
import com.saltedge.authenticator.sdk.model.connection.ConnectionStatus
import com.saltedge.authenticator.sdk.tools.crypt.CryptoToolsAbs
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManagerAbs
import com.saltedge.authenticator.tools.daysTillExpire
import com.saltedge.authenticator.tools.guid
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
import java.security.PrivateKey

@RunWith(RobolectricTestRunner::class)
class ConsentsListViewModelTest {

    private lateinit var viewModel: ConsentsListViewModel
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val mockConnectionsRepository = mock(ConnectionsRepositoryAbs::class.java)
    private val mockKeyStoreManager = mock(KeyStoreManagerAbs::class.java)
    private val mockApiManager = mock(AuthenticatorApiManagerAbs::class.java)
    private val mockPrivateKey = Mockito.mock(PrivateKey::class.java)
    private val mockCryptoTools = mock(CryptoToolsAbs::class.java)
    private val connection = Connection().apply {
        guid = "guid2"
        code = "demobank2"
        name = "Demobank2"
        status = "${ConnectionStatus.ACTIVE}"
        accessToken = "token2"
        createdAt = 300L
        updatedAt = 300L
        logoUrl = "https://www.fentury.com/"
    }
    private val mockConnectionAndKey = ConnectionAndKey(connection, mockPrivateKey)
    private val consentData: List<ConsentData> = listOf(
        ConsentData(
            id = "555",
            userId = "1",
            tppName = "title",
            consentTypeString = "aisp",
            accounts = emptyList(),
            expiresAt = DateTime(0).withZone(DateTimeZone.UTC),
            createdAt = DateTime(0).withZone(DateTimeZone.UTC),
            sharedData = ConsentSharedData(balance = true, transactions = true)
        ),
        ConsentData(
            id = "777",
            userId = "1",
            tppName = "title",
            consentTypeString = "aisp",
            accounts = emptyList(),
            expiresAt = DateTime(0).withZone(DateTimeZone.UTC),
            createdAt = DateTime(0).withZone(DateTimeZone.UTC),
            sharedData = ConsentSharedData(balance = true, transactions = true)
        )
    )

    private val daysLeftCount = DateTime(0).withZone(DateTimeZone.UTC).daysTillExpire()
    private val daysTillExpireDescription = countOfDays(daysLeftCount, context)
    private val spanned = SpannableStringBuilder(
        "${context.getString(R.string.expires_in)} $daysTillExpireDescription"
    )

    @Before
    fun setUp() {
        Mockito.doReturn(connection).`when`(mockConnectionsRepository).getByGuid("guid2")
        given(mockConnectionsRepository.getByGuid("guid2")).willReturn(connection)
        given(mockKeyStoreManager.createConnectionAndKeyModel(connection)).willReturn(
            mockConnectionAndKey
        )

        viewModel = ConsentsListViewModel(
            appContext = context,
            connectionsRepository = mockConnectionsRepository,
            keyStoreManager = mockKeyStoreManager,
            apiManager = mockApiManager,
            cryptoTools = mockCryptoTools
        )
    }

    @Test
    @Throws(Exception::class)
    fun onReceivedNewConsentsCase1() {
        //when
        viewModel.onReceivedNewConsents(result = emptyList())

        //then
        assertThat(viewModel.listItems.value, equalTo(emptyList()))
        assertThat(viewModel.consentsCount.value, equalTo(""))

    }

    @Test
    @Throws(Exception::class)
    fun onReceivedNewConsentsTestCase2() {
        //when
        viewModel.onReceivedNewConsents(consentData)

        //then
        assertThat(viewModel.listItems.value?.map { it.id }, equalTo(listOf("555", "777")))
        assertThat(viewModel.consentsCount.value, equalTo("2 consents"))
    }

    @Test
    @Throws(Exception::class)
    fun toConsentTypeDescriptionTest() {
        //when
        viewModel.onReceivedNewConsents(consentData)

        //then
        assertThat(viewModel.listItems.value?.map { it.id }, equalTo(listOf("555", "777")))

        //when
        val consentDataWithTypePisp: ConsentData = consentData[0].also {
            it.consentTypeString = "pisp_future"
        }
        viewModel.onReceivedNewConsents(listOf(consentDataWithTypePisp))

        //then
        assertThat(
            viewModel.listItems.value,
            equalTo(
                listOf(
                    ConsentItemViewModel(
                        id = "555",
                        tppName = "title",
                        consentTypeDescription = "Consent for future payment",
                        expiresAtDescription = spanned
                    )
                )
            )
        )

        //when
        val consentDataWithTypePispRecurring: ConsentData = consentData[0].also {
            it.consentTypeString = "pisp_recurring"
        }
        viewModel.onReceivedNewConsents(listOf(consentDataWithTypePispRecurring))

        //then
        assertThat(
            viewModel.listItems.value,
            equalTo(
                listOf(
                    ConsentItemViewModel(
                        id = "555",
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
    fun refreshConsentsTestCase1() {
        //given
        val bundle = Bundle().apply { guid = "guid2" }
        viewModel.setInitialData(bundle)

        //when
        viewModel.refreshConsents()

        //then
        Mockito.verify(mockApiManager).getConsents(
            connectionsAndKeys = listOf(ConnectionAndKey(connection, mockPrivateKey)),
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
            putSerializable(KEY_DATA, ArrayList<ConsentData>(consentData))
        }

        //when
        viewModel.setInitialData(bundle)

        //then
        assertThat(viewModel.logoUrl.value, equalTo("https://www.fentury.com/"))
        assertThat(viewModel.connectionTitle.value, equalTo("Demobank2"))
        assertThat(viewModel.consentsCount.value, equalTo("2 consents"))
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
        val bundle = Bundle().apply { guid = "guid2" }
        viewModel.setInitialData(bundle)
        viewModel.onReceivedNewConsents(consentData)

        //when
        viewModel.onListItemClick(0)

        //then
        assertNotNull(viewModel.onListItemClickEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTestCase2() {
        //given
        viewModel.setInitialData(Bundle())
        viewModel.onReceivedNewConsents(consentData)

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

        val bundle = Bundle().apply {
            guid = "guid2"
        }
        viewModel.setInitialData(bundle)
        viewModel.onReceivedNewConsents(consentData)

        assertThat(viewModel.listItems.value?.size, equalTo(2))

        //when
        viewModel.onActivityResult(
            requestCode = requestCode,
            resultCode = resultCode,
            data = intent
        )

        //then
        assertThat(viewModel.listItems.value?.size, equalTo(1))
        assertThat(
            viewModel.listItems.value,
            equalTo(
                listOf(
                    ConsentItemViewModel(
                        id = "777",
                        tppName = "title",
                        consentTypeDescription = "Access to account information",
                        expiresAtDescription = spanned
                    )
                )
            )
        )
    }
}
