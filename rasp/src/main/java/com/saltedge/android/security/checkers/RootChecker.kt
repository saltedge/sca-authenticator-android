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
package com.saltedge.android.security.checkers

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.saltedge.android.security.checkers.RaspConstants.Companion.BINARY_MAGISK
import com.saltedge.android.security.checkers.RaspConstants.Companion.BINARY_SU
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

/**
 * A simple root checker that gives an *indication* if the device is rooted or not.
 * Disclaimer: **root==god**, so there's no 100% way to check for root.
 *
 * @receiver context of Application
 * @return null if nothing to report or non-empty report string
 */
internal fun Context.checkIfDeviceRooted(): String? {
    val result = this.detectRootManagementApps()
            || this.detectPotentiallyDangerousApps()
            || checkForSuBinary()
            || checkForDangerousProps()
            || checkForRWPaths()
            || detectTestKeys()
            || checkSuExists()
            || checkForMagiskBinary()
    return if (result) "DeviceRooted" else null
}

/**
 * Using the PackageManager, check for a list of well known root apps.
 * @link {Constants.knownRootAppsPackages}
 *
 * @receiver - Application Context
 * @return true if one of the apps it's installed
 */
private fun Context.detectRootManagementApps(): Boolean {
    return this.isAnyPackageFromListInstalled(RaspConstants.knownRootAppsPackages)
}

/**
 * Using the PackageManager, check for a list of well known dangerous apps.
 * @link {Constants.knownDangerousAppsPackages}
 *
 * @receiver - Application Context
 * @return true if one of the apps it's installed
 */
private fun Context.detectPotentiallyDangerousApps(): Boolean {
    return this.isAnyPackageFromListInstalled(RaspConstants.knownDangerousAppsPackages)
}

/**
 * Using the PackageManager, check for a list of well known root cloak apps.
 * @link {Constants.knownRootCloakingPackages}
 *
 * @receiver - Application Context
 * @return true if one of the apps it's installed
 */
private fun Context.detectRootCloakingApps(): Boolean {
    return this.isAnyPackageFromListInstalled(RaspConstants.knownRootCloakingPackages)
}

/**
 * Checks various (Constants.suPaths) common locations for the SU binary
 *
 * @return true if found
 */
private fun checkForSuBinary(): Boolean = checkForBinary(BINARY_SU)

/**
 * Checks various (Constants.suPaths) common locations for the magisk binary (a well know root level program)
 *
 * @return true if found
 */
private fun checkForMagiskBinary(): Boolean = checkForBinary(BINARY_MAGISK)

/**
 * Checks for several system properties
 *
 * @return - true if dangerous props are found
 */
private fun checkForDangerousProps(): Boolean {
    val dangerousProps = mapOf("ro.debuggable" to "1", "ro.secure" to "0")
    val currentProperties = propsReader() ?: return false
    val result = currentProperties.any { propertyLine ->
        dangerousProps.keys.any {
            propertyLine.contains(it) && propertyLine.contains("[${dangerousProps[it]}]")
        }
    }
    if (result) Log.e("RootChecker", "Detected Dangerous Props: [${currentProperties.joinToString(", ")}]")
    return result
}

/**
 * When you're root you can change the permissions on common system directories,
 * this method checks if any of these path from Constants.pathsThatShouldNotBeWritable are writable.
 *
 * @return true if one of the dir is writable
 */
private fun checkForRWPaths(): Boolean {
    val result = mountReader()
            ?.map { line -> line.split(" ".toRegex()).dropLastWhile { it.isEmpty() } }
            ?.filter { args ->
                (args.size > 3) && RaspConstants.pathsThatShouldNotBeWritable.any { path ->
                    path.equals(args[1], ignoreCase = true)
                }
            }?.flatMap { args ->
                args[3].split(",".toRegex()).dropLastWhile { it.isEmpty() }
            }?.any {
                it.equals("rw", ignoreCase = true)
            } ?: return false
    if (result) Log.e("RootChecker", "Detected writeable path")
    return result
}

/**
 * Release-Keys and Test-Keys has to do with how the kernel is signed when it is compiled.
 * Test-Keys means it was signed with a custom key generated by a third-party developer.
 *
 * @return true if signed with Test-keys
 */
private fun detectTestKeys(): Boolean {
    val buildTags = android.os.Build.TAGS
    val result = buildTags != null && buildTags.contains("test-keys")
    if (result) Log.e("RootChecker", "Detected test Build.TAG")
    return result
}

/**
 * A variation on the checking for SU, this attempts a 'which su'
 * @return true if su found
 */
private fun checkSuExists(): Boolean {
    var process: Process? = null
    return try {
        process = Runtime.getRuntime().exec(arrayOf("which", BINARY_SU))
        var line = BufferedReader(InputStreamReader(process.inputStream)).readLine()
        if (line != null) Log.e("RootChecker", "Detected SU: $line ")
        line != null
    } catch (t: Throwable) {
        false
    } finally {
        process?.destroy()
    }
}


/**
 * Check if any package in the list is installed
 *
 * @receiver - Application Context
 * @param packages - list of packages to search for
 * @return true if any of the packages are installed
 */
private fun Context.isAnyPackageFromListInstalled(packages: Array<String>): Boolean {
    val manager = this.packageManager
    val result = packages.any { manager.packageInstalled(packageName = it) }
    if (result) Log.e("RootChecker", "Detected harmful packages: [${packages.filter { manager.packageInstalled(packageName = it) }.joinToString(separator = ", ")}]")
    return result
}

/**
 * Check if package in the list is installed
 *
 * @receiver - Application Context
 * @param packageName - packageName to search for
 * @return true if the package is installed
 */
private fun PackageManager.packageInstalled(packageName: String): Boolean {
    return try {
        this.getPackageInfo(packageName, 0)
        true
    } catch (ignored: PackageManager.NameNotFoundException) {
        false
    }
}

/**
 * Check existence of file in known SU paths
 *
 * @param filename - check for this existence of this file
 * @return true if found
 */
private fun checkForBinary(filename: String): Boolean {
    return try {
        val r = RaspConstants.suPaths.filter { File(it, filename).exists() }
        if (r.isNotEmpty()) Log.e("RootChecker", "Detected harmful binary: [$filename] in: [${r.joinToString(", ")}]")
        r.isNotEmpty()
    } catch (e: Exception) {
        false
    }
}

private fun propsReader(): Array<String>? = runtimeReader("getprop")

private fun mountReader(): Array<String>? = runtimeReader("mount")

private fun runtimeReader(command: String): Array<String>? {
    try {
        val inputStream = Runtime.getRuntime().exec(command).inputStream ?: return null
        val resultValue = Scanner(inputStream).useDelimiter("\\A").next()
        return resultValue.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    } catch (e: IOException) {
        Timber.e(e)
        return null
    } catch (e: NoSuchElementException) {
        Timber.e(e)
        return null
    }
}
