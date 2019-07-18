package com.saltedge.authenticator.unitTests.tool

import android.text.style.URLSpan
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.saltedge.authenticator.tool.parseHTML
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HTMLToolsTest {

    @Test
    @Throws(Exception::class)
    fun parseHTMLTest() {
        var spannedMessage = "<a href='https://www.fentury.com/'>Fentury</a>".parseHTML()

        assertThat(spannedMessage.toString(), equalTo("Fentury"))
        assertThat(spannedMessage.getSpans(0, 7, URLSpan::class.java).first().url,
                equalTo("https://www.fentury.com/"))

        spannedMessage = "Fentury.com".parseHTML()

        assertThat(spannedMessage.toString(), equalTo("Fentury.com"))
    }
}