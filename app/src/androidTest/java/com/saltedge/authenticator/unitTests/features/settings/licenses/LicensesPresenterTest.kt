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
package com.saltedge.authenticator.unitTests.features.settings.licenses

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.saltedge.authenticator.features.settings.licenses.LicensesContract
import com.saltedge.authenticator.features.settings.licenses.LicensesPresenter
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.inOrder

@RunWith(AndroidJUnit4::class)
class LicensesPresenterTest {

    @Test
    @Throws(Exception::class)
    fun constructorTest() {
        Assert.assertNull(createPresenter(viewContract = null).viewContract)
        Assert.assertNotNull(createPresenter(viewContract = mockView).viewContract)
    }

    @Test
    @Throws(Exception::class)
    fun getListItemsTest() {
        val presenter = createPresenter(viewContract = mockView)

        assertThat(presenter.getListItems(), equalTo(listOf(
                "Realm",
                "Dagger",
                "Android Compat Support Library",
                "Android Constraint Layout Library",
                "Android Material Components",
                "Square/Retrofit2",
                "Square/Okhttp3",
                "JodaTime",
                "Bumptech/Glide",
                "JUnit",
                "Jacoco",
                "Java Hamcrest",
                "Mockito",
                "MockK",
                "findbugs/jsr305",
                "ktlint",
                "jlleitschuh/ktlint-gradle"
        )))
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTest() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.onListItemClick(itemCode = "Realm")
        presenter.onListItemClick(itemCode = "Dagger")
        presenter.onListItemClick(itemCode = "Android Compat Support Library")
        presenter.onListItemClick(itemCode = "Android Constraint Layout Library")
        presenter.onListItemClick(itemCode = "Android Material Components")
        presenter.onListItemClick(itemCode = "Square/Retrofit2")
        presenter.onListItemClick(itemCode = "Square/Okhttp3")
        presenter.onListItemClick(itemCode = "JodaTime")
        presenter.onListItemClick(itemCode = "Bumptech/Glide")
        presenter.onListItemClick(itemCode = "JUnit")
        presenter.onListItemClick(itemCode = "Jacoco")
        presenter.onListItemClick(itemCode = "Java Hamcrest")
        presenter.onListItemClick(itemCode = "Mockito")
        presenter.onListItemClick(itemCode = "MockK")
        presenter.onListItemClick(itemCode = "findbugs/jsr305")

        val inOrder = inOrder(mockView)
        inOrder.verify(mockView).openLink(url = apacheLicense, title = "Realm")
        inOrder.verify(mockView).openLink(url = apacheLicense, title = "Dagger")
        inOrder.verify(mockView).openLink(url = apacheLicense, title = "Android Compat Support Library")
        inOrder.verify(mockView).openLink(url = apacheLicense, title = "Android Constraint Layout Library")
        inOrder.verify(mockView).openLink(url = apacheLicense, title = "Android Material Components")
        inOrder.verify(mockView).openLink(url = apacheLicense, title = "Square/Retrofit2")
        inOrder.verify(mockView).openLink(url = apacheLicense, title = "Square/Okhttp3")
        inOrder.verify(mockView).openLink(url = apacheLicense, title = "JodaTime")
        inOrder.verify(mockView).openLink(url = "https://raw.githubusercontent.com/bumptech/glide/master/LICENSE", title = "Bumptech/Glide")
        inOrder.verify(mockView).openLink(url = "https://junit.org/junit4/license.html", title = "JUnit")
        inOrder.verify(mockView).openLink(url = "https://www.jacoco.org/jacoco/trunk/doc/license.html", title = "Jacoco")
        inOrder.verify(mockView).openLink(url = "https://raw.githubusercontent.com/hamcrest/JavaHamcrest/master/LICENSE.txt", title = "Java Hamcrest")
        inOrder.verify(mockView).openLink(url = "https://raw.githubusercontent.com/mockito/mockito/release/2.x/LICENSE", title = "Mockito")
        inOrder.verify(mockView).openLink(url = apacheLicense, title = "MockK")
        inOrder.verify(mockView).openLink(url = "https://raw.githubusercontent.com/findbugsproject/findbugs/master/findbugs/licenses/LICENSE-jsr305.txt", title = "findbugs/jsr305")
    }

    @Test
    @Throws(Exception::class)
    fun onListItemClickTest_invalidParams() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.onListItemClick()

        Mockito.verifyNoMoreInteractions(mockView)

        presenter.viewContract = null
        presenter.onListItemClick()
        presenter.onListItemClick(itemCode = "Realm")

        Mockito.verifyNoMoreInteractions(mockView)
    }

    private val mockView = Mockito.mock(LicensesContract.View::class.java)
    private val apacheLicense = "https://www.apache.org/licenses/LICENSE-2.0.txt"

    private fun createPresenter(viewContract: LicensesContract.View? = null): LicensesPresenter {
        return LicensesPresenter().apply { this.viewContract = viewContract }
    }
}
