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

import android.content.Context
import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.test.core.app.ApplicationProvider
import com.saltedge.android.test_tools.ViewModelTest
import com.saltedge.authenticator.R
import com.saltedge.authenticator.TestFactory
import com.saltedge.authenticator.app.guid
import com.saltedge.authenticator.core.api.KEY_DATA
import com.saltedge.authenticator.core.api.model.ConsentData
import com.saltedge.authenticator.features.consents.common.countOfDays
import com.saltedge.authenticator.tools.daysTillExpire
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import java.lang.ref.WeakReference

@RunWith(RobolectricTestRunner::class)
class ConsentsListViewModelTest : ViewModelTest() {

    private lateinit var viewModel: ConsentsListViewModel
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val mockInteractor = mock(ConsentsListInteractorAbs::class.java)

    private val daysLeftCount = DateTime(0).withZone(DateTimeZone.UTC).daysTillExpire()
    private val daysTillExpireDescription = countOfDays(daysLeftCount, context)
    private val spanned = SpannableStringBuilder(
        "${context.getString(R.string.expires_in)} $daysTillExpireDescription"
    )

    @Before
    fun setUp() {
        viewModel = ConsentsListViewModel(
            weakContext = WeakReference(context),
            interactor = mockInteractor
        )
    }

    @Test
    @Throws(Exception::class)
    fun setInitialDataCase1() {
        //given
        val bundle = Bundle().apply {
            guid = TestFactory.connection1.guid
            putSerializable(KEY_DATA, ArrayList<ConsentData>(TestFactory.v1Consents))
        }
        given(mockInteractor.updateConnection(TestFactory.connection1.guid))
            .willReturn(TestFactory.connection1)

        //when
        viewModel.setInitialData(bundle)

        //then
        assertThat(viewModel.logoUrlData.value, equalTo("https://www.fentury.com/"))
        assertThat(viewModel.connectionTitleData.value, equalTo("Demobank1"))
        verify(mockInteractor).updateConnection(TestFactory.connection1.guid)
        verify(mockInteractor).onNewConsentsReceived(TestFactory.v1Consents)
    }

    @Test
    @Throws(Exception::class)
    fun setInitialDataCase2() {
        //given
        given(mockInteractor.updateConnection(null)).willReturn(null)

        //when
        viewModel.setInitialData(Bundle())

        //then
        Assert.assertNull(viewModel.logoUrlData.value)
        Assert.assertNull(viewModel.connectionTitleData.value)
    }

    @Test
    @Throws(Exception::class)
    fun refreshConsentsTest() {
        //when
        viewModel.refreshConsents()

        //then
        Mockito.verify(mockInteractor).updateConsents()
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTestCase1() {
        //given
        viewModel.onDatasetChanged(TestFactory.v1Consents)
        given(mockInteractor.getConsent(TestFactory.v1Consents.first().id))
            .willReturn(TestFactory.v1Consents.first())

        //when
        viewModel.onListItemClick(0)

        //then
        Assert.assertNotNull(viewModel.onListItemClickEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTestCase2() {
        //given empty lists

        //when
        viewModel.onListItemClick(0)

        //then
        Assert.assertNull(viewModel.onListItemClickEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTestCase3() {
        //given empty lists
        viewModel.onDatasetChanged(TestFactory.v1Consents)
        given(mockInteractor.getConsent(TestFactory.v1Consents.first().id))
            .willReturn(null)

        //when
        viewModel.onListItemClick(0)

        //then
        Assert.assertNull(viewModel.onListItemClickEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onRevokeConsentTest() {
        //given empty encrypted list
        viewModel.onDatasetChanged(TestFactory.v1Consents)
        given(mockInteractor.removeConsent(TestFactory.v1Consents.first().id))
            .willReturn(TestFactory.v1Consents.first())

        //when
        viewModel.onRevokeConsent(TestFactory.v1Consents.first().id)

        //then
        assertThat(
            viewModel.onConsentRemovedEvent.value!!.peekContent(),
            equalTo("Consent revoked for tppName111")
        )
    }

    @Test
    @Throws(Exception::class)
    fun onDatasetChangedTest() {
        //given not empty encrypted list

        //when
        viewModel.onDatasetChanged(TestFactory.v1Consents)

        //then
        assertThat(
            viewModel.listItems.value,
            equalTo(listOf(
                ConsentItem(
                    id = "111",
                    tppName = "tppName111",
                    consentTypeDescription = "Access to account information",
                    expiresAtDescription = spanned
                ),
                ConsentItem(
                    id = "112",
                    tppName = "tppName112",
                    consentTypeDescription = "Consent for future payment",
                    expiresAtDescription = spanned
                ),
                ConsentItem(
                    id = "113",
                    tppName = "tppName113",
                    consentTypeDescription = "Consent for recurring payment",
                    expiresAtDescription = spanned
                )
        )))
        assertThat(viewModel.consentsCount.value, equalTo("3 consents"))
    }
}
