package com.saltedge.authenticator.tools

import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.saltedge.authenticator.R
import com.saltedge.authenticator.widget.RoundedBitmapTransformation
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
fun ImageView.loadImage(imageUrl: String?, placeholderId: ResId, cornerRadius: Float = -1f) {
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
