# Create Certificate with CSR Sample

This sample demonstrates use of the AWS IoT APIs to create an AWS IoT certificate.  It does so by creating a Certficate Signing Request on the device and submits this to AWS IoT for signing.  It uses Cognito authentication in conjunction with AWS IoT to create an identity (client certificate and private key) and store it in a Java keystore.  After the keystore is created the application loads it on future runs.  To run this flow again, the keystore can be manually deleted by removing the keystore file through the adb shell.

## Requirements

* AndroidStudio or Eclipse
* Android API 10 or greater

## Using the Sample

1. Import the CreateCertWithCSR project into your IDE.
   - If you are using Android Studio:
      * From the Welcome screen, click on "Import project".
      * Browse to the CreateCertWithCSR directory and press OK.
	  * Accept the messages about adding Gradle to the project.
	  * If the SDK reports some missing Android SDK packages (like Build Tools or the Android API package), follow the instructions to install them.
   - If you are using Eclipse:
      * Go to File -> Import. Import Wizard will open.
      * Select General -> Existing Projects into Workspace. Click Next.
      * In Select root directory, browse to the samples directory.
      * Select the CreateCertWithCSR project to import.
      * Click Finish.
	  
1. Import the libraries :
   - If you use Android Studio, Gradle will take care of downloading these dependencies for you.
   - If you use Eclipse, you will need to download the AWS SDK for Android (http://aws.amazon.com/mobile/sdk/) and extract and copy these jars into the 'libs' directory for the project:
      * aws-android-sdk-core-X.X.X.jar
      * aws-android-sdk-iot-X.X.X.jar

1. In the [Amazon Cognito console](https://console.aws.amazon.com/cognito/), use Amazon Cognito to create a new identity pool. Obtain the `PoolID` constant. Make sure the [role](https://console.aws.amazon.com/iam/home?region=us-east-1#roles) has full permissions to access the AWS IoT APIs, as shown in this example:

	```
	{
	    "Version": "2012-10-17",
	    "Statement": [
	        {
	            "Effect": "Allow",
	            "Action": [
	                "iot:*"
	            ],
	            "Resource": "*"
	       }
	    ]
	}
	```

1. Open the CreateCertWithCSR project.

1. Open `MainActivity.java` and update the following constants with the appropriate values:

	```
	COGNITO_POOL_ID = "<COGNITO POOL ID GOES HERE>";
	MY_REGION = Regions.US_EAST_1;
	KEYSTORE_NAME = "iot_keystore";
	KEYSTORE_PASSWORD = "password";
	CERTIFICATE_ID = "default";
	```
	The Cognito pool ID and Region will need to be updated to reflect the values in your account.  For the others the default values will work, however you can update them to reflect your setup.

1. Build and run the sample app.

1. When the app starts it checks for the presence of a keystore on the device.  If the keystore is not present it enables the Create Certificate button to allow the user to create a CSR and submit it to the service.

Note: This application requires the SpongyCastle (BouncyCastle) library to create the signing request.
