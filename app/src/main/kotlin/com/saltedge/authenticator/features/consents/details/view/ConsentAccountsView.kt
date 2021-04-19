/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2020 Salt Edge Inc.
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
package com.saltedge.authenticator.features.consents.details.view

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.saltedge.authenticator.R
import com.saltedge.authenticator.sdk.api.model.AccountData
import com.saltedge.authenticator.tools.setFont

class ConsentAccountsView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val labelColor = ContextCompat.getColor(context, R.color.dark_100_and_grey_40)
    private val separatorColor = ContextCompat.getColor(context, R.color.theme_background)
    private val text16 = resources.getDimension(R.dimen.text_16)
    private val text14 = resources.getDimension(R.dimen.text_14)
    private val dp16 = resources.getDimension(R.dimen.dp_16).toInt()
    private val dp10 = resources.getDimension(R.dimen.dp_10).toInt()
    private val dp6 = resources.getDimension(R.dimen.dp_6).toInt()
    private val dp2 = resources.getDimension(R.dimen.dp_2).toInt()

    init {
        orientation = VERTICAL
        background = ContextCompat.getDrawable(context, R.drawable.shape_bg_passcode_label)
    }

    fun setAccounts(accounts: List<AccountData>?) {
        removeAllViews()
        val accountsList = accounts ?: return
        if (accountsList.isNotEmpty()) setPadding(0, dp6, 0, dp16)
        accountsList.forEachIndexed { index, accountData ->
            addView(createTitleLabel(accountData.name))
            accountData.accountNumber?.let { addView(createIdentifierLabel("Account number: $it")) }
            accountData.sortCode?.let { addView(createIdentifierLabel("Sort code: $it")) }
            accountData.iban?.let { addView(createIdentifierLabel("IBAN: $it")) }
            if (accountsList.lastIndex != index) addView(createSeparator())
        }
    }

    private fun createTitleLabel(value: String): View {
        return TextView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).also {
                it.marginStart = dp16
                it.marginEnd = dp16
                it.topMargin = dp10
            }
            setTextColor(labelColor)
            setFont(R.font.roboto_medium)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, text16)
            setLines(1)
            ellipsize = TextUtils.TruncateAt.END
            text = value
        }
    }

    private fun createIdentifierLabel(value: String): View {
        return TextView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).also {
                it.marginStart = dp16
                it.marginEnd = dp16
                it.topMargin = dp6
            }
            setTextColor(labelColor)
            setFont(R.font.roboto_regular)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, text14)
            setLines(1)
            ellipsize = TextUtils.TruncateAt.END
            text = value
        }
    }

    private fun createSeparator(): View {
        return View(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dp2).also {
                it.topMargin = dp10
            }
            setBackgroundColor(separatorColor)
        }
    }
}
