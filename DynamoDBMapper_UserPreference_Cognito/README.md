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
   * Update the ACCOUNT_ID, IDENTITY_POOL_ID, TEST_TABLE_NAME, and UNAUTH_ROLE_ID fields in
Constants.java which you can find in src/com/amazonaws/demo/userpreferencesom

For information on setting up Amazon Cognito for authentication please visit our [Getting started guide](http://docs.aws.amazon.com/mobile/sdkforandroid/developerguide/cognito-auth.html).

3. Run the project:
   * Go to Project ->  Clean.
   * Go to Project ->  Build All.
   * Go to Run -> Run.
