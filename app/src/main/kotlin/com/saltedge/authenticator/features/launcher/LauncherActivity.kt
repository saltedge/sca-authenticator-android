/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.launcher

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.KEY_DEEP_LINK
import com.saltedge.authenticator.app.LAUNCHER_SPLASH_DURATION
import com.saltedge.authenticator.app.ViewModelsFactory
import com.saltedge.authenticator.app.authenticatorApp
import com.saltedge.authenticator.cloud.registerNotificationChannels
import com.saltedge.authenticator.core.api.KEY_AUTHORIZATION_ID
import com.saltedge.authenticator.core.api.KEY_CONNECTION_ID
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.realm.initRealmDatabase
import com.saltedge.authenticator.tools.*
import javax.inject.Inject

class LauncherActivity : AppCompatActivity() {

    lateinit var viewModel: LauncherViewModel
    @Inject lateinit var viewModelFactory: ViewModelsFactory
    private var dialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.initRealmDatabase()
        this.authenticatorApp?.appComponent?.inject(this)
        setupViewModel()
        this.updateScreenshotLocking()
        this.applyPreferenceLocale()
        this.registerNotificationChannels()
        setContentView(R.layout.activity_launcher)
    }

    override fun onStop() {
        if (dialog?.isShowing == true) dialog?.dismiss()
        super.onStop()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory).get(LauncherViewModel::class.java)
        lifecycle.addObserver(viewModel)

        viewModel.onInitializationSuccess.observe(this, Observer {
            it?.let { proceedToNextScreen() }
        })
        viewModel.onDbInitializationFail.observe(this, Observer {
            it?.let { showDbErrorAlert() }
        })
        viewModel.onSecurityCheckFail.observe(this, Observer {
            it?.let { showSecurityErrorAlert() }
        })
        viewModel.closeEvent.observe(this, Observer {
            it?.let { finishAffinity() }
        })
        viewModel.supportClickEvent.observe(this, Observer<ViewModelEvent<Unit>> { event ->
            event.getContentIfNotHandled()?.let { startMailApp() }
        })
    }

    private fun proceedToNextScreen() {
        Handler().postDelayed({ startActivityWithPreset() }, LAUNCHER_SPLASH_DURATION)
    }

//  TODO: refactor when https://github.com/saltedge/sca-authenticator-android/issues/104 is completed
    private fun startActivityWithPreset() {
        this.startActivity(Intent(this, viewModel.getNextActivityClass())
            .putExtra(KEY_CONNECTION_ID, intent.getStringExtra(KEY_CONNECTION_ID))
            .putExtra(KEY_AUTHORIZATION_ID, intent.getStringExtra(KEY_AUTHORIZATION_ID))
            .putExtra(KEY_DEEP_LINK, intent.dataString)
            .apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
    }

    private fun showDbErrorAlert() {
        dialog = this.showDbErrorDialog(listener = DialogInterface.OnClickListener { _, _ ->
            viewModel.dbErrorCheckedByUser()
        })
    }

    private fun showSecurityErrorAlert() {
        dialog = this.showSecurityAlertDialog(listener = DialogInterface.OnClickListener { _, _ ->
            viewModel.securityErrorCheckedByUser()
        })
    }
}
