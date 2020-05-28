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
package com.saltedge.authenticator.features.authorizations.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.ViewModelsFactory
import com.saltedge.authenticator.cloud.clearNotifications
import com.saltedge.authenticator.databinding.AuthorizationsListBinding
import com.saltedge.authenticator.features.authorizations.common.AuthorizationViewModel
import com.saltedge.authenticator.features.authorizations.list.pagers.AuthorizationsContentPagerAdapter
import com.saltedge.authenticator.features.authorizations.list.pagers.AuthorizationsHeaderPagerAdapter
import com.saltedge.authenticator.features.authorizations.list.pagers.PagersScrollSynchronizer
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.tools.ResId
import com.saltedge.authenticator.tools.authenticatorApp
import com.saltedge.authenticator.tools.showQrScannerActivity
import com.saltedge.authenticator.widget.fragment.BaseFragment
import kotlinx.android.synthetic.main.fragment_authorizations_list.*
import javax.inject.Inject

class AuthorizationsListFragment : BaseFragment() {

    @Inject lateinit var viewModelFactory: ViewModelsFactory
    private lateinit var viewModel: AuthorizationsListViewModel
    private lateinit var binding: AuthorizationsListBinding
    private val pagersScrollSynchronizer = PagersScrollSynchronizer()
    private var headerAdapter: AuthorizationsHeaderPagerAdapter? = null
    private var contentAdapter: AuthorizationsContentPagerAdapter? = null


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
        activityComponents?.updateAppbar(
            titleResId = R.string.authorizations_feature_title,
            showMenu = true
        )
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_authorizations_list, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    override fun onStart() {
        super.onStart()
        contentAdapter?.listItemClickListener = viewModel
    }

    override fun onResume() {
        super.onResume()
        activity?.clearNotifications()
        headerAdapter?.startTimer()
    }

    override fun onPause() {
        headerAdapter?.stopTimer()
        super.onPause()
    }

    override fun onStop() {
        contentAdapter?.listItemClickListener = null
        super.onStop()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory).get(AuthorizationsListViewModel::class.java)
        viewModel.bindLifecycleObserver(lifecycle = lifecycle)

        viewModel.listItems.observe(this, Observer<List<AuthorizationViewModel>> {
            headerAdapter?.data = it
            contentAdapter?.data = it
        })
        viewModel.listItemUpdateEvent.observe(this, Observer<ViewModelEvent<Int>> {
            it.getContentIfNotHandled()?.let { itemIndex ->
                viewModel.listItemsValues.getOrNull(itemIndex)?.let { item ->
                    contentAdapter?.updateItem(item, itemIndex)
                    headerAdapter?.updateItem(item, itemIndex)
                }
            }
        })
        viewModel.onConfirmErrorEvent.observe(this, Observer<ViewModelEvent<String>> { event ->
            event.getContentIfNotHandled()?.let { errorMessage ->
                view?.let { Snackbar.make(it, errorMessage, Snackbar.LENGTH_LONG).show() }
            }
        })
        viewModel.onQrScanClickEvent.observe(this, Observer {
            it.getContentIfNotHandled()?.let { activity?.showQrScannerActivity() }
        })
        viewModel.emptyViewImage.observe(this, Observer<ResId> {
            emptyView.setImageResource(it)
        })
        viewModel.emptyViewActionText.observe(this, Observer<ResId?> {
            emptyView.setActionText(it)
        })
        viewModel.emptyViewTitleText.observe(this, Observer<ResId> {
            emptyView.setTitle(it)
        })
        viewModel.emptyViewDescriptionText.observe(this, Observer<ResId> {
            emptyView.setDescription(it)
        })
    }

    private fun setupViews() {
        activity?.let {
            contentAdapter = AuthorizationsContentPagerAdapter(it).apply {
                contentViewPager?.adapter = this
            }
            headerAdapter = AuthorizationsHeaderPagerAdapter(it, viewModel).apply {
                headerViewPager?.adapter = this
            }
        }
        pagersScrollSynchronizer.initViews(headerViewPager, contentViewPager)
        emptyView?.setActionOnClickListener(View.OnClickListener { viewModel.onEmptyViewActionClick() })// { viewModel.onEmptyViewActionClick() }
    }
}
