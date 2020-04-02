package com.saltedge.authenticator.features.settings.mvvm.about.di

import android.content.Context
import com.saltedge.authenticator.app.di.FragmentScope
import com.saltedge.authenticator.features.settings.mvvm.about.AboutViewModelFactory
import dagger.Module
import dagger.Provides

@Module
class AboutModule(val appContext: Context) {

    @FragmentScope
    @Provides
    fun provideFactory(): AboutViewModelFactory {
        return AboutViewModelFactory(appContext)
    }
}
