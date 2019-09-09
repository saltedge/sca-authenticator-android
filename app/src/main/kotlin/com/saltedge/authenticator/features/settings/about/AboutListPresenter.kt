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
package com.saltedge.authenticator.features.settings.about

import android.content.Context
import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.settings.common.SettingsItemViewModel
import com.saltedge.authenticator.sdk.constants.TERMS_LINK
import com.saltedge.authenticator.tool.AppTools
import javax.inject.Inject

class AboutListPresenter @Inject constructor(
    private val appContext: Context
) : AboutListContract.Presenter {

    override var viewContract: AboutListContract.View? = null

    override fun getListItems(): List<SettingsItemViewModel> {
        return listOf(
            SettingsItemViewModel(
                titleId = R.string.about_app_version,
                value = AppTools.getAppVersionName(appContext)
            ),
            SettingsItemViewModel(
                titleId = R.string.about_copyright,
                value = appContext.getString(R.string.about_copyright_description)
            ),
            SettingsItemViewModel(
                titleId = R.string.about_terms_service,
                itemIsClickable = true
            ),
            SettingsItemViewModel(
                titleId = R.string.about_open_source_licenses,
                itemIsClickable = true
            )
        )
    }

    override fun onListItemClick(itemIndex: Int, itemCode: String, itemViewId: Int) {
        when (itemViewId) {
            R.string.about_terms_service -> viewContract?.openLink(TERMS_LINK, itemViewId)
            R.string.about_open_source_licenses -> viewContract?.openLicensesList()
        }
    }

    override fun onListItemCheckedStateChanged(itemId: Int, checked: Boolean) {
    }
}
