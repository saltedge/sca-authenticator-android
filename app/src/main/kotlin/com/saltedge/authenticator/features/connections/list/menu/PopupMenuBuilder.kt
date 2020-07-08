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
package com.saltedge.authenticator.features.connections.list.menu

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.menu.MenuItemData
import com.saltedge.authenticator.tools.convertDpToPx

/**
 * Build view and show PopupWindow with items from MenuData
 */
class PopupMenuBuilder(
    val parentView: View,
    var itemClickListener: ItemClickListener? = null
) {

    private val context: Context
        get() = parentView.context
    private val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val itemHeight = context.resources.getDimensionPixelSize(R.dimen.popupMenuItemHeight)
    private val menuTopBottomPadding = context.resources.getDimensionPixelSize(R.dimen.popupMenuTopBottomPadding)
    private var menuData: MenuData = MenuData(menuId = 0, items = emptyList())
    private var popupWindow: PopupWindow? = null

    fun setContent(menuData: MenuData): PopupMenuBuilder {
        this.menuData = menuData
        popupWindow = PopupWindow(
            createMenuContentView(),
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            isOutsideTouchable = true
            elevation = convertDpToPx(30f).toFloat()
        }
        return this
    }

    fun show(anchorView: View): PopupWindow? {
        val popupHeight = itemHeight * menuData.items.size + menuTopBottomPadding * 2
        val y = if (anchorView.bottom + popupHeight > parentView.bottom ) {
            if (anchorView.bottom > parentView.bottom) popupHeight + anchorView.height else popupHeight
        } else 0
        popupWindow?.showAsDropDown(anchorView, 0, -y, Gravity.TOP or Gravity.END)
        return popupWindow
    }

    private fun createMenuContentView(): View {
        val menuContentView = layoutInflater.inflate(R.layout.view_popup_menu, null)
        val itemsLayout: LinearLayout = menuContentView.findViewById(R.id.itemsLayout)
        menuData.items.forEach { item ->
            itemsLayout.addMenuItem(
                item = item,
                clickListener = View.OnClickListener { view ->
                    popupWindow?.dismiss()
                    val itemId: Int = view.tag as Int
                    if (menuData.items.any { it.id == itemId }) {
                        itemClickListener?.onMenuItemClick(menuData.menuId, itemId)
                    }
                }
            )
        }
        menuContentView.invalidate()
        return menuContentView
    }

    private fun LinearLayout.addMenuItem(item: MenuItemData, clickListener: View.OnClickListener) {
        val itemView = layoutInflater.inflate(R.layout.view_popup_menu_item, this, false)
        itemView.tag = item.id

        itemView
            .findViewById<ImageView>(R.id.iconView)
            .setImageDrawable(ContextCompat.getDrawable(layoutInflater.context, item.iconRes))

        itemView.findViewById<TextView>(R.id.labelView).setText(item.textRes)
        itemView.setOnClickListener(clickListener)
        this.addView(itemView)
    }

    interface ItemClickListener {
        fun onMenuItemClick(menuId: Int, itemId: Int)
    }
}
