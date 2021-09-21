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

import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.settings.common.SettingsItemViewModel
import com.saltedge.authenticator.TestAppTools
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
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
    fun onListItemClickTestCase1() {
        //given
        val itemId = R.string.about_terms_service

        //when
        viewModel.onListItemClick(itemId = itemId)

        //then
        assertNotNull(viewModel.termsOfServiceItemClickEvent.value)
        assertNull(this.viewModel.licenseItemClickEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTestCase2() {
        //given
        val itemId = R.string.about_open_source_licenses

        //when
        viewModel.onListItemClick(itemId = itemId)

        //then
        assertNull(viewModel.termsOfServiceItemClickEvent.value)
        assertNotNull(this.viewModel.licenseItemClickEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTestCase3() {
        //given
        val itemId = R.string.authorization_feature_title

        //when
        viewModel.onListItemClick(itemId = itemId)

        //then
        assertNull(this.viewModel.termsOfServiceItemClickEvent.value)
        assertNull(this.viewModel.licenseItemClickEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun listItemsTest() {
        assertThat(
            viewModel.listItems,
            equalTo(listOf(
                SettingsItemViewModel(
                    titleId = R.string.about_app_version,
                    description = "3.2.1"
                ),
                SettingsItemViewModel(
                    titleId = R.string.about_copyright,
                    description = TestAppTools.getString(R.string.about_copyright_description)
                ),
                SettingsItemViewModel(titleId = R.string.about_terms_service),
                SettingsItemViewModel(titleId = R.string.about_open_source_licenses)
            ))
        )
    }
}
