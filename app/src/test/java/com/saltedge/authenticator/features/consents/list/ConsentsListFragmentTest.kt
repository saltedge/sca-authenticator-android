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

import android.os.Bundle
import com.saltedge.authenticator.app.KEY_GUID
import com.saltedge.authenticator.sdk.constants.KEY_DATA
import com.saltedge.authenticator.sdk.model.ConsentData
import com.saltedge.authenticator.sdk.model.ConsentSharedData
import org.hamcrest.CoreMatchers.equalTo
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Assert.assertNull
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ConsentsListFragmentTest {

    @Test
    @Throws(Exception::class)
    fun newInstanceTestCase1() {
        val arguments = ConsentsListFragment.newInstance(Bundle().apply {
            putString(KEY_GUID, "guid1")
            putSerializable(
                KEY_DATA, ConsentData(
                id = "555",
                userId = "1",
                tppName = "title",
                consentTypeString = "aisp",
                accounts = emptyList(),
                expiresAt = DateTime(0).withZone(DateTimeZone.UTC),
                createdAt = DateTime(0).withZone(DateTimeZone.UTC),
                sharedData = ConsentSharedData(balance = true, transactions = true)
            )
            )
        }).arguments!!

        assertThat(arguments.getString(KEY_GUID), equalTo("guid1"))
        assertThat(
            arguments.getSerializable(KEY_DATA) as ConsentData,
            equalTo(
                ConsentData(
                    id = "555",
                    userId = "1",
                    tppName = "title",
                    consentTypeString = "aisp",
                    accounts = emptyList(),
                    expiresAt = DateTime(0).withZone(DateTimeZone.UTC),
                    createdAt = DateTime(0).withZone(DateTimeZone.UTC),
                    sharedData = ConsentSharedData(balance = true, transactions = true)
                )
            )
        )
    }

    @Test
    @Throws(Exception::class)
    fun newInstanceTestCase2() {
        val arguments = ConsentsListFragment.newInstance(Bundle().apply {
            putString(KEY_GUID, "guid1")
        }).arguments!!

        assertThat(arguments.getString(KEY_GUID), equalTo("guid1"))
        assertNull(arguments.getSerializable(KEY_DATA))
    }
}
