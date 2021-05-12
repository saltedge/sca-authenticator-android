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
package com.saltedge.authenticator.core.polling

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import java.util.*

private const val POLLING_TIMEOUT = 3000L

abstract class PollingServiceAbs<T> : LifecycleObserver {

    private var timer: Timer? = null

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    open fun start() {
        try {
            timer = Timer()
            timer?.schedule(object : TimerTask() {
                override fun run() {
                    forcedFetch()
                }
            }, 0, POLLING_TIMEOUT)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    open fun stop() {
        try {
            timer?.cancel()
            timer?.purge()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        timer = null
    }


    fun register(lifecycle: Lifecycle) {
        lifecycle.addObserver(this)
    }

    fun isRunning(): Boolean = timer != null

    abstract fun forcedFetch()
    abstract var contract: T?
}
