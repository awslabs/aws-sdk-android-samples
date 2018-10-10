Introduction
============
This sample app will showcase using Cognito Your User Pools with the AWS Mobile SDK for Android. The sample has been updated to support the features introduced in GA.
The sample requires Android Studio and can run on Android SDK version 21 (Android 5) and above. Please follow the setup instructions to run this sample.
This sample app includes demonstrates how to sign-up new users, confirm a user, sign-in as a user, see/change/delete user details, change a password, change a user’s MFA settings, verify user attributes and see users remembered devices.

Running AmazonCognitoYourUserPoolsDemo 
======================================

1. Create Your user pool on the Cognito console
   - Go to https://console.aws.amazon.com/cognito/ , the console contains step-by-step instructions to create the pool.
   - Click on "_Manage your User Pools_" to open Your User Pools browser.
   - Click on "_Create a User Pool_" to open "_Create a user pool_" page, here you can start creating a user pool.
   - In "_Create a user pool_" page, give your pool a name and select "_Review default_" - this will create a user pool with default settings.
   - Click on "_Create pool_" to create the new user pool.
   - After creating a new pool, navigate to "_App Integration_" page (select "_Apps Integration_" from the navigation options on the left hand side) and click "_Add app client..._" from the "_UI Customization_" section.
   - Click "_Add an app client_" and give a name to the app, e.g. "My Android App".
   - Click "_Create app client_" to generate the app client id.
   - Get the App client id and App client secret, if the secret was generated. To see the App client secret click on "_Show Details_".
   - Get the "_Pool Id_" from the "_Pool details_" page (select "_General settings_" from the navigation options on the left hand side).

2. Download and import the AmazonCognitoYourUserPoolsDemo project into your Android Studio
   - From the Welcome screen, click on "_Import project_".
   - Browse to the AmazonCognitoYourUserPoolsDemo directory and click OK.
   - Accept requests to add Gradle to the project.
   - If the SDK reports missing Android SDK packages (such as Build Tools or the Android API package), import AWS SDK.
      
3. Modify the demo to run it on your user pool.
   - Open the file __AppHelper.java__ from the project files.
   - Locate these four variables and add your pool details: 
      * __userPoolId__ set this to your pool id.
      * __clientId__ set this to your app client id.
      * __clientSecret__ set this to your app client secret associated with the app client id. If your app client id does not have an associated client secret, set this variable to null, i.e. _clientSecret_ = _null_.
      * __cognitoRegion__ set this to AWS Cognito Your User Pools region.

4. You are now ready to run this demo.
