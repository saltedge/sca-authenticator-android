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
package com.saltedge.authenticator.features.connections.list

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.saltedge.authenticator.R
import com.saltedge.authenticator.widget.list.SpaceItemDecoration
import com.saltedge.authenticator.features.connections.common.ConnectionOptions
import com.saltedge.authenticator.features.connections.common.ConnectionViewModel
import com.saltedge.authenticator.features.connections.connect.ConnectProviderFragment
import com.saltedge.authenticator.features.connections.delete.DeleteConnectionDialog
import com.saltedge.authenticator.features.connections.edit.name.EditConnectionNameDialog
import com.saltedge.authenticator.features.connections.list.di.ConnectionsListModule
import com.saltedge.authenticator.features.connections.options.ConnectionOptionsDialog
import com.saltedge.authenticator.features.main.FabState
import com.saltedge.authenticator.interfaces.ListItemClickListener
import com.saltedge.authenticator.sdk.model.GUID
import com.saltedge.authenticator.tool.*
import com.saltedge.authenticator.widget.fragment.BaseFragment
import kotlinx.android.synthetic.main.fragment_connections_list.*
import javax.inject.Inject

class ConnectionsListFragment : BaseFragment(), ConnectionsListContract.View,
    ListItemClickListener, View.OnClickListener {

    @Inject
    lateinit var presenterContract: ConnectionsListContract.Presenter
    private val adapter = ConnectionsListAdapter(clickListener = this)
    private var optionsDialog: ConnectionOptionsDialog? = null
    private var headerDecorator: SpaceItemDecoration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activityComponents?.updateAppbarTitleWithFabAction(
            title = getString(R.string.connections_feature_title),
            action = FabState.ADD_CONNECTION
        )
        return inflater.inflate(R.layout.fragment_connections_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            activity?.let { connectionsListView?.layoutManager = LinearLayoutManager(it) }
            connectionsListView?.adapter = adapter
            emptyView?.setOnClickListener(this)
            val context = activity ?: return
            headerDecorator = SpaceItemDecoration(
                context = context
            ).apply { connectionsListView?.addItemDecoration(this) }
        } catch (e: Exception) {
            e.log()
        }
    }

    override fun onStart() {
        super.onStart()
        presenterContract.viewContract = this
        updateViewsContent()
    }

    override fun onStop() {
        presenterContract.viewContract = null
        super.onStop()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        presenterContract.onActivityResult(requestCode, resultCode, data)
    }

    override fun onClick(v: View?) {
        presenterContract.onViewClick(v?.id ?: return)
    }

    override fun onListItemClick(itemIndex: Int, itemCode: String, itemViewId: Int) {
        presenterContract.onListItemClick((adapter.getItem(itemIndex) as ConnectionViewModel).guid)
    }

    override fun updateListItemName(connectionGuid: GUID, name: String) {
        adapter.updateListItemName(connectionGuid, name)
    }

    override fun updateViewsContent() {
        presenterContract.getListItems().let {
            headerDecorator?.headerPositions = it.mapIndexed { index, _ -> index }.toTypedArray()
            adapter.data = it
        }
        val viewIsEmpty = adapter.isEmpty
        emptyView?.setVisible(viewIsEmpty)
        connectionsListView?.setVisible(!viewIsEmpty)
    }

    override fun showApiErrorView(message: String) {
        activity?.showWarningDialog(message)
    }

    override fun showSupportView(supportEmail: String?) {
        activity?.startMailApp(supportEmail)
    }

    override fun showConnectView(connectionGuid: GUID) {
        activity?.addFragment(ConnectProviderFragment.newInstance(connectionGuid = connectionGuid))
    }

    override fun showConnectionNameEditView(
        connectionGuid: String,
        connectionName: String,
        requestCode: Int
    ) {
        val dialog = EditConnectionNameDialog.newInstance(connectionGuid, connectionName).also {
            it.setTargetFragment(this, requestCode)
        }
        activity?.showDialogFragment(dialog)
    }

    override fun showDeleteConnectionView(connectionGuid: String?, requestCode: Int) {
        val dialog = DeleteConnectionDialog.newInstance(connectionGuid).also {
            it.setTargetFragment(this, requestCode)
        }
        activity?.showDialogFragment(dialog)
    }

    override fun showOptionsView(
        connectionGuid: String,
        options: Array<ConnectionOptions>,
        requestCode: Int
    ) {
        optionsDialog?.dismiss()
        optionsDialog = ConnectionOptionsDialog.newInstance(connectionGuid, options).also {
            it.setTargetFragment(this, requestCode)
        }
        optionsDialog?.let { activity?.showDialogFragment(it) }
    }

    override fun showQrScanView() {
        activity?.startQrScannerActivity()
    }

    private fun injectDependencies() {
        authenticatorApp?.appComponent?.addConnectionsListModule(ConnectionsListModule())?.inject(
            fragment = this
        )
    }
}
