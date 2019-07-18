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
package com.saltedge.authenticator.features.connections.options

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.KEY_GUID
import com.saltedge.authenticator.app.KEY_OPTION_ID
import com.saltedge.authenticator.features.connections.common.ConnectionOptions
import com.saltedge.authenticator.interfaces.ListItemClickListener
import com.saltedge.authenticator.widget.fragment.BaseBottomDialogFragment

class ConnectionOptionsDialog : BaseBottomDialogFragment(), ListItemClickListener {

    private val presenter = ConnectionOptionsPresenter()
    private val adapter = ConnectionOptionsAdapter(this)
    private var contentRecyclerView: RecyclerView? = null

    override fun getDialogViewLayout(): Int = R.layout.dialog_options

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.setInitialData(arguments?.getIntArray(KEY_OPTION_ID))
    }

    override fun onStart() {
        super.onStart()
        setupDialogViews()
    }

    override fun onListItemClick(itemIndex: Int, itemCode: String, itemViewId: Int) {
        val item = adapter.getItem(itemIndex) as ConnectionOptions
        dismiss()
        targetFragment?.onActivityResult(
                targetRequestCode,
                RESULT_OK,
                Intent().putExtra(KEY_GUID, arguments?.getString(KEY_GUID))
                        .putExtra(KEY_OPTION_ID, item.ordinal)
        )
    }

    private fun setupDialogViews() {
        contentRecyclerView = dialog?.findViewById<RecyclerView?>(R.id.recyclerView)
        activity?.let { contentRecyclerView?.layoutManager = LinearLayoutManager(it) }
        contentRecyclerView?.adapter = adapter
        adapter.data = presenter.listItems
    }

    companion object {
        fun newInstance(connectionGuid: String, options: Array<ConnectionOptions>): ConnectionOptionsDialog {
            return ConnectionOptionsDialog().apply {
                arguments = Bundle().apply {
                    putString(KEY_GUID, connectionGuid)
                    putIntArray(KEY_OPTION_ID, options.map { it.ordinal }.toIntArray())
                }
            }
        }
    }
}
