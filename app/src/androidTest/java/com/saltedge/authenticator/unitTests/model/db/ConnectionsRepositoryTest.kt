/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.unitTests.model.db

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.saltedge.authenticator.core.model.ConnectionStatus
import com.saltedge.authenticator.instrumentationTestTools.*
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.repository.ConnectionsRepository
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConnectionsRepositoryTest : DatabaseTestCase() {

    @Test
    @Throws(Exception::class)
    fun isEmptyTest() {
        Assert.assertTrue(ConnectionsRepository.isEmpty())
        Assert.assertNotNull(Connection().save())
        Assert.assertFalse(ConnectionsRepository.isEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun getConnectionsCountTest() {
        assertThat(ConnectionsRepository.getConnectionsCount(), equalTo(0L))

        Connection().setGuid("guid1").save()

        assertThat(ConnectionsRepository.getConnectionsCount(), equalTo(1L))

        Connection().setGuid("guid2").save()

        assertThat(ConnectionsRepository.getConnectionsCount(), equalTo(2L))
    }

    @Test
    @Throws(Exception::class)
    fun getCountByCodeTest() {
        assertThat(ConnectionsRepository.getConnectionsCountForProvider(""), equalTo(0L))
        Connection().setGuid("guid1").setCode("demobank1").save()
        Connection().setGuid("guid2").setCode("demobank2").save()

        assertThat(ConnectionsRepository.getConnectionsCount(), equalTo(2L))
        assertThat(
            ConnectionsRepository.getConnectionsCountForProvider("demobank1"),
            equalTo(1L)
        )
    }

    @Test
    @Throws(Exception::class)
    fun hasValidConnectionsTest() {
        Assert.assertFalse(ConnectionsRepository.hasActiveConnections())

        Connection().setGuid("guid1").setAccessToken("").setStatus(ConnectionStatus.INACTIVE).save()

        Assert.assertFalse(ConnectionsRepository.hasActiveConnections())

        Connection().setGuid("guid2").setAccessToken("").setStatus(ConnectionStatus.ACTIVE).save()

        Assert.assertFalse(ConnectionsRepository.hasActiveConnections())

        Connection().setGuid("guid3").setAccessToken("token3").setStatus(ConnectionStatus.INACTIVE).save()

        Assert.assertFalse(ConnectionsRepository.hasActiveConnections())

        Connection().setGuid("guid4").setAccessToken("token4").setStatus(ConnectionStatus.ACTIVE).save()

        Assert.assertTrue(ConnectionsRepository.hasActiveConnections())
    }

    @Test
    @Throws(Exception::class)
    fun getAllTest() {
        Assert.assertTrue(ConnectionsRepository.isEmpty())

        Connection().setGuid("guid1").setAccessToken("").setStatus(ConnectionStatus.INACTIVE).save()
        Thread.sleep(100);
        Connection().setGuid("guid2").setAccessToken("").setStatus(ConnectionStatus.ACTIVE).save()
        Thread.sleep(100);
        Connection().setGuid("guid3").setAccessToken("token3").setStatus(ConnectionStatus.INACTIVE).save()
        Thread.sleep(100);
        Connection().setGuid("guid4").setAccessToken("token4").setStatus(ConnectionStatus.ACTIVE).save()

        assertThat(
            ConnectionsRepository.getAllConnections().map { it.guid },
            equalTo(listOf("guid1", "guid2", "guid3", "guid4"))
        )
    }

    @Test
    @Throws(Exception::class)
    fun getAllValidTest() {
        Assert.assertTrue(ConnectionsRepository.isEmpty())

        Connection().setGuid("guid1").setAccessToken("").setStatus(ConnectionStatus.INACTIVE).save()
        Connection().setGuid("guid2").setAccessToken("").setStatus(ConnectionStatus.ACTIVE).save()
        Connection().setGuid("guid3").setAccessToken("token3").setStatus(ConnectionStatus.INACTIVE).save()
        Connection().setGuid("guid4").setAccessToken("token4").setStatus(ConnectionStatus.ACTIVE).save()

        assertThat(
            ConnectionsRepository.getAllActiveConnections().map { it.guid },
            equalTo(listOf("guid4"))
        )
    }

    @Test
    @Throws(Exception::class)
    fun deleteAllTest() {
        Assert.assertTrue(ConnectionsRepository.isEmpty())

        Connection().setGuid("guid1").setAccessToken("").setStatus(ConnectionStatus.INACTIVE).save()
        Connection().setGuid("guid2").setAccessToken("token4").setStatus(ConnectionStatus.ACTIVE).save()

        assertThat(ConnectionsRepository.getConnectionsCount(), equalTo(2L))

        ConnectionsRepository.deleteAllConnections()

        Assert.assertTrue(ConnectionsRepository.isEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun deleteConnectionTest() {
        Assert.assertTrue(ConnectionsRepository.isEmpty())

        Assert.assertFalse(ConnectionsRepository.deleteConnection(""))
        Assert.assertFalse(ConnectionsRepository.deleteConnection("token1"))

        Connection().setGuid("guid1").setAccessToken("").setStatus(ConnectionStatus.INACTIVE).save()
        Connection().setGuid("guid2").setAccessToken("").setStatus(ConnectionStatus.ACTIVE).save()
        Connection().setGuid("guid3").setAccessToken("token3").setStatus(ConnectionStatus.INACTIVE).save()
        Connection().setGuid("guid4").setAccessToken("token4").setStatus(ConnectionStatus.ACTIVE).save()

        assertThat(ConnectionsRepository.getConnectionsCount(), equalTo(4L))

        Assert.assertFalse(ConnectionsRepository.deleteConnection("guid0"))

        assertThat(ConnectionsRepository.getConnectionsCount(), equalTo(4L))

        Assert.assertTrue(ConnectionsRepository.deleteConnection("guid4"))

        assertThat(ConnectionsRepository.getConnectionsCount(), equalTo(3L))
    }

    @Test
    @Throws(Exception::class)
    fun invalidateConnectionsByTokensTest() {
        Connection().setGuid("guid1").setAccessToken("token3").setStatus(ConnectionStatus.INACTIVE).save()
        Connection().setGuid("guid2").setAccessToken("token4").setStatus(ConnectionStatus.ACTIVE).save()

        assertThat(
            ConnectionsRepository.getAllActiveConnections().map { it.guid },
            equalTo(listOf("guid2"))
        )

        ConnectionsRepository.invalidateConnectionsByTokens(listOf("token4"))

        Assert.assertTrue(ConnectionsRepository.getAllActiveConnections().isEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun connectionExistTest() {
        Assert.assertFalse(ConnectionsRepository.connectionExists(connectionGuid = "guid1"))

        Connection().setId("1").setGuid("guid1").save()

        Assert.assertTrue(ConnectionsRepository.connectionExists(connectionGuid = "guid1"))
        Assert.assertFalse(ConnectionsRepository.connectionExists(connectionGuid = "guid2"))

        Assert.assertTrue(ConnectionsRepository.connectionExists(connection = Connection().setGuid("guid1")))
        Assert.assertFalse(
            ConnectionsRepository.connectionExists(
                connection = Connection().setGuid(
                    "guid2"
                )
            )
        )
    }

    @Test
    @Throws(Exception::class)
    fun getByIdTest() {
        assertNull(ConnectionsRepository.getById(connectionID = "id1"))

        Connection().setGuid("guid1").setId("id1").save()
        Connection().setGuid("guid2").setId("id2").save()
        Connection().setGuid("guid3").setId("id3").save()

        assertNull(ConnectionsRepository.getById(connectionID = "id111"))
        assertThat(
            ConnectionsRepository.getById(connectionID = "id1")!!.guid,
            equalTo("guid1")
        )
    }

    @Test
    @Throws(Exception::class)
    fun getByGuidTest() {
        assertNull(ConnectionsRepository.getByGuid(null))
        assertNull(ConnectionsRepository.getByGuid(""))
        assertNull(ConnectionsRepository.getByGuid("guid1"))

        Connection().setGuid("guid1").setId("id1").save()
        Connection().setGuid("guid2").setId("id2").save()
        Connection().setGuid("guid3").setId("id3").save()

        assertNull(ConnectionsRepository.getByGuid(connectionGuid = "guid111"))
        assertThat(
            ConnectionsRepository.getByGuid(connectionGuid = "guid1")!!.guid,
            equalTo("guid1")
        )
    }

    @Test
    @Throws(Exception::class)
    fun providerSaveTest() {
        Assert.assertTrue(ConnectionsRepository.isEmpty())
        Assert.assertNotNull(Connection().save())
        assertThat(ConnectionsRepository.getConnectionsCount(), equalTo(1L))
    }

    /**
     * test updateNameAndSave when connections is managed
     */
    @Test
    @Throws(Exception::class)
    fun updateNameAndSaveTestCase1() {
        val connection = Connection().setName("Demobank1").setGuid("guid1").save()!!

        ConnectionsRepository.updateNameAndSave(connection, "Demo2")

        assertThat(connection.name, equalTo("Demo2"))
    }

    /**
     * test updateNameAndSave when connections isn't managed
     */
    @Test
    @Throws(Exception::class)
    fun updateNameAndSaveTestCase2() {
        val connection = Connection().setName("Demobank1").setGuid("guid1")

        ConnectionsRepository.updateNameAndSave(connection, newName = "Demo2")

        assertThat(connection.name, equalTo("Demo2"))
    }

    @Test
    @Throws(Exception::class)
    fun fixNameAndSaveTest() {
        Connection().setGuid("guid1").setCode("demo").setName("Demo").save()
        Thread.sleep(100);
        Connection().setGuid("guid2").setCode("test").setName("Test").save()
        Thread.sleep(100);
        ConnectionsRepository.fixNameAndSave(Connection().setGuid("guid3").setCode("demo").setName("Demo"))

        val models = ConnectionsRepository.getAllConnections()

        assertThat(
            models.map { it.guid },
            equalTo(listOf("guid1", "guid2", "guid3"))
        )
        assertThat(
            models.map { it.name },
            equalTo(listOf("Demo", "Test", "Demo (2)"))
        )
    }

    @Test
    @Throws(Exception::class)
    fun getActiveConnectionsWithoutTokenTestCase1() {
        Assert.assertTrue(ConnectionsRepository.isEmpty())

        Connection().setGuid("guid1").setAccessToken("token1").setStatus(ConnectionStatus.ACTIVE).apply {
            pushToken = "pushToken"
        }.save()

        assertThat(
            ConnectionsRepository.getActiveConnectionsWithoutToken(storedPushToken = "storedPushToken").map { it.guid },
            equalTo(listOf("guid1"))
        )
    }

    @Test
    @Throws(Exception::class)
    fun getActiveConnectionsWithoutTokenTestCase2() {
        Assert.assertTrue(ConnectionsRepository.isEmpty())

        Connection().setGuid("guid1").setAccessToken("token1").setStatus(ConnectionStatus.INACTIVE).apply {
            pushToken = "pushToken"
        }.save()

        assertThat(
            ConnectionsRepository.getActiveConnectionsWithoutToken(storedPushToken = "storedPushToken").map { it.guid },
            equalTo(listOf())
        )
    }
}
