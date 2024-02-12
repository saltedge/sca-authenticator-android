/*
 * Copyright (c) 2019 Salt Edge Inc.
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
import com.saltedge.authenticator.app.buildVersion26orGreater

const val CHANNEL_NAME = "SaltEdge Authenticator Channel"
const val NOTIFICATION_ID = 201

val Context.notificationChannelId: String
    get() = this.getString(R.string.default_notification_channel_id)

/**
 * Create notification channel (from SDK26) with specific settings
 *
 * @receiver context - application context
 */
@SuppressLint("NewApi")
fun Context.registerNotificationChannels() {
    if (buildVersion26orGreater) {
        val notificationChannel: NotificationChannel = NotificationChannel(
            this.notificationChannelId,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
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
    val notificationBuilder = NotificationCompat.Builder(this, this.notificationChannelId)
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
