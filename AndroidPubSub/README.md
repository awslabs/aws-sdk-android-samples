# Android PubSub Sample

This sample demonstrates use of the AWS IoT APIs to securely publish to and subscribe from MQTT topics.  It uses Cognito authentication in conjunction with AWS IoT to create an identity (client certificate and private key) and store it in a Java keystore.  This identity is then used to authenticate to AWS IoT.  Once a connection to the AWS IoT platform has been established, the application presents a simple UI to publish and subscribe over MQTT.  After certificate and private key have been added to the keystore the app will use these for future connections.

## Requirements

* AndroidStudio or Eclipse
* Android API 10 or greater

## Using the Sample

1. Import the AndroidPubSub project into your IDE.
   - If you are using Android Studio:
      * From the Welcome screen, click on "Import project".
      * Browse to the AndroidPubSub directory and press OK.
      * Accept the messages about adding Gradle to the project.
      * If the SDK reports some missing Android SDK packages (like Build Tools or the Android API package), follow the instructions to install them.
   - If you are using Eclipse:
      * Go to File -> Import. Import Wizard will open.
      * Select General -> Existing Projects into Workspace. Click Next.
      * In Select root directory, browse to the samples directory.
      * Select the AndroidPubSub project to import.
      * Click Finish.
      
1. Import the libraries :
   - If you use Android Studio, Gradle will take care of downloading these dependencies for you.
   - If you use Eclipse, you will need to download the AWS SDK for Android (http://aws.amazon.com/mobile/sdk/) and extract and copy these jars into the 'libs' directory for the project:
      * aws-android-sdk-core-X.X.X.jar
      * aws-android-sdk-iot-X.X.X.jar

1. This sample requires Cognito to authorize to AWS IoT in order to create a device certificate. Use Amazon Cognito to create a new identity pool:
	1. In the [Amazon Cognito Console](https://console.aws.amazon.com/cognito/), press the `Manage Federated Identities` button and on the resulting page press the `Create new identity pool` button.
	1. Give your identity pool a name and ensure that `Enable access to unauthenticated identities` under the `Unauthenticated identities` section is checked.  This allows the sample application to assume the unauthenticated role associated with this identity pool.  Press the `Create Pool` button to create your identity pool.

		**Important**: see note below on unauthenticated user access.

	1. As part of creating the identity pool, Cognito will setup two roles in [Identity and Access Management (IAM)](https://console.aws.amazon.com/iam/home#roles).  These will be named something similar to: `Cognito_PoolNameAuth_Role` and `Cognito_PoolNameUnauth_Role`.  You can view them by pressing the `View Details` button.  Now press the `Allow` button to create the roles.
	1. Save the `Identity pool ID` value that shows up in red in the "Getting started with Amazon Cognito" page, it should look similar to: `us-east-1:xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx" and note the region that is being used.  These will be used in the application code later.
    1. Now we will attach a policy to the unauthenticated role which has permissions to access the required AWS IoT APIs.  This is done by first creating an IAM Policy in the [IAM Console](https://console.aws.amazon.com/iam/home#roles) and then attaching it to the unauthenticated role.  Search for "pubsub" and click on the link for the unauth role.  Click on the "Add inline policy" button and add the following example policy which can be used with the sample application.  This policy allows the application to create a new certificate (including private key) as well as attach an existing policy to a certificate.

        ```
        {
          "Version": "2012-10-17",
          "Statement": [
            {
              "Effect": "Allow",
              "Action": [
                "iot:AttachPrincipalPolicy",
                "iot:CreateKeysAndCertificate"
              ],
              "Resource": [
                "*"
              ]
            }
          ]
        }
        ```

        More information on AWS IAM roles and policies can be found [here](http://docs.aws.amazon.com/IAM/latest/UserGuide/access_policies_manage.html).  More information on AWS IoT policies can be found [here](http://docs.aws.amazon.com/iot/latest/developerguide/authorization.html).

        **Note**: to keep this example simple it makes use of unauthenticated users in the identity pool.  This can be used for getting started and prototypes but unauthenticated users should typically only be given read-only permissions if used in production applications.  More information on Cognito identity pools including the Cognito developer guide can be found [here](http://aws.amazon.com/cognito/).

1. Note that the application does not actually create the AWS IoT policy itself, rather it relies on a policy to already be created in AWS IoT and then makes a call to attach that policy to the newly created certificate.  To create a policy in AWS IoT,
    1. Navigate to the [AWS IoT Console](https://console.aws.amazon.com/iot/home) and press the `Get Started` button.  On the resulting page click on `Secure` on the side panel and the click on `Policies`.
    1. Click on `Create a Policy`
    1. Give the policy a name.  Note this name as this is the string you will use in the application when making the attach policy API call.
    1. The policy should be created to allow connecting to AWS IoT as well as allowing publishing, subscribing and receiving messages on whatever topics you will use in the sample application.  Below is an example policy.  This policy allows access to all topics under your AWS IoT account.   To scope this policy down to specific topics specify them explicitly as ARNs in the resource section: `"Resource": "arn:aws:iot:<REGION>:<ACCOUNT ID>:topic/mytopic/mysubtopic"`.  Note that the first `topic` is an ARN specifer so this example actually specifies the topic `mytopic/mysubtopic`.
    1. To add this policy, click on `Advanced Mode` and replace the default policy with the following text and then click the `Create` button.

        ```
        {
          "Version": "2012-10-17",
          "Statement": [
            {
              "Effect": "Allow",
              "Action": "iot:Connect",
              "Resource": "*"
            },
            {
              "Effect": "Allow",
              "Action": [
                "iot:Publish",
                "iot:Subscribe",
                "iot:Receive"
              ],
              "Resource": "*"
            }
          ]
        }
        ```

1. Open the AndroidPubSub project.

1. Open `PubSubActivity.java` and update the following constants with the appropriate values:

    ```
    CUSTOMER_SPECIFIC_ENDPOINT = "<CHANGE_ME>";
    COGNITO_POOL_ID = "<CHANGE_ME>";
    AWS_IOT_POLICY_NAME = "CHANGE_ME";
    MY_REGION = Regions.US_EAST_1;
    KEYSTORE_NAME = "iot_keystore";
    KEYSTORE_PASSWORD = "password";
    CERTIFICATE_ID = "default";
    ```
    The customer specific endpoint (can be found on the IoT console settings page), Cognito pool ID, Region and AWS IoT policy name will need to be updated to reflect the values in your account.  The policy name is the name used when creating the IoT policy above.  For the other parameters the default values will work for this sample application.  The following describes these parameters in case they need to be updated going forward past this sample. The keystore name is the name used when writing the keystore file to the application's file directory.  The password is the password given to protect the keystore when written.  Certificate ID is the alias in the keystore for the certificate and private key entry.  If you end up creating a keystore off of the device you will need to update this to match the alias given when importing the certificate into the keystore.

1. Build and run the sample app.

1. The sample application will allow you to connect to the AWS IoT platform, and then publish or subscribe to a topic using MQTT.

Note: This application also contains commented-out code for acccessing a KeyStore that was deployed as a resource file as part of an APK.


### Creating a Keystore for Use with AWS IoT

It may be beneficial for your application to use an AWS IoT certificate and private key which were created off of the device.  The following instructions walk through the process of creating a keystore which can be placed on the filesystem of the device and accessed by the Android SDK.

The keytool command does not allow importing an existing private key into a keystore.  To work around this we first create a PKCS12 formatted keystore with the certficate and private key and then we convert it to a Java keystore using keytool.

#### Prerequsites

* OpenSSL
* Java Keytool Utility (available in the JDK, see [Keytool](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/keytool.html))
* BouncyCastle Provider Library (see [BouncyCastle Releases](http://www.bouncycastle.org/latest_releases.html))

#### Steps

1. Import certificate and private key into PKCS12 keystore.

        openssl pkcs12 -export -out <keystore name>.p12 -inkey <private key file>.pem -in <certificate file>.pem -name <alias name>
    
    The alias parameter defines the alias of the cert/key in the keystore.  This is used in the SDK to access the correct certificate and private key entry if the keystore contains more than one.  This command will prompt for a password.  This password will be the source password when converting to BKS in the following step.

1. Convert PKCS12 keystore to a BKS (BouncyCastle) keystore.

        keytool -importkeystore -srckeystore <keystore name>.p12 -srcstoretype pkcs12 -destkeystore <keystore name>.bks -deststoretype bks --provider org.bouncycastle.jce.provider.BouncyCastleProvider -–providerpath path/to/provider/jar/bcprov-jdk15on-146.jar

    This command will prompt for both a destination password and a source password.  The source password is the export password given in the previous step.  The destination password will be the password required to access the private key in the keystore going forward.  This password will be required inside your application when acccessing the keystore.  You can test the password in the next step.

1. List aliases in keystore to verify (optional).

        keytool -list -v -keystore <keystore name>.bks -storetype bks -storepass <keystore password> -provider org.bouncycastle.jce.provider.BouncyCastleProvider -providerpath path/to/provider/jar/bcprov-jdk15on-146.jar

1. Push to Android Emulator (optional).

        adb push <keystore name>.bks /data/user/0/your_app_dir_goes_here/files/<keystore name>

    The directory and filename used will depend on your use case.  Typically the application's files directory is in /data/user/0/<app namespace>/files/.  You may however choose to locate your keystore on removable media or another space on the filesystem.  The SDK allows for specifying the file path and name of the keystore so the choice is up to you.
