Running the AWSAndroidDemo Sample
============================================
This sample demonstrates how Android can interact with Amazon AWS.

1. Import the project into Eclipse 
   * Go to File -> Import.  Import Wizard will open.
   * Select General -> Existing Projects into Workspace.  Click Next.
   * In Select root directory, browse to samples directory.  List of all samples projects will appear.
   * Select the projects you want to import
   * Click Finish.
   
2. Copy the AWS SDK for Android jar (aws-android-sdk-X.X.X-debug.jar) into the libs directory for the project. 

3. Update your App configuration:
   * Open the AwsCredential.properties file located in src/com/amazonaws/demo.
   * Edit the file and provide your AWS Credentials.

4. Run the project:
   * Go to Project ->  Clean.
   * Go to Project ->  Build All.
   * Go to Run -> Run.
