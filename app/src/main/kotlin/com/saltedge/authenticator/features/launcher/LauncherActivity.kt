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
package com.saltedge.authenticator.features.launcher

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.saltedge.authenticator.app.KEY_DEEP_LINK
import com.saltedge.authenticator.features.launcher.di.LauncherModule
import com.saltedge.authenticator.model.realm.RealmManager
import com.saltedge.authenticator.sdk.constants.KEY_AUTHORIZATION_ID
import com.saltedge.authenticator.sdk.constants.KEY_CONNECTION_ID
import com.saltedge.authenticator.tool.*
import com.saltedge.authenticator.tool.secure.updateScreenshotLocking
import javax.inject.Inject

class LauncherActivity : AppCompatActivity() {

    lateinit var viewModel: LauncherViewModel
    @Inject lateinit var viewModelFactory: LauncherViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
        setupViewModel()
        this.updateScreenshotLocking()
        this.applyPreferenceLocale()
        this.registerNotificationChannels()
    }

    override fun onResume() {
        super.onResume()
        if (!AppTools.isTestsSuite(this)) RealmManager.initRealm(context = this)
        if (RealmManager.errorOccurred) showDbError() else startActivityWithPreset()
    }

    private fun startActivityWithPreset() {
        viewModel.setupApplication()

        this.startActivity(Intent(this, viewModel.getNextActivityClass())
            .putExtra(KEY_CONNECTION_ID, intent.getStringExtra(KEY_CONNECTION_ID))
            .putExtra(KEY_AUTHORIZATION_ID, intent.getStringExtra(KEY_AUTHORIZATION_ID))
            .putExtra(KEY_DEEP_LINK, intent.dataString)
            .apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
    }

    private fun injectDependencies() {
        authenticatorApp?.appComponent?.addLauncherModule(LauncherModule())?.inject(this)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProviders
            .of(this, viewModelFactory)
            .get(LauncherViewModel::class.java)
    }

    private fun showDbError() {
        this.showDbErrorDialog(DialogInterface.OnClickListener { _, _ ->
            RealmManager.resetError()
            finishAffinity()
        })
    }
}
