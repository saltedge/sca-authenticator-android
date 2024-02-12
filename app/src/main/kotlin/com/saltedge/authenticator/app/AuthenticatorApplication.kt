/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.app

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.security.ProviderInstaller
import com.google.firebase.FirebaseApp
import com.saltedge.authenticator.BuildConfig
import com.saltedge.authenticator.app.di.AppComponent
import com.saltedge.authenticator.app.di.AppModule
import com.saltedge.authenticator.app.di.DaggerAppComponent
import com.saltedge.authenticator.models.realm.RealmManager
import com.saltedge.authenticator.sdk.config.ApiV1Config
import com.saltedge.authenticator.sdk.v2.config.ApiV2Config
import com.saltedge.authenticator.tools.createCrashlyticsKit
import net.danlew.android.joda.JodaTimeAndroid
import timber.log.Timber

open class AuthenticatorApplication : Application(), Application.ActivityLifecycleCallbacks {

    private var currentActivityName: String = ""
    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        // JodaTime init
        JodaTimeAndroid.init(this)

        // Crashlytics init
        initFirebaseModules()

        // Patch Security Provider
        patchSecurityProvider()

        if (AppTools.isTestsSuite(this)) RealmManager.initRealm(this)

        appComponent = DaggerAppComponent.builder()
            .appModule(AppModule(applicationContext))
            .build()

        registerActivityLifecycleCallbacks(this)

        ApiV1Config.setupConfig(applicationContext)
        ApiV2Config.setupConfig(applicationContext)

        setupNightMode()
    }

    override fun onActivityPaused(activity: Activity) {
        currentActivityName = ""
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivityName = activity.javaClass.name ?: ""
    }

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityDestroyed(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    private fun initFirebaseModules() {
        FirebaseApp.initializeApp(this)
        createCrashlyticsKit()
    }

    private fun patchSecurityProvider() {
        try {
            if ("release" == BuildConfig.BUILD_TYPE) ProviderInstaller.installIfNeeded(this)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun setupNightMode() {
        AppCompatDelegate.setDefaultNightMode(appComponent.preferenceRepository().nightMode)
    }
}
