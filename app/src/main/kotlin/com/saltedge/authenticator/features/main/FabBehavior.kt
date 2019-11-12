package com.saltedge.authenticator.features.main

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.saltedge.authenticator.R

class FabBehavior(context: Context, attrs: AttributeSet?): CoordinatorLayout.Behavior<FloatingActionButton>(context, attrs) {

    private val fabDefaultBottomMargin = context.resources?.getDimension(R.dimen.dp_16)?.toInt() ?: 0

    constructor(context: Context) : this(context, null)

    override fun layoutDependsOn(parent: CoordinatorLayout, child: FloatingActionButton, dependency: View): Boolean {
        return dependency != null && (dependency is Snackbar.SnackbarLayout || dependency.id == R.id.bottomDivider)
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: FloatingActionButton, dependency: View): Boolean {
        if (parent != null && child != null &&
            dependency != null && (dependency is Snackbar.SnackbarLayout || dependency.id == R.id.bottomDivider))
            updateFloatingActionButton(parent, child)
        return true
    }

    private fun updateFloatingActionButton(parent: CoordinatorLayout, child: FloatingActionButton) {
        val y = parent.getDependencies(child).map { it.y }.sorted().firstOrNull()
            ?: (parent.y + parent.height)
        child.y = y - fabDefaultBottomMargin - child.height
    }
}
