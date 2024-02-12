/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.menu

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.KEY_OPTION_ID
import com.saltedge.authenticator.core.api.KEY_ID
import com.saltedge.authenticator.features.main.SharedViewModel
import com.saltedge.authenticator.interfaces.ListItemClickListener
import com.saltedge.authenticator.widget.fragment.BaseRoundedBottomDialogFragment

class BottomMenuDialog : BaseRoundedBottomDialogFragment(), ListItemClickListener {

    private val presenter = BottomMenuPresenter()
    private val adapter = MenuItemsAdapter(this)
    private var contentRecyclerView: RecyclerView? = null
    private val sharedViewModel: SharedViewModel by activityViewModels()

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
            sharedViewModel.onMenuItemSelected(Bundle().apply {
                putString(KEY_ID, presenter.menuId)
                putInt(KEY_OPTION_ID, item.id)
            })
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

        fun dataBundle(
            menuId: String = "",
            menuItems: List<MenuItemData>
        ): Bundle {
            return Bundle().apply {
                this.putString(KEY_ID, menuId)
                this.putSerializable(KEY_ITEMS, ArrayList(menuItems))
            }
        }
    }
}
