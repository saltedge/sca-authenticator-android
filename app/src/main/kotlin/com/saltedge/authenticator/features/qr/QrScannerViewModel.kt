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
package com.saltedge.authenticator.features.qr

import android.content.Context
import android.util.SparseArray
import androidx.core.util.forEach
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.vision.barcode.Barcode
import com.saltedge.authenticator.R
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.tools.isValidDeeplink
import com.saltedge.authenticator.tools.ResId

class QrScannerViewModel(
    val appContext: Context,
    val connectionsRepository: ConnectionsRepositoryAbs
) : ViewModel(), LifecycleObserver {
    val onCloseEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val setActivityResult = MutableLiveData<String>()
    val errorMessageResId = MutableLiveData<ResId?>()
    val descriptionRes: ResId = if (connectionsRepository.isEmpty()) R.string.scan_qr_description_first else R.string.scan_qr_description

    fun onViewClick(viewId: Int) {
        when (viewId) {
            R.id.closeImageView -> onCloseEvent.postValue(ViewModelEvent(Unit))
        }
    }

    fun onReceivedCodes(codes: SparseArray<Barcode>) {
        val deeplinks = mutableListOf<String>()
        codes.forEach { _, value ->
            if (value.displayValue.isValidDeeplink()) deeplinks.add(value.displayValue)
        }
        deeplinks.firstOrNull()?.let { deeplink ->
            setActivityResult.postValue(deeplink)
            onCloseEvent.postValue(ViewModelEvent(Unit))
        }
    }

    fun showErrorMessage(errorNameResId: ResId?) {
        errorMessageResId.postValue(errorNameResId)
    }
}
