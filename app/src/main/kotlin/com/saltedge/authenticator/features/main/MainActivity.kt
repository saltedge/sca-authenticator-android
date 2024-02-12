/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.ViewModelsFactory
import com.saltedge.authenticator.app.authenticatorApp
import com.saltedge.authenticator.databinding.ActivityMainBinding
import com.saltedge.authenticator.features.actions.NewAuthorizationListener
import com.saltedge.authenticator.features.onboarding.OnboardingSetupActivity
import com.saltedge.authenticator.interfaces.*
import com.saltedge.authenticator.models.location.DeviceLocationManager
import com.saltedge.authenticator.models.realm.initRealmDatabase
import com.saltedge.authenticator.tools.ResId
import com.saltedge.authenticator.tools.currentFragmentOnTop
import com.saltedge.authenticator.tools.showQrScannerActivity
import com.saltedge.authenticator.tools.updateScreenshotLocking
import com.saltedge.authenticator.widget.security.LockableActivity
import com.saltedge.authenticator.widget.security.UnlockAppInputView
import javax.inject.Inject

class MainActivity : LockableActivity(), ViewModelContract, SnackbarAnchorContainer, View.OnClickListener {

    override lateinit var viewModel: MainActivityViewModel
    @Inject lateinit var viewModelFactory: ViewModelsFactory
    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.initRealmDatabase()
        this.updateScreenshotLocking()
        authenticatorApp?.appComponent?.inject(this)
        setupViewModel()
        setupBinding(layoutInflater)
        viewModel.onLifeCycleCreate(savedInstanceState, intent)
        DeviceLocationManager.initManager(context = this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        viewModel.onNewIntent(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.onActivityResult(requestCode, resultCode, data)
    }

    override fun onResume() {
        super.onResume()
        DeviceLocationManager.startLocationUpdates()
    }

    override fun onPause() {
        DeviceLocationManager.stopLocationUpdates()
        super.onPause()
    }

    /**
     * Redirect back press events to fragments in container
     */
    override fun onBackPressed() {
        val onBackPressListener = currentFragmentOnTop() as? OnBackPressListener
        if (onBackPressListener?.onBackPress() != true) super.onBackPressed()
    }

    /**
     * on security lock of Activity ask current Fragment to close all active dialogs
     */
    override fun onLockActivity() {
        (currentFragmentOnTop() as? DialogHandlerListener)?.closeActiveDialogs()
    }

    override fun onUnlockActivity() {
        viewModel.onUnlock()
    }

    override fun getUnlockAppInputView(): UnlockAppInputView? = binding?.unlockAppInputView

    override fun getSnackbarAnchorView(): View? = binding?.activityRootLayout

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    override fun onClearAppDataEvent() {
        viewModel.onClearAppDataEvent()
    }

    override fun onClick(view: View?) {
        viewModel.onViewClick(viewId = view?.id ?: return)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainActivityViewModel::class.java)
        viewModel.bindLifecycleObserver(lifecycle = lifecycle)

        viewModel.appBarBackActionVisibility.observe(this, Observer<Int> { visibility ->
            binding?.appBarTopSection?.visibility = visibility
        })
        viewModel.appBarBackActionImageResource.observe(this, Observer<ResId> { drawableRes ->
            binding?.appBarBackAction?.setOnClickListener(this)
            val drawable = ContextCompat.getDrawable(this, drawableRes)
            binding?.appBarBackAction?.setImageDrawable(drawable)
        })
        viewModel.appBarTitle.observe(this, Observer<String> { text ->
            binding?.appBarTitle?.text = text
        })
        viewModel.appBarActionQRVisibility.observe(this, Observer<Int> { visibility ->
            binding?.appBarActionQrCode?.setOnClickListener(this)
            binding?.appBarActionQrCode?.visibility = visibility
        })
        viewModel.appBarActionThemeVisibility.observe(this, Observer<Int> { visibility ->
            binding?.appBarActionSwitchTheme?.setOnClickListener(this)
            binding?.appBarActionSwitchTheme?.visibility = visibility
        })
        viewModel.appBarActionMoreVisibility.observe(this, Observer<Int> { visibility ->
            binding?.appBarActionMore?.setOnClickListener(this)
            binding?.appBarActionMore?.visibility = visibility
        })
        viewModel.onAppbarMenuItemClickEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let {
                (this.currentFragmentOnTop() as? AppbarMenuItemClickListener)?.onAppbarMenuItemClick(it)
            }
        })
        viewModel.onBackActionClickEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { onBackPressed() }
        })
        viewModel.onRestartActivityEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { super.restartLockableActivity() }
        })
        viewModel.onShowAuthorizationDetailsEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { bundle ->
                findNavController(R.id.navHostFragment).navigate(R.id.authorizationDetailsFragment, bundle)
            }
        })
        viewModel.onShowConnectEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { bundle ->
                findNavController(R.id.navHostFragment).navigate(R.id.connectProviderFragment, bundle)
            }
        })
        viewModel.onShowSubmitActionEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { bundle ->
                findNavController(R.id.navHostFragment).navigate(R.id.submitActionFragment, bundle)
            }
        })
        viewModel.onQrScanClickEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { this.showQrScannerActivity() }
        })
        viewModel.onShowOnboardingEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let {
                finish()
                startActivity(Intent(this, OnboardingSetupActivity::class.java))
            }
        })
    }

    private fun setupBinding(layoutInflater: LayoutInflater) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
    }
}

val FragmentActivity.newAuthorizationListener: NewAuthorizationListener?
    get() = (this as? ViewModelContract)?.viewModel as? NewAuthorizationListener

val FragmentActivity.activityComponentsContract: ActivityComponentsContract?
    get() = (this as? ViewModelContract)?.viewModel as? ActivityComponentsContract
