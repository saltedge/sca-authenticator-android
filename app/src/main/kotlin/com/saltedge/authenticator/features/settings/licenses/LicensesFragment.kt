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
package com.saltedge.authenticator.features.settings.licenses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.ViewModelsFactory
import com.saltedge.authenticator.features.settings.common.SettingsAdapter
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.tools.addFragment
import com.saltedge.authenticator.tools.authenticatorApp
import com.saltedge.authenticator.widget.fragment.BaseFragment
import com.saltedge.authenticator.widget.fragment.WebViewFragment
import kotlinx.android.synthetic.main.fragment_base_list.*
import javax.inject.Inject

/**
 * Show licenses list
 */
class LicensesFragment : BaseFragment() {

    @Inject lateinit var viewModelFactory: ViewModelsFactory
    lateinit var viewModel: LicensesViewModel

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
            titleResId = R.string.about_open_source_licenses,
            backActionImageResId = R.drawable.ic_appbar_action_back
        )
        setupViews()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory).get(LicensesViewModel::class.java)

        viewModel.licenseItemClickEvent.observe(this, Observer<ViewModelEvent<Bundle>> {
            it.getContentIfNotHandled()?.let { bundle ->
                activity?.addFragment(WebViewFragment.newInstance(bundle))
            }
        })
    }

    private fun setupViews() {
        recyclerView?.layoutManager = LinearLayoutManager(activity)
        recyclerView?.adapter = SettingsAdapter(listener = viewModel)
            .apply { data = viewModel.listItems }
    }
}
