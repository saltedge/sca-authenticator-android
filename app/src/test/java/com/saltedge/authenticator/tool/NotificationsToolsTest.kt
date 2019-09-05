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
package com.saltedge.authenticator.tool

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
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

            assertThat(argument.id, equalTo(CHANNEL_ID))
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
