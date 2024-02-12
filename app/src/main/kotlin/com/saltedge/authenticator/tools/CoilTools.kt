/*
 * Copyright (c) 2022 Salt Edge Inc.
 */
package com.saltedge.authenticator.tools

import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.google.android.material.imageview.ShapeableImageView
import timber.log.Timber

/**
 * Loads image from remote resource to image view
 *
 * @receiver image view
 * @param imageUrl - remote image url
 * @param placeholderId - image to be set as default
 */
fun ShapeableImageView.loadImage(imageUrl: String?, placeholderId: ResId) {
    try {
        val imageLoader = ImageLoader.Builder(this.context)
            .componentRegistry { add(SvgDecoder(this@loadImage.context)) }
            .build()

        val request = ImageRequest.Builder(this.context)
            .crossfade(true)
            .crossfade(500)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .placeholder(placeholderId)
            .error(placeholderId)
            .data(imageUrl)
            .target(this)
            .build()

        imageLoader.enqueue(request)
    } catch (e: Exception) {
        Timber.e(e)
    }
}
