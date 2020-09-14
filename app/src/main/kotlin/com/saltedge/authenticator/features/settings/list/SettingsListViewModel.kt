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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.getDefaultSystemNightMode
import com.saltedge.authenticator.app.isSystemNightModeSupported
import com.saltedge.authenticator.app.switchDarkLightMode
import com.saltedge.authenticator.features.settings.common.SettingsItemViewModel
import com.saltedge.authenticator.interfaces.ListItemClickListener
import com.saltedge.authenticator.interfaces.MenuItem
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.models.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.model.connection.ConnectionAndKey
import com.saltedge.authenticator.sdk.model.connection.isActive
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManagerAbs
import com.saltedge.authenticator.tools.AppToolsAbs
import com.saltedge.authenticator.tools.postUnitEvent

class SettingsListViewModel(
    private val appContext: Context,
    private val appTools: AppToolsAbs,
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
    val setNightModelEvent = MutableLiveData<ViewModelEvent<Int>>()
    val listItems = MutableLiveData<List<SettingsItemViewModel>>(collectListItems())
    val listItemsValues: List<SettingsItemViewModel>?
        get() = listItems.value
    val spacesPositions: Array<Int>
        get() = arrayOf(0, listItems.value?.lastIndex ?: 0)

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
        listItemsValues?.firstOrNull { it.titleId == itemId }?.let {
            it.switchIsChecked = checked
        }
        when (itemId) {
            R.string.settings_screenshot_lock -> {
                if (preferenceRepository.screenshotLockEnabled != checked) {
                    preferenceRepository.screenshotLockEnabled = checked
                    screenshotClickEvent.postUnitEvent()
                }
            }
            R.string.settings_system_dark_mode -> {
                preferenceRepository.systemNightMode = checked
                val defaultNightMode = getDefaultSystemNightMode()
                if (preferenceRepository.nightMode != defaultNightMode && checked) {
                    preferenceRepository.nightMode = defaultNightMode
                    setNightModelEvent.postValue(ViewModelEvent(defaultNightMode))
                }
            }
        }
    }

    fun onAppbarMenuItemClick(menuItem: MenuItem) {
        if (menuItem != MenuItem.CUSTOM_NIGHT_MODE) return
        val newNighMode = appContext.switchDarkLightMode(preferenceRepository.nightMode)
        preferenceRepository.nightMode = newNighMode
        preferenceRepository.systemNightMode = false
        listItemsValues?.firstOrNull { it.titleId == R.string.settings_system_dark_mode }?.let {
            it.switchIsChecked = false
        }
        setNightModelEvent.postValue(ViewModelEvent(newNighMode))
    }

    fun onDialogActionIdClick(dialogActionId: Int) {
        if (dialogActionId == DialogInterface.BUTTON_POSITIVE) {
            sendRevokeRequestForConnections(connectionsRepository.getAllActiveConnections())
            deleteAllConnectionsAndKeys()
            clearSuccessEvent.postUnitEvent()
        }
    }

    fun restartConfirmed() {
        restartClickEvent.postUnitEvent()
    }

    private fun collectListItems(): List<SettingsItemViewModel> {
        val listItems = mutableListOf<SettingsItemViewModel>(
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
            )
        )
        if (isSystemNightModeSupported(appTools.getSDKVersion())) listItems.add(
            SettingsItemViewModel(
                iconId = R.drawable.ic_settings_dark_mode,
                titleId = R.string.settings_system_dark_mode,
                switchIsChecked = preferenceRepository.systemNightMode
            )
        )
        listItems.addAll(
            listOf<SettingsItemViewModel>(
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
        return listItems
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
