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
package com.saltedge.authenticator.unitTests.widget.radialprogress

import android.graphics.RectF
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.saltedge.authenticator.R
import com.saltedge.authenticator.testTools.TestTools
import com.saltedge.authenticator.widget.radialprogress.RadialProgressPresenter
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RadialProgressPresenterTest {

    @Test
    @Throws(Exception::class)
    fun constructorTest() {
        val presenter = RadialProgressPresenter()

        assertThat(presenter.maxProgress, equalTo(100))
        assertThat(presenter.remainedProgress, equalTo(0))
        assertThat(presenter.centerX, equalTo(0f))
        assertThat(presenter.centerY, equalTo(0f))
        assertThat(presenter.backgroundCircleRadius, equalTo(0f))
        assertThat(presenter.progressArcRectF, equalTo(RectF()))
    }

    @Test
    @Throws(Exception::class)
    fun setterTest() {
        val presenter = RadialProgressPresenter()

        assertThat(presenter.maxProgress, equalTo(100))
        assertThat(presenter.remainedProgress, equalTo(0))

        presenter.maxProgress = -1

        assertThat(presenter.maxProgress, equalTo(0))

        presenter.maxProgress = 200

        assertThat(presenter.maxProgress, equalTo(200))

        presenter.remainedProgress = -1

        assertThat(presenter.remainedProgress, equalTo(0))

        presenter.remainedProgress = 10

        assertThat(presenter.remainedProgress, equalTo(10))

        presenter.remainedProgress = 201

        assertThat(presenter.remainedProgress, equalTo(200))
    }

    @Test
    @Throws(Exception::class)
    fun onViewSizeChangedTest() {
        val basePadding = TestTools.applicationContext.resources.getDimension(R.dimen.dp_2)
        val presenter = RadialProgressPresenter()
        presenter.basePadding = basePadding

        presenter.onViewSizeChanged(0, 0)

        assertThat(presenter.basePadding, equalTo(basePadding))
        assertThat(presenter.centerX, equalTo(0f))
        assertThat(presenter.centerY, equalTo(0f))
        assertThat(presenter.backgroundCircleRadius, equalTo(0f))
        assertThat(presenter.progressArcRectF, equalTo(RectF()))

        val testSize = TestTools.applicationContext.resources.getDimension(R.dimen.dp_24)
        presenter.onViewSizeChanged(testSize.toInt(), testSize.toInt())
        val progressArcRadius = presenter.backgroundCircleRadius - basePadding

        assertThat(presenter.centerX, equalTo(testSize / 2f))
        assertThat(presenter.centerY, equalTo(testSize / 2f))
        assertThat(presenter.backgroundCircleRadius, equalTo(testSize / 2f - basePadding))
        assertThat(
            presenter.progressArcRectF,
            equalTo(
                RectF(
                    testSize / 2f - progressArcRadius,
                    testSize / 2f - progressArcRadius,
                    testSize / 2f + progressArcRadius,
                    testSize / 2f + progressArcRadius
                )
            )
        )
    }

    @Test
    @Throws(Exception::class)
    fun calculateSweepAngleTest() {
        val presenter = RadialProgressPresenter()

        presenter.maxProgress = 0
        presenter.remainedProgress = 0

        assertThat(presenter.maxProgress, equalTo(0))
        assertThat(presenter.remainedProgress, equalTo(0))
        assertThat(presenter.calculateSweepAngle(), equalTo(0f))

        presenter.maxProgress = 360
        presenter.remainedProgress = 0

        assertThat(presenter.maxProgress, equalTo(360))
        assertThat(presenter.remainedProgress, equalTo(0))
        assertThat(presenter.calculateSweepAngle(), equalTo(0f))

        presenter.remainedProgress = 90

        assertThat(presenter.remainedProgress, equalTo(90))
        assertThat(presenter.calculateSweepAngle(), equalTo(90f))

        presenter.remainedProgress = 270

        assertThat(presenter.remainedProgress, equalTo(270))
        assertThat(presenter.calculateSweepAngle(), equalTo(270f))
    }

    @Test
    @Throws(Exception::class)
    fun calculateStartAngleTest() {
        val presenter = RadialProgressPresenter()

        presenter.maxProgress = 0
        presenter.remainedProgress = 0

        assertThat(presenter.maxProgress, equalTo(0))
        assertThat(presenter.remainedProgress, equalTo(0))
        assertThat(presenter.calculateStartAngle(), equalTo(270f))

        presenter.maxProgress = 360
        presenter.remainedProgress = 0

        assertThat(presenter.maxProgress, equalTo(360))
        assertThat(presenter.remainedProgress, equalTo(0))
        assertThat(presenter.calculateStartAngle(), equalTo(270f))

        presenter.remainedProgress = 90

        assertThat(presenter.remainedProgress, equalTo(90))
        assertThat(presenter.calculateStartAngle(), equalTo(180f))

        presenter.remainedProgress = 270

        assertThat(presenter.remainedProgress, equalTo(270))
        assertThat(presenter.calculateStartAngle(), equalTo(0f))
    }
}
