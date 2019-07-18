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
package com.saltedge.android.security.hooks

import android.content.Context
import android.content.pm.PackageManager
import java.io.BufferedReader
import java.io.FileReader

/**
 * Anti-Hooking checkers
 * https://d3adend.org/blog/?p=589
 */
internal fun isHookingFrameworkDetected(context: Context): Boolean {
    return checkInstalledApps(context)
            || checkStackTrace()
            || checkMemory()
}

private val hooksApps = listOf("de.robv.android.xposed.installer", "com.saurik.substrate")

/**
 * Check what is installed on the device
 */
private fun checkInstalledApps(context: Context): Boolean {
    return context.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .map { it.packageName }
            .intersect(hooksApps)
            .isNotEmpty()
}

/**
 * Check the stack trace for suspicious method calls
 */
private fun checkStackTrace(): Boolean {
    try {
        throw Exception("Test")
    } catch (e: Exception) {
        val elements = e.stackTrace
        return elements.filter { it.className == "com.android.internal.os.ZygoteInit" }.size >= 2//Substrate is active on the device.
                || elements.any { it.className == "com.saurik.substrate.MS$2" && it.methodName == "invoked" }//A method on the stack trace has been hooked using Substrate.
                || elements.any { it.className == "de.robv.android.xposed.XposedBridge" && it.methodName == "main" }
                || elements.any { it.className == "de.robv.android.xposed.XposedBridge" && it.methodName == "handleHookedMethod" }
    }

}

//Check for native methods that shouldn’t be native

/**
 * Use /proc/pid/maps to detect suspicious shared objects or JARs loaded into memory.
 */
private fun checkMemory(): Boolean {
    var result = false
    try {
        val mapsFilename = "/proc/${android.os.Process.myPid()}/maps"
        val reader = BufferedReader(FileReader(mapsFilename))
        val libraries = HashSet<String>()
        var line: String?
        do {
            line = reader.readLine()
            line?.let {
                if (it.endsWith(".so") || it.endsWith(".jar")) {
                    libraries.add(line.substring(line.lastIndexOf(" ") + 1))
                }
            }
        } while (line != null)

        result = libraries.any {
            it.contains("com.saurik.substrate") ||  it.contains("XposedBridge.jar")
        }
        reader.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return result
}
