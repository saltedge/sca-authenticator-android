/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.launcher

import android.app.Activity
import android.content.Context
import androidx.lifecycle.*
import com.saltedge.android.security.RaspChecker
import com.saltedge.authenticator.BuildConfig
import com.saltedge.authenticator.features.main.MainActivity
import com.saltedge.authenticator.features.onboarding.OnboardingSetupActivity
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.realm.RealmManagerAbs
import com.saltedge.authenticator.models.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.app.AppTools
import com.saltedge.authenticator.tools.PasscodeToolsAbs
import com.saltedge.authenticator.tools.postUnitEvent
import timber.log.Timber

class LauncherViewModel(
    val appContext: Context,
    val preferenceRepository: PreferenceRepositoryAbs,
    val passcodeTools: PasscodeToolsAbs,
    val realmManager: RealmManagerAbs
) : ViewModel(), LifecycleObserver {

    val onDbInitializationFail = MutableLiveData<ViewModelEvent<Unit>>()
    val onInitializationSuccess = MutableLiveData<ViewModelEvent<Unit>>()
    val onSecurityCheckFail = MutableLiveData<ViewModelEvent<Unit>>()
    val closeEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val supportClickEvent = MutableLiveData<ViewModelEvent<Unit>>()
    private val shouldSetupApplication: Boolean
        get() = !preferenceRepository.passcodeExist()

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onLifeCycleResume() {
        val securityCheckNotPassed = !checkAppSecurity()
        when {
            securityCheckNotPassed -> {
                onSecurityCheckFail.postUnitEvent()
            }
            realmManager.errorOccurred -> {
                onInitializationSuccess.value = null
                onDbInitializationFail.postUnitEvent()
            }
            else -> {
                if (shouldSetupApplication) passcodeTools.replacePasscodeKey(appContext)
                onDbInitializationFail.value = null
                onInitializationSuccess.postUnitEvent()
            }
        }
    }

    fun getNextActivityClass(): Class<out Activity> =
        if (shouldSetupApplication) OnboardingSetupActivity::class.java else MainActivity::class.java

    fun dbErrorCheckedByUser() {
        realmManager.resetError()
        closeEvent.postUnitEvent()
    }

    fun securityErrorCheckedByUser() {
        supportClickEvent.postUnitEvent()
    }

    /**
     * Check security breaches. Generated report is logged.
     *
     * @return true if security report is empty or false
     */
    private fun checkAppSecurity(): Boolean {
        return if ("release" == BuildConfig.BUILD_TYPE) {
            val raspFailReport = RaspChecker.collectFailsReport(appContext)

            if (raspFailReport.isNotEmpty()) {
                val errorMessage = "App Is Tempered:[$raspFailReport]"
                Timber.e(Exception(errorMessage))
            }
            raspFailReport.isEmpty()
        } else true
    }
}
