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
package com.saltedge.authenticator.features.settings.list

import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.settings.common.CheckedTitleValueViewModel
import com.saltedge.authenticator.model.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.testTools.TestAppTools
import com.saltedge.authenticator.tool.secure.fingerprint.BiometricToolsAbs
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SettingsListPresenterTest {

    @Test
    @Throws(Exception::class)
    fun constructorTest() {
        Assert.assertNull(createPresenter(viewContract = null).viewContract)
        Assert.assertNotNull(createPresenter(viewContract = mockView).viewContract)
    }

    @Test
    @Throws(Exception::class)
    fun getListItemsTest() {
        val notificationsEnabled = true
        val screenshotLockEnabled = true
        Mockito.doReturn(notificationsEnabled).`when`(mockPreferences).notificationsEnabled
        Mockito.doReturn(screenshotLockEnabled).`when`(mockPreferences).screenshotLockEnabled
        val presenter = createPresenter(viewContract = mockView)

        assertThat(
            presenter.getListItems(), equalTo(
            listOf(
                CheckedTitleValueViewModel(
                    titleId = R.string.settings_passcode,
                    value = TestAppTools.getString(R.string.settings_passcode_description),
                    itemIsClickable = true
                ),
                CheckedTitleValueViewModel(
                    titleId = R.string.settings_notifications,
                    switchEnabled = true,
                    isChecked = notificationsEnabled
                ),
                CheckedTitleValueViewModel(
                    titleId = R.string.settings_screenshot_lock,
                    switchEnabled = true,
                    isChecked = screenshotLockEnabled
                ),
                CheckedTitleValueViewModel(
                    titleId = R.string.about_feature_title,
                    itemIsClickable = true
                ),
                CheckedTitleValueViewModel(
                    titleId = R.string.settings_report_bug,
                    itemIsClickable = true
                )
            )
        )
        )
    }

    @Test
    @Throws(Exception::class)
    fun onListItemCheckedStateChangedTest_touchId() {
        val presenter = createPresenter(viewContract = mockView)
        Mockito.doReturn(false).`when`(mockBiometricTools).isBiometricReady(TestAppTools.applicationContext)
        presenter.onListItemCheckedStateChanged(
            itemId = R.string.settings_fingerprint,
            checked = true
        )

        Mockito.verifyNoMoreInteractions(mockPreferences)

        Mockito.doReturn(true).`when`(mockBiometricTools).isBiometricReady(TestAppTools.applicationContext)
        presenter.onListItemCheckedStateChanged(
            itemId = R.string.settings_fingerprint,
            checked = true
        )

        Mockito.verify(mockPreferences).fingerprintEnabled = true
    }

    @Test
    @Throws(Exception::class)
    fun onListItemCheckedStateChangedTest_notifications() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.onListItemCheckedStateChanged(
            itemId = R.string.settings_notifications,
            checked = true
        )

        Mockito.verify(mockPreferences).notificationsEnabled = true
    }

    @Test
    @Throws(Exception::class)
    fun onListItemCheckedStateChangedTest_screenshot() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.onListItemCheckedStateChanged(
            itemId = R.string.settings_screenshot_lock,
            checked = true
        )

        Mockito.verify(mockPreferences).screenshotLockEnabled = true
    }

    @Test
    @Throws(Exception::class)
    fun onListItemCheckedStateChangedTest_invalidParams() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.onListItemCheckedStateChanged(itemId = -1, checked = true)

        Mockito.verifyNoMoreInteractions(mockPreferences)
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTest_passcode() {
        val presenter = createPresenter(viewContract = null)
        presenter.onListItemClick(itemId = R.string.settings_passcode)

        Mockito.verifyNoMoreInteractions(mockView)

        presenter.viewContract = mockView
        presenter.onListItemClick(itemId = R.string.settings_passcode)

        Mockito.verify(mockView).showPasscodeEditor()
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTest_fingerprint() {
        val presenter = createPresenter(viewContract = null)
        Mockito.doReturn(false).`when`(mockBiometricTools).isFingerprintNotConfigured(TestAppTools.applicationContext)
        presenter.onListItemClick(itemId = R.string.settings_fingerprint)

        Mockito.verifyNoMoreInteractions(mockView)

        Mockito.doReturn(true).`when`(mockBiometricTools).isFingerprintNotConfigured(TestAppTools.applicationContext)
        presenter.onListItemClick(itemId = R.string.settings_fingerprint)

        Mockito.verifyNoMoreInteractions(mockView)

        presenter.viewContract = mockView
        Mockito.doReturn(false).`when`(mockBiometricTools).isFingerprintNotConfigured(TestAppTools.applicationContext)
        presenter.onListItemClick(itemId = R.string.settings_fingerprint)

        Mockito.verifyNoMoreInteractions(mockView)

        Mockito.doReturn(true).`when`(mockBiometricTools).isFingerprintNotConfigured(TestAppTools.applicationContext)
        presenter.onListItemClick(itemId = R.string.settings_fingerprint)

        Mockito.verify(mockView).showSystemSettings()
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTest_about() {
        val presenter = createPresenter(viewContract = null)
        presenter.onListItemClick(itemId = R.string.about_feature_title)

        Mockito.verifyNoMoreInteractions(mockView)

        presenter.viewContract = mockView
        presenter.onListItemClick(itemId = R.string.about_feature_title)

        Mockito.verify(mockView).showAboutList()
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTest_support() {
        val presenter = createPresenter(viewContract = null)
        presenter.onListItemClick(itemId = R.string.settings_report_bug)

        Mockito.verifyNoMoreInteractions(mockView)

        presenter.viewContract = mockView
        presenter.onListItemClick(itemId = R.string.settings_report_bug)

        Mockito.verify(mockView).openMailApp()
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTest_invalidParams() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.onListItemClick(itemId = -1)

        Mockito.verifyNoMoreInteractions(mockView)
    }

    private val mockView = Mockito.mock(SettingsListContract.View::class.java)
    private val mockPreferences = Mockito.mock(PreferenceRepositoryAbs::class.java)
    private val mockBiometricTools = Mockito.mock(BiometricToolsAbs::class.java)

    private fun createPresenter(viewContract: SettingsListContract.View? = null): SettingsListPresenter {
        return SettingsListPresenter(
            TestAppTools.applicationContext,
            mockPreferences,
            mockBiometricTools
        )
            .apply { this.viewContract = viewContract }
    }
}
