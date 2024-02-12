/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.interfaces

interface OnBackPressListener {

    /**
     * Notify about back press action
     *
     * @return true if back press should be stopped by caller
     */
    fun onBackPress(): Boolean
}
