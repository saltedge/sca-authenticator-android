/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2019 Salt Edge Inc.
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
package com.saltedge.android.security.checker

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
