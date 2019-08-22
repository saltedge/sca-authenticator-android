package com.saltedge.authenticator.tool

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

private val imageLoaderOptions = RequestOptions()
    .diskCacheStrategy(DiskCacheStrategy.ALL)

/**
 * Loads image from remote resource to image view
 *
 * @receiver image view
 * @param imageUrl - remote image url
 * @param placeholderId - image to be set as default
 */
fun ImageView.loadImage(imageUrl: String?, placeholderId: Int) {
    try {
        Glide.with(context)
            .load(imageUrl)
            .apply(imageLoaderOptions)
            .placeholder(placeholderId)
            .error(placeholderId)
            .fitCenter()
            .into(this)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
