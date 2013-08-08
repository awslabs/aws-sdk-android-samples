Running the S3_SimpleDB_SNS_SQS_DemoTVMIdentity Sample
======================================================
This sample demonstrates interaction with the IdentityTVM.  The IdentityTVM requires the user to register with the 
App by first connecting to an external website and provide a username and password.  The username and username will 
then be required to log into the sample App.  In this sample the registration website is a specific page on the 
IdentityTVM.  

It is assumed that you were able to previously run the S3_SimpleDB_SNS_SQS_Demo sample.  Also, you need to be running an 
IdentityTVM which this sample App will connect to.  

To run this specific sample you will need to do the following:

1. Import the project into Eclipse 
   * Go to File -> Import.  Import Wizard will open.
   * Select General -> Existing Projects into Workspace.  Click Next.
   * In Select root directory, browse to samples directory.  List of all samples projects will appear.
   * Select the projects you want to import
   * Click Finish.

2. Update your App configuration:
   * Open the AwsCredential.properties file located in src/com/amazonaws/demo/identity.
   * Edit the file and provide:
     + The DNS domain name where your Token Vending Machine is running (ex: tvm.elasticbeanstalk.com)
     + The App Name you configured your Token Vending Machine with (ex: MyMobileAppName)
     + Set useSSL to "true" or "false" based on whether your configured your Token Vending Machine with SSL or not.

3. Run the project:
   * Go to Project ->  Clean.
   * Go to Project ->  Build All.
   * Go to Run -> Run.
