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
package com.saltedge.authenticator.features.settings.language

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.main.MainActivityContract
import com.saltedge.authenticator.features.settings.language.di.LanguageSelectModule
import com.saltedge.authenticator.tool.applyPreferenceLocale
import com.saltedge.authenticator.tool.authenticatorApp
import javax.inject.Inject

class LanguageSelectDialog : DialogFragment(), LanguageSelectContract.View {

    @Inject lateinit var presenterContract: LanguageSelectContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity, R.style.MaterialThemeDialog)
            .setTitle(R.string.settings_language)
            .setSingleChoiceItems(
                presenterContract.availableLanguages,
                presenterContract.currentItemIndex
            ) { _, which ->
                presenterContract.currentItemIndex = which
            }
            .setPositiveButton(android.R.string.ok) { _, _ -> presenterContract.onOkClick() }
            .setNegativeButton(R.string.actions_cancel) { _, _ -> closeView() }
            .create()
    }

    override fun closeView() {
        dismiss()
    }

    override fun applyChanges() {
        context?.applyPreferenceLocale()
        (activity as? MainActivityContract.View)?.restartActivity()
    }

    private fun injectDependencies() {
        authenticatorApp?.appComponent?.addLanguageSelectModule(LanguageSelectModule())?.inject(this)
    }
}
