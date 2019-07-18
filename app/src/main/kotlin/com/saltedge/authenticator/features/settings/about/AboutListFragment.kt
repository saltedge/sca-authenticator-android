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
package com.saltedge.authenticator.features.settings.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.settings.about.di.AboutListModule
import com.saltedge.authenticator.features.settings.about.di.DaggerAboutListComponent
import com.saltedge.authenticator.features.settings.common.SettingsAdapter
import com.saltedge.authenticator.features.settings.licenses.LicensesFragment
import com.saltedge.authenticator.tool.addFragment
import com.saltedge.authenticator.tool.log
import com.saltedge.authenticator.widget.fragment.BaseFragment
import com.saltedge.authenticator.widget.fragment.WebViewFragment
import kotlinx.android.synthetic.main.fragment_base_list.*
import javax.inject.Inject

class AboutListFragment : BaseFragment(), AboutListContract.View {

    @Inject lateinit var presenterContract: AboutListContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
            = inflater.inflate(R.layout.fragment_base_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activityComponents?.updateAppbarTitle(getString(R.string.about_feature_title))
        setupViews()
    }

    override fun onStart() {
        super.onStart()
        presenterContract.viewContract = this
    }

    override fun onStop() {
        presenterContract.viewContract = null
        super.onStop()
    }

    override fun openLink(url: String, titleId: Int) {
        activity?.addFragment(WebViewFragment.newInstance(url, getString(titleId)))
    }

    override fun openLicensesList() {
        activity?.addFragment(LicensesFragment())
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
            recyclerView?.adapter = SettingsAdapter(presenterContract).apply {
                data = presenterContract.getListItems()
            }
        } catch (e: Exception) {
            e.log()
        }
    }

    private fun injectDependencies() {
        activity?.let {
            DaggerAboutListComponent.builder().aboutListModule(AboutListModule(it)).build().inject(this)
        }
    }
}
