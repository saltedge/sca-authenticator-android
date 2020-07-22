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
package com.saltedge.authenticator.features.authorizations.list.pagers

import android.content.Context
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import com.saltedge.authenticator.app.TIME_VIEW_UPDATE_TIMEOUT
import com.saltedge.authenticator.features.authorizations.common.AuthorizationHeaderView
import com.saltedge.authenticator.features.authorizations.common.AuthorizationItemViewModel
import com.saltedge.authenticator.features.authorizations.common.TimerUpdateListener
import com.saltedge.authenticator.tools.getOrPut
import java.util.*
import kotlin.collections.HashSet

class AuthorizationsHeaderPagerAdapter(
    val context: Context,
    viewModelTimerUpdateListener: TimerUpdateListener
) : AuthorizationsPagerAdapter() {

    private val timerUpdateListeners: HashSet<TimerUpdateListener> = HashSet()
    private var timer: Timer = Timer()
    private val itemsMap = SparseArray<AuthorizationHeaderView>()

    init {
        timerUpdateListeners.add(viewModelTimerUpdateListener)
    }

    fun startTimer() {
        timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                HashSet(timerUpdateListeners).iterator().forEach { it.onTimeUpdate() }
            }
        }, 0, TIME_VIEW_UPDATE_TIMEOUT)
    }

    fun stopTimer() {
        timer.cancel()
        timer.purge()
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = itemsMap.getOrPut(position) {
            AuthorizationHeaderView(context = context)
        }
        updateViewContent(view, data[position])
        timerUpdateListeners.add(view as TimerUpdateListener)
        container.addView(view, 0)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
        super.destroyItem(container, position, view)
//        map.remove(position) //TODO: https://github.com/saltedge/sca-authenticator-android/issues/83
        timerUpdateListeners.remove(view as TimerUpdateListener)
    }

    private fun updateViewContent(pageView: View, model: AuthorizationItemViewModel) {
        (pageView as AuthorizationHeaderView).apply {
            setTitleAndLogo(
                title = model.connectionName,
                logoUrl = model.connectionLogoUrl
            )
            setProgressTime(startTime = model.startTime, endTime = model.endTime)
            ignoreTimeUpdate = model.ignoreTimeUpdate
        }
    }
}
