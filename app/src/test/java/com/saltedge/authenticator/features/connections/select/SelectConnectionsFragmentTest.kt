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
package com.saltedge.authenticator.features.connections.select

import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.connections.common.ConnectionViewModel
import com.saltedge.authenticator.features.connections.select.SelectConnectionsFragment.Companion.KEY_CONNECTIONS
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SelectConnectionsFragmentTest {

    @Test
    @Throws(Exception::class)
    fun newInstanceTestCase() {
        val connections: List<ConnectionViewModel> = listOf(
            ConnectionViewModel(
                guid = "guid",
                code = "code",
                name = "name",
                logoUrl = "logoUrl",
                statusDescription = "statusDescription",
                statusColorResId = 1,
                reconnectOptionIsVisible = false,
                deleteMenuItemText = R.string.actions_delete,
                deleteMenuItemImage = R.drawable.ic_menu_delete_24dp,
                isChecked = false
            )
        )
        val arguments = SelectConnectionsFragment.newInstance(
            connections = connections
        ).arguments

        assertThat(arguments?.getSerializable(KEY_CONNECTIONS) as List<ConnectionViewModel>,
            equalTo(connections))
    }
}
