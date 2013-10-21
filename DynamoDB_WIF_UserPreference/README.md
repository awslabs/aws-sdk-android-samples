Running the UserPreference Sample
============================================
This sample demonstrates how Android can interact with Amazon DynamoDB to store user preferences. For a detailed description of the code, open and read the UserPreference.html file.

1. Import the project into Eclipse 
   * Go to File -> Import.  Import Wizard will open.
   * Select General -> Existing Projects into Workspace.  Click Next.
   * In Select root directory, browse to samples directory.  List of all samples projects will appear.
   * Select the projects you want to import
   * Click Finish.

2. Update your App configuration:
   * Open the Strings.xml file located in res/values
   * Edit the file and provide the following for each WIF provider:
     + "fb_app_id", "app_id" and "fb_role_arn" if using Facebook
     + "google_client_id" and "google_role_arn" if using Google+
     + "amzn_api_key" and "amzn_role_arn" if using Login With Amazon   
   * In the Login.java uncomment the Web Identity Federation providers you wish to use 
     
3. Link Facebook SDK: (This project was tested using Facebook SDK Version 3.0.1)
	* Download the Facebook SDK for Android from https://developers.facebook.com/docs/android/
	* Import the SDK as an existing project into your Eclipse workspace (File -> Import -> Existing Project into Workspace -> Root of Facebook SDK)
	* Right click on the project and click properties
	* On the left hand column click Android
	* Under Library click add, and add the Facebook SDK for Android 
	(The SDK must have already been downloaded and imported into your workspace as an Eclipse project)

4. Run the project:
   * Go to Project ->  Clean.
   * Go to Project ->  Build All.
   * Go to Run -> Run.
