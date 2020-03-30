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
package com.saltedge.authenticator.features.settings.mvvm.about

import com.saltedge.authenticator.R
import com.saltedge.authenticator.testTools.TestAppTools
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AboutViewModelTest {

    private lateinit var viewModel: AboutViewModel

    @Test
    @Throws(Exception::class)
    fun getListItemsTest() {
        viewModel = AboutViewModel(TestAppTools.applicationContext)

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
}
