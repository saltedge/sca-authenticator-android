/*
 * Copyright (c) 2020 Salt Edge Inc.
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
