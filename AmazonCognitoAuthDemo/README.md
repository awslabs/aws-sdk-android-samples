# Running this sample

1. Create your user pool on the Cognito console
   - Follow the steps outlined [here](https://github.com/awslabs/aws-sdk-android-samples/blob/master/AmazonCognitoYourUserPoolsDemo/README.md#running-amazoncognitoyouruserpoolsdemo) to create a user pool.
   - Configure the App Client as follows:
      - Choose __App client settings__ from the navigation bar on the left-side of the console page.
      - Select __Cognito User Pool__ as one of the __Enabled Identity Providers__.
      - Type a __callback URL__ for the Amazon Cognito authorization server to call after users are authenticated. For the sample app set it to _myapp://_
      - Set __Sign out URL(s)__ to _myapp://_
      - You can enable both the __Authorization code grant__ and the __Implicit code grant__ under __Allowed OAuth flows__.
      - Unless you specifically want to exclude one, select the check boxes for all of the __Allowed OAuth scopes__.
      - Choose __Save changes__.
   - Configure a user pool domain
      - On the Domain name page, type a domain prefix that's available.
      - Make a note of the complete domain address.
      - Choose __Save changes__.

2. Download and import the AmazonCognitoAuthDemo project into your Android Studio
   - From the Welcome screen, click on "_Import project_".
   - Browse to the AmazonCognitoAuthDemo directory and click OK.
   - Accept requests to add Gradle to the project.
   - If the SDK reports missing Android SDK packages (such as Build Tools or the Android API package), import relevant Android SDKs.
      
3. Modify the demo to run it on your user pool.
   - Open the file [strings.xml](app/src/main/res/values/strings.xml) file.
   - Fill in the values for the following string resources : 
      * __cognito_web_domain__ set this to the user pool domain set above. It must be of the form _foo.auth.us-east-1.amazoncognito.com_ without the preceding _http://_ or _https://_
      * __cognito_client_id__ set this to your app client id obtained above.
      * __cognito_client_secret__ set this to your app client secret associated with the app client id.
      * __app_redirect__ set this to be same as callback URL for your app client(i.e. _myapp://_).

4. You are now ready to run this demo.


# Amazon Cognito Auth SDK for Android
You can now use Amazon Cognito Auth to easily add sign-in and sign-out to your mobile apps. Your User Pool in Amazon Cognito is a fully managed user directory that can scale to hundreds of millions of users, so you don't have to worry about building, securing, and scaling a solution to handle user management and authentication.

# Introduction
The Amazon Cognito Auth SDK for Android simplifies adding sign-up, sign-in functionality in your apps.<br/>
With this SDK, you can use Cognito User Pools’ app integration and federation features, with a customizable UI hosted by AWS to sign up and sign in users, and with built-in federation for external identity providers via SAML.<br/>
To learn more see our [Developer Guide](http://docs.aws.amazon.com/cognito/latest/developerguide/what-is-amazon-cognito.html). <br/>

If you are looking for our SDK to access all user APIs for Cognito User Pools, see the [Android Cognito Identity Provider SDK ](https://github.com/aws/aws-sdk-android/tree/master/aws-android-sdk-cognitoidentityprovider).

# Using Amazon Cognito Auth Android SDK
Find the Android sample for this SDK at the [GitHub Repository](https://github.com/awslabs/aws-sdk-android-samples/tree/master/AmazonCognitoAuthDemo).

## Dependencies
Add the following dependencies to your `app/build.gradle`.
<br/>

***AWS Android Cognito Auth*** The SDK with sign-in and sign-up functions `aws-android-sdk-cognitoauth`
```
dependency {
  implementation ('com.amazonaws:aws-android-sdk-cognitoauth:2.9.+@aar') { transitive = true }
}
```
To add other AWS Android SDK's in your app read the [Guide for AWS Android Mobile SDK](http://docs.aws.amazon.com/mobile/sdkforandroid/developerguide/setup.html).
<br/>

***Chrome Custom Tabs*** This SDK opens Cognito's hosted webpage's on Chrome.<br/>
```
dependency {
  implementation 'com.android.support:customtabs:25.0.0+'
}
```
**Note** Chrome is required on the Android device to use this SDK.

***Maven Central Repo***

To use the latest versions of aws-android-sdk-cognitoauth, you will need to add `mavenCentral()` to the list of repositories in your top-level `build.gradle` file.

## Instantiate Cognito Auth
Create a new instance of `Auth` with the following userpool settings.

**cognitoAuthWebDomain** The Cognito's authentication domain. <br/>This domain will point to the hosted UI pages. This domain can be created in userpool settings.<br/>This is just the domain name without the scheme. The SDK will always use *https*. <br/>**e.g.** foo.auth.us-east-1.amazoncognito.com

**cognitoClientId** The app client ID can be found by navigating to the `App client settings` in the Cognito UserPools console.

**signInRedirectUri** The Fully Qualified Domain Name (FQDN) to which Cognito must redirect to after authentication.<br/>
This must include the scheme. **e.g.** myApp://www.myApp.com

**signOutRedirectUri** The Fully Qualified Domain Name (FQDN) to which Cognito must redirect to after logout.<br/>This must also include the scheme.

```java
Auth.Builder builder = new Auth.Builder();
builder.setAppClientId(cognitoClientId)
       .setAppCognitoWebDomain(cognitoAuthWebDomain)
       .setSignInRedirect(signInRedirectUri)
       .setSignOutRedirect(signOutRedirectUri);
```
Set a callback for Cognito Auth that will be invoked after successful authentication or on failure.
```java
class callback implements AuthHandler {
    @Override
    public void onSuccess(AuthUserSession session) {
      // This will invoked to return tokens on successful authentication or when valid tokens are available locally.
      // 'session' will contain valid tokens for the user.
    }

    @Override
    public void onSignout() {
      // This will be invoked on successful sign-out.
    }

    @Override
    public void onFailure(Exception e) {
      // This will be invoked when error conditions. Probe the exception to get the exception details.
    }
}
```
Assign the `callback` to `Auth`.
```java
builder.setAuthHandler(new callback());
```
Create an instance for Cognito Auth.
```java
Auth cognitoAuth = builder.build();
```
## AndroidManifest
### Add intent-filter
Add an `intent-filter` in the app manifest to allow Android to invoke your app `Activity` after successful authentication and signout.<br/>
```xml
<intent-filter>
      <action android:name="android.intent.action.VIEW" />
      <category android:name="android.intent.category.DEFAULT" />
      <category android:name="android.intent.category.BROWSABLE" />
      <data android:host="YOUR_REDIRECT_URI_AUTHORITY"android:scheme="YOUR_REDIRECT_SCHEME"/>
</intent-filter>
```
### Add permissions
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```
## Get Session
Use the `getSession` API to authenticate a user and get tokens for the user.<br/>
```java
cognitoAuth.getSession();
```
This will open the Cognito login webpage and prompt the user to authenticate.
If valid tokens are already available for the last authenticated user, the tokens are returned from the `callback`.

The Cognito login webpage is launched on ***Chrome Custom Tabs***. After the user successfully authenticates and on redirect to the set `signInRedirectUri`, the Android will invoke the app `Activity` which has registered an `intent-filter` for `signInRedirectUri`.

Pass this redirect URI to the SDK to get the tokens for the user.<br/>
```java
cognitoAuth.getTokens(getIntent().getData());
```
## Sign out
This will sign out the current user by clearing cached tokens.<br/>
```java
cognitoAuth.signOut();
```
# Developer Feedback
We welcome developer feedback on this project. You can reach us by creating an issue on the GitHub repository or posting to the Amazon Cognito Identity forums and the below blog post:<br/>
* [GitHub Repository](https://github.com/aws/aws-sdk-android/tree/master/aws-android-sdk-cognitoauth)
* [AWS Forums](https://forums.aws.amazon.com/index.jspa)
