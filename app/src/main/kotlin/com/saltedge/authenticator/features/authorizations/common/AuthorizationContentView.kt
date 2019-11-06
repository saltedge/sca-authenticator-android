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
import androidx.core.view.isVisible
import com.saltedge.authenticator.R
import com.saltedge.authenticator.sdk.tools.hasHTMLTags
import com.saltedge.authenticator.tool.setVisible
import kotlinx.android.synthetic.main.view_authorization_content.view.*

class AuthorizationContentView : LinearLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        inflate(context, R.layout.view_authorization_content,this)
    }

    fun setViewMode(viewMode: ViewMode) {
        val showStatus = viewMode !== ViewMode.DEFAULT
        if (showStatus) {
            if (!statusLayout.isVisible) {
                statusLayout.alpha = 0.1f
                statusLayout?.setVisible(show = showStatus)
                statusLayout?.animate()?.setDuration(500)?.alpha(1.0f)?.start()
            }

            progressStatusView?.setVisible(show = viewMode.showProgress)
            statusImageView?.setVisible(show = !viewMode.showProgress)
            viewMode.statusImageResId?.let { statusImageView.setImageResource(it) }
            statusTitleTextView?.setText(viewMode.statusTitleResId)
            statusDescriptionTextView?.setText(viewMode.statusDescriptionResId)
        } else {
            statusLayout?.setVisible(show = showStatus)
        }
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
}
