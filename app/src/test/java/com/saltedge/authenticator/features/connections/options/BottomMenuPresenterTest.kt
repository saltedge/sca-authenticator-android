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
package com.saltedge.authenticator.features.connections.options

import com.saltedge.authenticator.features.connections.common.ConnectionOptions
import com.saltedge.authenticator.features.menu.BottomMenuPresenter
import com.saltedge.authenticator.features.menu.MenuItemData
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.empty
import org.junit.Test

class BottomMenuPresenterTest {

    @Test
    @Throws(Exception::class)
    fun setInitialDataTest() {
        val presenter = BottomMenuPresenter()

        assertThat(presenter.menuId, `is`(nullValue()))
        assertThat(presenter.listItems, `is`(empty()))

        presenter.setInitialData(menuId = null, menuItems = null)

        assertThat(presenter.menuId, `is`(nullValue()))
        assertThat(presenter.listItems, `is`(empty()))

        presenter.setInitialData(menuId = "0", menuItems = listOf(MenuItemData(1, 2, 3)))

        assertThat(presenter.menuId, equalTo("0"))
        assertThat(presenter.listItems, equalTo(listOf(MenuItemData(1, 2, 3))))
    }
}
