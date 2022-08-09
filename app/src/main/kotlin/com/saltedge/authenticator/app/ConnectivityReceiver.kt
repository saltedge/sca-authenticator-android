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
package com.saltedge.authenticator.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager

class ConnectivityReceiver() : BroadcastReceiver(), ConnectivityReceiverAbs {

    private var _hasNetworkConnection: Boolean = true

    override val hasNetworkConnection: Boolean
        get() = _hasNetworkConnection

    constructor(context: Context) : this() {
        context.registerReceiver(this, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        _hasNetworkConnection = isConnectedOrConnecting(context)
    }

    private var networkStateListeners: ArrayList<NetworkStateChangeListener> = ArrayList()

    override fun addNetworkStateChangeListener(listener: NetworkStateChangeListener) {
        networkStateListeners.add(listener)
    }

    override fun removeNetworkStateChangeListener(listener: NetworkStateChangeListener) {
        networkStateListeners.remove(listener)
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (ConnectivityManager.CONNECTIVITY_ACTION == intent?.action) {
            _hasNetworkConnection = isConnectedOrConnecting(context)
            networkStateListeners.forEach { it.onNetworkConnectionChanged(hasNetworkConnection) }
        }
    }

    private fun isConnectedOrConnecting(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        return connectivityManager?.activeNetworkInfo?.isConnectedOrConnecting ?: false
    }
}

interface ConnectivityReceiverAbs {
    val hasNetworkConnection: Boolean
    fun addNetworkStateChangeListener(listener: NetworkStateChangeListener)
    fun removeNetworkStateChangeListener(listener: NetworkStateChangeListener)
}

interface NetworkStateChangeListener {
    fun onNetworkConnectionChanged(isConnected: Boolean)
}
