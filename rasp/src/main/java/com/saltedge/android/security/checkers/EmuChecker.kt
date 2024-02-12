/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.android.security.checkers

import android.os.Build

private const val MIN_PROPERTIES_THRESHOLD = 2

/**
 * Device emulator checker. Checks if app started on emulator.
 *
 * @return null if nothing to report or non-empty report string
 */
internal fun checkIfDeviceEmulator(): String? {
    val result = listOf(
            Build.FINGERPRINT.startsWith("generic"),
            Build.FINGERPRINT.startsWith("unknown"),
            Build.BOOTLOADER.contains("unknown"),
            Build.HARDWARE.contains("goldfish"),
            Build.MANUFACTURER.contains("Genymotion"),
            Build.MODEL.contains("Android SDK built for x86"),
            Build.MODEL.contains("sdk"),
            (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")),
            Build.PRODUCT.contains("sdk")
    ).count { it } >= MIN_PROPERTIES_THRESHOLD
    return if (result) "DeviceIsEmulator" else null
}
