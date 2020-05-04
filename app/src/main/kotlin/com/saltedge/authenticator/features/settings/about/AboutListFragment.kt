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
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.ViewModelsFactory
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.features.settings.about.common.AboutAdapter
import com.saltedge.authenticator.features.settings.licenses.LicensesFragment
import com.saltedge.authenticator.interfaces.OnItemClickListener
import com.saltedge.authenticator.tools.addFragment
import com.saltedge.authenticator.tools.authenticatorApp
import com.saltedge.authenticator.tools.log
import com.saltedge.authenticator.widget.fragment.BaseFragment
import com.saltedge.authenticator.widget.fragment.WebViewFragment
import kotlinx.android.synthetic.main.fragment_base_list.*
import javax.inject.Inject

class AboutListFragment : BaseFragment(), OnItemClickListener {

    lateinit var viewModel: AboutViewModel
    @Inject lateinit var viewModelFactory: ViewModelsFactory

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
        activityComponents?.updateAppbar(titleResId = R.string.about_feature_title,
            actionImageResId = R.drawable.ic_appbar_action_back
        )
        return inflater.inflate(R.layout.fragment_base_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    override fun onItemClick(titleName: Int) {
        viewModel.onTitleClick(titleName)
    }

    private fun setupViews() {
        try {
            val layoutManager = LinearLayoutManager(activity)
            recyclerView?.layoutManager = layoutManager
            val dividerItemDecoration = DividerItemDecoration(context, layoutManager.orientation)
            ContextCompat.getDrawable(context ?: return, R.drawable.shape_full_divider)?.let {
                dividerItemDecoration.setDrawable(it)
            }
            recyclerView?.addItemDecoration(dividerItemDecoration)
            recyclerView?.adapter = AboutAdapter(this).apply {
                data = viewModel.getListItems()
            }
        } catch (e: Exception) {
            e.log()
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProviders
            .of(this, viewModelFactory)
            .get(AboutViewModel::class.java)

        viewModel.licenseItemClickEvent.observe(this, Observer<ViewModelEvent<Unit>> {
            it?.let { activity?.addFragment(LicensesFragment()) }
        })
        viewModel.termsOfServiceItemClickEvent.observe(this, Observer<ViewModelEvent<Bundle>> {
            it?.getContentIfNotHandled()?.let { args ->
                activity?.addFragment(WebViewFragment.newInstance(args = args))
            }
        })
    }
}
