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
package com.saltedge.authenticator.features.consents.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.ViewModelsFactory
import com.saltedge.authenticator.app.authenticatorApp
import com.saltedge.authenticator.databinding.FragmentConsentsListBinding
import com.saltedge.authenticator.features.main.SharedViewModel
import com.saltedge.authenticator.features.main.showWarningSnack
import com.saltedge.authenticator.interfaces.ListItemClickListener
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.tools.loadImage
import com.saltedge.authenticator.tools.navigateTo
import com.saltedge.authenticator.tools.stopRefresh
import com.saltedge.authenticator.widget.fragment.BaseFragment
import com.saltedge.authenticator.widget.list.SpaceItemDecoration
import kotlinx.android.synthetic.main.fragment_consents_list.*
import javax.inject.Inject

class ConsentsListFragment : BaseFragment(), ListItemClickListener {

    @Inject lateinit var viewModelFactory: ViewModelsFactory
    private lateinit var viewModel: ConsentsListViewModel
    private lateinit var headerDecorator: SpaceItemDecoration
    private var binding: FragmentConsentsListBinding? = null
    private val adapter = ConsentsListAdapter(clickListener = this)
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authenticatorApp?.appComponent?.inject(this)
        setupViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activityComponents?.updateAppbar(
            titleResId = R.string.consents_feature_title,
            backActionImageResId = R.drawable.ic_appbar_action_back
        )
        binding = FragmentConsentsListBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    override fun onListItemClick(itemIndex: Int, itemCode: String, itemViewId: Int) {
        viewModel.onListItemClick(itemIndex)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory).get(ConsentsListViewModel::class.java)

        viewModel.listItems.observe(this, Observer<List<ConsentItem>> {
            headerDecorator.setHeaderForAllItems(it.count())
            headerDecorator.footerPositions = arrayOf(it.count() - 1)
            adapter.data = it
        })
        viewModel.logoUrlData.observe(this, Observer<String> { logoUrl ->
            if (logoUrl == null) { //move this logic to vm
                binding?.connectionLogoView?.setImageDrawable(null)
            } else {
                binding?.connectionLogoView?.loadImage(logoUrl, R.drawable.shape_bg_app_logo)
            }
        })
        viewModel.connectionTitleData.observe(this, Observer<String> { text ->
            binding?.connectionTitleView?.text = text
        })
        viewModel.consentsCount.observe(this, Observer<String> { text ->
            binding?.consentsCountView?.text = text
        })
        viewModel.onListItemClickEvent.observe(this, Observer<ViewModelEvent<Bundle>> { event ->
            event.getContentIfNotHandled()?.let { bundle ->
                navigateTo(actionRes = R.id.consent_details, bundle = bundle)
            }
        })
        viewModel.onConsentRemovedEvent.observe(this, Observer<ViewModelEvent<String>> { event ->
            event.getContentIfNotHandled()?.let { message ->
                activity?.showWarningSnack(message = message, snackBarDuration = Snackbar.LENGTH_SHORT)
            }
        })
        viewModel.setInitialData(arguments)
    }

    private fun setupViews() {
        activity?.let {
            consentsListView?.layoutManager = LinearLayoutManager(it)
            consentsListView?.adapter = adapter
            if (!this::headerDecorator.isInitialized) headerDecorator = SpaceItemDecoration(context = it)
            consentsListView?.addItemDecoration(headerDecorator)
        }
        swipeRefreshLayout?.setOnRefreshListener {
            viewModel.refreshConsents()
            swipeRefreshLayout?.stopRefresh()
        }
        swipeRefreshLayout?.setColorSchemeResources(R.color.primary, R.color.red, R.color.green)
        sharedViewModel.onRevokeConsent.observe(viewLifecycleOwner, Observer<String> { result ->
            viewModel.onRevokeConsent(result)
        })
    }
}
