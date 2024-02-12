/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.connections.options

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
