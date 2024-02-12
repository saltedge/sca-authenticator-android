/*
 * Copyright (c) 2020 Salt Edge Inc.
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
import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.menu.MenuItemData
import com.saltedge.authenticator.tools.convertDpToPx
import com.saltedge.authenticator.tools.setTextColorResId

/**
 * Build view and show PopupWindow with items from MenuData
 */
class PopupMenuBuilder(
    private val parentView: View,
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
        val popupWidth = anchorView.context.resources.getDimensionPixelSize(R.dimen.popupMenuItemWidth)
        val margin = anchorView.context.resources.getDimensionPixelSize(R.dimen.dp_32)
        val x = parentView.right - popupWidth - margin
        val y = if (anchorView.bottom + popupHeight > parentView.bottom ) {
            if (anchorView.bottom > parentView.bottom) popupHeight + anchorView.height else popupHeight
        } else 0
        popupWindow?.showAsDropDown(anchorView, x, -y, Gravity.START)
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

        val iconView = itemView.findViewById<ImageView>(R.id.iconView)
        iconView.setImageResource(item.iconRes)

        itemView.findViewById<TextView>(R.id.labelView).apply {
            item.textRes?.let { setText(it) }
            item.text?.let { text = it }
            setTextColorResId(R.color.dark_100_and_grey_40)
        }

        itemView.setOnClickListener(if (item.isActive) clickListener else null)
        this.addView(itemView)
    }

    interface ItemClickListener {
        fun onMenuItemClick(menuId: Int, itemId: Int)
    }
}
