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
import android.graphics.PorterDuff
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.authorizations.common.AuthorizationViewModel
import com.saltedge.authenticator.sdk.tools.remainedExpirationTime
import com.saltedge.authenticator.sdk.tools.secondsFromDate
import com.saltedge.authenticator.tool.loadImage
import com.saltedge.authenticator.tool.setVisible

class AuthorizationsCardPagerAdapter(context: Context) :
    AuthorizationsPagerAdapter(context, R.layout.view_card_item_authorization) {

    override fun updateViewContent(pageView: View, model: AuthorizationViewModel) {
        pageView.findViewById<TextView>(R.id.providerNameView).text = model.connectionName
        val connectionLogoView = pageView.findViewById<ImageView>(R.id.connectionLogoView)
        if (model.connectionLogoUrl?.isEmpty() == true) {
            connectionLogoView?.setVisible(false)
        } else {
            connectionLogoView?.loadImage(
                imageUrl = model.connectionLogoUrl,
                placeholderId = R.drawable.ic_logo_bank_placeholder
            )
        }
        pageView.findViewById<TextView>(R.id.timerTextView)?.text =
            model.expiresAt.remainedExpirationTime()
        pageView.findViewById<ProgressBar>(R.id.progressBar)?.max = model.validSeconds
        pageView.findViewById<ProgressBar>(R.id.progressBar)?.progress =
            model.createdAt.secondsFromDate()
        pageView.findViewById<ProgressBar>(R.id.progressBar)?.progressDrawable?.setColorFilter(
            ContextCompat.getColor(pageView.context, R.color.blue),
            PorterDuff.Mode.SRC_IN
        )
    }
}
