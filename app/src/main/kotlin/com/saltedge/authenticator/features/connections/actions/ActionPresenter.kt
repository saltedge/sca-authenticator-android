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
package com.saltedge.authenticator.features.connections.actions

import android.content.Context
import android.util.Log
import com.saltedge.authenticator.R
import com.saltedge.authenticator.model.db.Connection
import com.saltedge.authenticator.model.db.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.model.GUID
import com.saltedge.authenticator.sdk.tools.ActionDeepLinkData
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManagerAbs
import javax.inject.Inject

class ActionPresenter @Inject constructor(
    private val appContext: Context,
    private val keyStoreManager: KeyStoreManagerAbs,
    private val connectionsRepository: ConnectionsRepositoryAbs
) : ActionContract.Presenter {

    private var connection = Connection()

    override var viewContract: ActionContract.View? = null
    override val completeTitle: String
        get() = appContext.getString(R.string.action_feature_title)

    private var viewMode: ViewMode = ViewMode.PROCESSING
    override val shouldShowProgressView: Boolean
        get() = viewMode == ViewMode.PROCESSING
    override val shouldShowCompleteView: Boolean
        get() = viewMode == ViewMode.ACTION_SUCCESS || viewMode == ViewMode.ACTION_ERROR

    override fun setInitialData(connectionGuid: GUID?, actionDeepLinkData: ActionDeepLinkData?) {
        when {
            connectionGuid != null -> {
                this.connection = connectionsRepository.getByGuid(connectionGuid) ?: Connection()
            }
            actionDeepLinkData != null -> {
                Log.d("some", "actionDeepLinkData != null")
            }
        }
    }

    override fun onDestroyView() {
        if (connection.guid.isNotEmpty() && connection.accessToken.isEmpty()) {
            keyStoreManager.deleteKeyPair(connection.guid)
        }
    }

    override fun getTitleResId(): Int = R.string.connections_new_connection

    override fun onViewClick(viewId: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onViewCreated() {
        when (viewMode) {
            ViewMode.PROCESSING -> startProcessingFlow()
            ViewMode.ACTION_ERROR -> Unit
            ViewMode.ACTION_SUCCESS -> Unit
        }
    }

    private fun startProcessingFlow() {
        Log.d("some", "PROCESSING")
    }
}

enum class ViewMode {
    PROCESSING, ACTION_SUCCESS, ACTION_ERROR
}
