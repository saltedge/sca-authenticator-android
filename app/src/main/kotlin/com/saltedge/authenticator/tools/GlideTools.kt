package com.saltedge.authenticator.tools

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.imageview.ShapeableImageView
import timber.log.Timber

private val imageLoaderOptions = RequestOptions()
    .diskCacheStrategy(DiskCacheStrategy.ALL)

/**
 * Loads image from remote resource to image view
 *
 * @receiver image view
 * @param imageUrl - remote image url
 * @param placeholderId - image to be set as default
 */
fun ShapeableImageView.loadImage(imageUrl: String?, placeholderId: ResId) {
    try {
        Glide.with(context)
            .load(imageUrl)
            .apply(imageLoaderOptions)
            .placeholder(placeholderId)
            .error(placeholderId)
            .fitCenter()
            .into(this)
    } catch (e: Exception) {
        Timber.e(e)
    }
}
