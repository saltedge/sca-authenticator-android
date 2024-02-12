/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.authorizations.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.saltedge.authenticator.R
import com.saltedge.authenticator.core.tools.remainedSeconds
import com.saltedge.authenticator.core.tools.remainedTimeDescription
import com.saltedge.authenticator.core.tools.secondsBetweenDates
import com.saltedge.authenticator.databinding.ViewAuthorizationHeaderBinding
import com.saltedge.authenticator.tools.loadImage
import org.joda.time.DateTime

class AuthorizationHeaderView : LinearLayout, TimerUpdateListener {

    private var startTime: DateTime? = null
    private var endTime: DateTime? = null
    var ignoreTimeUpdate: Boolean = false
    private var binding: ViewAuthorizationHeaderBinding

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        binding = ViewAuthorizationHeaderBinding.inflate(LayoutInflater.from(context), this, true)
    }

    fun setTitleAndLogo(title: String, logoUrl: String?) {
        binding.titleView.text = title

        if (logoUrl?.isEmpty() == true) {
            binding.logoView.setImageResource(R.drawable.shape_bg_connection_list_logo)
        } else {
            binding.logoView.loadImage(
                imageUrl = logoUrl,
                placeholderId = R.drawable.shape_radius6_grey_light_extra_and_dark_100
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

                binding.timeTextView.text = endTime.remainedTimeDescription()
                binding.timeProgressView.apply {
                    if (max != maxProgress) max = maxProgress
                    progress = remainedSeconds
                }
            }
        }
    }
}
