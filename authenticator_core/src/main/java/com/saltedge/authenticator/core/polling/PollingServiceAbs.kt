/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.core.polling

import java.util.*

private const val POLLING_TIMEOUT = 3000L

abstract class PollingServiceAbs<T> {

    private var timer: Timer? = null

    open fun start() {
        timer?.let { stop() } //stop before start to ensure that timer is single
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

    open fun stop() {
        try {
            timer?.cancel()
            timer?.purge()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        timer = null
    }

    fun isRunning(): Boolean = timer != null

    abstract fun forcedFetch()
    abstract var contract: T?
}
