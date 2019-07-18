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
package com.saltedge.authenticator.unitTests.features.connections.options

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.saltedge.authenticator.features.connections.common.ConnectionOptions
import com.saltedge.authenticator.features.connections.options.ConnectionOptionsPresenter
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConnectionOptionsPresenterTest {

    @Test
    @Throws(Exception::class)
    fun setInitialDataTest() {
        val presenter = ConnectionOptionsPresenter()
        presenter.setInitialData(optionsIds = intArrayOf(ConnectionOptions.RENAME.ordinal))

        assertThat(listOf(ConnectionOptions.RENAME), equalTo(presenter.listItems))

        presenter.setInitialData(optionsIds = intArrayOf(ConnectionOptions.RECONNECT.ordinal))

        assertThat(listOf(ConnectionOptions.RECONNECT), equalTo(presenter.listItems))

        presenter.setInitialData(optionsIds = intArrayOf(ConnectionOptions.DELETE.ordinal))

        assertThat(listOf(ConnectionOptions.DELETE), equalTo(presenter.listItems))

        presenter.setInitialData(optionsIds = intArrayOf(ConnectionOptions.REPORT_PROBLEM.ordinal))

        assertThat(listOf(ConnectionOptions.REPORT_PROBLEM), equalTo(presenter.listItems))
    }
}