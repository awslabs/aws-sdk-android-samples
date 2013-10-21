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
   * Open the AwsCredential.properties file located in src/com/amazonaws/tvmclient.
   * Edit the file and provide:
     + The DNS domain name where your Token Vending Machine is running (ex: tvm.elasticbeanstalk.com)
     + Set useSSL to "true" or "false" based on whether your configured your Token Vending Machine with SSL or not.

3. Run the project:
   * Go to Project ->  Clean.
   * Go to Project ->  Build All.
   * Go to Run -> Run.
