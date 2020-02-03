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
package com.saltedge.authenticator.features.connections.select

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.connections.common.ConnectionViewModel
import com.saltedge.authenticator.features.connections.list.ConnectionsListAdapter
import com.saltedge.authenticator.features.connections.select.di.SelectorConnectionsModule
import com.saltedge.authenticator.interfaces.ListItemClickListener
import com.saltedge.authenticator.interfaces.UpActionImageListener
import com.saltedge.authenticator.tool.ResId
import com.saltedge.authenticator.tool.authenticatorApp
import com.saltedge.authenticator.tool.log
import com.saltedge.authenticator.widget.fragment.BaseFragment
import com.saltedge.authenticator.widget.list.SpaceItemDecoration
import kotlinx.android.synthetic.main.fragment_select_connections.*
import javax.inject.Inject

const val KEY_CONNECTIONS = "CONNECTIONS"

class SelectConnectionsFragment : BaseFragment(), ListItemClickListener,
    SelectConnectionsContract.View, UpActionImageListener {

    @Inject
    lateinit var presenterContract: SelectConnectionsContract.Presenter
    private val adapter = ConnectionsListAdapter(clickListener = this)
    private var headerDecorator: SpaceItemDecoration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
        setHasOptionsMenu(false)
        val data = arguments?.getSerializable(KEY_CONNECTIONS) as List<ConnectionViewModel>
        adapter.data = data
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activityComponents?.updateAppbarTitleWithFabAction(getString(presenterContract.getTitleResId()))
        return inflater.inflate(R.layout.fragment_select_connections, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            activity?.let { connectionsListView?.layoutManager = LinearLayoutManager(it) }
            connectionsListView?.adapter = adapter
            val context = activity ?: return
            headerDecorator = SpaceItemDecoration(
                context = context
            ).apply { connectionsListView?.addItemDecoration(this) }
            headerDecorator?.headerPositions = adapter.data.mapIndexed { index, _ -> index }.toTypedArray()
        } catch (e: Exception) {
            e.log()
        }
    }

    override fun onStart() {
        super.onStart()
        presenterContract.viewContract = this
    }

    override fun onStop() {
        presenterContract.viewContract = null
        super.onStop()
    }

    override fun onListItemClick(itemIndex: Int, itemCode: String, itemViewId: Int) {
        presenterContract.onListItemClick((adapter.getItem(itemIndex) as ConnectionViewModel).guid)
    }

    override fun getUpActionImageResId(): ResId? = R.drawable.ic_close_white_24dp

    override fun returnConnection(connectionGuid: String) {
        (activity as? ConnectionSelectorResult)?.onConnectionSelected(connectionGuid)
    }

    private fun injectDependencies() {
        authenticatorApp?.appComponent?.addSelectorConnectionsModule(SelectorConnectionsModule())?.inject(
            fragment = this
        )
    }

    companion object {
        fun newInstance(connections: List<ConnectionViewModel>): SelectConnectionsFragment {
            val arrayList = ArrayList<ConnectionViewModel>(connections)
            return SelectConnectionsFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(KEY_CONNECTIONS, arrayList)
                }
            }
        }
    }
}
