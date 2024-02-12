/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.widget.fragment

import android.app.ProgressDialog
import androidx.fragment.app.Fragment
import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.main.activityComponentsContract
import com.saltedge.authenticator.interfaces.ActivityComponentsContract

abstract class BaseFragment : Fragment() {
    private var progressDialog: ProgressDialog? = null
    val activityComponents: ActivityComponentsContract?
        get() = activity?.activityComponentsContract

    override fun onDestroy() {
        progressDialog?.dismiss()
        progressDialog = null
        super.onDestroy()
    }

    protected fun showLoadProgress() {
        if (progressDialog == null) progressDialog = createProgressDialog()
        progressDialog?.show()
    }

    private fun createProgressDialog(): ProgressDialog? {
        val dialog = ProgressDialog(activity ?: return null, R.style.ProgressDialogTheme)
        dialog.setCancelable(false)
        dialog.setProgressStyle(android.R.style.Widget_ProgressBar_Large)
        return dialog
    }

    protected fun dismissLoadProgress() {
        progressDialog?.dismiss()
    }
}
