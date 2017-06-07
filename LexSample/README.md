Running Lex Sample
=============================================
This sample demonstrates how to use Amazon Lex interaction client library on Android.  This application uses AWS Cognito for authentication with Amazon Lex.

## Requirements

* AndroidStudio
* Android API 11 or greater

## Using the Sample

1. Import the LexDemo project into Android Studio.
   - From the Welcome screen, click on "Import project".
   - Browse to the LexDemo directory and press OK.
   - Accept the messages about adding Gradle to the project.
   - If the SDK reports some missing Android SDK packages (like Build Tools or the Android API package), follow the instructions to install them.

1. Import the libraries :
   - Gradle will take care of downloading these dependencies for you.

1. This sample requires Cognito to authorize to Amazon Lex to post content.  Use Amazon Cognito to create a new identity pool:
    1. In the [Amazon Cognito Console](https://console.aws.amazon.com/cognito/), select `Create Identity Pool`.
    1. Ensure `Enable access to unauthenticated identities` is checked.  This allows the sample application to assume the unauthenticated role associated with this identity pool.

        **Important**: see note below on unauthenticated user access.

    1. Obtain the `PoolID` constant.  This will be used in the application.
    1. As part of creating the identity pool Cognito will setup two roles in [Identity and Access Management (IAM)](https://console.aws.amazon.com/iam/home#roles).  These will be named something similar to: `Cognito_PoolNameAuth_Role` and `Cognito_PoolNameUnauth_Role`.
    1. Now we will attach a policy to the unauthenticated role which has permissions to access the required Amazon Lex API.  This is done by first creating an IAM Policy in the [IAM Console](https://console.aws.amazon.com/iam/home#policies) and then attaching it to the unauthenticated role.  Below is an example policy which can be used with the sample application.  This policy allows the application to perform all operations on the Amazon Lex service.

        ```
        {
          "Version": "2012-10-17",
          "Statement": [
            {
              "Effect": "Allow",
              "Action": [
                "lex:postContent"
              ],
              "Resource": [
                "*"
              ]
            }
          ]
        }
        ```

        More information on AWS IAM roles and policies can be found [here](http://docs.aws.amazon.com/IAM/latest/UserGuide/access_policies_manage.html).  More information on Amazon Lex policies can be found [here](http://docs.aws.amazon.com/lex/latest/dg/access-control-managing-permissions.html).

        **Note**: to keep this example simple it makes use of unauthenticated users in the identity pool.  This can be used for getting started and prototypes but unauthenticated users should typically only be given read-only permissions in production applications.  More information on Cognito identity pools including the Cognito developer guide can be found [here](http://aws.amazon.com/cognito/).

1. Open the LexSample project.

1. Open `res/values/strings.xml` and update the values for Cognito Identity Pool ID, AWS region for Cognito Identity Pool Id and AWS region for Amazon Lex and the Amazon Lex Bot name and Bot Alias

1. Build and run the sample app.

1. Select Text Demo or Voice Demo as appropriate, and try out some of the sample utterance you have configured on the Amazon Lex console.
