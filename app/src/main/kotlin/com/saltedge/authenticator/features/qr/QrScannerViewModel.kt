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

import android.content.pm.PackageManager
import android.util.SparseArray
import androidx.core.util.forEach
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.vision.barcode.Barcode
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.CAMERA_PERMISSION_REQUEST_CODE
import com.saltedge.authenticator.core.tools.isValidDeeplink
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.tools.ResId
import com.saltedge.authenticator.tools.postUnitEvent

class QrScannerViewModel(
    val connectionsRepository: ConnectionsRepositoryAbs
) : ViewModel(), LifecycleObserver {
    val onCloseEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val permissionGrantEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val setActivityResult = MutableLiveData<String>()
    val errorMessageResId = MutableLiveData<ResId?>()
    val descriptionRes: ResId = if (connectionsRepository.isEmpty()) R.string.scan_qr_description_first else R.string.scan_qr_description

    fun onViewClick(viewId: Int) {
        if (viewId == R.id.closeImageView) onCloseEvent.postUnitEvent()
    }

    fun onReceivedCodes(codes: SparseArray<Barcode>) {
        val deepLinks = mutableListOf<String>()
        codes.forEach { _, value ->
            if (value.displayValue.isValidDeeplink()) deepLinks.add(value.displayValue)
        }
        deepLinks.firstOrNull()?.let { deepLink ->
            setActivityResult.postValue(deepLink)
            onCloseEvent.postUnitEvent()
        }
    }

    fun onCameraInitException() {
        errorMessageResId.postValue(R.string.errors_camera_init)
    }

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE
            && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
        ) {
            permissionGrantEvent.postUnitEvent()
        } else {
            errorMessageResId.postValue(R.string.errors_permission_denied)
        }
    }

    fun onErrorConfirmed() {
        onCloseEvent.postUnitEvent()
    }
}
