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
package com.saltedge.authenticator.features.connections.common

import com.saltedge.authenticator.R
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ConnectionOptionsTest {

    @Test
    @Throws(Exception::class)
    fun iconIdTest() {
        assertThat(ConnectionOptions.RECONNECT.iconId, equalTo(R.drawable.ic_reconnect_gray_24dp))
        assertThat(ConnectionOptions.RECONNECT.textId, equalTo(R.string.actions_reconnect))
        assertThat(
            ConnectionOptions.REPORT_PROBLEM.iconId,
            equalTo(R.drawable.ic_about_black54_24dp)
        )
        assertThat(
            ConnectionOptions.REPORT_PROBLEM.textId,
            equalTo(R.string.actions_contact_support)
        )
        assertThat(ConnectionOptions.RENAME.iconId, equalTo(R.drawable.ic_rename_gray_24dp))
        assertThat(ConnectionOptions.RENAME.textId, equalTo(R.string.actions_rename))
        assertThat(ConnectionOptions.DELETE.iconId, equalTo(R.drawable.ic_delete_gray_24dp))
        assertThat(ConnectionOptions.DELETE.textId, equalTo(R.string.actions_delete))
    }

    @Test
    @Throws(Exception::class)
    fun valuesTest() {
        assertThat(
            ConnectionOptions.values(), equalTo(
            arrayOf(
                ConnectionOptions.RECONNECT,
                ConnectionOptions.REPORT_PROBLEM,
                ConnectionOptions.RENAME,
                ConnectionOptions.DELETE
            )
        )
        )
    }

    @Test
    @Throws(Exception::class)
    fun valueOfTest() {
        assertThat(ConnectionOptions.valueOf("RECONNECT"), equalTo(ConnectionOptions.RECONNECT))
        assertThat(
            ConnectionOptions.valueOf("REPORT_PROBLEM"),
            equalTo(ConnectionOptions.REPORT_PROBLEM)
        )
        assertThat(ConnectionOptions.valueOf("RENAME"), equalTo(ConnectionOptions.RENAME))
        assertThat(ConnectionOptions.valueOf("DELETE"), equalTo(ConnectionOptions.DELETE))
    }
}
