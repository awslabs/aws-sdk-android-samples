aws-sdk-android-samples
=======================

This repository has samples that demonstrate various aspects of the [AWS Mobile SDK Version 2 for Android](http://aws.amazon.com/sdkforandroid), you can get the [source on Github](https://github.com/aws/aws-sdk-android-v2).  To find the AWS Mobile SDK Version 1 for Android samples, please select the v1 branch.

Please refer to README file in each folder for more specific instructions. For
general issues and help, check the <a href="#help">help</a> section

### List of Samples

#### [CognitoSyncDemo](CognitoSyncDemo/README.md)
* This is a sample mobile application that demonstrates how to use Amazon Cognito.
    * AWS Services involved:
      + Amazon Cognito Identity
      + Amazon Cognito Sync

#### [S3_TransferManager](S3_Transfer_Manager/README.md)
* This is a sample mobile application that demonstrates how to use Amazon S3 Transfer Manager to download and upload files to Amazon S3 
    * AWS Services involved:
      + Amazon S3
      + Amazon Cognito
      
#### [DynamoDBMapper](DynamoDBMapper_UserPreference_Cognito/README.md)
* This is a sample mobile application that demonstrates how to use Amazon DynamoDB Object Mapper.
    * AWS Services involved:
      + Amazon Cognito Identity
      + Amazon DynamoDB
     
<a name="help"></a>
###Help
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
