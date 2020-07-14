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
package com.saltedge.authenticator.features.settings.language

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.tools.currentAppLocaleName
import com.saltedge.authenticator.tools.getAvailableLocalizations
import com.saltedge.authenticator.tools.localeCodeToName
import com.saltedge.authenticator.tools.postUnitEvent

class LanguageSelectViewModel(
    private val appContext: Context,
    private val preferenceRepository: PreferenceRepositoryAbs
) : ViewModel() {

    private var availableLocales = appContext.getAvailableLocalizations().sorted()
    var listItems: Array<String> = availableLocales.map { it.localeCodeToName() }.toTypedArray()
    var selectedItemIndex: Int = listItems.indexOf(appContext.currentAppLocaleName())
    val onLanguageChangedEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val onCloseEvent = MutableLiveData<ViewModelEvent<Unit>>()

    fun onOkClick() {
        onCloseEvent.postUnitEvent()
        if (appContext.currentAppLocaleName() != listItems[selectedItemIndex]) {
            preferenceRepository.currentLocale = availableLocales[selectedItemIndex]
            onLanguageChangedEvent.postUnitEvent()
        }
        onCloseEvent.postUnitEvent()
    }

    fun onCancelClick() {
        onCloseEvent.postUnitEvent()
    }
}
