/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.settings.about

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.settings.common.SettingsItemViewModel
import com.saltedge.authenticator.interfaces.ListItemClickListener
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.app.AppTools
import com.saltedge.authenticator.core.api.TERMS_LINK
import com.saltedge.authenticator.tools.postUnitEvent
import com.saltedge.authenticator.widget.fragment.WebViewFragment

class AboutViewModel(val appContext: Context) : ViewModel(), ListItemClickListener {

    val termsOfServiceItemClickEvent = MutableLiveData<ViewModelEvent<Bundle>>()
    val licenseItemClickEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val listItems = listOf(
        SettingsItemViewModel(
            titleId = R.string.about_app_version,
            description = AppTools.getAppVersionName(appContext)
        ),
        SettingsItemViewModel(
            titleId = R.string.about_copyright,
            description = appContext.getString(R.string.about_copyright_description)
        ),
        SettingsItemViewModel(titleId = R.string.about_terms_service),
        SettingsItemViewModel(titleId = R.string.about_open_source_licenses)
    )

    override fun onListItemClick(itemId: Int) {
        when (itemId) {
            R.string.about_terms_service -> {
                termsOfServiceItemClickEvent.postValue(
                    ViewModelEvent(
                        WebViewFragment.newBundle(
                            url = TERMS_LINK,
                            title = appContext.getString(R.string.about_terms_service)
                        )
                    )
                )
            }
            R.string.about_open_source_licenses -> licenseItemClickEvent.postUnitEvent()
        }
    }
}
