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
package com.saltedge.authenticator.features.authorizations.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.authorizations.common.AuthorizationViewModel
import com.saltedge.authenticator.features.authorizations.confirmPasscode.ConfirmPasscodeDialog
import com.saltedge.authenticator.features.authorizations.list.adapters.AuthorizationsContentPagerAdapter
import com.saltedge.authenticator.features.authorizations.list.adapters.AuthorizationsHeaderPagerAdapter
import com.saltedge.authenticator.features.authorizations.list.di.AuthorizationsListModule
import com.saltedge.authenticator.sdk.model.ApiErrorData
import com.saltedge.authenticator.sdk.model.getErrorMessage
import com.saltedge.authenticator.tool.*
import com.saltedge.authenticator.widget.biometric.BiometricPromptAbs
import com.saltedge.authenticator.widget.biometric.showAuthorizationConfirm
import com.saltedge.authenticator.widget.fragment.BaseFragment
import kotlinx.android.synthetic.main.fragment_authorizations_list.*
import javax.inject.Inject

class AuthorizationsListFragment : BaseFragment(), AuthorizationsListContract.View {

    @Inject
    lateinit var presenter: AuthorizationsListPresenter
    @Inject
    lateinit var biometricPrompt: BiometricPromptAbs
    private val pagersScrollSynchronizer = PagersScrollSynchronizer()
    private var headerAdapter: AuthorizationsHeaderPagerAdapter? = null
    private var contentAdapter: AuthorizationsContentPagerAdapter? = null

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
        activityComponents?.updateAppbarTitleWithFabAction(getString(R.string.authorizations_feature_title))
        return inflater.inflate(R.layout.fragment_authorizations_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            setupViews()
        } catch (e: Exception) {
            e.log()
        }
    }

    override fun onStart() {
        super.onStart()
        presenter.viewContract = this
        biometricPrompt.resultCallback = presenter
        contentAdapter?.listItemClickListener = presenter
    }

    override fun onResume() {
        super.onResume()
        presenter.onFragmentResume()
        headerAdapter?.startTimer()
    }

    override fun onPause() {
        headerAdapter?.stopTimer()
        presenter.onFragmentPause()
        super.onPause()
    }

    override fun onStop() {
        biometricPrompt.resultCallback = null
        presenter.viewContract = null
        contentAdapter?.listItemClickListener = null
        super.onStop()
    }

    override fun onDestroy() {
        presenter.onFragmentDestroy()
        super.onDestroy()
    }

    override fun showError(error: ApiErrorData) {
        view?.let {
            Snackbar.make(it, error.getErrorMessage(it.context), Snackbar.LENGTH_LONG).show()
        }
    }

    override fun updateViewsContent() {
        clearAllNotifications()
        activity?.runOnUiThread {
            listGroup?.setVisible(presenter.showContentViews)
            emptyView?.setVisible(presenter.showEmptyView)

            headerAdapter?.data = presenter.viewModels
            contentAdapter?.data = presenter.viewModels
        }
    }

    override fun updateItem(viewModel: AuthorizationViewModel, itemId: Int) {
        contentAdapter?.updateItem(viewModel, itemId)
        headerAdapter?.updateItem(viewModel, itemId)
    }

    override fun askUserBiometricConfirmation() {
        activity?.let { biometricPrompt.showAuthorizationConfirm(it) }
    }

    override fun askUserPasscodeConfirmation() {
        activity?.showDialogFragment(ConfirmPasscodeDialog.newInstance(resultCallback = presenter))
    }

    private fun setupViews() {
        activity?.let {
            contentAdapter = AuthorizationsContentPagerAdapter(it).apply {
                contentViewPager?.adapter = this
            }
            headerAdapter = AuthorizationsHeaderPagerAdapter(it, presenter).apply {
                headerViewPager?.adapter = this
            }
        }
        pagersScrollSynchronizer.initViews(headerViewPager, contentViewPager)
    }

    // Clear all system notification
    private fun clearAllNotifications() {
        activity?.clearNotifications()
    }

    private fun injectDependencies() {
        authenticatorApp?.appComponent
            ?.addAuthorizationsListModule(AuthorizationsListModule())?.inject(this)
    }
}
