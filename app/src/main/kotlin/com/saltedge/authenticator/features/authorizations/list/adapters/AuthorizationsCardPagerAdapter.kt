package com.saltedge.authenticator.features.authorizations.list.adapters

import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.saltedge.authenticator.R
import com.saltedge.authenticator.sdk.tools.remainedExpirationTime
import com.saltedge.authenticator.sdk.tools.secondsFromDate
import com.saltedge.authenticator.tool.loadImage
import com.saltedge.authenticator.tool.setVisible

class AuthorizationsCardPagerAdapter(context: Context) : AuthorizationsPagerAdapter() {

    private val layoutInflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        return inflatePageView(position).also { container.addView(it, 0) }
    }

    private fun inflatePageView(position: Int): View {
        val pageView = layoutInflater.inflate(R.layout.view_card_item_authorization, null)
        return pageView.apply { updateViewContent(this, position) }
    }

    private fun updateViewContent(pageView: View, position: Int) {
        pageView.findViewById<TextView>(R.id.providerNameView).text = data[position].connectionName
        val connectionLogoView = pageView.findViewById<ImageView>(R.id.connectionLogoView)
        if (data[position].connectionLogoUrl?.isEmpty() == true) {
            connectionLogoView?.setVisible(false)
        } else {
            connectionLogoView?.loadImage(
                imageUrl = data[position].connectionLogoUrl,
                placeholderId = R.drawable.ic_logo_bank_placeholder
            )
        }
        pageView.findViewById<TextView>(R.id.timerTextView)?.text =
            data[position].expiresAt.remainedExpirationTime()
        pageView.findViewById<ProgressBar>(R.id.progressBar)?.max =
            data[position].validSeconds
        pageView.findViewById<ProgressBar>(R.id.progressBar)?.progress =
            data[position].createdAt.secondsFromDate()
        pageView.findViewById<ProgressBar>(R.id.progressBar)?.progressDrawable?.setColorFilter(
            ContextCompat.getColor(pageView.context, R.color.blue),
            PorterDuff.Mode.SRC_IN
        )
    }
}
