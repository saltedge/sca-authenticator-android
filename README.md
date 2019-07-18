[![GPLv3 license](https://img.shields.io/badge/License-GPLv3-blue.svg)](http://perso.crans.org/besson/LICENSE.html)
[![Twitter URL](https://img.shields.io/twitter/url/https/twitter.com/fold_left.svg?style=social&label=Follow%20%40saltedge)](http://twitter.com/saltedge)

<br />
<p align="center">
  <img src="docs/authenticator_android_logo.png" alt="Logo" width="80" height="80">
  <h3 align="center">
    <a href="https://www.saltedge.com/products/strong_customer_authentication">
    Salt Edge Authenticator App - Strong Customer Authentication Solution
    </a>
  </h3>
  <p align="center">
    <br />
    <a href="https://github.com/saltedge/sca-identity-service-example/wiki"><strong>Explore our Wiki »</strong></a>
    <br />
    <br />
  </p>
</p>

# Authenticator Android Client  

Authenticator Android Client - is a mobile client of Authenticator API of Bank (Service Provider) System, that implements Strong Customer Authentication/Dynamic Linking process.  
The purpose of Authenticator Android Client is to add possibility to authorize required actions for end-user.

You can install app from 
<a href='https://play.google.com/store/apps/details?id=com.saltedge.authenticator'>
    <img src='https://upload.wikimedia.org/wikipedia/commons/thumb/c/cd/Get_it_on_Google_play.svg/1000px-Get_it_on_Google_play.svg.png' alt='Get it on Google Play' height="64px"/>
</a> 

Minimal supported Android SDK is SDK23 (Android Marshmallow)   

You can find source code of Authenticator here: 
  * [Authenticator Identity Service](https://github.com/saltedge/sca-identity-service-example).
  * [Authenticator iOS](https://github.com/saltedge/sca-authenticator-ios).  

## Project content

Project contains 3 main modules: 
* `app` - UI layer and UX flow;
* `authenticator_sdk` - Salt Edge Authenticator API interactions and utility tools, 
    See [SDK Description](/authenticator_sdk/README.md);
* `rasp` - set of tools for Runtime Application Self-Protection checks. 

## How Authenticator system works

Read about [Authenticator Identity Service API](https://github.com/saltedge/sca-identity-service-example/blob/master/docs/IDENTITY_SERVICE_API.md)  
and [Android Authenticator workflow](docs/WORKFLOW.md).

## Prerequisites
In order to build Salt Edge Authenticator for Android locally, it is necessary to install the following tools on the local machine:

* JDK 8
* Android SDK
* Android Studio

## How to build locally

1. Fork this repository
2. Import project as `Gradle project` in Android Studio
3. Add `google-services.json` configuration file to `/app` directory
4. Add `signing.properties` configuration file with release signing credentials for auto sign. (see: `signing.example.properties`) 
5. Build and run application on target device or emulator

## Contribute

In the spirit of [free software][free-sw], **everyone** is encouraged to help [improve this project](./CONTRIBUTING.md).

* [Contributing Rules](./CONTRIBUTING.md)  

[free-sw]: http://www.fsf.org/licensing/essays/free-sw.html

## Contact us

Feel free to [contact us](mailto:authenticator@saltedge.com)

## License

***Salt Edge Authenticator (SCA solution) is multi-licensed, and can be used and distributed:***
- under a GNU GPLv3 license for free (open source). See the [LICENSE](LICENSE.txt) file.
- under a proprietary (commercial) license, to be used in closed source applications. 
  
[More information about licenses](https://github.com/saltedge/sca-identity-service-example/wiki/Multi-license).  
  
***Additional permission under GNU GPL version 3 section 7***  
If you modify this Program, or any covered work, by linking or combining it with [THIRD PARTY LIBRARY](THIRD_PARTY_NOTICES.md) (or a modified version of that library), containing parts covered by the [TERMS OF LIBRARY's LICENSE](THIRD_PARTY_NOTICES.md), the licensors of this Program grant you additional permission to convey the resulting work. {Corresponding Source for a non-source form of such a combination shall include the source code for the parts of [LIBRARY](THIRD_PARTY_NOTICES.md) used as well as that of the covered work.}        
  
___
Copyright © 2019 Salt Edge. https://www.saltedge.com  

