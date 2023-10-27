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
import android.view.LayoutInflater
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
import com.saltedge.authenticator.databinding.ViewAuthorizationContentBinding
import com.saltedge.authenticator.tools.applyAlphaToColor
import com.saltedge.authenticator.tools.setVisible
import com.saltedge.authenticator.tools.toDateFormatString
import com.saltedge.authenticator.tools.toDateFormatStringWithUTC

class AuthorizationContentView : LinearLayout {
    private var blurringView: View? = null
    private var binding: ViewAuthorizationContentBinding

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        binding = ViewAuthorizationContentBinding.inflate(LayoutInflater.from(context), this, true)
        initBlurringView()
        binding.statusLayout.addView(blurringView, 0)
        (blurringView as? BlurringView)?.let {
            it.setBlurredView(binding.blurredView)
            it.invalidate()
        }
    }

    fun setViewMode(viewMode: AuthorizationStatus) {
        val showStatus = viewMode !== AuthorizationStatus.PENDING
        if (showStatus) {
            if (!binding.statusLayout.isVisible) {
                binding.statusLayout.alpha = 0.1f
                binding.statusLayout.setVisible(show = true)
                binding.statusLayout.animate()?.setDuration(500)?.alpha(1.0f)?.start()
            }

            binding.progressStatusView.setVisible(show = viewMode.processingMode)
            binding.statusImageView.setVisible(show = !viewMode.processingMode)
            viewMode.statusImageResId?.let { binding.statusImageView.setImageResource(it) }
            binding.statusTitleTextView.setText(viewMode.statusTitleResId)
            binding.statusDescriptionTextView.setText(viewMode.statusDescriptionResId)
        } else {
            binding.statusLayout.setVisible(show = false)
        }
    }

    fun setTitleAndDescription(title: String, description: DescriptionData) {
        setTitle(title)
        setDescription(description)
    }

    fun setActionClickListener(actionViewClickListener: OnClickListener) {
        binding.negativeActionView.setOnClickListener(actionViewClickListener)
        binding.positiveActionView.setOnClickListener(actionViewClickListener)
    }

    private fun setTitle(title: String) {
        binding.titleTextView.text = title
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
                binding.descriptionWebView.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        return true
                    }
                }
                binding.descriptionWebView.loadData(encodedHtml, "text/html", "base64")
            }
            paymentContent -> {
                showContent(paymentContentIsVisible = true)
                description.payment?.paymentDate = null // TODO: remove

                binding.payeeView.setVisible(show = description.payment?.payee != null)
                description.payment?.payee?.let {
                    binding.payeeView.setTitle(R.string.description_payee)
                    binding.payeeView.setDescription(it)
                }

                binding.amountView.setVisible(show = description.payment?.amount != null)
                description.payment?.amount?.let {
                    binding.amountView.setTitle(R.string.description_amount)
                    binding.amountView.setDescription(it)
                }

                binding.accountView.setVisible(show = description.payment?.account != null)
                description.payment?.account?.let {
                    binding.accountView.setTitle(R.string.description_account)
                    binding.accountView.setDescription(it)
                }

                binding.paymentDateView.setVisible(show = description.payment?.paymentDate != null)
                description.payment?.paymentDate?.toDateFormatString(appContext = context)?.let {
                    binding.paymentDateView.setTitle(R.string.description_payment_date)
                    binding.paymentDateView.setDescription(it)
                }

                binding.feeView.setVisible(show = description.payment?.fee != null)
                description.payment?.fee?.let {
                    binding.feeView.setTitle(R.string.description_fees)
                    binding.feeView.setDescription(it)
                }

                binding.exchangeRateView.setVisible(show = description.payment?.exchangeRate != null)
                description.payment?.exchangeRate?.let {
                    binding.exchangeRateView.setTitle(R.string.description_exchange_rate)
                    binding.exchangeRateView.setDescription(it)
                }

                binding.referenceView.setVisible(show = description.payment?.reference != null)
                description.payment?.reference?.let {
                    binding.referenceView.setTitle(R.string.description_reference)
                    binding.referenceView.setDescription(it)
                }
                if (extraContent) showExtraContent(description = description)
            }
            textContent -> {
                showContent(textContentIsVisible = true)
                binding.descriptionTextView.movementMethod = ScrollingMovementMethod()
                binding.descriptionTextView.text = description.text ?: ""
                if (extraContent) showExtraContent(description = description)
            }
        }
    }

    private fun showExtraContent(description: DescriptionData) {
        binding.dateView.setVisible(show = description.extra?.actionDate != null)
        description.extra?.actionDate?.let {
            binding.dateView.setTitle(R.string.description_extra_date)
            binding.dateView.setDescription(it.toDateFormatStringWithUTC(appContext = context))
        }

        binding.deviceView.setVisible(show = description.extra?.device != null)
        description.extra?.device?.let {
            binding.deviceView.setTitle(R.string.description_extra_from)
            binding.deviceView.setDescription(it)
        }

        binding.locationView.setVisible(show = description.extra?.location != null)
        description.extra?.location?.let {
            binding.locationView.setTitle(R.string.description_extra_location)
            binding.locationView.setDescription(it)
        }

        binding.ipView.setVisible(show = description.extra?.ip != null)
        description.extra?.ip?.let {
            binding.ipView.setTitle(R.string.description_extra_ip)
            binding.ipView.setDescription(it)
        }
    }

    private fun showContent(textContentIsVisible: Boolean = false, htmlContentIsVisible: Boolean = false, paymentContentIsVisible: Boolean = false) {
        binding.descriptionWebView.setVisible(show = htmlContentIsVisible)
        binding.descriptionTextView.setVisible(show = textContentIsVisible)
        binding.paymentView.setVisible(show = paymentContentIsVisible)
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
