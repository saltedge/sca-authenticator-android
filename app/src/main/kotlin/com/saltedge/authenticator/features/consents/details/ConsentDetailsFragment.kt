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
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.KEY_GUID
import com.saltedge.authenticator.app.ViewModelsFactory
import com.saltedge.authenticator.databinding.ConsentDetailsBinding
import com.saltedge.authenticator.sdk.constants.KEY_DATA
import com.saltedge.authenticator.sdk.model.ConsentData
import com.saltedge.authenticator.sdk.model.GUID
import com.saltedge.authenticator.tools.authenticatorApp
import com.saltedge.authenticator.widget.fragment.BaseFragment
import javax.inject.Inject

class ConsentDetailsFragment : BaseFragment() {

    @Inject lateinit var viewModelFactory: ViewModelsFactory
    private lateinit var viewModel: ConsentDetailsViewModel
    private lateinit var binding: ConsentDetailsBinding

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
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_consent_details,
            container,
            false
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        updateAppbar(title = viewModel.fragmentTitle.value)
        binding.executePendingBindings()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory).get(ConsentDetailsViewModel::class.java)
//        viewModel.bindLifecycleObserver(lifecycle = lifecycle)

        viewModel.fragmentTitle.observe(this, Observer<String> { title ->
            updateAppbar(title = title)
        })

        viewModel.setInitialData(
            connectionGuid = arguments?.getString(KEY_GUID, "") ?: "",
            data = arguments?.getSerializable(KEY_DATA) as? ConsentData
        )
    }

    private fun updateAppbar(title: String?) {
        activityComponents?.updateAppbar(
            title = title,
            backActionImageResId = R.drawable.ic_appbar_action_back
        )
    }

    companion object {
        /**
         * Create new instance of ConsentDetailsFragment
         *
         * @param consent - consent data
         * @return ConsentDetailsFragment
         */
        fun newInstance(
            connectionGuid: GUID,
            consent: ConsentData
        ): ConsentDetailsFragment {
            return ConsentDetailsFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(KEY_DATA, consent)
                    putSerializable(KEY_GUID, connectionGuid)
                }
            }
        }
    }
}
