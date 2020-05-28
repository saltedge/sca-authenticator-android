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

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.DELETE_ALL_REQUEST_CODE
import com.saltedge.authenticator.features.settings.common.SettingsHeaderModel
import com.saltedge.authenticator.features.settings.common.SettingsItemModel
import com.saltedge.authenticator.interfaces.ListItemClickListener
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.models.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.model.connection.ConnectionAndKey
import com.saltedge.authenticator.sdk.model.connection.isActive
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManagerAbs

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
    val restartClickEvent = MutableLiveData<ViewModelEvent<Unit>>()

    val listItems = listOf(
            SettingsHeaderModel(appContext.getString(R.string.settings_general)),
            SettingsItemModel(
                iconId = R.drawable.ic_setting_passcode,
                titleId = R.string.settings_passcode,
                itemIsClickable = true
            ),
            SettingsItemModel(
                iconId = R.drawable.ic_setting_language,
                titleId = R.string.settings_language,
                itemIsClickable = true
            ),
            SettingsItemModel(
                iconId = R.drawable.ic_setting_screenshots,
                titleId = R.string.settings_screenshot_lock,
                switchIsChecked = preferenceRepository.screenshotLockEnabled
            ),
            SettingsHeaderModel(appContext.getString(R.string.settings_info)),
            SettingsItemModel(
                iconId = R.drawable.ic_setting_about,
                titleId = R.string.about_feature_title,
                itemIsClickable = true
            ),
            SettingsItemModel(
                iconId = R.drawable.ic_setting_support,
                titleId = R.string.settings_report_bug,
                itemIsClickable = true
            ),
            SettingsHeaderModel(""),
            SettingsItemModel(
                iconId = R.drawable.ic_setting_clear,
                titleId = R.string.settings_clear_data,
                titleColor = ContextCompat.getColor(appContext, R.color.red),
                itemIsClickable = true
            ),
            SettingsHeaderModel("")
        )

    fun onActivityResult(requestCode: Int, resultCode: Int) {
        if (resultCode == Activity.RESULT_OK && requestCode == DELETE_ALL_REQUEST_CODE) {
            onUserConfirmedDeleteAllConnections()
        }
    }

    fun restartConfirmed() {
        restartClickEvent.postValue(ViewModelEvent(Unit))
    }

    override fun onListItemClick(itemId: Int) {
        when (itemId) {
            R.string.settings_passcode -> passcodeClickEvent.postValue(ViewModelEvent(Unit))
            R.string.settings_language -> languageClickEvent.postValue(ViewModelEvent(Unit))
            R.string.about_feature_title -> aboutClickEvent.postValue(ViewModelEvent(Unit))
            R.string.settings_report_bug -> supportClickEvent.postValue(ViewModelEvent(Unit))
            R.string.settings_clear_data -> clearClickEvent.postValue(ViewModelEvent(Unit))
        }
    }

    override fun onListItemCheckedStateChanged(itemId: Int, checked: Boolean) {
        when (itemId) {
            R.string.settings_screenshot_lock -> {
                preferenceRepository.screenshotLockEnabled = checked
                screenshotClickEvent.postValue(ViewModelEvent(Unit))
            }
        }
    }

    private fun onUserConfirmedDeleteAllConnections() {
        sendRevokeRequestForConnections(connectionsRepository.getAllActiveConnections())
        deleteAllConnectionsAndKeys()
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
