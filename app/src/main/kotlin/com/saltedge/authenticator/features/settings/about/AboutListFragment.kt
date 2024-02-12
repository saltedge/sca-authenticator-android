/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.settings.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.ViewModelsFactory
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.features.settings.common.SettingsAdapter
import com.saltedge.authenticator.app.authenticatorApp
import com.saltedge.authenticator.databinding.FragmentBaseListBinding
import com.saltedge.authenticator.tools.navigateTo
import com.saltedge.authenticator.widget.fragment.BaseFragment
import javax.inject.Inject

class AboutListFragment : BaseFragment() {

    @Inject lateinit var viewModelFactory: ViewModelsFactory
    lateinit var viewModel: AboutViewModel
    private lateinit var binding: FragmentBaseListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authenticatorApp?.appComponent?.inject(this)
        setupViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBaseListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activityComponents?.updateAppbar(
            titleResId = R.string.about_feature_title,
            backActionImageResId = R.drawable.ic_appbar_action_back
        )
        setupViews()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory).get(AboutViewModel::class.java)

        viewModel.licenseItemClickEvent.observe(this, Observer<ViewModelEvent<Unit>> {
            it?.let { navigateTo(R.id.license) }
        })
        viewModel.termsOfServiceItemClickEvent.observe(this, Observer<ViewModelEvent<Bundle>> {
            it?.getContentIfNotHandled()?.let { bundle ->
                navigateTo(
                    actionRes = R.id.terms_of_services,
                    bundle = bundle
                )
            }
        })
    }

    private fun setupViews() {
        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.adapter = SettingsAdapter(listener = viewModel)
            .apply { data = viewModel.listItems }
    }
}
