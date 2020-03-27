# Salt Edge RASP Module  

Salt Edge RASP Module - is a set of tools for checking possible breaches in application environment or application tempering.  

Checks next breaches:
 * OS is working under Root privileges
 * Current device is Emulator
 * Application can be debugged
 * Application signed with not verified signature
 * OS has installed hooking framework
  
## Prerequisites
In order to use Salt Edge RASP Module it is necessary to install the following tools on the local machine:

* [JDK 8](https://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [Android SDK](https://developer.android.com/studio/index.html)
* IDE (e.g. Android Studio)
* [Kotlin plugin installed](https://developer.android.com/kotlin/) v.1.3.+ [**optional**]
* Minimal supported Android SDK is SDK19 (Android 4 KitKat)  
   
## How to use RASP Module

1. Fork this repository
1. Import module `File/New/Import Module...` in your project
1. Setup module.
1. Build and run application on target device or emulator

### Setup Salt Edge RASP Module
Create `res/values/signatures.xml` like in example: 
Application on Java language:
```xml
    <resources xmlns:tools="http://schemas.android.com/tools">
        <string-array name="signatures" tools:ignore="InconsistentArrays">
            <item>QWERTY=</item>
        </string-array>
    </resources>
```
Where each item it is SHA hash of signing release key.  
To obtain hash string:  
1. run gradle task "signingReport"
2. convert key hash in HEX to Base64 string 
```bash
    echo 33:4E:48:84:19:50:3A:1F:63:A6:0F:F6:A1:C2:31:E5:01:38:55:2E | xxd -r -p | openssl base64
```

### Add To Application
```kotlin
    val raspFailReport = RaspChecker.collectFailsReport(applicationContext)
    if (raspFailReport.isNotEmpty()) {
        val errorMessage = "App Is Tempered:[$raspFailReport]"
        throw Exception(errorMessage)
    }
```
  
___
Copyright © 2020 Salt Edge. https://www.saltedge.com  
