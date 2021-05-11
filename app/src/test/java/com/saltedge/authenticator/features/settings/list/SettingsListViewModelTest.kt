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
package com.saltedge.authenticator.features.settings.list

import android.content.Context
import android.content.DialogInterface
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.test.core.app.ApplicationProvider
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.getDefaultSystemNightMode
import com.saltedge.authenticator.features.settings.common.SettingsItemViewModel
import com.saltedge.authenticator.interfaces.MenuItem
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.models.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.app.AppToolsAbs
import com.saltedge.authenticator.core.model.ConnectionStatus
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import junit.framework.TestCase.assertNull
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.robolectric.RobolectricTestRunner
import java.security.PrivateKey

@RunWith(RobolectricTestRunner::class)
class SettingsListViewModelTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val mockAppTools = Mockito.mock(AppToolsAbs::class.java)
    private val mockPreferences = Mockito.mock(PreferenceRepositoryAbs::class.java)
    private val mockKeyStoreManager = Mockito.mock(KeyManagerAbs::class.java)
    private val mockApiManager = Mockito.mock(AuthenticatorApiManagerAbs::class.java)
    private val mockConnectionsRepository = Mockito.mock(ConnectionsRepositoryAbs::class.java)
    private val mockConnection1 = Connection().apply {
        guid = "guid1"
        id = "1"
        code = "demobank3"
        name = "Demobank3"
        status = "${ConnectionStatus.ACTIVE}"
        accessToken = "token1"
        logoUrl = "url"
        createdAt = 200L
        updatedAt = 200L
    }
    private val mockPrivateKey = Mockito.mock(PrivateKey::class.java)
    private val mockConnectionAndKey = RichConnection(mockConnection1, mockPrivateKey)
    private lateinit var viewModel: SettingsListViewModel

    @Before
    fun setUp() {
        Mockito.doReturn(true).`when`(mockPreferences).screenshotLockEnabled
        given(mockConnectionsRepository.getAllActiveConnections()).willReturn(listOf(mockConnection1))
        given(mockKeyStoreManager.enrichConnection(mockConnection1)).willReturn(mockConnectionAndKey)
        viewModel = SettingsListViewModel(
            appContext = context,
            appTools = mockAppTools,
            keyStoreManager = mockKeyStoreManager,
            apiManager = mockApiManager,
            connectionsRepository = mockConnectionsRepository,
            preferenceRepository = mockPreferences
        )
    }

    @Test
    @Throws(Exception::class)
    fun getListItemsTestCase1() {
        //given
        given(mockAppTools.getSDKVersion()).willReturn(Build.VERSION_CODES.Q)
        viewModel = SettingsListViewModel(
            appContext = context,
            appTools = mockAppTools,
            keyStoreManager = mockKeyStoreManager,
            apiManager = mockApiManager,
            connectionsRepository = mockConnectionsRepository,
            preferenceRepository = mockPreferences
        )

        //when
        val values = viewModel.listItems.value!!

        //then
        assertThat(values.size, equalTo(7))
        assertThat(
            values,
            equalTo(
                listOf(
                    SettingsItemViewModel(
                        iconId = R.drawable.ic_setting_passcode,
                        titleId = R.string.settings_passcode_description,
                        itemIsClickable = true
                    ),
                    SettingsItemViewModel(
                        iconId = R.drawable.ic_setting_language,
                        titleId = R.string.settings_language,
                        itemIsClickable = true
                    ),
                    SettingsItemViewModel(
                        iconId = R.drawable.ic_setting_screenshots,
                        titleId = R.string.settings_screenshot_lock,
                        switchIsChecked = true
                    ),
                    SettingsItemViewModel(
                        iconId = R.drawable.ic_settings_dark_mode,
                        titleId = R.string.settings_system_dark_mode,
                        switchIsChecked = false
                    ),
                    SettingsItemViewModel(
                        iconId = R.drawable.ic_setting_about,
                        titleId = R.string.about_feature_title,
                        itemIsClickable = true
                    ),
                    SettingsItemViewModel(
                        iconId = R.drawable.ic_setting_support,
                        titleId = R.string.settings_report,
                        itemIsClickable = true
                    ),
                    SettingsItemViewModel(
                        iconId = R.drawable.ic_setting_clear,
                        titleId = R.string.settings_clear_data,
                        titleColorRes = R.color.red,
                        itemIsClickable = true
                    )
                )
            )
        )
    }

    @Test
    @Throws(Exception::class)
    fun getListItemsTestCase2() {
        //given
        given(mockAppTools.getSDKVersion()).willReturn(Build.VERSION_CODES.P)
        viewModel = SettingsListViewModel(
            appContext = context,
            appTools = mockAppTools,
            keyStoreManager = mockKeyStoreManager,
            apiManager = mockApiManager,
            connectionsRepository = mockConnectionsRepository,
            preferenceRepository = mockPreferences
        )

        //when
        val values = viewModel.listItems.value!!

        //then
        assertThat(values.size, equalTo(6))
        assertThat(
            values,
            equalTo(
                listOf(
                    SettingsItemViewModel(
                        iconId = R.drawable.ic_setting_passcode,
                        titleId = R.string.settings_passcode_description,
                        itemIsClickable = true
                    ),
                    SettingsItemViewModel(
                        iconId = R.drawable.ic_setting_language,
                        titleId = R.string.settings_language,
                        itemIsClickable = true
                    ),
                    SettingsItemViewModel(
                        iconId = R.drawable.ic_setting_screenshots,
                        titleId = R.string.settings_screenshot_lock,
                        switchIsChecked = true
                    ),
                    SettingsItemViewModel(
                        iconId = R.drawable.ic_setting_about,
                        titleId = R.string.about_feature_title,
                        itemIsClickable = true
                    ),
                    SettingsItemViewModel(
                        iconId = R.drawable.ic_setting_support,
                        titleId = R.string.settings_report,
                        itemIsClickable = true
                    ),
                    SettingsItemViewModel(
                        iconId = R.drawable.ic_setting_clear,
                        titleId = R.string.settings_clear_data,
                        titleColorRes = R.color.red,
                        itemIsClickable = true
                    )
                )
            )
        )
    }

    @Test
    @Throws(Exception::class)
    fun onDialogActionIdClickTestCase1() {
        //when
        viewModel.onDialogActionIdClick(DialogInterface.BUTTON_POSITIVE)

        //then
        Mockito.verify(mockConnectionsRepository).deleteAllConnections()
        Mockito.verify(mockApiManager).revokeConnections(
            connectionsAndKeys = listOf(mockConnectionAndKey),
            resultCallback = null
        )
    }

    @Test
    @Throws(Exception::class)
    fun onDialogActionIdClickTestCase2() {
        //when
        viewModel.onDialogActionIdClick(DialogInterface.BUTTON_NEGATIVE)

        //then
        Mockito.never()
    }

    @Test
    @Throws(Exception::class)
    fun restartConfirmedTest() {
        //when
        viewModel.restartConfirmed()

        //then
        assertThat(viewModel.restartClickEvent.value, equalTo(ViewModelEvent(Unit)))
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTestCase1() {
        //given
        val itemId = R.string.settings_passcode_description

        //when
        viewModel.onListItemClick(itemId = itemId)

        //them
        assertThat(viewModel.passcodeClickEvent.value, equalTo(ViewModelEvent(Unit)))
        assertNull(viewModel.languageClickEvent.value)
        assertNull(viewModel.aboutClickEvent.value)
        assertNull(viewModel.supportClickEvent.value)
        assertNull(viewModel.clearClickEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTestCase2() {
        //given
        val itemId = R.string.settings_language

        //when
        viewModel.onListItemClick(itemId = itemId)

        //them
        assertNull(viewModel.passcodeClickEvent.value)
        assertThat(viewModel.languageClickEvent.value, equalTo(ViewModelEvent(Unit)))
        assertNull(viewModel.aboutClickEvent.value)
        assertNull(viewModel.supportClickEvent.value)
        assertNull(viewModel.clearClickEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTestCase3() {
        //given
        val itemId = R.string.about_feature_title

        //when
        viewModel.onListItemClick(itemId = itemId)

        //them
        assertNull(viewModel.languageClickEvent.value)
        assertNull(viewModel.passcodeClickEvent.value)
        assertThat(viewModel.aboutClickEvent.value, equalTo(ViewModelEvent(Unit)))
        assertNull(viewModel.supportClickEvent.value)
        assertNull(viewModel.clearClickEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTestCase4() {
        //given
        val itemId = R.string.settings_report

        //when
        viewModel.onListItemClick(itemId = itemId)

        //them
        assertNull(viewModel.languageClickEvent.value)
        assertNull(viewModel.passcodeClickEvent.value)
        assertNull(viewModel.aboutClickEvent.value)
        assertThat(viewModel.supportClickEvent.value, equalTo(ViewModelEvent(Unit)))
        assertNull(viewModel.clearClickEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTestCase5() {
        //given
        val itemId = R.string.settings_clear_data

        //when
        viewModel.onListItemClick(itemId = itemId)

        //them
        assertNull(viewModel.languageClickEvent.value)
        assertNull(viewModel.passcodeClickEvent.value)
        assertNull(viewModel.aboutClickEvent.value)
        assertNull(viewModel.supportClickEvent.value)
        assertThat(viewModel.clearClickEvent.value, equalTo(ViewModelEvent(Unit)))
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTestCase6() {
        //given
        val itemId = R.string.settings_screenshot_lock

        //when
        viewModel.onListItemClick(itemId = itemId)

        //them
        assertNull(viewModel.languageClickEvent.value)
        assertNull(viewModel.passcodeClickEvent.value)
        assertNull(viewModel.aboutClickEvent.value)
        assertNull(viewModel.supportClickEvent.value)
        assertNull(viewModel.clearClickEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onListItemCheckedStateChangedTestCase1() {
        //given
        val itemId = R.string.settings_clear_data
        Mockito.clearInvocations(mockPreferences)

        //when
        viewModel.onListItemCheckedStateChanged(itemId = itemId, checked = true)

        //then
        Mockito.verifyNoMoreInteractions(mockPreferences)
        assertNull(viewModel.screenshotClickEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onListItemCheckedStateChangedTestCase2() {
        //given
        val itemId = R.string.settings_screenshot_lock
        given(mockPreferences.screenshotLockEnabled).willReturn(false)

        //when
        viewModel.onListItemCheckedStateChanged(itemId = itemId, checked = true)

        //then
        Mockito.verify(mockPreferences).screenshotLockEnabled = true
        assertThat(viewModel.screenshotClickEvent.value, equalTo(ViewModelEvent(Unit)))
    }

    @Test
    @Throws(Exception::class)
    fun onListItemCheckedStateChangedTestCase3() {
        //given
        val itemId = R.string.settings_system_dark_mode
        val checked = true
        val defaultMode = getDefaultSystemNightMode()
        given(mockPreferences.nightMode).willReturn(AppCompatDelegate.MODE_NIGHT_YES)

        //when
        viewModel.onListItemCheckedStateChanged(itemId = itemId, checked = checked)

        //then
        Mockito.verify(mockPreferences).systemNightMode = checked
        Mockito.verify(mockPreferences).nightMode = defaultMode
        assertThat(viewModel.setNightModelEvent.value, equalTo(ViewModelEvent(defaultMode)))
    }

    @Test
    @Throws(Exception::class)
    fun onListItemCheckedStateChangedTestCase4() {
        //given
        val itemId = R.string.settings_system_dark_mode
        val checked = false
        val defaultMode = getDefaultSystemNightMode()
        given(mockPreferences.nightMode).willReturn(AppCompatDelegate.MODE_NIGHT_YES)

        //when
        viewModel.onListItemCheckedStateChanged(itemId = itemId, checked = checked)

        //then
        Mockito.verify(mockPreferences).systemNightMode = checked
        Mockito.verify(mockPreferences, never()).nightMode = defaultMode
        assertNull(viewModel.setNightModelEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onAppbarMenuItemClickTestCase1() {
        //given
        val menuItem = MenuItem.MORE_MENU
        given(mockPreferences.nightMode).willReturn(AppCompatDelegate.MODE_NIGHT_YES)

        //when
        viewModel.onAppbarMenuItemClick(menuItem)

        //then
        Mockito.verify(mockPreferences, never()).systemNightMode
        Mockito.verify(mockPreferences, never()).nightMode
        assertNull(viewModel.setNightModelEvent.value)
    }

    @Test
    @Throws(Exception::class)
    fun onAppbarMenuItemClickTestCase2() {
        //given
        val menuItem = MenuItem.CUSTOM_NIGHT_MODE
        given(mockPreferences.nightMode).willReturn(AppCompatDelegate.MODE_NIGHT_YES)

        //when
        viewModel.onAppbarMenuItemClick(menuItem)

        //then
        Mockito.verify(mockPreferences).systemNightMode = false
        Mockito.verify(mockPreferences).nightMode = AppCompatDelegate.MODE_NIGHT_NO
        assertThat(viewModel.setNightModelEvent.value, equalTo(ViewModelEvent(AppCompatDelegate.MODE_NIGHT_NO)))
    }

    @Test
    @Throws(Exception::class)
    fun testSpacesPositions() {
        assertThat(viewModel.spacesPositions, equalTo(arrayOf(0, viewModel.listItemsValues!!.lastIndex)))
    }
}
