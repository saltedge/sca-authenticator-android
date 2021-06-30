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
package com.saltedge.authenticator.models.location

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*

interface DeviceLocationManagerAbs {
    val locationDescription: String?
    fun initManager(context: Context)
    fun startLocationUpdates()
    fun locationPermissionsGranted(): Boolean
    fun stopLocationUpdates()
    fun isLocationProviderActive(): Boolean
}

object DeviceLocationManager : DeviceLocationManagerAbs {

    val permissions = arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
    override var locationDescription: String? = null
        private set
    @SuppressLint("StaticFieldLeak")
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            locationDescription = result.lastLocation.headerValue
        }
    }
    private val context: Context
        get() = fusedLocationClient.applicationContext

    override fun initManager(context: Context) {
        if (!this::fusedLocationClient.isInitialized) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        }
    }

    override fun startLocationUpdates() {
        if (locationPermissionsGranted()) {
            initManager(context)

            val request = LocationRequest.create()
            request.interval = 10000
            request.fastestInterval = 5000
            request.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

            if (permissions.any { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }) {
                fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
            } else {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    locationDescription = location?.headerValue
                }
            }
        }
    }

    override fun locationPermissionsGranted(): Boolean {
        val context = fusedLocationClient.applicationContext
        return permissions.any { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
    }

    override fun isLocationProviderActive(): Boolean {
        val providerInfo = Settings.Secure.getString(context.contentResolver, Settings.Secure.LOCATION_PROVIDERS_ALLOWED)
        return providerInfo?.isNotEmpty() == true
    }

    override fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}

val Location.headerValue: String
    get() = "GEO:${latitude};${longitude}"
