/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2020 Salt Edge Inc.
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
package com.saltedge.authenticator.features.launcher

import com.saltedge.authenticator.features.main.MainActivity
import com.saltedge.authenticator.features.onboarding.OnboardingSetupActivity
import com.saltedge.authenticator.model.realm.RealmManagerAbs
import com.saltedge.authenticator.model.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.testTools.TestAppTools
import com.saltedge.authenticator.tool.secure.PasscodeTools
import com.saltedge.authenticator.tool.secure.PasscodeToolsAbs
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LauncherViewModelTest {

    private lateinit var viewModel: LauncherViewModel
    private val mockPreferenceRepository = Mockito.mock(PreferenceRepositoryAbs::class.java)
    private val mockPasscodeTools = Mockito.mock(PasscodeToolsAbs::class.java)
    private val mockRealmManager = Mockito.mock(RealmManagerAbs::class.java)

    @Before
    fun setUp() {
        viewModel = LauncherViewModel(
            appContext = TestAppTools.applicationContext,
            preferenceRepository = mockPreferenceRepository,
            passcodeTools = mockPasscodeTools,
            realmManager = mockRealmManager
        )
    }

    /**
     * When passcode is not exist, and the getNextActivityClass() was called,
     * then it will start OnboardingSetupActivity
     *
     * @see OnboardingSetupActivity
     */
    @Test
    @Throws(Exception::class)
    fun getNextActivityClassTestCase1() {
        Mockito.doReturn(false).`when`(mockPreferenceRepository).passcodeExist()

        assertEquals(viewModel.getNextActivityClass(), OnboardingSetupActivity::class.java)
    }

    /**
     * When passcode is exist, and the getNextActivityClass() was called,
     * then it will start MainActivity
     *
     * @see MainActivity
     */
    @Test
    @Throws(Exception::class)
    fun getNextActivityClassTestCase2() {
        Mockito.doReturn(true).`when`(mockPreferenceRepository).passcodeExist()

        assertEquals(viewModel.getNextActivityClass(), MainActivity::class.java)
    }

    /**
     * When error of realm manager was occurred, and the onLifeCycleResume() was called,
     * then it will init realm and it will change the onDbInitializationFail value
     */
    @Test
    @Throws(Exception::class)
    fun onLifeCycleResumeTestCase1() {
        Mockito.doReturn(true).`when`(mockRealmManager).errorOccurred

        viewModel.onLifeCycleResume()

        Mockito.verify(mockRealmManager).initRealm(TestAppTools.applicationContext)
        assertTrue(mockRealmManager.errorOccurred)
        assertNotNull(viewModel.onDbInitializationFail.value)
        assertNull(viewModel.onInitializationSuccess.value)
    }

    /**
     * When passcode is not exist, and the onLifeCycleResume() was called,
     * then passcode key it will replace on new
     *
     * @see PasscodeTools
     */
    @Test
    @Throws(Exception::class)
    fun onLifeCycleResumeTestCase2() {
        Mockito.doReturn(false).`when`(mockPreferenceRepository).passcodeExist()

        viewModel.onLifeCycleResume()

        Mockito.verify(mockPasscodeTools).replacePasscodeKey(TestAppTools.applicationContext)

        Mockito.doReturn(true).`when`(mockPreferenceRepository).passcodeExist()
        viewModel.onLifeCycleResume()

        Mockito.verifyNoMoreInteractions(mockPasscodeTools)
    }

    /**
     * When error of realm manager wasn't occurred, and the onLifeCycleResume() was called,
     * then it will init realm and it will change the onInitializationSuccess value
     */
    @Test
    @Throws(Exception::class)
    fun onLifeCycleResumeTestCase3() {
        viewModel.onLifeCycleResume()

        Mockito.verify(mockRealmManager).initRealm(TestAppTools.applicationContext)
        assertFalse(mockRealmManager.errorOccurred)
        assertNull(viewModel.onDbInitializationFail.value)
        assertNotNull(viewModel.onInitializationSuccess.value)
    }

    @Test
    @Throws(Exception::class)
    fun onOkClickTest() {
        viewModel.onOkClick()

        Mockito.verify(mockRealmManager).resetError()
        assertNotNull(viewModel.buttonClickEvent.value)
    }
}
