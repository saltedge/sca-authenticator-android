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

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.*
import androidx.viewpager.widget.ViewPager
import com.google.android.material.snackbar.Snackbar
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.SHOW_REQUEST_CODE
import com.saltedge.authenticator.app.TIME_VIEW_UPDATE_TIMEOUT
import com.saltedge.authenticator.features.authorizations.common.AuthorizationViewModel
import com.saltedge.authenticator.features.authorizations.confirmPasscode.ConfirmPasscodeDialog
import com.saltedge.authenticator.features.authorizations.details.AuthorizationDetailsFragment
import com.saltedge.authenticator.features.authorizations.list.di.AuthorizationsListModule
import com.saltedge.authenticator.sdk.model.ApiErrorData
import com.saltedge.authenticator.sdk.model.getErrorMessage
import com.saltedge.authenticator.tool.*
import com.saltedge.authenticator.widget.biometric.BiometricPromptAbs
import com.saltedge.authenticator.widget.biometric.showAuthorizationConfirm
import com.saltedge.authenticator.widget.fragment.BaseFragment
import kotlinx.android.synthetic.main.fragment_authorizations_list.*
import java.util.*
import javax.inject.Inject

private const val RECYCLER_LAYOUT_STATE = "recycler_layout_state"

class AuthorizationsListFragment : BaseFragment(), AuthorizationsListContract.View {

    @Inject
    lateinit var presenterContract: AuthorizationsListPresenter
    @Inject
    lateinit var biometricPrompt: BiometricPromptAbs
    @Inject
    lateinit var timeViewUpdateTimer: Timer
    private var contentAdapter: AuthorizationsPagerAdapter? = null
    private var headerAdapter: AuthorizationsCardPagerAdapter? = null
    private var scrollState = ViewPager.SCROLL_STATE_IDLE

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
        contentAdapter = activity?.applicationContext?.let { AuthorizationsPagerAdapter(it) }
        headerAdapter = activity?.applicationContext?.let { AuthorizationsCardPagerAdapter(it) }
        activityComponents?.updateAppbarTitle(getString(R.string.authorizations_feature_title))
        return inflater.inflate(R.layout.fragment_authorizations_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            val listState: Parcelable? = savedInstanceState?.getParcelable(RECYCLER_LAYOUT_STATE)
            reinitAndUpdateViewsContent(listState)
        } catch (e: Exception) {
            e.log()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_authorizations, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.menu_refresh) {
            presenterContract.onRefreshClick()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        presenterContract.viewContract = this
        biometricPrompt.resultCallback = presenterContract
        contentAdapter?.listener = presenterContract
        startListUpdateTimer()
        clearAllNotifications()
    }

    override fun onResume() {
        super.onResume()
        presenterContract.onFragmentStart()
    }

    override fun onPause() {
        presenterContract.onFragmentStop()
        super.onPause()
    }

    override fun onStop() {
        stopListUpdateTimer()
        biometricPrompt.resultCallback = null
        presenterContract.viewContract = null
        contentAdapter?.listener = null
        super.onStop()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        presenterContract.processFragmentResult(requestCode, resultCode, data)
    }

    override fun showAuthorizationDetailsView(authorizationViewModel: AuthorizationViewModel) {
        val fragment = AuthorizationDetailsFragment.newInstance(viewModel = authorizationViewModel)
        fragment.setTargetFragment(this, SHOW_REQUEST_CODE)
        activity?.addFragment(fragment)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
//        outState.putParcelable(
//            RECYCLER_LAYOUT_STATE,
//            recyclerView?.layoutManager?.onSaveInstanceState()
//        )
    }

    override fun showError(error: ApiErrorData) {
        view?.let {
            Snackbar.make(it, error.getErrorMessage(it.context), Snackbar.LENGTH_LONG).show()
        }
    }

    override fun refreshListView() {
        if (isVisible) contentAdapter?.notifyDataSetChanged()
    }

    override fun updateViewsContentInUiThread() {
        activity?.runOnUiThread { if (this.isVisible) updateViewContent() }
    }

    override fun reinitAndUpdateViewsContent(listState: Parcelable?) {
        activity?.let {
            updateViewContent()
        }
    }

    override fun updateViewContent() {
        try {
            contentAdapter?.data = presenterContract.viewModels
            contentViewPager?.adapter = contentAdapter
            headerAdapter?.data = presenterContract.viewModels
            headerViewPager?.adapter = headerAdapter
            val viewIsEmpty = contentAdapter?.isEmpty ?: false
            emptyView?.setVisible(viewIsEmpty)
            contentViewPager?.setVisible(!viewIsEmpty)
            headerViewPager?.setVisible(!viewIsEmpty)
            swipeContentAuthorizations()
            swipeHeaderAuthorizations()
        } catch (e: Exception) {
            e.log()
        }
    }

    override fun updateItem(viewModel: AuthorizationViewModel, itemId: Int) {
//        cardAdapter.updateItem(viewModel, itemId)
    }

    override fun askUserBiometricConfirmation() {
        activity?.let { biometricPrompt.showAuthorizationConfirm(it) }
    }

    override fun askUserPasscodeConfirmation() {
        activity?.showDialogFragment(
            ConfirmPasscodeDialog.newInstance(resultCallback = presenterContract)
        )
    }

    private fun swipeContentAuthorizations() {
        headerViewPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                scrollState = state
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    contentViewPager.setCurrentItem(headerViewPager.currentItem, false)
                }
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                if (scrollState == ViewPager.SCROLL_STATE_IDLE) {
                    return
                }
                val headerWidth = headerViewPager.width.minus(
                    headerViewPager.paddingStart
                        .plus(headerViewPager.paddingEnd)
                ).toFloat()
                val contentWidth = contentViewPager.width.toFloat()
                contentViewPager?.scrollTo(
                    (headerViewPager.scrollX.toFloat() * (contentWidth / headerWidth)).toInt(),
                    contentViewPager.scrollY
                )
            }

            override fun onPageSelected(position: Int) {}
        })
    }

    private fun swipeHeaderAuthorizations() {
        contentViewPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                scrollState = state
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    headerViewPager.setCurrentItem(contentViewPager.currentItem, false)
                }
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                if (scrollState == ViewPager.SCROLL_STATE_IDLE) {
                    return
                }
                val headerWidth = headerViewPager.width.minus(
                    headerViewPager.paddingStart
                        .plus(headerViewPager.paddingEnd)
                ).toFloat()
                val contentWidth = contentViewPager.width.toFloat()
                headerViewPager?.scrollTo(
                    (contentViewPager.scrollX.toFloat() * (headerWidth / contentWidth)).toInt(),
                    headerViewPager.scrollY
                )
            }

            override fun onPageSelected(position: Int) {}
        })
    }

    private fun startListUpdateTimer() {
        timeViewUpdateTimer = Timer()
        timeViewUpdateTimer.schedule(object : TimerTask() {
            override fun run() {
                activity?.runOnUiThread {
//                    if (recyclerView?.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
//                        presenterContract.onTimerTick()
//                    }
                }
            }
        }, 0, TIME_VIEW_UPDATE_TIMEOUT)
    }

    private fun stopListUpdateTimer() {
        timeViewUpdateTimer.cancel()
        timeViewUpdateTimer.purge()
    }

    // Clear all system notification
    private fun clearAllNotifications() {
        activity?.clearNotifications()
    }

    private fun injectDependencies() {
        authenticatorApp?.appComponent?.addAuthorizationsListModule(AuthorizationsListModule())?.inject(
            this
        )
    }
}
