/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.core.tools

import android.os.Build
import android.text.Html
import android.text.Spanned
import androidx.core.text.HtmlCompat

@Suppress("DEPRECATION")
fun String.hasHTMLTags(): Boolean {
    val parsedString = parseHTMLToSpanned()
    return parsedString.getSpans(0, parsedString.length - 1, Any::class.java).isNotEmpty()
}

@Suppress("DEPRECATION")
private fun String.parseHTMLToSpanned(): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(this)
    }
}
