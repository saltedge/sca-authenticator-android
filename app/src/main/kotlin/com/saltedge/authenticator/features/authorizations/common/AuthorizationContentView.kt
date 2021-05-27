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
import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.fivehundredpx.android.blur.BlurringView
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.buildVersionLessThan23
import com.saltedge.authenticator.core.api.model.DescriptionData
import com.saltedge.authenticator.tools.applyAlphaToColor
import com.saltedge.authenticator.tools.setVisible
import kotlinx.android.synthetic.main.view_authorization_content.view.*

class AuthorizationContentView : LinearLayout {
    private var blurringView: View? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        inflate(context, R.layout.view_authorization_content,this)
        initBlurringView()
        statusLayout.addView(blurringView, 0)
        (blurringView as? BlurringView)?.let {
            it.setBlurredView(blurredView)
            it.invalidate()
        }
    }

    fun setViewMode(viewMode: AuthorizationStatus) {
        val showStatus = viewMode !== AuthorizationStatus.PENDING
        if (showStatus) {
            if (!statusLayout.isVisible) {
                statusLayout.alpha = 0.1f
                statusLayout?.setVisible(show = showStatus)
                statusLayout?.animate()?.setDuration(500)?.alpha(1.0f)?.start()
            }

            progressStatusView?.setVisible(show = viewMode.processingMode)
            statusImageView?.setVisible(show = !viewMode.processingMode)
            viewMode.statusImageResId?.let { statusImageView.setImageResource(it) }
            statusTitleTextView?.setText(viewMode.statusTitleResId)
            statusDescriptionTextView?.setText(viewMode.statusDescriptionResId)
        } else {
            statusLayout?.setVisible(show = showStatus)
        }
    }

    fun setTitleAndDescription(title: String, description: DescriptionData) {
        setTitle(title)
        setDescription(description)
    }

    fun setActionClickListener(actionViewClickListener: OnClickListener) {
        negativeActionView?.setOnClickListener(actionViewClickListener)
        positiveActionView?.setOnClickListener(actionViewClickListener)
    }

    private fun setTitle(title: String) {
        titleTextView?.text = title
    }

    private fun setDescription(description: DescriptionData) {
        val htmlContent = description.hasHtmlContent
        descriptionTextView?.setVisible(show = !htmlContent)
        descriptionWebView?.setVisible(show = htmlContent)
        if (htmlContent) {
            val html = description.html ?: ""
            val encodedHtml = android.util.Base64.encodeToString(html.toByteArray(), android.util.Base64.NO_PADDING)
            descriptionWebView?.loadData(encodedHtml, "text/html", "base64")
        } else {
            descriptionTextView?.movementMethod = ScrollingMovementMethod()
            descriptionTextView?.text = description.text ?: ""
        }
    }

    private fun initBlurringView() {
        val viewLayoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT
        )
        val overlayColor = ContextCompat.getColor(context, R.color.theme_background).applyAlphaToColor(0.8f)
        blurringView = if (buildVersionLessThan23) {
            View(context).apply {
                layoutParams = viewLayoutParams
                setBackgroundColor(overlayColor)
            }
        } else {
            BlurringView(context).apply {
                layoutParams = viewLayoutParams
                setOverlayColor(overlayColor)
                setBlurRadius(24)
                setDownsampleFactor(4)
            }
        }
    }
}
