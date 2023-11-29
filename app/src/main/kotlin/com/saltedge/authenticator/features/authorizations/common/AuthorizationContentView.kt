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
import android.webkit.WebView
import android.webkit.WebViewClient
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
                statusLayout?.setVisible(show = true)
                statusLayout?.animate()?.setDuration(500)?.alpha(1.0f)?.start()
            }

            progressStatusView?.setVisible(show = viewMode.processingMode)
            statusImageView?.setVisible(show = !viewMode.processingMode)
            viewMode.statusImageResId?.let { statusImageView.setImageResource(it) }
            statusTitleTextView?.setText(viewMode.statusTitleResId)
            statusDescriptionTextView?.setText(viewMode.statusDescriptionResId)
        } else {
            statusLayout?.setVisible(show = false)
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
                descriptionWebView.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        return true
                    }
                }
                descriptionWebView?.loadData(encodedHtml, "text/html", "base64")
            }
            paymentContent -> {
                showContent(paymentContentIsVisible = true)
                description.payment?.paymentDate = null // TODO: remove

                payeeView?.setVisible(show = description.payment?.payee != null)
                description.payment?.payee?.let {
                    payeeView?.setTitle(R.string.description_payee)
                    payeeView?.setDescription(it)
                }

                amountView?.setVisible(show = description.payment?.amount != null)
                description.payment?.amount?.let {
                    amountView?.setTitle(R.string.description_amount)
                    amountView?.setDescription(it)
                }

                accountView?.setVisible(show = description.payment?.account != null)
                description.payment?.account?.let {
                    accountView?.setTitle(R.string.description_account)
                    accountView?.setDescription(it)
                }

                paymentDateView?.setVisible(show = description.payment?.paymentDate != null)
                description.payment?.paymentDate?.toDateFormatString(appContext = context)?.let {
                    paymentDateView?.setTitle(R.string.description_payment_date)
                    paymentDateView?.setDescription(it)
                }

                feeView?.setVisible(show = description.payment?.fee != null)
                description.payment?.fee?.let {
                    feeView?.setTitle(R.string.description_fees)
                    feeView?.setDescription(it)
                }

                exchangeRateView?.setVisible(show = description.payment?.exchangeRate != null)
                description.payment?.exchangeRate?.let {
                    exchangeRateView?.setTitle(R.string.description_exchange_rate)
                    exchangeRateView?.setDescription(it)
                }

                referenceView?.setVisible(show = description.payment?.reference != null)
                description.payment?.reference?.let {
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
        dateView?.setVisible(show = description.extra?.actionDate != null)
        description.extra?.actionDate?.let {
            dateView?.setTitle(R.string.description_extra_date)
            dateView?.setDescription(it.toDateFormatStringWithUTC(appContext = context))
        }

        deviceView?.setVisible(show = description.extra?.device != null)
        description.extra?.device?.let {
            deviceView?.setTitle(R.string.description_extra_from)
            deviceView?.setDescription(it)
        }

        locationView?.setVisible(show = description.extra?.location != null)
        description.extra?.location?.let {
            locationView?.setTitle(R.string.description_extra_location)
            locationView?.setDescription(it)
        }

        ipView?.setVisible(show = description.extra?.ip != null)
        description.extra?.ip?.let {
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
