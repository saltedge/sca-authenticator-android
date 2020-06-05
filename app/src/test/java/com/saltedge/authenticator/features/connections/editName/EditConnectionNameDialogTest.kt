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
package com.saltedge.authenticator.features.connections.editName

import com.saltedge.authenticator.app.KEY_GUID
import com.saltedge.authenticator.features.connections.edit.EditConnectionNameDialog
import com.saltedge.authenticator.sdk.constants.KEY_NAME
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EditConnectionNameDialogTest {

    @Test
    @Throws(Exception::class)
    fun newInstanceTest() {
        val arguments =
            EditConnectionNameDialog.newInstance(guid = "guid1", name = "Demobank").arguments!!

        assertThat(arguments.getString(KEY_GUID), equalTo("guid1"))
        assertThat(arguments.getString(KEY_NAME), equalTo("Demobank"))
    }
}
