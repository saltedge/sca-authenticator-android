/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2021 Salt Edge Inc.
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

import com.saltedge.authenticator.app.AppToolsAbs
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.model.isActive
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.models.repository.PreferenceRepositoryAbs

abstract class SettingsListInteractor(
    private val appTools: AppToolsAbs,
    private val keyStoreManager: KeyManagerAbs,
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val preferenceRepository: PreferenceRepositoryAbs
) : SettingsListInteractorAbs {

    override var systemNightMode: Boolean
        get() = preferenceRepository.systemNightMode
        set(value) {
            preferenceRepository.systemNightMode = value
        }
    override var screenshotLockEnabled: Boolean
        get() = preferenceRepository.screenshotLockEnabled
        set(value) {
            preferenceRepository.screenshotLockEnabled = value
        }
    override var nightMode: Int
        get() = preferenceRepository.nightMode
        set(value) {
            preferenceRepository.nightMode = value
        }

    override fun getSDKVersion(): Int {
        return appTools.getSDKVersion()
    }

    override fun sendRevokeRequestForConnections() {
        val connectionsAndKeys: List<RichConnection> =
            connectionsRepository.getAllActiveConnections().filter { it.isActive() }
                .mapNotNull { keyStoreManager.enrichConnection(it) }

        revokeConnections(connectionsAndKeys = connectionsAndKeys)
    }

    abstract override fun revokeConnections(connectionsAndKeys: List<RichConnection>)

    override fun deleteAllConnectionsAndKeys() {
        val connectionGuids = connectionsRepository.getAllConnections().map { it.guid }
        keyStoreManager.deleteKeyPairsIfExist(connectionGuids)
        connectionsRepository.deleteAllConnections()
    }
}

interface SettingsListInteractorAbs {
    fun getSDKVersion(): Int
    fun deleteAllConnectionsAndKeys()
    fun sendRevokeRequestForConnections()
    fun revokeConnections(connectionsAndKeys: List<RichConnection>)

    var nightMode: Int
    var screenshotLockEnabled: Boolean
    var systemNightMode: Boolean
}
