/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.cloud

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.saltedge.authenticator.app.AuthenticatorApplication
import com.saltedge.authenticator.core.api.KEY_AUTHORIZATION_ID
import com.saltedge.authenticator.core.api.KEY_CONNECTION_ID
import com.saltedge.authenticator.features.main.MainActivity
import com.saltedge.authenticator.models.repository.PreferenceRepository
import timber.log.Timber
import javax.inject.Inject

class CloudMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var pushTokenUpdater: PushTokenUpdater

    override fun onCreate() {
        super.onCreate()
        (application as AuthenticatorApplication).appComponent.inject(this)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        this.showAuthNotification(
            remoteMessage.notification?.title,
            remoteMessage.notification?.body,
            createPendingIntent(
                remoteMessage.data[KEY_CONNECTION_ID],
                remoteMessage.data[KEY_AUTHORIZATION_ID]
            )
        )
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        saveToken(token)
        pushTokenUpdater.updatePushToken()
    }

    /**
     * Save token in preference repository
     *
     * @param token - token of firebase messaging service
     * @see PreferenceRepository
     */
    private fun saveToken(token: String) {
        PreferenceRepository.cloudMessagingToken = token
    }

    /**
     * Create intent to show Activity on notification click.
     * If Main Activity is active, then pending intent will lead to MainActivity else to LauncherActivity
     *
     * @param connectionId - id of Connection
     * @param authorizationId - id of Authorization
     * @return pending intent with method getActivity
     * @see PendingIntent.getActivity
     */
    private fun createPendingIntent(
        connectionId: String?,
        authorizationId: String?
    ): PendingIntent? {
        return if (connectionId?.isNotEmpty() == true && authorizationId?.isNotEmpty() == true) {
            val requestCode: Int = convertToRequestCodeNumber(authorizationId)

            val activityIntent = Intent(this, MainActivity::class.java)
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            activityIntent.putExtra(KEY_CONNECTION_ID, connectionId)
            activityIntent.putExtra(KEY_AUTHORIZATION_ID, authorizationId)

            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                PendingIntent.FLAG_IMMUTABLE
            else
                PendingIntent.FLAG_UPDATE_CURRENT

            PendingIntent.getActivity(this, requestCode, activityIntent, flags)
        } else {
            null
        }
    }

    private fun convertToRequestCodeNumber(code: String?): Int {
        return try {
            code?.toInt() ?: 0
        } catch (exception: NumberFormatException) {
            Timber.e(exception)
            0
        }
    }
}
