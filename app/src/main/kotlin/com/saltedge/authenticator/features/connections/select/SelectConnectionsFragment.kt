/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.connections.select

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.ViewModelsFactory
import com.saltedge.authenticator.features.connections.common.ConnectionItem
import com.saltedge.authenticator.features.connections.list.ConnectionsListAdapter
import com.saltedge.authenticator.features.main.SharedViewModel
import com.saltedge.authenticator.interfaces.ListItemClickListener
import com.saltedge.authenticator.interfaces.OnBackPressListener
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.app.authenticatorApp
import com.saltedge.authenticator.core.model.GUID
import com.saltedge.authenticator.databinding.FragmentConnectionsListBinding
import com.saltedge.authenticator.databinding.FragmentConsentsListBinding
import com.saltedge.authenticator.tools.popBackStack
import com.saltedge.authenticator.tools.setVisible
import com.saltedge.authenticator.widget.fragment.BaseFragment
import com.saltedge.authenticator.widget.list.SpaceItemDecoration
import javax.inject.Inject

class SelectConnectionsFragment : BaseFragment(), OnBackPressListener, ListItemClickListener {

    @Inject lateinit var viewModelFactory: ViewModelsFactory
    private lateinit var viewModel: SelectConnectionsViewModel
    private val adapter = ConnectionsListAdapter(clickListener = this)
    private var headerDecorator: SpaceItemDecoration? = null
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var binding: FragmentConnectionsListBinding

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
        activityComponents?.updateAppbar(
            titleResId = R.string.choose_connection_feature_title,
            backActionImageResId = R.drawable.ic_appbar_action_close
        )
        binding = FragmentConnectionsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.emptyView.setVisible(false)
        binding.connectionsListView.setVisible(true)
        activity?.let {
            binding.connectionsListView.layoutManager = LinearLayoutManager(it)
            binding.connectionsListView.adapter = adapter
            headerDecorator = SpaceItemDecoration(context = it).apply {
                binding.connectionsListView.addItemDecoration(this)
            }
        }
        binding.proceedView.isEnabled = false
        binding.proceedView.setVisible(true)
    }

    override fun onBackPress(): Boolean {
        sharedViewModel.onSelectConnection("")
        return false
    }

    override fun onListItemClick(itemIndex: Int, itemCode: String, itemViewId: Int) {
        viewModel.onListItemClick(itemIndex)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory).get(SelectConnectionsViewModel::class.java)
        lifecycle.addObserver(viewModel)

        viewModel.listItems.observe(this, Observer<List<ConnectionItem>> {
            headerDecorator?.setHeaderForAllItems(it.count())
            headerDecorator?.footerPositions = arrayOf(it.count() - 1)
            it?.let { adapter.data = it }
        })

        viewModel.onListItemClickEvent.observe(this, Observer<ViewModelEvent<Int>> { event ->
            event.getContentIfNotHandled()?.let { itemIndex ->
                viewModel.listItemsValues.getOrNull(itemIndex)?.let { item ->
                    viewModel.changeStateItem(item)
                    adapter.notifyDataSetChanged()
                    binding.proceedView.isEnabled = true
                    binding.proceedView.setOnClickListener { viewModel.proceedConnection(item.guid) }
                }
            }
        })
        viewModel.onProceedClickEvent.observe(this, Observer<GUID> { connectionGuid ->
            sharedViewModel.onSelectConnection(connectionGuid)
            popBackStack()
        })

        (arguments?.getSerializable(KEY_CONNECTIONS) as? List<ConnectionItem>)?.let {
            viewModel.setInitialData(it)
        }
    }

    companion object {
        const val KEY_CONNECTIONS = "CONNECTIONS"

        fun dataBundle(connections: List<ConnectionItem>): Bundle {
            val arrayList = ArrayList<ConnectionItem>(connections)
            return Bundle().apply { this.putSerializable(KEY_CONNECTIONS, arrayList) }
        }
    }
}
