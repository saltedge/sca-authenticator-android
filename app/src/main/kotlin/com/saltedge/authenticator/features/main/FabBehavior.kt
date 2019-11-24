/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2019 Salt Edge Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 or later.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * For the additional permissions granted for Salt Edge Authenticator
 * under Section 7 of the GNU General Public License see THIRD_PARTY_NOTICES.md
 */
package com.saltedge.authenticator.features.main

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.saltedge.authenticator.R
import com.saltedge.authenticator.tool.printToLogcat

class FabBehavior : CoordinatorLayout.Behavior<FloatingActionButton> {

    constructor() : super()

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun layoutDependsOn(parent: CoordinatorLayout, child: FloatingActionButton, dependency: View): Boolean {
        return dependency is Snackbar.SnackbarLayout || dependency.id == R.id.bottomDivider
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: FloatingActionButton, dependency: View): Boolean {
        if (child.height > 0 && (dependency is Snackbar.SnackbarLayout || dependency.id == R.id.bottomDivider)) {
            updateFloatingActionButton(parent, child)
        }
        return true
    }

    private fun updateFloatingActionButton(parent: CoordinatorLayout, child: FloatingActionButton) {
        val fabDefaultBottomMargin = child.resources?.getDimension(R.dimen.dp_16)?.toInt() ?: 0
        val minDependencyY = parent.getDependencies(child).map { it.y }.min() ?: (parent.y + parent.height)
        child.y = minDependencyY - fabDefaultBottomMargin - child.height
    }
}
