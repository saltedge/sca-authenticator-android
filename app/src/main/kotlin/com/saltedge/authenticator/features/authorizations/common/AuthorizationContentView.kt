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
import com.saltedge.authenticator.tool.hasHTMLTags
import com.saltedge.authenticator.tool.setVisible
import kotlinx.android.synthetic.main.view_authorization_content.view.*

class AuthorizationContentView : LinearLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        inflateView()
    }

    fun setTitle(title: String) {
        titleTextView?.text = title
    }

    fun setDescription(description: String) {
        with(shouldShowDescriptionWebView(description)) {
            descriptionTextView?.setVisible(show = !this)
            descriptionWebView?.setVisible(show = this)
            if (this) {
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

    private fun inflateView() {
        inflate(context, R.layout.view_authorization_content,this)
    }

    private fun shouldShowDescriptionWebView(description: String): Boolean {
        return description.hasHTMLTags()
    }
}
