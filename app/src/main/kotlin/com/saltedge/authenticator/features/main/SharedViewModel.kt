/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.main

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.saltedge.authenticator.core.model.GUID
import com.saltedge.authenticator.models.ViewModelEvent

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
