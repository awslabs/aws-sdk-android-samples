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

    To access the service any certificate needs to have a policy associated.  This policy contols what actions are able to be performed by the client authenticating with that particualr certificate.  This sample application does not create its own policy, rather it assumes a policy has been created in the service.  When the application creates a certificate it makes a service call to associate the pre-existing policy with the newly created certificate.

1. In the [Amazon AWS IoT console](https://console.aws.amazon.com/iot/), create a policy with full permissions to access AWS IoT as shown in this example.  Select 'Create a Policy', fill in the 'Name' field, set 'Action' to 'iot:\*', set 'Resource' to '\*', and then click 'Create'.

1. Open the AndroidPubSub project.

1. Open `PubSubActivity.java` and update the following constants with the appropriate values:

    ```
    CUSTOMER_SPECIFIC_ENDPOINT_PREFIX = "<CUSTOMER SPECIFIC ENDPOINT PREFIX>";
    COGNITO_POOL_ID = "<COGNITO POOL ID GOES HERE>";
    AWS_IOT_POLICY_NAME = "<YOUR IOT POLICY GOES HERE>";
    MY_REGION = Regions.US_EAST_1;
    KEYSTORE_NAME = "iot_keystore";
    KEYSTORE_PASSWORD = "password";
    CERTIFICATE_ID = "default";
    ```
    The customer specific endpoint, Cognito pool ID, Region and AWS IoT policy name will need to be updated to reflect the values in your account.  For the others the default values will work, however you can update them to reflect your setup.

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
