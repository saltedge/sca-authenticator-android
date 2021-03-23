# Salt Edge Applock Module

An easy-to-use and customizable passcode(e.g. 4-digit PIN) view for Android application.
Module provides next functionality:
1) lock view with passcode/biometric input used for hiding data on the current screen when app is going to foreground;
2) setup new passcode screen;
3) edit passcode screen.

### Setup Salt Edge Applock Module
1. Fork this repository
2. Import module `File/New/Import Module...` in your project
3. Add a dependency to your `build.gradle(:app)`:
```implementation project(':applock')```
4. In your `Application` class add the initialization of the module, usually this happens in the `onCreate` method:
```SEAppLock.initAppLock(applicationContext, appName, forgotActionButtonText, otpDescriptionText)```

5. You can access the stored encrypted passcode in the following way:
```PasscodeTools.getPasscode()```

6. To block the certain activity, please extend it from the `LockableActivity`.
Now every time you log in to the app, it will ask for a passcode.
You will need to override two functions: `onSelectedClearApplicationData()`, `getUnlockAppInputView()`.

7. Function `onSelectedClearApplicationData()` listens click on action button in the Forgot Passcode Screen which is shown when you click the "Forgot?" button on `unlockAppInputView`.

8. In the function `getUnlockAppInputView()` you should return the view `unlockAppInputView` that you need to place in the `.xml` file of the current activity

```xml
<com.fentury.applock.widget.security.UnlockAppInputView
        android:id="@+id/unlockAppInputView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
```

9. To avoid asking for a passcode, need to pass Intent with flag `KEY_SKIP_PIN`:
```Intent.putExtra(KEY_SKIP_PIN, true)```

10. To clear all preferences from the module use:
```SEAppLock.clearPasscodePreferences()```

11. Use the `PasscodeSetupFragment` to setup passcode in your application.

12. Use the `PasscodeEditFragment` to edit passcode in your application.

13. To listen to passcode changes, you need to implement the `PasscodeListener` interface in `activity`
where you intent to add fragment the `PasscodeSetupFragment`/`PasscodeEditFragment`.

___
Copyright © 2020 Salt Edge. https://www.saltedge.com  
