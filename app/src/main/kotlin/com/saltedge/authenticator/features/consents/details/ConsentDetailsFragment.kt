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
package com.saltedge.authenticator.features.consents.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.ViewModelsFactory
import com.saltedge.authenticator.features.main.SharedViewModel
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.app.authenticatorApp
import com.saltedge.authenticator.databinding.FragmentConsentDetailsBinding
import com.saltedge.authenticator.tools.popBackStack
import com.saltedge.authenticator.tools.showConfirmRevokeConsentDialog
import com.saltedge.authenticator.tools.showWarningDialog
import com.saltedge.authenticator.widget.fragment.BaseFragment
import kotlinx.android.synthetic.main.fragment_consent_details.*
import javax.inject.Inject

class ConsentDetailsFragment : BaseFragment() {

    @Inject lateinit var viewModelFactory: ViewModelsFactory
    private lateinit var viewModel: ConsentDetailsViewModel
    private var binding: FragmentConsentDetailsBinding? = null
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
        binding = FragmentConsentDetailsBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        updateAppbar(title = viewModel.fragmentTitle.value)
        revokeView?.setOnClickListener { viewModel.onRevokeActionClick() }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory).get(ConsentDetailsViewModel::class.java)

        viewModel.fragmentTitle.observe(this, Observer<String> { title ->
            updateAppbar(title = title)
        })
        viewModel.daysLeft.observe(this, Observer<String> { text ->
            binding?.daysLeftView?.text = text
        })
        viewModel.consentTitle.observe(this, Observer<String> { text ->
            binding?.titleView?.text = text
        })
        viewModel.consentDescription.observe(this, { text ->
            binding?.descriptionView?.text = text
        })
        viewModel.accounts.observe(this, { accounts ->
            binding?.accountsView?.setAccounts(accounts)
        })
        viewModel.sharedDataVisibility.observe(this, Observer<Int> { visibility ->
            binding?.sharedDataView?.visibility = visibility
        })
        viewModel.sharedBalanceVisibility.observe(this, Observer<Int> { visibility ->
            binding?.balance?.visibility = visibility
        })
        viewModel.sharedTransactionsVisibility.observe(this, Observer<Int> { visibility ->
            binding?.transactions?.visibility = visibility
        })
        viewModel.consentGranted.observe(this, { text ->
            binding?.grantedView?.text = text
        })
        viewModel.consentExpires.observe(this, Observer<String> { text ->
            binding?.expiresAtView?.text = text
        })
        viewModel.revokeQuestionEvent.observe(this, Observer<ViewModelEvent<String>> { event ->
            event.getContentIfNotHandled()?.let { message ->
                activity?.showConfirmRevokeConsentDialog(message) { _, _ ->
                    viewModel.onRevokeConfirmedByUser()
                }
            }
        })
        viewModel.revokeErrorEvent.observe(this, Observer<ViewModelEvent<String>> { event ->
            event.getContentIfNotHandled()?.let { message -> activity?.showWarningDialog(message) }
        })
        viewModel.revokeSuccessEvent.observe(this, Observer<ViewModelEvent<String>> { event ->
            event.getContentIfNotHandled()?.let { consentId ->
                sharedViewModel.onRevokeConsent(consentId)
                popBackStack()
            }

        })
        viewModel.setInitialData(arguments)
    }

    private fun updateAppbar(title: String?) {
        activityComponents?.updateAppbar(
            title = title,
            backActionImageResId = R.drawable.ic_appbar_action_back
        )
    }
}
