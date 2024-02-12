/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.settings.about

import com.saltedge.android.test_tools.ViewModelTest
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
class AboutViewModelTest : ViewModelTest() {

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
                    description = "3.6.1"
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
