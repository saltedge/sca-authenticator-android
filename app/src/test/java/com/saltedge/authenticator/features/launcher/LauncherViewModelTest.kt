/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.launcher

import com.saltedge.android.test_tools.ViewModelTest
import com.saltedge.authenticator.features.main.MainActivity
import com.saltedge.authenticator.features.onboarding.OnboardingSetupActivity
import com.saltedge.authenticator.models.realm.RealmManagerAbs
import com.saltedge.authenticator.models.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.TestAppTools
import com.saltedge.authenticator.tools.PasscodeTools
import com.saltedge.authenticator.tools.PasscodeToolsAbs
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LauncherViewModelTest : ViewModelTest() {

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

        assertFalse(mockRealmManager.errorOccurred)
        assertNull(viewModel.onDbInitializationFail.value)
        assertNotNull(viewModel.onInitializationSuccess.value)
    }

    @Test
    @Throws(Exception::class)
    fun onOkClickTest() {
        viewModel.dbErrorCheckedByUser()

        Mockito.verify(mockRealmManager).resetError()
        assertNotNull(viewModel.closeEvent.value)
    }
}
