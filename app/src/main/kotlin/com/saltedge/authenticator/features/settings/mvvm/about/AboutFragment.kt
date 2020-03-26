package com.saltedge.authenticator.features.settings.mvvm.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.settings.licenses.LicensesFragment
import com.saltedge.authenticator.sdk.constants.TERMS_LINK
import com.saltedge.authenticator.tool.addFragment
import com.saltedge.authenticator.tool.log
import com.saltedge.authenticator.widget.fragment.BaseFragment
import com.saltedge.authenticator.widget.fragment.WebViewFragment
import kotlinx.android.synthetic.main.fragment_base_list.*

class AboutFragment : BaseFragment() {

    private lateinit var viewModel: AboutViewModel
    private lateinit var viewModelFactory: AboutViewModelFactory
    private var onItemClickListener: OnItemClickListener = object : OnItemClickListener {
        override fun onItemClick(titleName: Int) {
            when (titleName) {
                R.string.about_terms_service -> {
                    activity?.addFragment(WebViewFragment.newInstance(TERMS_LINK, getString(R.string.about_terms_service)))
                }
                R.string.about_open_source_licenses -> activity?.addFragment(LicensesFragment())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewModelFactory = AboutViewModelFactory(requireContext(), onItemClickListener)
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(AboutViewModel::class.java)

        return inflater.inflate(R.layout.fragment_base_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activityComponents?.updateAppbarTitleWithFabAction(getString(R.string.about_feature_title))
        setupViews()
    }

    private fun setupViews() {
        try {
            val layoutManager = LinearLayoutManager(activity)
            recyclerView?.layoutManager = layoutManager
            val dividerItemDecoration = DividerItemDecoration(context, layoutManager.orientation)
            ContextCompat.getDrawable(context ?: return, R.drawable.shape_full_divider)?.let {
                dividerItemDecoration.setDrawable(it)
            }
            recyclerView?.addItemDecoration(dividerItemDecoration)
            recyclerView?.adapter = AboutAdapter(viewModel.onItemClickListener).apply {
                data = viewModel.getListItems()
            }
        } catch (e: Exception) {
            e.log()
        }
    }
}
