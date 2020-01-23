# Salt Edge Authenticator Android SDK  

Salt Edge Authenticator Android SDK - is a set of tools for implementing connection to Salt Edge Authenticator API of Service Provider (e.g Bank) System, that implements Strong Customer Authentication/Dynamic Linking process.  

You can find source code of Authenticator Identity Service here: for [Authenticator Identity Service](identity_service_repo).   
## How Salt Edge Authenticator works

Read Wiki docs about [Authenticator Identity Service](wiki) API and workflow.

## Prerequisites
In order to use Authenticator SDK it is necessary to install the following tools on the local machine:

* JDK 8
* Android SDK
* IDE (e.g. Android Studio)

## How to add SDK module to your project

> Don't forget that module requires Kotlin v.1.3.+ plugin installed 
and minimal supported Android SDK is SDK21 (Android 5 Lollipop)  

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
implementation ('com.saltedge.authenticator.sdk:authenticator_sdk:1.0.0@aar') {
    transitive = true
}
```  
    
## Data models  
  
Authenticator SDK provide next main data models:    
 * [Provider Data](#provider-data-model)
 * [Connection](#connection-model)
 * [ConnectionAndKey](#connectionandkey-model)
 * [Encrypted Authorization model](#encrypted-authorization-model)
 * [Decrypted Authorization model](#decrypted-authorization-model)

### Provider Data model
`Provider Data` it is an entity which contains Provider's info.  

Fields:  
 * `connect_url`   **[string]** - base URL of the Identity Service. Required to build Authenticator API requests.
 * `code`          **[string]** - unique code of the Service Provider
 * `name`          **[string]** - human readable name of the Service Provider
 * `logo_url`      **[string]** - URL of the Service Provider's logo asset
 * `support_email` **[string]** - email address Customer Support service of Service Provider
 * `version`       **[string]** - version number of Authenticator API.
  
### Connection model
`Connection` it is an entity which stores Provider's info and Link to API info. Should be stored in the persistent storage (e.g. database).  SDK provides `ConnectionAbs` interface and application should have class which implements `ConnectionAbs`.

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
 
 ### Encrypted Authorization model
`Authorization` it is an entity which describes pending event which require user confirmation.
`Encrypted Authorization` contains encrypted `Authorization` fields.  

 - `id` **[string]** - an unique code of authorization action  
 - `connection_id` **[string]** - an unique ID of Mobile Client (Service Connection). Used to decrypt models in the mobile application
 - `iv` **[string]** - an initialization vector of encryption algorithm, this string is encrypted with public key linked to mobile client
 - `key` **[string]** - a secure key of encryption algorithm, this string is encrypted with public key linked to mobile client
 - `algorithm` **[string]** - an encryption algorithm and block mode type
 - `data` **[string]** - an encrypted authorization payload with algorithm mentioned above
 
 ### Decrypted Authorization model
 `Authorization` it is an entity which describes pending event which require user confirmation.  
 
 * `id` **[string]** - a unique id of authorization action
 * `connection_id` **[string]** - a unique ID of Connection. Used to decrypt models in the mobile application
 * `title` **[string]** - a human-readable title of authorization action
 * `description` **[string]** - a human-readable description of authorization action
 * `authorization_code` **[string]** - a unique code for each operation (e.g. payment transaction), specific to the attributes of operation, must be used once
 * `created_at` **[datetime]** - time when the authorization was created
 * `expires_at` **[datetime]** - time when the authorization should expire
  
  
## How to use  
  
Authenticator SDK provide next features:  
* [Create connection (Link Bank flow)](#create-connection)
* [Remove connection (Remove Bank)](#remove-connection)
* [Fetch authorizations list](#get-authorizations-list)
* [Poll authorizations list](#poll-authorizations-list)
* [Fetch authorization by ID](#get-authorization-by-id)
* [Poll authorization by id](#poll-authorization-by-id)
* [Confirm authorization](#confirm-authorization)
* [Deny authorization](#deny-authorization)

### Link to Identity Service

1. Fetch [Service Provider info](#provider-data-model) from configuration url (provides all required for linking information). 
    ```kotlin
        authenticatorApiManager.getProviderData(configurationUrl, resultCallback = object : FetchProviderDataResult {
          override fun fetchProviderResult(result: ProviderData?, error: ApiErrorData?) {
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

3. Post connection data and receive ConnectionCreateResult  
    ```kotlin
        AuthenticatorApiManager.createConnectionRequest(
                baseUrl = connection.connectUrl,
                publicKey = publicKeyAsPemEncodedString,
                pushToken = firebaseCloudMessagingToken,
                resultCallback = object : ConnectionInitResult() {
                    override fun onConnectionInitSuccess(response: ConnectionCreateResult) {
                        // process success response
                        // open response.connect_url on WebView if required
                    }
    
                    override fun onConnectionInitFailure(error: ApiErrorData) {
                        // handle error response
                    }
                }
        )
    ```  
    If it is required authentication on Service Provider side then response will contains `connect_url` which should be used for opening authentication page in WebView.  
4. Processing success result
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
or if authentication is not required extract access token from `connect_url`.
   
5. Set `accessToken` to `Connection` and save `Connection` to persistent storage (e.g. Realm, SQLite).  
  
That's all, you have connection to Service Provider.


### Remove Connection
In some cases application may want to destroy current linking.  

1. Send revoke request
    ```kotlin
        AuthenticatorApiManager.revokeConnections(
            connectionsAndKeys = connectionsAndKeysToRevoke, 
            resultCallback = object : ConnectionsRevokeResult { }
        )
    ```
    Where  `connectionsAndKeysToRevoke` - List<ConnectionAndKey>

1. Delete connections from persistent storage

1. Delete related key pairs from Android Keystore
    ```kotlin
        KeyStoreManager.deleteKeyPairs(connectionGuids)
    ```

### Get Authorizations list

1. Send request
    ```kotlin
        AuthenticatorApiManager.getAuthorizations(
            connectionsAndKeys = connectionsAndKeysToGet, 
            resultCallback = object : FetchAuthorizationsResult {
                override fun onFetchAuthorizationsResult(result: List<EncryptedAuthorizationData>, 
                                                         errors: List<ApiErrorData>) {                   
                    //process errors or process encrypted authorizations result 
                } 
            }
        )
    ```

1. Decrypt received encrypted authorization data. For decrypt should be used related PrivateKey 
(encryptedAuthorization.connectionId -> Connection.guid -> PrivateKey)
    ```kotlin
        CryptoTools.decryptAuthorizationData(encryptedAuthorization, rsaPrivateKey)
    ```

1. Show decrypted Authorizations list to user
  
### Poll authorizations list

For periodically fetch of authorizations list use AuthorizationsPollingService.  
Poll period is set to 2 seconds. For preventing memory leak application should start/stop polling depending on application components lifecycle (e.g. Activity's onStart() and onStop())  

1. Create polling service
    ```kotlin
        pollingService = AuthenticatorApiManager.createAuthorizationsPollingService()
    ```

1. Start polling service
    ```kotlin
        pollingService.contract = object : FetchAuthorizationsContract { }
        pollingService.start()
    ```

1. Stop polling service
    ```kotlin
        pollingService.contract = null
        pollingService.stop()
    ```

### get authorization by ID

1. Send request
    ```kotlin
        AuthenticatorApiManager.getAuthorization(
            connectionAndKey = connectionAndKeyToGet,
            authorizationId = requiredAuthorizationId,
            resultCallback = object : FetchAuthorizationResult {
                override fun fetchAuthorizationResult(result: EncryptedAuthorizationData?, 
                                                      error: ApiErrorData?) {                   
                    //process error or process encrypted authorization result
                } 
            }
        )
    ```

1. Decrypt authorization data. For decrypt use related PrivateKey 
(encryptedAuthorization.connectionId -> Connection.guid -> PrivateKey)
    ```kotlin
        CryptoTools.decryptAuthorizationData(encryptedAuthorization, rsaPrivateKey)
    ```

1. Show decrypted Authorization to user

### Poll authorization by ID

For periodically fetch of single authorization use SingleAuthorizationPollingService.  
Poll period is set to 2 seconds. For preventing memory leak application should start/stop polling depending on application components lifecycle (e.g. Activity's onStart() and onStop())  

1. Create polling service
    ```kotlin
        pollingService = AuthenticatorApiManager.createSingleAuthorizationPollingService()
    ```

1. Start service
    ```kotlin
        pollingService.contract = object : FetchAuthorizationContract { }
        pollingService.start()
    ```

1. Stop service
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
        resultCallback = object : ConfirmAuthorizationResult {
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
        resultCallback = object : ConfirmAuthorizationResult {
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

  
___
Copyright © 2019 Salt Edge. https://www.saltedge.com  
