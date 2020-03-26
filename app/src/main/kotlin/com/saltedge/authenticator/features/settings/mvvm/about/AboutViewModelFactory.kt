package com.saltedge.authenticator.features.settings.mvvm.about

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AboutViewModelFactory(val appContext: Context, private val onItemClickListener: OnItemClickListener) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AboutViewModel::class.java)) {
            return AboutViewModel(appContext, onItemClickListener) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
