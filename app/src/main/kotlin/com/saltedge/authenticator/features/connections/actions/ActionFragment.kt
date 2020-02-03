package com.saltedge.authenticator.features.connections.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.KEY_GUID
import com.saltedge.authenticator.features.connections.actions.di.ActionModule
import com.saltedge.authenticator.interfaces.UpActionImageListener
import com.saltedge.authenticator.sdk.tools.ActionDeepLinkData
import com.saltedge.authenticator.tool.ResId
import com.saltedge.authenticator.tool.authenticatorApp
import com.saltedge.authenticator.tool.finishFragment
import com.saltedge.authenticator.tool.setVisible
import com.saltedge.authenticator.widget.fragment.BaseFragment
import kotlinx.android.synthetic.main.fragment_action.*
import javax.inject.Inject

const val KEY_ACTION_DEEP_LINK_DATA = "ACTION_DEEP_LINK_DATA"

class ActionFragment : BaseFragment(), ActionContract.View, UpActionImageListener {

    @Inject
    lateinit var presenterContract: ActionContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
        presenterContract.setInitialData(
            connectionGuid = arguments?.getString(KEY_GUID),
            actionDeepLinkData = arguments?.getSerializable(KEY_ACTION_DEEP_LINK_DATA) as? ActionDeepLinkData)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activityComponents?.updateAppbarTitleWithFabAction(getString(presenterContract.getTitleResId()))
        return inflater.inflate(R.layout.fragment_action, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateViewsContent()
        presenterContract.viewContract = this
        presenterContract.onViewCreated()
    }

    override fun onDestroyView() {
        presenterContract.viewContract = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        presenterContract.onDestroyView()
        super.onDestroy()
    }

    override fun updateViewsContent() {
        completeView?.setTitleText(presenterContract.completeTitle)

        updateLayoutsVisibility()
    }

    override fun getUpActionImageResId(): ResId? = R.drawable.ic_close_white_24dp

    override fun closeView() {
        activity?.finishFragment()
    }

    private fun injectDependencies() {
        authenticatorApp?.appComponent?.addActionModule(ActionModule())?.inject(
            this
        )
    }

    private fun updateLayoutsVisibility() {
        fragmentActionProcessing?.setVisible(show = presenterContract.shouldShowProgressView)
        completeView?.setVisible(show = presenterContract.shouldShowCompleteView)
    }

    companion object {
        fun newInstance(
            connectionGuid: String,
            actionDeepLinkData: ActionDeepLinkData? = null
        ): ActionFragment {
            return ActionFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(KEY_ACTION_DEEP_LINK_DATA, actionDeepLinkData)
                    putString(KEY_GUID, connectionGuid)
                }
            }
        }
    }
}
