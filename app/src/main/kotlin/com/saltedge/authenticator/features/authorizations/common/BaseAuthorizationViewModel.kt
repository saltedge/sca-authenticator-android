/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2021 Salt Edge Inc.
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
package com.saltedge.authenticator.features.authorizations.common

import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.LOCATION_PERMISSION_REQUEST_CODE
import com.saltedge.authenticator.features.connections.list.shouldRequestPermission
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.location.DeviceLocationManagerAbs
import com.saltedge.authenticator.tools.ResId
import com.saltedge.authenticator.tools.postUnitEvent

abstract class BaseAuthorizationViewModel(
    private val locationManager: DeviceLocationManagerAbs
) : ViewModel() {

    val onAskPermissionsEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val onGoToSystemSettingsEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val onRequestPermissionEvent = MutableLiveData<Triple<String, String, Boolean>>()
    val onRequestGPSProviderEvent = MutableLiveData<ViewModelEvent<Unit>>()

    abstract fun updateAuthorization(item: AuthorizationItemViewModel, confirm: Boolean)

    fun onPermissionRationaleDialogActionClick(
        dialogActionId: Int,
        actionResId: ResId,
        activity: FragmentActivity? = null
    ) {
        if (dialogActionId == DialogInterface.BUTTON_POSITIVE) {
            when (actionResId) {
                R.string.actions_proceed -> onAskPermissionsEvent.postUnitEvent()
                R.string.actions_go_to_settings -> onGoToSystemSettingsEvent.postUnitEvent()
                R.string.actions_enable -> activity?.let { enableGps(it) }
            }
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE
            && grantResults.any { it == PackageManager.PERMISSION_GRANTED }
        ) {
            locationManager.startLocationUpdates()
        }
    }

    fun onViewItemClick(itemViewId: Int, authorizationModel: AuthorizationItemViewModel?) {
        val confirm = when (itemViewId) {
            R.id.positiveActionView -> true
            R.id.negativeActionView -> false
            else -> return
        }
        authorizationModel?.let {
            val shouldRequestPermission = shouldRequestPermission(
                geolocationRequired = it.geolocationRequired,
                locationPermissionsAreGranted = locationManager.locationPermissionsGranted()
            )
            if (shouldRequestPermission) {
                onRequestPermissionEvent.postValue(
                    Triple(
                        it.connectionID,
                        it.authorizationID,
                        confirm
                    )
                )
            } else if (it.geolocationRequired && !locationManager.isLocationProviderActive()) {
                onRequestGPSProviderEvent.postUnitEvent()
            } else {
                updateAuthorization(item = it, confirm = confirm)
            }
        }
    }

    private fun enableGps(activity: FragmentActivity) {
        val manager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && hasGPSDevice(manager)) {
            locationManager.enableGps(activity)
        }
    }

    private fun hasGPSDevice(manager: LocationManager): Boolean {
        return manager.allProviders.contains(LocationManager.GPS_PROVIDER)
    }
}
