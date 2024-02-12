/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.consents.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.ViewModelsFactory
import com.saltedge.authenticator.app.authenticatorApp
import com.saltedge.authenticator.databinding.FragmentConsentsListBinding
import com.saltedge.authenticator.features.main.SharedViewModel
import com.saltedge.authenticator.features.main.showWarningSnack
import com.saltedge.authenticator.interfaces.ListItemClickListener
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.tools.loadImage
import com.saltedge.authenticator.tools.navigateTo
import com.saltedge.authenticator.tools.stopRefresh
import com.saltedge.authenticator.widget.fragment.BaseFragment
import com.saltedge.authenticator.widget.list.SpaceItemDecoration
import javax.inject.Inject

class ConsentsListFragment : BaseFragment(), ListItemClickListener {

    @Inject lateinit var viewModelFactory: ViewModelsFactory
    private lateinit var viewModel: ConsentsListViewModel
    private lateinit var headerDecorator: SpaceItemDecoration
    private var binding: FragmentConsentsListBinding? = null
    private val adapter = ConsentsListAdapter(clickListener = this)
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authenticatorApp?.appComponent?.inject(this)
        setupViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activityComponents?.updateAppbar(
            titleResId = R.string.consents_feature_title,
            backActionImageResId = R.drawable.ic_appbar_action_back
        )
        binding = FragmentConsentsListBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    override fun onListItemClick(itemIndex: Int, itemCode: String, itemViewId: Int) {
        viewModel.onListItemClick(itemIndex)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory).get(ConsentsListViewModel::class.java)

        viewModel.listItems.observe(this, Observer<List<ConsentItem>> {
            headerDecorator.setHeaderForAllItems(it.count())
            headerDecorator.footerPositions = arrayOf(it.count() - 1)
            adapter.data = it
        })
        viewModel.logoUrlData.observe(this, Observer<String> { logoUrl ->
            if (logoUrl == null) { //move this logic to vm
                binding?.connectionLogoView?.setImageDrawable(null)
            } else {
                binding?.connectionLogoView?.loadImage(logoUrl, R.drawable.shape_bg_app_logo)
            }
        })
        viewModel.connectionTitleData.observe(this, Observer<String> { text ->
            binding?.connectionTitleView?.text = text
        })
        viewModel.consentsCount.observe(this, Observer<String> { text ->
            binding?.consentsCountView?.text = text
        })
        viewModel.onListItemClickEvent.observe(this, Observer<ViewModelEvent<Bundle>> { event ->
            event.getContentIfNotHandled()?.let { bundle ->
                navigateTo(actionRes = R.id.consent_details, bundle = bundle)
            }
        })
        viewModel.onConsentRemovedEvent.observe(this, Observer<ViewModelEvent<String>> { event ->
            event.getContentIfNotHandled()?.let { message ->
                activity?.showWarningSnack(message = message, snackBarDuration = Snackbar.LENGTH_SHORT)
            }
        })
        viewModel.setInitialData(arguments)
    }

    private fun setupViews() {
        activity?.let {
            binding?.consentsListView?.layoutManager = LinearLayoutManager(it)
            binding?.consentsListView?.adapter = adapter
            if (!this::headerDecorator.isInitialized) headerDecorator = SpaceItemDecoration(context = it)
            binding?.consentsListView?.addItemDecoration(headerDecorator)
        }
        binding?.swipeRefreshLayout?.setOnRefreshListener {
            viewModel.refreshConsents()
            binding?.swipeRefreshLayout?.stopRefresh()
        }
        binding?.swipeRefreshLayout?.setColorSchemeResources(R.color.primary, R.color.red, R.color.green)
        sharedViewModel.onRevokeConsent.observe(viewLifecycleOwner, Observer<String> { result ->
            viewModel.onRevokeConsent(result)
        })
    }
}
