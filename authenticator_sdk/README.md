# Salt Edge Authenticator Android SDK  

Salt Edge Authenticator Android SDK - is a set of tools for implementing connection to Salt Edge Authenticator API of Service Provider (e.g Bank) System, that implements Strong Customer Authentication/Dynamic Linking process.  

You can find source code of Authenticator Identity Service here: for [Authenticator Identity Service](identity_service_repo). Read Wiki docs about [Authenticator Identity Service](wiki) API and workflow.    

## Content
 * [Prerequisites](#prerequisites)
 * [How to add SDK to project](#how-to-add-sdk-to-project)
 * [Data models](#data-models)
 * [Features](#how-to-use)
  
## Prerequisites
In order to use Authenticator SDK it is necessary to install the following tools on the local machine:

* [JDK 8](https://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [Android SDK](https://developer.android.com/studio/index.html)
* IDE (e.g. Android Studio)
* [Kotlin plugin installed](https://developer.android.com/kotlin/) v.1.3.+ [**optional**]
* Minimal supported Android SDK is SDK21 (Android 5 Lollipop)  
   
   
## How to add SDK to project

### Add as project module

1. Fork this repository
1. Import module `File/New/Import Module...` in your project
1. Build and run application on target device or emulator
  
### Add as dependency:

1. Add maven repository to application's build.gradle
```groovy
    repositories {
        maven {
            url 'https://raw.github.com/saltedge/sca-authenticator-android/master/maven-repo/'
        }
    }
```
1. Add SDK dependency to application build.gradle
```groovy
    implementation ('com.saltedge.authenticator.sdk:authenticator_sdk:1.1.1@aar') {
        transitive = true
    }
```  

---
## Data models  
  
Authenticator SDK provide next main data models:    
 * [Provider Data](#provider-data-model)
 * [Connection](#connection-model)
 * [ConnectionAndKey](#connectionandkey-model)
 * [Encrypted Authorization model](#encrypted-authorization-model)
 * [Decrypted Authorization model](#decrypted-authorization-model)

### Provider Data model
`ProviderData` class it is an entity which contains Provider's info.  

Fields:  
 * `connect_url`   **[string]** - base URL of the Identity Service. Required to build Authenticator API requests.
 * `code`          **[string]** - unique code of the Service Provider
 * `name`          **[string]** - human readable name of the Service Provider
 * `logo_url`      **[string]** - URL of the Service Provider's logo asset
 * `support_email` **[string]** - email address Customer Support service of Service Provider
 * `version`       **[string]** - version number of Authenticator API.
  
### Connection model
Application should create a class which implements `ConnectionAbs` interface for storing data required to access API. Should be stored in the persistent storage (e.g. database).  

Fields:  
 * `guid`        **[string]** - alias to RSA keypair in Keystore
 * `id`          **[string]** - unique id received from Authenticator API
 * `name`        **[string]** - provider's name from ProviderData
 * `code`        **[string]** - provider's code
 * `logoUrl`     **[string]** - provider's logo url. May be empty
 * `connectUrl`  **[string]** - base url of Authenticator API
 * `accessToken` **[string]** - access token for accessing Authenticator API resources
 * `status`      **[string]** - connection Status (ACTIVE or INACTIVE)
 
 ### ConnectionAndKey model
`ConnectionAndKey` it is often used wrapper for Connection and related PrivateKey.
 
 ### Encrypted model (Authorization or Consent)
`Encrypted Authorization` contains encrypted `Authorization` fields.  
`Authorization` it is an entity which describes pending action which require user confirmation.
  
 - `id` **[string]** - an unique code of authorization action  
 - `connection_id` **[string]** - an unique ID of Mobile Client (Service Connection). Used to decrypt models in the mobile application
 - `iv` **[string]** - an initialization vector of encryption algorithm, this string is encrypted with public key linked to mobile client
 - `key` **[string]** - a secure key of encryption algorithm, this string is encrypted with public key linked to mobile client
 - `algorithm` **[string]** - an encryption algorithm and block mode type
 - `data` **[string]** - an encrypted authorization payload with algorithm mentioned above
 
 ### Decrypted Authorization model
 `Authorization` it is an entity which describes pending action which require user confirmation.  
 
 * `id` **[string]** - a unique id of authorization action
 * `connection_id` **[string]** - a unique ID of Connection. Used to decrypt models in the mobile application
 * `title` **[string]** - a human-readable title of authorization action
 * `description` **[string]** - a human-readable description of authorization action
 * `authorization_code` **[string]** - a unique code for each operation (e.g. payment transaction), specific to the attributes of operation, must be used once
 * `created_at` **[datetime]** - time when the authorization was created
 * `expires_at` **[datetime]** - time when the authorization should expire
  
  
---
## How to use  
  
Authenticator SDK provide next features:  
* [Initialize SDK](#initialize-sdk)
* [Enrollment](#link-to-sca-service)
* [Remove connection (Remove Bank)](#remove-connection)
* [Fetch authorizations list](#get-authorizations-list)
* [Poll authorizations list](#poll-authorizations-list)
* [Fetch authorization by ID](#get-authorization-by-id)
* [Poll authorization by id](#poll-authorization-by-id)
* [Confirm authorization](#confirm-authorization)
* [Deny authorization](#deny-authorization)
* [Send Instant Action](#send-instant-action)

### Initialize SDK
Authenticator requires initialization.  
Application on Kotlin language:
```kotlin
    AuthenticatorApiManager.initializeSdk(applicationContext)
```
  
Application on Java language:
```java
    AuthenticatorApiManager.INSTANCE.initializeSdk(applicationContext)
```
  
### Link to SCA Service

1. Fetch [Service Provider info](#provider-data-model) from configuration url (provides all required for linking information).  
_This step can be skipped if application already knows service configuration._ 

```kotlin
    authenticatorApiManager.getProviderConfigurationData(providerConfigurationUrl, resultCallback = object : FetchProviderConfigurationDataResult {
      override fun fetchProviderConfigurationDataResult(providerData: ProviderData?, error: ApiErrorData?) {
        // process result or error
      }
    })
```  
    success result can be validated:
```kotlin
    providerData.isValid()
```  
    Fetching of provider data is optional when application is destinated for linking with single Service Provider and application already has all required Service Provider info.

2. Create RSA key pair and `Connection` model  
   Key pair should have alias equal to `connection.guid`.
```kotlin
    KeyStoreManager.createOrReplaceRsaKeyPair(connection.guid)
```

3. Post connection data and receive ConnectionCreateListener  
```kotlin
    AuthenticatorApiManager.createConnectionRequest(
            appContext,
            conenction,
            firebaseCloudMessagingToken,
            connectQueryParam,
            object : ConnectionCreateListener() {
                override fun onConnectionCreateSuccess(response: CreateConnectionData) {
                    // process success response
                    // open response.connectUrl in WebView or get response.accessToken if present
                }

                override fun onConnectionCreateFailure(apiErrorData: ApiErrorData) {
                    // handle error response
                }
            }
    )
```  
    If authentication is required then response will contains `connect_url` which should be used for opening authentication page in WebView. If authentication is not required then get access token from response.  
    
4. Processing success result (*This step can be skipped if Customer is already authenticated*).  

Open url:  
```kotlin
  val webViewClient = ConnectWebClient(contract = object : ConnectWebClientContract { })
  webView.webViewClient = webViewClient
  webView.loadUrl(authentication_url)
```  
    
and wait for access token:  
```kotlin
  private val webViewClient = ConnectWebClient(contract = object : ConnectWebClientContract {
      override fun webAuthFinishSuccess(id: ConnectionID, accessToken: Token) {
          // save access token  
      }

      override fun webAuthFinishError(errorClass: String, errorMessage: String?) {
          // handle error result and remove previously created key pairs from KeyStore
      }
  })
```   
   
   
5. Set `accessToken` to `Connection` entity and save `Connection` entity to persistent storage (e.g. Realm, SQLite).  
  
That's all, you have connection to Service Provider.


### Remove Connection
In some cases application may want to destroy current linking.  

1. Send revoke request
```kotlin
    AuthenticatorApiManager.revokeConnections(
        connectionsAndKeys = connectionsAndKeysToRevoke, 
        resultCallback = object : ConnectionsRevokeListener {
            override fun onConnectionsRevokeResult(result: List<Token>, 
                                                   errors: List<ApiErrorData>) {                   
                //process errors and success results 
            } 
        }
    )
```  
  Where  `connectionsAndKeysToRevoke` - List<ConnectionAndKey>. 
  ConnectionAndKey - it is a model with pair of params: Connection and related PrivateKey

2. Delete connections from persistent storage

3. Delete related key pairs from Android Keystore
```kotlin
    KeyStoreManager.deleteKeyPairs(connectionGuids)
```

### Get Authorizations list
To show pending Authorizations app should request them and decrypt the result.  

1. Send request
```kotlin
    AuthenticatorApiManager.getAuthorizations(
        connectionsAndKeys = connectionsAndKeysToGet, 
        resultCallback = object : FetchEncryptedDataListener {
               override fun onFetchEncryptedDataListener(
                   result: List<EncryptedAuthorizationData>, 
                   errors: List<ApiErrorData>
               ) {                   
                   //process errors or process encrypted authorizations result 
               } 
        }
    )
```
2. Decrypt received encrypted authorization data. For decrypt should be used related PrivateKey 
(encryptedData.connectionId -> Connection.guid -> PrivateKey)
```kotlin
    CryptoTools.decryptAuthorizationData(encryptedData, rsaPrivateKey)
```
3. Show decrypted Authorizations list to user
  
### Poll authorizations list

For periodically fetch of authorizations list use AuthorizationsPollingService.  
Poll period is set to 2 seconds. For preventing memory leak application should start/stop polling depending on application components lifecycle (e.g. Activity's onStart() and onStop())  

1. Create polling service
```kotlin
    pollingService = AuthenticatorApiManager.createAuthorizationsPollingService()
```
2. Start polling service
```kotlin
    pollingService.contract = object : FetchAuthorizationsContract { }
    pollingService.start()
```
3. Stop polling service
```kotlin
    pollingService.contract = null
    pollingService.stop()
```

### Get authorization by ID

1. Send request
```kotlin
    AuthenticatorApiManager.getAuthorization(
        connectionAndKey = connectionAndKeyToGet,
        authorizationId = requiredAuthorizationId,
        resultCallback = object : FetchAuthorizationListener {
           override fun fetchAuthorizationResult(
               result: EncryptedAuthorizationData?, 
               error: ApiErrorData?
           ) {                   
                //process error or process encrypted authorization result
            } 
        }
    )
```

2. Decrypt authorization data. For decrypt use related PrivateKey 
(encryptedData.connectionId -> Connection.guid -> PrivateKey)
    ```kotlin
        CryptoTools.decryptAuthorizationData(encryptedData, rsaPrivateKey)
    ```

3. Show decrypted Authorization to user

### Poll authorization by ID

For periodically fetch of single authorization use SingleAuthorizationPollingService.  
Poll period is set to 2 seconds. For preventing memory leak application should start/stop polling depending on application components lifecycle (e.g. Activity's onStart() and onStop())  

1. Create polling service
```kotlin
    pollingService = AuthenticatorApiManager.createSingleAuthorizationPollingService()
```

2. Start service
```kotlin
    pollingService.contract = object : FetchAuthorizationContract { }
    pollingService.start()
```

3. Stop service
```kotlin
    pollingService.contract = null
    pollingService.stop()
```

### Confirm authorization  
Each pending Authorization can be confirmed. Application can ask user to identify by entering PIN code or Fingerprint.  

```kotlin
    AuthenticatorApiManager.confirmAuthorization(
        connectionAndKey = connectionAndKeyForAuthorizationConfirm,
        authorizationId = confirmedAuthorizationId,
        authorizationCode = authorizationCode,
        resultCallback = object : ConfirmAuthorizationListener {
            override fun onConfirmDenyFailure(error: ApiErrorData) {
                //process request error
            }
            override fun onConfirmDenySuccess(result: ConfirmDenyResultData) {
                //process confirm authorization request result
                //ConfirmDenyResultData(success: Boolean, authorizationId: String)
            }
        }
    )
```

### Deny authorization  
Each pending Authorization can be denyed. Application can ask user to identify by entering PIN code or Fingerprint.   
```kotlin
    AuthenticatorApiManager.denyAuthorization(
        connectionAndKey = connectionAndKeyForAuthorizationDeny,
        authorizationId = deniedAuthorizationId,
        authorizationCode = authorizationCode,
        resultCallback = object : ConfirmAuthorizationListener {
            override fun onConfirmDenyFailure(error: ApiErrorData) {
                //process request error
            }
            override fun onConfirmDenySuccess(result: ConfirmDenyResultData) {
                //process deny authorization request result 
                //ConfirmDenyResultData(success: Boolean, authorizationId: String)
            }
        }
    )
```
### Send Instant Action  
Instant Action feature is designated to authenticate an action of Service Provider (e.g. Sign-In, Payment Order). 
Each Instant Action has unique code `actionUUID`. After receiving of `actionUUID`, Authenticator app should submit to selected by user Connection:
```kotlin
    AuthenticatorApiManager.sendAction(
        actionUUID,
        connectionAndKey,
        resultCallback = object : ActionSubmitListener {
            override fun onActionInitSuccess(response: SubmitActionData) {
                //process success result
            }
            override fun onActionInitFailure(error: ApiErrorData) {
                //process request error
            }
        }
    )
```
On success, Authenticator app receives `SubmitActionData` which has optional fields `connectionId` and `authorizationId` (if is reqiored additional confirmation).

### Get Consents list
To show active Consents app should request them and decrypt the result.  

1. Send request
```kotlin
    AuthenticatorApiManager.getConsents(
        connectionsAndKeys = connectionsAndKeysToGet, 
        resultCallback = object : FetchEncryptedDataListener {
            override fun onFetchEncryptedDataListener(
                result: List<EncryptedAuthorizationData>, 
                errors: List<ApiErrorData>
            ) {                   
                //process errors or process encrypted consents result 
            } 
        }
    )
```

2. Decrypt received encrypted authorization data. For decrypt should be used related PrivateKey 
(encryptedData.connectionId -> Connection.guid -> PrivateKey)
```kotlin
    CryptoTools.decryptConsentData(encryptedData, rsaPrivateKey)
```

### Revoke Consent  

Send request
```kotlin
    AuthenticatorApiManager.revokeConsent(
        consentId  = "consentId",   
        connectionAndKey = connectionAndKeyForConsent,  
        resultCallback = object : ConsentRevokeListener {
            override fun onConsentRevokeFailure(error: ApiErrorData) {
                //process errors
            }
            override fun onConsentRevokeSuccess(result: ConsentRevokeResponseData) {
                //process success result
            } 
        }
    )
```
  
  
___
Copyright © 2019 - 2020 Salt Edge. https://www.saltedge.com  
