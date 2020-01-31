package com.saltedge.authenticator.features.connections.actions

import android.os.Bundle
import com.saltedge.authenticator.app.KEY_GUID
import com.saltedge.authenticator.sdk.tools.ActionDeepLinkData
import com.saltedge.authenticator.widget.fragment.BaseFragment

class ActionFragment : BaseFragment() {

    companion object {
        fun newInstance(
            connectionGuid: String,
            actionDeepLinkData: ActionDeepLinkData? = null
        ): ActionFragment {
            return ActionFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("ACTION_DEEP_LINK_DATA", actionDeepLinkData)
                    putString(KEY_GUID, connectionGuid)
                }
            }
        }
    }
}
