/**
 * @author Fentury Team
 * Copyright (c) 2020 Salt Edge. All rights reserved.
 */
package com.fentury.applock.root

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity

/**
 * Show dialog fragment
 *
 * @receiver fragment activity
 * @param dialog object
 */
fun FragmentActivity.showDialogFragment(dialog: DialogFragment) {
    try {
        dialog.show(supportFragmentManager, dialog.javaClass.simpleName)
    } catch (ignored: IllegalStateException) {
    } catch (e: Exception) {}
}