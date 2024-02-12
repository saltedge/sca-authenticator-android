/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.tools

import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.saltedge.authenticator.models.repository.PreferenceRepository

/**
 * Blocks the ability to take screenshots for activity window
 *
 * @receiver AppCompatActivity
 */
fun AppCompatActivity.updateScreenshotLocking() {
    val flag = WindowManager.LayoutParams.FLAG_SECURE
    if (PreferenceRepository.screenshotLockEnabled) {
        this.window.setFlags(flag, flag)
    } else {
        this.window.clearFlags(flag)
    }
}
