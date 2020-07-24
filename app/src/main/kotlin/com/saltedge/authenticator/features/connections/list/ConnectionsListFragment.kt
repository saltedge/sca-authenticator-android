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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.ViewModelsFactory
import com.saltedge.authenticator.databinding.ConnectionsListBinding
import com.saltedge.authenticator.features.connections.common.ConnectionItemViewModel
import com.saltedge.authenticator.features.connections.list.menu.MenuData
import com.saltedge.authenticator.features.connections.list.menu.PopupMenuBuilder
import com.saltedge.authenticator.features.main.SharedViewModel
import com.saltedge.authenticator.interfaces.DialogHandlerListener
import com.saltedge.authenticator.interfaces.ListItemClickListener
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.sdk.model.GUID
import com.saltedge.authenticator.tools.authenticatorApp
import com.saltedge.authenticator.tools.showQrScannerActivity
import com.saltedge.authenticator.tools.startMailApp
import com.saltedge.authenticator.tools.stopRefresh
import com.saltedge.authenticator.widget.fragment.BaseFragment
import com.saltedge.authenticator.widget.list.SpaceItemDecoration
import kotlinx.android.synthetic.main.fragment_connections_list.*
import javax.inject.Inject

class ConnectionsListFragment : BaseFragment(),
    ListItemClickListener,
    View.OnClickListener,
    DialogHandlerListener
{
    @Inject lateinit var viewModelFactory: ViewModelsFactory
    private lateinit var viewModel: ConnectionsListViewModel
    private lateinit var binding: ConnectionsListBinding
    private val adapter = ConnectionsListAdapter(clickListener = this)
    private var headerDecorator: SpaceItemDecoration? = null
    private var popupWindow: PopupWindow? = null
    private var dialogFragment: DialogFragment? = null
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
            titleResId = R.string.connections_feature_title,
            backActionImageResId = R.drawable.ic_appbar_action_back
        )
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_connections_list,
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

    override fun onClick(v: View?) {
        viewModel.onViewClick(v?.id ?: return)
    }

    override fun onListItemClick(itemIndex: Int, itemCode: String, itemViewId: Int) {
        viewModel.onListItemClick(itemIndex)
    }

    override fun onStop() {
        popupWindow?.dismiss()
        swipeRefreshLayout?.stopRefresh()
        super.onStop()
    }

    override fun closeActiveDialogs() {
        if (dialogFragment?.isVisible == true) dialogFragment?.dismiss()
        if (popupWindow?.isShowing == true) popupWindow?.dismiss()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(ConnectionsListViewModel::class.java)
        lifecycle.addObserver(viewModel)

        viewModel.listItems.observe(this, Observer<List<ConnectionItemViewModel>> {
            headerDecorator?.setHeaderForAllItems(it.count())
            headerDecorator?.footerPositions = arrayOf(it.count() - 1)
            adapter.data = it
        })
        viewModel.onQrScanClickEvent.observe(this, Observer<ViewModelEvent<Unit>> {
            it.getContentIfNotHandled()?.let { activity?.showQrScannerActivity() }
        })
        viewModel.onRenameClickEvent.observe(this, Observer<ViewModelEvent<Bundle>> { event ->
            event.getContentIfNotHandled()?.let { bundle ->
                findNavController().navigate(R.id.edit_connection_name_dialog, bundle)
            }
        })
        viewModel.onDeleteClickEvent.observe(this, Observer<ViewModelEvent<String>> { event ->
            event.getContentIfNotHandled()?.let { guid ->
                findNavController().navigate(ConnectionsListFragmentDirections.deleteConnectionDialog(guid))
            }
        })
        viewModel.onListItemClickEvent.observe(this, Observer<ViewModelEvent<MenuData>> { event ->
            event.getContentIfNotHandled()?.let { data -> popupWindow = showPopupMenu(data) }
        })
        viewModel.onReconnectClickEvent.observe(this, Observer<ViewModelEvent<String>> { event ->
            event.getContentIfNotHandled()?.let { guid ->
                findNavController().navigate(ConnectionsListFragmentDirections.connectProvider(guid))
            }
        })
        viewModel.onViewConsentsClickEvent.observe(this, Observer<ViewModelEvent<Bundle>> { event ->
            event.getContentIfNotHandled()?.let { bundle ->
                findNavController().navigate(R.id.consents_list, bundle)
            }
        })
        viewModel.onSupportClickEvent.observe(this, Observer<ViewModelEvent<String?>> { event ->
            event.getContentIfNotHandled()?.let { supportEmail ->
                activity?.startMailApp(supportEmail)
            }
        })
        viewModel.updateListItemEvent.observe(this, Observer<ConnectionItemViewModel> { itemIndex ->
            adapter.updateListItem(itemIndex)
        })
    }

    private fun setupViews() {
        emptyView?.setActionOnClickListener(this)
        activity?.let {
            connectionsListView?.layoutManager = LinearLayoutManager(it)
            connectionsListView?.adapter = adapter
            headerDecorator = SpaceItemDecoration(context = it).apply {
                connectionsListView?.addItemDecoration(this)
            }
        }
        swipeRefreshLayout?.setOnRefreshListener {
            viewModel.refreshConsents()
            swipeRefreshLayout?.stopRefresh()
        }
        swipeRefreshLayout?.setColorSchemeResources(R.color.primary, R.color.red, R.color.green)
        binding.executePendingBindings()
        sharedViewModel.newConnectionNameEntered.observe(viewLifecycleOwner, Observer<Bundle> { result ->
            viewModel.onEditNameResult(result)
        })
        sharedViewModel.connectionDeleted.observe(viewLifecycleOwner, Observer<GUID> { result ->
            viewModel.onDeleteItemResult(result)
        })
    }

    private fun showPopupMenu(menuData: MenuData): PopupWindow? {
        val parentView = connectionsListView ?: return null
        val anchorView = connectionsListView?.layoutManager?.findViewByPosition(menuData.menuId) ?: return null
        return PopupMenuBuilder(parentView, viewModel).setContent(menuData).show(anchorView)
    }
}
