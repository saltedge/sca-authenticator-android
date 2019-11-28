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

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.KEY_GUID
import com.saltedge.authenticator.tool.setButtonsColor

class DeleteConnectionDialog :
    DialogFragment(),
    DeleteConnectionContract.View,
    DialogInterface.OnClickListener {

    private var presenter = DeleteConnectionPresenter(viewContract = this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.guid = arguments?.getString(KEY_GUID)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity!!)
            .setTitle(presenter.viewTitle(targetRequestCode))
            .setPositiveButton(android.R.string.ok, this)
            .setNegativeButton(R.string.actions_cancel, this)
            .setMessage(presenter.viewMessage(targetRequestCode))
            .create()
    }

    override fun onStart() {
        super.onStart()
        (dialog as AlertDialog).setButtonsColor(R.color.blue)
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        presenter.onActionViewClick(which)
    }

    override fun dismissView() {
        dismiss()
    }

    override fun setResultOk(resultIntent: Intent) {
        targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, resultIntent)
    }

    companion object {
        fun newInstance(connectionGuid: String?): DeleteConnectionDialog =
            DeleteConnectionDialog().apply {
                arguments = Bundle().apply { putString(KEY_GUID, connectionGuid) }
            }
    }
}
