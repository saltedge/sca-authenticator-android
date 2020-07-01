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
package com.saltedge.authenticator.features.consents

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.KEY_GUID
import com.saltedge.authenticator.app.ViewModelsFactory
import com.saltedge.authenticator.databinding.ConsentsListBinding
import com.saltedge.authenticator.features.connections.common.ConnectionViewModel
import com.saltedge.authenticator.features.consents.common.ConsentItemViewModel
import com.saltedge.authenticator.interfaces.ListItemClickListener
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.sdk.model.ConsentData
import com.saltedge.authenticator.tools.authenticatorApp
import com.saltedge.authenticator.tools.loadRoundedImage
import com.saltedge.authenticator.tools.stopRefresh
import com.saltedge.authenticator.widget.fragment.BaseFragment
import com.saltedge.authenticator.widget.list.SpaceItemDecoration
import kotlinx.android.synthetic.main.fragment_consents_list.*
import kotlinx.android.synthetic.main.fragment_consents_list.subTitleView
import kotlinx.android.synthetic.main.fragment_consents_list.titleView
import javax.inject.Inject

const val KEY_CONSENTS = "consents"

class ConsentsListFragment : BaseFragment(),
    ListItemClickListener {

    @Inject lateinit var viewModelFactory: ViewModelsFactory
    private lateinit var viewModel: ConsentsListViewModel
    private lateinit var binding: ConsentsListBinding
    private val adapter = ConsentsListAdapter(clickListener = this)
    private var headerDecorator: SpaceItemDecoration? = null

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
            titleResId = R.string.consents_feature_title,
            backActionImageResId = R.drawable.ic_appbar_action_back
        )
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_consents_list,
            container,
            false
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    override fun onListItemClick(itemIndex: Int, itemCode: String, itemViewId: Int) {
        viewModel.onListItemClick(itemIndex)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(ConsentsListViewModel::class.java)
        lifecycle.addObserver(viewModel)

        viewModel.listItems.observe(this, Observer<List<ConsentItemViewModel>> {
            headerDecorator?.setHeaderForAllItems(it.count())
            headerDecorator?.footerPositions = arrayOf(it.count() - 1)
            adapter.data = it
        })
        viewModel.connectionItem.observe(this, Observer<ConnectionViewModel> {
            titleView?.text = it.name
            subTitleView?.text = it.consentDescription
            logoImageView?.loadRoundedImage(
                imageUrl = it.logoUrl,
                placeholderId = R.drawable.shape_bg_app_logo,
                cornerRadius = resources.getDimension(R.dimen.connections_list_logo_radius)
            )
        })

        viewModel.setInitialData(
            connectionGuid = arguments?.getString(KEY_GUID),
            consents = arguments?.getSerializable(KEY_CONSENTS) as List<ConsentData>
        )
        viewModel.onListItemClickEvent.observe(this, Observer<ViewModelEvent<Int>> { event ->
            event.getContentIfNotHandled()?.let { itemIndex ->
                //TODO: Show details of Consent
            }
        })
    }

    private fun setupViews() {
        activity?.let {
            consentsListView?.layoutManager = LinearLayoutManager(it)
            consentsListView?.adapter = adapter
            headerDecorator = SpaceItemDecoration(context = it).apply {
                consentsListView?.addItemDecoration(this)
            }
        }
        swipeRefreshLayout?.setOnRefreshListener {
            viewModel.refreshConsents()
            swipeRefreshLayout?.stopRefresh()
        }
        swipeRefreshLayout?.setColorSchemeResources(R.color.primary, R.color.red, R.color.green)
        binding.executePendingBindings()
    }


    companion object {
        fun newInstance(bundle: Bundle): ConsentsListFragment {
            return ConsentsListFragment().apply { arguments = bundle }
        }
    }
}
