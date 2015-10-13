Running the UserPreference OM Sample
============================================
This sample demonstrates how Android can interact with Amazon DynamoDB to store user preferences. For a detailed description of the code, open and read the UserPreference.html file.

1. Import the project into Eclipse 
   * Go to File -> Import.  Import Wizard will open.
   * Select General -> Existing Projects into Workspace.  Click Next.
   * In Select root directory, browse to samples directory.  List of all samples projects will appear.
   * Select the projects you want to import
   * Click Finish.

2. Update your App configuration:
   * Make sure you have an identity pool created and configured at https://console.aws.amazon.com/cognito/ and you downloaded the starter code at the last step of the wizard.
   * Update the ACCOUNT_ID, IDENTITY_POOL_ID, TEST_TABLE_NAME, and UNAUTH_ROLE_ID fields in
Constants.java which you can find in src/com/amazonaws/demo/userpreferencesom
   * Note that you do not need to create this table online. The app will create the table, so enter any name.
   * You will need to update the permissions for the role you will use here. 
      * Go to [IAM](https://console.aws.amazon.com/iam/home), select the region in which your role was created, and select roles.
      * Select the appropriate role, then under the permissions tab, select attach role policy, and select Amazon DynamoDB Full Access.

For information on setting up Amazon Cognito for authentication please visit our [Getting started guide](http://docs.aws.amazon.com/mobile/sdkforandroid/developerguide/cognito-auth.html).

Note, the default region for this table (in AmazonClientManager) is US West 2, which is Oregon. If you wish to view this table visit the [DynamoDB console](https://console.aws.amazon.com/dynamodb/home?region=us-west-2#gs:)

3. Import the AWS SDK for Android
   * You can download the newest AWS Android SDK from http://aws.amazon.com/mobile/sdk/ and copy the jars into libs directory. Include the following jars
      + aws-android-sdk-core-X.X.X.jar
      + aws-android-sdk-ddb-X.X.X.jar
      + aws-android-sdk-ddb-mapper-X.X.X.jar

4. Run the project:
   * Go to Project ->  Clean.
   * Go to Project ->  Build All.
   * Go to Run -> Run.
