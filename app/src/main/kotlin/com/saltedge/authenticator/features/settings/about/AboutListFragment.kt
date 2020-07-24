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
package com.saltedge.authenticator.features.settings.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.ViewModelsFactory
import com.saltedge.authenticator.app.options
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.features.settings.common.SettingsAdapter
import com.saltedge.authenticator.tools.authenticatorApp
import com.saltedge.authenticator.widget.fragment.BaseFragment
import kotlinx.android.synthetic.main.fragment_base_list.*
import javax.inject.Inject

class AboutListFragment : BaseFragment() {

    @Inject lateinit var viewModelFactory: ViewModelsFactory
    lateinit var viewModel: AboutViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authenticatorApp?.appComponent?.inject(this)
        setupViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_base_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activityComponents?.updateAppbar(
            titleResId = R.string.about_feature_title,
            backActionImageResId = R.drawable.ic_appbar_action_back
        )
        setupViews()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory).get(AboutViewModel::class.java)

        viewModel.licenseItemClickEvent.observe(this, Observer<ViewModelEvent<Unit>> {
            it?.let { findNavController().navigate(R.id.license, null, options) }
        })
        viewModel.termsOfServiceItemClickEvent.observe(this, Observer<ViewModelEvent<Bundle>> {
            it?.getContentIfNotHandled()?.let { bundle ->
                findNavController().navigate(R.id.terms_of_services, bundle, options)
            }
        })
    }

    private fun setupViews() {
        recyclerView?.layoutManager = LinearLayoutManager(activity)
        recyclerView?.adapter = SettingsAdapter(listener = viewModel)
            .apply { data = viewModel.listItems }
    }
}
