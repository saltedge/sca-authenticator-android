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

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.content.pm.Signature
import android.os.Build
import android.util.Base64
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * App Signature checker. Checks if app signed with release certificate
 *
 * @receiver context of Application
 * @return null if nothing to report or non-empty report string
 */
internal fun Context.checkAppSignatures(appSignatures: List<String>): String? {
    var exceptionMessage = ""
    val result = try {
        getSignaturesHashes(this).intersect(appSignatures).isEmpty()
    } catch (e: NameNotFoundException) {
        e.printStackTrace()
        exceptionMessage = e.localizedMessage ?: "NameNotFoundException"
        true
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
        exceptionMessage = e.localizedMessage ?: "NoSuchAlgorithmException"
        true
    }
    return if (result) "AppSignatureInvalid:[$exceptionMessage]" else null
}

internal fun getSignaturesHashes(context: Context): List<String> {
    val md = MessageDigest.getInstance("SHA-256")
    return (getSignatures(context) ?: emptyArray())
            .map { signature ->
                String(Base64.encode(md.apply { update(signature.toByteArray()) }.digest(), Base64.DEFAULT))
                .trimEnd('\n')
            }
}

@Suppress("DEPRECATION")
@SuppressLint("PackageManagerGetSignatures")
internal fun getSignatures(context: Context): Array<out Signature>? {
    return if (Build.VERSION.SDK_INT >= 28) {
        context.packageManager
                .getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                .signingInfo.apkContentsSigners
    } else {
        context.packageManager
                .getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
                .signatures
    }
}
