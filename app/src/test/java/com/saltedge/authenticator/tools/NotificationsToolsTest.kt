/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.tools

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import com.saltedge.authenticator.cloud.CHANNEL_NAME
import com.saltedge.authenticator.cloud.registerNotificationChannels
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito

class NotificationsToolsTest {

    private val mockContext = Mockito.mock(Context::class.java)
    private val notificationManager = Mockito.mock(NotificationManager::class.java)

    @RequiresApi(Build.VERSION_CODES.O)
    @Test
    @Throws(Exception::class)
    fun registerNotificationChannelsTest() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Mockito.`when`(mockContext.getSystemService(Context.NOTIFICATION_SERVICE)).thenReturn(
                notificationManager
            )
            val captor: ArgumentCaptor<NotificationChannel> =
                ArgumentCaptor.forClass(NotificationChannel::class.java)
            mockContext.registerNotificationChannels()

            Mockito.verify(notificationManager).createNotificationChannel(captor.capture())

            val argument: NotificationChannel = captor.value

            assertThat(argument.id, equalTo("com.saltedge.authenticator.notifications"))
            assertThat(argument.name.toString(), equalTo(CHANNEL_NAME))
            assertThat(argument.importance, equalTo(NotificationManagerCompat.IMPORTANCE_HIGH))
            assertThat(argument.lightColor, equalTo(Color.BLUE))
            assertThat(argument.vibrationPattern, equalTo(longArrayOf(0, 150, 100, 150)))
            assertThat(argument.lockscreenVisibility, equalTo(Notification.VISIBILITY_PRIVATE))
        } else {
            mockContext.registerNotificationChannels() // Dummy call
        }
    }
}
