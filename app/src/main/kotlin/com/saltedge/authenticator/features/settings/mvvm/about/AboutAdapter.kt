package com.saltedge.authenticator.features.settings.mvvm.about

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.saltedge.authenticator.R
import com.saltedge.authenticator.databinding.AboutBinding
import com.saltedge.authenticator.features.settings.common.SettingsItemViewModel
import com.saltedge.authenticator.widget.list.AbstractListAdapter

class AboutAdapter(private val clickListener: OnItemClickListener?) : AbstractListAdapter() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AboutItemViewHolder {
        val binding: AboutBinding = DataBindingUtil
            .inflate(
                LayoutInflater.from(parent.context), R.layout.view_item_about,
                parent, false
            )
        return AboutItemViewHolder(binding = binding)
    }

    override fun onBindHolder(holder: RecyclerView.ViewHolder, position: Int, item: Any) {
        (holder as AboutItemViewHolder).bind(item as SettingsItemViewModel, listener = clickListener)
    }
}
