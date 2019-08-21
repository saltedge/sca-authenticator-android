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
package com.saltedge.authenticator.unitTests.features.webView

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.saltedge.authenticator.R
import com.saltedge.authenticator.sdk.constants.KEY_TITLE
import com.saltedge.authenticator.testTools.TestTools
import com.saltedge.authenticator.widget.fragment.WebViewFragment
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WebViewFragmentTest {

    @Test
    @Throws(Exception::class)
    fun newInstanceTest() {
        var arguments = WebViewFragment.newInstance(
            url = "www.priora.com",
            title = TestTools.getString(R.string.app_name)
        ).arguments!!

        assertThat(arguments.getString(WebViewFragment.KEY_URL), equalTo("www.priora.com"))
        assertThat(arguments.getString(KEY_TITLE), equalTo(TestTools.getString(R.string.app_name)))

        arguments = WebViewFragment.newInstance(title = "test").arguments!!

        Assert.assertTrue(arguments.getString(WebViewFragment.KEY_URL)!!.isEmpty())
        assertThat(arguments.getString(KEY_TITLE), equalTo("test"))
    }
}
