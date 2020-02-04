package com.saltedge.authenticator.features.connections.actions

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.KEY_GUID
import com.saltedge.authenticator.features.connections.actions.di.ActionModule
import com.saltedge.authenticator.interfaces.UpActionImageListener
import com.saltedge.authenticator.sdk.tools.ActionDeepLinkData
import com.saltedge.authenticator.tool.*
import com.saltedge.authenticator.widget.fragment.BaseFragment
import kotlinx.android.synthetic.main.fragment_action.*
import kotlinx.android.synthetic.main.fragment_connect_processing.view.*
import javax.inject.Inject

const val KEY_ACTION_DEEP_LINK_DATA = "ACTION_DEEP_LINK_DATA"

class ActionFragment : BaseFragment(),
    ActionContract.View,
    UpActionImageListener,
    View.OnClickListener
{

    @Inject
    lateinit var presenterContract: ActionContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
        arguments?.getString(KEY_GUID)?.let {
            presenterContract.setInitialData(
                connectionGuid = it,
                actionDeepLinkData = arguments?.getSerializable(KEY_ACTION_DEEP_LINK_DATA) as ActionDeepLinkData)
        }
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
        completeView?.setOnClickListener(this)
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
        completeView?.setIconResource(presenterContract.iconResId)
        completeView?.setTitleText(presenterContract.completeTitle)
        completeView?.setSubtitleText(presenterContract.completeMessage)
        completeView?.setMainActionText(presenterContract.mainActionTextResId)
        fragmentActionProcessing.titleView.text = getString(R.string.action_status_in_progress)

        updateLayoutsVisibility()
    }

    override fun getUpActionImageResId(): ResId? = R.drawable.ic_close_white_24dp

    override fun closeView() {
        activity?.finishFragment()
    }

    override fun showErrorAndFinish(message: String) {
        activity?.showWarningDialog(
            message = message,
            listener = DialogInterface.OnClickListener { _: DialogInterface, _: Int ->
                activity?.finishFragment()
            }
        )
    }

    override fun onClick(v: View?) {
        presenterContract.onViewClick(view?.id ?: return)
    }

    private fun injectDependencies() {
        authenticatorApp?.appComponent?.addActionModule(ActionModule())?.inject(
            this
        )
    }

    private fun updateLayoutsVisibility() {
        fragmentActionProcessing?.setVisible(show = !presenterContract.showCompleteView)
        completeView?.setVisible(show = presenterContract.showCompleteView)
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
