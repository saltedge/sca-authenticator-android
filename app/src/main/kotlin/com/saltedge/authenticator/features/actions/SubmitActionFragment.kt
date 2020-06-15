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

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.CONNECTIONS_REQUEST_CODE
import com.saltedge.authenticator.app.KEY_CONNECTION_GUID
import com.saltedge.authenticator.app.ViewModelsFactory
import com.saltedge.authenticator.databinding.SubmitActionBinding
import com.saltedge.authenticator.features.connections.common.ConnectionViewModel
import com.saltedge.authenticator.features.connections.select.SelectConnectionsFragment
import com.saltedge.authenticator.features.main.newAuthorizationListener
import com.saltedge.authenticator.generated.callback.OnClickListener
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.sdk.model.appLink.ActionAppLinkData
import com.saltedge.authenticator.sdk.model.authorization.AuthorizationIdentifier
import com.saltedge.authenticator.tools.*
import com.saltedge.authenticator.widget.fragment.BaseFragment
import kotlinx.android.synthetic.main.fragment_submit_action.*
import javax.inject.Inject

class SubmitActionFragment : BaseFragment(), DialogInterface.OnClickListener {

    @Inject lateinit var viewModelFactory: ViewModelsFactory
    private lateinit var viewModel: SubmitActionViewModel
    private lateinit var binding: SubmitActionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authenticatorApp?.appComponent?.inject(this)
        setupViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activityComponents?.updateAppbar(
            titleResId = R.string.action_new_action_title,
            backActionImageResId = R.drawable.ic_appbar_action_close
        )
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_submit_action,
            container,
            false
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        completeView?.setClickListener(View.OnClickListener { v -> viewModel.onViewClick(v.id) })
        viewModel.onViewCreated()
    }

    override fun onClick(listener: DialogInterface?, dialogActionId: Int) {
        viewModel.onDialogActionIdClick(dialogActionId)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        viewModel.onActivityResult(requestCode, resultCode, data)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(SubmitActionViewModel::class.java)
        lifecycle.addObserver(viewModel)

        viewModel.setInitialData(
            actionAppLinkData = arguments?.getSerializable(KEY_ACTION_DEEP_LINK_DATA) as? ActionAppLinkData
                ?: return
        )

        viewModel.onCloseEvent.observe(this, Observer<ViewModelEvent<Unit>> {
            it.getContentIfNotHandled()?.let { activity?.finishFragment() }
        })

        viewModel.onShowErrorEvent.observe(this, Observer<ViewModelEvent<String>> {
            it.getContentIfNotHandled()?.let { message ->
                activity?.showWarningDialog(
                    message = message,
                    listener = this
                )}
        })
        viewModel.onOpenLinkEvent.observe(this, Observer<ViewModelEvent<Uri>> {
            it.getContentIfNotHandled()?.let { url ->
                context?.startActivity(Intent(Intent.ACTION_VIEW, url))
            }
        })
        viewModel.setResultAuthorizationIdentifier.observe(this, Observer<AuthorizationIdentifier> {
            activity?.newAuthorizationListener?.onNewAuthorization(it)
        })
        viewModel.iconResId.observe(this, Observer<Int> { iconResId ->
            completeView?.setIconResource(iconResId)
        })
        viewModel.completeTitleResId.observe(this, Observer<Int> { completeTitleResId ->
            completeView?.setTitleText(completeTitleResId)
        })
        viewModel.completeDescriptionResId.observe(this, Observer<Int> { completeMessageResId ->
            completeView?.setDescription(completeMessageResId)
        })
        viewModel.mainActionTextResId.observe(this, Observer<Int> { mainActionTextResId ->
            completeView?.setMainActionText(mainActionTextResId)
        })
        viewModel.showConnectionsSelectorFragmentEvent.observe(this, Observer<List<ConnectionViewModel>> {
            val selectConnectionsFragment = SelectConnectionsFragment.newInstance(connections = it)
            selectConnectionsFragment.setTargetFragment(this, CONNECTIONS_REQUEST_CODE)
            activity?.addFragment(
                fragment = selectConnectionsFragment,
                animateTransition = false
            )
        })
    }

    companion object {
        const val KEY_ACTION_DEEP_LINK_DATA = "ACTION_DEEP_LINK_DATA"

        fun newInstance(actionAppLinkData: ActionAppLinkData): SubmitActionFragment {
            return SubmitActionFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(KEY_ACTION_DEEP_LINK_DATA, actionAppLinkData)
                }
            }
        }
    }
}
