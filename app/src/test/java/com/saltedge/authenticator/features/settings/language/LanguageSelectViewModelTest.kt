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
package com.saltedge.authenticator.features.settings.language

import com.saltedge.android.test_tools.ViewModelTest
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.TestAppTools
import junit.framework.TestCase.assertNull
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LanguageSelectViewModelTest : ViewModelTest() {

    private val mockPreferenceRepository = Mockito.mock(PreferenceRepositoryAbs::class.java)
    private lateinit var viewModel: LanguageSelectViewModel

    @Before
    fun setUp() {
        viewModel = LanguageSelectViewModel(TestAppTools.applicationContext, mockPreferenceRepository)
    }

    @Test
    @Throws(Exception::class)
    fun listItemsTest() {
        assertThat(viewModel.listItems, equalTo(arrayOf("English")))
    }

    @Test
    @Throws(Exception::class)
    fun selectedItemIndexTest() {
        assertThat(viewModel.selectedItemIndex, equalTo(0))
    }

    @Test
    @Throws(Exception::class)
    fun onOkClickTest() {//TODO add more cases with onLanguageChangedEvent when new languages will appear
        viewModel.onOkClick()

        assertNull(viewModel.onLanguageChangedEvent.value)
        assertThat(viewModel.onCloseEvent.value, equalTo(ViewModelEvent(Unit)))
    }

    @Test
    @Throws(Exception::class)
    fun onCancelClickTest() {
        viewModel.onCancelClick()

        assertThat(viewModel.onCloseEvent.value, equalTo(ViewModelEvent(Unit)))
    }
}
