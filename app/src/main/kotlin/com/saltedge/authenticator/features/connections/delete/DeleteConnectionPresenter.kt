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
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.DELETE_ALL_REQUEST_CODE

class DeleteConnectionPresenter(var viewContract: DeleteConnectionContract.View?) {
    var guid: String? = null

    fun viewTitle(requestCode: Int): Int {
        return if (requestCode == DELETE_ALL_REQUEST_CODE) R.string.delete_connections_title
        else R.string.delete_connection_title
    }

    fun viewMessage(requestCode: Int): Int {
        return if (requestCode == DELETE_ALL_REQUEST_CODE) R.string.delete_connections_message
        else R.string.delete_connection_message
    }

    fun onActionViewClick(dialogActionId: Int) {
        when (dialogActionId) {
            DialogInterface.BUTTON_POSITIVE -> {
                viewContract?.dismissView()
                guid?.let { viewContract?.returnSuccessResult(it) }
            }
            DialogInterface.BUTTON_NEGATIVE -> viewContract?.dismissView()
        }
    }
}
