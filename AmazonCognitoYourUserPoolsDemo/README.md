
Running AmazonCognitoYourUserPoolsDemo 
======================================

1. Create Your user pool on the Cognito console
   - Go to https://console.aws.amazon.com/cognito/ , the console contains step-by-step instructions to create the pool.
   - Click on "Manage your User Pools" to open Your User Pools browser.
   - Click on "Create a User Pool" to open "Create a user pool" page, here you can start creating a user pool.
   - In "Create a user pool" page, give your pool a name and select "Review default" - this will create a user pool with default settings.
   - Click on "Create pool" to create the new user pool.
   - After creating a new pool, navigate to "Apps" page (select "Apps" from the navigation options on the left hand side).
   - Click "Add an app" and give a name to the app, e.g. "My Android App".
   - Click "Create app" to generate the app client id.
   - Get the App client id and App client secret, if the secret was generated.
   - Get the "Pool Id" from the "Pool details" page.

2. Download and import the AmazonCognitoYourUserPoolsDemo project into your Android Studio
   - From the Welcome screen, click on "Import project".
   - Browse to the AmazonCognitoYourUserPoolsDemo directory and click OK.
   - Accept requests to add Gradle to the project.
   - If the SDK reports missing Android SDK packages (such as Build Tools or the Android API package), import AWS SDK.
      
3. Modify the demo to run it on your user pool.
   - Open the file AppHelper.java from the project files.
   - Locate these three variables and add your pool details: 
      * userPoolId set this to your pool id.
      * clientId set this to your app client id.
      * clientSecret set this to your app client secret associated with the app client id. If your app client id does not have an associated client secret, set this variable to null, i.e. clientSecret = null.

4. You are now ready to run this demo.