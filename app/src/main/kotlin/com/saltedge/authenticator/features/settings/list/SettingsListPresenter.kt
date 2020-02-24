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

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.DELETE_ALL_REQUEST_CODE
import com.saltedge.authenticator.features.settings.common.SettingsItemViewModel
import com.saltedge.authenticator.model.db.Connection
import com.saltedge.authenticator.model.db.ConnectionsRepositoryAbs
import com.saltedge.authenticator.model.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.model.connection.ConnectionAndKey
import com.saltedge.authenticator.sdk.model.connection.isActive
import com.saltedge.authenticator.sdk.model.connection.toConnectionAndKey
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManagerAbs
import com.saltedge.authenticator.sdk.tools.biometric.BiometricToolsAbs
import javax.inject.Inject

class SettingsListPresenter @Inject constructor(
    private val appContext: Context,
    private val keyStoreManager: KeyStoreManagerAbs,
    private val apiManager: AuthenticatorApiManagerAbs,
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val preferences: PreferenceRepositoryAbs,
    private val biometricTools: BiometricToolsAbs
) : SettingsListContract.Presenter {

    override var viewContract: SettingsListContract.View? = null

    override fun getListItems(): List<SettingsItemViewModel> {
        return listOf(
            SettingsItemViewModel(
                titleId = R.string.settings_passcode,
                value = appContext.getString(R.string.settings_passcode_description),
                itemIsClickable = true
            ),
            SettingsItemViewModel(
                titleId = R.string.settings_notifications,
                switchEnabled = true,
                isChecked = preferences.notificationsEnabled
            ),
            SettingsItemViewModel(
                titleId = R.string.settings_screenshot_lock,
                switchEnabled = true,
                isChecked = preferences.screenshotLockEnabled
            ),
            SettingsItemViewModel(
                titleId = R.string.about_feature_title,
                itemIsClickable = true
            ),
            SettingsItemViewModel(
                titleId = R.string.settings_report_bug,
                itemIsClickable = true
            ),
            SettingsItemViewModel(
                titleId = R.string.settings_clear_all_data,
                itemIsClickable = true,
                colorResId = R.color.red
            )
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null || resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            DELETE_ALL_REQUEST_CODE -> onUserConfirmedDeleteAllConnections()
        }
    }

    override fun onListItemCheckedStateChanged(itemId: Int, checked: Boolean) {
        when (itemId) {
            R.string.settings_fingerprint ->
                if (biometricTools.isBiometricReady(appContext)) {
                    if (checked) biometricTools.activateFingerprint()
                    preferences.fingerprintEnabled = checked
                }
            R.string.settings_notifications ->
                preferences.notificationsEnabled = checked
            R.string.settings_screenshot_lock -> {
                preferences.screenshotLockEnabled = checked
                viewContract?.showRestartAppQuery()
            }
        }
    }

    override fun onListItemClick(itemId: Int) {
        when (itemId) {
            R.string.settings_passcode -> viewContract?.showPasscodeEditor()
            R.string.settings_fingerprint ->
                if (biometricTools.isBiometricNotConfigured(appContext)) {
                    viewContract?.showSystemSettings()
                }
            R.string.about_feature_title -> viewContract?.showAboutList()
            R.string.settings_report_bug -> viewContract?.openMailApp()
            R.string.settings_clear_all_data -> viewContract?.showDeleteConnectionView(requestCode = DELETE_ALL_REQUEST_CODE)
        }
    }

    override fun getPositionsOfHeaders(): Array<Int> = arrayOf(0, 3, 5)

    private fun onUserConfirmedDeleteAllConnections() {
        sendRevokeRequestForConnections(connectionsRepository.getAllActiveConnections())
        deleteAllConnectionsAndKeys()
    }

    private fun sendRevokeRequestForConnections(connections: List<Connection>) {
        val connectionsAndKeys: List<ConnectionAndKey> = connections.filter { it.isActive() }
            .mapNotNull { it.toConnectionAndKey(keyStoreManager) }

        apiManager.revokeConnections(connectionsAndKeys = connectionsAndKeys, resultCallback = null)
    }

    private fun deleteAllConnectionsAndKeys() {
        val connectionGuids = connectionsRepository.getAllConnections().map { it.guid }
        keyStoreManager.deleteKeyPairs(connectionGuids)
        connectionsRepository.deleteAllConnections()
    }
}
