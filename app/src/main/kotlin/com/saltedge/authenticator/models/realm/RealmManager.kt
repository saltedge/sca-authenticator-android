/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.models.realm

import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.saltedge.authenticator.app.AppTools
import com.saltedge.authenticator.core.tools.createRandomGuid
import com.saltedge.authenticator.models.repository.PreferenceRepository
import io.realm.Realm
import io.realm.RealmConfiguration
import timber.log.Timber

const val DB_SCHEMA_VERSION = 5L

interface RealmManagerAbs {
    val initialized: Boolean
    val errorOccurred: Boolean
    fun initRealm(context: Context)
    fun getDefaultInstance(): Realm
    fun resetError()
}

object RealmManager : RealmManagerAbs {

    private const val DB_NAME = "authenticator_app.realm"
    private var initErrorOccurred: Boolean? = null
    override val initialized: Boolean
        get() = initErrorOccurred != null
    override val errorOccurred: Boolean
        get() = initErrorOccurred == true

    override fun initRealm(context: Context) {
        if (!initialized) {
            Realm.init(context)
            val builder = RealmConfiguration.Builder()
                .schemaVersion(DB_SCHEMA_VERSION)
                .name(DB_NAME)
            builder.migration(runMigrations())
            if (AppTools.isTestsSuite(context)) builder.inMemory()
            else builder.encryptionKey(getOrCreateDatabaseKey())
            Realm.setDefaultConfiguration(builder.build())
            checkDbInstance()
        }
    }

    override fun getDefaultInstance(): Realm = Realm.getDefaultInstance()

    override fun resetError() {
        initErrorOccurred = null
    }

    /**
     * Get or create new database key
     *
     * @return database key
     */
    private fun getOrCreateDatabaseKey(): ByteArray {
        if (PreferenceRepository.dbKey.isEmpty()) PreferenceRepository.dbKey = createRandomGuid()
        return PreferenceRepository.dbKey.toByteArray()
    }

    private fun checkDbInstance() {
        try {
            initErrorOccurred = false
            Realm.getDefaultInstance()
        } catch (e: Exception) {
            Timber.e(e)
            initErrorOccurred = true
        }
    }
}

fun FragmentActivity.initRealmDatabase() {
    if (!RealmManager.initialized) RealmManager.initRealm(context = this)
}
