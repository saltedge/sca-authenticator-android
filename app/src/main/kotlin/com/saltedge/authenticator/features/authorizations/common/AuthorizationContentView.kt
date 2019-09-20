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
import android.text.method.ScrollingMovementMethod
import android.util.AttributeSet
import android.widget.LinearLayout
import com.saltedge.authenticator.R
import com.saltedge.authenticator.tool.ResId
import com.saltedge.authenticator.tool.hasHTMLTags
import com.saltedge.authenticator.tool.setVisible
import kotlinx.android.synthetic.main.view_authorization_content.view.*

class AuthorizationContentView : LinearLayout {

    enum class Mode {
        LOADING, DEFAULT, CONFIRM_PROCESSING, DENY_PROCESSING, CONFIRM_SUCCESS, DENY_SUCCESS, ERROR, TIME_OUT, UNAVAILABLE
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        inflate(context, R.layout.view_authorization_content,this)
    }

    private val Mode.showProgress: Boolean
        get() = this === Mode.LOADING || this === Mode.CONFIRM_PROCESSING || this === Mode.DENY_PROCESSING

    fun setViewMode(viewMode: Mode) {
        statusLayout?.setVisible(show = viewMode !== Mode.DEFAULT)
        progressStatusView?.setVisible(show = viewMode.showProgress)
        statusImageView?.setVisible(show = !viewMode.showProgress)
        statusImageResId(viewMode)?.let { statusImageView.setImageResource(it) }
        statusTitleTextView?.setText(statusTitleResId(viewMode))
        statusDescriptionTextView?.setText(statusDescriptionResId(viewMode))
    }

    fun setTitleAndDescription(title: String, description: String) {
        titleTextView?.text = title

        description.hasHTMLTags().let { shouldShowDescriptionView ->
            descriptionTextView?.setVisible(show = !shouldShowDescriptionView)
            descriptionWebView?.setVisible(show = shouldShowDescriptionView)
            if (shouldShowDescriptionView) {
                descriptionWebView?.loadData(description, "text/html; charset=utf-8", "UTF-8")
            } else {
                descriptionTextView?.movementMethod = ScrollingMovementMethod()
                descriptionTextView?.text = description
            }
        }
    }

    fun setActionClickListener(actionViewClickListener: OnClickListener) {
        negativeActionView?.setOnClickListener(actionViewClickListener)
        positiveActionView?.setOnClickListener(actionViewClickListener)
    }

    private fun statusImageResId(viewMode: Mode): ResId? {
        return when(viewMode) {
            Mode.CONFIRM_SUCCESS -> R.drawable.ic_auth_success_70
            Mode.DENY_SUCCESS -> R.drawable.ic_auth_denied_70
            Mode.ERROR -> R.drawable.ic_auth_error_70
            Mode.TIME_OUT -> R.drawable.ic_auth_timeout_70
            Mode.UNAVAILABLE -> R.drawable.ic_auth_error_70
            else -> null
        }
    }

    private fun statusTitleResId(viewMode: Mode): ResId {
        return when(viewMode) {
            Mode.LOADING, Mode.DEFAULT -> R.string.authorizations_loading
            Mode.CONFIRM_PROCESSING, Mode.DENY_PROCESSING -> R.string.authorizations_processing
            Mode.CONFIRM_SUCCESS -> R.string.authorizations_confirmed
            Mode.DENY_SUCCESS -> R.string.authorizations_denied
            Mode.ERROR -> R.string.authorizations_error
            Mode.TIME_OUT -> R.string.authorizations_time_out
            Mode.UNAVAILABLE -> R.string.authorizations_unavailable
        }
    }

    private fun statusDescriptionResId(viewMode: Mode): ResId {
        return when(viewMode) {
            Mode.LOADING, Mode.DEFAULT -> R.string.authorizations_loading_description
            Mode.CONFIRM_PROCESSING, Mode.DENY_PROCESSING -> R.string.authorizations_processing_description
            Mode.CONFIRM_SUCCESS -> R.string.authorizations_confirmed_description
            Mode.DENY_SUCCESS -> R.string.authorizations_denied_description
            Mode.ERROR -> R.string.authorizations_error_description
            Mode.TIME_OUT -> R.string.authorizations_time_out_description
            Mode.UNAVAILABLE -> R.string.authorizations_unavailable
        }
    }
}
