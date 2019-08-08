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
package com.saltedge.authenticator.features.settings.passcode.saver

import android.os.AsyncTask
import com.saltedge.authenticator.tool.secure.PasscodeToolsAbs

class PasscodeSaver(
    private val passcodeTools: PasscodeToolsAbs,
    override var callback: PasscodeSaveResultListener?
) : AsyncTask<String, Void, Boolean>(), PasscodeSaverAbs {

    override fun runNewTask(passcode: String) {
        this.execute(passcode)
    }

    override fun doInBackground(vararg params: String?): Boolean {
        return passcodeTools.savePasscode(passcode = params.firstOrNull() ?: return false)
    }

    override fun onPostExecute(result: Boolean) {
        super.onPostExecute(result)
        callback?.passcodeSavedWithResult(result)
    }
}
