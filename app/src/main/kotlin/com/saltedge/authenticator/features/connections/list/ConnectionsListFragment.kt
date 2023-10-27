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

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.ViewModelsFactory
import com.saltedge.authenticator.app.authenticatorApp
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.GUID
import com.saltedge.authenticator.databinding.FragmentConnectionsListBinding
import com.saltedge.authenticator.features.connections.common.ConnectionItem
import com.saltedge.authenticator.features.connections.list.menu.MenuData
import com.saltedge.authenticator.features.connections.list.menu.PopupMenuBuilder
import com.saltedge.authenticator.features.main.SharedViewModel
import com.saltedge.authenticator.interfaces.DialogHandlerListener
import com.saltedge.authenticator.interfaces.ListItemClickListener
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.location.DeviceLocationManager
import com.saltedge.authenticator.tools.*
import com.saltedge.authenticator.widget.fragment.BaseFragment
import com.saltedge.authenticator.widget.list.SpaceItemDecoration
import javax.inject.Inject

class ConnectionsListFragment : BaseFragment(),
    ListItemClickListener,
    View.OnClickListener,
    DialogHandlerListener
{
    @Inject lateinit var viewModelFactory: ViewModelsFactory
    private lateinit var viewModel: ConnectionsListViewModel
    private var binding: FragmentConnectionsListBinding? = null
    private val adapter = ConnectionsListAdapter(clickListener = this)
    private var headerDecorator: SpaceItemDecoration? = null
    private var popupWindow: PopupWindow? = null
    private var dialogFragment: DialogFragment? = null
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var alertDialog: AlertDialog? = null
    private val requestMultiplePermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.forEach { actionMap ->
            when (actionMap.key) {
                Manifest.permission.ACCESS_FINE_LOCATION -> {
                    if (actionMap.value) {
                        viewModel.updateLocationStateOfConnection()
                    }
                }
                Manifest.permission.ACCESS_COARSE_LOCATION -> {
                    if (actionMap.value) {
                        viewModel.updateLocationStateOfConnection()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.authenticatorApp?.appComponent?.inject(this)
        setupViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activityComponents?.updateAppbar(
            titleResId = R.string.connections_feature_title,
            backActionImageResId = R.drawable.ic_appbar_action_back
        )

        binding = FragmentConnectionsListBinding.inflate(inflater, container, false)
        return binding?.root
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
        binding?.swipeRefreshLayout?.stopRefresh()
        super.onStop()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    override fun closeActiveDialogs() {
        if (dialogFragment?.isVisible == true) dialogFragment?.dismiss()
        if (popupWindow?.isShowing == true) popupWindow?.dismiss()
        if (alertDialog?.isShowing == true) alertDialog?.dismiss()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory).get(ConnectionsListViewModel::class.java)
        lifecycle.addObserver(viewModel)

        viewModel.listItems.observe(this, Observer<List<ConnectionItem>> {
            headerDecorator?.setHeaderForAllItems(it.count())
            headerDecorator?.footerPositions = arrayOf(it.count() - 1)
            adapter.data = it
        })
        viewModel.emptyViewVisibility.observe(this, Observer<Int> { visibility ->
            binding?.emptyView?.visibility = visibility
        })
        viewModel.listVisibility.observe(this, Observer<Int> { visibility ->
            binding?.connectionsListView?.visibility = visibility
        })
        viewModel.onQrScanClickEvent.observe(this, Observer<ViewModelEvent<Unit>> {
            it.getContentIfNotHandled()?.let { activity?.showQrScannerActivity() }
        })
        viewModel.onRenameClickEvent.observe(this, Observer<ViewModelEvent<Bundle>> { event ->
            event.getContentIfNotHandled()?.let { bundle ->
                navigateToDialog(R.id.edit_connection_name_dialog, bundle)
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
        viewModel.onReconnectClickEvent.observe(this, Observer<ViewModelEvent<ReconnectData>> { event ->
            event.getContentIfNotHandled()?.let { connectionData ->
                findNavController().navigate(
                    ConnectionsListFragmentDirections.connectProvider(
                        guid = connectionData.guid,
                        apiVersion = connectionData.apiVersion
                    )
                )
            }
        })
        viewModel.onViewConsentsClickEvent.observe(this, Observer<ViewModelEvent<Bundle>> { event ->
            event.getContentIfNotHandled()?.let { bundle ->
                navigateTo(actionRes = R.id.consents_list, bundle = bundle)
            }
        })
        viewModel.onSupportClickEvent.observe(this, Observer<ViewModelEvent<String?>> { event ->
            event.getContentIfNotHandled()?.let { supportEmail ->
                activity?.startMailApp(supportEmail)
            }
        })
        viewModel.onErrorEvent.observe(this, Observer<ViewModelEvent<ApiErrorData>> { event ->
            event.getContentIfNotHandled()?.let { error ->
                view?.let { anchor ->
                    val message = error.getErrorMessage(anchor.context)
                    Snackbar.make(anchor, message, Snackbar.LENGTH_LONG).show()
                }
            }
        })
        viewModel.onAccessToLocationClickEvent.observe(this, { event ->
            event.getContentIfNotHandled()?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alertDialog = if (activity?.shouldShowRequestPermissionRationale(DeviceLocationManager.permissions[0]) == false
                        || activity?.shouldShowRequestPermissionRationale(DeviceLocationManager.permissions[1]) == false) {
                            activity?.showInfoDialog(
                                titleResId = R.string.grant_access_location_title,
                                messageResId = R.string.grant_access_location_description,
                                positiveButtonResId = R.string.actions_go_to_settings,
                                listener = { _, dialogActionId ->
                                    viewModel.onDialogActionClick(dialogActionId, R.string.actions_go_to_settings)
                                })
                    } else {
                        activity?.showInfoDialog(
                            titleResId = R.string.grant_access_location_title,
                            messageResId = R.string.grant_access_location_description,
                            positiveButtonResId = R.string.actions_proceed,
                            listener = { _, dialogActionId ->
                                viewModel.onDialogActionClick(dialogActionId, R.string.actions_proceed)
                            })
                    }
                }
            }
        })
        viewModel.onGoToSettingsEvent.observe(this, { event ->
            event.getContentIfNotHandled()?.let {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", requireActivity().packageName, null)
                startActivity(intent)
            }
        })
        viewModel.onAskPermissionsEvent.observe(this, Observer<ViewModelEvent<Unit>> {
            it.getContentIfNotHandled()?.let {
                requestMultiplePermissions.launch(DeviceLocationManager.permissions)
            }
        })
        viewModel.updateListItemEvent.observe(this, Observer<ConnectionItem> { itemIndex ->
            adapter.updateListItem(itemIndex)
        })
        viewModel.onShowNoInternetConnectionDialogEvent.observe(this, Observer<ViewModelEvent<GUID>> { event ->
            event.getContentIfNotHandled()?.let { guid ->
            activity?.showInfoDialog(
                titleResId = R.string.warning_no_internet_connection,
                messageResId = R.string.warning_no_internet_connection_description,
                positiveButtonResId = R.string.actions_retry,
                listener = { _, dialogActionId ->
                    viewModel.onDialogActionClick(dialogActionId, R.string.actions_retry, guid)
                })
            }
        })
    }

    private fun setupViews() {
        binding?.emptyView?.setActionOnClickListener(this)
        activity?.let {
            binding?.connectionsListView?.layoutManager = LinearLayoutManager(it)
            binding?.connectionsListView?.adapter = adapter
            headerDecorator = SpaceItemDecoration(context = it).apply {
                binding?.connectionsListView?.addItemDecoration(this)
            }
        }
        binding?.swipeRefreshLayout?.setOnRefreshListener {
            viewModel.refreshConsents()
            binding?.swipeRefreshLayout?.stopRefresh()
        }
        binding?.swipeRefreshLayout?.setColorSchemeResources(R.color.primary, R.color.red, R.color.green)
        sharedViewModel.newConnectionNameEntered.observe(viewLifecycleOwner, Observer<Bundle> { result ->
            viewModel.onItemNameChanged(result)
        })
        sharedViewModel.connectionDeleted.observe(viewLifecycleOwner, Observer<GUID> { result ->
            viewModel.deleteItem(result)
        })
    }

    private fun showPopupMenu(menuData: MenuData): PopupWindow? {
        val parentView = binding?.connectionsListView ?: return null
        val anchorView = binding?.connectionsListView?.layoutManager?.findViewByPosition(menuData.menuId) ?: return null
        return PopupMenuBuilder(parentView, viewModel).setContent(menuData).show(anchorView)
    }
}
