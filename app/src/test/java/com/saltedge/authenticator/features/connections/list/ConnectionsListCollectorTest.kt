/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.connections.list

import com.saltedge.authenticator.features.connections.common.shouldRequestLocationPermission
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ConnectionsListCollectorTest {

    @Test
    @Throws(Exception::class)
    fun shouldRequestPermissionTest() {
        assertTrue(
            shouldRequestLocationPermission(
                geolocationRequired = true,
                locationPermissionsAreGranted = false
            )
        )
        assertFalse(
            shouldRequestLocationPermission(
                geolocationRequired = true,
                locationPermissionsAreGranted = true
            )
        )
        assertFalse(
            shouldRequestLocationPermission(
                geolocationRequired = false,
                locationPermissionsAreGranted = false
            )
        )
        assertFalse(
            shouldRequestLocationPermission(
                geolocationRequired = false,
                locationPermissionsAreGranted = true
            )
        )
    }
}
