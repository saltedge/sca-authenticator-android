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
package com.saltedge.authenticator.features.settings.about

import android.os.Bundle
import androidx.lifecycle.Observer
import com.saltedge.authenticator.R
import com.saltedge.authenticator.events.ViewModelEvent
import com.saltedge.authenticator.features.settings.about.common.AboutListItemViewModel
import com.saltedge.authenticator.testTools.TestAppTools
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AboutViewModelTest {

    private lateinit var viewModel: AboutViewModel

    @Before
    fun setUp() {
        viewModel = AboutViewModel(TestAppTools.applicationContext)
    }

    @Test
    @Throws(Exception::class)
    fun onTitleClickTestCase1() {
        val showEventObserver: Observer<ViewModelEvent<Bundle>> = mock()
        viewModel.termsOfServiceItemClickEvent.observeForever(showEventObserver)

        viewModel.onTitleClick(R.string.about_terms_service)

        assertNotNull(this.viewModel.termsOfServiceItemClickEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onTitleClickTestCase2() {
        val showEventObserver: Observer<ViewModelEvent<Unit>> = mock()
        viewModel.licenseItemClickEvent.observeForever(showEventObserver)

        viewModel.onTitleClick(R.string.about_open_source_licenses)

        assertNotNull(this.viewModel.licenseItemClickEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onTitleClickTestCase3() {
        val showEventObserver: Observer<ViewModelEvent<Unit>> = mock()
        viewModel.licenseItemClickEvent.observeForever(showEventObserver)

        viewModel.onTitleClick(-1)

        assertNull(this.viewModel.termsOfServiceItemClickEvent.value)
        assertNull(this.viewModel.licenseItemClickEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun getListItemsTest() {
        assertThat(
            viewModel.getListItems(), equalTo(
            listOf(
                AboutListItemViewModel(
                    titleId = R.string.about_app_version,
                    value = "2.3.2"
                ),
                AboutListItemViewModel(
                    titleId = R.string.about_copyright,
                    value = TestAppTools.getString(R.string.about_copyright_description)
                ),
                AboutListItemViewModel(
                    titleId = R.string.about_terms_service
                ),
                AboutListItemViewModel(
                    titleId = R.string.about_open_source_licenses
                )
            )
        )
        )
    }

    private fun <T> mock(): Observer<ViewModelEvent<T>> {
        return mock(Observer::class.java) as Observer<ViewModelEvent<T>>
    }
}
