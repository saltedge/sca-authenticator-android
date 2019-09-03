/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2019 Salt Edge Inc.
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
package com.saltedge.authenticator.features.authorizations.details

import com.saltedge.authenticator.features.authorizations.common.AuthorizationViewModel
import com.saltedge.authenticator.sdk.constants.KEY_DATA
import org.hamcrest.CoreMatchers.equalTo
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AuthorizationDetailsFragmentTest {

    @Test
    @Throws(Exception::class)
    fun newInstanceTest() {
        val createdAt = DateTime.now(DateTimeZone.UTC)
        val model = AuthorizationViewModel(
            authorizationId = "1",
            authorizationCode = "111",
            expiresAt = createdAt.plusMinutes(3),
            title = "title1",
            description = "desc1",
            validSeconds = 300,
            connectionId = "1",
            connectionName = "Demobank",
            connectionLogoUrl = null,
            isProcessing = false
        )
        val fragment = AuthorizationDetailsFragment.newInstance(model)
        val data = fragment.arguments!!.getSerializable(KEY_DATA) as? AuthorizationViewModel

        assertThat(data, equalTo(model))
    }
}
