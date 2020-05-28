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
import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.settings.common.SettingsItemModel
import junit.framework.TestCase.assertNull
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LicensesViewModelTest {
    private val apache2LicenseLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    private lateinit var viewModel: LicensesViewModel

    @Before
    fun setUp() {
        viewModel = LicensesViewModel()
    }

    @Test
    @Throws(Exception::class)
    fun getListItemsTest() {
        assertThat(
            viewModel.listItems,
            equalTo(listOf(
                SettingsItemModel(titleId = R.string.library_realm),
                SettingsItemModel(titleId = R.string.library_dagger),
                SettingsItemModel(titleId = R.string.library_compat),
                SettingsItemModel(titleId = R.string.library_constraint),
                SettingsItemModel(titleId = R.string.library_material),
                SettingsItemModel(titleId = R.string.library_retrofit),
                SettingsItemModel(titleId = R.string.library_okhttp),
                SettingsItemModel(titleId = R.string.library_joda),
                SettingsItemModel(titleId = R.string.library_glide),
                SettingsItemModel(titleId = R.string.library_junit),
                SettingsItemModel(titleId = R.string.library_jacoco),
                SettingsItemModel(titleId = R.string.library_hamcrest),
                SettingsItemModel(titleId = R.string.library_mockito),
                SettingsItemModel(titleId = R.string.library_mockk),
                SettingsItemModel(titleId = R.string.library_jsr),
                SettingsItemModel(titleId = R.string.library_ktlint),
                SettingsItemModel(titleId = R.string.library_jlleitschuh),
                SettingsItemModel(titleId = R.string.library_blur)
            ))
        )
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTestCase1() {
        //when
        val testResult = ArrayList<Pair<Int, String>>()
        viewModel.licenseItemClickEvent.observeForever(Observer{
            testResult.add(it.peekContent())
        })
        viewModel.listItems.forEach { viewModel.onListItemClick(itemId = it.titleId) }

        //then
        assertThat(
            testResult,
            equalTo(listOf<Pair<Int, String>>(
                Pair(R.string.library_realm, apache2LicenseLink),
                Pair(R.string.library_dagger, apache2LicenseLink),
                Pair(R.string.library_compat, apache2LicenseLink),
                Pair(R.string.library_constraint, apache2LicenseLink),
                Pair(R.string.library_material, apache2LicenseLink),
                Pair(R.string.library_retrofit, apache2LicenseLink),
                Pair(R.string.library_okhttp, apache2LicenseLink),
                Pair(R.string.library_joda, apache2LicenseLink),
                Pair(R.string.library_glide, "https://raw.githubusercontent.com/bumptech/glide/master/LICENSE"),
                Pair(R.string.library_junit, "https://junit.org/junit4/license.html"),
                Pair(R.string.library_jacoco, "https://www.jacoco.org/jacoco/trunk/doc/license.html"),
                Pair(R.string.library_hamcrest, "https://raw.githubusercontent.com/hamcrest/JavaHamcrest/master/LICENSE.txt"),
                Pair(R.string.library_mockito, "https://raw.githubusercontent.com/mockito/mockito/release/2.x/LICENSE"),
                Pair(R.string.library_mockk, apache2LicenseLink),
                Pair(R.string.library_jsr, "https://raw.githubusercontent.com/findbugsproject/findbugs/master/findbugs/licenses/LICENSE-jsr305.txt"),
                Pair(R.string.library_ktlint, "https://raw.githubusercontent.com/pinterest/ktlint/master/LICENSE"),
                Pair(R.string.library_jlleitschuh, "https://raw.githubusercontent.com/JLLeitschuh/ktlint-gradle/master/LICENSE.txt"),
                Pair(R.string.library_blur, "https://raw.githubusercontent.com/500px/500px-android-blur/master/LICENSE.txt")
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
