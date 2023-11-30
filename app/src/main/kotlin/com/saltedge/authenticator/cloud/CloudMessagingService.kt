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
package com.saltedge.authenticator.cloud

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.saltedge.authenticator.core.api.KEY_AUTHORIZATION_ID
import com.saltedge.authenticator.core.api.KEY_CONNECTION_ID
import com.saltedge.authenticator.features.main.MainActivity
import com.saltedge.authenticator.models.repository.PreferenceRepository

class CloudMessagingService : FirebaseMessagingService() {

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
            val activityIntent = Intent(this, MainActivity::class.java)
            activityIntent.putExtra(KEY_CONNECTION_ID, connectionId)
            activityIntent.putExtra(KEY_AUTHORIZATION_ID, authorizationId)

            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE
            else PendingIntent.FLAG_UPDATE_CURRENT
            PendingIntent.getActivity(this, 0, activityIntent, flags)
        } else {
            null
        }
    }
}
