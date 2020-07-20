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
package com.saltedge.authenticator.features.settings.licenses

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.settings.common.SettingsItemViewModel
import com.saltedge.authenticator.interfaces.ListItemClickListener
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.sdk.constants.TERMS_LINK
import com.saltedge.authenticator.widget.fragment.WebViewFragment

class LicensesViewModel(private val appContext: Context) : ViewModel(), ListItemClickListener {
    private val apache2LicenseLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    private val data = listOf(
        Pair(SettingsItemViewModel(titleId = R.string.library_realm), apache2LicenseLink),
        Pair(SettingsItemViewModel(titleId = R.string.library_dagger), apache2LicenseLink),
        Pair(SettingsItemViewModel(titleId = R.string.library_compat), apache2LicenseLink),
        Pair(SettingsItemViewModel(titleId = R.string.library_constraint), apache2LicenseLink),
        Pair(SettingsItemViewModel(titleId = R.string.library_material), apache2LicenseLink),
        Pair(SettingsItemViewModel(titleId = R.string.library_retrofit), apache2LicenseLink),
        Pair(SettingsItemViewModel(titleId = R.string.library_okhttp), apache2LicenseLink),
        Pair(SettingsItemViewModel(titleId = R.string.library_joda), apache2LicenseLink),
        Pair(SettingsItemViewModel(titleId = R.string.library_glide), "https://raw.githubusercontent.com/bumptech/glide/master/LICENSE"),
        Pair(SettingsItemViewModel(titleId = R.string.library_junit), "https://junit.org/junit4/license.html"),
        Pair(SettingsItemViewModel(titleId = R.string.library_jacoco), "https://www.jacoco.org/jacoco/trunk/doc/license.html"),
        Pair(SettingsItemViewModel(titleId = R.string.library_hamcrest), "https://raw.githubusercontent.com/hamcrest/JavaHamcrest/master/LICENSE.txt"),
        Pair(SettingsItemViewModel(titleId = R.string.library_mockito), "https://raw.githubusercontent.com/mockito/mockito/release/2.x/LICENSE"),
        Pair(SettingsItemViewModel(titleId = R.string.library_mockk), apache2LicenseLink),
        Pair(SettingsItemViewModel(titleId = R.string.library_jsr), "https://raw.githubusercontent.com/findbugsproject/findbugs/master/findbugs/licenses/LICENSE-jsr305.txt"),
        Pair(SettingsItemViewModel(titleId = R.string.library_ktlint), "https://raw.githubusercontent.com/pinterest/ktlint/master/LICENSE"),
        Pair(SettingsItemViewModel(titleId = R.string.library_jlleitschuh), "https://raw.githubusercontent.com/JLLeitschuh/ktlint-gradle/master/LICENSE.txt"),
        Pair(SettingsItemViewModel(titleId = R.string.library_blur), "https://raw.githubusercontent.com/500px/500px-android-blur/master/LICENSE.txt")
    )

    val licenseItemClickEvent = MutableLiveData<ViewModelEvent<Bundle>>()
    val listItems = data.map { it.first }

    override fun onListItemClick(itemId: Int) {
        data.firstOrNull { it.first.titleId == itemId }?.let {
            licenseItemClickEvent.postValue(ViewModelEvent(
                WebViewFragment.newBundle(
                    url = it.second,
                    title = appContext.getString(it.first.titleId)
                )
            ))
        }
    }
}
