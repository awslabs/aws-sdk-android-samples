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

1. This sample requires Cognito to authorize to AWS IoT in order to access device shadows. Use Amazon Cognito to create a new identity pool:
    1. In the [Amazon Cognito Console](https://console.aws.amazon.com/cognito/), select`Create Identity Pool`.
    1. Ensure`Enable access to unauthenticated identities` is checked. This allows the sample application to assume the unauthenticated role associated with this identity pool.
    
        **Important**: see note below on unauthenticated user access.
        
    1. Obtain the `PoolID` constant.  This will be used in the application.
    1. As part of creating the identity pool Cognito will setup two roles in [Identity and Access Management (IAM)](https://console.aws.amazon.com/iam/home#roles).  These will be named something similar to:`Cognito_PoolNameAuth_Role` and`Cognito_PoolNameUnauth_Role`.
    1. Now we will attach a policy to the unauthenticated role which has permissions to access the required AWS IoT APIs.  This is done by first creating an IAM Policy in the [IAM Console](https://console.aws.amazon.com/iam/home#policies) and then attaching it to the unauthenticated role.  Below is an example policy which can be used with the sample application.  This policy allows the application to get and update the two thing shadows used in this sample.

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
              "Resource": [
                "arn:aws:iot:<REGION>:<ACCOUNT ID>:thing/TemperatureStatus",
                "arn:aws:iot:<REGION>:<ACCOUNT ID>:thing/TemperatureControl"
              ]
            }
          ]
        }
    	```

        More information on AWS IAM roles and policies can be found [here](http://docs.aws.amazon.com/IAM/latest/UserGuide/access_policies_manage.html).  More information on AWS IoT policies can be found [here](http://docs.aws.amazon.com/iot/latest/developerguide/authorization.html).
    
        **Note**: to keep this example simple it makes use of unauthenticated users in the identity pool.  This can be used for getting started and prototypes but unauthenticated users should typically only be given read-only permissions in production applications.  More information on Cognito identity pools including the Cognito developer guide can be found [here](http://aws.amazon.com/cognito/).

1. Open the TemperatureControl project.

1. Open `MainActivity.java` and update the following constants with the appropriate values:

	```
	CUSTOMER_SPECIFIC_ENDPOINT = "<CHANGE_ME>";
	COGNITO_POOL_ID = "<CHANGE_ME>";
	MY_REGION = Regions.US_EAST_1;
	```

1. Install the [AWS IoT JavaScript SDK for Embedded Devices](https://github.com/aws/aws-iot-device-sdk-js).

1. Follow the instructions in the AWS IoT JavaScript SDK for Embedded Devices to install depenedencies for the temperature-control example application.

1. Start the AWS IoT JavaScript SDK for Embedded Devices temperature-control example application using `--test-mode=2` to simulate a temperature control device.

1. Build and run the sample app.

1. Refreshing the UI of the sample application is done by clicking the circular refresh button in the upper-right corner.
