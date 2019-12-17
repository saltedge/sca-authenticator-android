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
package com.saltedge.authenticator.features.authorizations.list.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.saltedge.authenticator.app.TIME_VIEW_UPDATE_TIMEOUT
import com.saltedge.authenticator.features.authorizations.common.AuthorizationHeaderView
import com.saltedge.authenticator.features.authorizations.common.AuthorizationStatusListener
import com.saltedge.authenticator.features.authorizations.common.AuthorizationViewModel
import com.saltedge.authenticator.features.authorizations.common.TimeUpdateListener
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class AuthorizationsHeaderPagerAdapter(
    val context: Context,
    val expirationListener: AuthorizationStatusListener
) : AuthorizationsPagerAdapter() {

    private val timeUpdateListeners: HashSet<TimeUpdateListener> = HashSet()
    private var timeViewUpdateTimer: Timer = Timer()
    private val map = HashMap<Int, View>()

    fun startTimer() {
        timeViewUpdateTimer = Timer()
        timeViewUpdateTimer.schedule(object : TimerTask() {
            override fun run() {
                if (existExpiredModels()) expirationListener.onViewModelsExpired()
                if (existModelsShouldBeDestroyed()) expirationListener.onViewModelsShouldBeDestroyed()
                timeUpdateListeners.iterator().forEach { it.onTimeUpdate() }
            }
        }, 0, TIME_VIEW_UPDATE_TIMEOUT)
    }

    fun stopTimer() {
        timeViewUpdateTimer.cancel()
        timeViewUpdateTimer.purge()
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = map.getOrPut(position) {
            AuthorizationHeaderView(context = context)
        }
        updateViewContent(view, data[position])
        timeUpdateListeners.add(view as TimeUpdateListener)
        container.addView(view, 0)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
        super.destroyItem(container, position, view)
        timeUpdateListeners.remove(view as TimeUpdateListener)
    }

    private fun existExpiredModels(): Boolean = data.any { it.isExpired }

    private fun existModelsShouldBeDestroyed(): Boolean = data.any { it.shouldBeDestroyed }

    private fun updateViewContent(pageView: View, model: AuthorizationViewModel) {
        (pageView as AuthorizationHeaderView).apply {
            setTitleAndLogo(
                title = model.connectionName,
                logoUrl = model.connectionLogoUrl
            )
            setProgressTime(startTime = model.createdAt, endTime = model.expiresAt)
            ignoreTimeUpdate = model.ignoreTimeUpdate
        }
    }
}
