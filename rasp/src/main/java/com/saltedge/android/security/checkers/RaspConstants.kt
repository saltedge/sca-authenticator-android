/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.android.security.checkers

internal class RaspConstants {
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
