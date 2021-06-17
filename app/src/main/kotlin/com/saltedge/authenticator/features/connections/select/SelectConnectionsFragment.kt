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
package com.saltedge.authenticator.features.connections.select

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.ViewModelsFactory
import com.saltedge.authenticator.features.connections.common.ConnectionItem
import com.saltedge.authenticator.features.connections.list.ConnectionsListAdapter
import com.saltedge.authenticator.features.main.SharedViewModel
import com.saltedge.authenticator.interfaces.ListItemClickListener
import com.saltedge.authenticator.interfaces.OnBackPressListener
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.app.authenticatorApp
import com.saltedge.authenticator.core.model.GUID
import com.saltedge.authenticator.tools.popBackStack
import com.saltedge.authenticator.tools.setVisible
import com.saltedge.authenticator.widget.fragment.BaseFragment
import com.saltedge.authenticator.widget.list.SpaceItemDecoration
import kotlinx.android.synthetic.main.fragment_connections_list.*
import javax.inject.Inject

class SelectConnectionsFragment : BaseFragment(), OnBackPressListener, ListItemClickListener {

    @Inject lateinit var viewModelFactory: ViewModelsFactory
    private lateinit var viewModel: SelectConnectionsViewModel
    private val adapter = ConnectionsListAdapter(clickListener = this)
    private var headerDecorator: SpaceItemDecoration? = null
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
    ): View {
        activityComponents?.updateAppbar(
            titleResId = R.string.choose_connection_feature_title,
            backActionImageResId = R.drawable.ic_appbar_action_close
        )
        return inflater.inflate(R.layout.fragment_connections_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emptyView?.setVisible(false)
        connectionsListView?.setVisible(true)
        activity?.let {
            connectionsListView?.layoutManager = LinearLayoutManager(it)
            connectionsListView?.adapter = adapter
            headerDecorator = SpaceItemDecoration(context = it).apply {
                connectionsListView?.addItemDecoration(this)
            }
        }
        proceedView?.isEnabled = false
        proceedView?.setVisible(true)
    }

    override fun onBackPress(): Boolean {
        sharedViewModel.onSelectConnection("")
        return false
    }

    override fun onListItemClick(itemIndex: Int, itemCode: String, itemViewId: Int) {
        viewModel.onListItemClick(itemIndex)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory).get(SelectConnectionsViewModel::class.java)
        lifecycle.addObserver(viewModel)

        viewModel.listItems.observe(this, Observer<List<ConnectionItem>> {
            headerDecorator?.setHeaderForAllItems(it.count())
            headerDecorator?.footerPositions = arrayOf(it.count() - 1)
            it?.let { adapter.data = it }
        })

        viewModel.onListItemClickEvent.observe(this, Observer<ViewModelEvent<Int>> { event ->
            event.getContentIfNotHandled()?.let { itemIndex ->
                viewModel.listItemsValues.getOrNull(itemIndex)?.let { item ->
                    viewModel.changeStateItem(item)
                    adapter.notifyDataSetChanged()
                    proceedView.isEnabled = true
                    proceedView.setOnClickListener { viewModel.proceedConnection(item.guid) }
                }
            }
        })
        viewModel.onProceedClickEvent.observe(this, Observer<GUID> { connectionGuid ->
            sharedViewModel.onSelectConnection(connectionGuid)
            popBackStack()
        })

        (arguments?.getSerializable(KEY_CONNECTIONS) as? List<ConnectionItem>)?.let {
            viewModel.setInitialData(it)
        }
    }

    companion object {
        const val KEY_CONNECTIONS = "CONNECTIONS"

        fun dataBundle(connections: List<ConnectionItem>): Bundle {
            val arrayList = ArrayList<ConnectionItem>(connections)
            return Bundle().apply { this.putSerializable(KEY_CONNECTIONS, arrayList) }
        }
    }
}
