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
package com.saltedge.android.security.root

internal class Constants {
    companion object {

        const val BINARY_SU = "su"
        const val BINARY_BUSYBOX = "busybox"
        const val BINARY_MAGISK = "magisk"
        val knownRootAppsPackages = arrayOf(
                "com.noshufou.android.su",
                "com.noshufou.android.su.elite",
                "eu.chainfire.supersu",
                "com.koushikdutta.superuser",
                "com.thirdparty.superuser",
                "com.yellowes.su",
                "com.topjohnwu.magisk")

        val knownDangerousAppsPackages = arrayOf(
                "com.koushikdutta.rommanager",
                "com.koushikdutta.rommanager.license",
                "com.dimonvideo.luckypatcher",
                "com.chelpus.lackypatch",
                "com.ramdroid.appquarantine",
                "com.ramdroid.appquarantinepro",
                "com.android.vending.billing.InAppBillingService.COIN",
                "com.chelpus.luckypatcher")

        val knownRootCloakingPackages = arrayOf(
                "com.devadvance.rootcloak",
                "com.devadvance.rootcloakplus",
                "de.robv.android.xposed.installer",
                "com.saurik.substrate",
                "com.zachspong.temprootremovejb",
                "com.amphoras.hidemyroot",
                "com.amphoras.hidemyrootadfree",
                "com.formyhm.hiderootPremium",
                "com.formyhm.hideroot")

        // These must end with a /
        val suPaths = arrayOf(
                "/data/local/",
                "/data/local/bin/",
                "/data/local/xbin/",
                "/sbin/",
                "/su/bin/",
                "/system/bin/",
                "/system/bin/.ext/",
                "/system/bin/failsafe/",
                "/system/sd/xbin/",
                "/system/usr/we-need-root/",
                "/system/xbin/",
                "/cache/",
                "/data/",
                "/dev/")

        val pathsThatShouldNotBeWritable = arrayOf(
                "/system",
                "/system/bin",
                "/system/sbin",
                "/system/xbin",
                "/vendor/bin",
                "/sbin",
                "/etc")
        //"/sys",
        //"/proc",
        //"/dev"
    }
}
