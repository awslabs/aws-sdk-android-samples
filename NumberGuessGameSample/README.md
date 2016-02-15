Running the NumberGuessingGame Sample
=============================================
This sample demonstrates how to use AWS mobile services including Cognito Identity, Cognito Sync, DynamoDB, Mobile Analytics and Login with Amazon.

1. Create a Cognito identity pool
   * Go to https://console.aws.amazon.com/cognito/ and create a new identity pool. Make sure to enable access to unauthenticated identities. On the next screen, you will be asked to assign IAM roles to your identities, choose "Don't Allow". We'll updated this in step 3 later.
   * Download the starter code at the last step of the wizard.
   * In your starter code, you got your Identity Pool ID. You will use it later.

2. Use CloudFormation to setup DynamoDB, IAM Rols and IAM Policies automatically
   * Open file cloudformationSetup.template in the sample project. In "Mappings":"PoolId":"Id", change the value of "MypoolId" to the identity pool id you just got from step 1.
   * Go to https://console.aws.amazon.com/cloudformation/ and create a stack with cloudformationSetup.template you just modified. Remember your stack name because your IAM Roles' names will starts with your stack name.
   * Wait a few minutes for the process to complete.

3. Update Cognito identity pool roles
   * Go to https://console.aws.amazon.com/cognito/ and select the pool you created.
   * Click "Edit identity pool" on the top right corner.
   * Click "Unauthenticated role", choose "[your stack name]-unauthRole-XXXXXXXXXXX" from drop down list.
   * Click "Authenticated role", choose "[your stack name]-authRole-XXXXXXXXXXX" from drop down list.
   * Save changes.

4. Configure Mobile Analytics service
   * Go to https://console.aws.amazon.com/mobileanalytics/ and click "Add an App" in the top dropdown box.
   * Enter a name for the sample and you will get a App ID which will be used later.

5. Import the sample project
   * Import the sample as Android project into Eclipse.
   * Open com.amazonaws.demo.Constants.java.
   * Update "IDENTITY\_POOL\_ID" with the value you got from step 1.
   * Update "MOBILE\_ANALYTICS\_APP\_ID" with the value you got from step 3.

6. Add support for Login with Amazon
   * Follow the instructions at https://login.amazon.com/android to register a new application. Make sure that the package name is the same as in the AndroidManifest.xml and signature is the same as your debug.keystore's MD5 fingerprint.
   * After creating successfully, you will get an App ID for Login with Amazon and an API Key Value.
   * Go to edit your identity pool which was created in step 1. In "Public Identity Providers" section, enter your Amazon App ID.
   * Copy and paste the API key to assets/api\_key.txt in sample project.

7. Build the sample. You have two options:<br/>
   A) If you have Gradle installed, just go to the root directory of the sample and run command "gradle build".<br/>
   B) Otherwise, you can download the newest AWS Android SDK from http://aws.amazon.com/mobile/sdk/ and copy jars into libs directory. Include the following jars under libs/debug directory (for release purposes, you can include the release jars under libs/release directory)
      + aws-android-sdk-core-x.x.x.jar
      + extras/aws-android-sdk-cognito-x.x.x.jar
      + aws-android-sdk-ddb-mapper-x.x.x.jar
      + aws-android-sdk-ddb-x.x.x.jar
      + aws-android-sdk-mobileanalytics-x.x.x.jar<br/>
    Then you can click Run->Run in Eclipse.

8. Everything is ready, just run the sample app. Besides the functionality you can see in the app, you can also open https://console.aws.amazon.com/mobileanalytics/ to watch your custom events.
