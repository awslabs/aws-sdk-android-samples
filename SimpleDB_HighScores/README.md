Running the HighScores Sample
============================================
This sample demonstrates how Android can interact with Amazon SimpleDB to store a high score list.  For a detailed description of the code, open and read the HighScores.html file.

1. Import the project into Eclipse 
   * Go to File -> Import.  Import Wizard will open.
   * Select General -> Existing Projects into Workspace.  Click Next.
   * In Select root directory, browse to samples directory.  List of all samples projects will appear.
   * Select the projects you want to import
   * Click Finish.

2. Copy the AWS SDK for Android jar (aws-android-sdk-X.X.X-debug.jar) into the libs directory for the project. 

2. Update your App configuration:
   * Open the Constants.java file located in src/com/amazonaws/demo/highscores.
   * Edit the file and provide your AWS Credentials.

3. Run the project:
   * Go to Project ->  Clean.
   * Go to Project ->  Build All.
   * Go to Run -> Run.



