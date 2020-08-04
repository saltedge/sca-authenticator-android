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
package com.saltedge.authenticator.features.settings.list

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.ViewModelsFactory
import com.saltedge.authenticator.app.applyNightMode
import com.saltedge.authenticator.app.navigateTo
import com.saltedge.authenticator.features.main.showWarningSnack
import com.saltedge.authenticator.features.settings.common.SettingsAdapter
import com.saltedge.authenticator.features.settings.common.SettingsItemViewModel
import com.saltedge.authenticator.interfaces.AppbarMenuItemClickListener
import com.saltedge.authenticator.interfaces.DialogHandlerListener
import com.saltedge.authenticator.interfaces.MenuItem
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.tools.*
import com.saltedge.authenticator.widget.fragment.BaseFragment
import com.saltedge.authenticator.widget.list.SpaceItemDecoration
import kotlinx.android.synthetic.main.fragment_base_list.*
import javax.inject.Inject

class SettingsListFragment : BaseFragment(), DialogHandlerListener, AppbarMenuItemClickListener {

    @Inject lateinit var viewModelFactory: ViewModelsFactory
    private lateinit var viewModel: SettingsListViewModel
    private var adapter: SettingsAdapter? = null
    private var alertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authenticatorApp?.appComponent?.inject(this)
        setupViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_base_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activityComponents?.updateAppbar(
            titleResId = R.string.settings_feature_title,
            backActionImageResId = R.drawable.ic_appbar_action_back,
            showMenu = arrayOf(MenuItem.CUSTOM_NIGHT_MODE)
        )
        setupViews()
    }

    override fun closeActiveDialogs() {
        if (alertDialog?.isShowing == true) alertDialog?.dismiss()
    }

    override fun onAppbarMenuItemClick(menuItem: MenuItem) {
        viewModel.onAppbarMenuItemClick(menuItem)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory).get(SettingsListViewModel::class.java)

        viewModel.languageClickEvent.observe(this, Observer<ViewModelEvent<Unit>> { event ->
            event.getContentIfNotHandled()?.let { findNavController().navigate(R.id.language) }
        })
        viewModel.passcodeClickEvent.observe(this, Observer<ViewModelEvent<Unit>> { event ->
            event.getContentIfNotHandled()?.let { navigateTo(actionRes = R.id.passcode_edit) }
        })
        viewModel.aboutClickEvent.observe(this, Observer<ViewModelEvent<Unit>> { event ->
            event.getContentIfNotHandled()?.let { navigateTo(actionRes = R.id.about_list) }
        })
        viewModel.supportClickEvent.observe(this, Observer<ViewModelEvent<Unit>> { event ->
            event.getContentIfNotHandled()?.let { activity?.startMailApp() }
        })
        viewModel.clearClickEvent.observe(this, Observer<ViewModelEvent<Unit>> { event ->
            event.getContentIfNotHandled()?.let {
                alertDialog = activity?.showResetDataAndSettingsDialog(DialogInterface.OnClickListener { _, dialogActionId ->
                    viewModel.onDialogActionIdClick(dialogActionId)
                })
            }
        })
        viewModel.clearSuccessEvent.observe(this, Observer<ViewModelEvent<Unit>> {
            it.getContentIfNotHandled()?.let {
                activity?.showWarningSnack(
                    textResId = R.string.settings_clear_success,
                    snackBarDuration = Snackbar.LENGTH_SHORT
                )
            }
        })
        viewModel.screenshotClickEvent.observe(this, Observer<ViewModelEvent<Unit>> {
            it.getContentIfNotHandled()?.let {
                view?.let {
                    activity?.showWarningSnack(
                        textResId = R.string.settings_restart_app,
                        snackBarDuration = Snackbar.LENGTH_LONG
                    )?.setAction(getString(android.R.string.ok)) { viewModel.restartConfirmed() }
                }
            }
        })
        viewModel.restartClickEvent.observe(this, Observer<ViewModelEvent<Unit>> {
            it.getContentIfNotHandled()?.let { activity?.restartApp() }
        })
        viewModel.setNightModelEvent.observe(this, Observer<ViewModelEvent<Int>> {
            it.getContentIfNotHandled()?.let { nightMode -> activity?.applyNightMode(nightMode) }
        })
        viewModel.listItems.observe(this, Observer<List<SettingsItemViewModel>> {
            adapter?.data = it
        })
    }

    private fun setupViews() {
        activity?.let {
            recyclerView?.layoutManager = LinearLayoutManager(it)
            recyclerView?.addItemDecoration(
                SpaceItemDecoration(
                    context = it,
                    headerPositions = viewModel.spacesPositions)
            )
        }
        adapter = SettingsAdapter(listener = viewModel).apply {
            viewModel.listItemsValues?.let { data = it }
        }
        recyclerView?.adapter = adapter
    }
}
