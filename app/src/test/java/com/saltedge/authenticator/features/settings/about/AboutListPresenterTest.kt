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
package com.saltedge.authenticator.features.settings.about

import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.settings.common.SettingsItemViewModel
import com.saltedge.authenticator.sdk.constants.TERMS_LINK
import com.saltedge.authenticator.testTools.TestAppTools
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AboutListPresenterTest {

    @Test
    @Throws(Exception::class)
    fun constructorTest() {
        Assert.assertNull(createPresenter(viewContract = null).viewContract)
        Assert.assertNotNull(createPresenter(viewContract = mockView).viewContract)
    }

    @Test
    @Throws(Exception::class)
    fun getListItemsTest() {
        val presenter = createPresenter(viewContract = mockView)

        assertThat(
            presenter.getListItems(), equalTo(
            listOf(
                SettingsItemViewModel(
                    titleId = R.string.about_app_version,
                    value = "2.3.2"
                ),
                SettingsItemViewModel(
                    titleId = R.string.about_copyright,
                    value = TestAppTools.getString(R.string.about_copyright_description)
                ),
                SettingsItemViewModel(
                    titleId = R.string.about_terms_service,
                    itemIsClickable = true
                ),
                SettingsItemViewModel(
                    titleId = R.string.about_open_source_licenses,
                    itemIsClickable = true
                )
            )
        )
        )
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTest_terms() {
        val presenter = createPresenter(viewContract = null)
        presenter.onListItemClick(itemViewId = R.string.about_terms_service)

        Mockito.verifyNoMoreInteractions(mockView)

        presenter.viewContract = mockView
        presenter.onListItemClick(itemViewId = R.string.about_terms_service)

        Mockito.verify(mockView).openLink(url = TERMS_LINK, titleId = R.string.about_terms_service)
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTest_licenses() {
        val presenter = createPresenter(viewContract = null)
        presenter.onListItemClick(itemViewId = R.string.about_open_source_licenses)

        Mockito.verifyNoMoreInteractions(mockView)

        presenter.viewContract = mockView
        presenter.onListItemClick(itemViewId = R.string.about_open_source_licenses)

        Mockito.verify(mockView).openLicensesList()
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTest_invalidParams() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.onListItemClick(itemViewId = -1)

        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    @Throws(Exception::class)
    fun onListItemCheckedStateChangedTest() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.onListItemCheckedStateChanged(itemId = -1, checked = true)

        Mockito.verifyNoMoreInteractions(mockView)
    }

    private val mockView = Mockito.mock(AboutListContract.View::class.java)

    private fun createPresenter(viewContract: AboutListContract.View? = null): AboutListPresenter {
        return AboutListPresenter(TestAppTools.applicationContext)
            .apply { this.viewContract = viewContract }
    }
}
