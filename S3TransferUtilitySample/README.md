Running S3TransferUtility Sample
=============================================
This sample demonstrates how to use the high-level class TransferUtility to perform download and upload tasks and manage the tasks.  You can also follow the [tutorial](https://github.com/awslabs/aws-sdk-android-samples/blob/master/S3TransferUtilitySample/S3TransferUtilityTutorial.md) for a step-by-step guide in understanding the S3TransferUtilitySample.

1. **Create a identity pool**
   * Go to [Amazon Cognito Console](https://console.aws.amazon.com/cognito/) and choose `Manage Identity Pools`. 
   * Click `Create new Identity pool` button on the top left of the console.
   * Give a name for the Identity pool and check `Enable access to unauthenticated identities` under the `Unauthenticated Identities` section, click `Create pool` button on the bottom right.
   * To enable Cognito Identities to access your resources, expand the `View Details` section to see the two roles that are to be created. Make a note of the `unauth` role whose name is of the form `Cognito_<IdentityPoolName>Unauth_Role`. Now click `Allow` button in the bottom right of the console.
   * Under `Get AWSCredentials` section, in the code snippet to create `CognitoCachingCredentialsProvider `, find the Identity pool ID and the AWS region and make note of them. You will need to add to the sample application later.

2. **Set up permissions**
   * Go to [Amazon IAM Console](https://console.aws.amazon.com/iam/home) and select "Roles".
   * Select the `unauth` role you just created in step 1, which is of the form `Cognito_<IdentityPoolName>Unauth_Role`.
   * Select `Attach Policy`, then find `AmazonS3FullAccess` and attach it it to the role.
   * Note:  This will grant users in the identity pool full access to all buckets and operations in S3.  In a real app, you should restrict users to only have access to the resources they need.
   
3. **Create a bucket**
   * Go to [Amazon S3 Console](https://console.aws.amazon.com/s3/home) and click `Create bucket`.
   * Enter a name for the bucket that is DNS-compliant. For restrictions on bucket naming refer [Bucket Restrictions and Limitations](http://docs.aws.amazon.com/AmazonS3/latest/dev/BucketRestrictions.html).
   * Choose the region that you want the bucket to be created.
   * Click `Create`. Note the name and the region of the bucket that was created.

4. **Import the sample project**
   * Import the sample as Android project into your IDE of choice.
   * Open `awsconfiguration.json` in /res/raw directory.
   * Update `PoolId` with the ID of the Cognito Identity Pool created in Step-1.
   * Update `Region` with the region of the Cognito Identity Pool created from Step-1. For example, `us-east-1`. The `Region` column in [Amazon Cognito Identity Regions](http://docs.aws.amazon.com/general/latest/gr/rande.html#cognito_identity_region) represents the region string.
     ```
     "CredentialsProvider": {
       "CognitoIdentity": {
         "Default": {
           "PoolId": "REPLACE_ME",
           "Region": "REPLACE_ME"
         }
       }
     },
     ```
   * Update `Bucket` with the name of the S3 Bucket created in Step-3.
   * Update `Region` with the region of the S3 Bucket created from Step-3. For example, `us-east-1`. The `Region`column in [Amazon S3 Regions](http://docs.aws.amazon.com/general/latest/gr/rande.html#s3_region) represents the region string.
     ```
     "S3TransferUtility": {
       "Default": {
         "Bucket": "REPLACE_ME",
         "Region": "REPLACE_ME"
       }
     }
     ```

5. **Import the AWS SDK for Android**
   * This application used `Gradle` to resolve the dependencies required by the app and fetches from maven. This app depends on `aws-android-sdk-s3` in `build.gradle`.
   
6. Build and Run the sample
