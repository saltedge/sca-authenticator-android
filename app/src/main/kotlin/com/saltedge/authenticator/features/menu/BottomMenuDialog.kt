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
package com.saltedge.authenticator.features.menu

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.KEY_ID
import com.saltedge.authenticator.app.KEY_OPTION_ID
import com.saltedge.authenticator.interfaces.ListItemClickListener
import com.saltedge.authenticator.widget.fragment.BaseRoundedBottomDialogFragment

class BottomMenuDialog : BaseRoundedBottomDialogFragment(), ListItemClickListener {

    private val presenter = BottomMenuPresenter()
    private val adapter = MenuItemsAdapter(this)
    private var contentRecyclerView: RecyclerView? = null

    override fun getDialogViewLayout(): Int = R.layout.dialog_menu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.setInitialData(
            arguments?.getString(KEY_ID),
            arguments?.getSerializable(KEY_ITEMS) as? List<MenuItemData>
        )
    }

    override fun onStart() {
        super.onStart()
        setupDialogViews()
    }

    override fun onListItemClick(itemIndex: Int, itemCode: String, itemViewId: Int) {
        val item = adapter.getItem(itemIndex) as MenuItemData
        dismiss()
        val menuItemSelectListener = activity as? MenuItemSelectListener
        if (menuItemSelectListener == null) {
            targetFragment?.onActivityResult(
                targetRequestCode,
                RESULT_OK,
                Intent()
                    .putExtra(KEY_ID, presenter.menuId)
                    .putExtra(KEY_OPTION_ID, item.id)
            )
        } else {
            menuItemSelectListener.onMenuItemSelected(presenter.menuId ?: "", item.id)
        }
    }

    private fun setupDialogViews() {
        contentRecyclerView = dialog?.findViewById(R.id.recyclerView)
        activity?.let { contentRecyclerView?.layoutManager = LinearLayoutManager(it) }
        contentRecyclerView?.adapter = adapter
        adapter.data = presenter.listItems
    }

    companion object {
        const val KEY_ITEMS = "items"

        fun newInstance(
            menuId: String = "",
            menuItems: List<MenuItemData>
        ): BottomMenuDialog {
            return BottomMenuDialog().apply {
                arguments = Bundle().apply {
                    putString(KEY_ID, menuId)
                    putSerializable(KEY_ITEMS, ArrayList(menuItems))
                }
            }
        }

        fun newInstance(bundle: Bundle): BottomMenuDialog =
            BottomMenuDialog().apply { arguments = bundle }
    }
}
