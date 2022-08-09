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
import com.saltedge.authenticator.tools.toDateFormatString
import com.saltedge.authenticator.tools.toDateFormatStringWithUTC
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
        val textContent = description.hasTextContent
        val extraContent = description.hasExtraContent
        val paymentContent = description.hasPaymentContent

        when {
            htmlContent -> {
                showContent(htmlContentIsVisible = true)
                val html = description.html ?: ""
                val encodedHtml = android.util.Base64.encodeToString(html.toByteArray(), android.util.Base64.NO_PADDING)
                descriptionWebView?.loadData(encodedHtml, "text/html", "base64")
            }
            paymentContent -> {
                showContent(paymentContentIsVisible = true)
                description.payment?.payee?.let {
                    payeeView?.setVisible(show = true)
                    payeeView?.setTitle(R.string.description_payee)
                    payeeView?.setDescription(it)
                }
                description.payment?.amount?.let {
                    amountView?.setVisible(show = true)
                    amountView?.setTitle(R.string.description_amount)
                    amountView?.setDescription(it)
                }
                description.payment?.account?.let {
                    accountView?.setVisible(show = true)
                    accountView?.setTitle(R.string.description_account)
                    accountView?.setDescription(it)
                }
                description.payment?.paymentDate?.toDateFormatString(appContext = context)?.let {
                    paymentDateView?.setVisible(show = true)
                    paymentDateView?.setTitle(R.string.description_payment_date)
                    paymentDateView?.setDescription(it)
                }
                description.payment?.fee?.let {
                    feeView?.setVisible(show = true)
                    feeView?.setTitle(R.string.description_fees)
                    feeView?.setDescription(it)
                }
                description.payment?.exchangeRate?.let {
                    exchangeRateView?.setVisible(show = true)
                    exchangeRateView?.setTitle(R.string.description_exchange_rate)
                    exchangeRateView?.setDescription(it)
                }
                description.payment?.reference?.let {
                    referenceView?.setVisible(show = true)
                    referenceView?.setTitle(R.string.description_reference)
                    referenceView?.setDescription(it)
                }
                if (extraContent) showExtraContent(description = description)
            }
            textContent -> {
                showContent(textContentIsVisible = true)
                descriptionTextView?.movementMethod = ScrollingMovementMethod()
                descriptionTextView?.text = description.text ?: ""
                if (extraContent) showExtraContent(description = description)
            }
        }
    }

    private fun showExtraContent(description: DescriptionData) {
        description.extra?.actionDate?.let {
            dateView?.setVisible(show = true)
            dateView?.setTitle(R.string.description_extra_date)
            dateView?.setDescription(it.toDateFormatStringWithUTC(appContext = context))
        }
        description.extra?.device?.let {
            deviceView?.setVisible(show = true)
            deviceView?.setTitle(R.string.description_extra_from)
            deviceView?.setDescription(it)
        }
        description.extra?.location?.let {
            locationView?.setVisible(show = true)
            locationView?.setTitle(R.string.description_extra_location)
            locationView?.setDescription(it)
        }
        description.extra?.ip?.let {
            ipView?.setVisible(show = true)
            ipView?.setTitle(R.string.description_extra_ip)
            ipView?.setDescription(it)
        }
    }

    private fun showContent(textContentIsVisible: Boolean = false, htmlContentIsVisible: Boolean = false, paymentContentIsVisible: Boolean = false) {
        descriptionWebView?.setVisible(show = htmlContentIsVisible)
        descriptionTextView?.setVisible(show = textContentIsVisible)
        paymentView?.setVisible(show = paymentContentIsVisible)
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
