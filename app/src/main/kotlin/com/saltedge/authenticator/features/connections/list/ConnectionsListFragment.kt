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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.DELETE_REQUEST_CODE
import com.saltedge.authenticator.app.RENAME_REQUEST_CODE
import com.saltedge.authenticator.app.ViewModelsFactory
import com.saltedge.authenticator.databinding.ConnectionsListBinding
import com.saltedge.authenticator.features.connections.common.ConnectionViewModel
import com.saltedge.authenticator.features.connections.create.ConnectProviderFragment
import com.saltedge.authenticator.features.connections.delete.DeleteConnectionDialog
import com.saltedge.authenticator.features.connections.edit.name.EditConnectionNameDialog
import com.saltedge.authenticator.interfaces.ListItemClickListener
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.tools.*
import com.saltedge.authenticator.widget.fragment.BaseFragment
import com.saltedge.authenticator.widget.list.SpaceItemDecoration
import kotlinx.android.synthetic.main.fragment_authorizations_list.*
import kotlinx.android.synthetic.main.fragment_connections_list.*
import kotlinx.android.synthetic.main.fragment_connections_list.emptyView
import javax.inject.Inject

class ConnectionsListFragment : BaseFragment(), ListItemClickListener, View.OnClickListener {

    @Inject lateinit var viewModelFactory: ViewModelsFactory
    private lateinit var viewModel: ConnectionsListViewModel
    private lateinit var binding: ConnectionsListBinding
    private val adapter = ConnectionsListAdapter(clickListener = this)
    private var headerDecorator: SpaceItemDecoration? = null

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.onActivityResult(requestCode, resultCode, data)
    }

    override fun onClick(v: View?) {
        viewModel.onViewClick(v?.id ?: return)
    }

    override fun onListItemClick(itemIndex: Int, itemCode: String, itemViewId: Int) {
        viewModel.onListItemClick(itemIndex)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(ConnectionsListViewModel::class.java)
        lifecycle.addObserver(viewModel)

        viewModel.listItems.observe(this, Observer<List<ConnectionViewModel>> {
            headerDecorator?.headerPositions =
                it.mapIndexed { index, _ -> index }.toTypedArray()
            adapter.data = it
        })
        viewModel.onQrScanClickEvent.observe(this, Observer<ViewModelEvent<Unit>> {
            it.getContentIfNotHandled()?.let {
                activity?.showQrScannerActivity()
            }
        })
        viewModel.onRenameClickEvent.observe(this, Observer<ViewModelEvent<Bundle>> {
            it.getContentIfNotHandled()?.let {
                val dialog = EditConnectionNameDialog.newInstance(it).also {
                    it.setTargetFragment(this, RENAME_REQUEST_CODE)
                }
                activity?.showDialogFragment(dialog)
            }
        })
        viewModel.onDeleteClickEvent.observe(this, Observer<ViewModelEvent<Bundle>> {
            it.getContentIfNotHandled()?.let {
                val dialog = DeleteConnectionDialog.newInstance(it).also {
                    it.setTargetFragment(this, DELETE_REQUEST_CODE)
                }
                activity?.showDialogFragment(dialog)
            }
        })
        viewModel.onListItemClickEvent.observe(this, Observer<ViewModelEvent<Int>> { event ->
            event.getContentIfNotHandled()?.let { itemIndex ->
                viewModel.listItemsValues.getOrNull(itemIndex)?.let { item ->
                    showPopupMenu(view = connectionsListView?.getChildAt(itemIndex), item = item)
                }
            }
        })
        viewModel.onReconnectClickEvent.observe(this, Observer<ViewModelEvent<String>> {
            it.getContentIfNotHandled()?.let {
                activity?.addFragment(ConnectProviderFragment.newInstance(connectionGuid = it))
            }
        })
        viewModel.onSupportClickEvent.observe(this, Observer<ViewModelEvent<String?>> {
            it.getContentIfNotHandled()?.let { supportEmail ->
                activity?.startMailApp(supportEmail)
            }
        })
        viewModel.updateListItemEvent.observe(this, Observer<ConnectionViewModel> { itemIndex ->
                adapter.updateListItem(itemIndex)
        })
    }

    private fun setupViews() {
        connectionsListView?.layoutManager = LinearLayoutManager(activity ?: return)
        connectionsListView?.adapter = adapter
        emptyView?.setActionOnClickListener(this)
        headerDecorator = SpaceItemDecoration(
            context = activity ?: return
        ).apply {
            connectionsListView?.addItemDecoration(this)
        }
        binding.executePendingBindings()
    }

    private fun showPopupMenu(view: View?, item: ConnectionViewModel) {
        val layoutInflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = layoutInflater.inflate(R.layout.popup_menu_layout, null)
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true
        popupWindow.showAsDropDown(view)

        val renameView = popupView.findViewById<LinearLayout>(R.id.renameView)
        val reconnectView = popupView.findViewById<LinearLayout>(R.id.reconnectView)
        val contactSupportView = popupView.findViewById<LinearLayout>(R.id.contactSupportView)
        val deleteView = popupView.findViewById<LinearLayout>(R.id.deleteView)
        val deleteImageView = popupView.findViewById<ImageView>(R.id.deleteImageView)
        val deleteTextView = popupView.findViewById<TextView>(R.id.deleteTextView)

        reconnectView.setVisible(item.reconnectOptionIsVisible)
        deleteTextView.setText(item.deleteMenuItemText)
        deleteImageView.setImageResource(item.deleteMenuItemImage)

        reconnectView.setOnClickListener {
            popupWindow.dismiss()
            viewModel.onReconnectOptionSelected()
        }
        renameView.setOnClickListener {
            popupWindow.dismiss()
            viewModel.onRenameOptionSelected()
        }
        contactSupportView.setOnClickListener {
            popupWindow.dismiss()
            viewModel.onContactSupportOptionSelected()
        }
        deleteView.setOnClickListener {
            popupWindow.dismiss()
            viewModel.onDeleteOptionsSelected()
        }
    }
}
