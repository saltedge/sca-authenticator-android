/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.models.location

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import timber.log.Timber

interface DeviceLocationManagerAbs {
    val locationDescription: String?
    fun initManager(context: Context)
    fun startLocationUpdates()
    fun locationPermissionsGranted(): Boolean
    fun stopLocationUpdates()
    fun isLocationStateEnabled(): Boolean
    fun enableGps(activity: FragmentActivity)
}

@SuppressLint("StaticFieldLeak")
object DeviceLocationManager : DeviceLocationManagerAbs {

    val permissions = arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
    override var locationDescription: String? = null
        private set

    private lateinit var context: Context
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            locationDescription = result.lastLocation?.headerValue
        }
    }
    private const val REQUEST_LOCATION = 199

    override fun initManager(context: Context) {
        this.context = context
        if (!this::fusedLocationClient.isInitialized) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        }
    }

    override fun startLocationUpdates() {
        if (locationPermissionsGranted()) {
            initManager(context)

            val request = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 60_000).apply {
                setMinUpdateIntervalMillis(10_000)
                setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
                setWaitForAccurateLocation(true)
            }.build()

            if (permissions.any { ContextCompat.checkSelfPermission(context, it) == PERMISSION_GRANTED }) {
                fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
            } else {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    locationDescription = location?.headerValue
                }
            }
        }
    }

    override fun locationPermissionsGranted(): Boolean {
        return permissions.any { ContextCompat.checkSelfPermission(context, it) == PERMISSION_GRANTED }
    }

    override fun isLocationStateEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }

    override fun enableGps(activity: FragmentActivity) {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10_000).apply {
            setMinUpdateIntervalMillis(5_000)
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setWaitForAccurateLocation(true)
        }.build()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val client: SettingsClient = LocationServices.getSettingsClient(activity)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                when (exception.status.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        exception.startResolutionForResult(activity, REQUEST_LOCATION)
                    } catch (sendEx: IntentSender.SendIntentException) {
                        Timber.e(sendEx)
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        activity.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                }
            }
        }
    }

    override fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}

val Location.headerValue: String
    get() = "GEO:${latitude};${longitude}"
