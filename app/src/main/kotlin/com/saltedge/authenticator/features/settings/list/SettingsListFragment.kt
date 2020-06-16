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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.ViewModelsFactory
import com.saltedge.authenticator.features.main.buildWarning
import com.saltedge.authenticator.features.settings.about.AboutListFragment
import com.saltedge.authenticator.features.settings.common.SettingsAdapter
import com.saltedge.authenticator.features.settings.language.LanguageSelectDialog
import com.saltedge.authenticator.features.settings.passcode.PasscodeEditFragment
import com.saltedge.authenticator.interfaces.MenuItem
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.tools.*
import com.saltedge.authenticator.widget.fragment.BaseFragment
import kotlinx.android.synthetic.main.fragment_base_list.*
import javax.inject.Inject

class SettingsListFragment : BaseFragment() {

    @Inject lateinit var viewModelFactory: ViewModelsFactory
    private lateinit var viewModel: SettingsListViewModel

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
            showMenu = arrayOf(MenuItem.THEME)
        )
        setupViews()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory).get(SettingsListViewModel::class.java)

        viewModel.languageClickEvent.observe(this, Observer<ViewModelEvent<Unit>> {
            it.getContentIfNotHandled()?.let { activity?.showDialogFragment(LanguageSelectDialog()) }
        })
        viewModel.passcodeClickEvent.observe(this, Observer<ViewModelEvent<Unit>> {
            it.getContentIfNotHandled()?.let { activity?.addFragment(PasscodeEditFragment()) }
        })
        viewModel.aboutClickEvent.observe(this, Observer<ViewModelEvent<Unit>> {
            it.getContentIfNotHandled()?.let { activity?.addFragment(AboutListFragment()) }
        })
        viewModel.supportClickEvent.observe(this, Observer<ViewModelEvent<Unit>> {
            it.getContentIfNotHandled()?.let { activity?.startMailApp() }
        })
        viewModel.clearClickEvent.observe(this, Observer<ViewModelEvent<Unit>> {
            it.getContentIfNotHandled()?.let {
                activity?.showResetDataAndSettingsDialog(DialogInterface.OnClickListener { _, _ ->
                    viewModel.onUserConfirmedClearAppData()
                })
            }
        })
        viewModel.clearSuccessEvent.observe(this, Observer<ViewModelEvent<Unit>> {
            it.getContentIfNotHandled()?.let {
                activity?.buildWarning(
                    textResId = R.string.settings_clear_success,
                    snackBarDuration = Snackbar.LENGTH_SHORT
                )?.show()
            }
        })
        viewModel.screenshotClickEvent.observe(this, Observer<ViewModelEvent<Unit>> {
            it.getContentIfNotHandled()?.let {
                view?.let {
                    activity?.buildWarning(
                        textResId = R.string.settings_restart_app,
                        snackBarDuration = Snackbar.LENGTH_LONG
                    )?.setAction(getString(android.R.string.ok)) { viewModel.restartConfirmed() }
                    ?.show()
                }
            }
        })
        viewModel.restartClickEvent.observe(this, Observer<ViewModelEvent<Unit>> {
            it.getContentIfNotHandled()?.let { activity?.restartApp() }
        })
    }

    private fun setupViews() {
        recyclerView?.layoutManager = LinearLayoutManager(activity)
        recyclerView?.adapter = SettingsAdapter(listener = viewModel)
            .apply { data = viewModel.listItems }
    }
}
