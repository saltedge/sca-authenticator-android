/*
 * Copyright (c) 2019 Salt Edge Inc.
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
