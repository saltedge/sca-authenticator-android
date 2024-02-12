/*
 * Copyright (c) 2019 Salt Edge Inc.
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
