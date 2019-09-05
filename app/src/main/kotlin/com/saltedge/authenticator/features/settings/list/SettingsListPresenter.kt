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

import android.content.Context
import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.settings.common.CheckedTitleValueViewModel
import com.saltedge.authenticator.features.settings.common.HeaderViewModel
import com.saltedge.authenticator.model.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.tool.secure.fingerprint.BiometricToolsAbs
import javax.inject.Inject

class SettingsListPresenter @Inject constructor(
    private val appContext: Context,
    private val preferences: PreferenceRepositoryAbs,
    private val biometricTools: BiometricToolsAbs
) : SettingsListContract.Presenter {

    override var viewContract: SettingsListContract.View? = null

    override fun getListItems(): List<Any> {
        return listOf(
            HeaderViewModel(),
            CheckedTitleValueViewModel(
                titleId = R.string.settings_passcode,
                value = appContext.getString(R.string.settings_passcode_description),
                itemIsClickable = true
            ),
            CheckedTitleValueViewModel(
                titleId = R.string.settings_notifications,
                switchEnabled = true,
                isChecked = preferences.notificationsEnabled
            ),
            CheckedTitleValueViewModel(
                titleId = R.string.settings_screenshot_lock,
                switchEnabled = true,
                isChecked = preferences.screenshotLockEnabled
            ),
            HeaderViewModel(),
            CheckedTitleValueViewModel(
                titleId = R.string.about_feature_title,
                itemIsClickable = true
            ),
            CheckedTitleValueViewModel(
                titleId = R.string.settings_report_bug,
                itemIsClickable = true
            )
        )
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
                viewContract?.showInfo(R.string.settings_restart_app)
            }
        }
    }

    override fun onListItemClick(itemId: Int) {
        when (itemId) {
            R.string.settings_passcode -> viewContract?.showPasscodeEditor()
            R.string.settings_fingerprint ->
                if (biometricTools.isFingerprintNotConfigured(appContext)) {
                    viewContract?.showSystemSettings()
                }
            R.string.about_feature_title -> viewContract?.showAboutList()
            R.string.settings_report_bug -> viewContract?.openMailApp()
        }
    }
}
