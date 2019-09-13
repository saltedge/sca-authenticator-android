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

import android.content.Intent
import com.saltedge.authenticator.features.connections.common.ConnectionOptions
import com.saltedge.authenticator.features.connections.common.ConnectionViewModel
import com.saltedge.authenticator.interfaces.BaseViewContract
import com.saltedge.authenticator.sdk.model.GUID

interface ConnectionsListContract {
    interface View : BaseViewContract {
        fun showApiErrorView(message: String)
        fun showSupportView(supportEmail: String?)
        fun showConnectView(connectionGuid: GUID)
        fun showConnectionNameEditView(
            connectionGuid: GUID,
            connectionName: String,
            requestCode: Int
        )

        fun showDeleteConnectionView(connectionGuid: GUID? = null, requestCode: Int)
        fun showOptionsView(
            connectionGuid: GUID,
            options: Array<ConnectionOptions>,
            requestCode: Int
        )

        fun showQrScanView()
        fun updateListItemName(connectionGuid: GUID, name: String)
    }

    /**
     * Abstraction of ConnectionsList Presenter
     * @see ConnectionsListPresenter
     */
    interface Presenter {
        var viewContract: View?
        fun getListItems(): List<ConnectionViewModel>
        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
        fun onListItemClick(connectionGuid: String)
        fun onViewClick(viewId: Int)
    }
}
