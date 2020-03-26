package com.saltedge.authenticator.features.settings.mvvm.about

import android.content.Context
import androidx.lifecycle.ViewModel
import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.settings.common.SettingsItemViewModel
import com.saltedge.authenticator.tool.AppTools

class AboutViewModel(val appContext: Context, val onItemClickListener: OnItemClickListener): ViewModel() {

    fun getListItems(): List<SettingsItemViewModel> {
        return listOf(
            SettingsItemViewModel(
                titleId = R.string.about_app_version,
                value = AppTools.getAppVersionName(appContext)
            ),
            SettingsItemViewModel(
                titleId = R.string.about_copyright,
                value = appContext.getString(R.string.about_copyright_description)
            ),
            SettingsItemViewModel(
                titleId = R.string.about_terms_service,
                itemIsClickable = true
            ),
            SettingsItemViewModel(
                titleId = R.string.about_open_source_licenses,
                itemIsClickable = true
            )
        )
    }
}
