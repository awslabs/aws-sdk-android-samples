#S3 Transfer Manager Demo

###Set up Cognito
First we will need to set up Cognito, which is the recommended way of 
providing authentication. Do this through the AWS Console.

###Set up permissions
Now that you've set up Cognito, we need to set up the IAM permissions.

1. Go to https://console.aws.amazon.com/iam/home
2. Select Roles
3. Press the "Create New Role" button
4. Choose a name for your role, and then press "Continue"
5. Under "AWS Service Roles", choose "Amazon EC2". (As of this writing there is
no template for using Cognito, so we can actually just choose any that let us 
use a custom policy)
6. Select "Custom Policy"
7. Fill in a name of your choosing for "Policy Name", and then enter this into
the "Policy Document" (Replacing YOUR_BUCKET with your unique bucket's name)
```
<pre>
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": ["s3:GetObject", "s3:PutObject"],
      "Resource": "arn:aws:s3:::<YOUR_BUCKET>/${cognito-identity.amazonaws.com:sub}/*"
    },{
      "Effect": "Allow",
      "Action": "s3:ListBucket",
      "Resource": "arn:aws:s3:::<YOUR_BUCKET>",
      "Condition": 
      {"StringLike": 
        {"s3:prefix":"${cognito-identity.amazonaws.com:sub}/*"}
      }
    }
  ]
}
</pre>
```
8. Select "Create Role"
9. Finally, select the role you just made and go to the "Trust Relationships"
tab
10. Click "Edit Trust Relationship"
11. Replace the "Policy Document" with the following
```
<pre>
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "",
      "Effect": "Allow",
      "Principal": {
        "Federated": "cognito-identity.amazonaws.com"
      },
      "Action": "sts:AssumeRoleWithWebIdentity"
    }
  ]
}
</pre>
```
12. Select "Update Policy" to apply the changes
13. Take note of the "Role ARN" under the "Summary" tab; we'll need this later

###Set up S3
Go to the S3 console from your browser, and make a new bucket, naming it
something of your choosing.

###Setting up the project
You'll need to set up the project for your environment. If you are going to
build the project from command line, run the command

    android update project -p <path_to_project> -t android-10

assuming android is already in your path. If not, you can find it in
"<path_to_sdk>/tools".

###Changing Constants
Before you try to compile the app, you'll first need to change the values in
com.amazonaws.demo.s3_transfer_manager.Constants to match the things on your
specific AWS account. 

Change all of these to be the corresponding values on your account

    public static final String AWS_ACCOUNT_ID = "YOUR_ACCOUNT_ID";
    public static final String COGNITO_POOL_ID = 
            "YOUR_COGNITO_POOL_ID"
    public static final String COGNITO_ROLE_UNAUTH = 
            "YOUR_COGNITO_ROLE_UNAUTH";
    public static final String BUCKET_NAME = "YOUR_BUCKET_NAME";

###Compiling
Now that you have done all of that, you can compile the app. To do this from the
command line, just run

    ant debug

from the root project directory, assuming you have ant installed. After 
compiling, you can install by running

    ant installd

also from the root directory. You can do compile and install all at once with

    ant debug install

as well.
