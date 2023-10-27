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

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.ViewModelsFactory
import com.saltedge.authenticator.app.authenticatorApp
import com.saltedge.authenticator.cloud.clearNotifications
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.databinding.FragmentAuthorizationsListBinding
import com.saltedge.authenticator.features.authorizations.common.AuthorizationItemViewModel
import com.saltedge.authenticator.features.authorizations.list.pagers.AuthorizationsContentPagerAdapter
import com.saltedge.authenticator.features.authorizations.list.pagers.AuthorizationsHeaderPagerAdapter
import com.saltedge.authenticator.features.authorizations.list.pagers.PagersScrollSynchronizer
import com.saltedge.authenticator.features.main.SharedViewModel
import com.saltedge.authenticator.interfaces.AppbarMenuItemClickListener
import com.saltedge.authenticator.interfaces.DialogHandlerListener
import com.saltedge.authenticator.interfaces.MenuItem
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.location.DeviceLocationManager
import com.saltedge.authenticator.tools.*
import com.saltedge.authenticator.widget.fragment.BaseFragment
import javax.inject.Inject

class AuthorizationsListFragment : BaseFragment(), AppbarMenuItemClickListener, DialogHandlerListener {

    @Inject lateinit var viewModelFactory: ViewModelsFactory
    private lateinit var viewModel: AuthorizationsListViewModel
    private var binding: FragmentAuthorizationsListBinding? = null
    private val pagersScrollSynchronizer = PagersScrollSynchronizer()
    private var headerAdapter: AuthorizationsHeaderPagerAdapter? = null
    private var contentAdapter: AuthorizationsContentPagerAdapter? = null
    private var dialogFragment: DialogFragment? = null
    private var alertDialog: AlertDialog? = null
    private val sharedViewModel: SharedViewModel by activityViewModels()
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
            titleResId = R.string.app_name_short,
            showMenu = arrayOf(MenuItem.SCAN_QR, MenuItem.MORE_MENU)
        )
        binding = FragmentAuthorizationsListBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    override fun onStart() {
        super.onStart()
        setupSharedObserver()
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

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    override fun onAppbarMenuItemClick(menuItem: MenuItem) {
        viewModel.onAppbarMenuItemClick(menuItem)
    }

    override fun closeActiveDialogs() {
        if (dialogFragment?.isVisible == true) dialogFragment?.dismiss()
        if (alertDialog?.isShowing == true) alertDialog?.dismiss()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory).get(AuthorizationsListViewModel::class.java)
        viewModel.bindLifecycleObserver(lifecycle = lifecycle)

        viewModel.listItems.observe(this, Observer<List<AuthorizationItemViewModel>> {
            headerAdapter?.data = it
            contentAdapter?.data = it
        })

        viewModel.listVisibility.observe(this, Observer<Int> { visibility ->
            binding?.listGroup?.visibility = visibility
        })

        viewModel.emptyViewVisibility.observe(this, Observer<Int> { visibility ->
            binding?.emptyView?.visibility = visibility
        })

        viewModel.listItemUpdateEvent.observe(this, Observer<ViewModelEvent<Int>> {
            it.getContentIfNotHandled()?.let { itemIndex ->
                viewModel.listItemsValues.getOrNull(itemIndex)?.let { item ->
                    contentAdapter?.updateItem(item, itemIndex)
                    headerAdapter?.updateItem(item, itemIndex)
                }
            }
        })
        viewModel.errorEvent.observe(this, Observer<ViewModelEvent<ApiErrorData>> { event ->
            event.getContentIfNotHandled()?.let { error ->
                view?.let {
                    val errorMessage = error.getErrorMessage(it.context)
                    Snackbar.make(it, errorMessage, Snackbar.LENGTH_LONG).show()
                }
            }
        })
        viewModel.onQrScanClickEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { activity?.showQrScannerActivity() }
        })
        viewModel.onMoreMenuClickEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { menuItems ->
                navigateToDialog(R.id.bottom_menu_dialog, menuItems)
            }
        })
        viewModel.onShowConnectionsListEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { navigateTo(actionRes = R.id.connections_list) }
        })
        viewModel.onShowSettingsListEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { navigateTo(actionRes = R.id.settings_list) }
        })
        viewModel.onRequestPermissionEvent.observe(this, Observer { event ->
            event?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alertDialog = if (activity?.shouldShowRequestPermissionRationale(DeviceLocationManager.permissions[0]) == false
                        || activity?.shouldShowRequestPermissionRationale(DeviceLocationManager.permissions[1]) == false) { //TODO: try to extract business logic in vm
                        activity?.showInfoDialog(
                            titleResId = R.string.grant_access_location_title,
                            messageResId = R.string.grant_access_location_description,
                            positiveButtonResId = R.string.actions_go_to_settings,
                            listener = { _, dialogActionId ->
                                viewModel.onPermissionRationaleDialogActionClick(dialogActionId, R.string.actions_go_to_settings)
                            })
                    } else {
                        activity?.showInfoDialog(
                            titleResId = R.string.grant_access_location_title,
                            messageResId = R.string.grant_access_location_description,
                            positiveButtonResId = R.string.actions_proceed,
                            listener = { _, dialogActionId ->
                                viewModel.onPermissionRationaleDialogActionClick(dialogActionId, R.string.actions_proceed)
                            })
                    }
                }
            }
        })
        viewModel.onGoToSystemSettingsEvent.observe(this, { event ->
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
        viewModel.emptyViewImage.observe(this, Observer<ResId> {
            binding?.emptyView?.setImageResource(it)
        })
        viewModel.emptyViewActionText.observe(this, Observer<ResId?> {
            binding?.emptyView?.setActionText(it)
        })
        viewModel.emptyViewTitleText.observe(this, Observer<ResId> {
            binding?.emptyView?.setTitle(it)
        })
        viewModel.emptyViewDescriptionText.observe(this, Observer<ResId> {
            binding?.emptyView?.setDescription(it)
        })
        viewModel.onRequestGPSProviderEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let {
                activity?.showInfoDialog(
                    titleResId = R.string.enable_gps_title,
                    messageResId = R.string.enable_gps_description,
                    positiveButtonResId = R.string.actions_enable,
                    listener = { _, dialogActionId ->
                        viewModel.onPermissionRationaleDialogActionClick(
                            dialogActionId,
                            R.string.actions_enable,
                            activity
                        )
                    })
            }
        })
    }

    private fun setupViews() {
        activity?.let {
            contentAdapter = AuthorizationsContentPagerAdapter(it).apply {
                binding?.contentViewPager?.adapter = this
            }
            headerAdapter = AuthorizationsHeaderPagerAdapter(it, viewModel).apply {
                binding?.headerViewPager?.adapter = this
            }
        }
        pagersScrollSynchronizer.initViews(binding?.headerViewPager, binding?.contentViewPager)
        binding?.emptyView?.setActionOnClickListener { viewModel.onEmptyViewActionClick() }
    }

    private fun setupSharedObserver() {
        sharedViewModel.onBottomMenuItemSelected.observe(viewLifecycleOwner, Observer<ViewModelEvent<Bundle>> { event ->
            event.getContentIfNotHandled()?.let { viewModel.onItemMenuClicked(it) }
        })
    }
}
