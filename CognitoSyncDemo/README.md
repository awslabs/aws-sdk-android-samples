Running the CognitoSyncDemo Sample
============================================
This sample demonstrates how to use Cognito Sync client library on Android. It supports Facebook Login, Login with Amazon, developer authenticated identities as well as Unauthenticated Identities.

1. Import the CognitoSyncDemo project into your IDE.
   - If you are using Eclipse:
      * Go to File -> Import. Import Wizard will open.
      * Select General -> Existing Projects into Workspace. Click Next.
      * In Select root directory, browse to the samples directory.
      * Select the CognitoSyncDemo project to import.
      * Click Finish.
   - If you are using Android Studio:
      * From the Welcome screen, click on "Import project".
      * Browse to the CognitoSyncDemo directory and press OK.
	  * Accept the messages about adding Gradle to the project.
	  * If the SDK reports some missing Android SDK packages (like Build Tools or the Android API package), follow the instructions to install them.
	  
2. Import the libraries :
   - If you use Eclipse, you will need to download the AWS SDK for Android (http://aws.amazon.com/mobile/sdk/) and extract and copy these jars into the 'libs' directory for the project:
      * aws-android-sdk-core-X.X.X.jar
      * aws-android-sdk-cognito-X.X.X.jar
   - If you use Android Studio, Gradle will take care of downloading these dependencies for you.

3. Import the Facebook SDK into the project. Note that while Facebook Login is not required to run the app, the Facebook SDK is still required to build it.
   - If you are using Eclipse, you will need to download the Facebook SDK, import it to the workspace and add it as a dependency of CognitoSyncDemo.
   - If you use Android Studio, again Gradle will do everything for you.

4. Update your App configuration for Cognito:
   * Make sure you have an identity pool created and configured at https://console.aws.amazon.com/cognito/ and you downloaded the starter code at the last step of the wizard.
   * Open CognitoSyncClientManager.java
   * Update "IDENTITY_POOL_ID", and "REGION" with the values from the starter code.
   * At this point you can run the sample if you have the support of unauthenticated identity configured in the identity pool.
     + Go to Project ->  Clean.
     + Go to Project ->  Build All.
     + Go to Run -> Run.
   * To support Facebook Login and Login with Amazon, continue with step 4 and step 5.

5. To add support for Facebook Login (Optional)
   * Follow the instructions at https://developers.facebook.com/docs/android/getting-started/ to create a Facebook app
     + For "Package Name", enter com.amazonaws.cognito.sync.demo
     + For "Class Name", enter com.amazonaws.cognito.sync.demo.MainActivity
     + You may also need to include a key hash
   * Make sure your identity pool is configured to support Facebook login by entering the Facebook app ID at https://console.aws.amazon.com/cognito/ from the previous step.
   * Import the Facebook SDK into Eclipse following https://developers.facebook.com/docs/android/getting-started/
   * Link to the Facebook SDK project and configure the Facebook app ID
     + Open the strings.xml file located in res/values
     + Update "facebook_app_id" with the app ID of the app you created
     + Open project properties and under Android remove the placeholder Facebook library "path/to/facebook/sdk" and add "FacebookSDK"
   * At this point you can run the sample with Facebook Login.

6. To add support for Login with Amazon. (Optional)
   * Follow the instructions at https://login.amazon.com/android to register a new application
     + For "Label", enter Cognito sync demo
     + For "Package Name", enter com.amazonaws.cognito.sync.demo
   * Make sure your identity pool is configured to support Login with Amazon by entering the Client ID at https://console.aws.amazon.com/cognito/ from the previous step.
   * Copy and paste the API key to assets/api_key.txt
   * If this isn't configured properly, the "Login with Amazon" button will be disabled in the sample app.
   
7. To add support for Developer authenticated identities using the Sample Cognito Developer Authentication application (Optional)
	* Follow the ReadMe instructions in [the server side application](https://github.com/awslabs/amazon-cognito-developer-authentication-sample) and set up the server application.
	* In the CognitoSyncClientManager class set the useDeveloperAuthenticatedIdentities boolean to 'true'. 
	* In the DeveloperAuthenticationProvider class :
		* Set the application endpoint received from the Amazon ElasticBeanStalk console.
		* Set the developer provider name in the to the one you set in Amazon Cognito console for your identity pool.
