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

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.core.app.NotificationCompat
import com.saltedge.authenticator.R
import com.saltedge.authenticator.tools.buildVersion26OrGreater

const val CHANNEL_ID = "com.saltedge.authenticator.notifications"
const val CHANNEL_NAME = "SaltEdge Authenticator Channel"
const val NOTIFICATION_ID = 201

/**
 * Create notification channel (from SDK26) with specific settings
 *
 * @receiver context - application context
 */
@SuppressLint("NewApi")
fun Context.registerNotificationChannels() {
    if (buildVersion26OrGreater) {
        val notificationChannel: NotificationChannel = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
        ).apply {

            // Configure the notification channel.
            enableLights(true)
            lightColor = Color.BLUE
            enableVibration(true)
            vibrationPattern = setVibrationPattern()
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        }

        this.getNotificationManager().createNotificationChannel(notificationChannel)
    }
}

/**
 * Show auth notification with specific settings
 *
 * @param notificationTitle - notification title
 * @param notificationBody - notification description
 * @param activityIntent - pending intent
 * @receiver context - application context
 */
fun Context.showAuthNotification(
    notificationTitle: String?,
    notificationBody: String?,
    activityIntent: PendingIntent?
) {
    val contentTitle = notificationTitle ?: this.getString(R.string.authorizations_notification_title)
    val contentText = notificationBody ?: this.getString(R.string.authorizations_notification_description)
    val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_logo_app_notifications)
        .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.mipmap.ic_launcher))
        .setContentTitle(contentTitle)
        .setContentText(contentText)
        .setAutoCancel(true)
        .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE)
        .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
        .apply { activityIntent?.let { setContentIntent(it) } }
    this.getNotificationManager().notify(NOTIFICATION_ID, notificationBuilder.build())
}

/**
 * Clear all notifications
 *
 * @receiver context - application context
 */
fun Context.clearNotifications() {
    this.applicationContext.getNotificationManager().cancelAll()
}

/**
 * Set vibration pattern with specific options
 *
 * @return long array - vibration notifications
 */
private fun setVibrationPattern(): LongArray = longArrayOf(0, 150, 100, 150)

/**
 * Get notification manager
 *
 * @receiver context - application context
 * @return notification manager
 */
private fun Context.getNotificationManager(): NotificationManager =
    this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
