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
package com.saltedge.authenticator.widget.fragment

import android.app.ProgressDialog
import androidx.fragment.app.Fragment
import com.saltedge.authenticator.R
import com.saltedge.authenticator.interfaces.ActivityComponentsContract

abstract class BaseFragment : Fragment() {

    private var progressDialog: ProgressDialog? = null
    private var actionBarIsHidden: Boolean = false
    val activityComponents: ActivityComponentsContract?
        get() = activity as? ActivityComponentsContract

    override fun onResume() {
        super.onResume()
        if (actionBarIsHidden) activityComponents?.hideActionBar()
        else activityComponents?.showActionBar()
    }

    override fun onDestroy() {
        progressDialog?.dismiss()
        progressDialog = null
        super.onDestroy()
    }

    protected fun hideActionBar() {
        actionBarIsHidden = true
        if (isResumed) activityComponents?.hideActionBar()
    }

    protected fun showLoadProgress() {
        if (progressDialog == null) progressDialog = createProgressDialog()
        progressDialog?.show()
    }

    private fun createProgressDialog(): ProgressDialog? {
        val dialog = ProgressDialog(activity, R.style.ProgressDialogTheme)
        dialog.setCancelable(false)
        dialog.setProgressStyle(android.R.style.Widget_ProgressBar_Large)
        return dialog
    }

    protected fun dismissLoadProgress() {
        progressDialog?.dismiss()
    }
}
