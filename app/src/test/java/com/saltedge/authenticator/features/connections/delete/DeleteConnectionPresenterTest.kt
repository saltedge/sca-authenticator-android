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
package com.saltedge.authenticator.features.connections.delete

import android.content.DialogInterface
import android.content.Intent
import com.saltedge.authenticator.app.KEY_GUID
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DeleteConnectionPresenterTest : DeleteConnectionContract.View {

    private var dismissViewCallback: Boolean = false
    private var resultIntentCallback: Intent? = null

    override fun dismissView() {
        this.dismissViewCallback = true
    }

    override fun setResultOk(resultIntent: Intent) {
        this.resultIntentCallback = resultIntent
    }

    @Before
    fun setUp() {
        dismissViewCallback = false
        resultIntentCallback = null
    }

    @Test
    @Throws(Exception::class)
    fun getViewContractTest() {
        val presenter = DeleteConnectionPresenter(viewContract = this)

        assertNull(presenter.guid)
        assertNotNull(presenter.viewContract)
    }

    @Test
    @Throws(Exception::class)
    fun onActionViewClickTestCase1() {
        val presenter = DeleteConnectionPresenter(viewContract = this)

        presenter.onActionViewClick(DialogInterface.BUTTON_NEGATIVE)

        assertTrue(dismissViewCallback)
        assertNull(resultIntentCallback)
    }

    @Test
    @Throws(Exception::class)
    fun onActionViewClickTestCase2() {
        val presenter = DeleteConnectionPresenter(viewContract = this)
        presenter.guid = "test"

        presenter.onActionViewClick(DialogInterface.BUTTON_POSITIVE)

        assertTrue(dismissViewCallback)
        assertThat(resultIntentCallback!!.getStringExtra(KEY_GUID), equalTo("test"))
    }
}
