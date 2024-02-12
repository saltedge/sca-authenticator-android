/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.widget.fragment

import com.saltedge.authenticator.R

abstract class BaseRoundedBottomDialogFragment : BaseBottomDialogFragment() {

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme
}
