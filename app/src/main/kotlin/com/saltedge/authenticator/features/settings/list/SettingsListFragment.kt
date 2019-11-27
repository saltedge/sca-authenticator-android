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
package com.saltedge.authenticator.features.settings.list

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.saltedge.authenticator.R
import com.saltedge.authenticator.widget.list.SpaceItemDecoration
import com.saltedge.authenticator.features.connections.delete.DeleteConnectionDialog
import com.saltedge.authenticator.features.settings.about.AboutListFragment
import com.saltedge.authenticator.features.settings.common.SettingsAdapter
import com.saltedge.authenticator.features.settings.language.LanguageSelectDialog
import com.saltedge.authenticator.features.settings.list.di.SettingsListModule
import com.saltedge.authenticator.features.settings.passcode.PasscodeEditFragment
import com.saltedge.authenticator.interfaces.CheckableListItemClickListener
import com.saltedge.authenticator.tool.*
import com.saltedge.authenticator.widget.fragment.BaseFragment
import kotlinx.android.synthetic.main.fragment_base_list.*
import javax.inject.Inject

class SettingsListFragment : BaseFragment(), SettingsListContract.View,
    CheckableListItemClickListener, View.OnClickListener {

    @Inject lateinit var presenterContract: SettingsListContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_base_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activityComponents?.updateAppbarTitleWithFabAction(getString(R.string.settings_feature_title))
        setupViews()
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
        presenterContract.onListItemClick(itemViewId)
    }

    override fun onListItemCheckedStateChanged(itemId: Int, checked: Boolean) {
        presenterContract.onListItemCheckedStateChanged(itemId, checked)
    }

    override fun showLanguageSelector() {
        activity?.showDialogFragment(LanguageSelectDialog())
    }

    override fun showPasscodeEditor() {
        activity?.addFragment(PasscodeEditFragment())
    }

    override fun showSystemSettings() {
        activity?.startSystemSettings()
    }

    override fun showAboutList() {
        activity?.addFragment(AboutListFragment())
    }

    override fun openMailApp() {
        activity?.startMailApp()
    }

    override fun showInfo(message: Int) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.actions_ok), this)
                .show()
        }
    }

    override fun onClick(v: View?) {
        activity?.restartApp()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        presenterContract.onActivityResult(requestCode, resultCode, data)
    }

    override fun showDeleteConnectionView(requestCode: Int) {
        val dialog = DeleteConnectionDialog.newInstance(null).also {
            it.setTargetFragment(this, requestCode)
        }
        activity?.showDialogFragment(dialog)
    }

    private fun setupViews() {
        try {
            val context = activity ?: return
            val layoutManager = LinearLayoutManager(context)
            recyclerView?.layoutManager = layoutManager
            recyclerView?.addItemDecoration(
                SpaceItemDecoration(
                    context = context,
                    headerPositions = presenterContract.getPositionsOfHeaders()
                )
            )
            recyclerView?.adapter = SettingsAdapter(this).apply {
                data = presenterContract.getListItems()
            }
        } catch (e: Exception) {
            e.log()
        }
    }

    private fun injectDependencies() {
        authenticatorApp?.appComponent?.addSettingsListModule(SettingsListModule())?.inject(this)
    }
}
