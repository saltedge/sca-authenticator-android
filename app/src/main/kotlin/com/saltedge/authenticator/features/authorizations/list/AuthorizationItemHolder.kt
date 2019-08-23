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
package com.saltedge.authenticator.features.authorizations.list

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.authorizations.common.AuthorizationViewModel
import com.saltedge.authenticator.features.authorizations.common.remainedTimeTillExpire
import com.saltedge.authenticator.interfaces.ListItemClickListener
import com.saltedge.authenticator.sdk.tools.remainedExpirationTime
import com.saltedge.authenticator.sdk.tools.secondsFromDate
import com.saltedge.authenticator.tool.*
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_authorization_details.*
import kotlinx.android.synthetic.main.view_item_authorization.*
import kotlinx.android.synthetic.main.view_item_authorization.connectionLogoView
import kotlinx.android.synthetic.main.view_item_authorization.progressBar
import kotlinx.android.synthetic.main.view_item_authorization.providerNameView
import kotlinx.android.synthetic.main.view_item_authorization.timerTextView

class AuthorizationItemHolder(
    parent: ViewGroup,
    private val listener: ListItemClickListener?
) : RecyclerView.ViewHolder(parent.inflateListItemView(R.layout.view_item_authorization)),
    LayoutContainer,
    View.OnClickListener {

    override val containerView: View?
        get() = itemView

    // DATA
    private var confirmItem: AuthorizationViewModel? = null

    override fun onClick(v: View?) {
        notifyClickListener(viewId = v?.id ?: return, code = confirmItem?.authorizationId ?: return)
    }

    fun bind(newConfirmItem: AuthorizationViewModel) {
        updateProgressViews(newConfirmItem)
        if (confirmItem != newConfirmItem) {
            confirmItem = newConfirmItem
            updateDescriptionViews(newConfirmItem)
        }
    }

    private fun updateProgressViews(viewModel: AuthorizationViewModel) {
        progressBar?.max = viewModel.validSeconds
        progressBar?.progress = viewModel.createdAt.secondsFromDate()
        timerTextView?.text = viewModel.expiresAt.remainedExpirationTime()
    }

    private fun updateDescriptionViews(viewModel: AuthorizationViewModel) {
        providerNameView?.text = viewModel.connectionName
        if (viewModel.connectionLogoUrl?.isEmpty() == true) {
            connectionLogoView?.setVisible(false)
        } else {
            connectionLogoView?.loadImage(
                imageUrl = viewModel.connectionLogoUrl,
                placeholderId = R.drawable.ic_logo_bank_placeholder
            )
        }
    }

    private fun notifyClickListener(viewId: Int, code: String) {
        if (adapterPosition > RecyclerView.NO_POSITION) {
            listener?.onListItemClick(
                itemIndex = adapterPosition,
                itemCode = code,
                itemViewId = viewId
            )
        }
    }
}
