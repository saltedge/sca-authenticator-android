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
import android.view.ViewTreeObserver
import androidx.recyclerview.widget.RecyclerView
import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.authorizations.common.AuthorizationViewModel
import com.saltedge.authenticator.features.authorizations.common.remainedSecondsTillExpire
import com.saltedge.authenticator.features.authorizations.common.remainedTimeTillExpire
import com.saltedge.authenticator.interfaces.ListItemClickListener
import com.saltedge.authenticator.tool.*
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.view_item_authorization.*

class AuthorizationItemHolder(
        parent: ViewGroup,
        private val listener: ListItemClickListener?
) : RecyclerView.ViewHolder(parent.inflateListItemView(R.layout.view_item_authorization)),
        LayoutContainer,
        View.OnClickListener,
        ViewTreeObserver.OnGlobalLayoutListener {

    override val containerView: View?
        get() = itemView

    // DATA
    private var confirmItem: AuthorizationViewModel? = null

    init {
        negativeActionView?.setFont(R.font.roboto_medium)
        positiveActionView?.setFont(R.font.roboto_medium)
        negativeActionView?.setOnClickListener(this)
        positiveActionView?.setOnClickListener(this)
        detailsActionView?.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        notifyClickListener(viewId = v?.id ?: return, code = confirmItem?.authorizationId ?: return)
    }

    override fun onGlobalLayout() {
        descriptionTextView?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
        descriptionTextView?.isTextTruncated()?.let {
            detailsActionView?.setVisible(show = it)
        }
    }

    fun bind(newConfirmItem: AuthorizationViewModel) {
        updateProgressViews(newConfirmItem)
        if (confirmItem != newConfirmItem) {
            confirmItem = newConfirmItem
            updateDescriptionViews(newConfirmItem)
            updateLayoutsVisibility(newConfirmItem.isProcessing)
        }
    }

    private fun updateProgressViews(viewModel: AuthorizationViewModel) {
        progressView?.max = viewModel.validSeconds
        progressView?.remainedProgress = viewModel.remainedSecondsTillExpire()
        timerTextView?.text = viewModel.remainedTimeTillExpire()
    }

    private fun updateDescriptionViews(viewModel: AuthorizationViewModel) {
        connectionNameView?.text = viewModel.connectionName
        if (viewModel.connectionLogoUrl?.isEmpty() == true) {
            connectionLogoView?.setVisible(false)
        } else {
            connectionLogoView?.loadImage(
                    imageUrl = viewModel.connectionLogoUrl,
                    placeholderId = R.drawable.ic_logo_bank_placeholder
            )
        }
        titleTextView?.text = viewModel.title
        descriptionTextView?.text = viewModel.description.parseHTML()
    }

    private fun updateLayoutsVisibility(isInProgress: Boolean) {
        headerLayout?.setVisible(show = !isInProgress)
        actionsLayout?.setVisible(show = !isInProgress)
        progressBarView?.setVisible(show = isInProgress)
        updateExpandDescriptionActionView()
    }

    private fun updateExpandDescriptionActionView() {
        val descriptionView = descriptionTextView ?: return
        val isTextTruncated = descriptionView.isTextTruncated()
        if (isTextTruncated != null) {
            detailsActionView?.setVisible(show = isTextTruncated)
        } else {
            detailsActionView?.setVisible(show = false)
            descriptionView.viewTreeObserver.addOnGlobalLayoutListener(this)
        }
    }

    private fun notifyClickListener(viewId: Int, code: String) {
        if (adapterPosition > RecyclerView.NO_POSITION) {
            listener?.onListItemClick(itemIndex = adapterPosition, itemCode = code, itemViewId = viewId)
        }
    }
}
