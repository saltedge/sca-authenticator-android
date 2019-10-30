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
package com.saltedge.authenticator.features.authorizations.common

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.saltedge.authenticator.R
import com.saltedge.authenticator.sdk.tools.remainedSeconds
import com.saltedge.authenticator.sdk.tools.remainedTimeDescription
import com.saltedge.authenticator.sdk.tools.secondsBetweenDates
import com.saltedge.authenticator.tool.loadImage
import kotlinx.android.synthetic.main.view_authorization_header.view.*
import org.joda.time.DateTime

class AuthorizationHeaderView : LinearLayout, TimeUpdateListener {

    private var startTime: DateTime? = null
    private var endTime: DateTime? = null
    var ignoreTimeUpdate: Boolean = false

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        inflate(context, R.layout.view_authorization_header, this)
    }

    fun setTitleAndLogo(title: String, logoUrl: String?) {
        titleView?.text = title

        if (logoUrl?.isEmpty() == true) {
            logoView?.setImageResource(R.drawable.ic_logo_bank_placeholder)
        } else {
            logoView?.loadImage(
                imageUrl = logoUrl,
                placeholderId = R.drawable.ic_logo_bank_placeholder
            )
        }
    }

    fun setProgressTime(startTime: DateTime, endTime: DateTime) {
        this.startTime = startTime
        this.endTime = endTime
        onTimeUpdate()
    }

    override fun onTimeUpdate() {
        if (!ignoreTimeUpdate) post { updateTimeViewsContent() }
    }

    private fun updateTimeViewsContent() {
        startTime?.let { startTime ->
            endTime?.let { endTime ->
                val maxProgress = secondsBetweenDates(startTime, endTime)
                val remainedSeconds = endTime.remainedSeconds()

                timeTextView?.text = endTime.remainedTimeDescription()
                timeProgressView?.apply {
                    if (max != maxProgress) max = maxProgress
                    progress = remainedSeconds
                }
            }
        }
    }
}
