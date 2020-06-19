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
import android.os.Build
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.switchDarkLightMode
import com.saltedge.authenticator.features.settings.common.SettingsItemViewModel
import com.saltedge.authenticator.interfaces.ListItemClickListener
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.models.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.model.connection.ConnectionAndKey
import com.saltedge.authenticator.sdk.model.connection.isActive
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManagerAbs
import com.saltedge.authenticator.tools.postUnitEvent

class SettingsListViewModel(
    private val appContext: Context,
    private val keyStoreManager: KeyStoreManagerAbs,
    private val apiManager: AuthenticatorApiManagerAbs,
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val preferenceRepository: PreferenceRepositoryAbs
) : ViewModel(), ListItemClickListener {

    val languageClickEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val passcodeClickEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val screenshotClickEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val aboutClickEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val supportClickEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val clearClickEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val clearSuccessEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val restartClickEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val darkModeClickEvent = MutableLiveData<ViewModelEvent<Unit>>()
    var onSetNightMode = MutableLiveData<ViewModelEvent<Int>>()

    fun getListItems(): List<SettingsItemViewModel> {
        val listItems = listOf(
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
                switchIsChecked = preferenceRepository.screenshotLockEnabled
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
        val isSystemDarkMode = isSystemDarkMode()
        return if (isSystemDarkMode) listItems +  SettingsItemViewModel(
            iconId = R.drawable.ic_settings_dark_mode,
            titleId = R.string.settings_system_dark_mode,
            switchIsChecked = preferenceRepository.screenshotLockEnabled
        ) else listItems
    }

    fun isSystemDarkMode(): Boolean {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q  //change on >=
    }

    fun restartConfirmed() {
        restartClickEvent.postValue(ViewModelEvent(Unit))
    }

    override fun onListItemClick(itemId: Int) {
        when (itemId) {
            R.string.settings_passcode_description -> passcodeClickEvent.postUnitEvent()
            R.string.settings_language -> languageClickEvent.postUnitEvent()
            R.string.about_feature_title -> aboutClickEvent.postUnitEvent()
            R.string.settings_report -> supportClickEvent.postUnitEvent()
            R.string.settings_clear_data -> clearClickEvent.postUnitEvent()
        }
    }

    override fun onListItemCheckedStateChanged(itemId: Int, checked: Boolean) {
        when (itemId) {
            R.string.settings_screenshot_lock -> {
                preferenceRepository.screenshotLockEnabled = checked
                screenshotClickEvent.postValue(ViewModelEvent(Unit))
            }
            R.string.settings_system_dark_mode -> {
                preferenceRepository.darkModeEnabled = checked
                darkModeClickEvent.postValue(ViewModelEvent(Unit))
            }
        }
    }

    fun changeDarkThemeMode() {
        val nightMode = preferenceRepository.nightMode
        preferenceRepository.nightMode = appContext.switchDarkLightMode(nightMode)
        onSetNightMode.postValue(ViewModelEvent(preferenceRepository.nightMode))
    }

    fun onUserConfirmedClearAppData() {
        sendRevokeRequestForConnections(connectionsRepository.getAllActiveConnections())
        deleteAllConnectionsAndKeys()
        clearSuccessEvent.postUnitEvent()
    }

    private fun sendRevokeRequestForConnections(connections: List<Connection>) {
        val connectionsAndKeys: List<ConnectionAndKey> = connections.filter { it.isActive() }
            .mapNotNull { keyStoreManager.createConnectionAndKeyModel(it) }

        apiManager.revokeConnections(connectionsAndKeys = connectionsAndKeys, resultCallback = null)
    }

    private fun deleteAllConnectionsAndKeys() {
        val connectionGuids = connectionsRepository.getAllConnections().map { it.guid }
        keyStoreManager.deleteKeyPairs(connectionGuids)
        connectionsRepository.deleteAllConnections()
    }
}
