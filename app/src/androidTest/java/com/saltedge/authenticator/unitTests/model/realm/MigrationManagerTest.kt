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
package com.saltedge.authenticator.unitTests.model.realm

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.saltedge.authenticator.instrumentationTestTools.TestTools
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.realm.DB_SCHEMA_VERSION
import com.saltedge.authenticator.models.realm.runMigrations
import io.realm.Realm
import io.realm.RealmConfiguration
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

private const val INITIAL_DB_NAME = "initial.realm"
private const val INITIAL_DB_PATH = "db/$INITIAL_DB_NAME"
private const val INITIAL_DB_SIZE = 8192

@RunWith(AndroidJUnit4::class)
class MigrationManagerTest {

    @Test
    @Throws(Exception::class)
    fun testMigration() {
        Realm.init(TestTools.applicationContext)
        val config = RealmConfiguration.Builder()
            .name(INITIAL_DB_NAME)
            .schemaVersion(DB_SCHEMA_VERSION)
            .migration(runMigrations())
            .build()

        assertThat(copyInitialRealmFile(INITIAL_DB_PATH, config.path), equalTo(INITIAL_DB_SIZE))
        assertThat(File(config.path).length(), equalTo(INITIAL_DB_SIZE.toLong()))

        Realm.getInstance(config).use {
            testMigration2(it)
            testMigration3(it)
            testMigration4(it)
        }
    }

    private fun testMigration2(realm: Realm) {
        val connection = realm.where(Connection::class.java).findFirst()

        assertNull(connection?.supportEmail)
    }

    private fun testMigration3(realm: Realm) {
        val connection = realm.where(Connection::class.java).findFirst()

        assertNull(connection?.consentManagementSupported)
        assertNull(connection?.geolocationRequired)
    }

    private fun testMigration4(realm: Realm) {
        val connection = realm.where(Connection::class.java).findFirst()

        assertThat(connection?.providerRsaPublicKeyPem, equalTo(""))
    }

    private fun copyInitialRealmFile(sourcePath: String, targetPath: String): Int {
        removeTargetFile(targetPath)
        return copyFile(sourcePath, targetPath)
    }

    private fun copyFile(sourcePath: String, targetPath: String): Int {
        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null
        var totalBytesRead = 0
        try {
            val file = File(targetPath)
            inputStream =
                InstrumentationRegistry.getInstrumentation().context.resources.assets.open(
                    sourcePath
                )
            outputStream = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var bytesRead = inputStream.read(buffer)
            while (bytesRead > 0) {
                totalBytesRead += bytesRead
                outputStream.write(buffer, 0, bytesRead)
                bytesRead = inputStream.read(buffer)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
        return totalBytesRead
    }

    private fun removeTargetFile(targetPath: String) {
        with(File(targetPath)) { if (exists()) delete() }
    }
}
