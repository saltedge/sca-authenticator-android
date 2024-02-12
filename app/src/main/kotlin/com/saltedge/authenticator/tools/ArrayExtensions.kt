/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.tools

import android.util.SparseArray

/**
 * Returns the value for the given key. If the key is not found in the SparseArray,
 * calls the [defaultValue] function, puts its result into the array under the given key
 * and returns it.
 *
 * @see HashMap.getOrPut()
 */
inline fun <V> SparseArray<V>.getOrPut(key: Int, defaultValue: () -> V): V {
    return this.get(key) ?: defaultValue().also { this.put(key, it) }
}

