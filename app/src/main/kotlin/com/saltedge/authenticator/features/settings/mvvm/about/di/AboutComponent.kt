package com.saltedge.authenticator.features.settings.mvvm.about.di

import com.saltedge.authenticator.app.di.FragmentScope
import com.saltedge.authenticator.features.settings.mvvm.about.AboutFragment
import dagger.Component

@FragmentScope
@Component(modules = [AboutModule::class])
interface AboutComponent {

    fun inject(fragment: AboutFragment)
}
