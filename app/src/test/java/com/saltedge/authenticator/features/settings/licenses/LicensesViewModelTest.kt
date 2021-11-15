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
package com.saltedge.authenticator.features.settings.licenses

import androidx.lifecycle.Observer
import com.saltedge.android.test_tools.ViewModelTest
import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.settings.common.SettingsItemViewModel
import com.saltedge.authenticator.TestAppTools
import com.saltedge.authenticator.core.api.KEY_TITLE
import com.saltedge.authenticator.widget.fragment.WebViewFragment
import junit.framework.TestCase.assertNull
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LicensesViewModelTest : ViewModelTest() {
    private val apache2LicenseLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    private lateinit var viewModel: LicensesViewModel

    @Before
    fun setUp() {
        viewModel = LicensesViewModel(TestAppTools.applicationContext)
    }

    @Test
    @Throws(Exception::class)
    fun getListItemsTest() {
        assertThat(
            viewModel.listItems,
            equalTo(listOf(
                SettingsItemViewModel(titleId = R.string.library_realm),
                SettingsItemViewModel(titleId = R.string.library_dagger),
                SettingsItemViewModel(titleId = R.string.library_compat),
                SettingsItemViewModel(titleId = R.string.library_constraint),
                SettingsItemViewModel(titleId = R.string.library_material),
                SettingsItemViewModel(titleId = R.string.library_retrofit),
                SettingsItemViewModel(titleId = R.string.library_okhttp),
                SettingsItemViewModel(titleId = R.string.library_joda),
                SettingsItemViewModel(titleId = R.string.library_glide),
                SettingsItemViewModel(titleId = R.string.library_junit),
                SettingsItemViewModel(titleId = R.string.library_jacoco),
                SettingsItemViewModel(titleId = R.string.library_hamcrest),
                SettingsItemViewModel(titleId = R.string.library_mockito),
                SettingsItemViewModel(titleId = R.string.library_mockk),
                SettingsItemViewModel(titleId = R.string.library_jsr),
                SettingsItemViewModel(titleId = R.string.library_ktlint),
                SettingsItemViewModel(titleId = R.string.library_jlleitschuh),
                SettingsItemViewModel(titleId = R.string.library_blur),
                SettingsItemViewModel(titleId = R.string.library_timber)
            ))
        )
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTestCase1() {
        //when
        val testResult = ArrayList<Pair<String, String>>()
        viewModel.licenseItemClickEvent.observeForever(Observer{
            val title = it.peekContent().getString(KEY_TITLE)!!
            val url = it.peekContent().getString(WebViewFragment.KEY_URL)!!
            testResult.add(Pair(title, url))
        })
        viewModel.listItems.forEach { viewModel.onListItemClick(itemId = it.titleId) }

        //then
        assertThat(
            testResult,
            equalTo(listOf<Pair<String, String>>(
                Pair(TestAppTools.getString(R.string.library_realm), apache2LicenseLink),
                Pair(TestAppTools.getString(R.string.library_dagger), apache2LicenseLink),
                Pair(TestAppTools.getString(R.string.library_compat), apache2LicenseLink),
                Pair(TestAppTools.getString(R.string.library_constraint), apache2LicenseLink),
                Pair(TestAppTools.getString(R.string.library_material), apache2LicenseLink),
                Pair(TestAppTools.getString(R.string.library_retrofit), apache2LicenseLink),
                Pair(TestAppTools.getString(R.string.library_okhttp), apache2LicenseLink),
                Pair(TestAppTools.getString(R.string.library_joda), apache2LicenseLink),
                Pair(TestAppTools.getString(R.string.library_glide), "https://raw.githubusercontent.com/bumptech/glide/master/LICENSE"),
                Pair(TestAppTools.getString(R.string.library_junit), "https://junit.org/junit4/license.html"),
                Pair(TestAppTools.getString(R.string.library_jacoco), "https://www.jacoco.org/jacoco/trunk/doc/license.html"),
                Pair(TestAppTools.getString(R.string.library_hamcrest), "https://raw.githubusercontent.com/hamcrest/JavaHamcrest/master/LICENSE.txt"),
                Pair(TestAppTools.getString(R.string.library_mockito), "https://raw.githubusercontent.com/mockito/mockito/release/2.x/LICENSE"),
                Pair(TestAppTools.getString(R.string.library_mockk), apache2LicenseLink),
                Pair(TestAppTools.getString(R.string.library_jsr), "https://raw.githubusercontent.com/findbugsproject/findbugs/master/findbugs/licenses/LICENSE-jsr305.txt"),
                Pair(TestAppTools.getString(R.string.library_ktlint), "https://raw.githubusercontent.com/pinterest/ktlint/master/LICENSE"),
                Pair(TestAppTools.getString(R.string.library_jlleitschuh), "https://raw.githubusercontent.com/JLLeitschuh/ktlint-gradle/master/LICENSE.txt"),
                Pair(TestAppTools.getString(R.string.library_blur), "https://raw.githubusercontent.com/500px/500px-android-blur/master/LICENSE.txt"),
                Pair(TestAppTools.getString(R.string.library_timber), "https://raw.githubusercontent.com/JakeWharton/timber/trunk/LICENSE.txt")
            ))
        )
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTestCase2() {
        viewModel.onListItemClick(itemId = R.string.app_name)

        assertNull(viewModel.licenseItemClickEvent.value)
    }
}
