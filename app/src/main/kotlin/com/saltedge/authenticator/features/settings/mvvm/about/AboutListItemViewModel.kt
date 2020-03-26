package com.saltedge.authenticator.features.settings.mvvm.about

import com.saltedge.authenticator.features.settings.common.SettingsItemViewModel

class AboutListItemViewModel(item: SettingsItemViewModel) {

    val titleName: Int = item.titleId
    val subTitleName: String? = item.value
    var isVisible: Boolean = item.value != null
}
