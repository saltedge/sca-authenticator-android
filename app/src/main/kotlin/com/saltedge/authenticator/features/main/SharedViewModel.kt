/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2020 Salt Edge Inc.
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
package com.saltedge.authenticator.features.main

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.sdk.model.GUID

class SharedViewModel : ViewModel() {
    val newConnectionNameEntered = MutableLiveData<Bundle>()
    val connectionDeleted = MutableLiveData<GUID>()
    val onBottomMenuItemSelected = MutableLiveData<ViewModelEvent<Bundle>>()
    val onSelectConnection = MutableLiveData<GUID>()
    val onRevokeConsent = MutableLiveData<String>()

    fun onNewConnectionNameEntered(item: Bundle) {
        newConnectionNameEntered.value = item
    }

    fun onConnectionDeleted(guid: GUID) {
        connectionDeleted.value = guid
    }

    fun onMenuItemSelected(selection: Bundle) {
        onBottomMenuItemSelected.value = ViewModelEvent(selection)
    }

    fun onSelectConnection(guid: GUID) {
        onSelectConnection.value = guid
    }

    fun onRevokeConsent(consentId: String) {
        onRevokeConsent.value = consentId
    }
}
