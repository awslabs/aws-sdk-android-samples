# Temperature Control Sample

This sample demonstrates use of the AWS IoT device shadow APIs.  It works in conjunction with the Temperature Control Example Program in the [AWS IoT JavaScript SDK for Embedded Devices](https://github.com/aws/aws-iot-device-sdk-js).  This applicaiton uses AWS Cognito for authentication with AWS IoT.

## Requirements

* AndroidStudio or Eclipse
* Android API 10 or greater

## Using the Sample

1. Import the TemperatureControl project into your IDE.
   - If you are using Android Studio:
      * From the Welcome screen, click on "Import project".
      * Browse to the TemperatureControl directory and press OK.
	  * Accept the messages about adding Gradle to the project.
	  * If the SDK reports some missing Android SDK packages (like Build Tools or the Android API package), follow the instructions to install them.
   - If you are using Eclipse:
      * Go to File -> Import. Import Wizard will open.
      * Select General -> Existing Projects into Workspace. Click Next.
      * In Select root directory, browse to the samples directory.
      * Select the TemperatureControl project to import.
      * Click Finish.
	  
1. Import the libraries :
   - If you use Android Studio, Gradle will take care of downloading these dependencies for you.
   - If you use Eclipse, you will need to download the AWS SDK for Android (http://aws.amazon.com/mobile/sdk/) and extract and copy these jars into the 'libs' directory for the project:
      * aws-android-sdk-core-X.X.X.jar
      * aws-android-sdk-iot-X.X.X.jar

1. In the [Amazon Cognito console](https://console.aws.amazon.com/cognito/), use Amazon Cognito to create a new identity pool. Obtain the `PoolID` constant. Make sure the [role](https://console.aws.amazon.com/iam/home?region=us-east-1#roles) has Shadow permissions to access the AWS IoT APIs, as shown in this example:

	```
	{
	    "Version": "2012-10-17",
	    "Statement": [
	        {
	            "Effect": "Allow",
	            "Action": [
                    "iot:GetThingShadow",
                    "iot:UpdateThingShadow"
	            ],
	            "Resource": "*"
	       }
	    ]
	}
	```

1. In the [Amazon AWS IoT console](https://console.aws.amazon.com/iot/), create a policy with full permissions to access AWS IoT as shown in this example.  Select 'Create a Policy', fill in the 'Name' field, set 'Action' to 'iot:\*', set 'Resource' to '\*', and then click 'Create'.

1. Open the AndroidPubSub project.

1. Open `MainActivity.java` and update the following constants with the appropriate values:

	```
	CUSTOMER_SPECIFIC_ENDPOINT_PREFIX = "<CUSTOMER SPECIFIC ENDPOINT PREFIX>";
	COGNITO_POOL_ID = "<COGNITO POOL ID GOES HERE>";
	MY_REGION = Regions.US_EAST_1;
	```

1. Install the [AWS IoT JavaScript SDK for Embedded Devices](https://github.com/aws/aws-iot-device-sdk-js).

1. Follow the instructions in the AWS IoT JavaScript SDK for Embedded Devices to install depenedencies for the temperature-control example application.

1. Start the AWS IoT JavaScript SDK for Embedded Devices temperature-control example application using `--test-mode=2` to simulate a temperature control device.

1. Build and run the sample app.

1. Refreshing the UI of the sample application is done by clicking the circular refresh button in the upper-right corner.
