/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.actions

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.ViewModelsFactory
import com.saltedge.authenticator.app.authenticatorApp
import com.saltedge.authenticator.core.api.KEY_DATA
import com.saltedge.authenticator.core.model.ActionAppLinkData
import com.saltedge.authenticator.core.model.GUID
import com.saltedge.authenticator.databinding.FragmentSubmitActionBinding
import com.saltedge.authenticator.features.main.SharedViewModel
import com.saltedge.authenticator.features.main.newAuthorizationListener
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.sdk.api.model.authorization.AuthorizationIdentifier
import com.saltedge.authenticator.tools.navigateTo
import com.saltedge.authenticator.tools.popBackStack
import com.saltedge.authenticator.widget.fragment.BaseFragment
import javax.inject.Inject

class SubmitActionFragment : BaseFragment() {

    @Inject lateinit var viewModelFactory: ViewModelsFactory
    private lateinit var viewModel: SubmitActionViewModel
    private var binding: FragmentSubmitActionBinding? = null
    private val sharedViewModel: SharedViewModel by activityViewModels()

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
        binding = FragmentSubmitActionBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.completeView?.setClickListener { v -> viewModel.onViewClick(v.id) }
        viewModel.onViewCreated()
        sharedViewModel.onSelectConnection.observe(viewLifecycleOwner, Observer<GUID> { result ->
            viewModel.onConnectionSelected(guid = result)
        })
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(SubmitActionViewModel::class.java)
        lifecycle.addObserver(viewModel)

        viewModel.completeViewVisibility.observe(this, Observer<Int> { visibility ->
            binding?.completeView?.visibility = visibility
        })

        viewModel.actionProcessingVisibility.observe(this, Observer<Int> { visibility ->
            binding?.fragmentActionProcessingLayout?.root?.visibility = visibility
        })

        viewModel.onCloseEvent.observe(this, Observer<ViewModelEvent<Unit>> {
            it.getContentIfNotHandled()?.let { popBackStack() }
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
            binding?.completeView?.setIconResource(iconResId)
        })
        viewModel.completeTitleResId.observe(this, Observer<Int> { completeTitleResId ->
            binding?.completeView?.setTitleText(completeTitleResId)
        })
        viewModel.completeDescription.observe(this, Observer<String> { completeMessage ->
            binding?.completeView?.setDescription(completeMessage)
        })
        viewModel.mainActionTextResId.observe(this, Observer<Int> { mainActionTextResId ->
            binding?.completeView?.setMainActionText(mainActionTextResId)
        })
        viewModel.showConnectionsSelectorFragmentEvent.observe(this, Observer<ViewModelEvent<Bundle>> {
                it?.getContentIfNotHandled()?.let { bundle ->
                    navigateTo(actionRes = R.id.select_connections, bundle = bundle)
                }
            })
        viewModel.setInitialData(
            actionAppLinkData = arguments?.getSerializable(KEY_DATA) as? ActionAppLinkData ?: return
        )
    }
}
