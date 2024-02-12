/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.connections.delete

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.saltedge.authenticator.R
import com.saltedge.authenticator.core.model.GUID
import com.saltedge.authenticator.features.main.SharedViewModel

class DeleteConnectionDialog : DialogFragment(),
    DeleteConnectionContract.View,
    DialogInterface.OnClickListener
{
    private var presenter = DeleteConnectionPresenter(viewContract = this)
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val safeArgs: DeleteConnectionDialogArgs by navArgs()
    private val guid: String
        get() = safeArgs.guid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.guid = guid
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme)
            .setTitle(presenter.viewTitle(targetRequestCode))
            .setPositiveButton(R.string.actions_delete, this)
            .setNegativeButton(R.string.actions_cancel, this)
            .setMessage(presenter.viewMessage(targetRequestCode))
            .create()
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        presenter.onActionViewClick(which)
    }

    override fun dismissView() {
        dismiss()
    }

    override fun returnSuccessResult(guid: GUID) {
        sharedViewModel.onConnectionDeleted(guid)
    }
}
