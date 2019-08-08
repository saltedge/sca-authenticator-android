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
package com.saltedge.authenticator.features.settings.licenses

import com.saltedge.authenticator.interfaces.ListItemClickListener

private const val LINK_APACHE_2_LICENSE = "https://www.apache.org/licenses/LICENSE-2.0.txt"

class LicensesPresenter : ListItemClickListener {

    var viewContract: LicensesContract.View? = null

    private val listData: Map<String, String> = mapOf(
        "Realm" to LINK_APACHE_2_LICENSE,
        "Dagger" to LINK_APACHE_2_LICENSE,
        "Android Compat Support Library" to LINK_APACHE_2_LICENSE,
        "Android Constraint Layout Library" to LINK_APACHE_2_LICENSE,
        "Android Material Components" to LINK_APACHE_2_LICENSE,
        "Square/Retrofit2" to LINK_APACHE_2_LICENSE,
        "Square/Okhttp3" to LINK_APACHE_2_LICENSE,
        "JodaTime" to LINK_APACHE_2_LICENSE,
        "Bumptech/Glide" to "https://raw.githubusercontent.com/bumptech/glide/master/LICENSE",
        "JUnit" to "https://junit.org/junit4/license.html",
        "Jacoco" to "https://www.jacoco.org/jacoco/trunk/doc/license.html",
        "Java Hamcrest" to "https://raw.githubusercontent.com/hamcrest/JavaHamcrest/master/LICENSE.txt",
        "Mockito" to "https://raw.githubusercontent.com/mockito/mockito/release/2.x/LICENSE",
        "MockK" to LINK_APACHE_2_LICENSE,
        "findbugs/jsr305" to "https://raw.githubusercontent.com/findbugsproject/findbugs/master/findbugs/licenses/LICENSE-jsr305.txt",
        "ktlint" to "https://raw.githubusercontent.com/pinterest/ktlint/master/LICENSE",
        "jlleitschuh/ktlint-gradle" to "https://raw.githubusercontent.com/JLLeitschuh/ktlint-gradle/master/LICENSE.txt"
    )

    fun getListItems(): List<String> = listData.keys.toList()

    override fun onListItemClick(itemIndex: Int, itemCode: String, itemViewId: Int) {
        listData[itemCode]?.let { viewContract?.openLink(it, itemCode) }
    }
}
