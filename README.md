AWS Mobile SDK for Android Samples
=======================

This repository has samples that demonstrate various aspects of the [AWS Mobile SDK Version 2 for Android](http://aws.amazon.com/sdkforandroid), you can get the [source on Github](https://github.com/aws/aws-sdk-android-v2).  To find the AWS Mobile SDK Version 1 for Android samples, please select the v1 branch.

Please refer to README file in each folder for more specific instructions. For
general issues and help, check the <a href="#faqs">FAQs</a> section

### List of Samples

* [AmazonKinesisVideoDemoApp](AmazonKinesisVideoDemoApp/) This is a sample mobile application that demonstrates how to stream video to Amazon Kinesis Video Streaming. Involved AWS Services are:
  + Amazon Kinesis Video Streaming
  + Amazon Cognito Identity
  + Amazon Cognito Identity Provider (Your User Pools)

* [[DEPRECATED]CognitoSyncDemo]([DEPRECATED]CognitoSyncDemo/README.md). This is a sample mobile application that demonstrates how to use Amazon Cognito. Involved AWS Services are:
  + Amazon Cognito Identity
  + Amazon Cognito Sync

* [S3TransferUtilitySample](S3TransferUtilitySample/README.md). This is a sample mobile application that demonstrates how to use Amazon S3 Transfer Utility to download and upload files to Amazon S3. Involved AWS Services are:
  + Amazon Cognito Identity
  + Amazon S3
      
* [[DEPRECATED]DynamoDBMapper]([DEPRECATED]DynamoDBMapper_UserPreference_Cognito/README.md). This is a sample mobile application that demonstrates how to use Amazon DynamoDB Object Mapper. Involved AWS Services are:
  + Amazon Cognito Identity
  + Amazon DynamoDB
     
* [AndroidPubSub](AndroidPubSub/README.md). This sample demonstrates use of the AWS IoT APIs to securely publish to and subscribe from MQTT topics with TLS and IoT certificates. Involved AWS Services are:
  + Amazon Cognito Identity
  + AWS IoT

* [AndroidPubSubWebSocket](AndroidPubSubWebSocket/README.md). This sample demonstrates use of the AWS IoT APIs to securely publish to and subscribe from MQTT topics with WebSockets. Involved AWS Services are:
  + Amazon Cognito Identity
  + AWS IoT

* [CreateIotCertWithCSR](CreateIotCertWithCSR/README.md). This sample demonstrates use of the AWS IoT APIs to create an AWS IoT certificate. Involved AWS Services are:
  + Amazon Cognito Identity
  + AWS IoT

* [TemperatureControl](TemperatureControl/README.md). This sample demonstrates use of the AWS IoT device shadow APIs. Involved AWS Services are:
  + Amazon Cognito Identity
  + AWS IoT

* [PollyDemo](PollyDemo/README.md). This is a sample mobile application that demonstrates how to use Amazon Polly. Involved AWS Services are:
  + Amazon Cognito Identity
  + Amazon Polly

* [Lex Sample](LexSample/README.md). This is a sample mobile application that demonstrates how to use Amazon Lex. Involved AWS Services are:
  + Amazon Cognito Identity
  + Amazon Lex

### How to Run a Sample
#### Requirements
* Android SDK. You can install the SDK via Android Studio or as stand-alone tools. See [Installing the Android SDK](http://developer.android.com/sdk/installing/index.html).
* Your favorite IDE, either Android Studio or Eclipse. The former is the official IDE for Android. However all samples are backward compatible with Eclipse, though the setup is a little more complicated.
* Or Gradle if you prefer CLI.

#### Android Studio
This is the recommended way to run samples.
* Import sample project into Android Studio. In the welcome screen, click `Import project (Eclipse ADT, Gradle, etc.)`. Navigate to the sample directory and select a sample project to import.
* Update source code with your AWS resources. Please read the README of each sample for more details.
* Run it!

#### Gradle
* Make sure `ANDROID_HOME` environment variable is set to point to your Android SDK. See [Getting Started with Gradle](https://guides.codepath.com/android/Getting-Started-with-Gradle).
* Update source code with your AWS resources. Please read the README of each sample for more details.
* Connect an Android device to your computer or start an Android emulator. The minimum required API version for most samples is API level 10.
* Compile the sample and install it. Run `gradlew installDebug`. Or if you on a Windows computer, use `gradlew.bat` instead. 

#### Eclipse
* Import sample project. Via menu, click File -> New -> Other -> Android Project from Existing Code. Browse to the sample directory and select a sample project to import.
* Get AWS Mobile SDK for Android. You can download it from [AWS Mobile SDK](https://aws.amazon.com/mobile/sdk/) website. Extract it, and copy libraries to the `libs` folder of the sample. Please refer to the README of each sample for required libraries.
* Update source code with your AWS resources. Please read the README of each sample for more details.
* Run it!

### FAQs<a name="faqs"></a>
##### The sample can no longer be compiled/imported if I move it to another directory
* We typically include the dependencies in the repo and have them already
linked, so if you move the project you'll need to also update the path to the
dependency. To do this, modify the project.properties file of the project

##### How do I use a different version of an included library(such as the Facebook SDK)?
* Change the path to the library in project.properties of the project you are
building to be the path to the version you want

##### I'm getting an error saying that there are multiple versions of a jar
* To fix this, make sure that all the jars you are using conform to the same
version. You can do this by just replacing all the problematic jars with the
version you would like to use.

##### Where do I find login-with-amazon-sdk.jar?
* The jar is usually already included where required. However, if not or if you want to use a different version, you can find it <a href="https://developer.amazon.com/public/apis/engage/login-with-amazon/docs/install_sdk_android.html">here</a>.

### Getting Help

We use [AWS Android SDK GitHub issues](https://github.com/aws/aws-sdk-android/issues) for tracking questions, bugs, and feature requests.
