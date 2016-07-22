# Android PubSub with WebSockets Sample

This sample demonstrates use of the AWS IoT APIs to securely publish to and subscribe from MQTT topics with a WebSocket. Authentication of the WebSocket connection is done with Amazon Cognito. Once a connection to the AWS IoT platform has been established, the application presents a simple UI to publish and subscribe over MQTT.

## Requirements

* AndroidStudio or Eclipse
* Android API 10 or greater

## Using the Sample

1. Import the AndroidPubSubWebSocket project into your IDE.
   - If you are using Android Studio:
      * From the Welcome screen, click on "Import project".
      * Browse to the AndroidPubSubWebSocket directory and press OK.
      * Accept the messages about adding Gradle to the project.
      * If the SDK reports some missing Android SDK packages (like Build Tools or the Android API package), follow the instructions to install them.
   - If you are using Eclipse:
      * Go to File -> Import. Import Wizard will open.
      * Select General -> Existing Projects into Workspace. Click Next.
      * In Select root directory, browse to the samples directory.
      * Select the AndroidPubSubWebSocket project to import.
      * Click Finish.
      
1. Import the libraries :
   - If you use Android Studio, Gradle will take care of downloading these dependencies for you.
   - If you use Eclipse, you will need to download the AWS SDK for Android (http://aws.amazon.com/mobile/sdk/) and extract and copy these jars into the 'libs' directory for the project:
      * aws-android-sdk-core-X.X.X.jar
      * aws-android-sdk-iot-X.X.X.jar
      
1. This sample requires Cognito to authorize to AWS IoT and establish a WebSocket connection. Use Amazon Cognito to create a new identity pool:
    1. In the [Amazon Cognito Console](https://console.aws.amazon.com/cognito/), select`Create Identity Pool`.
    1. Ensure`Enable access to unauthenticated identities` is checked. This allows the sample application to assume the unauthenticated role associated with this identity pool.
    
        **Important**: see note below on unauthenticated user access.
        
    1. Obtain the `PoolID` constant.  This will be used in the application.
    1. As part of creating the identity pool Cognito will setup two roles in [Identity and Access Management (IAM)](https://console.aws.amazon.com/iam/home#roles).  These will be named something similar to:`Cognito_PoolNameAuth_Role` and`Cognito_PoolNameUnauth_Role`.
    1. Now we will attach a policy to the unauthenticated role which has permissions to access the required AWS IoT APIs.  This is done by first creating an IAM Policy in the [IAM Console](https://console.aws.amazon.com/iam/home#policies) and then attaching it to the unauthenticated role.  Below is an example policy which can be used with the sample application.  This policy allows any client ID to connect and allows publishing, subscribing and receiving messages on the topic:`mytopic/mysubtopic`.

        ```
        {
          "Version": "2012-10-17",
          "Statement": [
            {
              "Effect": "Allow",
              "Action": [
                "iot:Connect"
              ],
              "Resource": [
                "*"
              ]
            },
            {
              "Effect": "Allow",
              "Action": [
                "iot:Publish",
                "iot:Subscribe",
                "iot:Receive"
              ],
              "Resource": [
                "arn:aws:iot:<REGION>:<ACCOUNT ID>:topic/mytopic/mysubtopic"
              ]
            }
          ]
        }
        ```

        More information on AWS IAM roles and policies can be found [here](http://docs.aws.amazon.com/IAM/latest/UserGuide/access_policies_manage.html).  More information on AWS IoT policies can be found [here](http://docs.aws.amazon.com/iot/latest/developerguide/authorization.html).

        **Note**: to keep this example simple it makes use of unauthenticated users in the identity pool.  This can be used for getting started and prototypes but unauthenticated users should typically only be given read-only permissions if used in production applications.  More information on Cognito identity pools including the Cognito developer guide can be found [here](http://aws.amazon.com/cognito/).

1. Open the AndroidPubSubWebSocket project.

1. Open `PubSubActivity.java` and update the following constants with the appropriate values:

    ```
    CUSTOMER_SPECIFIC_ENDPOINT = "<CHANGE_ME>";
    COGNITO_POOL_ID = "<CHANGE_ME>";
    MY_REGION = Regions.US_EAST_1;
    ```

1. Build and run the sample app.

1. The sample application will allow you to connect to the AWS IoT platform, and then publish or subscribe to a topic using MQTT.

