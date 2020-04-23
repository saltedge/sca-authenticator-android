/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2020 Salt Edge Inc.
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
package com.saltedge.authenticator.features.actions

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.KEY_GUID
import com.saltedge.authenticator.features.actions.di.SubmitActionModule
import com.saltedge.authenticator.interfaces.UpActionImageListener
import com.saltedge.authenticator.sdk.model.appLink.ActionAppLinkData
import com.saltedge.authenticator.sdk.model.authorization.AuthorizationIdentifier
import com.saltedge.authenticator.tool.*
import com.saltedge.authenticator.widget.fragment.BaseFragment
import kotlinx.android.synthetic.main.fragment_submit_action.*
import javax.inject.Inject

class SubmitActionFragment : BaseFragment(),
    SubmitActionContract.View,
    UpActionImageListener,
    View.OnClickListener {

    @Inject
    lateinit var presenterContract: SubmitActionContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
        presenterContract.setInitialData(
            connectionGuid = arguments?.getString(KEY_GUID) ?: return,
            actionAppLinkData = arguments?.getSerializable(KEY_ACTION_DEEP_LINK_DATA) as? ActionAppLinkData
                ?: return
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activityComponents?.updateAppbarTitle(getString(R.string.action_new_action))
        return inflater.inflate(R.layout.fragment_submit_action, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        completeView?.setOnClickListener(this)
        presenterContract.viewContract = this
        presenterContract.onViewCreated()
    }

    override fun onDestroyView() {
        presenterContract.viewContract = null
        super.onDestroyView()
    }

    override fun getUpActionImageResId(): ResId? = R.drawable.ic_close_white_24dp

    override fun closeView() {
        activity?.finishFragment()
    }

    override fun showErrorAndFinish(message: String) {
        activity?.showWarningDialog(
            message = message,
            listener = DialogInterface.OnClickListener { _: DialogInterface, _: Int ->
                closeView()
            }
        )
    }

    override fun setResultAuthorizationIdentifier(authorizationIdentifier: AuthorizationIdentifier) {
        (activity as? AuthorizationListener)?.onNewAuthorization(authorizationIdentifier)
    }

    override fun onClick(v: View?) {
        presenterContract.onViewClick(view?.id ?: return)
    }

    override fun updateCompleteViewContent(
        iconResId: Int,
        completeTitleResId: Int,
        completeMessageResId: Int,
        mainActionTextResId: Int
    ) {
        completeView?.setIconResource(iconResId)
        completeView?.setTitleText(completeTitleResId)
        completeView?.setSubtitleText(completeMessageResId)
        completeView?.setMainActionText(mainActionTextResId)
    }

    override fun setProcessingVisibility(show: Boolean) {
        completeView?.setVisible(!show)
        fragmentActionProcessingLayout?.setVisible(show)
    }

    override fun openLink(url: String) {
        context?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private fun injectDependencies() {
        authenticatorApp?.appComponent?.addActionModule(SubmitActionModule())?.inject(
            this
        )
    }

    companion object {
        const val KEY_ACTION_DEEP_LINK_DATA = "ACTION_DEEP_LINK_DATA"

        fun newInstance(
            connectionGuid: String,
            actionAppLinkData: ActionAppLinkData
        ): SubmitActionFragment {
            return SubmitActionFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(KEY_ACTION_DEEP_LINK_DATA, actionAppLinkData)
                    putString(KEY_GUID, connectionGuid)
                }
            }
        }
    }
}
