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
package com.saltedge.authenticator.features.connections.delete

import android.content.DialogInterface
import android.content.Intent
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.DELETE_ALL_REQUEST_CODE
import com.saltedge.authenticator.app.KEY_GUID

class DeleteConnectionPresenter(var viewContract: DeleteConnectionContract.View?) {
    var guid: String? = null

    fun viewTitle(requestCode: Int): Int {
        return if (requestCode == DELETE_ALL_REQUEST_CODE) R.string.ui_dialog_clear_data_title
        else R.string.ui_dialog_delete_title
    }

    fun viewMessage(requestCode: Int): Int {
        return if (requestCode == DELETE_ALL_REQUEST_CODE) R.string.ui_dialog_clear_data_question
        else R.string.ui_dialog_delete_question
    }

    fun onActionViewClick(dialogActionId: Int) {
        when (dialogActionId) {
            DialogInterface.BUTTON_POSITIVE -> {
                viewContract?.dismissView()
                viewContract?.setResultOk(Intent().putExtra(KEY_GUID, guid))
            }
            DialogInterface.BUTTON_NEGATIVE -> viewContract?.dismissView()
        }
    }
}
