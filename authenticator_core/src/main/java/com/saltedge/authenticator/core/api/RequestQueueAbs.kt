/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.core.api

/**
 * Implements logic for handling multiple requests queue
 *
 * @see ApiResponseInterceptor
 */
abstract class RequestQueueAbs<T> : ApiResponseInterceptor<T>() {

    private var queueCount: Int = 0

    fun setQueueSize(size: Int) {
        queueCount = size
    }

    protected fun onResponseReceived() {
        countDown()
        if (queueIsEmpty()) onQueueFinished()
    }

    protected fun queueIsEmpty() = queueCount <= 0

    protected abstract fun onQueueFinished()

    private fun countDown() {
        queueCount--
    }
}
