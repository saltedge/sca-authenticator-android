# Authenticator Android SDK  

Authenticator Android SDK - is a module for connecting to Salt Edge Authenticator API of Bank (Service Provider) System, that implements Strong Customer Authentication/Dynamic Linking process.  

You can find source code of Authenticator Identity Service here: for [Authenticator Identity Service iOS](identity_service_repo).   

## How Authenticator system works

Read Wiki docs about [Authenticator Identity Service](wiki) API and workflow.

## Prerequisites
In order to use Authenticator SDK it is necessary to install the following tools on the local machine:

* JDK 8
* Android SDK
* Android Studio

## How to add SDK module to your project

1. Fork this repository
1. Import module `File/New/Import Module...` in your project
1. Build and run application on target device or emulator

Don't forget that module requires Kotlin v.1.3.+ plugin installed 
and minimal supported Android SDK is SDK21 (Android Lollipop)

or you can add SDK as dependency:

1. Add maven repository to application build.gradle
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
implementation ('com.saltedge.sdk:saltedge-library:3.2.0@aar') {
       transitive = true
   }

## How to use

Authenticator SDK provide next main data models:    
 * [Connection](#connection-model)
 * [ConnectionAndKey](#connectionandkey-model)
 * [Encrypted Authorization model](#encrypted-authorization-model)
 * [Decrypted Authorization model](#decrypted-authorization-model)
  
Authenticator SDK provide next features:  
* [Create connection (Link Bank flow)](#create-connection)
* [Remove connection (Remove Bank)](#remove-connection)
* [Fetch authorizations list](#fetch-authorizations-list)
* [Poll authorizations list](#poll-authorizations-list)
* [Fetch authorization by ID](#fetch-authorization-by-id)
* [Poll authorization by id](#poll-authorization-by-id)
* [Confirm authorization](#confirm-authorization)
* [Deny authorization](#deny-authorization)
  
### Data models
  
#### Connection model
 * `guid` **[string]** - Alias to RSA keypair in Keystore
 * `id` **[string]** - Unique id received from Authenticator API
 * `name` **[string]** - Provider's name from ProviderData
 * `code` **[string]** - Provider's code
 * `logoUrl` **[string]** - Provider's logo url. May be empty
 * `connectUrl` **[string]** - Base url of Authenticator API
 * `accessToken` **[string]** - Access token for accessing Authenticator API resources
 * `status` **[string]** - Connection Status. ACTIVE or INACTIVE
 
 #### ConnectionAndKey model
 In most request it is used custom model `ConnectionAndKey, which wraps Connection and related PrivateKey.
 
 #### Encrypted Authorization model
 - `id` **[string]** - a unique code of authorization action  
 - `connection_id` **[string]** - a unique ID of Mobile Client (Service Connection). Used to decrypt models in the mobile application
 - `iv` **[string]** - an initialization vector of encryption algorithm, this string is encrypted with public key linked to mobile client
 - `key` **[string]** - an secure key of encryption algorithm, this string is encrypted with public key linked to mobile client
 - `algorithm` **[string]** - encryption algorithm and block mode type
 - `data` **[string]** - encrypted authorization payload with algorithm mentioned above
 
 #### Decrypted Authorization model
 * `id` **[string]** - a unique id of authorization action
 * `connection_id` **[string]** - a unique ID of Connection. Used to decrypt models in the mobile application
 * `title` **[string]** - a human-readable title of authorization action
 * `description` **[string]** - a human-readable description of authorization action
 * `authorization_code` **[string]** - a unique code for each operation (e.g. payment transaction), specific to the attributes of operation, must be used once
 * `created_at` **[datetime]** - time when the authorization was created
 * `expires_at` **[datetime]** - time when the authorization should expire

### Create connection

1. Scan QR code

1. Extract qr code content (deep link)
```authenticator://saltedge.com/connect?configuration=https://example.com/configuration```

1. Extract configuration url from deep link.  
Use String extension from `DeepLinkTools.kt`  
    ```kotlin
        val configurationUrl: String? = "deep link string".extractConnectConfigurationLink()
    ```

1. Fetch Provider Data from configuration url
    ```kotlin
        authenticatorApiManager.getProviderData(configurationUrl, resultCallback = object : FetchProviderDataResult {
          override fun fetchProviderResult(result: ProviderData?, error: ApiErrorData?) {
            // process result or error
          }
        })
    ```

1. Validate Provider Data
    ```kotlin
        providerData.isValid()
    ```

1. Create `Connection` model and create RSA key pair with `connection.guid` alias.
    ```kotlin
        KeyStoreManager.createOrReplaceRsaKeyPair(connection.guid)
    ```

1. Post `Connection` data and receive authorization url (`connect_url`)
    ```kotlin
        AuthenticatorApiManager.initConnectionRequest(
                baseUrl = connection.connectUrl,
                publicKey = publicKeyAsPemEncodedString,
                pushToken = firebaseCloudMessagingToken,
                resultCallback = object : ConnectionInitResult() {
                    override fun onConnectionInitSuccess(response: AuthenticateConnectionData) {
                        // process success response
                        // open response.connect_url on WebView
                    }
    
                    override fun onConnectionInitFailure(error: ApiErrorData) {
                        // handle error response
                    }
                }
        )
    ```

1. Set `ConnectWebViewClient` as webViewClient of WebView and load `authentication_url`
    ```kotlin
        val webViewClient = ConnectWebClient(contract = object : ConnectWebClientContract { })
        webView.webViewClient = webViewClient
        webView.loadUrl(authentication_url)
    ```

1. Wait for `webAuthFinishSuccess(url)`
    ```kotlin
        private val webViewClient = ConnectWebClient(contract = object : ConnectWebClientContract {
            override fun webAuthFinishSuccess(id: ConnectionID, accessToken: Token) {
                // save access token  
            }
    
            override fun webAuthFinishError(errorClass: String, errorMessage: String?) {
                // handle error result
                // remove previously created key pairs from KeyStore
            }
        })
    ```
 
1. Set `accessToken` to `Connection` and save `Connection` to persistent storage (e.g. Realm, SQLite).

That's all, you have connection to Bank (Service Provider).

### Remove Connection

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

### Fetch authorizations list

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

1. Decrypt authorization data. For decrypt you need PrivateKey 
(encryptedAuthorization.connectionId -> Connection.guid -> PrivateKey)
    ```kotlin
        CryptoTools.decryptAuthorizationData(encryptedAuthorization, rsaPrivateKey)
    ```

1. Show decrypted Authorizations list to user
  
### Poll authorizations list

For periodically fetch of authorizations list use AuthorizationsPollingService.  
Poll period is set to 2 seconds.

1. Create polling service
    ```kotlin
        pollingService = AuthenticatorApiManager.createAuthorizationsPollingService()
    ```

1. Start service
    ```kotlin
        pollingService.contract = object : FetchAuthorizationsContract { }
        pollingService.start()
    ```

1. Stop service
    ```kotlin
        pollingService.contract = null
        pollingService.stop()
    ```

### Fetch authorization by ID

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

1. Decrypt authorization data. For decrypt you need PrivateKey 
(encryptedAuthorization.connectionId -> Connection.guid -> PrivateKey)
    ```kotlin
        CryptoTools.decryptAuthorizationData(encryptedAuthorization, rsaPrivateKey)
    ```

1. Show decrypted Authorization to user

### Poll authorization by ID

For periodically fetch of single authorization use SingleAuthorizationPollingService.  
Poll period is set to 2 seconds.

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
User can confirm authorization
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
User can deny authorization
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
