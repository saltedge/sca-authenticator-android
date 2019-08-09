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
package com.saltedge.authenticator.app

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.google.android.gms.security.ProviderInstaller
import com.saltedge.android.security.BuildConfig
import com.saltedge.android.security.RaspChecker
import com.saltedge.authenticator.app.di.AppComponent
import com.saltedge.authenticator.app.di.AppModule
import com.saltedge.authenticator.app.di.DaggerAppComponent
import com.saltedge.authenticator.model.realm.RealmManager
import com.saltedge.authenticator.tool.AppTools
import com.saltedge.authenticator.tool.createCrashlyticsKit
import com.saltedge.authenticator.tool.log
import io.fabric.sdk.android.Fabric
import net.danlew.android.joda.JodaTimeAndroid

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
    }

    override fun onActivityPaused(activity: Activity?) {
        currentActivityName = ""
    }

    override fun onActivityResumed(activity: Activity?) {
        currentActivityName = activity?.javaClass?.name ?: ""

        val failResultList = RaspChecker.collectFailsResult(this)
        if (BuildConfig.BUILD_TYPE == "release" && failResultList.isNotEmpty()) {
            val errorClasses = failResultList.joinToString(separator = ", ")
            val errorMessage = "App Is Tempered: $errorClasses"
            val exception = Exception(errorMessage)
            exception.log(errorMessage)
            throw exception
        }
    }

    override fun onActivityStarted(activity: Activity?) {}

    override fun onActivityDestroyed(activity: Activity?) {}

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {}

    override fun onActivityStopped(activity: Activity?) {}

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {}

    private fun initFirebaseModules() {
        Fabric.with(this, createCrashlyticsKit())
    }

    private fun patchSecurityProvider() {
        try {
            ProviderInstaller.installIfNeeded(this)
        } catch (e: Exception) {
            e.log()
        }
    }
}