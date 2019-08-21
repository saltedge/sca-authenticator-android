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
package com.saltedge.authenticator.tool.secure

import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.saltedge.authenticator.model.repository.PreferenceRepository

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
