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
import androidx.recyclerview.widget.LinearLayoutManager
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.KEY_CONNECTION_GUID
import com.saltedge.authenticator.features.connections.common.ConnectionViewModel
import com.saltedge.authenticator.features.connections.list.ConnectionsListAdapter
import com.saltedge.authenticator.interfaces.ListItemClickListener
import com.saltedge.authenticator.sdk.model.GUID
import com.saltedge.authenticator.tools.finishFragment
import com.saltedge.authenticator.tools.setVisible
import com.saltedge.authenticator.widget.fragment.BaseFragment
import com.saltedge.authenticator.widget.list.SpaceItemDecoration
import kotlinx.android.synthetic.main.fragment_connections_list.*

class SelectConnectionsFragment : BaseFragment(), ListItemClickListener {

    private val adapter = ConnectionsListAdapter(clickListener = this)
    private var headerDecorator: SpaceItemDecoration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (arguments?.getSerializable(KEY_CONNECTIONS) as? List<ConnectionViewModel>)?.let {
            adapter.data = it
        }
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
            headerDecorator?.headerPositions = adapter.data.mapIndexed { index, _ -> index }.toTypedArray()
        }
        proceedView.isEnabled = false
        proceedView.setVisible(true)
    }

    override fun onListItemClick(itemIndex: Int, itemCode: String, itemViewId: Int) {
        val item = (adapter.getItem(itemIndex) as ConnectionViewModel)
        adapter.data.filter { (it as ConnectionViewModel).isChecked }.forEach { (it as ConnectionViewModel).isChecked = false }
        item.isChecked = true
        adapter.notifyDataSetChanged()
        proceedView.isEnabled = true
        proceedView.setOnClickListener { proceedConnection(item.guid) }
    }

    private fun proceedConnection(connectionGuid: GUID) {
        activity?.finishFragment()
        val resultIntent = Intent()
        resultIntent.putExtra(KEY_CONNECTION_GUID, connectionGuid)
        targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, resultIntent)
    }

    companion object {
        const val KEY_CONNECTIONS = "CONNECTIONS"

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
